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
import com.eviware.soapui.impl.support.AbstractMockOperation;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockOperation;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

/**
 * Creates a new WsdlMockResponse in the specified WsdlMockOperation
 *
 * @author ole.matzura
 */

public class NewMockResponseAction extends AbstractSoapUIAction<AbstractMockOperation> {
    public static final String SOAPUI_ACTION_ID = "NewMockResponseAction";

    public NewMockResponseAction() {
        super("New MockResponse", "Creates a new MockResponse for this MockOperation");
    }

    public void perform(AbstractMockOperation mockOperation, Object param) {
        String name = UISupport.prompt("Enter name of new MockResponse", getName(),
                "Response " + (mockOperation.getMockResponseCount() + 1));

        if (name != null) {
            if (mockOperation instanceof WsdlMockOperation) {
                boolean shouldCreateResponse = true;
                UISupport.showDesktopPanel(((WsdlMockOperation) mockOperation).addNewMockResponse(name, shouldCreateResponse));
            } else if (mockOperation instanceof RestMockAction) {
                UISupport.showDesktopPanel(((RestMockAction) mockOperation).addNewMockResponse(name));
            }
        }
    }
}
