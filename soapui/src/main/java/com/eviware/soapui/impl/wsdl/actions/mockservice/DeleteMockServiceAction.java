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

package com.eviware.soapui.impl.wsdl.actions.mockservice;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.model.mock.MockService;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

/**
 * Removes a MockService from its WsdlProject
 *
 * @author Ole.Matzura
 */

public class DeleteMockServiceAction extends AbstractSoapUIAction<MockService> {
    public DeleteMockServiceAction() {
        super("Remove", "Removes this MockService from the Project");
    }

    public void perform(MockService mockService, Object param) {
        if (SoapUI.getMockEngine().hasRunningMock(mockService)) {
            UISupport.showErrorMessage("Cannot remove MockService while mocks are running");
            return;
        }

        if (UISupport.confirm("Remove MockService [" + mockService.getName() + "] from Project", "Remove MockService")) {
            mockService.getProject().removeMockService(mockService);
        }
    }

}
