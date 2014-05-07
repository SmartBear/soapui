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

package com.eviware.soapui.impl.support.components;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.XmlBeans;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.submit.transports.http.HttpResponse;
import com.eviware.soapui.impl.wsdl.support.wsdl.WsdlContext;
import com.eviware.soapui.model.iface.Response;
import com.eviware.soapui.support.editor.xml.support.AbstractXmlDocument;

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

    public String getXml() {
        Response response = request.getResponse();
        return response == null ? null : response.getContentAsString();
    }

    public void setXml(String xml) {
        HttpResponse response = (HttpResponse) request.getResponse();
        if (response != null) {
            try {
                settingResponse = true;
                String oldXml = response.getContentAsString();
                response.setResponseContent(xml);
                fireXmlChanged(oldXml, xml);
            } finally {
                settingResponse = false;
            }
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (settingResponse) {
            return;
        }

        if (evt.getPropertyName().equals(WsdlRequest.RESPONSE_PROPERTY)) {
            Response oldResponse = (Response) evt.getOldValue();
            Response newResponse = (Response) evt.getNewValue();

            fireXmlChanged(oldResponse == null ? null : oldResponse.getContentAsString(), newResponse == null ? null
                    : newResponse.getContentAsString());
        }

        if (evt.getPropertyName().equals(WsdlRequest.RESPONSE_CONTENT_PROPERTY)) {
            String oldResponse = (String) evt.getOldValue();
            String newResponse = (String) evt.getNewValue();

            fireXmlChanged(oldResponse, newResponse);
        }
    }

    public SchemaTypeSystem getTypeSystem() {
        WsdlInterface iface = (WsdlInterface) request.getOperation().getInterface();
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
