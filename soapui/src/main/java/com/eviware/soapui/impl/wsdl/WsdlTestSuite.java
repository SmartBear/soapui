/*
 * Copyright 2004-2014 SmartBear Software
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

package com.eviware.soapui.impl.wsdl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.LoadTestConfig;
import com.eviware.soapui.config.SecurityTestConfig;
import com.eviware.soapui.config.TestCaseConfig;
import com.eviware.soapui.config.TestCaseDocumentConfig;
import com.eviware.soapui.config.TestStepSecurityTestConfig;
import com.eviware.soapui.config.TestSuiteConfig;
import com.eviware.soapui.config.TestSuiteRunTypesConfig;
import com.eviware.soapui.config.TestSuiteRunTypesConfig.Enum;
import com.eviware.soapui.impl.wsdl.loadtest.WsdlLoadTest;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestSuiteRunner;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.support.ModelSupport;
import com.eviware.soapui.model.testsuite.TestCase;
import com.eviware.soapui.model.testsuite.TestSuite;
import com.eviware.soapui.model.testsuite.TestSuiteListener;
import com.eviware.soapui.model.testsuite.TestSuiteRunContext;
import com.eviware.soapui.model.testsuite.TestSuiteRunListener;
import com.eviware.soapui.model.testsuite.TestSuiteRunner;
import com.eviware.soapui.security.SecurityTest;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.resolver.ResolveDialog;
import com.eviware.soapui.support.scripting.SoapUIScriptEngine;
import com.eviware.soapui.support.scripting.SoapUIScriptEngineRegistry;
import com.eviware.soapui.support.types.StringToObjectMap;

/**
 * TestSuite implementation for WSDL projects.
 *
 * @author Ole.Matzura
 */

public class WsdlTestSuite extends AbstractTestPropertyHolderWsdlModelItem<TestSuiteConfig> implements TestSuite {
    public final static String SETUP_SCRIPT_PROPERTY = WsdlTestSuite.class.getName() + "@setupScript";
    public final static String TEARDOWN_SCRIPT_PROPERTY = WsdlTestSuite.class.getName() + "@tearDownScript";
    public static final String ICON_NAME = "/testSuite.gif";

    private final WsdlProject project;
    private List<WsdlTestCase> testCases = new ArrayList<WsdlTestCase>();
    private Set<TestSuiteListener> testSuiteListeners = new HashSet<TestSuiteListener>();
    private Set<TestSuiteRunListener> testSuiteRunListeners = new HashSet<TestSuiteRunListener>();
    private SoapUIScriptEngine setupScriptEngine;
    private SoapUIScriptEngine tearDownScriptEngine;

    public WsdlTestSuite(WsdlProject project, TestSuiteConfig config) {
        super(config, project, ICON_NAME);
        this.project = project;

        if (!config.isSetProperties()) {
            config.addNewProperties();
        }

        setPropertiesConfig(config.getProperties());

        List<TestCaseConfig> testCaseConfigs = config.getTestCaseList();
        for (int i = 0; i < testCaseConfigs.size(); i++) {
            testCases.add(buildTestCase(testCaseConfigs.get(i), false));
        }

        if (!config.isSetRunType()) {
            config.setRunType(TestSuiteRunTypesConfig.SEQUENTIAL);
        }

        for (TestSuiteListener listener : SoapUI.getListenerRegistry().getListeners(TestSuiteListener.class)) {
            addTestSuiteListener(listener);
        }

        for (TestSuiteRunListener listener : SoapUI.getListenerRegistry().getListeners(TestSuiteRunListener.class)) {
            addTestSuiteRunListener(listener);
        }

    }

    public WsdlTestCase buildTestCase(TestCaseConfig testCaseConfig, boolean forLoadTest) {
        return new WsdlTestCase(this, testCaseConfig, forLoadTest);
    }

    public TestSuiteRunType getRunType() {
        Enum runType = getConfig().getRunType();

        if (runType.equals(TestSuiteRunTypesConfig.PARALLELL)) {
            return TestSuiteRunType.PARALLEL;
        } else {
            return TestSuiteRunType.SEQUENTIAL;
        }
    }

