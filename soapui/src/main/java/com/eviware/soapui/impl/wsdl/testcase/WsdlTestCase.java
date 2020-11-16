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

package com.eviware.soapui.impl.wsdl.testcase;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.analytics.Analytics;
import com.eviware.soapui.analytics.SoapUIActions;
import com.eviware.soapui.config.LoadTestConfig;
import com.eviware.soapui.config.SecurityTestConfig;
import com.eviware.soapui.config.TestCaseConfig;
import com.eviware.soapui.config.TestStepConfig;
import com.eviware.soapui.config.WsrmVersionTypeConfig;
import com.eviware.soapui.impl.wsdl.AbstractTestPropertyHolderWsdlModelItem;
import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.impl.wsdl.loadtest.LoadTestAssertion;
import com.eviware.soapui.impl.wsdl.loadtest.WsdlLoadTest;
import com.eviware.soapui.impl.wsdl.loadtest.assertions.TestStepStatusAssertion;
import com.eviware.soapui.impl.wsdl.panels.teststeps.amf.AMFTestRunListener;
import com.eviware.soapui.impl.wsdl.support.wsrm.WsrmTestRunListener;
import com.eviware.soapui.impl.wsdl.support.wsrm.WsrmUtils;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.registry.HttpRequestStepFactory;
import com.eviware.soapui.impl.wsdl.teststeps.registry.WsdlTestStepFactory;
import com.eviware.soapui.impl.wsdl.teststeps.registry.WsdlTestStepRegistry;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.security.SecurityScan;
import com.eviware.soapui.model.support.ModelSupport;
import com.eviware.soapui.model.testsuite.LoadTest;
import com.eviware.soapui.model.testsuite.TestCase;
import com.eviware.soapui.model.testsuite.TestCaseRunContext;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.TestRunListener;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.security.SecurityTest;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.swing.ActionList;
import com.eviware.soapui.support.action.swing.DefaultActionList;
import com.eviware.soapui.support.resolver.ResolveDialog;
import com.eviware.soapui.support.scripting.SoapUIScriptEngine;
import com.eviware.soapui.support.scripting.SoapUIScriptEngineRegistry;
import com.eviware.soapui.support.types.StringToObjectMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

/**
 * TestCase implementation for WSDL projects
 *
 * @author Ole.Matzura
 */

public class WsdlTestCase extends AbstractTestPropertyHolderWsdlModelItem<TestCaseConfig> implements TestCase {
    private final static Logger logger = LogManager.getLogger(WsdlTestCase.class);
    public final static String KEEP_SESSION_PROPERTY = WsdlTestCase.class.getName() + "@keepSession";
    public final static String FAIL_ON_ERROR_PROPERTY = WsdlTestCase.class.getName() + "@failOnError";
    public final static String FAIL_ON_ERRORS_PROPERTY = WsdlTestCase.class.getName() + "@failOnErrors";
    public final static String DISCARD_OK_RESULTS = WsdlTestCase.class.getName() + "@discardOkResults";
    public final static String SETUP_SCRIPT_PROPERTY = WsdlTestCase.class.getName() + "@setupScript";
    public final static String TEARDOWN_SCRIPT_PROPERTY = WsdlTestCase.class.getName() + "@tearDownScript";
    public static final String TIMEOUT_PROPERTY = WsdlTestCase.class.getName() + "@timeout";
    public static final String SEARCH_PROPERTIES_PROPERTY = WsdlTestCase.class.getName() + "@searchProperties";
    public static final String ICON_NAME = "/testcase.png";

    private final WsdlTestSuite testSuite;
    private final List<WsdlTestStep> testSteps = new ArrayList<WsdlTestStep>();
    private final List<WsdlLoadTest> loadTests = new ArrayList<WsdlLoadTest>();
    private final List<SecurityTest> securityTests = new ArrayList<SecurityTest>();
    private final Set<TestRunListener> testRunListeners = new HashSet<TestRunListener>();
    private DefaultActionList createActions;
    private final boolean forLoadTest;
    private SoapUIScriptEngine setupScriptEngine;
    private SoapUIScriptEngine tearDownScriptEngine;
    /**
     * runFromHereContext is used only for run from here action
     * TODO: runFromHereContext is only used from UI and should be moved in a UI model. For more information
     * SOAP-165
     */
    private StringToObjectMap runFromHereContext = new StringToObjectMap();

