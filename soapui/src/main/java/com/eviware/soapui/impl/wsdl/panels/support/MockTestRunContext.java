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

package com.eviware.soapui.impl.wsdl.panels.support;

import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.model.support.AbstractSubmitContext;
import com.eviware.soapui.model.testsuite.TestCase;
import com.eviware.soapui.model.testsuite.TestCaseRunContext;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.TestStep;

/**
 * Dummy TestRunContext used when executing TestSteps one by one
 *
 * @author ole.matzura
 */

public class MockTestRunContext extends AbstractSubmitContext<ModelItem> implements TestCaseRunContext {
    private final MockTestRunner mockTestRunner;
    private final WsdlTestStep testStep;

    public MockTestRunContext(MockTestRunner mockTestRunner, WsdlTestStep testStep) {
        super(testStep == null ? mockTestRunner.getTestCase() : testStep);
        this.mockTestRunner = mockTestRunner;
        this.testStep = testStep;
        setProperty("log", mockTestRunner.getLog());
        mockTestRunner.setMockRunContext(this);
    }

    public TestStep getCurrentStep() {
        return testStep;
    }

    @Override
    public void setProperty(String name, Object value) {
        super.setProperty(name, value, getTestCase());
    }

    public int getCurrentStepIndex() {
        return testStep == null ? -1 : testStep.getTestCase().getIndexOfTestStep(testStep);
    }

    public TestCaseRunner getTestRunner() {
        return mockTestRunner;
    }

    @Override
    public Object get(Object key) {
        if ("currentStep".equals(key)) {
            return getCurrentStep();
        }

        if ("currentStepIndex".equals(key)) {
            return getCurrentStepIndex();
        }

        if ("settings".equals(key)) {
            return getSettings();
        }

        if ("testCase".equals(key)) {
            return getTestCase();
        }

        if ("testRunner".equals(key)) {
            return getTestRunner();
        }

        Object result = getProperty(key.toString());

        if (result == null) {
            result = super.get(key);
        }

        return result;
    }

    @Override
    public Object put(String key, Object value) {
        Object oldValue = get(key);
        setProperty(key, value);
        return oldValue;
    }

    public Object getProperty(String name) {
        return getProperty(name, testStep, testStep == null ? null : (WsdlTestCase) testStep.getTestCase());
    }

    public Object getProperty(String testStepName, String propertyName) {
        TestStep ts = testStep == null ? null : testStep.getTestCase().getTestStepByName(testStepName);
        return ts == null ? null : ts.getPropertyValue(propertyName);
    }

    public TestCase getTestCase() {
        return testStep == null ? null : testStep.getTestCase();
    }

    public Settings getSettings() {
        return testStep == null ? null : testStep.getSettings();
    }
}
