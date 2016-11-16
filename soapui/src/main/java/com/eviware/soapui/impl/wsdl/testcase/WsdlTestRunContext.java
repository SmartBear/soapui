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

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.model.TestModelItem;
import com.eviware.soapui.model.propertyexpansion.PropertyExpander;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.model.support.AbstractSubmitContext;
import com.eviware.soapui.model.testsuite.TestCase;
import com.eviware.soapui.model.testsuite.TestCaseRunContext;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.support.types.StringToObjectMap;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * TestRunContext for WsdlTestCase runners
 *
 * @author Ole.Matzura
 */

public class WsdlTestRunContext extends AbstractSubmitContext<TestModelItem> implements TestCaseRunContext {
    private final TestCaseRunner testRunner;
    private int currentStepIndex;
    private TestCase testCase;
    private PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    public WsdlTestRunContext(TestCaseRunner testRunner, StringToObjectMap properties, TestModelItem testModelItem) {
        super(testModelItem, properties);
        this.testRunner = testRunner;
    }

    public WsdlTestRunContext(TestStep testStep) {
        super(testStep);

        testRunner = null;
        testCase = testStep.getTestCase();
        currentStepIndex = testCase.getIndexOfTestStep(testStep);
    }

    public TestStep getCurrentStep() {
        if (currentStepIndex < 0 || currentStepIndex >= getTestCase().getTestStepCount()) {
            return null;
        }

        return getTestCase().getTestStepAt(currentStepIndex);
    }

    @Override
    public void setProperty(String name, Object value) {
        Object oldValue = new Object();
        super.setProperty(name, value, getTestCase());
        if (pcs != null) {
            pcs.firePropertyChange(name, oldValue, value);
        }
    }

    public int getCurrentStepIndex() {
        return currentStepIndex;
    }

    public void setCurrentStep(int index) {
        currentStepIndex = index;
    }

    public TestCaseRunner getTestRunner() {
        return testRunner;
    }

    public Object getProperty(String testStepName, String propertyName) {
        TestStep testStep = getTestCase().getTestStepByName(testStepName);
        return testStep == null ? null : testStep.getPropertyValue(propertyName);
    }

    public TestCase getTestCase() {
        return testRunner == null ? testCase : testRunner.getTestCase();
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
        WsdlTestCase testCase = (WsdlTestCase) getTestCase();
        TestStep testStep = currentStepIndex >= 0 && currentStepIndex < testCase.getTestStepCount() ? testCase
                .getTestStepAt(currentStepIndex) : null;

        return getProperty(name, testStep, testCase);
    }

    public void reset() {
        resetProperties();
        currentStepIndex = 0;
    }

    public String expand(String content) {
        return PropertyExpander.expandProperties(this, content);
    }

    public Settings getSettings() {
        return testCase == null ? SoapUI.getSettings() : testCase.getSettings();
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }
}
