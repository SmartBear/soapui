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

import com.eviware.soapui.config.TestStepConfig;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.registry.WsdlTestStepFactory;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

/**
 * Adds a WsdlTestStep specified by the supplied WsdlTestStepFactory to a
 * WsdlTestCase
 *
 * @author ole.matzura
 */

public class AddWsdlTestStepAction extends AbstractSoapUIAction<WsdlTestCase> {
    public final static String SOAPUI_ACTION_ID = "AddWsdlTestStepAction";

    public AddWsdlTestStepAction() {
        super("Add Step", "Adds a TestStep to this TestCase");
    }

    public void perform(WsdlTestCase testCase, Object param) {
        WsdlTestStepFactory factory = (WsdlTestStepFactory) param;

        if (!factory.canAddTestStepToTestCase(testCase)) {
            return;
        }

        String name = UISupport.prompt("Specify name for new step", "Add Step", factory.getTestStepName());

        if (name == null) {
            return;
        }
        while (testCase.getTestStepByName(name.trim()) != null) {
            name = UISupport.prompt("Specify unique name of TestStep", "Rename TestStep", name);
            if (StringUtils.isNullOrEmpty(name)) {
                return;
            }
        }
        TestStepConfig newTestStepConfig = factory.createNewTestStep(testCase, name);
        if (newTestStepConfig != null) {
            WsdlTestStep testStep = testCase.addTestStep(newTestStepConfig);
            if (testStep != null) {
                UISupport.selectAndShow(testStep);
            }
        }
    }
}
