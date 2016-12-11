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

package com.eviware.soapui.impl.wsdl.support;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.support.http.HttpClientSupport;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestRunContext;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.TestRunListener;
import com.eviware.soapui.model.testsuite.TestRunnable;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.TestStepResult;
import com.eviware.soapui.model.testsuite.TestStepResult.TestStepStatus;
import com.eviware.soapui.support.types.StringToObjectMap;
import org.apache.commons.collections.list.TreeList;
import org.apache.http.protocol.BasicHttpContext;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Abstract runner class - runs all steps in a TestCase or a SecurityTest and
 * collects performance data
 *
 * @author dragica.soldo
 */

public abstract class AbstractTestCaseRunner<T extends TestRunnable, T2 extends WsdlTestRunContext> extends
        AbstractTestRunner<T, T2> implements TestCaseRunner {
    private TestRunListener[] testRunListeners = new TestRunListener[0];
    @SuppressWarnings("unchecked")
    private List<TestStepResult> testStepResults = Collections.synchronizedList(new TreeList());
    private int gotoStepIndex;
    private int resultCount;
    private int initCount;
    private int startStep = 0;

    public AbstractTestCaseRunner(T modelItem, StringToObjectMap properties) {
        super(modelItem, properties);
    }

    public int getGotoStepIndex() {
        return gotoStepIndex;
    }

    public int getStartStep() {
        return startStep;
    }

    public void setStartStep(int startStep) {
        this.startStep = startStep;
    }

    public void onCancel(String reason) {
        TestStep currentStep = getRunContext().getCurrentStep();
        if (currentStep != null) {
            currentStep.cancel();
        }
    }

    public void onFail(String reason) {
        TestStep currentStep = getRunContext().getCurrentStep();
        if (currentStep != null) {
            currentStep.cancel();
        }
    }

    public void internalRun(T2 runContext) throws Exception {
        WsdlTestCase testCase = getTestCase();

        gotoStepIndex = -1;
        testStepResults.clear();

        // create state for testcase if specified
        if (testCase.getKeepSession()) {
            if (!(runContext.getProperty(SubmitContext.HTTP_STATE_PROPERTY) instanceof BasicHttpContext)) {
                runContext.setProperty(SubmitContext.HTTP_STATE_PROPERTY, HttpClientSupport.createEmptyContext());
            }
        } else {
            runContext.setProperty(SubmitContext.HTTP_STATE_PROPERTY, null);
        }

        fillInTestRunnableListeners();
        runSetupScripts(runContext);
        if (!isRunning()) {
            return;
        }

        if (testCase.getTimeout() > 0) {
            startTimeoutTimer(testCase.getTimeout());
        }

        notifyBeforeRun();
        if (!isRunning()) {
            return;
        }

        initCount = getStartStep();

        setStartTime();
        for (; initCount < testCase.getTestStepCount() && isRunning(); initCount++) {
            WsdlTestStep testStep = testCase.getTestStepAt(initCount);
            if (testStep.isDisabled()) {
                continue;
            }

            try {
                testStep.prepare(this, runContext);
            } catch (Exception e) {
                setStatus(Status.FAILED);
                SoapUI.logError(e);
                throw new Exception("Failed to prepare testStep [" + testStep.getName() + "]; " + e.toString());
            }
        }

        int currentStepIndex = startStep;
        runContext.setCurrentStep(currentStepIndex);

        for (; isRunning() && currentStepIndex < testCase.getTestStepCount(); currentStepIndex++) {
            if ((currentStepIndex = runCurrentTestStep(runContext, currentStepIndex)) == -2) {
                return;
            }
        }

        failTestRunnableOnErrors(runContext);
        preserveContext(getRunContext());
    }

    protected abstract void failTestRunnableOnErrors(T2 runContext);

    /**
     * Runs current testStep , returns index of the next step to be run and -2 in
     * case execution should break if canceled
     *
     * @param runContext
     * @param currentStepIndex
     * @return
     * @throws Exception
     */
    protected abstract int runCurrentTestStep(T2 runContext, int currentStepIndex) throws Exception;

    protected void internalFinally(T2 runContext) {
        WsdlTestCase testCase = getTestCase();

        for (int c = 0; c < initCount && c < testCase.getTestStepCount(); c++) {
            WsdlTestStep testStep = testCase.getTestStepAt(c);
            if (!testStep.isDisabled()) {
                testStep.finish(this, runContext);
            }
        }

        try {
            runTearDownScripts(runContext);
        } catch (Exception e) {
            SoapUI.logError(e);
        }

        notifyAfterRun();

        clear(runContext);
    }

    protected void runSetupScripts(T2 runContext) throws Exception {
        getTestCase().runSetupScript(runContext, this);
    }

    protected void runTearDownScripts(T2 runContext) throws Exception {
        getTestCase().runTearDownScript(runContext, this);
    }

    protected void clear(T2 runContext) {
        runContext.clear();
        testRunListeners = null;
    }

    protected void fillInTestRunnableListeners() {
        testRunListeners = getTestCase().getTestRunListeners();

    }

    public TestStepResult runTestStepByName(String name) {
        return runTestStep(getTestCase().getTestStepByName(name), true, true);
    }

    public TestStepResult runTestStep(TestStep testStep) {
        return runTestStep(testStep, true, true);
    }

    public TestStepResult runTestStep(TestStep testStep, boolean discard, boolean process) {
        if (!runBeforeSteps(testStep)) {
            return null;
        }
        TestStepResult stepResult = testStep.run(this, getRunContext());

        testStepResults.add(stepResult);
        resultCount++;
        // TODO check all replacements of getTestRunnable with getTestCase in the
        // method
        // enforceMaxResults( getTestRunnable().getMaxResults() );
        enforceMaxResults(getTestCase().getMaxResults());

        for (int i = 0; i < testRunListeners.length; i++) {
            if (Arrays.asList(getTestCase().getTestRunListeners()).contains(testRunListeners[i])) {
                testRunListeners[i].afterStep(this, getRunContext(), stepResult);
            }
        }

        // discard?
        // if( discard && stepResult.getStatus() == TestStepStatus.OK &&
        // getTestRunnable().getDiscardOkResults()
        if (discard && stepResult.getStatus() != TestStepStatus.FAILED && getTestCase().getDiscardOkResults()
                && !stepResult.isDiscarded()) {
            stepResult.discard();
        }

        if (process && stepResult.getStatus() == TestStepStatus.FAILED) {
            // if( getTestRunnable().getFailOnError() )
            if (getTestCase().getFailOnError()) {
                setError(stepResult.getError());
                fail("Cancelling due to failed test step");
            } else {
                getRunContext().setProperty(TestCaseRunner.Status.class.getName(), TestCaseRunner.Status.FAILED);
            }
        }
        // preserveContext( getRunContext() );
        return stepResult;
    }

    protected boolean runBeforeSteps(TestStep testStep) {
        for (TestRunListener testRunListener : testRunListeners) {
            if (Arrays.asList(getTestCase().getTestRunListeners()).contains(testRunListener)) {
                testRunListener.beforeStep(this, getRunContext(), testStep);
            }

            if (!isRunning()) {
                return false;
            }
        }
        return true;
    }

    /**
     * create backup of context properties in WsdlTestCase. This is used for RUN
     * FROM HERE action.
     *
     * @param runContext
     */
    private void preserveContext(T2 runContext) {
        getTestCase().setRunFromHereContext(runContext.getProperties());
    }

    protected void notifyAfterRun() {
        if (testRunListeners == null || testRunListeners.length == 0) {
            return;
        }

        for (int i = 0; i < testRunListeners.length; i++) {
            try {
                if (Arrays.asList(getTestCase().getTestRunListeners()).contains(testRunListeners[i])) {
                    testRunListeners[i].afterRun(this, getRunContext());
                }
            } catch (Throwable t) {
                SoapUI.logError(t);
            }
        }

    }

    protected void notifyBeforeRun() {
        if (testRunListeners == null || testRunListeners.length == 0) {
            return;
        }

        for (int i = 0; i < testRunListeners.length; i++) {
            try {
                if (Arrays.asList(getTestCase().getTestRunListeners()).contains(testRunListeners[i])) {
                    testRunListeners[i].beforeRun(this, getRunContext());
                }
            } catch (Throwable t) {
                SoapUI.logError(t);
            }
        }

    }

    public abstract WsdlTestCase getTestCase();

    public long getTimeTaken() {
        if(isRunning()){
            return System.currentTimeMillis() - getStartTime();
        }
        long sum = 0;
        for (TestStepResult testStepResult : testStepResults) {
            if (testStepResult != null) {
                sum += testStepResult.getTimeTaken();
            }
        }
        return sum;
    }

    public List<TestStepResult> getResults() {
        return testStepResults;
    }

    public void setResultCount(int resultCount) {
        this.resultCount = resultCount;
    }

    public int getResultCount() {
        return resultCount;
    }

    public void gotoStep(int index) {
        gotoStepIndex = index;
    }

    public void enforceMaxResults(long maxResults) {
        if (maxResults < 1) {
            return;
        }

        while (testStepResults.size() > maxResults) {
            testStepResults.remove(0);
        }
    }

    public void gotoStepByName(String stepName) {
        TestStep testStep = getTestCase().getTestStepByName(stepName);
        if (testStep != null) {
            gotoStep(getTestCase().getIndexOfTestStep(testStep));
        }
    }
}
