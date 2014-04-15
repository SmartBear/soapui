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

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockResponse;
import com.eviware.soapui.impl.wsdl.support.wsdl.WsdlContext;
import com.eviware.soapui.model.mock.MockResponse;
import com.eviware.soapui.model.mock.MockResult;
import com.eviware.soapui.support.editor.xml.XmlDocument;
import com.eviware.soapui.support.editor.xml.support.AbstractXmlDocument;
import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.XmlBeans;

/**
 * XmlDocument for the last request to a WsdlMockResponse
 *
 * @author ole.matzura
 */

public class MockRequestXmlDocument extends AbstractXmlDocument {
    private final MockResponse mockResponse;

    public MockRequestXmlDocument(MockResponse response) {
        this.mockResponse = response;
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
        MockResult mockResult = mockResponse.getMockResult();
        return mockResult == null ? null : mockResult.getMockRequest().getRequestContent();
    }

    public void setXml(String xml) {
        MockResult mockResult = mockResponse.getMockResult();
        if (mockResult != null) {
            String oldXml = getXml();
            mockResult.getMockRequest().setRequestContent(xml);
            oldXml = "";
            fireXmlChanged(oldXml, xml);
        } else {
            fireXmlChanged(null, xml);
        }
    }
}
