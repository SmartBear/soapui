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

import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.model.testsuite.TestCase;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.TestSuite;

public class TestSuiteMetrics {
    final private TestSuite testSuite;

    public TestSuiteMetrics(TestSuite testSuite) {
        this.testSuite = testSuite;
    }

    public int getTestCaseCount() {
        return testSuite.getTestCaseCount();
    }

    public int getTestStepCount() {
        int result = 0;

        for (TestCase testCase : testSuite.getTestCaseList()) {
            result += testCase.getTestStepCount();
        }

        return result;
    }

    public int getAssertionCount() {
        int result = 0;

        for (TestCase testCase : testSuite.getTestCaseList()) {
            for (TestStep testStep : testCase.getTestStepList()) {
                if (testStep instanceof Assertable) {
                    result += ((Assertable) testStep).getAssertionCount();
                }
            }
        }

        return result;
    }

    public int getLoadTestCount() {
        int result = 0;
        for (TestCase testCase : testSuite.getTestCaseList()) {
            result += testCase.getLoadTestCount();
        }
        return result;
    }

}
