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
 * Clones a WsdlMockService
 *
 * @author Ole.Matzura
 */

public class RestartMockServiceAction<MockServiceType extends MockService>
        extends AbstractSoapUIAction<MockServiceType> {
    public final static String SOAPUI_ACTION_ID = "RestartMockServiceAction";

    public RestartMockServiceAction() {
        super("Restart", "(Re)starts this MockService and opens its desktop window if required");
    }

    public void perform(MockServiceType mockService, Object param) {
        try {
            UISupport.setHourglassCursor();
            if (!SoapUI.getDesktop().hasDesktopPanel(mockService)) {
                UISupport.showDesktopPanel(mockService);
            }

            if (mockService.getMockRunner() != null) {
                mockService.getMockRunner().stop();
            }

            mockService.start();
        } catch (Exception e) {
            UISupport.showErrorMessage(e);
        } finally {
            UISupport.resetCursor();
        }
    }
}
