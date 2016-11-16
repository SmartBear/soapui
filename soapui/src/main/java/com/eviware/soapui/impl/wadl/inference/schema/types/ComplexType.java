/*
 * SoapUI, Copyright (C) 2004-2016 SmartBear Software 
 *
 * Licensed under the EUPL, Version 1.1 or - as soon as they will be approved by the European Commission - subsequent 
 * versions of the EUPL (the "Licence"); 
 * You may not use this work except in compliance with the Licence. 
 * You may obtain a copy of the Licence at: 
 * 
 * http://ec.europa.eu/idabc/eupl 
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is 
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either 
 * express or implied. See the Licence for the specific language governing permissions and limitations 
 * under the Licence. 
 */

package com.eviware.soapui.impl.wadl.inference.schema.types;

import com.eviware.soapui.impl.wadl.inference.ConflictHandler;
import com.eviware.soapui.impl.wadl.inference.schema.Content;
import com.eviware.soapui.impl.wadl.inference.schema.Context;
import com.eviware.soapui.impl.wadl.inference.schema.Particle;
import com.eviware.soapui.impl.wadl.inference.schema.Schema;
import com.eviware.soapui.impl.wadl.inference.schema.Settings;
import com.eviware.soapui.impl.wadl.inference.schema.Type;
import com.eviware.soapui.impl.wadl.inference.schema.content.EmptyContent;
import com.eviware.soapui.inferredSchema.ComplexTypeConfig;
import com.eviware.soapui.inferredSchema.ParticleConfig;
import com.eviware.soapui.inferredSchema.TypeReferenceConfig;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ComplexType corresponds to an xs:complexType. It's definition is displayed in
 * the schema for which it belongs.
 *
 * @author Dain Nilsson
 */
public class ComplexType implements Type {
    private String name;
    private Schema schema;
    private Map<QName, Particle> attributes;
    private Content content;
    private boolean mixed = false;
    private boolean completed = false;

    public ComplexType(Schema schema, String name, boolean completed) {
        this.schema = schema;
        this.name = name;
        this.completed = completed;
        content = new EmptyContent(schema, completed);
        attributes = new HashMap<QName, Particle>();
        schema.addType(this);
    }

    public ComplexType(ComplexTypeConfig xml, Schema schema) {
        this.schema = schema;
        name = xml.getName();
        completed = xml.getCompleted();
        mixed = xml.getMixed();
        content = Content.Factory.parse(xml.getContent(), schema);
        attributes = new HashMap<QName, Particle>();
        for (ParticleConfig item : xml.getAttributeList()) {
            Particle p = Particle.Factory.parse(item, schema);
            attributes.put(new QName("", p.getName().getLocalPart()), p);
        }
        schema.addType(this);
    }

    public void save(ComplexTypeConfig xml) {
        xml.setName(name);
        xml.setCompleted(completed);
        xml.setMixed(mixed);
        List<ParticleConfig> particleList = new ArrayList<ParticleConfig>();
        for (Particle item : attributes.values()) {
            particleList.add(item.save());
        }
        xml.setAttributeArray(particleList.toArray(new ParticleConfig[0]));
        xml.setContent(content.save());
    }

    public TypeReferenceConfig save() {
        TypeReferenceConfig xml = TypeReferenceConfig.Factory.newInstance();
        xml.setReference(new QName(schema.getNamespace(), name));
        return xml;
    }

    public void setContent(Content content) {
        this.content = content;
    }

