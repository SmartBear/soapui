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

package com.eviware.soapui.impl.wsdl.actions.mockresponse;

import com.eviware.soapui.impl.wsdl.mock.WsdlMockResponse;
import com.eviware.soapui.impl.wsdl.support.soap.SoapVersion;
import com.eviware.soapui.impl.wsdl.support.wsa.WsaUtils;
import com.eviware.soapui.model.propertyexpansion.DefaultPropertyExpansionContext;
import com.eviware.soapui.support.UISupport;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

/**
 * Adds WS-A headers to the specified WsdlRequests requestContent
 *
 * @author dragica.soldo
 */

public class AddWsaHeadersToMockResponseAction extends AbstractAction {
    private final WsdlMockResponse mockResponse;

    public AddWsaHeadersToMockResponseAction(WsdlMockResponse mockResponse) {
        super("Add WS-A headers");
        this.mockResponse = mockResponse;
    }

    public void actionPerformed(ActionEvent e) {
        try {
            SoapVersion soapVersion = mockResponse.getOperation().getInterface().getSoapVersion();
            String content = mockResponse.getResponseContent();
            WsaUtils wsaUtils = new WsaUtils(content, soapVersion, mockResponse.getOperation(),
                    new DefaultPropertyExpansionContext(mockResponse));
            content = wsaUtils.addWSAddressingMockResponse(mockResponse);
            mockResponse.setResponseContent(content);
        } catch (Exception e1) {
            UISupport.showErrorMessage(e1);
        } finally {
            UISupport.resetCursor();
        }
    }
}
