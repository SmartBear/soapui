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

package com.eviware.soapui.impl.wsdl.loadtest.strategy;

import com.eviware.soapui.config.LoadTestLimitTypesConfig;
import com.eviware.soapui.impl.wsdl.loadtest.WsdlLoadTest;
import com.eviware.soapui.model.testsuite.LoadTestRunContext;
import com.eviware.soapui.model.testsuite.LoadTestRunner;
import com.eviware.soapui.model.testsuite.TestCaseRunContext;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.TestStepResult;
import org.apache.xmlbeans.XmlObject;

import javax.swing.JComponent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * LoadStrategy allowing maximum runs and request delays
 *
 * @author Ole.Matzura
 */

public abstract class AbstractLoadStrategy implements LoadStrategy {
    private PropertyChangeSupport propertyChangeSupport;
    private final String type;
    private final WsdlLoadTest loadTest;

    public AbstractLoadStrategy(String type, WsdlLoadTest loadTest) {
        this.type = type;
        this.loadTest = loadTest;
        propertyChangeSupport = new PropertyChangeSupport(this);
    }

    public XmlObject getConfig() {
        return null;
    }

    public JComponent getConfigurationPanel() {
        return null;
    }

    public String getType() {
        return type;
    }

    public WsdlLoadTest getLoadTest() {
        return loadTest;
    }

    public void addConfigurationChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(CONFIGURATION_PROPERTY, listener);
    }

    public void removeConfigurationChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(CONFIGURATION_PROPERTY, listener);
    }

    public void notifyConfigurationChanged() {
        propertyChangeSupport.firePropertyChange(CONFIGURATION_PROPERTY, null, null);
    }

    public boolean allowThreadCountChangeDuringRun() {
        return true;
    }

    public void afterLoadTest(LoadTestRunner loadTestRunner, LoadTestRunContext context) {
    }

    public void afterTestCase(LoadTestRunner loadTestRunner, LoadTestRunContext context, TestCaseRunner testRunner,
                              TestCaseRunContext runContext) {
    }

    public void afterTestStep(LoadTestRunner loadTestRunner, LoadTestRunContext context, TestCaseRunner testRunner,
                              TestCaseRunContext runContext, TestStepResult testStepResult) {
    }

    public void beforeLoadTest(LoadTestRunner loadTestRunner, LoadTestRunContext context) {
        if (getLoadTest().getLimitType() == LoadTestLimitTypesConfig.COUNT
                && getLoadTest().getTestLimit() < getLoadTest().getThreadCount()) {
            getLoadTest().setThreadCount(getLoadTest().getTestLimit());
        }
    }

    public void beforeTestCase(LoadTestRunner loadTestRunner, LoadTestRunContext context, TestCaseRunner testRunner,
                               TestCaseRunContext runContext) {
    }

    public void beforeTestStep(LoadTestRunner loadTestRunner, LoadTestRunContext context, TestCaseRunner testRunner,
                               TestCaseRunContext runContext, TestStep testStep) {
    }

    public void loadTestStarted(LoadTestRunner loadTestRunner, LoadTestRunContext context) {
    }

    public void loadTestStopped(LoadTestRunner loadTestRunner, LoadTestRunContext context) {
    }

    public void recalculate(LoadTestRunner loadTestRunner, LoadTestRunContext context) {
    }

    public void updateConfig(XmlObject config) {
    }
}
