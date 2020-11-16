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

package com.eviware.soapui.impl.wsdl.loadtest.assertions;

import com.eviware.soapui.config.LoadTestAssertionConfig;
import com.eviware.soapui.impl.wsdl.loadtest.LoadTestAssertion;
import com.eviware.soapui.impl.wsdl.loadtest.WsdlLoadTest;
import com.eviware.soapui.impl.wsdl.support.Configurable;
import com.eviware.soapui.model.support.ModelSupport;
import com.eviware.soapui.model.support.TestSuiteListenerAdapter;
import com.eviware.soapui.model.testsuite.LoadTestRunContext;
import com.eviware.soapui.model.testsuite.LoadTestRunner;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.support.UISupport;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.xmlbeans.XmlObject;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * Base class for LoadTestAssertions
 *
 * @author Ole.Matzura
 */

public abstract class AbstractLoadTestAssertion implements LoadTestAssertion {
    private LoadTestAssertionConfig assertionConfig;
    @SuppressWarnings("unused")
    private final static Logger log = LogManager.getLogger(AbstractLoadTestAssertion.class);
    private ImageIcon icon;
    private final WsdlLoadTest loadTest;
    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    private String testStepName;
    private TestStep testStep;
    private TestStepPropertyChangeListener testStepPropertyChangeListener = new TestStepPropertyChangeListener();
    private InternalTestSuiteListener testSuiteListener = new InternalTestSuiteListener();

    protected static final String TEST_STEP_ELEMENT = "test-step";
    protected static final String TEST_STEP_FIELD = "TestStep";

    public AbstractLoadTestAssertion(LoadTestAssertionConfig assertionConfig, WsdlLoadTest loadTest) {
        this.assertionConfig = assertionConfig;
        this.loadTest = loadTest;

        loadTest.getTestCase().getTestSuite().addTestSuiteListener(testSuiteListener);
    }

    public void initIcon(String url) {
        icon = UISupport.createImageIcon(url);
    }

    public LoadTestAssertionConfig getConfiguration() {
        return assertionConfig;
    }

    public void updateConfiguration(LoadTestAssertionConfig configuration) {
        assertionConfig = configuration;
    }

    protected void setConfiguration(XmlObject configuration) {
        XmlObject oldConfig = assertionConfig.getConfiguration();
        assertionConfig.setConfiguration(configuration);
        propertyChangeSupport.firePropertyChange(AbstractLoadTestAssertion.CONFIGURATION_PROPERTY, oldConfig,
                configuration);
    }

    public String getName() {
        return assertionConfig.isSetName() ? assertionConfig.getName() : assertionConfig.getType();
    }

    public void setName(String name) {
        String old = getName();
        assertionConfig.setName(name);
        propertyChangeSupport.firePropertyChange(NAME_PROPERTY, old, name);
    }

    public WsdlLoadTest getLoadTest() {
        return loadTest;
    }

    public class RenameAssertionAction extends AbstractAction {
        public RenameAssertionAction() {
            super("Rename");
            putValue(Action.SHORT_DESCRIPTION, "Renames this assertion");
        }

        public void actionPerformed(ActionEvent e) {
            String name = UISupport.prompt("Specify name for this assertion", "Rename Assertion",
                    AbstractLoadTestAssertion.this.getName());
            if (name == null || name.equals(AbstractLoadTestAssertion.this.getName())) {
                return;
            }

            setName(name);
        }
    }

    public class ConfigureAssertionAction extends AbstractAction {
        public ConfigureAssertionAction() {
            super("Configure");
            putValue(Action.SHORT_DESCRIPTION, "Configures this assertion");
        }

        public void actionPerformed(ActionEvent e) {
            ((Configurable) AbstractLoadTestAssertion.this).configure();
        }
    }

    public ImageIcon getIcon() {
        return icon;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(propertyName, listener);
    }

    protected String returnErrorOrFail(String message, int maxErrors, LoadTestRunner testRunner,
                                       LoadTestRunContext context) {
        String propertyKey = getClass().getName() + hashCode();
        Long errorCount = (Long) context.getProperty(propertyKey);

        if (errorCount == null) {
            errorCount = 1L;
        } else {
            errorCount = new Long(errorCount.longValue() + 1);
        }

        if (maxErrors >= 0 && errorCount >= maxErrors) {
            testRunner.fail("Maximum number of errors [" + maxErrors + "] for assertion [" + getName() + "] exceeded");
        }

        context.setProperty(propertyKey, errorCount);

        return message;
    }

    public String getTargetStep() {
        return testStepName;
    }

    public void setTargetStep(String name) {
        testStepName = name;
        initTestStep();
    }

    abstract protected void updateConfiguration();

    protected boolean targetStepMatches(TestStep testStep) {
        return testStepName == null || testStepName.equals(ANY_TEST_STEP) || testStep.getName().equals(testStepName);
    }

    protected String[] getTargetStepOptions(boolean includeAll) {
        if (includeAll) {
            return ModelSupport.getNames(new String[]{ANY_TEST_STEP, ALL_TEST_STEPS}, getLoadTest().getTestCase()
                    .getTestStepList());
        } else {
            return ModelSupport.getNames(new String[]{ANY_TEST_STEP}, getLoadTest().getTestCase().getTestStepList());
        }
    }

    private void initTestStep() {
        if (testStep != null) {
            testStep.removePropertyChangeListener(testStepPropertyChangeListener);
        }

        testStep = getLoadTest().getTestCase().getTestStepByName(testStepName);
        if (testStep != null) {
            testStep.addPropertyChangeListener(TestStep.NAME_PROPERTY, testStepPropertyChangeListener);
        }
    }

    public void release() {
        if (testStep != null) {
            testStep.removePropertyChangeListener(testStepPropertyChangeListener);
        }

        loadTest.getTestCase().getTestSuite().removeTestSuiteListener(testSuiteListener);
    }

    private final class InternalTestSuiteListener extends TestSuiteListenerAdapter {
        public void testStepRemoved(TestStep removedTestStep, int index) {
            if (removedTestStep.getName().equals(testStepName)
                    && removedTestStep.getTestCase() == testStep.getTestCase()) {
                testStepName = ANY_TEST_STEP;
                updateConfiguration();
            }
        }
    }

    private final class TestStepPropertyChangeListener implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
            testStepName = evt.getNewValue().toString();
            updateConfiguration();
        }
    }
}
