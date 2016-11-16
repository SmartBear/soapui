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

package com.eviware.soapui.support.components;

import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.model.support.TestSuiteListenerAdapter;
import com.eviware.soapui.model.testsuite.TestStep;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class TestStepComboBoxModel extends AbstractListModel implements ComboBoxModel {
    private final WsdlTestCase testCase;
    private WsdlTestStep selectedStep;
    private int selectedStepIndex = -1;
    private TestStepNameListener testStepNameListener = new TestStepNameListener();
    private InternalTestSuiteListener testSuiteListener;

    public TestStepComboBoxModel(WsdlTestCase testCase) {
        this.testCase = testCase;

        testSuiteListener = new InternalTestSuiteListener();
        testCase.getTestSuite().addTestSuiteListener(testSuiteListener);
    }

    public void release() {
        testCase.getTestSuite().removeTestSuiteListener(testSuiteListener);
    }

    public Object getElementAt(int index) {
        return testCase.getTestStepAt(index).getName();
    }

    public int getSize() {
        return testCase.getTestStepCount();
    }

    private final class InternalTestSuiteListener extends TestSuiteListenerAdapter {
        @Override
        public void testStepAdded(TestStep testStep, int index) {
            if (testStep.getTestCase() == testCase) {
                fireIntervalAdded(TestStepComboBoxModel.this, index, index);
            }
        }

        @Override
        public void testStepMoved(TestStep testStep, int fromIndex, int offset) {
            if (testStep.getTestCase() == testCase) {
                fireContentsChanged(TestStepComboBoxModel.this, fromIndex, fromIndex + offset);
            }
        }

        @Override
        public void testStepRemoved(TestStep testStep, int index) {
            if (testStep.getTestCase() == testCase) {
                fireIntervalRemoved(TestStepComboBoxModel.this, index, index);
            }

            if (index == selectedStepIndex) {
                setSelectedItem(null);
            }
        }
    }

    public Object getSelectedItem() {
        return selectedStep == null ? null : selectedStep.getName();
    }

    public void setSelectedItem(Object anItem) {
        if (selectedStep != null) {
            selectedStep.removePropertyChangeListener(testStepNameListener);
        }

        selectedStep = testCase.getTestStepByName((String) anItem);
        if (selectedStep != null) {
            selectedStep.addPropertyChangeListener(WsdlTestStep.NAME_PROPERTY, testStepNameListener);
            selectedStepIndex = testCase.getIndexOfTestStep(selectedStep);
        } else {
            selectedStepIndex = -1;
        }

        fireContentsChanged(this, -1, -1);
    }

    /**
     * Listen for testStep name changes and modify comboBox model accordingly
     */

    private final class TestStepNameListener implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
            Object oldItem = evt.getOldValue();
            int stepIndex = testCase.getTestStepIndexByName((String) oldItem);
            if (stepIndex != -1) {
                fireContentsChanged(TestStepComboBoxModel.this, stepIndex, stepIndex);

                if (selectedStep != null && testCase.getIndexOfTestStep(selectedStep) == stepIndex) {
                    fireContentsChanged(this, -1, -1);
                }
            }
        }
    }

    public WsdlTestStep getSelectedStep() {
        return selectedStep;
    }
}
