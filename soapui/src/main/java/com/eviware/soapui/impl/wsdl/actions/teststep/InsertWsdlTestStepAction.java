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

package com.eviware.soapui.impl.wsdl.actions.teststep;

import com.eviware.soapui.config.TestStepConfig;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.registry.WsdlTestStepFactory;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

/**
 * Inserts a WsdlTestStep specified by the supplied WsdlTestStepFactory at the
 * position to the specified WsdlTestStep
 *
 * @author ole.matzura
 */

public class InsertWsdlTestStepAction extends AbstractSoapUIAction<WsdlTestStep> {
    public static final String SOAPUI_ACTION_ID = "InsertWsdlTestStepAction";

    public InsertWsdlTestStepAction() {
        super("Insert Step", "Inserts a TestStep at the position of this TestStep");
    }

    public void perform(WsdlTestStep testStep, Object param) {
        WsdlTestStepFactory factory = (WsdlTestStepFactory) param;
        WsdlTestCase testCase = testStep.getTestCase();

        if (!factory.canAddTestStepToTestCase(testCase)) {
            return;
        }

        String name = UISupport.prompt("Specify name for new step", "Insert Step", factory.getTestStepName());
        if (name != null) {
            TestStepConfig newTestStepConfig = factory.createNewTestStep(testCase, name);
            if (newTestStepConfig != null) {
                int ix = testCase.getIndexOfTestStep(testStep);
                testStep = testCase.insertTestStep(newTestStepConfig, ix + 1);
                if (testStep != null) {
                    UISupport.selectAndShow(testStep);
                }
            }
        }
    }
}