    public WsdlTestCase(WsdlTestSuite testSuite, TestCaseConfig config, boolean forLoadTest) {
        super(config, testSuite, ICON_NAME);

        this.testSuite = testSuite;
        this.forLoadTest = forLoadTest;

        if (!getConfig().isSetProperties()) {
            getConfig().addNewProperties();
        }

        setPropertiesConfig(getConfig().getProperties());

        List<TestStepConfig> testStepConfigs = config.getTestStepList();
        List<TestStepConfig> removed = new ArrayList<TestStepConfig>();
        for (TestStepConfig tsc : testStepConfigs) {
            WsdlTestStep testStep = createTestStepFromConfig(tsc);
            if (testStep != null) {
                ensureUniqueName(testStep);
                testSteps.add(testStep);
            } else {
                removed.add(tsc);
            }
        }

        if (removed.size() > 0) {
            testStepConfigs.removeAll(removed);
        }

        if (!forLoadTest) {
            List<LoadTestConfig> loadTestConfigs = config.getLoadTestList();
            for (LoadTestConfig tsc : loadTestConfigs) {
                WsdlLoadTest loadTest = buildLoadTest(tsc);
                loadTests.add(loadTest);
            }
        }

        List<SecurityTestConfig> securityTestConfigs = config.getSecurityTestList();
        for (SecurityTestConfig tsc : securityTestConfigs) {
            SecurityTest securityTest = buildSecurityTest(tsc);
            securityTests.add(securityTest);
        }

        // init default configs
        if (!config.isSetFailOnError()) {
            config.setFailOnError(true);
        }

        if (!config.isSetFailTestCaseOnErrors()) {
            config.setFailTestCaseOnErrors(true);
        }

        if (!config.isSetKeepSession()) {
            config.setKeepSession(false);
        }

        if (!config.isSetMaxResults()) {
            config.setMaxResults(0);
        }

        for (TestRunListener listener : SoapUI.getListenerRegistry().getListeners(TestRunListener.class)) {
            addTestRunListener(listener);
        }

        WsrmTestRunListener wsrmListener = new WsrmTestRunListener();

        addTestRunListener(wsrmListener);
        addTestRunListener(new AMFTestRunListener());
    }

    public boolean isForLoadTest() {
        return forLoadTest;
    }

    public WsdlLoadTest buildLoadTest(LoadTestConfig tsc) {
        return new WsdlLoadTest(this, tsc);
    }

    public boolean getKeepSession() {
        return getConfig().getKeepSession();
    }

    public void setKeepSession(boolean keepSession) {
        boolean old = getKeepSession();
        if (old != keepSession) {
            getConfig().setKeepSession(keepSession);
            notifyPropertyChanged(KEEP_SESSION_PROPERTY, old, keepSession);
        }
    }

    public void setSetupScript(String script) {
        String oldScript = getSetupScript();

        if (!getConfig().isSetSetupScript()) {
            getConfig().addNewSetupScript();
        }

        getConfig().getSetupScript().setStringValue(script);
        if (setupScriptEngine != null) {
            setupScriptEngine.setScript(script);
        }

        notifyPropertyChanged(SETUP_SCRIPT_PROPERTY, oldScript, script);
    }

    public String getSetupScript() {
        return getConfig().isSetSetupScript() ? getConfig().getSetupScript().getStringValue() : null;
    }

    public void setTearDownScript(String script) {
        String oldScript = getTearDownScript();

        if (!getConfig().isSetTearDownScript()) {
            getConfig().addNewTearDownScript();
        }

        getConfig().getTearDownScript().setStringValue(script);
        if (tearDownScriptEngine != null) {
            tearDownScriptEngine.setScript(script);
        }

        notifyPropertyChanged(TEARDOWN_SCRIPT_PROPERTY, oldScript, script);
    }

    public String getTearDownScript() {
        return getConfig().isSetTearDownScript() ? getConfig().getTearDownScript().getStringValue() : null;
    }

    public boolean getFailOnError() {
        return getConfig().getFailOnError();
    }

    public boolean getFailTestCaseOnErrors() {
        return getConfig().getFailTestCaseOnErrors();
    }

    public void setFailOnError(boolean failOnError) {
        boolean old = getFailOnError();
        if (old != failOnError) {
            getConfig().setFailOnError(failOnError);
            notifyPropertyChanged(FAIL_ON_ERROR_PROPERTY, old, failOnError);
        }
    }

    public void setFailTestCaseOnErrors(boolean failTestCaseOnErrors) {
        boolean old = getFailTestCaseOnErrors();
        if (old != failTestCaseOnErrors) {
            getConfig().setFailTestCaseOnErrors(failTestCaseOnErrors);
            notifyPropertyChanged(FAIL_ON_ERRORS_PROPERTY, old, failTestCaseOnErrors);
        }
    }

    public boolean getSearchProperties() {
        return getConfig().getSearchProperties();
    }

