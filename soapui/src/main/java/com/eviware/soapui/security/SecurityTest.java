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

package com.eviware.soapui.security;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.SecurityScanConfig;
import com.eviware.soapui.config.SecurityTestConfig;
import com.eviware.soapui.config.TestAssertionConfig;
import com.eviware.soapui.config.TestStepSecurityTestConfig;
import com.eviware.soapui.impl.wsdl.AbstractTestPropertyHolderWsdlModelItem;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.TestModelItem;
import com.eviware.soapui.model.security.SecurityScan;
import com.eviware.soapui.model.testsuite.TestRunnable;
import com.eviware.soapui.model.testsuite.TestRunner.Status;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.TestStepResult;
import com.eviware.soapui.model.testsuite.TestStepResult.TestStepStatus;
import com.eviware.soapui.security.registry.SecurityScanFactory;
import com.eviware.soapui.security.result.SecurityTestStepResult;
import com.eviware.soapui.security.support.SecurityTestRunListener;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.scripting.SoapUIScriptEngine;
import com.eviware.soapui.support.scripting.SoapUIScriptEngineRegistry;
import com.eviware.soapui.support.types.StringToObjectMap;
import org.apache.xmlbeans.XmlObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class is used to connect a TestCase with a set of security scans
 *
 * @author SoapUI team
 */
