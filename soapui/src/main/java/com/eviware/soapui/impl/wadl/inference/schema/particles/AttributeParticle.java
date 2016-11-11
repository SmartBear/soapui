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

package com.eviware.soapui.impl.wadl.inference.schema.particles;

import com.eviware.soapui.impl.wadl.inference.ConflictHandler;
import com.eviware.soapui.impl.wadl.inference.schema.Context;
import com.eviware.soapui.impl.wadl.inference.schema.Particle;
import com.eviware.soapui.impl.wadl.inference.schema.Schema;
import com.eviware.soapui.impl.wadl.inference.schema.Settings;
import com.eviware.soapui.impl.wadl.inference.schema.Type;
import com.eviware.soapui.inferredSchema.AttributeParticleConfig;
import com.eviware.soapui.inferredSchema.MapEntryConfig;
import org.apache.xmlbeans.XmlException;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents an xs:attribute, with a name, a type, etc.
 *
 * @author Dain Nilsson
 */
public class AttributeParticle implements Particle {
    private String name;
    private Schema schema;
    private Type type;
    private Map<String, String> attributes;

    public AttributeParticle(Schema schema, String name) {
        this.schema = schema;
        this.name = name;
        type = Type.Factory.newType(schema);
        attributes = new HashMap<String, String>();
    }

    public AttributeParticle(AttributeParticleConfig xml, Schema schema) {
        this.schema = schema;
        name = xml.getName();
        type = Type.Factory.parse(xml.getType(), schema);
        attributes = new HashMap<String, String>();
        for (MapEntryConfig entry : xml.getAttributeList()) {
            attributes.put(entry.getKey(), entry.getValue());
        }
    }

    public AttributeParticleConfig save() {
        AttributeParticleConfig xml = AttributeParticleConfig.Factory.newInstance();
        xml.setName(name);
        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            MapEntryConfig mapEntry = xml.addNewAttribute();
            mapEntry.setKey(entry.getKey());
            mapEntry.setValue(entry.getValue());
        }
        xml.setType(type.save());
        return xml;
    }

    public String getAttribute(String key) {
        String value = attributes.get(key);
        if (value == null) {
            value = "";
        }
        return value;
    }

    public QName getName() {
        return new QName(schema.getNamespace(), name);
    }

    public Type getType() {
        return type;
    }

    public void setAttribute(String key, String value) {
        attributes.put(key, value);
    }

    public void setType(Type type) {
        this.type = type;
    }

    public void validate(Context context) throws XmlException {
        context.getCursor().push();
        Type newType = type.validate(context);
        if (newType != type) {
            String problem = "Illegal value for attribute '" + name + "' with type '" + type.getName() + "'.";
            if (context.getHandler().callback(ConflictHandler.Event.MODIFICATION, ConflictHandler.Type.ATTRIBUTE,
                    getName(), context.getPath(), "Illegal value.")) {
                type = newType;
                context.getCursor().pop();
                validate(context);
                return;
            } else {
                throw new XmlException(problem);
            }
        }
        context.getCursor().pop();
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("<" + schema.getPrefixForNamespace(Settings.xsdns) + ":" + getPType()
                + " name=\"" + name + "\" type=\"");
        if (type.getSchema() != schema) {
            s.append(schema.getPrefixForNamespace(type.getSchema().getNamespace()) + ":");
        }
        s.append(type.getName() + "\"");
        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            s.append(" " + entry.getKey() + "=\"" + entry.getValue() + "\"");
        }
        s.append("/>");
        return s.toString();
    }

    public Particle.ParticleType getPType() {
        return Particle.ParticleType.ATTRIBUTE;
    }

}