    public Type validate(Context context) throws XmlException {
        XmlCursor cursor = context.getCursor();
        List<QName> seen = new ArrayList<QName>();
        cursor.push();
        if (!mixed && isMixed(context)) {
            // TODO: Check with ConflictHandler
            mixed = true;
        }
        cursor.pop();
        cursor.push();
        if (cursor.toFirstAttribute()) {
            do {
                QName qname = cursor.getName();
                if (attributes.containsKey(qname)) {
                    attributes.get(qname).validate(context);
                } else if (qname.getNamespaceURI().equals(Settings.xsins)) {
                    // Ignore
                } else if (context.getHandler().callback(ConflictHandler.Event.CREATION, ConflictHandler.Type.ATTRIBUTE,
                        new QName(schema.getNamespace(), qname.getLocalPart()), context.getPath(), "Undeclared attribute.")) {
                    if (qname.getNamespaceURI().equals(schema.getNamespace()) || qname.getNamespaceURI().equals("")) {
                        newAttribute(qname).validate(context);
                    } else {
                        Schema otherSchema = context.getSchemaSystem().getSchemaForNamespace(qname.getNamespaceURI());
                        schema.putPrefixForNamespace(qname.getPrefix(), qname.getNamespaceURI());
                        if (otherSchema == null) {
                            otherSchema = context.getSchemaSystem().newSchema(qname.getNamespaceURI());
                        }
                        Particle ref = otherSchema.getParticle(qname.getLocalPart());
                        if (ref == null) {
                            ref = otherSchema.newAttribute(qname.getLocalPart());
                        }
                        if (completed) {
                            ref.setAttribute("use", "optional");
                        }
                        Particle newAttribute = Particle.Factory.newReferenceInstance(schema, ref);
                        attributes.put(qname, newAttribute);
                        newAttribute.validate(context);
                    }
                } else {
                    throw new XmlException("Illegal attribute!");
                }
                seen.add(qname);
            }
            while (cursor.toNextAttribute());
        }
        // Make sure all attributes have been accounted for
        for (QName item : attributes.keySet()) {
            if (!seen.contains(item) && !attributes.get(item).getAttribute("use").equals("optional")) {
                if (context.getHandler().callback(ConflictHandler.Event.MODIFICATION, ConflictHandler.Type.ATTRIBUTE,
                        item, context.getPath(), "Required attribute missing.")) {
                    attributes.get(item).setAttribute("use", "optional");
                } else {
                    throw new XmlException("Required attribute missing!");
                }
            }
        }
        cursor.pop();
        if (!cursor.toFirstChild()) {
            cursor.toFirstContentToken();
        }
        if (!context.getAttribute("nil").equals("true")) {
            validateContent(context);
        }
        completed = true;
        return this;
    }

    private void validateContent(Context context) throws XmlException {
        context.getCursor().push();
        context.putAttribute("typeName", name);
        Content newContent = content.validate(context);
        context.clearAttribute("typeName");
        if (content != newContent) {
            String problem = "Illegal content for complexType '" + name + "'.";
            if (context.getHandler().callback(ConflictHandler.Event.MODIFICATION, ConflictHandler.Type.TYPE,
                    new QName(schema.getNamespace(), name), context.getPath(), "Illegal complex content.")) {
                content = newContent;
                context.getCursor().pop();
                validateContent(context);
                return;
            } else {
                throw new XmlException(problem);
            }
        }
        context.getCursor().pop();
    }

    private boolean isMixed(Context context) {
        QName name = context.getCursor().getName();
        SchemaTypeSystem sts;
        try {
            sts = XmlBeans.compileXsd(new XmlObject[]{XmlObject.Factory
                    .parse("<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns=\"" + name.getNamespaceURI()
                            + "\" targetNamespace=\"" + name.getNamespaceURI() + "\">" + "<xs:element name=\""
                            + name.getLocalPart() + "\"><xs:complexType><xs:sequence>"
                            + "<xs:any processContents=\"skip\" minOccurs=\"0\" maxOccurs=\"unbounded\" /></xs:sequence>"
                            + "<xs:anyAttribute processContents=\"skip\"/></xs:complexType></xs:element></xs:schema>")},
                    XmlBeans.getBuiltinTypeSystem(), null);
            SchemaTypeLoader stl = XmlBeans
                    .typeLoaderUnion(new SchemaTypeLoader[]{sts, XmlBeans.getBuiltinTypeSystem()});
            if (!stl.parse(context.getCursor().xmlText(), null, null).validate()) {
                return true;
            }
        } catch (XmlException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }

    public String getName() {
        return name;
    }

    public Schema getSchema() {
        return schema;
    }

    public void setSchema(Schema schema) {
        this.schema = schema;
    }

    @Override
    public String toString() {
        String xsdns = schema.getPrefixForNamespace(Settings.xsdns);
        StringBuilder s = new StringBuilder("<" + xsdns + ":complexType name=\"" + name + "\"");
        if (mixed) {
            s.append(" mixed=\"true\"");
        }
        s.append(">");
        StringBuilder attrs = new StringBuilder();
        for (Particle item : attributes.values()) {
            attrs.append(item);
        }
        s.append(content.toString(attrs.toString()));
        s.append("</" + xsdns + ":complexType>");
        return s.toString();
    }

    private Particle newAttribute(QName qname) {
        Particle p = Particle.Factory.newAttributeInstance(schema, qname.getLocalPart());
        attributes.put(qname, p);
        if (completed) {
            p.setAttribute("use", "optional");
        }
        return p;
    }

}
