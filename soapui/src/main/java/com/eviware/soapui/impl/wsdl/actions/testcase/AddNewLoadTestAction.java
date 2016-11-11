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
import com.eviware.soapui.impl.wsdl.loadtest.WsdlLoadTest;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

/**
 * Adds a new WsdlLoadTest to a WsdlTestCase
 *
 * @author Ole.Matzura
 */

public class AddNewLoadTestAction extends AbstractSoapUIAction<WsdlTestCase> {
    public static final String SOAPUI_ACTION_ID = "AddNewLoadTestAction";

    public AddNewLoadTestAction() {
        super("New LoadTest", "Creates a new LoadTest for this TestCase");
    }

    public void perform(WsdlTestCase testCase, Object param) {

        String name = UISupport.prompt("Specify name of LoadTest", "New LoadTest",
                "LoadTest " + (testCase.getLoadTestCount() + 1));
        if (name == null) {
            return;
        }

        WsdlLoadTest loadTest = testCase.addNewLoadTest(name);
        UISupport.selectAndShow(loadTest);
        Analytics.trackAction(SoapUIActions.CREATE_LOAD_TEST.getActionName());
    }
}
