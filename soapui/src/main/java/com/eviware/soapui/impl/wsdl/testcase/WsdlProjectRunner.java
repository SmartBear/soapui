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

package com.eviware.soapui.impl.wsdl.testcase;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.impl.wsdl.support.AbstractTestRunner;
import com.eviware.soapui.model.project.Project;
import com.eviware.soapui.model.propertyexpansion.DefaultPropertyExpansionContext;
import com.eviware.soapui.model.support.TestSuiteRunListenerAdapter;
import com.eviware.soapui.model.testsuite.ProjectRunListener;
import com.eviware.soapui.model.testsuite.ProjectRunner;
import com.eviware.soapui.model.testsuite.TestSuite;
import com.eviware.soapui.model.testsuite.TestSuite.TestSuiteRunType;
import com.eviware.soapui.model.testsuite.TestSuiteRunContext;
import com.eviware.soapui.model.testsuite.TestSuiteRunListener;
import com.eviware.soapui.model.testsuite.TestSuiteRunner;
import com.eviware.soapui.support.types.StringToObjectMap;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WsdlProjectRunner extends AbstractTestRunner<WsdlProject, WsdlProjectRunContext> implements ProjectRunner {
    private ProjectRunListener[] listeners;
    private Set<TestSuiteRunner> finishedRunners = Collections.synchronizedSet(new HashSet<>());
    private Set<TestSuiteRunner> startedRunners = new HashSet<>();
    private final Object lock = new Object();
    private int currentTestSuiteIndex;
    private WsdlTestSuite currentTestSuite;
    private TestSuiteRunListener internalTestRunListener = new InternalTestSuiteRunListener();

    public WsdlProjectRunner(WsdlProject project, StringToObjectMap properties) {
        super(project, properties);
    }

    @Override
    public WsdlProjectRunContext createContext(StringToObjectMap properties) {
        return new WsdlProjectRunContext(this, properties);
    }

    @Override
    public void onCancel(String reason) {
        synchronized (lock) {
            for (TestSuiteRunner runner : startedRunners.toArray(new TestSuiteRunner[startedRunners.size()])) {
                if (runner.isRunning()) {
                    runner.cancel(reason);
                }
            }
        }
    }

    @Override
    public void onFail(String reason) {
        synchronized (lock) {
            for (TestSuiteRunner runner : startedRunners.toArray(new TestSuiteRunner[startedRunners.size()])) {
                if (runner.isRunning()) {
                    runner.cancel(reason);
                }
            }
        }
    }

    @Override
    public void internalRun(WsdlProjectRunContext runContext) throws Exception {
        WsdlProject project = getTestRunnable();

        listeners = project.getProjectRunListeners();
        project.runBeforeRunScript(runContext, this);
        if (!isRunning()) {
            return;
        }

        if (project.getTimeout() > 0) {
            startTimeoutTimer(project.getTimeout());
        }

        notifyBeforeRun();
        if (!isRunning()) {
            return;
        }

        if (project.getRunType() == TestSuiteRunType.SEQUENTIAL) {
            runSequential(project, runContext);
        } else if (project.getRunType() == TestSuiteRunType.PARALLEL) {
            runParallel(project, runContext);
        }
    }

    private void runParallel(WsdlProject project, WsdlProjectRunContext runContext) {
        currentTestSuiteIndex = -1;
        currentTestSuite = null;

        synchronized (lock) {

            for (TestSuite testSuite : project.getTestSuiteList()) {
                if (!testSuite.isDisabled()) {
                    testSuite.addTestSuiteRunListener(internalTestRunListener);
                    notifyBeforeRunTestSuite(testSuite);
                    runTestSuite((WsdlTestSuite) testSuite, true);
                }
            }
        }

        for (TestSuiteRunner startedRunner : startedRunners) {
            startedRunner.waitUntilFinished();
        }

        updateStatus();
    }

    private void runSequential(WsdlProject project, WsdlProjectRunContext runContext) {
        currentTestSuiteIndex = 0;
        for (; isRunning() && currentTestSuiteIndex < project.getTestSuiteCount(); currentTestSuiteIndex++) {
            currentTestSuite = (WsdlTestSuite) project.getTestSuiteAt(currentTestSuiteIndex);
            if (!currentTestSuite.isDisabled()) {
                notifyBeforeRunTestSuite(currentTestSuite);
                WsdlTestSuiteRunner testSuiteRunner = runTestSuite(currentTestSuite, false);
                finishedRunners.add(testSuiteRunner);
                notifyAfterRunTestSuite(testSuiteRunner);
            }
        }

        updateStatus();
    }

    private void updateStatus() {
        for (TestSuiteRunner runner : finishedRunners) {
            if (runner.getStatus() == Status.FAILED) {
                setStatus(Status.FAILED);
                break;
            }
        }
    }

    private WsdlTestSuiteRunner runTestSuite(WsdlTestSuite testSuite, boolean async) {
        DefaultPropertyExpansionContext properties = (DefaultPropertyExpansionContext) getRunContext().getProperties();
        properties.put("#ProjectRunner#", this);

        // this is here for backwards compatibility, should be removed eventually
        properties.put("#TestSuiteRunner#", this);

        WsdlTestSuiteRunner currentRunner = testSuite.run(properties, true);
        startedRunners.add(currentRunner);
        if (!async) {
            currentRunner.waitUntilFinished();
        }

        return currentRunner;
    }

    @Override
    protected void internalFinally(WsdlProjectRunContext runContext) {
        WsdlProject project = getTestRunnable();

        try {
            project.runAfterRunScript(runContext, this);
        } catch (Exception e) {
            SoapUI.logError(e);
        }

        notifyAfterRun();

        runContext.clear();
        listeners = null;
    }

    private void notifyAfterRun() {
        if (listeners == null || listeners.length == 0) {
            return;
        }

        for (int i = 0; i < listeners.length; i++) {
            listeners[i].afterRun(this, getRunContext());
        }
    }

    private void notifyBeforeRun() {
        if (listeners == null || listeners.length == 0) {
            return;
        }

        for (int i = 0; i < listeners.length; i++) {
            listeners[i].beforeRun(this, getRunContext());
        }
    }

    private void notifyAfterRunTestSuite(TestSuiteRunner testSuiteRunner) {
        if (listeners == null || listeners.length == 0) {
            return;
        }

        for (int i = 0; i < listeners.length; i++) {
            listeners[i].afterTestSuite(this, getRunContext(), testSuiteRunner);
        }
    }

    private void notifyBeforeRunTestSuite(TestSuite testSuite) {
        if (listeners == null || listeners.length == 0) {
            return;
        }

        for (int i = 0; i < listeners.length; i++) {
            listeners[i].beforeTestSuite(this, getRunContext(), testSuite);
        }
    }

    @Override
    public List<TestSuiteRunner> getResults() {
        return Arrays.asList(finishedRunners.toArray(new TestSuiteRunner[finishedRunners.size()]));
    }

    protected void finishRunner(TestSuiteRunner testRunner) {
        notifyAfterRunTestSuite(testRunner);
        finishedRunners.add(testRunner);
        testRunner.getTestSuite().removeTestSuiteRunListener(internalTestRunListener);
    }

    private class InternalTestSuiteRunListener extends TestSuiteRunListenerAdapter {
        @Override
        public void afterRun(TestSuiteRunner testRunner, TestSuiteRunContext runContext) {
            finishRunner(testRunner);
        }
    }

    @Override
    public Project getProject() {
        return getTestRunnable();
    }
}
