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

package com.eviware.soapui.impl.wsdl.actions.testcase;

import com.eviware.soapui.analytics.Analytics;
import com.eviware.soapui.analytics.SoapUIActions;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.security.SecurityTest;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

/**
 * Adds a new SecurityTest to a WsdlTestCase
 */

public class AddNewSecurityTestAction extends AbstractSoapUIAction<WsdlTestCase> {
    public static final String SOAPUI_ACTION_ID = "AddNewSecurityTestAction";

    public AddNewSecurityTestAction() {
        super("New SecurityTest", "Creates a new SecurityTest for this TestCase");
    }

    public void perform(WsdlTestCase testCase, Object param) {
        String name = UISupport.prompt("Specify name of SecurityTest", "New SecurityTest",
                "SecurityTest " + (testCase.getSecurityTestCount() + 1));
        if (StringUtils.isNullOrEmpty(name)) {
            return;
        }

        while (testCase.getSecurityTestByName(name.trim()) != null) {
            name = UISupport.prompt("Specify unique name of SecurityTest", "Rename SecurityTest", name);
            if (StringUtils.isNullOrEmpty(name)) {
                return;
            }
        }

        SecurityTest securityTest = testCase.addNewSecurityTest(name);
        UISupport.selectAndShow(securityTest);
        Analytics.trackAction(SoapUIActions.CREATE_SECURITY_TEST.getActionName());
    }
}
