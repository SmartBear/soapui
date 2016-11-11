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

package com.eviware.soapui.impl.wsdl.actions.request;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.support.soap.SoapVersion;
import com.eviware.soapui.impl.wsdl.support.wsa.WsaUtils;
import com.eviware.soapui.model.propertyexpansion.DefaultPropertyExpansionContext;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

/**
 * Adds WS-A headers to the specified WsdlRequests requestContent
 *
 * @author dragica.soldo
 */

public class AddWsaHeadersToRequestAction extends AbstractAction {
    private final WsdlRequest request;

    public AddWsaHeadersToRequestAction(WsdlRequest request) {
        super("Add WS-A headers");
        this.request = request;
    }

    public void actionPerformed(ActionEvent e) {
        try {
            SoapVersion soapVersion = request.getOperation().getInterface().getSoapVersion();
            String content = request.getRequestContent();
            WsaUtils wsaUtils = new WsaUtils(content, soapVersion, request.getOperation(),
                    new DefaultPropertyExpansionContext(request));
            content = wsaUtils.addWSAddressingRequest(request);
            request.setRequestContent(content);
        } catch (Exception e1) {
            SoapUI.logError(e1);
        }
    }
}
