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

package com.eviware.soapui.impl.wadl.inference.schema;

import com.eviware.soapui.impl.wadl.inference.ConflictHandler;
import com.eviware.soapui.impl.wadl.inference.ConflictHandler.Event;
import com.eviware.soapui.impl.wadl.inference.schema.types.ComplexType;
import com.eviware.soapui.impl.wadl.inference.schema.types.EmptyType;
import com.eviware.soapui.inferredSchema.ComplexTypeConfig;
import com.eviware.soapui.inferredSchema.MapEntryConfig;
import com.eviware.soapui.inferredSchema.ParticleConfig;
import com.eviware.soapui.inferredSchema.SchemaConfig;
import com.eviware.soapui.support.StringUtils;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents an inferred schema for a single namespace.
 *
 * @author Dain Nilsson
 */
public class Schema {
    private SchemaSystem schemaSystem;
    private String namespace;
    private Map<String, String> prefixes;
    private Map<String, ComplexType> types;
    private List<Particle> particles;
    private EmptyType empty = new EmptyType(this);

    /**
     * Constructs a blank new Schema for the given namespace in the given
     * SchemaSystem.
     *
     * @param namespace    The namespace for the new schema.
     * @param schemaSystem The SchemaSystem in which to place the newly created Schema.
     */
    public Schema(String namespace, SchemaSystem schemaSystem) {
        this.schemaSystem = schemaSystem;
        this.namespace = namespace;
        prefixes = new HashMap<String, String>();
        particles = new ArrayList<Particle>();
        types = new HashMap<String, ComplexType>();
        putPrefixForNamespace("xs", Settings.xsdns);
    }

    /**
     * Constructs a Schema object using previously saved data.
     *
     * @param xml          The XmlObject to which data has previously been saved.
     * @param schemaSystem The SchemaSystem in which to place the newly created Schema.
     */
    public Schema(SchemaConfig xml, SchemaSystem schemaSystem) {
        this.schemaSystem = schemaSystem;
        namespace = xml.getNamespace();
        prefixes = new HashMap<String, String>();
        particles = new ArrayList<Particle>();
        types = new HashMap<String, ComplexType>();
        for (MapEntryConfig entry : xml.getPrefixList()) {
            prefixes.put(entry.getKey(), entry.getValue());
        }
        for (ParticleConfig item : xml.getParticleList()) {
            particles.add(Particle.Factory.parse(item, this));
        }
        for (ComplexTypeConfig item : xml.getComplexTypeList()) {
            types.put(item.getName(), new ComplexType(item, this));
        }
    }

    /**
     * Save the Schema to an XmlObject.
     *
     * @param xml A blank XmlObject to save to.
     */
    public void save(SchemaConfig xml) {
        xml.setNamespace(namespace);
        for (Map.Entry<String, String> entry : prefixes.entrySet()) {
            MapEntryConfig mapEntry = xml.addNewPrefix();
            mapEntry.setKey(entry.getKey());
            mapEntry.setValue(entry.getValue());
        }
        List<ParticleConfig> particleList = new ArrayList<ParticleConfig>();
        for (Particle item : particles) {
            particleList.add(item.save());
        }
        xml.setParticleArray(particleList.toArray(new ParticleConfig[0]));
        for (ComplexType item : types.values()) {
            item.save(xml.addNewComplexType());
        }
    }

    /**
     * Add a ComplexType to this Schema.
     *
     * @param type The ComplexType to be added.
     */
    public void addType(ComplexType type) {
        types.put(type.getName(), type);
        type.setSchema(this);
    }

    /**
     * Getter for the namespace of this Schema.
     *
     * @return The namespace of this Schema.
     */
    public String getNamespace() {
        return namespace;
    }

    /**
     * Gets the prefix used in this schema for a different namespace, if one
     * exists.
     *
     * @param namespace Another namespace to get the prefix for.
     * @return The prefix used for the given namespace.
     */
    public String getPrefixForNamespace(String namespace) {
        return prefixes.get(namespace);
    }

    /**
     * Set the prefix used in this schema for a different namespace.
     *
     * @param prefix    The prefix to be used.
     * @param namespace The namespace to use the prefix for.
     */
    public void putPrefixForNamespace(String prefix, String namespace) {
        prefixes.put(namespace, prefix);
    }

    /**
     * Get a Type contained in this schema by name.
     *
     * @param name The name of a contained Type.
     * @return Returns the Type, if one is found. Otherwise returns null.
     */
    public Type getType(String name) {
        return types.get(name);
    }

    /**
     * Create and add a new root element for this schema.
     *
     * @param name The name to give the newly created element.
     * @return Returns the newly created element.
     */
    public Particle newElement(String name) {
        Particle p = Particle.Factory.newElementInstance(this, name);
        particles.add(p);
        return p;
    }

    /**
     * Create and add a new global attribute for this schema.
     *
     * @param name The name to give the newly created attribute.
     * @return Returns the newly created attribute.
     */
    public Particle newAttribute(String name) {
        Particle p = Particle.Factory.newAttributeInstance(this, name);
        particles.add(p);
        return p;
    }

    public String toString() {
        StringBuilder s = new StringBuilder("<?xml version=\"1.0\" encoding=\"utf-8\" ?>" + "<"
                + getPrefixForNamespace(Settings.xsdns) + ":schema ");

        if (StringUtils.hasContent(namespace)) {
            s.append("targetNamespace=\"" + namespace + "\" " + "xmlns=\"" + namespace + "\" ");
        }

        for (Map.Entry<String, String> entry : prefixes.entrySet()) {
            s.append("xmlns:" + entry.getValue() + "=\"" + entry.getKey() + "\" ");
        }
        s.append("elementFormDefault=\"qualified\">");
        for (Particle item : particles) {
            s.append(item);
        }
        for (Type item : types.values()) {
            s.append(item);
        }
        if (s.toString().contains("type=\"" + empty.getName() + "\"")) {
            s.append(empty);
        }
        s.append("</" + getPrefixForNamespace(Settings.xsdns) + ":schema>");
        return s.toString();
    }

    /**
     * Validates an XML document contained in a given Context object.
     *
     * @param context A Context object containing the XML data to be validated, and
     *                other needed contextual variables.
     * @throws XmlException On unresolvable validation error.
     */
    public void validate(Context context) throws XmlException {
        XmlCursor cursor = context.getCursor();
        Particle root = getParticle(cursor.getName().getLocalPart());
        if (root == null) {
            if (context.getHandler().callback(Event.CREATION, ConflictHandler.Type.ELEMENT, cursor.getName(),
                    "/" + cursor.getName().getLocalPart(), "Undeclared root element.")) {
                root = newElement(cursor.getName().getLocalPart());
            } else {
                throw new XmlException("Illegal root element");
            }
        }

        root.validate(context);
    }

    /**
     * Getter for the SchemaSystem that contains this Schema.
     *
     * @return Returns the parent SchemaSystem.
     */
    public SchemaSystem getSystem() {
        return schemaSystem;
    }

    /**
     * Get a global particle by its name.
     *
     * @param name The name of the particle to get.
     * @return Returns the Particle if one is found. Otherwise returns null.
     */
    public Particle getParticle(String name) {
        for (Particle item : particles) {
            if (item.getName().getLocalPart().equals(name)) {
                return item;
            }
        }
        return null;
    }
}
