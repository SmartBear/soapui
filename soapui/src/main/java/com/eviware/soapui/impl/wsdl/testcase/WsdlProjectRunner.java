/*
 * SoapUI, Copyright (C) 2004-2017 SmartBear Software
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
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class WsdlProjectRunner extends AbstractTestRunner<WsdlProject, WsdlProjectRunContext> implements ProjectRunner {

    private final ReentrantLock lock = new ReentrantLock();
    private final AtomicInteger runCount = new AtomicInteger(-1);
    private volatile boolean running;

    private ProjectRunListener[] listeners;
    private final HashSet<TestSuiteRunner> finishedRunners = new HashSet<>();
    private final HashSet<TestSuiteRunner> activeRunners = new HashSet<>();
    private int currentTestSuiteIndex;
    private WsdlTestSuite currentTestSuite;
    private final TestSuiteRunListener internalTestRunListener = new InternalTestSuiteRunListener();

    public WsdlProjectRunner(WsdlProject project, StringToObjectMap properties) {
        super(project, properties);
    }

    public WsdlProjectRunContext createContext(StringToObjectMap properties) {
        return new WsdlProjectRunContext(this, properties);
    }

    public void onCancel(String reason) {
        try {
            lock.lock();
            for (TestSuiteRunner runner : activeRunners.toArray(new TestSuiteRunner[activeRunners.size()])) {
                runner.cancel(reason);
            }
        } finally {
            lock.unlock();
        }
    }

    public void onFail(String reason) {
        try {
            lock.lock();
            for (TestSuiteRunner runner : activeRunners.toArray(new TestSuiteRunner[activeRunners.size()])) {
                runner.fail(reason);
            }
        } finally {
            lock.unlock();
        }
    }

    public void internalRun(WsdlProjectRunContext runContext) throws Exception {
        WsdlProject project = getTestRunnable();

        try {
            lock.lock();

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
        } finally {
            lock.unlock();
        }

        if (project.getRunType() == TestSuiteRunType.SEQUENTIAL) {
            runSequential(project, runContext);
        } else if (project.getRunType() == TestSuiteRunType.PARALLEL) {
            runParallel(project, runContext);
        }
    }

    private void runParallel(WsdlProject project, WsdlProjectRunContext runContext) {
        try {
            lock.lock();
            currentTestSuiteIndex = -1;
            currentTestSuite = null;

            int i = 0;
            for (TestSuite testSuite : project.getTestSuiteList()) {
                if (!testSuite.isDisabled()) {
                    i++;
                    testSuite.addTestSuiteRunListener(internalTestRunListener);
                    notifyBeforeRunTestSuite(testSuite);
                    runTestSuite((WsdlTestSuite) testSuite, true);
                }
            }
            runCount.set(i);
            running = true;
        } finally {
            lock.unlock();
        }

        while (runCount.get() != 0) {
            synchronized (runCount) {
                try {
                    runCount.wait(1000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void runSequential(WsdlProject project, WsdlProjectRunContext runContext) {
        currentTestSuiteIndex = 0;
        for (; isRunning() && currentTestSuiteIndex < project.getTestSuiteCount(); currentTestSuiteIndex++) {
            currentTestSuite = (WsdlTestSuite) project.getTestSuiteAt(currentTestSuiteIndex);
            if (!currentTestSuite.isDisabled()) {
                notifyBeforeRunTestSuite(currentTestSuite);
                WsdlTestSuiteRunner testSuiteRunner = runTestSuite(currentTestSuite, false);
                activeRunners.remove(testSuiteRunner);
                finishedRunners.add(testSuiteRunner);
                notifyAfterRunTestSuite(testSuiteRunner);
            }
        }

        updateStatus();
    }

    private void updateStatus() {
        activeRunners.clear();
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
        activeRunners.add(currentRunner);
        if (!async) {
            currentRunner.waitUntilFinished();
        }

        return currentRunner;
    }

    protected void internalFinally(WsdlProjectRunContext runContext) {
        try {
            lock.lock();

            WsdlProject project = getTestRunnable();

            try {
                project.runAfterRunScript(runContext, this);
            } catch (Exception e) {
                SoapUI.logError(e);
            }

            notifyAfterRun();

            runContext.clear();
            listeners = null;
        } finally {
            lock.unlock();
        }
    }

    private void notifyAfterRun() {
        try {
            lock.lock();
        if (listeners == null || listeners.length == 0) {
            return;
        }

        for (int i = 0; i < listeners.length; i++) {
            listeners[i].afterRun(this, getRunContext());
        }
        } finally {
            lock.unlock();
        }
    }

    private void notifyBeforeRun() {
        try {
            lock.lock();
            if (listeners == null || listeners.length == 0) {
                return;
            }

            for (int i = 0; i < listeners.length; i++) {
                listeners[i].beforeRun(this, getRunContext());
            }
        } finally {
            lock.unlock();
        }
    }

    private void notifyAfterRunTestSuite(TestSuiteRunner testSuiteRunner) {
        try {
            lock.lock();
            if (listeners == null || listeners.length == 0) {
                return;
            }

            for (int i = 0; i < listeners.length; i++) {
                listeners[i].afterTestSuite(this, getRunContext(), testSuiteRunner);
            }
        } finally {
            lock.unlock();
        }
    }

    private void notifyBeforeRunTestSuite(TestSuite testSuite) {
        try {
            lock.lock();
            if (listeners == null || listeners.length == 0) {
                return;
            }

            for (int i = 0; i < listeners.length; i++) {
                listeners[i].beforeTestSuite(this, getRunContext(), testSuite);
            }
        } finally {
            lock.unlock();
        }
    }

    public List<TestSuiteRunner> getResults() {
        try {
            lock.lock();
            return Arrays.asList(finishedRunners.toArray(new TestSuiteRunner[finishedRunners.size()]));
        } finally {
            lock.unlock();
        }
    }

    private class InternalTestSuiteRunListener extends TestSuiteRunListenerAdapter {
        @Override
        public void afterRun(TestSuiteRunner testRunner, TestSuiteRunContext runContext) {
            try {
                while (!running) {
                    try {
                        Thread.sleep(100L);
                    } catch (InterruptedException cause) {
                        cause.printStackTrace();
                    }
                }

                try {
                    lock.lock();
                    notifyAfterRunTestSuite(testRunner);

                    finishedRunners.add(testRunner);

                    testRunner.getTestSuite().removeTestSuiteRunListener(internalTestRunListener);


                } finally {
                    lock.unlock();
                }
            } finally {
                runCount.decrementAndGet();
                if (runCount.get() == 0) {
                    updateStatus();
                }
            }
        }
    }

    public Project getProject() {
        return getTestRunnable();
    }
}
