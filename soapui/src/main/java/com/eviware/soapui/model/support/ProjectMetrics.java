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

package com.eviware.soapui.model.support;

import com.eviware.soapui.model.mock.MockOperation;
import com.eviware.soapui.model.mock.MockService;
import com.eviware.soapui.model.project.Project;
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.model.testsuite.TestCase;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.TestSuite;

public class ProjectMetrics {
    private final Project project;

    public ProjectMetrics(Project project) {
        this.project = project;
    }

    public int getTestCaseCount() {
        int result = 0;

        for (TestSuite testSuite : project.getTestSuiteList()) {
            result += testSuite.getTestCaseCount();
        }

        return result;
    }

    public int getTestStepCount() {
        int result = 0;

        for (TestSuite testSuite : project.getTestSuiteList()) {
            for (TestCase testCase : testSuite.getTestCaseList()) {
                result += testCase.getTestStepCount();
            }
        }

        return result;
    }

    public int getAssertionCount() {
        int result = 0;

        for (TestSuite testSuite : project.getTestSuiteList()) {
            for (TestCase testCase : testSuite.getTestCaseList()) {
                for (TestStep testStep : testCase.getTestStepList()) {
                    if (testStep instanceof Assertable) {
                        result += ((Assertable) testStep).getAssertionCount();
                    }
                }
            }
        }

        return result;
    }

    public int getLoadTestCount() {
        int result = 0;

        for (TestSuite testSuite : project.getTestSuiteList()) {
            for (TestCase testCase : testSuite.getTestCaseList()) {
                result += testCase.getLoadTestCount();
            }
        }

        return result;
    }

    public int getMockOperationCount() {
        int result = 0;

        for (MockService mockService : project.getMockServiceList()) {
            result += mockService.getMockOperationCount();
        }

        return result;
    }

    public int getMockResponseCount() {
        int result = 0;

        for (MockService mockService : project.getMockServiceList()) {
            for (MockOperation mockOperation : mockService.getMockOperationList()) {
                result += mockOperation.getMockResponseCount();
            }
        }

        return result;
    }

    public int getRestMockActionCount() {
        int restMockActionCount = 0;
        for (MockService mockService : project.getRestMockServiceList()) {
            restMockActionCount += mockService.getMockOperationCount();
        }
        return restMockActionCount;
    }

    public int getRestMockResponseCount() {
        int restMockResponseCount = 0;
        for (MockService mockService : project.getRestMockServiceList()) {
            for (MockOperation mockOperation : mockService.getMockOperationList()) {
                restMockResponseCount += mockOperation.getMockResponseCount();
            }
        }
        return restMockResponseCount;
    }
}
