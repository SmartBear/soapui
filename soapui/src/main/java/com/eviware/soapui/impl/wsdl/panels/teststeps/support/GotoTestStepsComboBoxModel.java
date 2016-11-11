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

package com.eviware.soapui.impl.wsdl.panels.teststeps.support;

import com.eviware.soapui.impl.wsdl.teststeps.WsdlGotoTestStep.GotoCondition;
import com.eviware.soapui.model.support.TestSuiteListenerAdapter;
import com.eviware.soapui.model.testsuite.TestCase;
import com.eviware.soapui.model.testsuite.TestStep;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * ComboBox-model used by combo in the WsdlGotoTestStep desktop panel for
 * selecting a conditions target teststep
 *
 * @author Ole.Matzura
 */

public class GotoTestStepsComboBoxModel extends AbstractListModel implements ComboBoxModel {
    private final TestCase testCase;
    private GotoCondition condition;
    private InternalTestSuiteListener testSuiteListener = new InternalTestSuiteListener();
    ;
    private InternalPropertyChangeListener propertyChangeListener = new InternalPropertyChangeListener();

    public GotoTestStepsComboBoxModel(TestCase testCase, GotoCondition condition) {
        super();
        this.testCase = testCase;
        this.condition = condition;

        testCase.getTestSuite().addTestSuiteListener(testSuiteListener);

        if (condition != null) {
            condition.addPropertyChangeListener(GotoCondition.TARGET_STEP_PROPERTY, propertyChangeListener);
        }

        for (int c = 0; c < testCase.getTestStepCount(); c++) {
            testCase.getTestStepAt(c).addPropertyChangeListener(TestStep.NAME_PROPERTY, propertyChangeListener);
        }
    }

    public GotoCondition getCondition() {
        return condition;
    }

    public void setCondition(GotoCondition condition) {
        if (this.condition != null) {
            this.condition.removePropertyChangeListener(GotoCondition.TARGET_STEP_PROPERTY, propertyChangeListener);
        }

        this.condition = condition;

        if (condition != null) {
            condition.addPropertyChangeListener(GotoCondition.TARGET_STEP_PROPERTY, propertyChangeListener);
        }

        fireContentsChanged(this, 0, getSize());
    }

    public void setSelectedItem(Object anItem) {
        if (condition != null) {
            condition.setTargetStep(anItem == null ? null : anItem.toString());
        }
    }

    public Object getSelectedItem() {
        return condition == null ? null : condition.getTargetStep();
    }

    public int getSize() {
        return testCase.getTestStepCount();
    }

    public Object getElementAt(int index) {
        return testCase.getTestStepAt(index).getName();
    }

    private class InternalTestSuiteListener extends TestSuiteListenerAdapter {
        public void testStepAdded(TestStep testStep, int index) {
            if (testStep.getTestCase() == testCase) {
                fireContentsChanged(GotoTestStepsComboBoxModel.this, 0, getSize());
            }
        }

        public void testStepRemoved(TestStep testStep, int index) {
            if (testStep.getTestCase() == testCase) {
                fireContentsChanged(GotoTestStepsComboBoxModel.this, 0, getSize());
            }
        }
    }

    private class InternalPropertyChangeListener implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
            fireContentsChanged(GotoTestStepsComboBoxModel.this, 0, getSize());
        }
    }

    public void release() {
        testCase.getTestSuite().removeTestSuiteListener(testSuiteListener);

        if (condition != null) {
            condition.removePropertyChangeListener(GotoCondition.TARGET_STEP_PROPERTY, propertyChangeListener);
        }

        for (int c = 0; c < testCase.getTestStepCount(); c++) {
            testCase.getTestStepAt(c).removePropertyChangeListener(TestStep.NAME_PROPERTY, propertyChangeListener);
        }
    }
}
