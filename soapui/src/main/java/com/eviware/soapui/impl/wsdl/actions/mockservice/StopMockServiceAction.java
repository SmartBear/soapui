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

import com.eviware.soapui.model.mock.MockRunner;
import com.eviware.soapui.model.mock.MockService;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

/**
 * Clones a WsdlMockService
 *
 * @author Ole.Matzura
 */

public class StopMockServiceAction<MockServiceType extends MockService>
        extends AbstractSoapUIAction<MockServiceType> {
    public final static String SOAPUI_ACTION_ID = "StopMockServiceAction";

    public StopMockServiceAction() {
        super("Stop", "Stops the MockService if running");
    }

    public void perform(MockServiceType mockService, Object param) {
        try {
            MockRunner mockRunner = mockService.getMockRunner();
            if (mockRunner != null) {
                mockRunner.stop();
            }
        } catch (Exception e) {
            UISupport.showErrorMessage(e);
        }
    }
}