    public void setRunType(TestSuiteRunType runType) {
        TestSuiteRunType oldRunType = getRunType();

        if (runType == TestSuiteRunType.PARALLEL && oldRunType != TestSuiteRunType.PARALLEL) {
            getConfig().setRunType(TestSuiteRunTypesConfig.PARALLELL);
            notifyPropertyChanged(RUNTYPE_PROPERTY, oldRunType, runType);
        } else if (runType == TestSuiteRunType.SEQUENTIAL && oldRunType != TestSuiteRunType.SEQUENTIAL) {
            getConfig().setRunType(TestSuiteRunTypesConfig.SEQUENTIAL);
            notifyPropertyChanged(RUNTYPE_PROPERTY, oldRunType, runType);
        }
    }

    public WsdlProject getProject() {
        return project;
    }

    public int getTestCaseCount() {
        return testCases.size();
    }

    public WsdlTestCase getTestCaseAt(int index) {
        return testCases.get(index);
    }

    public WsdlTestCase getTestCaseByName(String testCaseName) {
        return (WsdlTestCase) getWsdlModelItemByName(testCases, testCaseName);
    }

    @Override
    public TestCase getTestCaseById(UUID testCaseId) {
        return (WsdlTestCase) getWsdlModelItemById(testCases, testCaseId);
    }

    public WsdlTestCase cloneTestCase(WsdlTestCase testCase, String name) {
        testCase.beforeSave();
        TestCaseConfig newTestCase = getConfig().addNewTestCase();
        newTestCase.set(testCase.getConfig());
        newTestCase.setName(name);
        WsdlTestCase newWsdlTestCase = buildTestCase(newTestCase, false);
        ModelSupport.createNewIds(newWsdlTestCase);
        newWsdlTestCase.afterLoad();

        testCases.add(newWsdlTestCase);
        fireTestCaseAdded(newWsdlTestCase);

        return newWsdlTestCase;
    }

    public WsdlTestCase addNewTestCase(String name) {
        WsdlTestCase testCase = buildTestCase(getConfig().addNewTestCase(), false);
        testCase.setName(name);
        testCase.setFailOnError(true);
        testCase.setSearchProperties(true);
        testCases.add(testCase);
        fireTestCaseAdded(testCase);

        return testCase;
    }

    public WsdlTestCase importTestCase(WsdlTestCase testCase, String name, int index, boolean includeLoadTests,
                                       boolean includeSecurityTests, boolean createCopy) {
        testCase.beforeSave();

        if (index >= testCases.size()) {
            index = -1;
        }

        TestCaseConfig testCaseConfig = index == -1 ? (TestCaseConfig) getConfig().addNewTestCase().set(
                testCase.getConfig().copy()) : (TestCaseConfig) getConfig().insertNewTestCase(index).set(
                testCase.getConfig().copy());
        testCaseConfig.setName(name);

        if (!includeLoadTests) {
            testCaseConfig.setLoadTestArray(new LoadTestConfig[0]);
        }

        if (createCopy) {
            testCaseConfig.setSecurityTestArray(new SecurityTestConfig[0]);
        }

        WsdlTestCase oldTestCase = testCase;
        testCase = buildTestCase(testCaseConfig, false);

        if (createCopy) {
            ModelSupport.createNewIds(testCase);
        }

        if (index == -1) {
            testCases.add(testCase);
        } else {
            testCases.add(index, testCase);
        }

        testCase.afterLoad();

        if (createCopy) {
            testCase.afterCopy(null, oldTestCase);

            if (includeSecurityTests) {
                testCase.importSecurityTests(null, oldTestCase);
            }
        }

        fireTestCaseAdded(testCase);
        resolveImportedTestCase(testCase);

        return testCase;
    }

    public void removeTestCase(WsdlTestCase testCase) {
        int ix = testCases.indexOf(testCase);

        testCases.remove(ix);
        try {
            fireTestCaseRemoved(testCase);
        } finally {
            testCase.release();
            getConfig().removeTestCase(ix);
        }
    }

    public void fireTestCaseAdded(WsdlTestCase testCase) {
        TestSuiteListener[] a = testSuiteListeners.toArray(new TestSuiteListener[testSuiteListeners.size()]);

        for (int c = 0; c < a.length; c++) {
            a[c].testCaseAdded(testCase);
        }
    }

