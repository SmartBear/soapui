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
import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.model.testsuite.LoadTestRunner;
import com.eviware.soapui.model.testsuite.TestCase;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.TestRunner;
import com.eviware.soapui.model.testsuite.TestRunner.Status;
import com.eviware.soapui.model.testsuite.TestSuiteRunContext;
import com.eviware.soapui.model.testsuite.TestSuiteRunListener;
import com.eviware.soapui.model.testsuite.TestSuiteRunner;
import com.eviware.soapui.monitor.support.TestMonitorListenerAdapter;
import com.eviware.soapui.security.SecurityTestRunner;

import javax.swing.JProgressBar;
import java.awt.Color;

/**
 * Class that keeps a JProgressBars state in sync with a TestCase
 *
 * @author Ole.Matzura
 */

public class ProgressBarTestSuiteAdapter {
    private final JProgressBar progressBar;
    private final WsdlTestSuite testSuite;
    private InternalTestSuiteRunListener internalTestRunListener;
    private InternalTestMonitorListener internalTestMonitorListener;

    public ProgressBarTestSuiteAdapter(JProgressBar progressBar, WsdlTestSuite testSuite) {
        this.progressBar = progressBar;
        this.testSuite = testSuite;

        setLoadTestingState();
        setSecurityTestingState();

        internalTestRunListener = new InternalTestSuiteRunListener();
        testSuite.addTestSuiteRunListener(internalTestRunListener);
        internalTestMonitorListener = new InternalTestMonitorListener();
        SoapUI.getTestMonitor().addTestMonitorListener(internalTestMonitorListener);
    }

    public void release() {
        testSuite.removeTestSuiteRunListener(internalTestRunListener);
        SoapUI.getTestMonitor().removeTestMonitorListener(internalTestMonitorListener);
    }

    private void setLoadTestingState() {
        if (SoapUI.getTestMonitor().hasRunningLoadTest(testSuite)) {
            progressBar.setIndeterminate(true);
            progressBar.setString("load testing");
        } else {
            progressBar.setIndeterminate(false);
            progressBar.setString("");
        }
    }

    private void setSecurityTestingState() {
        if (SoapUI.getTestMonitor().hasRunningSecurityTest(testSuite)) {
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

    public class InternalTestSuiteRunListener implements TestSuiteRunListener {
        public void beforeRun(TestSuiteRunner testRunner, TestSuiteRunContext runContext) {
            if (progressBar.isIndeterminate()) {
                return;
            }

            progressBar.getModel().setMaximum(testRunner.getTestSuite().getTestCaseCount());
            progressBar.setForeground(Color.GREEN.darker());
        }

        public void beforeTestCase(TestSuiteRunner testRunner, TestSuiteRunContext runContext, TestCase testCase) {
            if (progressBar.isIndeterminate()) {
                return;
            }

            progressBar.setString(testCase.getName());
            progressBar.setValue(testRunner.getResults().size());
        }

        public void afterTestCase(TestSuiteRunner testRunner, TestSuiteRunContext runContext, TestCaseRunner result) {
            if (progressBar.isIndeterminate()) {
                return;
            }

            if (result.getStatus() == TestRunner.Status.FAILED) {
                progressBar.setForeground(Color.RED);
            } else if (!testSuite.isFailOnErrors()) {
                progressBar.setForeground(Color.GREEN.darker());
            }

            progressBar.setValue(testRunner.getResults().size() + 1);
        }

        public void afterRun(TestSuiteRunner testRunner, TestSuiteRunContext runContext) {
            if (testRunner.getStatus() == Status.FAILED) {
                progressBar.setForeground(Color.RED);
            } else if (testRunner.getStatus() == Status.FINISHED) {
                progressBar.setForeground(Color.GREEN.darker());
            }

            if (progressBar.isIndeterminate()) {
                return;
            }

            if (testRunner.getStatus() == TestCaseRunner.Status.FINISHED) {
                progressBar.setValue(testRunner.getTestSuite().getTestCaseCount());
            }

            progressBar.setString(testRunner.getStatus().toString());
        }
    }
}
