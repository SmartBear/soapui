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

package com.eviware.soapui.integration.loadui;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.LoadTestLimitTypesConfig;
import com.eviware.soapui.impl.wsdl.loadtest.LoadTestAssertion;
import com.eviware.soapui.impl.wsdl.loadtest.WsdlLoadTest;
import com.eviware.soapui.impl.wsdl.loadtest.assertions.TestStepAverageAssertion;
import com.eviware.soapui.impl.wsdl.loadtest.assertions.TestStepMaxAssertion;
import com.eviware.soapui.impl.wsdl.loadtest.assertions.TestStepTpsAssertion;
import com.eviware.soapui.impl.wsdl.loadtest.strategy.BurstLoadStrategy;
import com.eviware.soapui.impl.wsdl.loadtest.strategy.LoadStrategy;
import com.eviware.soapui.impl.wsdl.loadtest.strategy.SimpleLoadStrategy;
import com.eviware.soapui.impl.wsdl.loadtest.strategy.ThreadCountChangeLoadStrategy;
import com.eviware.soapui.impl.wsdl.loadtest.strategy.VarianceLoadStrategy;
import com.eviware.soapui.settings.HttpSettings;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ContextMapping {
    private static final String NOT_SELECTED = "-";
    private static final String TEST_CASE = "testCase";
    private static final String TEST_SUITE = "testSuite";
    private static final String PROJECT_FILE = "projectFile";
    public static final String MOCK_SERVICE = "mockService";
    public static final String PATH = "path";
    public static final String PORT = "port";
    private static final String CLOSE_CONNECTIONS_AFTER_REQUEST = "closeConnectionsAfterRequest";

    private static final String SOAPUI_RUNNER_LABEL = "soapuiSamplerLabel";
    public static final String LOADUI_TEST_CASE_NAME = "loaduiTestCaseName";
    public static final String LOADUI_PROJECT_NAME = "loaduiProjectName";
    private static final String SOAPUI_RUNNER_PROPERTIES = "SoapUISamplerComponent_properties";
    private static final String MOCKSERVICE_RUNNER_LABEL = "mockRunnerLabel";
    private static final String MOCKSERVICE_RUNNER_PROPERTIES = "MockServiceComponent_properties";

    private static final String ASSERTION_LABEL = "assertionLabel";
    private static final String ASSERTION_TYPE = "assertionType";
    private static final String ASSERTION_PROPERTIES = "assertionProperties";
    private static final String ASSERTION = "Assertion";
    private static final String ASSERTION_CREATE_NEW = "assertionCreateNew";

    private static final String STATISTICS_LABEL = "statisticsLabel";
    private static final String STATISTICS_TYPE = "statisticsType";
    private static final String STATISTICS_PROPERTIES = "statisticsProperties";
    private static final String STATISTICS = "Statistics";
    private static final String STATISTICS_CREATE_NEW = "statisticsCreateNew";

    private static final String TRIGGER_LABEL = "triggerLabel";
    private static final String TRIGGER_TYPE = "triggerType";
    protected static final String TRIGGER_PROPERTIES = "triggerProperties";
    private static final String TRIGGER_CREATE_NEW = "triggerCreateNew";

    private static final String DELAY_LABEL = "delayLabel";
    private static final String DELAY_TYPE = "delayType";
    private static final String DELAY_PROPERTIES = "delayProperties";
    private static final String DELAY = "Delay";
    private static final String DELAY_CREATE_NEW = "delayCreateNew";

    protected static final String FIXED_LOAD_TRIGGER = "Fixed Load";
    private static final String VARIANCE_TRIGGER = "Variance";
    private static final String RAMP_TRIGGER = "Ramp";

    private WsdlLoadTest loadTest;
    private String loadUIProject;
    private String loadUITestCase;
    private String loadUISoapUISampler;
    private String soapUIProjectPath;
    private String soapUITestSuite;
    private String soapUITestCase;
    protected String loadUITriggerType;
    private String soapUIMockService;
    private String mockServicePath;
    private String mockServicePort;
    private String loadUIMockServiceRunner;

    HashMap<String, String> triggerProperties;
    HashMap<String, String> delayProperties;
    List<HashMap<String, String>> assertionPropertiesList;
    HashMap<String, String> statisticsProperties;

    public ContextMapping(WsdlLoadTest loadTest, String loadUIProject, String loadUITestCase, String loadUISoapUISampler) {
        this.loadTest = loadTest;
        this.loadUIProject = loadUIProject;
        this.loadUITestCase = loadUITestCase;
        this.loadUISoapUISampler = loadUISoapUISampler;
    }

    public ContextMapping(String soapUIProjectPath, String soapUITestSuite, String soapUITestCase,
                          String loadUIProject, String loadUITestCase, String loadUISoapUISampler) {
        this.loadUIProject = loadUIProject;
        this.loadUITestCase = loadUITestCase;
        this.loadUISoapUISampler = loadUISoapUISampler;
        this.soapUIProjectPath = soapUIProjectPath;
        this.soapUITestSuite = soapUITestSuite;
        this.soapUITestCase = soapUITestCase;

    }

    public ContextMapping(String soapUIProjectPath, String soapUIMockService, String path, String port,
                          String loadUIProject, String loadUITestCase, String loadUIMockServiceRunner) {
        this.loadUIProject = loadUIProject;
        this.loadUITestCase = loadUITestCase;
        this.loadUIMockServiceRunner = loadUIMockServiceRunner;
        this.soapUIProjectPath = soapUIProjectPath;
        this.soapUIMockService = soapUIMockService;
        this.mockServicePath = path;
        this.mockServicePort = port;

    }

    public static String createProperyValue(Class clazz, String value) {
        return clazz.getName() + "@" + value;
    }

    public HashMap<String, Object> setCreateSoapUIRunnerContext(String generatorType, String analisysType) {
        HashMap<String, Object> context = new HashMap<String, Object>();
        HashMap<String, String> properties = new HashMap<String, String>();

        properties.put(PROJECT_FILE, createProperyValue(File.class, soapUIProjectPath));
        properties.put(TEST_SUITE, createProperyValue(String.class, soapUITestSuite));
        properties.put(TEST_CASE, createProperyValue(String.class, soapUITestCase));
        context.put(LOADUI_PROJECT_NAME, loadUIProject);
        context.put(LOADUI_TEST_CASE_NAME, loadUITestCase);
        context.put(SOAPUI_RUNNER_LABEL, loadUISoapUISampler);
        context.put(SOAPUI_RUNNER_PROPERTIES, properties);

        if (!NOT_SELECTED.equals(generatorType)) {
            mapDefaultTriggerProperties(generatorType);
            context.put(TRIGGER_PROPERTIES, triggerProperties);
            context.put(TRIGGER_LABEL, loadUITriggerType);
            context.put(TRIGGER_TYPE, loadUITriggerType);
            context.put(TRIGGER_CREATE_NEW, new Boolean(true));
        }

        if (!NOT_SELECTED.equals(analisysType)) {
            mapStatisticsProperties(null);
            context.put(STATISTICS_PROPERTIES, statisticsProperties);
            context.put(STATISTICS_LABEL, STATISTICS);
            context.put(STATISTICS_TYPE, STATISTICS);
            context.put(STATISTICS_CREATE_NEW, new Boolean(true));
        }
        return context;
    }

    public HashMap<String, Object> setCreateMockServiceRunnerContext() {
        HashMap<String, Object> context = new HashMap<String, Object>();
        HashMap<String, String> properties = new HashMap<String, String>();

        properties.put(PROJECT_FILE, createProperyValue(File.class, soapUIProjectPath));
        properties.put(MOCK_SERVICE, createProperyValue(String.class, soapUIMockService));
        properties.put(PATH, createProperyValue(String.class, mockServicePath));
        properties.put(PORT, createProperyValue(String.class, mockServicePort));
        context.put(LOADUI_PROJECT_NAME, loadUIProject);
        context.put(LOADUI_TEST_CASE_NAME, loadUITestCase);
        context.put(MOCKSERVICE_RUNNER_LABEL, loadUIMockServiceRunner);
        context.put(MOCKSERVICE_RUNNER_PROPERTIES, properties);
        return context;
    }

    public HashMap<String, Object> setInitExportLoadTestToLoadUIContext() {
        HashMap<String, Object> context = new HashMap<String, Object>();
        HashMap<String, String> properties = new HashMap<String, String>();

        properties.put(PROJECT_FILE,
                createProperyValue(File.class, loadTest.getTestCase().getTestSuite().getProject().getPath()));
        properties.put(TEST_SUITE, createProperyValue(String.class, loadTest.getTestCase().getTestSuite().getName()));
        properties.put(TEST_CASE, createProperyValue(String.class, loadTest.getTestCase().getName()));

        properties.put(
                CLOSE_CONNECTIONS_AFTER_REQUEST,
                createProperyValue(Boolean.class,
                        Boolean.toString(loadTest.getSettings().getBoolean(HttpSettings.CLOSE_CONNECTIONS))));

        context.put(LOADUI_PROJECT_NAME, loadUIProject);
        context.put(LOADUI_TEST_CASE_NAME, loadUITestCase);
        context.put(SOAPUI_RUNNER_LABEL, loadUISoapUISampler);

        context.put(SOAPUI_RUNNER_PROPERTIES, properties);

        mapInitialTriggerProperties(loadTest);
        context.put(TRIGGER_PROPERTIES, triggerProperties);
        context.put(TRIGGER_LABEL, loadUITriggerType);
        context.put(TRIGGER_TYPE, loadUITriggerType);
        context.put(TRIGGER_CREATE_NEW, new Boolean(true));

        if (delayProperties != null) {
            context.put(DELAY_PROPERTIES, delayProperties);
            context.put(DELAY_LABEL, DELAY);
            context.put(DELAY_TYPE, DELAY);
            context.put(DELAY_CREATE_NEW, new Boolean(true));
        }
        mapAssertionProperties(loadTest);
        for (int i = 0; i < assertionPropertiesList.size(); i++) {
            context.put(ASSERTION_PROPERTIES + i, assertionPropertiesList.get(i));
            context.put(ASSERTION_LABEL + i, ASSERTION + i);
            context.put(ASSERTION_TYPE + i, ASSERTION);
            context.put(ASSERTION_CREATE_NEW + i, new Boolean(true));

        }
        mapStatisticsProperties(loadTest);
        context.put(STATISTICS_PROPERTIES, statisticsProperties);
        context.put(STATISTICS_LABEL, STATISTICS);
        context.put(STATISTICS_TYPE, STATISTICS);
        context.put(STATISTICS_CREATE_NEW, new Boolean(true));

        return context;
    }

    protected void mapInitialTriggerProperties(WsdlLoadTest loadTest) {
        triggerProperties = new HashMap<String, String>();
        LoadStrategy loadStrategy = loadTest.getLoadStrategy();
        if (loadStrategy instanceof SimpleLoadStrategy) {
            SimpleLoadStrategy currentStrategy = (SimpleLoadStrategy) loadStrategy;
            loadUITriggerType = FIXED_LOAD_TRIGGER;
            triggerProperties.put("load", createProperyValue(Long.class, Long.toString(loadTest.getThreadCount())));
            long testDelay = currentStrategy.getTestDelay();
            if (testDelay > 0) {
                delayProperties = new HashMap<String, String>();
                delayProperties.put("delay", createProperyValue(Long.class, Long.toString(testDelay)));
                int randomFactor = (int) (currentStrategy.getRandomFactor() * 100);
                delayProperties.put("randomDelay", createProperyValue(Long.class, Integer.toString(randomFactor)));
            }
            return;
        }
        if (loadStrategy instanceof VarianceLoadStrategy) {
            loadUITriggerType = VARIANCE_TRIGGER;
            VarianceLoadStrategy currentStrategy = (VarianceLoadStrategy) loadStrategy;
            return;
        }
        if (loadStrategy instanceof ThreadCountChangeLoadStrategy) {
            loadUITriggerType = RAMP_TRIGGER;
            ThreadCountChangeLoadStrategy currentStrategy = (ThreadCountChangeLoadStrategy) loadStrategy;
            return;
        }
        if (loadStrategy instanceof BurstLoadStrategy) {
            loadUITriggerType = FIXED_LOAD_TRIGGER;
            triggerProperties.put("load", createProperyValue(Long.class, Long.toString(loadTest.getThreadCount())));
            return;
        }
    }

    protected void mapDefaultTriggerProperties(String generatorType) {
        triggerProperties = new HashMap<String, String>();
        loadUITriggerType = generatorType;
    }

    public HashMap<String, Object> setFinalExportLoadTestToLoadUIContext(HashMap<String, Object> createdItemContext) {
        mapExistingTriggerProperties(loadTest, createdItemContext);
        createdItemContext.put(TRIGGER_PROPERTIES, triggerProperties);
        return createdItemContext;
    }

    // repopulates the properties with right values, if property is not set old
    // value it means leave the old value
    protected HashMap<String, String> mapExistingTriggerProperties(WsdlLoadTest loadTest,
                                                                   HashMap<String, Object> createdItemContext) {
        LoadStrategy loadStrategy = loadTest.getLoadStrategy();
        triggerProperties = (HashMap<String, String>) createdItemContext.get(TRIGGER_PROPERTIES);
        if (loadStrategy instanceof VarianceLoadStrategy) {
            loadUITriggerType = VARIANCE_TRIGGER;
            VarianceLoadStrategy currentStrategy = (VarianceLoadStrategy) loadStrategy;
            long rate = extractLongProperty("rate");
            triggerProperties.put("shape", createProperyValue(String.class, "Sine-wave"));
            triggerProperties.put("amplitude",
                    createProperyValue(Long.class, Long.toString(((long) (currentStrategy.getVariance() * rate)))));
            triggerProperties.put("period",
                    createProperyValue(Long.class, Long.toString(currentStrategy.getInterval() / 1000)));
        }
        if (loadStrategy instanceof ThreadCountChangeLoadStrategy) {
            loadUITriggerType = RAMP_TRIGGER;
            ThreadCountChangeLoadStrategy currentStrategy = (ThreadCountChangeLoadStrategy) loadStrategy;
            long end = extractLongProperty("end");
            triggerProperties.put(
                    "end",
                    createProperyValue(
                            Long.class,
                            Long.toString(end
                                    * (currentStrategy.getEndThreadCount() / currentStrategy.getStartThreadCount()))));
            if (loadTest.getLimitType().equals(LoadTestLimitTypesConfig.TIME)) {
                triggerProperties
                        .put("period", createProperyValue(Long.class, Long.toString(loadTest.getTestLimit())));
            }
        }
        return triggerProperties;
    }

    public Long extractLongProperty(String propertyKey) {
        Long value = null;
        String[] parts = triggerProperties.get(propertyKey).split("@");
        try {
            value = new Long(parts[1]);
        } catch (NumberFormatException e) {
            SoapUI.logError(e, "property " + propertyKey + "is not a Long");
        }
        return value;
    }

    private void mapAssertionProperties(WsdlLoadTest loadTest) {
        assertionPropertiesList = new ArrayList<HashMap<String, String>>();
        List<LoadTestAssertion> loadTestAssertions = loadTest.getAssertionList();
        for (LoadTestAssertion loadTestAssertion : loadTestAssertions) {
            if (loadTestAssertion.getTargetStep().equals(LoadTestAssertion.ALL_TEST_STEPS)) {
                HashMap<String, String> asrtProperties = new HashMap<String, String>();
                if (loadTestAssertion instanceof TestStepTpsAssertion) {
                    asrtProperties.put("value", createProperyValue(String.class, "Tps"));
                }
                if (loadTestAssertion instanceof TestStepMaxAssertion) {
                    asrtProperties.put("value", createProperyValue(String.class, "Max"));
                }
                if (loadTestAssertion instanceof TestStepAverageAssertion) {
                    asrtProperties.put("value", createProperyValue(String.class, "Avg"));
                }
                assertionPropertiesList.add(asrtProperties);
            }
        }

    }

    private void mapStatisticsProperties(WsdlLoadTest loadTest) {
        statisticsProperties = new HashMap<String, String>();
    }

    public boolean isFinalTriggerMappingNeeded() {
        if (loadUITriggerType.equals(VARIANCE_TRIGGER) || loadUITriggerType.equals(RAMP_TRIGGER)) {
            return true;
        } else {
            return false;
        }
    }

}