    public void fireTestCaseRemoved(WsdlTestCase testCase) {
        TestSuiteListener[] a = testSuiteListeners.toArray(new TestSuiteListener[testSuiteListeners.size()]);

        for (int c = 0; c < a.length; c++) {
            a[c].testCaseRemoved(testCase);
        }
    }

    private void fireTestCaseMoved(WsdlTestCase testCase, int ix, int offset) {
        TestSuiteListener[] a = testSuiteListeners.toArray(new TestSuiteListener[testSuiteListeners.size()]);

        for (int c = 0; c < a.length; c++) {
            a[c].testCaseMoved(testCase, ix, offset);
        }
    }

    public void fireTestStepAdded(WsdlTestStep testStep, int index) {
        TestSuiteListener[] a = testSuiteListeners.toArray(new TestSuiteListener[testSuiteListeners.size()]);

        for (int c = 0; c < a.length; c++) {
            a[c].testStepAdded(testStep, index);
        }
    }

    public void fireTestStepRemoved(WsdlTestStep testStep, int ix) {
        TestSuiteListener[] a = testSuiteListeners.toArray(new TestSuiteListener[testSuiteListeners.size()]);

        for (int c = 0; c < a.length; c++) {
            a[c].testStepRemoved(testStep, ix);
        }
    }

    public void fireTestStepMoved(WsdlTestStep testStep, int ix, int offset) {
        TestSuiteListener[] a = testSuiteListeners.toArray(new TestSuiteListener[testSuiteListeners.size()]);

        for (int c = 0; c < a.length; c++) {
            a[c].testStepMoved(testStep, ix, offset);
        }
    }

    public void fireLoadTestAdded(WsdlLoadTest loadTest) {
        TestSuiteListener[] a = testSuiteListeners.toArray(new TestSuiteListener[testSuiteListeners.size()]);

        for (int c = 0; c < a.length; c++) {
            a[c].loadTestAdded(loadTest);
        }
    }

    public void fireLoadTestRemoved(WsdlLoadTest loadTest) {
        TestSuiteListener[] a = testSuiteListeners.toArray(new TestSuiteListener[testSuiteListeners.size()]);

        for (int c = 0; c < a.length; c++) {
            a[c].loadTestRemoved(loadTest);
        }
    }

    public void addTestSuiteListener(TestSuiteListener listener) {
        testSuiteListeners.add(listener);
    }

    public void removeTestSuiteListener(TestSuiteListener listener) {
        testSuiteListeners.remove(listener);
    }

    public void addTestSuiteRunListener(TestSuiteRunListener listener) {
        testSuiteRunListeners.add(listener);
    }

    public void removeTestSuiteRunListener(TestSuiteRunListener listener) {
        testSuiteRunListeners.remove(listener);
    }

    public int getTestCaseIndex(TestCase testCase) {
        return testCases.indexOf(testCase);
    }

    @Override
    public void release() {
        super.release();

        for (WsdlTestCase testCase : testCases) {
            testCase.release();
        }

        testSuiteListeners.clear();

        if (setupScriptEngine != null) {
            setupScriptEngine.release();
        }

        if (tearDownScriptEngine != null) {
            tearDownScriptEngine.release();
        }
    }

    public List<TestCase> getTestCaseList() {
        List<TestCase> result = new ArrayList<TestCase>();
        for (WsdlTestCase testCase : testCases) {
            result.add(testCase);
        }

        return result;
    }

    public Map<String, TestCase> getTestCases() {
        Map<String, TestCase> result = new HashMap<String, TestCase>();
        for (TestCase testCase : testCases) {
            result.put(testCase.getName(), testCase);
        }

        return result;
    }

    /**
     * Moves a testcase by the specified offset, a bit awkward since xmlbeans
     * doesn't support reordering of arrays, we need to create copies of the
     * contained XmlObjects
     *
     * @param ix
     * @param offset
     */

