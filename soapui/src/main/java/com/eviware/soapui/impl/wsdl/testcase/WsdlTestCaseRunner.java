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

package com.eviware.soapui.impl.wsdl.testcase;

import com.eviware.soapui.impl.wsdl.support.AbstractTestCaseRunner;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.TestStepResult;
import com.eviware.soapui.support.types.StringToObjectMap;

/**
 * WSDL TestCase Runner - runs all steps in a testcase and collects performance
 * data
 *
 * @author Ole.Matzura
 */

public class WsdlTestCaseRunner extends AbstractTestCaseRunner<WsdlTestCase, WsdlTestRunContext> {

    @SuppressWarnings("unchecked")
    public WsdlTestCaseRunner(WsdlTestCase testCase, StringToObjectMap properties) {
        super(testCase, properties);
    }

    public WsdlTestRunContext createContext(StringToObjectMap properties) {
        return new WsdlTestRunContext(this, properties, this.getTestCase());
    }

    @Override
    protected int runCurrentTestStep(WsdlTestRunContext runContext, int currentStepIndex) {
        TestStep currentStep = runContext.getCurrentStep();
        if (!currentStep.isDisabled()) {
            TestStepResult stepResult = runTestStep(currentStep, true, true);
            if (stepResult == null) {
                return -2;
            }

            if (!isRunning()) {
                return -2;
            }

            if (getGotoStepIndex() != -1) {
                currentStepIndex = getGotoStepIndex() - 1;
                gotoStep(-1);
            }
        }

        runContext.setCurrentStep(currentStepIndex + 1);
        return currentStepIndex;
    }

    @Override
    public WsdlTestCase getTestCase() {
        return getTestRunnable();
    }

    @Override
    protected void failTestRunnableOnErrors(WsdlTestRunContext runContext) {
        if (runContext.getProperty(TestCaseRunner.Status.class.getName()) == TestCaseRunner.Status.FAILED
                && getTestCase().getFailTestCaseOnErrors()) {
            fail("Failing due to failed test step");
        }
    }
}
