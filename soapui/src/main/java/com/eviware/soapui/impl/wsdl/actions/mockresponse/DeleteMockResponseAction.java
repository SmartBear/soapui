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

import com.eviware.soapui.model.mock.MockOperation;
import com.eviware.soapui.model.mock.MockResponse;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

/**
 * Deletes a MockResponse from its MockOperation
 *
 * @author ole.matzura
 */

public class DeleteMockResponseAction extends AbstractSoapUIAction<MockResponse> {
    public DeleteMockResponseAction() {
        super("Delete", "Deletes this MockResponse");
    }

    public void perform(MockResponse mockResponse, Object param) {
        if (UISupport.confirm("Delete MockResponse [" + mockResponse.getName() + "] from MockOperation ["
                + mockResponse.getMockOperation().getName() + "]", getName())) {
            MockOperation operation = mockResponse.getMockOperation();
            operation.removeMockResponse(mockResponse);
        }
    }
}
