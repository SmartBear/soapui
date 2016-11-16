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

package com.eviware.soapui.impl.wsdl.actions.testsuite;

import com.eviware.soapui.analytics.Analytics;
import com.eviware.soapui.analytics.SoapUIActions;
import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

/**
 * Adds a new WsdlTestCase to a WsdlTestSuite
 *
 * @author Ole.Matzura
 */

public class AddNewTestCaseAction extends AbstractSoapUIAction<WsdlTestSuite> {
    public static final String SOAPUI_ACTION_ID = "AddNewTestCaseAction";

    public AddNewTestCaseAction() {
        super("New TestCase", "Creates a new TestCase in this TestSuite");
        // putValue( Action.ACCELERATOR_KEY, UISupport.getKeyStroke( "menu N" ));
    }

    public void perform(WsdlTestSuite testSuite, Object param) {
        String name = UISupport.prompt("Specify name of TestCase", "New TestCase",
                "TestCase " + (testSuite.getTestCaseCount() + 1));
        if (name == null) {
            return;
        }
        while (testSuite.getTestCaseByName(name.trim()) != null) {
            name = UISupport.prompt("Specify unique name of TestCase", "Rename TestCase", name);
            if (StringUtils.isNullOrEmpty(name)) {
                return;
            }
        }

        WsdlTestCase testCase = testSuite.addNewTestCase(name);
        Analytics.trackAction(SoapUIActions.CREATE_TEST_CASE.getActionName());
        UISupport.showDesktopPanel(testCase);
    }
}
