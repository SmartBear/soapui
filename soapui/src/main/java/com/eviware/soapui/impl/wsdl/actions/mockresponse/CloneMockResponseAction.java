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

import com.eviware.soapui.config.MockResponseConfig;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockOperation;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockResponse;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

/**
 * Clones a WsdlMockResponse
 *
 * @author ole.matzura
 */

public class CloneMockResponseAction extends AbstractSoapUIAction<WsdlMockResponse> {
    public CloneMockResponseAction() {
        super("Clone", "Clones this MockResponse");
    }

    public void perform(WsdlMockResponse mockResponse, Object param) {
        String name = UISupport.prompt("Specify name of cloned MockResponse", getName(),
                "Copy of " + mockResponse.getName());
        if (name == null) {
            return;
        }

        WsdlMockOperation mockOperation = mockResponse.getMockOperation();
        mockOperation.beforeSave();
        MockResponseConfig config = mockOperation.getConfig().addNewResponse();
        config.set(mockResponse.getConfig().copy());
        config.setName(name);
        WsdlMockResponse newResponse = mockOperation.addNewMockResponse(config);

        UISupport.selectAndShow(newResponse);
    }
}
