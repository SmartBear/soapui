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

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.model.support.TestRunListenerAdapter;
import com.eviware.soapui.model.testsuite.LoadTestRunner;
import com.eviware.soapui.model.testsuite.TestCaseRunContext;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.TestRunner.Status;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.TestStepResult;
import com.eviware.soapui.model.testsuite.TestStepResult.TestStepStatus;
import com.eviware.soapui.monitor.support.TestMonitorListenerAdapter;
import com.eviware.soapui.security.SecurityTestRunner;

import javax.swing.JProgressBar;
import java.awt.Color;

/**
 * Class that keeps a JProgressBars state in sync with a TestCase
 *
 * @author Ole.Matzura
 */

public class ProgressBarTestCaseAdapter {
    private final JProgressBar progressBar;
    private final WsdlTestCase testCase;
    private InternalTestRunListener internalTestRunListener;
    private InternalTestMonitorListener internalTestMonitorListener;

    public ProgressBarTestCaseAdapter(JProgressBar progressBar, WsdlTestCase testCase) {
        this.progressBar = progressBar;
        this.testCase = testCase;

        setLoadTestingState();
        setSecurityTestingState();

        internalTestRunListener = new InternalTestRunListener();
        testCase.addTestRunListener(internalTestRunListener);
        internalTestMonitorListener = new InternalTestMonitorListener();
        SoapUI.getTestMonitor().addTestMonitorListener(internalTestMonitorListener);
    }

    public void release() {
        testCase.removeTestRunListener(internalTestRunListener);
        SoapUI.getTestMonitor().removeTestMonitorListener(internalTestMonitorListener);
    }

    private void setLoadTestingState() {
        if (SoapUI.getTestMonitor().hasRunningLoadTest(testCase)) {
            progressBar.setIndeterminate(true);
            progressBar.setString("load testing");
        } else {
            progressBar.setIndeterminate(false);
            progressBar.setString("");
        }
    }

    private void setSecurityTestingState() {
        if (SoapUI.getTestMonitor().hasRunningSecurityTest(testCase)) {
            progressBar.setIndeterminate(true);
            progressBar.setString("security testing");
        } else {
            progressBar.setIndeterminate(false);
            progressBar.setString("");
        }
    }

    private class InternalTestMonitorListener extends TestMonitorListenerAdapter {
        public void loadTestStarted(LoadTestRunner loadTestRunner) {
            setLoadTestingState();
        }

        public void loadTestFinished(LoadTestRunner loadTestRunner) {
            setLoadTestingState();
        }

        public void securityTestStarted(SecurityTestRunner securityTestRunner) {
            setSecurityTestingState();
        }

        public void securityTestFinished(SecurityTestRunner securityTestRunner) {
            setSecurityTestingState();
        }
    }

    public class InternalTestRunListener extends TestRunListenerAdapter {
        public void beforeRun(TestCaseRunner testRunner, TestCaseRunContext runContext) {
            if (progressBar.isIndeterminate()) {
                return;
            }

            progressBar.getModel().setMaximum(testRunner.getTestCase().getTestStepCount());
            progressBar.setForeground(Color.GREEN.darker());
            progressBar.setValue(0);
            progressBar.setString("");
        }

        public void beforeStep(TestCaseRunner testRunner, TestCaseRunContext runContext, TestStep testStep) {
            if (progressBar.isIndeterminate()) {
                return;
            }

            if (testStep != null) {
                progressBar.setString(testStep.getName());
                progressBar.setValue(runContext.getCurrentStepIndex());
            }
        }

        public void afterStep(TestCaseRunner testRunner, TestCaseRunContext runContext, TestStepResult result) {
            if (progressBar.isIndeterminate()) {
                return;
            }

            if (result.getStatus() == TestStepStatus.FAILED) {
                progressBar.setForeground(Color.RED);
            } else if (!testCase.getFailTestCaseOnErrors()) {
                progressBar.setForeground(Color.GREEN.darker());
            }

            progressBar.setValue(runContext.getCurrentStepIndex() + 1);
        }

        public void afterRun(TestCaseRunner testRunner, TestCaseRunContext runContext) {
            if (testRunner.getStatus() == Status.FAILED) {
                progressBar.setForeground(Color.RED);
            } else if (testRunner.getStatus() == Status.FINISHED) {
                progressBar.setForeground(Color.GREEN.darker());
            }

            if (progressBar.isIndeterminate()) {
                return;
            }

            if (testRunner.getStatus() == TestCaseRunner.Status.FINISHED) {
                progressBar.setValue(testRunner.getTestCase().getTestStepCount());
            }

            progressBar.setString(testRunner.getStatus().toString());
        }
    }
}
