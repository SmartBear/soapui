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

import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCaseRunner;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.model.testsuite.TestCaseRunContext;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;
import com.eviware.soapui.support.types.StringToObjectMap;

/**
 * Renames a WsdlTestStep
 *
 * @author Ole.Matzura
 */

public class RunFromTestStepAction extends AbstractSoapUIAction<WsdlTestStep> {
    public RunFromTestStepAction() {
        super("Run from here", "Runs the TestCase starting at this step");
    }

    public void perform(WsdlTestStep testStep, Object param) {
        StringToObjectMap properties = recoverContextProperties(testStep);
        properties.put(TestCaseRunContext.INTERACTIVE, Boolean.TRUE);

        WsdlTestCaseRunner testCaseRunner = new WsdlTestCaseRunner(testStep.getTestCase(), properties);
        testCaseRunner.setStartStep(testStep.getTestCase().getIndexOfTestStep(testStep));
        testCaseRunner.start(true);
    }

    private StringToObjectMap recoverContextProperties(WsdlTestStep testStep) {
        StringToObjectMap properties = null;
        try {
            if (testStep.getParent() instanceof WsdlTestCase) {
                properties = ((WsdlTestCase) testStep.getParent()).getRunFromHereContext();
            } else {
                properties = new StringToObjectMap();
            }
        } catch (Exception e) {
            properties = new StringToObjectMap();
        }
        return properties;
    }
}
