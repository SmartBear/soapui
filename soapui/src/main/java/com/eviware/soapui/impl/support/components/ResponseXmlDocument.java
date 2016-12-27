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

package com.eviware.soapui.impl.support.components;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.submit.transports.http.DocumentContent;
import com.eviware.soapui.impl.wsdl.submit.transports.http.HttpResponse;
import com.eviware.soapui.impl.wsdl.support.wsdl.WsdlContext;
import com.eviware.soapui.model.iface.Response;
import com.eviware.soapui.support.editor.xml.support.AbstractXmlDocument;
import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.XmlBeans;

import javax.annotation.Nonnull;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * XmlDocument for the response to a WsdlRequest
 *
 * @author ole.matzura
 */

public class ResponseXmlDocument extends AbstractXmlDocument implements PropertyChangeListener {
    private final WsdlRequest request;
    private boolean settingResponse;

    public ResponseXmlDocument(WsdlRequest request) {
        this.request = request;
        request.addPropertyChangeListener(this);
    }

    @Nonnull
    @Override
    public DocumentContent getDocumentContent(Format format) {
        Response response = request.getResponse();
        return new DocumentContent(response == null ? null : response.getContentType(), response == null ? null : response.getContentAsString());
    }

    @Override
    public void setDocumentContent(DocumentContent documentContent) {
        HttpResponse response = request.getResponse();
        if (response != null) {
            try {
                settingResponse = true;
                response.setResponseContent(documentContent.getContentAsString());
                fireContentChanged();
            } finally {
                settingResponse = false;
            }
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (settingResponse) {
            return;
        }

        if (evt.getPropertyName().equals(WsdlRequest.RESPONSE_PROPERTY)
                || evt.getPropertyName().equals(WsdlRequest.RESPONSE_CONTENT_PROPERTY)) {
            fireContentChanged();
        }
    }

    public SchemaTypeSystem getTypeSystem() {
        WsdlInterface iface = request.getOperation().getInterface();
        WsdlContext wsdlContext = iface.getWsdlContext();
        try {
            return wsdlContext.getSchemaTypeSystem();
        } catch (Exception e1) {
            SoapUI.logError(e1);
            return XmlBeans.getBuiltinTypeSystem();
        }
    }

    public void release() {
        request.removePropertyChangeListener(this);
    }
}
