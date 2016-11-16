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

package com.eviware.soapui.security.scan;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.SecurityScanConfig;
import com.eviware.soapui.config.StrategyTypeConfig;
import com.eviware.soapui.config.TestAssertionConfig;
import com.eviware.soapui.impl.wsdl.AbstractWsdlModelItem;
import com.eviware.soapui.impl.wsdl.WsdlSubmitContext;
import com.eviware.soapui.impl.wsdl.support.assertions.AssertableConfig;
import com.eviware.soapui.impl.wsdl.support.assertions.AssertionsSupport;
import com.eviware.soapui.impl.wsdl.teststeps.TestRequest;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlMessageAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestRequestStep;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.TestAssertionRegistry.AssertableType;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.model.iface.MessageExchange;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.security.SecurityScan;
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.model.testsuite.AssertionError;
import com.eviware.soapui.model.testsuite.AssertionsListener;
import com.eviware.soapui.model.testsuite.ResponseAssertion;
import com.eviware.soapui.model.testsuite.SamplerTestStep;
import com.eviware.soapui.model.testsuite.TestAssertion;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.security.ExecutionStrategyHolder;
import com.eviware.soapui.security.Securable;
import com.eviware.soapui.security.SecurityTest;
import com.eviware.soapui.security.SecurityTestRunContext;
import com.eviware.soapui.security.SecurityTestRunner;
import com.eviware.soapui.security.SecurityTestRunnerImpl;
import com.eviware.soapui.security.result.SecurityResult.ResultStatus;
import com.eviware.soapui.security.result.SecurityScanRequestResult;
import com.eviware.soapui.security.result.SecurityScanResult;
import com.eviware.soapui.security.support.FailedSecurityMessageExchange;
import com.eviware.soapui.security.support.SecurityTestRunListener;

import javax.swing.JComponent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Implementation that is common for all security scans. Support for security
 * workflow.
 *
 * @author robert
 */
