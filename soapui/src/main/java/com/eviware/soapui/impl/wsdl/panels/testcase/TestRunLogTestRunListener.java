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

package com.eviware.soapui.impl.wsdl.panels.testcase;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCaseRunner;
import com.eviware.soapui.model.support.TestRunListenerAdapter;
import com.eviware.soapui.model.testsuite.TestCaseRunContext;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.TestStepResult;
import com.eviware.soapui.support.DateUtil;

import javax.swing.SwingUtilities;
import java.util.Date;

public class TestRunLogTestRunListener extends TestRunListenerAdapter {
    protected final JTestRunLog runLog;
    protected final boolean clearOnRun;

    public TestRunLogTestRunListener(JTestRunLog runLog, boolean clearOnRun) {
        this.runLog = runLog;
        this.clearOnRun = clearOnRun;
    }

    public void beforeRun(TestCaseRunner testRunner, TestCaseRunContext runContext) {
        if (SoapUI.getTestMonitor().hasRunningLoadTest(testRunner.getTestCase())
                || SoapUI.getTestMonitor().hasRunningSecurityTest(testRunner.getTestCase())) {
            return;
        }

        if (clearOnRun) {
            runLog.clear();
        }

        String testCaseName = testRunner.getTestCase().getName();
        runLog.addBoldText("TestCase [" + testCaseName + "] started at " + DateUtil.formatExtraFull(new Date()));
        runLog.setStepIndex(0);
    }

    public void afterRun(TestCaseRunner testRunner, TestCaseRunContext runContext) {
        if (SoapUI.getTestMonitor().hasRunningLoadTest(testRunner.getTestCase())
                || SoapUI.getTestMonitor().hasRunningSecurityTest(testRunner.getTestCase())) {
            return;
        }

        WsdlTestCaseRunner wsdlRunner = (WsdlTestCaseRunner) testRunner;

        String testCaseName = testRunner.getTestCase().getName();
        if (testRunner.getStatus() == TestCaseRunner.Status.CANCELED) {
            runLog.addText("TestCase [" + testCaseName + "] canceled [" + testRunner.getReason() + "], time taken = "
                    + wsdlRunner.getTimeTaken());
        } else if (testRunner.getStatus() == TestCaseRunner.Status.FAILED) {
            String msg = wsdlRunner.getReason();
            if (wsdlRunner.getError() != null) {
                if (msg != null) {
                    msg += ":";
                }

                msg += wsdlRunner.getError();
            }

            runLog.addText("TestCase [" + testCaseName + "] failed [" + msg + "], time taken = "
                    + wsdlRunner.getTimeTaken());
        } else {
            runLog.addText("TestCase [" + testCaseName + "] finished with status [" + testRunner.getStatus()
                    + "], time taken = " + wsdlRunner.getTimeTaken());
        }
    }

    public synchronized void afterStep(TestCaseRunner testRunner, TestCaseRunContext runContext,
                                       final TestStepResult stepResult) {
        if (SoapUI.getTestMonitor().hasRunningLoadTest(testRunner.getTestCase())
                || SoapUI.getTestMonitor().hasRunningSecurityTest(testRunner.getTestCase())) {
            return;
        }

        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                runLog.addTestStepResult(stepResult);
            }
        });
    }
}