    public void setSearchProperties(boolean searchProperties) {
        boolean old = getSearchProperties();
        if (old != searchProperties) {
            getConfig().setSearchProperties(searchProperties);
            notifyPropertyChanged(SEARCH_PROPERTIES_PROPERTY, old, searchProperties);
        }
    }

    public boolean getDiscardOkResults() {
        return getConfig().getDiscardOkResults();
    }

    public void setDiscardOkResults(boolean discardOkResults) {
        boolean old = getDiscardOkResults();
        if (old != discardOkResults) {
            getConfig().setDiscardOkResults(discardOkResults);
            notifyPropertyChanged(DISCARD_OK_RESULTS, old, discardOkResults);
        }
    }

    public int getMaxResults() {
        return getConfig().getMaxResults();
    }

    public void setMaxResults(int maxResults) {
        int old = getMaxResults();
        if (old != maxResults) {
            getConfig().setMaxResults(maxResults);
            notifyPropertyChanged("maxResults", old, maxResults);
        }
    }

    private WsdlTestStep createTestStepFromConfig(TestStepConfig tsc) {
        WsdlTestStepFactory factory = WsdlTestStepRegistry.getInstance().getFactory(tsc.getType());
        if (factory != null) {
            WsdlTestStep testStep = factory.buildTestStep(this, tsc, forLoadTest);
            return testStep;
        } else {
            logger.error("Failed to create test step for [" + tsc.getName() + "]");
            return null;
        }
    }

    private boolean ensureUniqueName(WsdlTestStep testStep) {
        String name = testStep.getName();
        while (name == null || getTestStepByName(name.trim()) != null) {
            if (name == null) {
                name = testStep.getName();
            } else {
                int cnt = 0;

                while (getTestStepByName(name.trim()) != null) {
                    cnt++;
                    name = testStep.getName() + " " + cnt;
                }

                if (cnt == 0) {
                    break;
                }
            }

            name = UISupport.prompt(
                    "TestStep name must be unique, please specify new name for step\n" + "[" + testStep.getName()
                            + "] in TestCase [" + getTestSuite().getProject().getName() + "->" + getTestSuite().getName()
                            + "->" + getName() + "]", "Change TestStep name", name);

            if (name == null) {
                return false;
            }
        }

        if (!name.equals(testStep.getName())) {
            testStep.setName(name);
        }

        return true;
    }

    public WsdlLoadTest addNewLoadTest(String name) {
        WsdlLoadTest loadTest = buildLoadTest(getConfig().addNewLoadTest());
        loadTest.setStartDelay(0);
        loadTest.setName(name);
        loadTests.add(loadTest);

        loadTest.addAssertion(TestStepStatusAssertion.STEP_STATUS_TYPE, LoadTestAssertion.ANY_TEST_STEP, false);

        (getTestSuite()).fireLoadTestAdded(loadTest);

        return loadTest;
    }

    public void removeLoadTest(WsdlLoadTest loadTest) {
        int ix = loadTests.indexOf(loadTest);

        loadTests.remove(ix);

        try {
            (getTestSuite()).fireLoadTestRemoved(loadTest);
        } finally {
            loadTest.release();
            getConfig().removeLoadTest(ix);
        }
    }

    @Override
    public WsdlTestSuite getTestSuite() {
        return testSuite;
    }

    public WsdlTestStep cloneStep(WsdlTestStep testStep, String name) {
        return testStep.clone(this, name);
    }

    @Override
    @Nonnull
    public WsdlTestStep getTestStepAt(int index) {
        return testSteps.get(index);
    }

    @Override
    public int getTestStepCount() {
        return testSteps.size();
    }

    @Override
    public WsdlLoadTest getLoadTestAt(int index) {
        return loadTests.get(index);
    }

    @Override
    public LoadTest getLoadTestByName(String loadTestName) {
        return (LoadTest) getWsdlModelItemByName(loadTests, loadTestName);
    }

    @Override
    public int getLoadTestCount() {
        return loadTests.size();
    }

    public WsdlTestStep addTestStep(TestStepConfig stepConfig) {
        return insertTestStep(null, stepConfig, -1, true);
    }

    public WsdlTestStep addTestStep(String type, String name) {
        WsdlTestStepFactory testStepFactory = WsdlTestStepRegistry.getInstance().getFactory(type);
        if (testStepFactory != null) {
            TestStepConfig newStepConfig = testStepFactory.createNewTestStep(this, name);
            if (newStepConfig != null) {
                return addTestStep(newStepConfig);
            }
        }

        return null;
    }

