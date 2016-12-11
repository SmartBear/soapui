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

import com.eviware.soapui.impl.wsdl.mock.WsdlMockService;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

/**
 * Renames a WsdlMockService
 *
 * @author Ole.Matzura
 */

public class RenameMockServiceAction extends AbstractSoapUIAction<WsdlMockService> {
    public RenameMockServiceAction() {
        super("Rename", "Renames this MockService");
    }

    public void perform(WsdlMockService mockService, Object param) {
        String name = UISupport.prompt("Specify name of MockService", "Rename MockService", mockService.getName());
        if (name == null || name.equals(mockService.getName())) {
            return;
        }
        while (mockService.getProject().getMockServiceByName(name.trim()) != null) {
            name = UISupport.prompt("Specify unique name of MockService", "Rename MockService", mockService.getName());
            if (name == null || name.equals(mockService.getName())) {
                return;
            }
        }

        mockService.setName(name);
    }
}
