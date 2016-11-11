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

package com.eviware.soapui.impl.wsdl.actions.iface.tools.soapui;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.support.SecurityTestRunnerAction;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.security.SecurityTest;
import com.eviware.soapui.support.action.SoapUIAction;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

public class SecurityTestSecurityTestRunnerAction extends AbstractSoapUIAction<SecurityTest> {

    public SecurityTestSecurityTestRunnerAction() {
        super("Launch SecurityTestRunner", "Launch command-line SecurityTestRunner for this SecurityTest");
    }

    @Override
    public void perform(SecurityTest target, Object param) {
        SoapUIAction<ModelItem> action = SoapUI.getActionRegistry().getAction(SecurityTestRunnerAction.SOAPUI_ACTION_ID);
        SoapUI.setLaunchedTestRunner(true);
        action.perform(target.getTestCase().getTestSuite().getProject(), target);
    }

}
