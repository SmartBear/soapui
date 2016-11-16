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

package com.eviware.soapui.impl.wsdl.actions.project;

import com.eviware.soapui.analytics.Analytics;
import com.eviware.soapui.analytics.SoapUIActions;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.model.mock.MockService;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

public class AddNewRestMockServiceAction extends AbstractSoapUIAction<WsdlProject> {
    public static final String SOAPUI_ACTION_ID = "AddNewRestMockServiceAction";

    public AddNewRestMockServiceAction() {
        super("New REST MockService", "Creates a new REST MockService in this project");
    }

    public void perform(WsdlProject target, Object param) {
        if (createRestMockService(target) != null) {
            Analytics.trackAction(SoapUIActions.CREATE_REST_MOCK.getActionName());
        }
    }

    public MockService createRestMockService(WsdlProject project) {
        String name = UISupport.prompt("Specify name of MockService", "New MockService",
                "REST MockService " + (project.getRestMockServiceCount() + 1));
        if (name == null) {
            return null;
        }
        while (project.getMockServiceByName(name.trim()) != null) {
            name = UISupport.prompt("Specify unique name of REST MockService", "Rename MockService", name);
        }

        MockService mockService = project.addNewRestMockService(name.trim());
        UISupport.select(mockService);

        return mockService;
    }
}