public class SecurityTest extends AbstractTestPropertyHolderWsdlModelItem<SecurityTestConfig> implements TestModelItem,
        TestRunnable {
    public final static String STARTUP_SCRIPT_PROPERTY = SecurityTest.class.getName() + "@startupScript";
    public final static String TEARDOWN_SCRIPT_PROPERTY = SecurityTest.class.getName() + "@tearDownScript";
    public final static String FAIL_ON_SCANS_ERRORS_PROPERTY = SecurityTest.class.getName() + "@failOnScansErrors";
    public final static String FAIL_ON_ERROR_PROPERTY = SecurityTest.class.getName() + "@failOnError";
    public final static String SKIP_DATASOURCE_LOOP_PROPERTY = SecurityTest.class.getName() + "@skipDataSourceLoop";
    public static final String ICON_NAME = "/security_test.gif";
    private WsdlTestCase testCase;
    private Set<SecurityTestRunListener> securityTestRunListeners = Collections
            .synchronizedSet(new HashSet<SecurityTestRunListener>());
    private Map<TestStep, Set<SecurityTestRunListener>> securityTestStepRunListeners = new HashMap<TestStep, Set<SecurityTestRunListener>>();
    private Map<TestStep, SecurityTestStepResult> securityTestStepResultMap;

    private HashMap<String, List<SecurityScan>> securityScansMap = new HashMap<String, List<SecurityScan>>();
    private ArrayList<SecurityTestListener> securityTestListeners = new ArrayList<SecurityTestListener>();

    private SecurityTestRunnerImpl runner;
    private SoapUIScriptEngine startupScriptEngine;
    private SoapUIScriptEngine tearDownScriptEngine;

    public SecurityTest(WsdlTestCase testCase, SecurityTestConfig config) {
        super(config, testCase, ICON_NAME);
        this.testCase = testCase;
        if (!getConfig().isSetProperties()) {
            getConfig().addNewProperties();
        }

        setPropertiesConfig(getConfig().getProperties());

        securityTestStepResultMap = new LinkedHashMap<TestStep, SecurityTestStepResult>();

        for (SecurityTestRunListener listener : SoapUI.getListenerRegistry().getListeners(SecurityTestRunListener.class)) {
            addSecurityTestRunListener(listener);
        }
    }

    public void release() {
        super.release();

        securityTestRunListeners.clear();
        if (securityTestStepResultMap != null) {
            securityTestStepResultMap.clear();
        }
        securityScansMap.clear();
        securityTestListeners.clear();
    }

    /**
     * Adds new securityScan for the specific TestStep by Security Scan Type
     *
     * @param testStep
     * @param securityScanName
     * @return SecurityScan
     */
    public SecurityScan addNewSecurityScan(TestStep testStep, String securityScanName) {
        SecurityScanFactory factory = SoapUI.getSoapUICore().getSecurityScanRegistry()
                .getFactoryByName(securityScanName);
        SecurityScanConfig newScanConfig = factory.createNewSecurityScan(securityScanName);
        return addSecurityScan(testStep, factory, newScanConfig);
    }

    /**
     * Adds a securityScan for the specific TestStep
     *
     * @param testStep
     * @param factory
     * @param newScanConfig
     * @return
     */
    public SecurityScan addSecurityScan(TestStep testStep, SecurityScanFactory factory, SecurityScanConfig newScanConfig) {
        SecurityScan newSecScan = null;

        boolean hasScans = false;
        List<TestStepSecurityTestConfig> testStepSecurityTestList = getConfig().getTestStepSecurityTestList();
        if (!testStepSecurityTestList.isEmpty()) {
            for (int i = 0; i < testStepSecurityTestList.size(); i++) {
                TestStepSecurityTestConfig testStepSecurityTest = testStepSecurityTestList.get(i);
                if (testStepSecurityTest.getTestStepId().equals(testStep.getId())) {
                    newSecScan = buildSecurityScan(factory, newScanConfig, testStepSecurityTest, testStep);
                    hasScans = true;
                    break;
                }
            }
        }
        if (!hasScans) {
            TestStepSecurityTestConfig testStepSecurityTest = getConfig().addNewTestStepSecurityTest();
            testStepSecurityTest.setTestStepId(testStep.getId());

            newSecScan = buildSecurityScan(factory, newScanConfig, testStepSecurityTest, testStep);
        }

        addSecurityScanToMapByTestStepId(testStep.getId(), newSecScan);
        return newSecScan;

    }

    /**
     * Adds new security scan to TestStep SecurityTest config
     *
     * @param factory
     * @param newSecScanConfig
     * @param testStepSecurityTestConfig
     * @param testStep
     * @return
     */
    private SecurityScan buildSecurityScan(SecurityScanFactory factory, SecurityScanConfig newSecScanConfig,
                                           TestStepSecurityTestConfig testStepSecurityTestConfig, TestStep testStep) {
        SecurityScanConfig newSecurityScan = testStepSecurityTestConfig.addNewTestStepSecurityScan();
        newSecurityScan.setType(newSecScanConfig.getType());
        newSecurityScan.setName(newSecScanConfig.getName());
        newSecurityScan.setConfig(newSecScanConfig.getConfig());
        newSecurityScan.setAssertionArray(newSecScanConfig.getAssertionList().toArray(new TestAssertionConfig[0]));
        newSecurityScan.setTestStep(newSecScanConfig.getTestStep());
        newSecurityScan.setCheckedParameters(newSecScanConfig.getCheckedParameters());
        newSecurityScan.setExecutionStrategy(newSecScanConfig.getExecutionStrategy());

        return factory.buildSecurityScan(testStep, newSecurityScan, this);
    }

    private void addSecurityScanToMapByTestStepId(String testStepId, SecurityScan newSecScan) {
        if (securityScansMap.containsKey(testStepId)) {
            if (!securityScansMap.get(testStepId).contains(newSecScan)) {
                securityScansMap.get(testStepId).add(newSecScan);
            }
        } else {
            List<SecurityScan> list = new ArrayList<SecurityScan>();
            list.add(newSecScan);
            securityScansMap.put(testStepId, list);
        }
        fireSecurityScanAdded(newSecScan);
    }

    private void fireSecurityScanAdded(SecurityScan newSecScan) {
        for (SecurityTestListener listener : securityTestListeners) {
            listener.securityScanAdded(newSecScan);
        }
    }

    private void fireSecurityScanRemoved(SecurityScan newSecScan) {
        for (SecurityTestListener listener : securityTestListeners) {
            listener.securityScanRemoved(newSecScan);
        }
    }

    /**
     * Remove securityScan for the specific TestStep
     *
     * @param testStep
     * @param securityScan
     */
    public void removeSecurityScan(TestStep testStep, SecurityScan securityScan) {
        if (isRunning()) {
            return;
        }

        List<TestStepSecurityTestConfig> testStepSecurityTestList = getConfig().getTestStepSecurityTestList();
        if (!testStepSecurityTestList.isEmpty()) {
            for (int i = 0; i < testStepSecurityTestList.size(); i++) {
                TestStepSecurityTestConfig testStepSecurityTest = testStepSecurityTestList.get(i);
                if (testStepSecurityTest.getTestStepId().equals(testStep.getId())) {
                    List<SecurityScanConfig> securityScanList = testStepSecurityTest.getTestStepSecurityScanList();
                    Iterator<SecurityScanConfig> secListIterator = securityScanList.iterator();
                    while (secListIterator.hasNext()) {
                        SecurityScanConfig current = secListIterator.next();
                        if (current.getName().equals(securityScan.getName())) {
                            secListIterator.remove();
                            break;
                        }
                    }
                    if (securityScanList.isEmpty()) {
                        getConfig().removeTestStepSecurityTest(i);
                    }
                }
            }
        }
        removeSecurityScanFromMapByTestStepId(testStep.getId(), securityScan);
    }

    public void removeSecurityScanWhenRemoveTestStep(TestStep testStep, SecurityScan securityScan) {
        List<TestStepSecurityTestConfig> testStepSecurityTestList = getConfig().getTestStepSecurityTestList();
        if (!testStepSecurityTestList.isEmpty()) {
            for (int i = 0; i < testStepSecurityTestList.size(); i++) {
                TestStepSecurityTestConfig testStepSecurityTest = testStepSecurityTestList.get(i);
                if (testStepSecurityTest.getTestStepId().equals(testStep.getId())) {
                    List<SecurityScanConfig> securityScanList = testStepSecurityTest.getTestStepSecurityScanList();
                    Iterator<SecurityScanConfig> secListIterator = securityScanList.iterator();
                    while (secListIterator.hasNext()) {
                        SecurityScanConfig current = secListIterator.next();
                        if (current.getName().equals(securityScan.getName())) {
                            secListIterator.remove();
                            break;
                        }
                    }
                    if (securityScanList.isEmpty()) {
                        getConfig().removeTestStepSecurityTest(i);
                    }
                }
            }
        }
    }

    private void removeSecurityScanFromMapByTestStepId(String testStepId, SecurityScan securityScan) {
        if (securityScansMap.containsKey(testStepId)) {
            if (securityScansMap.get(testStepId).contains(securityScan)) {
                securityScansMap.get(testStepId).remove(securityScan);
                fireSecurityScanRemoved(securityScan);
                securityScan.release();
            }
        }
    }

    /**
     * Returns a map of testids to security scans
     *
     * @return A map of TestStepIds to their relevant security scans
     */
    public HashMap<String, List<SecurityScan>> getSecurityScansMap() {

        if (!securityScansMap.isEmpty()) {
            return securityScansMap;
        }

        return createSecurityScansMap();
    }

    public int getSecurityScanCount() {
        Iterator<List<SecurityScan>> scannedSteps = getSecurityScansMap().values().iterator();
        int count = 0;
        while (scannedSteps.hasNext()) {
            List<SecurityScan> scanList = scannedSteps.next();
            count += scanList.size();
        }
        return count;
    }

    public int getStepSecurityApplicableScansCount(TestStepResult tsr) {
        Iterator<List<SecurityScan>> scannedSteps = getSecurityScansMap().values().iterator();
        int count = 0;
        while (scannedSteps.hasNext()) {
            List<SecurityScan> scanList = scannedSteps.next();
            for (SecurityScan securityScan : scanList) {
                if (securityScan.getTestStep().getId().equals(tsr.getTestStep().getId())
                        && (tsr.getStatus() != TestStepStatus.FAILED || securityScan.isApplyForFailedStep())) {
                    count++;
                }
            }
        }
        return count;
    }

    private HashMap<String, List<SecurityScan>> createSecurityScansMap() {
        if (getConfig() != null) {
            if (!getConfig().getTestStepSecurityTestList().isEmpty()) {
                for (TestStepSecurityTestConfig testStepSecurityTestListConfig : getConfig().getTestStepSecurityTestList()) {
                    List<SecurityScan> scanList = new ArrayList<SecurityScan>();
                    if (testStepSecurityTestListConfig != null) {
                        if (!testStepSecurityTestListConfig.getTestStepSecurityScanList().isEmpty()) {
                            for (SecurityScanConfig secScanConfig : testStepSecurityTestListConfig
                                    .getTestStepSecurityScanList()) {
                                TestStep testStep = null;
                                for (TestStep ts : testCase.getTestSteps().values()) {
                                    if (testStepSecurityTestListConfig.getTestStepId().equals(ts.getId())) {
                                        testStep = ts;
                                        SecurityScan securityScan = SoapUI.getSoapUICore().getSecurityScanRegistry()
                                                .getFactory(secScanConfig.getType())
                                                .buildSecurityScan(testStep, secScanConfig, this);
                                        scanList.add(securityScan);
                                    }
                                }
                            }
                        }
                    }
                    if (!scanList.isEmpty()) {
                        securityScansMap.put(testStepSecurityTestListConfig.getTestStepId(), scanList);
                    }
                }
            }
        }
        return securityScansMap;
    }

    public Map<TestStep, SecurityTestStepResult> getSecurityTestStepResultMap() {
        return securityTestStepResultMap;
    }

    public void clearSecurityTestStepResultMap() {
        securityTestStepResultMap.clear();
    }

    /**
     * Puts result of a SecurityTest on a TestStep level to a map, if map
     * previously contained value for specified TestStep it is being replaced
     * with the new result value
     *
     * @param testStep
     * @param securityTestStepResult
     */
    public void putSecurityTestStepResult(TestStep testStep, SecurityTestStepResult securityTestStepResult) {
        securityTestStepResultMap.put(testStep, securityTestStepResult);
    }

    /**
     * @return the current testcase
     */
    public WsdlTestCase getTestCase() {
        return testCase;
    }

    public SecurityTestRunner run(StringToObjectMap context, boolean async) {
        if (runner != null && runner.getStatus() == Status.RUNNING) {
            return null;
        }

        runner = new SecurityTestRunnerImpl(this, context);
        runner.start(async);
        return runner;
    }

    /**
     * Sets the script to be used on startup
     *
     * @param script
     */
    public void setStartupScript(String script) {
        String oldScript = getStartupScript();

        if (!getConfig().isSetSetupScript()) {
            getConfig().addNewSetupScript();
        }

        getConfig().getSetupScript().setStringValue(script);
        if (startupScriptEngine != null) {
            startupScriptEngine.setScript(script);
        }

        notifyPropertyChanged(STARTUP_SCRIPT_PROPERTY, oldScript, script);
    }

    /**
     * @return The current startup script
     */
    public String getStartupScript() {
        return getConfig() != null ? (getConfig().isSetSetupScript() ? getConfig().getSetupScript().getStringValue()
                : "") : "";
    }

    /**
     * Executes the startup Script
     *
     * @param runContext
     * @param runner
     * @return
     * @throws Exception
     */
    public Object runStartupScript(SecurityTestRunContext runContext, SecurityTestRunner runner) throws Exception {
        String script = getStartupScript();
        if (StringUtils.isNullOrEmpty(script)) {
            return null;
        }

        if (startupScriptEngine == null) {
            startupScriptEngine = SoapUIScriptEngineRegistry.create(this);
            startupScriptEngine.setScript(script);
        }

        startupScriptEngine.setVariable("context", runContext);
        startupScriptEngine.setVariable("securityTestRunner", runner);
        startupScriptEngine.setVariable("log", SoapUI.ensureGroovyLog());
        return startupScriptEngine.run();
    }

    /**
     * Sets the script to be used on teardown
     *
     * @param script
     */
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

    /**
     * @return The current teardown script
     */
    public String getTearDownScript() {
        return getConfig() != null ? (getConfig().isSetTearDownScript() ? getConfig().getTearDownScript()
                .getStringValue() : "") : "";
    }

    /**
     * Executes the teardown Script
     *
     * @param runContext
     * @param runner
     * @return
     * @throws Exception
     */
    public Object runTearDownScript(SecurityTestRunContext runContext, SecurityTestRunner runner) throws Exception {
        String script = getTearDownScript();
        if (StringUtils.isNullOrEmpty(script)) {
            return null;
        }

        if (tearDownScriptEngine == null) {
            tearDownScriptEngine = SoapUIScriptEngineRegistry.create(this);
            tearDownScriptEngine.setScript(script);
        }

        tearDownScriptEngine.setVariable("context", runContext);
        tearDownScriptEngine.setVariable("securityTestRunner", runner);
        tearDownScriptEngine.setVariable("log", SoapUI.ensureGroovyLog());
        return tearDownScriptEngine.run();
    }

    public List<SecurityScan> getTestStepSecurityScans(String testStepId) {
        return getSecurityScansMap().get(testStepId) != null ? getSecurityScansMap().get(testStepId)
                : new ArrayList<SecurityScan>();
    }

    public SecurityScan getTestStepSecurityScanByName(String testStepId, String securityScanName) {
        List<SecurityScan> securityScansList = getTestStepSecurityScans(testStepId);
        for (int c = 0; c < securityScansList.size(); c++) {
            SecurityScan securityScan = getTestStepSecurityScanAt(testStepId, c);
            if (securityScanName.equals(securityScan.getName())) {
                return securityScan;
            }
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    public <T extends SecurityScan> List<T> getTestStepSecurityScanByType(String testStepId, Class<T> securityScanType) {
        List<T> result = new ArrayList<T>();
        for (SecurityScan scan : getTestStepSecurityScans(testStepId)) {
            if (securityScanType.isAssignableFrom(scan.getClass())) {
                result.add((T) scan);
            }
        }

        return result;
    }

    public SecurityScan getTestStepSecurityScanAt(String testStepId, int index) {
        List<SecurityScan> securityScansList = getTestStepSecurityScans(testStepId);
        return securityScansList.get(index);
    }

    public int getTestStepSecurityScansCount(String testStepId) {
        if (getSecurityScansMap().isEmpty()) {
            return 0;
        } else {
            if (getSecurityScansMap().get(testStepId) != null) {
                return getSecurityScansMap().get(testStepId).size();
            } else {
                return 0;
            }
        }
    }

    /**
     * Moves specified SecurityScan of a TestStep in a list
     *
     * @param testStep
     * @param securityScan
     * @param index
     * @param offset       specifies position to move to , negative value means moving up
     *                     while positive value means moving down
     * @return new AbstractSecurityScan
     */
    public SecurityScan moveTestStepSecurityScan(TestStep testStep, SecurityScan securityScan, int index, int offset) {
        List<TestStepSecurityTestConfig> testStepSecurityTestList = getConfig().getTestStepSecurityTestList();
        if (!testStepSecurityTestList.isEmpty()) {
            for (TestStepSecurityTestConfig testStepSecurityTest : testStepSecurityTestList) {
                if (testStepSecurityTest.getTestStepId().equals(testStep.getId())) {
                    List<SecurityScanConfig> securityScanList = testStepSecurityTest.getTestStepSecurityScanList();
                    SecurityScanFactory factory = SoapUI.getSoapUICore().getSecurityScanRegistry()
                            .getFactory(securityScan.getType());
                    SecurityScanConfig newSecScanConfig = (SecurityScanConfig) securityScan.getConfig().copy();
                    SecurityScan newSecScan = factory.buildSecurityScan(testStep, newSecScanConfig, this);

                    securityScanList.remove(securityScan.getConfig());
                    securityScanList.add(index + offset, newSecScanConfig);
                    SecurityScanConfig[] cc = new SecurityScanConfig[securityScanList.size()];
                    for (int i = 0; i < securityScanList.size(); i++) {
                        cc[i] = securityScanList.get(i);
                    }
                    testStepSecurityTest.setTestStepSecurityScanArray(cc);

                    TestStepSecurityTestConfig[] vv = new TestStepSecurityTestConfig[testStepSecurityTestList.size()];
                    for (int i = 0; i < testStepSecurityTestList.size(); i++) {
                        vv[i] = testStepSecurityTestList.get(i);
                    }
                    getConfig().setTestStepSecurityTestArray(vv);
                    return newSecScan;
                }
            }
        }
        return null;
    }

    public String findTestStepScanUniqueName(String testStepId, String type) {
        String name = type;
        int numNames = 0;
        List<SecurityScan> securityScansList = getTestStepSecurityScans(testStepId);
        if (securityScansList != null && !securityScansList.isEmpty()) {
            for (SecurityScan existingScan : securityScansList) {
                if (existingScan.getType().equals(name)) {
                    numNames++;
                }
            }
        }
        if (numNames != 0) {
            name += " " + numNames;
        }
        return name;
    }

    public void addSecurityTestRunListener(SecurityTestRunListener listener) {
        if (listener == null) {
            throw new RuntimeException("listener must not be null");
        }

        securityTestRunListeners.add(listener);
    }

    public void removeSecurityTestRunListener(SecurityTestRunListener listener) {
        securityTestRunListeners.remove(listener);
    }

    public SecurityTestRunListener[] getSecurityTestRunListeners() {
        return securityTestRunListeners.toArray(new SecurityTestRunListener[securityTestRunListeners.size()]);
    }

    public boolean getFailSecurityTestOnScanErrors() {
        return getConfig().getFailSecurityTestOnScanErrors();
    }

    public void setFailSecurityTestOnScanErrors(boolean failSecurityTestOnErrors) {
        boolean old = getFailSecurityTestOnScanErrors();
        if (old != failSecurityTestOnErrors) {
            getConfig().setFailSecurityTestOnScanErrors(failSecurityTestOnErrors);
            notifyPropertyChanged(FAIL_ON_SCANS_ERRORS_PROPERTY, old, failSecurityTestOnErrors);
        }
    }

    public boolean getFailOnError() {
        return getConfig().getFailOnError();
    }

    public void setFailOnError(boolean failOnError) {
        boolean old = getFailOnError();
        if (old != failOnError) {
            getConfig().setFailOnError(failOnError);
            notifyPropertyChanged(FAIL_ON_ERROR_PROPERTY, old, failOnError);
        }
    }

    public boolean getSkipDataSourceLoops() {
        return getConfig().getSkipDataSourceLoops();
    }

    public void setSkipDataSourceLoops(boolean skipDataSourceLoops) {
        boolean old = getSkipDataSourceLoops();
        if (old != skipDataSourceLoops) {
            getConfig().setSkipDataSourceLoops(skipDataSourceLoops);
            notifyPropertyChanged(SKIP_DATASOURCE_LOOP_PROPERTY, old, skipDataSourceLoops);
        }
    }

    public void addTestStepRunListener(TestStep testStep, SecurityTestRunListener listener) {
        if (listener == null) {
            throw new RuntimeException("listener must not be null");
        }

        if (securityTestStepRunListeners.containsKey(testStep)) {
            securityTestStepRunListeners.get(testStep).add(listener);
        } else {
            Set<SecurityTestRunListener> listeners = new HashSet<SecurityTestRunListener>();
            listeners.add(listener);
            securityTestStepRunListeners.put(testStep, listeners);
        }
    }

    public void removeTestStepRunListener(TestStep testStep, SecurityTestRunListener listener) {
        securityTestStepRunListeners.remove(securityTestStepRunListeners.get(testStep));
    }

    public SecurityTestRunListener[] getTestStepRunListeners(TestStep testStep) {
        if (securityTestStepRunListeners.containsKey(testStep)) {
            Set<SecurityTestRunListener> listeners = securityTestStepRunListeners.get(testStep);
            return listeners.toArray(new SecurityTestRunListener[listeners.size()]);
        } else {
            return new SecurityTestRunListener[0];
        }
    }

    @Override
    public List<? extends ModelItem> getChildren() {
        List<ModelItem> result = new ArrayList<ModelItem>();
        Set<String> testStepIds = getSecurityScansMap().keySet();
        for (String testStepId : testStepIds) {
            List<SecurityScan> t = getSecurityScansMap().get(testStepId);
            for (int i = 0; i < t.size(); i++) {
                SecurityScan scan = t.get(i);
                result.add((ModelItem) scan);
            }
        }
        return result;
    }

    public void resetConfigOnMove(SecurityTestConfig securityTestConfig) {
        setConfig(securityTestConfig);

        if (securityTestConfig != null) {
            if (!securityTestConfig.getTestStepSecurityTestList().isEmpty()) {
                for (TestStepSecurityTestConfig testStepSecurityTestListConfig : securityTestConfig
                        .getTestStepSecurityTestList()) {
                    List<SecurityScan> scanList = getSecurityScansMap().get(testStepSecurityTestListConfig.getTestStepId());

                    for (int i = 0; i < scanList.size(); i++) {
                        scanList.get(i).updateSecurityConfig(
                                testStepSecurityTestListConfig.getTestStepSecurityScanList().get(i));
                    }
                }
            }
        }
    }

    /**
     * Checks if we can add new SecurityScan for the specific TestStep (only one
     * type of SecurityScan for TestStep is allowed)
     *
     * @param testStep
     * @return boolean
     */
    public boolean canAddSecurityScan(TestStep testStep, String securityScanName) {
        boolean hasScansOfType = false;
        String securityScanType = SoapUI.getSoapUICore().getSecurityScanRegistry()
                .getSecurityScanTypeForName(securityScanName);

        for (SecurityScan scan : getTestStepSecurityScans(testStep.getId())) {
            if (securityScanType.equals(scan.getType())) {
                hasScansOfType = true;
                break;
            }
        }

        return !hasScansOfType;
    }

    /**
     * Creates array of all available security scan names (those that have not
     * been added to test step).
     *
     * @param testStep
     * @return boolean
     */
    public String[] getAvailableSecurityScanNames(TestStep testStep, String[] securityScanNames) {
        List<String> availableNames = new ArrayList<String>();

        for (int i = 0; i < securityScanNames.length; i++) {
            String name = securityScanNames[i];
            if (canAddSecurityScan(testStep, name)) {
                availableNames.add(name);
            }
        }

        return availableNames.toArray(new String[availableNames.size()]);
    }

    public boolean importSecurityScan(TestStep targetTestStep, SecurityScan securityScanToClone, boolean overwrite) {
        // testCase.beforeSave();
        XmlObject newConfig = securityScanToClone.getConfig().copy();

        SecurityScanConfig newScanConfig = SecurityScanConfig.Factory.newInstance();
        newScanConfig.set(newConfig);
        SecurityScanFactory factory = SoapUI.getSoapUICore().getSecurityScanRegistry()
                .getFactory(newScanConfig.getType());
        boolean targetStepHasScans = getTestStepSecurityScansCount(targetTestStep.getId()) > 0;
        if (targetStepHasScans) {
            boolean targetHasScanOfSameType = false;
            for (SecurityScan oldScan : getTestStepSecurityScans(targetTestStep.getId())) {
                if (oldScan.getType().equals(securityScanToClone.getType())) {
                    // there already is a scan of particular type in target
                    // teststep
                    targetHasScanOfSameType = true;
                    if (overwrite) {
                        removeSecurityScan(targetTestStep, oldScan);
                        addSecurityScan(targetTestStep, factory, newScanConfig);
                    } else {
                        return false;
                    }
                    break;
                }
            }
            if (!targetHasScanOfSameType) {
                // teststep doesn't have particular scan, but has other
                // scans
                addSecurityScan(targetTestStep, factory, newScanConfig);
            }

        } else {
            // teststep doesn't have particular scan, but has other
            // scans
            addSecurityScan(targetTestStep, factory, newScanConfig);
        }

        return true;
    }

    public void addSecurityTestListener(SecurityTestListener listener) {
        securityTestListeners.add(listener);
    }

    public void removeSecurityTestListener(SecurityTestListener listener) {
        securityTestListeners.remove(listener);
    }

    public boolean isRunning() {
        if (runner == null) {
            return false;
        } else {
            return runner.isRunning();
        }
    }

    protected boolean skipTest(TestStep testStep) {
        return false;
    }

    public void resetAllScansSkipFurtherRunning() {
        for (String testStepId : getSecurityScansMap().keySet()) {
            for (SecurityScan scan : getTestStepSecurityScans(testStepId)) {
                scan.setSkipFurtherRunning(false);
            }
        }
    }
}