    public WsdlTestStep addTestStep(String type, String name, String endpoint, String method) {
        WsdlTestStepFactory requestStepFactory = WsdlTestStepRegistry.getInstance().getFactory(type);
        if (requestStepFactory instanceof HttpRequestStepFactory) {
            TestStepConfig newStepConfig = ((HttpRequestStepFactory) requestStepFactory).createNewTestStep(this, name, endpoint, method);
            if (newStepConfig != null) {
                return addTestStep(newStepConfig);
            }
        }

        return null;
    }

    public WsdlTestStep insertTestStep(String type, String name, int index) {
        WsdlTestStepFactory testStepFactory = WsdlTestStepRegistry.getInstance().getFactory(type);
        if (testStepFactory != null) {
            TestStepConfig newStepConfig = testStepFactory.createNewTestStep(this, name);
            if (newStepConfig != null) {
                return insertTestStep(null, newStepConfig, index, false);
            }
        }

        return null;
    }

    public WsdlTestStep importTestStep(WsdlTestStep testStep, String name, int index, boolean createCopy) {
        testStep.beforeSave();
        TestStepConfig newStepConfig = (TestStepConfig) testStep.getConfig().copy();
        newStepConfig.setName(name);

        WsdlTestStep result = insertTestStep(testStep.getTestCase(), newStepConfig, index, createCopy);
        if (result == null) {
            return null;
        }

        if (createCopy) {
            ModelSupport.createNewIds(result);
        }

        resolveTestCase();
        return result;
    }

    private void resolveTestCase() {
        ResolveDialog resolver = new ResolveDialog("Validate TestCase", "Checks TestCase for inconsistencies", null);
        resolver.setShowOkMessage(false);
        resolver.resolve(this);
    }

    public WsdlTestStep[] importTestSteps(WsdlTestCase oldTestCase, WsdlTestStep[] testSteps, int index, boolean createCopies) {
        TestStepConfig[] newStepConfigs = new TestStepConfig[testSteps.length];

        for (int c = 0; c < testSteps.length; c++) {
            testSteps[c].beforeSave();
            newStepConfigs[c] = (TestStepConfig) testSteps[c].getConfig().copy();
        }

        WsdlTestStep[] result = insertTestSteps(oldTestCase, newStepConfigs, index, createCopies);

        resolveTestCase();
        return result;
    }

    public WsdlTestStep insertTestStep(TestStepConfig stepConfig, int ix) {
        return insertTestStep(null, stepConfig, ix, true);
    }

    public WsdlTestStep insertTestStep(WsdlTestCase oldTestCase, TestStepConfig stepConfig, int ix, boolean clearIds) {
        TestStepConfig newStepConfig = ix == -1 ? getConfig().addNewTestStep() : getConfig().insertNewTestStep(ix);
        newStepConfig.set(stepConfig);
        WsdlTestStep testStep = createTestStepFromConfig(newStepConfig);

        if (!ensureUniqueName(testStep)) {
            testStep.release();
            getConfig().getTestStepList().remove(newStepConfig);
            return null;
        }

        if (clearIds) {
            ModelSupport.createNewIds(testStep);
        }

        if (ix == -1) {
            testSteps.add(testStep);
        } else {
            testSteps.add(ix, testStep);
        }

        testStep.afterLoad();

        WsdlTestSuite oldTestSuite = oldTestCase == null ? null : oldTestCase.getTestSuite();
        testStep.afterCopy(oldTestSuite, oldTestCase);

        if (getTestSuite() != null) {
            (getTestSuite()).fireTestStepAdded(testStep, ix == -1 ? testSteps.size() - 1 : ix);
        }

        notifyPropertyChanged("testSteps", null, testStep);

        return testStep;
    }

    public WsdlTestStep[] insertTestSteps(WsdlTestCase oldTestCase, TestStepConfig[] stepConfig, int ix, boolean clearIds) {
        WsdlTestStep[] result = new WsdlTestStep[stepConfig.length];

        for (int c = 0; c < stepConfig.length; c++) {
            TestStepConfig newStepConfig = ix == -1 ? getConfig().addNewTestStep() : getConfig()
                    .insertNewTestStep(ix + c);
            newStepConfig.set(stepConfig[c]);
            WsdlTestStep testStep = createTestStepFromConfig(newStepConfig);

            if (!ensureUniqueName(testStep)) {
                return null;
            }

            if (clearIds) {
                ModelSupport.createNewIds(testStep);
            }

            if (ix == -1) {
                testSteps.add(testStep);
            } else {
                testSteps.add(ix + c, testStep);
            }

            result[c] = testStep;
        }

        for (int c = 0; c < result.length; c++) {
            result[c].afterLoad();
            result[c].afterCopy(oldTestCase.getTestSuite(), oldTestCase);
            if (getTestSuite() != null) {
                (getTestSuite()).fireTestStepAdded(result[c], getIndexOfTestStep(result[c]));
            }

            notifyPropertyChanged("testSteps", null, result[c]);
        }

        return result;
    }

