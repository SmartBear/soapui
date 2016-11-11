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

package com.eviware.soapui.impl.wsdl.actions.iface;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.support.AbstractInterface;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.model.mock.MockOperation;
import com.eviware.soapui.model.mock.MockService;
import com.eviware.soapui.model.testsuite.TestCase;
import com.eviware.soapui.model.testsuite.TestSuite;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

/**
 * Removes a WsdlInterface from a WsdlProject
 *
 * @author Ole.Matzura
 */

public class RemoveInterfaceAction extends AbstractSoapUIAction<WsdlInterface> {
    public RemoveInterfaceAction() {
        super("Remove", "Removes this interface from the project");
    }

    public void perform(WsdlInterface iface, Object param) {
        if (hasRunningDependingTests(iface)) {
            UISupport.showErrorMessage("Cannot remove Interface due to running depending tests");
            return;
        }

        if (UISupport.confirm("Remove interface [" + iface.getName() + "] from project [" + iface.getProject().getName()
                + "]?", "Remove Interface")) {
            if (hasDependingTests(iface)) {
                if (!UISupport.confirm("Interface has depending TestSteps which will also be removed. Remove anyway?",
                        "Remove Interface")) {
                    return;
                }
            }

            if (hasDependingMockOperations(iface)) {
                if (!UISupport.confirm(
                        "Interface has depending MockOperations which will also be removed. Remove anyway?",
                        "Remove Interface")) {
                    return;
                }
            }

            WsdlProject project = (WsdlProject) iface.getProject();
            project.removeInterface(iface);
        }
    }

    public static boolean hasRunningDependingTests(AbstractInterface<?> iface) {
        if (SoapUI.getTestMonitor() == null) {
            return false;
        }

        for (int c = 0; c < iface.getProject().getTestSuiteCount(); c++) {
            TestSuite testSuite = iface.getProject().getTestSuiteAt(c);
            for (int i = 0; i < testSuite.getTestCaseCount(); i++) {
                TestCase testCase = testSuite.getTestCaseAt(i);
                if (!SoapUI.getTestMonitor().hasRunningTest(testCase)) {
                    continue;
                }

                for (int j = 0; j < testCase.getTestStepCount(); j++) {
                    WsdlTestStep testStep = (WsdlTestStep) testCase.getTestStepAt(j);
                    if (testStep.dependsOn(iface)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public static boolean hasDependingTests(AbstractInterface<?> iface) {
        for (int c = 0; c < iface.getProject().getTestSuiteCount(); c++) {
            TestSuite testSuite = iface.getProject().getTestSuiteAt(c);
            for (int i = 0; i < testSuite.getTestCaseCount(); i++) {
                TestCase testCase = testSuite.getTestCaseAt(i);

                for (int j = 0; j < testCase.getTestStepCount(); j++) {
                    WsdlTestStep testStep = (WsdlTestStep) testCase.getTestStepAt(j);
                    if (testStep.dependsOn(iface)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public static boolean hasDependingMockOperations(WsdlInterface iface) {
        for (int c = 0; c < iface.getProject().getMockServiceCount(); c++) {
            MockService mockService = iface.getProject().getMockServiceAt(c);
            for (int i = 0; i < mockService.getMockOperationCount(); i++) {
                MockOperation mockOperation = mockService.getMockOperationAt(i);
                if (mockOperation.getOperation().getInterface() == iface) {
                    return true;
                }
            }
        }

        return false;
    }
}
