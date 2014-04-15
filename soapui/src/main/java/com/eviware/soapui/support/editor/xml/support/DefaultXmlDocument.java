/*
 * Copyright 2004-2014 SmartBear Software
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

package com.eviware.soapui.support.editor.xml.support;

import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.XmlBeans;

import com.eviware.soapui.support.xml.XmlUtils;

/**
 * Default XmlDocument that works on a standard xml string
 *
 * @author ole.matzura
 */

public class DefaultXmlDocument extends AbstractXmlDocument {
    private String xml;
    private SchemaTypeSystem typeSystem;

    public DefaultXmlDocument(String xml) {
        this.xml = xml;
    }

    public DefaultXmlDocument() {
    }

    public void setTypeSystem(SchemaTypeSystem typeSystem) {
        this.typeSystem = typeSystem;
    }

    public SchemaTypeSystem getTypeSystem() {
        if (typeSystem != null) {
            return typeSystem;
        }

        try {
            typeSystem = XmlUtils.createXmlObject(xml).schemaType().getTypeSystem();
            return typeSystem;
        } catch (Exception e) {
            return XmlBeans.getBuiltinTypeSystem();
        }
    }

    public String getXml() {
        return xml;
    }

    public void setXml(String xml) {
        String oldXml = this.xml;
        this.xml = xml;
        if ("<not-xml/>".equals(xml)) {
            fireXmlChanged("", xml);
        }

        fireXmlChanged(oldXml, xml);
    }

    public void release() {
        typeSystem = null;
    }
}