    public void removeTestStep(WsdlTestStep testStep) {
        int ix = testSteps.indexOf(testStep);
        if (ix == -1) {
            logger.error("TestStep [" + testStep.getName() + "] passed to removeTestStep in testCase [" + getName()
                    + "] not found");
            return;
        }

        testSteps.remove(ix);
        for (SecurityTest securityTest : getSecurityTestList()) {
            List<SecurityScan> testStepChecks = securityTest.getTestStepSecurityScans(testStep.getId());
            for (Iterator<SecurityScan> iterator = testStepChecks.iterator(); iterator.hasNext(); ) {
                SecurityScan chk = iterator.next();
                securityTest.removeSecurityScanWhenRemoveTestStep(testStep, chk);
                iterator.remove();
            }

        }

        try {
            (getTestSuite()).fireTestStepRemoved(testStep, ix);
        } finally {
            notifyPropertyChanged("testSteps", testStep, null);

            testStep.release();

            for (int c = 0; c < getConfig().sizeOfTestStepArray(); c++) {
                if (testStep.getConfig() == getConfig().getTestStepArray(c)) {
                    getConfig().removeTestStep(c);
                    break;
                }
            }
        }
    }

    @Override
    public WsdlTestCaseRunner run(StringToObjectMap properties, boolean async) {
        WsdlTestCaseRunner runner = new WsdlTestCaseRunner(this, properties);
        runner.start(async);

        return runner;
    }

    @Override
    public void addTestRunListener(TestRunListener listener) {
        if (listener == null) {
            throw new RuntimeException("listener must not be null");
        }

        testRunListeners.add(listener);
    }

    @Override
    public void removeTestRunListener(TestRunListener listener) {
        testRunListeners.remove(listener);
    }

    public TestRunListener[] getTestRunListeners() {
        return testRunListeners.toArray(new TestRunListener[testRunListeners.size()]);
    }

    public Map<String, TestStep> getTestSteps() {
        Map<String, TestStep> result = new HashMap<String, TestStep>();
        for (TestStep testStep : testSteps) {
            result.put(testStep.getName(), testStep);
        }

        return result;
    }

    public Map<String, TestStep> getTestStepsOrdered() {
        Map<String, TestStep> result = new TreeMap<String, TestStep>();
        for (TestStep testStep : testSteps) {
            result.put(testStep.getName(), testStep);
        }

        return result;
    }

    public Map<String, LoadTest> getLoadTests() {
        Map<String, LoadTest> result = new HashMap<String, LoadTest>();
        for (LoadTest loadTest : loadTests) {
            result.put(loadTest.getName(), loadTest);
        }

        return result;
    }

    @Override
    public int getIndexOfTestStep(TestStep step) {
        return testSteps.indexOf(step);
    }

    /**
     * Moves a step by the specified offset, a bit awkward since xmlbeans doesn't
     * support reordering of arrays, we need to create copies of the contained
     * XmlObjects
     *
     * @param ix
     * @param offset
     */

    @Override
    public void moveTestStep(int ix, int offset) {
        if (offset == 0) {
            return;
        }
        WsdlTestStep step = testSteps.get(ix);

        if (ix + offset >= testSteps.size()) {
            offset = testSteps.size() - ix - 1;
        }

        testSteps.remove(ix);
        testSteps.add(ix + offset, step);

        TestStepConfig[] configs = new TestStepConfig[testSteps.size()];

        TestCaseConfig conf = getConfig();
        for (int c = 0; c < testSteps.size(); c++) {
            if (offset > 0) {
                if (c < ix) {
                    configs[c] = (TestStepConfig) conf.getTestStepArray(c).copy();
                } else if (c < (ix + offset)) {
                    configs[c] = (TestStepConfig) conf.getTestStepArray(c + 1).copy();
                } else if (c == ix + offset) {
                    configs[c] = (TestStepConfig) conf.getTestStepArray(ix).copy();
                } else {
                    configs[c] = (TestStepConfig) conf.getTestStepArray(c).copy();
                }
            } else {
                if (c < ix + offset) {
                    configs[c] = (TestStepConfig) conf.getTestStepArray(c).copy();
                } else if (c == ix + offset) {
                    configs[c] = (TestStepConfig) conf.getTestStepArray(ix).copy();
                } else if (c <= ix) {
                    configs[c] = (TestStepConfig) conf.getTestStepArray(c - 1).copy();
                } else {
                    configs[c] = (TestStepConfig) conf.getTestStepArray(c).copy();
                }
            }
        }

        conf.setTestStepArray(configs);
        for (int c = 0; c < configs.length; c++) {
            (testSteps.get(c)).resetConfigOnMove(conf.getTestStepArray(c));
        }

        (getTestSuite()).fireTestStepMoved(step, ix, offset);
    }

