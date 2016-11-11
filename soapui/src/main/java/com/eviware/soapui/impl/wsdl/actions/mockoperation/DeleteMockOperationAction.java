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

package com.eviware.soapui.impl.wsdl.actions.mockoperation;

import com.eviware.soapui.impl.rest.mock.RestMockAction;
import com.eviware.soapui.model.mock.MockOperation;
import com.eviware.soapui.model.mock.MockService;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

/**
 * Deletes a WsdlMockOperation from its WsdlMockService
 *
 * @author Ole.Matzura
 */

public class DeleteMockOperationAction extends AbstractSoapUIAction<MockOperation> {
    public DeleteMockOperationAction() {
        super("Remove", "Removes this node");
    }

    public void perform(MockOperation mockOperation, Object param) {

        String opEquivalentName = mockOperation instanceof RestMockAction ? "Mock Action" : "Mock Operation";

        if (UISupport.confirm("Remove " + opEquivalentName + " [" + mockOperation.getName() + "] from MockService ["
                + mockOperation.getMockService().getName() + "]", "Remove " + opEquivalentName)) {
            MockService mockService = mockOperation.getMockService();
            mockService.removeMockOperation(mockOperation);
        }
    }
}
