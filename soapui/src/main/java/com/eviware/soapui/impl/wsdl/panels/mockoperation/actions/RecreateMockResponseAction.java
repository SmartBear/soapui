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

import com.eviware.soapui.model.iface.Operation;
import com.eviware.soapui.model.mock.MockResponse;
import com.eviware.soapui.settings.WsdlSettings;
import com.eviware.soapui.support.UISupport;

import javax.swing.AbstractAction;
import javax.swing.Action;
import java.awt.event.ActionEvent;

/**
 * Recreates an SOAP response message for WsdlMockResponse from its WSDL/XSD
 * definition
 *
 * @author ole.matzura
 */

public class RecreateMockResponseAction extends AbstractAction {
    private final MockResponse mockResponse;

    public RecreateMockResponseAction(MockResponse mockResponse) {
        super("Recreate response");
        this.mockResponse = mockResponse;
        putValue(Action.SMALL_ICON, UISupport.createImageIcon("/recreate_request.gif"));
        putValue(Action.SHORT_DESCRIPTION, "Recreates a default response from the schema");
    }

    public void actionPerformed(ActionEvent arg0) {
        Operation operation = mockResponse.getMockOperation().getOperation();
        if (operation == null) {
            UISupport.showErrorMessage("Missing operation for this mock response");
            return;
        }

        String response = mockResponse.getResponseContent();
        if (response != null && response.trim().length() > 0
                && !UISupport.confirm("Overwrite current response?", "Recreate response")) {
            return;
        }

        boolean createOptional = mockResponse.getSettings().getBoolean(
                WsdlSettings.XML_GENERATION_ALWAYS_INCLUDE_OPTIONAL_ELEMENTS);
        if (!createOptional) {
            createOptional = UISupport.confirm("Create optional elements in schema?", "Create Request");
        }

        String req = operation.createResponse(createOptional);
        if (req == null) {
            UISupport.showErrorMessage("Response creation failed");
            return;
        }

        mockResponse.setResponseContent(req);
    }

}
