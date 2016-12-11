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

package com.eviware.soapui.impl.wsdl.panels.mockoperation.actions;

import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.model.mock.MockResponse;
import com.eviware.soapui.support.UISupport;

import javax.swing.AbstractAction;
import javax.swing.Action;
import java.awt.event.ActionEvent;

/**
 * Creates an empty SOAP response message for WsdlMockResponse
 *
 * @author ole.matzura
 */

public class CreateEmptyWsdlMockResponseAction extends AbstractAction {
    private final MockResponse mockResponse;

    public CreateEmptyWsdlMockResponseAction(MockResponse mockResponse) {
        super("Create Empty");
        this.mockResponse = mockResponse;
        putValue(Action.SMALL_ICON, UISupport.createImageIcon("/create_empty_request.gif"));
        putValue(Action.SHORT_DESCRIPTION, "Creates an empty SOAP response");
    }

    public void actionPerformed(ActionEvent e) {
        //FIXME for rest mocking action
        WsdlOperation operation = (WsdlOperation) mockResponse.getMockOperation().getOperation();

        if (operation == null) {
            UISupport.showErrorMessage("Missing operation for this mock response");
            return;
        }

        if (UISupport.confirm("Overwrite current response with empty one?", "Create Empty")) {
            WsdlInterface iface = operation.getInterface();
            mockResponse.setResponseContent(iface.getMessageBuilder().buildEmptyMessage());
        }
    }
}
