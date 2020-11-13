/*
 * SoapUI, Copyright (C) 2004-2019 SmartBear Software
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

package com.eviware.soapui.impl.wsdl.panels.support;

import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.model.testsuite.TestCaseRunContext;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.TestStepResult;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Dummy TestRunner used when executing TestSteps one by one
 *
 * @author ole.matzura
 */

public class MockTestRunner extends AbstractMockTestRunner<WsdlTestCase> implements TestCaseRunner {
    private MockTestRunContext mockRunContext;

    public MockTestRunner(WsdlTestCase testCase) {
        this(testCase, null);
    }

    public MockTestRunner(WsdlTestCase testCase, Logger logger) {
        super(testCase, logger);
    }

    public WsdlTestCase getTestCase() {
        return getTestRunnable();
    }

    public List<TestStepResult> getResults() {
        return new ArrayList<TestStepResult>();
    }

    public TestCaseRunContext getRunContext() {
        return mockRunContext;
    }

    public TestStepResult runTestStep(TestStep testStep) {
        return testStep.run(this, mockRunContext);
    }

    public TestStepResult runTestStepByName(String name) {
        return getTestCase().getTestStepByName(name).run(this, mockRunContext);
    }

    public void gotoStep(int index) {
        getLog().info("Going to step " + index + " [" + getTestCase().getTestStepAt(index).getName() + "]");
    }

    public void gotoStepByName(String stepName) {
        getLog().info("Going to step [" + stepName + "]");
    }

    public void setMockRunContext(MockTestRunContext mockRunContext) {
        this.mockRunContext = mockRunContext;
    }
}
