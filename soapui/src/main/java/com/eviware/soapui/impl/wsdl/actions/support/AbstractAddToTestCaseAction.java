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

package com.eviware.soapui.impl.wsdl.actions.support;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.testsuite.TestSuite;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for actions that add TestSteps to a TestCase
 *
 * @author ole.matzura
 */

public abstract class AbstractAddToTestCaseAction<T extends ModelItem> extends AbstractSoapUIAction<T> {
    public AbstractAddToTestCaseAction(String name, String description) {
        super(name, description);
    }

    public static WsdlTestCase getTargetTestCase(WsdlProject project) {
        List<WsdlTestCase> testCases = new ArrayList<WsdlTestCase>();
        List<WsdlTestSuite> testSuites = new ArrayList<WsdlTestSuite>();
        List<String> testCaseNames = new ArrayList<String>();
        WsdlTestCase testCase;

        if (project.getTestSuiteCount() == 0) {
            return addNewTestSuiteAndTestCase(project, "Missing TestSuite in project, enter name to create");
        }

        for (int c = 0; c < project.getTestSuiteCount(); c++) {
            WsdlTestSuite testSuite = project.getTestSuiteAt(c);
            for (int i = 0; i < testSuite.getTestCaseCount(); i++) {
                testCase = testSuite.getTestCaseAt(i);

                testCases.add(testCase);
                testCaseNames.add((testCaseNames.size() + 1) + ": " + testSuite.getName() + " - " + testCase.getName());
                testSuites.add(testSuite);
            }

            testCases.add(null);
            testSuites.add(testSuite);
            testCaseNames.add((testCaseNames.size() + 1) + ": " + testSuite.getName() + " -> Create new TestCase");
        }

        if (testCases.size() == 0) {
            List<String> testSuiteNames = new ArrayList<String>();

            for (int c = 0; c < project.getTestSuiteCount(); c++) {
                TestSuite testSuite = project.getTestSuiteAt(c);
                testSuiteNames.add((testSuiteNames.size() + 1) + ": " + testSuite.getName());
            }

            String selection = (String) UISupport.prompt("Select TestSuite to create TestCase in", "Select TestSuite",
                    testSuiteNames.toArray());
            if (selection == null) {
                return null;
            }

            WsdlTestSuite testSuite = project.getTestSuiteAt(testSuiteNames.indexOf(selection));

            String name = UISupport.prompt("Specify name of TestCase", "Create TestCase",
                    "TestCase " + (testSuite.getTestCaseCount() + 1));
            if (name == null) {
                return null;
            }

            return testSuite.addNewTestCase(name);
        } else {
            testCases.add(null);
            testSuites.add(null);
            testCaseNames.add((testCaseNames.size() + 1) + ": -> Create new TestSuite");

            String selection = (String) UISupport.prompt("Select TestCase", "Select TestCase", testCaseNames.toArray());
            if (selection == null) {
                return null;
            }

            testCase = testCases.get(testCaseNames.indexOf(selection));
            while (testCase != null
                    && (SoapUI.getTestMonitor().hasRunningLoadTest(testCase) || SoapUI.getTestMonitor()
                    .hasRunningSecurityTest(testCase))) {
                UISupport.showErrorMessage("Can not add to TestCase that is currently LoadTesting or SecurityTesting");

                selection = (String) UISupport.prompt("Select TestCase", "Select TestCase", testCaseNames.toArray());
                if (selection == null) {
                    return null;
                }

                testCase = testCases.get(testCaseNames.indexOf(selection));
            }

            // selected create new?
            if (testCase == null) {
                WsdlTestSuite testSuite = testSuites.get(testCaseNames.indexOf(selection));

                // selected create new testsuite?
                if (testSuite == null) {
                    return addNewTestSuiteAndTestCase(project, "Specify name of TestSuite");
                } else {
                    String name = UISupport.prompt("Specify name of TestCase", "Create TestCase", "TestCase "
                            + (testSuite.getTestCaseCount() + 1));
                    if (name == null) {
                        return null;
                    }

                    return testSuite.addNewTestCase(name);
                }
            }
        }

        return testCase;
    }

    protected static WsdlTestCase addNewTestSuiteAndTestCase(WsdlProject project, String questionText) {
        String testSuiteName = UISupport.prompt(questionText,
                "Create TestSuite", "TestSuite " + (project.getTestSuiteCount() + 1));
        if (testSuiteName == null) {
            return null;
        }

        String testCaseName = UISupport.prompt("Specify name of TestCase", "Create TestCase", "TestCase 1");
        if (testCaseName == null) {
            return null;
        }

        WsdlTestSuite testSuite = project.addNewTestSuite(testSuiteName);
        return testSuite.addNewTestCase(testCaseName);
    }
}
