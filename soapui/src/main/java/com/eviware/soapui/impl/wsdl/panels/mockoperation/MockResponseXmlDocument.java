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

package com.eviware.soapui.impl.wsdl.panels.mockoperation;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import com.eviware.soapui.model.mock.MockResponse;
import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.XmlBeans;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockResponse;
import com.eviware.soapui.impl.wsdl.support.wsdl.WsdlContext;
import com.eviware.soapui.support.editor.xml.support.AbstractXmlDocument;

/**
 * XmlDocument for a WsdlMockResponse
 *
 * @author ole.matzura
 */

public class MockResponseXmlDocument extends AbstractXmlDocument implements PropertyChangeListener {
    private final MockResponse mockResponse;

    public MockResponseXmlDocument(MockResponse response) {
        this.mockResponse = response;

        mockResponse.addPropertyChangeListener(WsdlMockResponse.RESPONSE_CONTENT_PROPERTY, this);
    }

    public SchemaTypeSystem getTypeSystem() {
        try {
            if (mockResponse instanceof WsdlMockResponse) {
                WsdlOperation operation = (WsdlOperation) mockResponse.getMockOperation().getOperation();
                if (operation != null) {
                    WsdlInterface iface = operation.getInterface();
                    WsdlContext wsdlContext = iface.getWsdlContext();
                    return wsdlContext.getSchemaTypeSystem();
                }
            }
        } catch (Exception e1) {
            SoapUI.logError(e1);
        }

        return XmlBeans.getBuiltinTypeSystem();
    }

    public String getXml() {
        return mockResponse.getResponseContent();
    }

    public void setXml(String xml) {
        mockResponse.setResponseContent(xml);
    }

    public void propertyChange(PropertyChangeEvent arg0) {
        fireXmlChanged((String) arg0.getOldValue(), (String) arg0.getNewValue());
    }

    @Override
    public void release() {
        mockResponse.removePropertyChangeListener(WsdlMockResponse.RESPONSE_CONTENT_PROPERTY, this);
        super.release();
    }
}
