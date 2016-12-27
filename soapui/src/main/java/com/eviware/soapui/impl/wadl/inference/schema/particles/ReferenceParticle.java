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

import com.eviware.soapui.impl.wadl.inference.schema.Context;
import com.eviware.soapui.impl.wadl.inference.schema.Particle;
import com.eviware.soapui.impl.wadl.inference.schema.Schema;
import com.eviware.soapui.impl.wadl.inference.schema.Settings;
import com.eviware.soapui.impl.wadl.inference.schema.Type;
import com.eviware.soapui.inferredSchema.MapEntryConfig;
import com.eviware.soapui.inferredSchema.ReferenceParticleConfig;
import org.apache.xmlbeans.XmlException;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.Map;

/**
 * A ReferenceParticle is a reference to a particle in another namespace. It may
 * be either an xs:element or an xs:attribute.
 *
 * @author Dain Nilsson
 */
public class ReferenceParticle implements Particle {
    private Schema schema;
    private Particle reference;
    private QName referenceQName;
    private Map<String, String> attributes;

    public ReferenceParticle(Schema schema, Particle reference) {
        this.schema = schema;
        this.reference = reference;
        referenceQName = reference.getName();
        attributes = new HashMap<String, String>();
    }

    public ReferenceParticle(ReferenceParticleConfig xml, Schema schema) {
        this.schema = schema;
        referenceQName = xml.getReference();
        attributes = new HashMap<String, String>();
        for (MapEntryConfig entry : xml.getAttributeList()) {
            attributes.put(entry.getKey(), entry.getValue());
        }
    }

    public ReferenceParticleConfig save() {
        ReferenceParticleConfig xml = ReferenceParticleConfig.Factory.newInstance();
        xml.setReference(referenceQName);
        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            MapEntryConfig mapEntry = xml.addNewAttribute();
            mapEntry.setKey(entry.getKey());
            mapEntry.setValue(entry.getValue());
        }
        return xml;
    }

    private Particle getReference() {
        if (reference == null) {
            reference = schema.getSystem().getSchemaForNamespace(referenceQName.getNamespaceURI())
                    .getParticle(referenceQName.getLocalPart());
        }
        return reference;
    }

    public QName getName() {
        return referenceQName;
    }

    public String getAttribute(String key) {
        String value = attributes.get(key);
        if ((key.equals("minOccurs") || key.equals("maxOccurs")) && value == null) {
            value = "1";
        }
        return value;
    }

    public void setAttribute(String key, String value) {
        attributes.put(key, value);
    }

    public Type getType() {
        return null;
    }

    public void setType(Type type) {
    }

    public void validate(Context context) throws XmlException {
        context.pushPath();
        getReference().validate(context);
        context.popPath();
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("<" + schema.getPrefixForNamespace(Settings.xsdns) + ":"
                + getReference().getPType() + " ref=\"" + schema.getPrefixForNamespace(referenceQName.getNamespaceURI())
                + ":" + referenceQName.getLocalPart() + "\"");
        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            s.append(" " + entry.getKey() + "=\"" + entry.getValue() + "\"");
        }
        s.append("/>");
        return s.toString();
    }

    public Particle.ParticleType getPType() {
        return getReference().getPType();
    }

}