public abstract class AbstractSecurityScan extends AbstractWsdlModelItem<SecurityScanConfig> implements
        ResponseAssertion, SecurityScan {
    private SecurityScanResult securityScanResult;
    private SecurityScanRequestResult securityScanRequestResult;
    private TestStep testStep;
    protected AssertionsSupport assertionsSupport;

    private AssertionStatus currentStatus;
    private ExecutionStrategyHolder executionStrategy;
    private TestStep originalTestStepClone;
    private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private boolean skipFurtherRunning;

    public AbstractSecurityScan(TestStep testStep, SecurityScanConfig config, ModelItem parent, String icon) {
        super(config, parent, icon);
        if (config == null) {
            config = SecurityScanConfig.Factory.newInstance();
            setConfig(config);
        }

        this.testStep = testStep;

        if (config.getExecutionStrategy() == null) {
            config.addNewExecutionStrategy();
            config.getExecutionStrategy().setStrategy(StrategyTypeConfig.ONE_BY_ONE);
            config.getExecutionStrategy().setDelay(100);
        } else if (config.getExecutionStrategy().getStrategy() == null) {
            config.getExecutionStrategy().setStrategy(StrategyTypeConfig.ONE_BY_ONE);
            config.getExecutionStrategy().setDelay(100);
        }

		/*
         * if security scan have no strategy, set its value to
		 * StrategyTypeConfig.NO_STRATEGY.
		 */
        setExecutionStrategy(new ExecutionStrategyHolder(config.getExecutionStrategy()));

        if (config.getCheckedParameters() == null) {
            config.addNewCheckedParameters();
        }

        initAssertions();

        setApplyForFailedTestStep(config.getApplyForFailedStep());
        if (!config.isSetDisabled()) {
            setDisabled(false);
        }
    }

    @Override
    public void copyConfig(SecurityScanConfig config) {
        super.setConfig(config);
        getConfig().setType(config.getType());
        getConfig().setName(config.getName());
        getConfig().setConfig(config.getConfig());
        getConfig().setTestStep(config.getTestStep());

        TestAssertionConfig[] assertions = config.getAssertionList().toArray(new TestAssertionConfig[0]);
        getConfig().setAssertionArray(assertions);
        initAssertions();

        getConfig().setExecutionStrategy(config.getExecutionStrategy());
        setExecutionStrategy(new ExecutionStrategyHolder(config.getExecutionStrategy()));
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.eviware.soapui.security.scan.SecurityScan#updateSecurityConfig(com
     * .eviware.soapui.config.SecurityScanConfig)
     */
    public void updateSecurityConfig(SecurityScanConfig config) {
        setConfig(config);

        assertionsSupport.refresh();

        if (executionStrategy != null && config.getExecutionStrategy() != null) {
            executionStrategy.updateConfig(config.getExecutionStrategy());
        }
    }

    protected void initAssertions() {
        assertionsSupport = new AssertionsSupport(this, new AssertableConfig() {
            public TestAssertionConfig addNewAssertion() {
                return getConfig().addNewAssertion();
            }

            public List<TestAssertionConfig> getAssertionList() {
                return getConfig().getAssertionList();
            }

            public void removeAssertion(int ix) {
                getConfig().removeAssertion(ix);
            }

            public TestAssertionConfig insertAssertion(TestAssertionConfig source, int ix) {
                TestAssertionConfig conf = getConfig().insertNewAssertion(ix);
                conf.set(source);
                return conf;
            }
        });
    }

    /*
     * (non-Javadoc)
     *
     * @see com.eviware.soapui.security.scan.SecurityScan#run(com.eviware.soapui
     * .model.testsuite.TestStep,
     * com.eviware.soapui.security.SecurityTestRunContext,
     * com.eviware.soapui.security.SecurityTestRunner)
     */
    public SecurityScanResult run(TestStep testStep, SecurityTestRunContext context,
                                  SecurityTestRunner securityTestRunner) {
        securityScanResult = new SecurityScanResult(this);
        SecurityTestRunListener[] securityTestListeners = ((SecurityTest) getParent()).getSecurityTestRunListeners();

        PropertyChangeNotifier notifier = new PropertyChangeNotifier();
        boolean noMutations = true;
        while (hasNext(testStep, context)) {
            noMutations = false;
            if (((SecurityTestRunnerImpl) securityTestRunner).isCanceled()) {
                securityScanResult.setStatus(ResultStatus.CANCELED);
                clear();
                return securityScanResult;
            }
            securityScanRequestResult = new SecurityScanRequestResult(this);
            securityScanRequestResult.startTimer();
            originalTestStepClone = ((SecurityTestRunnerImpl) securityTestRunner)
                    .cloneForSecurityScan((WsdlTestStep) this.testStep);
            execute(securityTestRunner, originalTestStepClone, context);
            notifier.notifyChange();
            securityScanRequestResult.stopTimer();
            assertResponse(getSecurityScanRequestResult().getMessageExchange(), context);
            // add to summary result
            securityScanResult.addSecurityRequestResult(getSecurityScanRequestResult());
            for (int i = 0; i < securityTestListeners.length; i++) {
                if (Arrays.asList(((SecurityTest) getParent()).getSecurityTestRunListeners()).contains(
                        securityTestListeners[i])) {
                    securityTestListeners[i].afterSecurityScanRequest((SecurityTestRunnerImpl) securityTestRunner,
                            context, securityScanRequestResult);
                }
            }

            try {
                Thread.sleep(getExecutionStrategy().getDelay());
            } catch (InterruptedException e) {
                SoapUI.logError(e, "Security Scan Request Delay Interrupted!");
            }
        }

        if (noMutations) {
            securityScanResult.setStatus(ResultStatus.SKIPPED);
        }
        return securityScanResult;
    }

    protected void clear() {

    }

    /**
     * should be implemented in every particular scan it executes one request,
     * modified by securityScan if necessary and internally adds messages for
     * logging to SecurityScanRequestResult
     */
    abstract protected void execute(SecurityTestRunner runner, TestStep testStep, SecurityTestRunContext context);

    /**
     * checks if specific SecurityScan still has modifications left
     *
     * @param testStep2
     * @param context
     */
    abstract protected boolean hasNext(TestStep testStep2, SecurityTestRunContext context);

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.eviware.soapui.security.scan.SecurityScan#isConfigurable()
	 */

    public boolean isConfigurable() {
        return true;
    }

    /**
     * Overide if SecurityScan have Optional component
     */
    @Override
    public JComponent getComponent() {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.eviware.soapui.security.scan.SecurityScan#getType()
     */
    public abstract String getType();

    /*
     * (non-Javadoc)
     *
     * @see com.eviware.soapui.security.scan.SecurityScan#getTestStep()
     */
    public TestStep getTestStep() {
        return testStep;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.eviware.soapui.security.scan.SecurityScan#setTestStep(com.eviware
     * .soapui.model.testsuite.TestStep)
     */
    public void setTestStep(TestStep step) {
        testStep = step;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.eviware.soapui.security.scan.SecurityScan#isDisabled()
     */
    public boolean isDisabled() {
        return getConfig().getDisabled();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.eviware.soapui.security.scan.SecurityScan#setDisabled(boolean)
     */
    public void setDisabled(boolean disabled) {
        boolean oldValue = isDisabled();
        getConfig().setDisabled(disabled);
        pcs.firePropertyChange("disabled", oldValue, disabled);
    }

    public static boolean isSecurable(TestStep testStep) {
        if (testStep != null && testStep instanceof Securable) {
            return true;
        } else {
            return false;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.eviware.soapui.security.scan.SecurityScan#getExecutionStrategy()
     */
    public ExecutionStrategyHolder getExecutionStrategy() {
        return this.executionStrategy;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.eviware.soapui.security.scan.SecurityScan#setExecutionStrategy(com
     * .eviware.soapui.security.ExecutionStrategyHolder)
     */
    public void setExecutionStrategy(ExecutionStrategyHolder executionStrategy) {
        ExecutionStrategyHolder oldValue = getExecutionStrategy();
        this.executionStrategy = executionStrategy;
        pcs.firePropertyChange("executionStrategy", oldValue, executionStrategy);
    }

    protected TestRequest getOriginalResult(SecurityTestRunnerImpl securityRunner, TestStep testStep) {
        testStep.run(securityRunner, securityRunner.getRunContext());

        return getRequest(testStep);
    }

    protected TestRequest getRequest(TestStep testStep) {
        if (testStep instanceof SamplerTestStep) {
            return ((SamplerTestStep) testStep).getTestRequest();
        }
        return null;
    }

    private class PropertyChangeNotifier {
        private ResultStatus oldStatus;

        public PropertyChangeNotifier() {
            oldStatus = getSecurityStatus();
        }

        public void notifyChange() {
            ResultStatus newStatus = getSecurityStatus();

            if (oldStatus != newStatus) {
                notifyPropertyChanged(STATUS_PROPERTY, oldStatus, newStatus);
            }

            oldStatus = newStatus;
        }
    }

    @Override
    public TestAssertion addAssertion(String label) {
        PropertyChangeNotifier notifier = new PropertyChangeNotifier();
        try {
            WsdlMessageAssertion assertion = assertionsSupport.addWsdlAssertion(label);
            if (assertion == null) {
                return null;
            }

            if (getAssertableContentAsXml() != null) {
                assertRequests(assertion);
                assertResponses(assertion);
                notifier.notifyChange();
            }

            return assertion;
        } catch (Exception e) {
            SoapUI.logError(e);
            return null;
        }
    }

    /**
     * @param assertion run all responses against this assertion
     */
    private void assertResponses(WsdlMessageAssertion assertion) {
        if (securityScanResult != null) {
            for (SecurityScanRequestResult result : securityScanResult.getSecurityRequestResultList()) {
                if (result.getMessageExchange() == null) {
                    return;
                }

                assertion.assertResponse(result.getMessageExchange(), new WsdlSubmitContext(testStep));
            }
        }
    }

    /**
     * @param assertion run all request against this assertion
     */
    private void assertRequests(WsdlMessageAssertion assertion) {
        if (securityScanResult != null) {
            for (SecurityScanRequestResult result : securityScanResult.getSecurityRequestResultList()) {
                if (result.getMessageExchange() == null) {
                    return;
                }

                assertion.assertRequest(result.getMessageExchange(), new WsdlSubmitContext(testStep));
            }
        }
    }

    @Override
    public void removeAssertion(TestAssertion assertion) {
        PropertyChangeNotifier notifier = new PropertyChangeNotifier();

        try {
            assertionsSupport.removeAssertion((WsdlMessageAssertion) assertion);

        } finally {
            ((WsdlMessageAssertion) assertion).release();
            notifier.notifyChange();
        }
    }

    @Override
    public TestAssertion moveAssertion(int ix, int offset) {
        WsdlMessageAssertion assertion = getAssertionAt(ix);
        PropertyChangeNotifier notifier = new PropertyChangeNotifier();

        try {
            return assertionsSupport.moveAssertion(ix, offset);
        } finally {
            ((WsdlMessageAssertion) assertion).release();
            notifier.notifyChange();
        }
    }

    @Override
    public WsdlMessageAssertion getAssertionAt(int c) {
        return assertionsSupport.getAssertionAt(c);
    }

    @Override
    public void addAssertionsListener(AssertionsListener listener) {
        assertionsSupport.addAssertionsListener(listener);
    }

    @Override
    public void removeAssertionsListener(AssertionsListener listener) {
        assertionsSupport.removeAssertionsListener(listener);
    }

    @Override
    public int getAssertionCount() {
        return assertionsSupport.getAssertionCount();
    }

    @Override
    public AssertionStatus getAssertionStatus() {
        int cnt = getAssertionCount();
        if (cnt == 0) {
            return currentStatus;
        }

        if (securityScanResult != null && securityScanResult.getStatus() == ResultStatus.OK) {
            currentStatus = AssertionStatus.VALID;
        } else {
            currentStatus = AssertionStatus.FAILED;
        }

        return currentStatus;
    }

    public ResultStatus getSecurityStatus() {
        return securityScanResult != null ? securityScanResult.getStatus() : ResultStatus.UNKNOWN;
    }

    @Override
    public String getAssertableContentAsXml() {
        if (testStep instanceof Assertable) {
            return ((Assertable) testStep).getAssertableContentAsXml();
        }

        return null;
    }

    @Override
    public String getAssertableContent() {
        if (testStep instanceof Assertable) {
            return ((Assertable) testStep).getAssertableContent();
        }

        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.eviware.soapui.model.testsuite.Assertable#getAssertableType()
     *
     * Decided to go with assertions on request and response so we can implement
     * "men in the middle" attacks using monitor.
     */
    @Override
    public AssertableType getAssertableType() {
        return AssertableType.BOTH;
    }

    @Override
    public TestAssertion getAssertionByName(String name) {
        return assertionsSupport.getAssertionByName(name);
    }

    @Override
    public List<TestAssertion> getAssertionList() {
        return new ArrayList<TestAssertion>(assertionsSupport.getAssertionList());
    }

    @Override
    public Map<String, TestAssertion> getAssertions() {
        return assertionsSupport.getAssertions();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.eviware.soapui.security.scan.SecurityScan#getAssertionsSupport()
     */
    public AssertionsSupport getAssertionsSupport() {
        return assertionsSupport;
    }

    @Override
    public TestAssertion cloneAssertion(TestAssertion source, String name) {
        return assertionsSupport.cloneAssertion(source, name);
    }

    @Override
    public String getDefaultAssertableContent() {
        if (testStep instanceof Assertable) {
            return ((Assertable) testStep).getDefaultAssertableContent();
        }

        return null;
    }

    @Override
    public Interface getInterface() {
        if (testStep instanceof WsdlTestRequestStep) {
            return ((WsdlTestRequestStep) testStep).getInterface();
        }

        return null;
    }

    @Override
    public ModelItem getModelItem() {
        return this;
    }

    public AssertionStatus assertResponse(MessageExchange messageExchange, SubmitContext context) {
        AssertionStatus finalResult = null;

        try {
            PropertyChangeNotifier notifier = new PropertyChangeNotifier();

            if (messageExchange != null) {
                context.setProperty(SECURITY_SCAN_REQUEST_RESULT, getSecurityScanRequestResult());

                for (WsdlMessageAssertion assertion : assertionsSupport.getAssertionList()) {
                    AssertionStatus currentResult = assertion.assertResponse(messageExchange, context);
                    updateMessages(currentResult, assertion);

                    if (finalResult == null || finalResult != AssertionStatus.FAILED) {
                        finalResult = currentResult;
                    }
                }

                setStatus(finalResult);

                notifier.notifyChange();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return finalResult;
    }

    /**
     * Sets SecurityScanStatus based on the status of all assertions added
     *
     * @param result
     */
    private void setStatus(AssertionStatus result) {
        if (result == AssertionStatus.FAILED) {
            getSecurityScanRequestResult().setStatus(ResultStatus.FAILED);
        } else if (result == AssertionStatus.VALID) {
            getSecurityScanRequestResult().setStatus(ResultStatus.OK);

        } else if (result == AssertionStatus.UNKNOWN) {
            getSecurityScanRequestResult().setStatus(ResultStatus.UNKNOWN);
        }
    }

    private void updateMessages(AssertionStatus result, WsdlMessageAssertion assertion) {
        if (result == AssertionStatus.FAILED) {
            for (AssertionError error : assertion.getErrors()) {
                getSecurityScanRequestResult().addMessage(error.getMessage());
            }
        }
    }

    // name used in configuration panel
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.eviware.soapui.security.scan.SecurityScan#getConfigName()
	 */
    public abstract String getConfigName();

    // description usd in configuration panel
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.eviware.soapui.security.scan.SecurityScan#getConfigDescription()
	 */
    public abstract String getConfigDescription();

    // help url used for configuration panel ( help for this scan )
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.eviware.soapui.security.scan.SecurityScan#getHelpURL()
	 */
    public abstract String getHelpURL();

    protected void setSecurityScanRequestResult(SecurityScanRequestResult securityScanRequestResult) {
        this.securityScanRequestResult = securityScanRequestResult;
    }

    protected SecurityScanRequestResult getSecurityScanRequestResult() {
        return securityScanRequestResult;
    }

    /**
     * Overide if SecurityScan needs advanced settings
     */
    @Override
    public JComponent getAdvancedSettingsPanel() {
        return null;
    }

    @Override
    public SecurityScanResult getSecurityScanResult() {
        return securityScanResult;
    }

    /**
     * @param message
     */
    protected void reportSecurityScanException(String message) {
        getSecurityScanRequestResult().setMessageExchange(new FailedSecurityMessageExchange());
        getSecurityScanRequestResult().setStatus(ResultStatus.FAILED);
        getSecurityScanRequestResult().addMessage(message);
    }

    @Override
    public void addWsdlAssertion(String assertionLabel) {
        assertionsSupport.addWsdlAssertion(assertionLabel);
    }

    @Override
    public boolean isApplyForFailedStep() {
        return getConfig().getApplyForFailedStep();
    }

    @Override
    public void setApplyForFailedTestStep(boolean apply) {
        getConfig().setApplyForFailedStep(apply);
    }

    @Override
    public boolean isRunOnlyOnce() {
        return getConfig().getRunOnlyOnce();
    }

    @Override
    public void setRunOnlyOnce(boolean runOnlyOnce) {
        getConfig().setRunOnlyOnce(runOnlyOnce);
    }

    public void release() {
        if (assertionsSupport != null) {
            assertionsSupport.release();
        }

        if (securityScanResult != null) {
            securityScanResult.release();
        }

        if (securityScanRequestResult != null) {
            securityScanRequestResult.release();
        }

    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    @Override
    public boolean isSkipFurtherRunning() {
        return skipFurtherRunning;
    }

    @Override
    public void setSkipFurtherRunning(boolean skipFurtherRunning) {
        this.skipFurtherRunning = skipFurtherRunning;
    }

}
