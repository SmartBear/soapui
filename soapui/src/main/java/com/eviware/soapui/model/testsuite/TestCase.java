/*
 * Copyright 2004-2014 SmartBear Software
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

package com.eviware.soapui.model.testsuite;

import com.eviware.soapui.config.TestStepConfig;
import com.eviware.soapui.model.TestModelItem;
import com.eviware.soapui.security.SecurityTest;
import com.eviware.soapui.support.types.StringToObjectMap;

import java.util.List;
import java.util.UUID;

/**
 * A TestCase holding a number of TestSteps
 *
 * @author Ole.Matzura
 */

public interface TestCase extends TestModelItem, ResultContainer, TestRunnable {
    public final static String STATUS_PROPERTY = TestCase.class.getName() + "@status";
    public final static String DISABLED_PROPERTY = TestCase.class.getName() + "@disabled";

    public TestSuite getTestSuite();

    public TestStep getTestStepAt(int index);

    public int getIndexOfTestStep(TestStep testStep);

    public int getTestStepCount();

    public List<TestStep> getTestStepList();

    public LoadTest getLoadTestAt(int index);

    public LoadTest getLoadTestByName(String loadTestName);

    public int getIndexOfLoadTest(LoadTest loadTest);

    public int getLoadTestCount();

    public List<LoadTest> getLoadTestList();

    public TestCaseRunner run(StringToObjectMap context, boolean async);

    public void addTestRunListener(TestRunListener listener);

    public void removeTestRunListener(TestRunListener listener);

    public int getTestStepIndexByName(String stepName);

    public <T extends TestStep> T findPreviousStepOfType(TestStep referenceStep, Class<T> stepClass);

    public <T extends TestStep> T findNextStepOfType(TestStep referenceStep, Class<T> stepClass);

    public <T extends TestStep> List<T> getTestStepsOfType(Class<T> stepType);

    public void moveTestStep(int index, int offset);

    public TestStep getTestStepByName(String stepName);

    public TestStep getTestStepById(UUID testStepId);

    public boolean isDisabled();

    public String getLabel();

    public SecurityTest getSecurityTestAt(int index);

    public SecurityTest getSecurityTestByName(String securityTestName);

    public int getIndexOfSecurityTest(SecurityTest securityTest);

    public int getSecurityTestCount();

    public List<SecurityTest> getSecurityTestList();

    TestStep insertTestStep(TestStepConfig config, int position);
}