    public WsdlTestCase moveTestCase(int ix, int offset) {
        WsdlTestCase testCase = testCases.get(ix);

        if (offset == 0) {
            return testCase;
        }

        testCases.remove(ix);
        testCases.add(ix + offset, testCase);

        TestCaseConfig[] configs = new TestCaseConfig[testCases.size()];

        for (int c = 0; c < testCases.size(); c++) {
            if (offset > 0) {
                if (c < ix) {
                    configs[c] = (TestCaseConfig) getConfig().getTestCaseArray(c).copy();
                } else if (c < (ix + offset)) {
                    configs[c] = (TestCaseConfig) getConfig().getTestCaseArray(c + 1).copy();
                } else if (c == ix + offset) {
                    configs[c] = (TestCaseConfig) getConfig().getTestCaseArray(ix).copy();
                } else {
                    configs[c] = (TestCaseConfig) getConfig().getTestCaseArray(c).copy();
                }
            } else {
                if (c < ix + offset) {
                    configs[c] = (TestCaseConfig) getConfig().getTestCaseArray(c).copy();
                } else if (c == ix + offset) {
                    configs[c] = (TestCaseConfig) getConfig().getTestCaseArray(ix).copy();
                } else if (c <= ix) {
                    configs[c] = (TestCaseConfig) getConfig().getTestCaseArray(c - 1).copy();
                } else {
                    configs[c] = (TestCaseConfig) getConfig().getTestCaseArray(c).copy();
                }
            }
        }

        getConfig().setTestCaseArray(configs);
        for (int c = 0; c < configs.length; c++) {
            testCases.get(c).resetConfigOnMove(getConfig().getTestCaseArray(c));
        }

        fireTestCaseMoved(testCase, ix, offset);
        return testCase;
    }

    public int getIndexOfTestCase(TestCase testCase) {
        return testCases.indexOf(testCase);
    }

