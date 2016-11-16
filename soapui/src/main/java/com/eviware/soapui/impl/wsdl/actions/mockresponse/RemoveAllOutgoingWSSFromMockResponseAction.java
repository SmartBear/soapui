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

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockResponse;
import com.eviware.soapui.impl.wsdl.support.wss.WssUtils;
import com.eviware.soapui.support.UISupport;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

/**
 * Removes all WSS outgoing Tokens from the specified MockResponse
 * requestContent
 *
 * @author dragica.soldo
 */

public class RemoveAllOutgoingWSSFromMockResponseAction extends AbstractAction {
    private final WsdlMockResponse response;

    public RemoveAllOutgoingWSSFromMockResponseAction(WsdlMockResponse response) {
        super("Remove all outgoing wss");
        this.response = response;
    }

    public void actionPerformed(ActionEvent e) {
        try {
            if (UISupport.confirm("Remove all outgoing wss", "Remove all outgoing wss")) {
                String content = response.getResponseContent();
                response.setResponseContent(WssUtils.removeWSSOutgoing(content, response));
            }
        } catch (Exception e1) {
            SoapUI.logError(e1);
        }

    }
}