    @Override
    public int getIndexOfLoadTest(LoadTest loadTest) {
        return loadTests.indexOf(loadTest);
    }

    @Override
    public int getTestStepIndexByName(String stepName) {
        for (int c = 0; c < testSteps.size(); c++) {
            if (testSteps.get(c).getName().equals(stepName)) {
                return c;
            }
        }

        return -1;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends TestStep> T findPreviousStepOfType(TestStep referenceStep, Class<T> stepClass) {
        int currentStepIndex = getIndexOfTestStep(referenceStep);
        int ix = currentStepIndex - 1;
        while (ix >= 0 && !stepClass.isAssignableFrom(getTestStepAt(ix).getClass())) {
            ix--;
        }

        return (T) (ix < 0 ? null : getTestStepAt(ix));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends TestStep> T findNextStepOfType(TestStep referenceStep, Class<T> stepClass) {
        int currentStepIndex = getIndexOfTestStep(referenceStep);
        int ix = currentStepIndex + 1;
        while (ix < getTestStepCount() && !stepClass.isAssignableFrom(getTestStepAt(ix).getClass())) {
            ix++;
        }

        return (T) (ix >= getTestStepCount() ? null : getTestStepAt(ix));
    }

    @Override
    public List<TestStep> getTestStepList() {
        List<TestStep> result = new ArrayList<TestStep>();
        for (TestStep step : testSteps) {
            result.add(step);
        }

        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends TestStep> List<T> getTestStepsOfType(Class<T> stepType) {
        List<T> result = new ArrayList<T>();
        for (TestStep step : testSteps) {
            if (step.getClass().isAssignableFrom(stepType)) {
                result.add((T) step);
            }
        }

        return result;
    }

    @Override
    public WsdlTestStep getTestStepByName(String stepName) {
        return (WsdlTestStep) getWsdlModelItemByName(testSteps, stepName);
    }

    @Override
    public TestStep getTestStepById(UUID testStepId) {
        return (WsdlTestStep) getWsdlModelItemById(testSteps, testStepId);
    }

    public WsdlLoadTest cloneLoadTest(WsdlLoadTest loadTest, String name) {
        loadTest.beforeSave();

        LoadTestConfig loadTestConfig = getConfig().addNewLoadTest();
        loadTestConfig.set(loadTest.getConfig().copy());

        WsdlLoadTest newLoadTest = buildLoadTest(loadTestConfig);
        newLoadTest.setName(name);
        ModelSupport.createNewIds(newLoadTest);
        newLoadTest.afterLoad();
        loadTests.add(newLoadTest);

        (getTestSuite()).fireLoadTestAdded(newLoadTest);

        return newLoadTest;
    }

    @Override
    public void release() {
        super.release();

        for (WsdlTestStep testStep : testSteps) {
            testStep.release();
        }

        for (WsdlLoadTest loadTest : loadTests) {
            loadTest.release();
        }

        for (SecurityTest securityTest : securityTests) {
            securityTest.release();
        }

        testRunListeners.clear();

        if (setupScriptEngine != null) {
            setupScriptEngine.release();
        }

        if (tearDownScriptEngine != null) {
            tearDownScriptEngine.release();
        }
    }

    public ActionList getCreateActions() {
        return createActions;
    }

    public void resetConfigOnMove(TestCaseConfig testCaseConfig) {
        setConfig(testCaseConfig);
        int mod = 0;

        List<TestStepConfig> configs = getConfig().getTestStepList();
        for (int c = 0; c < configs.size(); c++) {
            if (WsdlTestStepRegistry.getInstance().hasFactory(configs.get(c))) {
                (testSteps.get(c - mod)).resetConfigOnMove(configs.get(c));
            } else {
                mod++;
            }
        }

        List<LoadTestConfig> loadTestConfigs = getConfig().getLoadTestList();
        for (int c = 0; c < loadTestConfigs.size(); c++) {
            loadTests.get(c).resetConfigOnMove(loadTestConfigs.get(c));
        }

        List<SecurityTestConfig> securityTestConfigs = getConfig().getSecurityTestList();
        for (int c = 0; c < securityTestConfigs.size(); c++) {
            securityTests.get(c).resetConfigOnMove(securityTestConfigs.get(c));
        }

        setPropertiesConfig(testCaseConfig.getProperties());
    }

    @Override
    public List<LoadTest> getLoadTestList() {
        List<LoadTest> result = new ArrayList<LoadTest>();
        for (LoadTest loadTest : loadTests) {
            result.add(loadTest);
        }

        return result;
    }

    public Object runSetupScript(TestCaseRunContext runContext, TestCaseRunner runner) throws Exception {
        String script = getSetupScript();
        if (StringUtils.isNullOrEmpty(script)) {
            return null;
        }

        if (setupScriptEngine == null) {
            setupScriptEngine = SoapUIScriptEngineRegistry.create(this);
            setupScriptEngine.setScript(script);
        }

        setupScriptEngine.setVariable("testCase", this);
        setupScriptEngine.setVariable("context", runContext);
        setupScriptEngine.setVariable("testRunner", runner);
        setupScriptEngine.setVariable("log", SoapUI.ensureGroovyLog());
        return setupScriptEngine.run();
    }

    public Object runTearDownScript(TestCaseRunContext runContext, TestCaseRunner runner) throws Exception {
        String script = getTearDownScript();
        if (StringUtils.isNullOrEmpty(script)) {
            return null;
        }

        if (tearDownScriptEngine == null) {
            tearDownScriptEngine = SoapUIScriptEngineRegistry.create(this);
            tearDownScriptEngine.setScript(script);
        }

        tearDownScriptEngine.setVariable("context", runContext);
        tearDownScriptEngine.setVariable("testCase", this);
        tearDownScriptEngine.setVariable("testRunner", runner);
        tearDownScriptEngine.setVariable("log", SoapUI.ensureGroovyLog());
        return tearDownScriptEngine.run();
    }

    @Override
    public List<? extends ModelItem> getChildren() {
        List<ModelItem> result = new ArrayList<ModelItem>();
        result.addAll(getTestStepList());
        result.addAll(getLoadTestList());
        result.addAll(getSecurityTestList());
        return result;
    }

    @Override
    public void setName(String name) {
        String oldLabel = getLabel();

        super.setName(name);

        String label = getLabel();
        if (oldLabel != null && !oldLabel.equals(label)) {
            notifyPropertyChanged(LABEL_PROPERTY, oldLabel, label);
        }
    }

    @Override
    public String getLabel() {
        String name = getName();
        if (isDisabled()) {
            return name + " (disabled)";
        } else {
            return name;
        }
    }

    @Override
    public boolean isDisabled() {
        return getConfig().getDisabled();
    }

    public void setDisabled(boolean disabled) {
        String oldLabel = getLabel();

        boolean oldDisabled = isDisabled();
        if (oldDisabled == disabled) {
            return;
        }

        if (disabled) {
            getConfig().setDisabled(disabled);
        } else if (getConfig().isSetDisabled()) {
            getConfig().unsetDisabled();
        }

        notifyPropertyChanged(DISABLED_PROPERTY, oldDisabled, disabled);

        String label = getLabel();
        if (!oldLabel.equals(label)) {
            notifyPropertyChanged(LABEL_PROPERTY, oldLabel, label);
        }
    }

    public long getTimeout() {
        return getConfig().getTimeout();
    }

    public void setTimeout(long timeout) {
        long old = getTimeout();
        getConfig().setTimeout(timeout);
        notifyPropertyChanged(TIMEOUT_PROPERTY, old, timeout);
    }

    public void exportTestCase(File file) {
        try {
            this.getConfig().newCursor().save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void afterCopy(WsdlTestSuite oldTestSuite, WsdlTestCase oldTestCase) {
        for (WsdlTestStep testStep : testSteps) {
            testStep.afterCopy(oldTestSuite, oldTestCase);
        }
    }

    public void importSecurityTests(WsdlTestSuite oldTestSuite, WsdlTestCase oldTestCase) {
        for (SecurityTest secTest : oldTestCase.getSecurityTestList()) {
            SecurityTest newSecurityTest = addNewSecurityTest(secTest.getName());
            for (int i = 0; i < oldTestCase.getTestStepList().size(); i++)

            {
                TestStep oldStep = oldTestCase.getTestStepAt(i);
                TestStep newStep = getTestStepAt(i);
                for (SecurityScan secCheck : secTest.getTestStepSecurityScans(oldStep.getId())) {
                    newSecurityTest.importSecurityScan(newStep, secCheck, true);
                }
            }
        }
    }

    public void setWsrmEnabled(boolean enabled) {
        getConfig().setWsrmEnabled(enabled);
    }

    public void setWsrmAckTo(String ackTo) {
        getConfig().setWsrmAckTo(ackTo);
    }

    public void setWsrmExpires(Long expires) {
        getConfig().setWsrmExpires(expires);
    }

    public void setWsrmVersion(String version) {
        getConfig().setWsrmVersion(WsrmVersionTypeConfig.Enum.forString(version));
    }

    public boolean getWsrmEnabled() {
        return getConfig().getWsrmEnabled();
    }

    public String getWsrmAckTo() {
        return getConfig().getWsrmAckTo();
    }

    public long getWsrmExpires() {
        return getConfig().getWsrmExpires();
    }

    public String getWsrmVersion() {
        if (getConfig().getWsrmVersion() == null) {
            return WsrmVersionTypeConfig.X_1_0.toString();
        }
        return getConfig().getWsrmVersion().toString();
    }

    public String getWsrmVersionNamespace() {
        return WsrmUtils.getWsrmVersionNamespace(getConfig().getWsrmVersion());
    }

    public void setAmfAuthorisation(boolean enabled) {
        getConfig().setAmfAuthorisation(enabled);
    }

    public boolean getAmfAuthorisation() {
        return getConfig().getAmfAuthorisation();
    }

    public void setAmfLogin(String login) {
        getConfig().setAmfLogin(login);
    }

    public String getAmfLogin() {
        if (getConfig().getAmfLogin() == null) {
            return "";
        } else {
            return getConfig().getAmfLogin();
        }
    }

    public void setAmfPassword(String password) {
        getConfig().setAmfPassword(password);
    }

    public String getAmfPassword() {
        if (getConfig().getAmfPassword() == null) {
            return "";
        } else {
            return getConfig().getAmfPassword();
        }
    }

    public void setAmfEndpoint(String endpoint) {
        getConfig().setAmfEndpoint(endpoint);
    }

    public String getAmfEndpoint() {
        if (getConfig().getAmfEndpoint() == null) {
            return "";
        } else {
            return getConfig().getAmfEndpoint();
        }
    }

    @Override
    public int getSecurityTestCount() {
        return securityTests.size();
    }

    @Override
    public int getIndexOfSecurityTest(SecurityTest securityTest) {
        return securityTests.indexOf(securityTest);
    }

    @Override
    public SecurityTest getSecurityTestAt(int index) {
        return securityTests.get(index);
    }

    @Override
    public SecurityTest getSecurityTestByName(String securityTestName) {
        return (SecurityTest) getWsdlModelItemByName(securityTests, securityTestName);
    }

    @Override
    public List<SecurityTest> getSecurityTestList() {
        return securityTests;
    }

    public Map<String, SecurityTest> getSecurityTests() {
        Map<String, SecurityTest> result = new HashMap<String, SecurityTest>();
        for (SecurityTest securityTest : securityTests) {
            result.put(securityTest.getName(), securityTest);
        }

        return result;
    }

    public SecurityTest addNewSecurityTest(String name) {
        SecurityTest securityTest = buildSecurityTest(getConfig().addNewSecurityTest());
        securityTest.setName(name);
        securityTest.setFailOnError(true);
        securityTest.setSkipDataSourceLoops(false);
        securityTests.add(securityTest);

        (getTestSuite()).fireSecurityTestAdded(securityTest);

        return securityTest;
    }

    protected SecurityTest buildSecurityTest(SecurityTestConfig addNewSecurityTest) {
        return new SecurityTest(this, addNewSecurityTest);
    }

    public SecurityTest cloneSecurityTest(SecurityTest securityTest, String name) {
        SecurityTestConfig securityTestConfig = getConfig().addNewSecurityTest();
        securityTestConfig.set(securityTest.getConfig().copy());

        SecurityTest newSecurityTest = buildSecurityTest(securityTestConfig);
        newSecurityTest.setName(name);
        ModelSupport.createNewIds(newSecurityTest);
        newSecurityTest.afterLoad();
        securityTests.add(newSecurityTest);

        (getTestSuite()).fireSecurityTestAdded(newSecurityTest);

        return newSecurityTest;
    }

    public void removeSecurityTest(SecurityTest securityTest) {
        int ix = securityTests.indexOf(securityTest);

        securityTests.remove(ix);

        try {
            (getTestSuite()).fireSecurityTestRemoved(securityTest);
        } finally {
            securityTest.release();
            getConfig().removeSecurityTest(ix);
        }
    }

    public StringToObjectMap getRunFromHereContext() {
        return runFromHereContext;
    }

    public void setRunFromHereContext(StringToObjectMap runFromHereContext) {
        if (!isForLoadTest()) {
            this.runFromHereContext = new StringToObjectMap(runFromHereContext);
        }
    }
}