    public List<? extends ModelItem> getChildren() {
        return getTestCaseList();
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

    public Object runSetupScript(TestSuiteRunContext context, TestSuiteRunner runner) throws Exception {
        String script = getSetupScript();
        if (StringUtils.isNullOrEmpty(script)) {
            return null;
        }

        if (setupScriptEngine == null) {
            setupScriptEngine = SoapUIScriptEngineRegistry.create(this);
            setupScriptEngine.setScript(script);
        }

        setupScriptEngine.setVariable("runner", runner);
        setupScriptEngine.setVariable("context", context);
        setupScriptEngine.setVariable("testSuite", this);
        setupScriptEngine.setVariable("log", SoapUI.ensureGroovyLog());
        return setupScriptEngine.run();
    }

    public Object runTearDownScript(TestSuiteRunContext context, TestSuiteRunner runner) throws Exception {
        String script = getTearDownScript();
        if (StringUtils.isNullOrEmpty(script)) {
            return null;
        }

        if (tearDownScriptEngine == null) {
            tearDownScriptEngine = SoapUIScriptEngineRegistry.create(this);
            tearDownScriptEngine.setScript(script);
        }

        tearDownScriptEngine.setVariable("runner", runner);
        tearDownScriptEngine.setVariable("context", context);
        tearDownScriptEngine.setVariable("testSuite", this);
        tearDownScriptEngine.setVariable("log", SoapUI.ensureGroovyLog());
        return tearDownScriptEngine.run();
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

    public String getLabel() {
        String name = getName();
        if (isDisabled()) {
            return name + " (disabled)";
        } else {
            return name;
        }
    }

    public boolean isFailOnErrors() {
        return getConfig().getFailOnErrors();
    }

    public void setFailOnErrors(boolean failOnErrors) {
        getConfig().setFailOnErrors(failOnErrors);
    }

    public boolean isAbortOnError() {
        return getConfig().getAbortOnError();
    }

    public void setAbortOnError(boolean abortOnError) {
        getConfig().setAbortOnError(abortOnError);
    }

    public long getTimeout() {
        return getConfig().getTimeout();
    }

    public void setTimeout(long timeout) {
        getConfig().setTimeout(timeout);
    }

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

    public void replace(WsdlTestCase testCase, TestCaseConfig newTestCase) {

        int ix = testCases.indexOf(testCase);

        testCases.remove(ix);
        try {
            fireTestCaseRemoved(testCase);
        } finally {
            testCase.release();
            getConfig().removeTestCase(ix);
        }

        TestCaseConfig newConfig = (TestCaseConfig) getConfig().insertNewTestCase(ix).set(newTestCase)
                .changeType(TestCaseConfig.type);
        testCase = buildTestCase(newConfig, false);
        testCases.add(ix, testCase);
        testCase.afterLoad();
        fireTestCaseAdded(testCase);

        resolveImportedTestCase(testCase);
    }

    public void importTestCase(File file) {
        TestCaseConfig importTestCaseConfig = null;

        if (!file.exists()) {
            UISupport.showErrorMessage("Error loading test case ");
            return;
        }

        try {
            importTestCaseConfig = TestCaseDocumentConfig.Factory.parse(file).getTestCase();
        } catch (Exception e) {
            SoapUI.logError(e);
        }

        if (importTestCaseConfig != null) {
            TestCaseConfig newConfig = (TestCaseConfig) getConfig().addNewTestCase().set(importTestCaseConfig)
                    .changeType(TestCaseConfig.type);
            WsdlTestCase newTestCase = buildTestCase(newConfig, false);
            ModelSupport.createNewIds(newTestCase);

			/*
             * security test keeps reference to test step by id, which gets changed
			 * during importing, so old values needs to be rewritten to new ones.
			 * 
			 * Create tarnsition table ( old id , new id ) and use it to replace
			 * all old ids in new imported test case.
			 */
            LinkedHashMap<String, String> oldNewIds = new LinkedHashMap<String, String>();
            for (int cnt = 0; cnt < importTestCaseConfig.getTestStepList().size(); cnt++) {
                oldNewIds.put(importTestCaseConfig.getTestStepList().get(cnt).getId(), newTestCase.getTestStepList()
                        .get(cnt).getId());
            }

            for (SecurityTest scan : newTestCase.getSecurityTests().values()) {
                for (TestStepSecurityTestConfig secStepConfig : scan.getConfig().getTestStepSecurityTestList()) {
                    if (oldNewIds.containsKey(secStepConfig.getTestStepId())) {
                        secStepConfig.setTestStepId(oldNewIds.get(secStepConfig.getTestStepId()));
                    }
                }
            }

            newTestCase.afterLoad();

            testCases.add(newTestCase);
            fireTestCaseAdded(newTestCase);

            resolveImportedTestCase(newTestCase);
        } else {
            UISupport.showErrorMessage("Not valid test case xml");
        }
    }

    private void resolveImportedTestCase(WsdlTestCase newTestCase) {
        ResolveDialog resolver = new ResolveDialog("Validate TestCase", "Checks TestCase for inconsistencies", null);
        resolver.setShowOkMessage(false);
        resolver.resolve(newTestCase);
    }

    public void export(File file) {
        try {
            this.getConfig().newCursor().save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void afterCopy(WsdlTestSuite oldTestSuite) {

        for (int i = 0; i < testCases.size(); i++) {
            WsdlTestCase testCase = getTestCaseAt(i);
            WsdlTestCase oldTestCase = oldTestSuite.getTestCaseAt(i);

            testCase.afterCopy(oldTestSuite, oldTestCase);
            testCase.importSecurityTests(oldTestSuite, oldTestCase);
        }

    }

    public WsdlTestSuiteRunner run(StringToObjectMap context, boolean async) {
        WsdlTestSuiteRunner testSuiteRunner = new WsdlTestSuiteRunner(this, context);
        testSuiteRunner.start(async);
        return testSuiteRunner;
    }

    public TestSuiteRunListener[] getTestSuiteRunListeners() {
        return testSuiteRunListeners.toArray(new TestSuiteRunListener[testSuiteRunListeners.size()]);
    }

    public void resetConfigOnMove(TestSuiteConfig testSuiteConfig) {
        setConfig(testSuiteConfig);

        List<TestCaseConfig> configs = getConfig().getTestCaseList();
        for (int c = 0; c < configs.size(); c++) {
            testCases.get(c).resetConfigOnMove(configs.get(c));
        }

        setPropertiesConfig(testSuiteConfig.getProperties());
    }

    public void fireSecurityTestAdded(SecurityTest securityTest) {
        TestSuiteListener[] a = testSuiteListeners.toArray(new TestSuiteListener[testSuiteListeners.size()]);

        for (int c = 0; c < a.length; c++) {
            a[c].securityTestAdded(securityTest);
        }

    }

    public void fireSecurityTestRemoved(SecurityTest securityTest) {
        TestSuiteListener[] a = testSuiteListeners.toArray(new TestSuiteListener[testSuiteListeners.size()]);

        for (int c = 0; c < a.length; c++) {
            a[c].securityTestRemoved(securityTest);
        }
    }
}
