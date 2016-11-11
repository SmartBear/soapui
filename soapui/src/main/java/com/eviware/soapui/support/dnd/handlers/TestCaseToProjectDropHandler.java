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

package com.eviware.soapui.support.dnd.handlers;

import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.model.support.ModelSupport;
import com.eviware.soapui.support.UISupport;

import java.util.HashSet;
import java.util.Set;

public class TestCaseToProjectDropHandler extends AbstractAfterModelItemDropHandler<WsdlTestCase, WsdlProject> {
    public TestCaseToProjectDropHandler() {
        super(WsdlTestCase.class, WsdlProject.class);
    }

    @Override
    boolean canCopyAfter(WsdlTestCase source, WsdlProject target) {
        return true;
    }

    @Override
    boolean canMoveAfter(WsdlTestCase source, WsdlProject target) {
        return true;
    }

    @Override
    boolean copyAfter(WsdlTestCase testCase, WsdlProject target) {
        WsdlTestSuite testSuite = getTargetTestSuite(target, "Copy TestCase");
        if (testSuite == null) {
            return false;
        }

        testCase = TestCaseToTestSuiteDropHandler.copyTestCase(testCase, testSuite, -1);
        if (testCase != null) {
            UISupport.select(testCase);
        }

        return testCase != null;
    }

    private WsdlTestSuite getTargetTestSuite(WsdlProject target, String title) {
        String name = "TestSuite 1";
        if (target.getTestSuiteCount() > 0) {
            String[] names = ModelSupport.getNames(target.getTestSuiteList(), new String[]{"<Create New>"});
            name = UISupport.prompt("Specify target TestSuite for TestCase", title, names);
            if (name == null) {
                return null;
            }
        }

        WsdlTestSuite testSuite = target.getTestSuiteByName(name);
        if (testSuite == null) {
            name = UISupport.prompt("Specify name for new TestSuite", title, "TestSuite "
                    + (target.getTestSuiteCount() + 1));
            if (name == null) {
                return null;
            }

            testSuite = target.addNewTestSuite(name);
        }

        Set<Interface> requiredInterfaces = new HashSet<Interface>();

        for (int i = 0; i < testSuite.getTestCaseCount(); i++) {
            WsdlTestCase testCase = testSuite.getTestCaseAt(i);

            for (int y = 0; y < testCase.getTestStepCount(); y++) {
                WsdlTestStep testStep = testCase.getTestStepAt(y);
                requiredInterfaces.addAll(testStep.getRequiredInterfaces());
            }
        }

        if (!DragAndDropSupport.importRequiredInterfaces(target, requiredInterfaces, title)) {
            return null;
        } else {
            return testSuite;
        }
    }

    @Override
    boolean moveAfter(WsdlTestCase testCase, WsdlProject target) {
        WsdlTestSuite testSuite = getTargetTestSuite(target, "Move TestCase");
        if (testSuite == null) {
            return false;
        }

        testCase = TestCaseToTestSuiteDropHandler.moveTestCase(testCase, testSuite, -1);
        if (testCase != null) {
            UISupport.select(testCase);
        }

        return testCase != null;
    }

    @Override
    String getCopyAfterInfo(WsdlTestCase source, WsdlProject target) {
        return "Copy TestCase [" + source.getName() + "] to TestSuite in Project [" + target.getName() + "]";
    }

    @Override
    String getMoveAfterInfo(WsdlTestCase source, WsdlProject target) {
        return "Move TestCase [" + source.getName() + "] to TestSuite in Project [" + target.getName() + "]";
    }
}
