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

package com.eviware.soapui.support.editor.xml;

import com.eviware.soapui.impl.wsdl.submit.transports.http.DocumentContent;
import com.eviware.soapui.support.PropertyChangeNotifier;
import com.eviware.soapui.support.editor.EditorDocument;
import org.apache.xmlbeans.SchemaTypeSystem;

import javax.annotation.Nonnull;

/**
 * Document class used by XmlEditors
 *
 * @author ole.matzura
 */

public interface XmlDocument extends PropertyChangeNotifier, EditorDocument {
    public final static String XML_PROPERTY = XmlDocument.class.getName() + "@xml";
    //TODO: make this a real property?
    public final static String CONTENT_PROPERTY = XmlDocument.class.getName() + "@content";

    /**
     * Use #getDocumentContent instead.
     *
     * @return content as string, sometimes XML, sometimes JSON, sometimes something else
     */
    @Deprecated
    public String getXml();

    @Nonnull
    public DocumentContent getDocumentContent();

    public void setXml(DocumentContent documentContent);

    public SchemaTypeSystem getTypeSystem();

}
