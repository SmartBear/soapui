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

package com.eviware.soapui.support.editor.xml.support;

import com.eviware.soapui.impl.wsdl.submit.transports.http.DocumentContent;
import com.eviware.soapui.support.xml.XmlUtils;
import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.XmlBeans;

import javax.annotation.Nonnull;

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

    @Override
    public void setDocumentContent(DocumentContent documentContent) {
        this.xml = documentContent.getContentAsString();
        if ("<not-xml/>".equals(documentContent.getContentAsString())) {
            fireContentChanged();
        }

        fireContentChanged();
    }

    public void release() {
        typeSystem = null;
    }

    @Nonnull
    @Override
    public DocumentContent getDocumentContent(Format format) {
        return new DocumentContent("text/xml", xml);
    }
}
