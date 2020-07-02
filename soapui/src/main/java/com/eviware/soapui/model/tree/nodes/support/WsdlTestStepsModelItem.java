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

package com.eviware.soapui.model.tree.nodes.support;

import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.model.support.TestSuiteListenerAdapter;
import com.eviware.soapui.model.testsuite.TestCase;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.TestSuiteListener;
import com.eviware.soapui.support.UISupport;

/**
 * ModelItem for TestSteps node
 *
 * @author ole.matzura
 */

public class WsdlTestStepsModelItem extends BaseTestsModelItem {
    private TestSuiteListener listener = new InternalTestSuiteListener();

    public WsdlTestStepsModelItem(TestCase testCase) {
        super(testCase, createLabel(testCase), UISupport.createImageIcon("/teststeps.gif"));

        testCase.getTestSuite().addTestSuiteListener(listener);
    }

    private static String createLabel(TestCase testCase) {
        return "Test Steps (" + testCase.getTestStepCount() + ")";
    }

    public Settings getSettings() {
        return testCase.getSettings();
    }

    @Override
    public String getName() {
        return createLabel(testCase);
    }

    public WsdlTestCase getTestCase() {
        return (WsdlTestCase) testCase;
    }

    @Override
    public void release() {
        super.release();
        testCase.getTestSuite().removeTestSuiteListener(listener);
    }

    public void updateLabel() {
        setName(createLabel(testCase));
    }

    public class InternalTestSuiteListener extends TestSuiteListenerAdapter implements TestSuiteListener {
        @Override
        public void testStepAdded(TestStep testStep, int index) {
            if (testStep.getTestCase() == testCase) {
                updateLabel();
            }
        }

        @Override
        public void testStepRemoved(TestStep testStep, int index) {
            if (testStep.getTestCase() == testCase) {
                updateLabel();
            }
        }

        @Override
        public void testCaseRemoved(TestCase testCase) {
            if (testCase == WsdlTestStepsModelItem.this.testCase) {
                testCase.getTestSuite().removeTestSuiteListener(listener);
            }
        }
    }

}
