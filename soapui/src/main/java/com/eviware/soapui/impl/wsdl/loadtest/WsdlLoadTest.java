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

package com.eviware.soapui.impl.wsdl.loadtest;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.LoadStrategyConfig;
import com.eviware.soapui.config.LoadTestAssertionConfig;
import com.eviware.soapui.config.LoadTestConfig;
import com.eviware.soapui.config.LoadTestLimitTypesConfig;
import com.eviware.soapui.config.LoadTestLimitTypesConfig.Enum;
import com.eviware.soapui.impl.wsdl.AbstractWsdlModelItem;
import com.eviware.soapui.impl.wsdl.loadtest.assertions.AbstractLoadTestAssertion;
import com.eviware.soapui.impl.wsdl.loadtest.assertions.LoadTestAssertionRegistry;
import com.eviware.soapui.impl.wsdl.loadtest.data.LoadTestStatistics;
import com.eviware.soapui.impl.wsdl.loadtest.log.LoadTestLog;
import com.eviware.soapui.impl.wsdl.loadtest.log.LoadTestLogErrorEntry;
import com.eviware.soapui.impl.wsdl.loadtest.strategy.BurstLoadStrategy;
import com.eviware.soapui.impl.wsdl.loadtest.strategy.LoadStrategy;
import com.eviware.soapui.impl.wsdl.loadtest.strategy.LoadStrategyFactory;
import com.eviware.soapui.impl.wsdl.loadtest.strategy.LoadStrategyRegistry;
import com.eviware.soapui.impl.wsdl.loadtest.strategy.SimpleLoadStrategy;
import com.eviware.soapui.impl.wsdl.support.Configurable;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCaseRunner;
import com.eviware.soapui.impl.wsdl.teststeps.SimplePathPropertySupport;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.model.support.LoadTestRunListenerAdapter;
import com.eviware.soapui.model.testsuite.LoadTest;
import com.eviware.soapui.model.testsuite.LoadTestRunContext;
import com.eviware.soapui.model.testsuite.LoadTestRunListener;
import com.eviware.soapui.model.testsuite.LoadTestRunner;
import com.eviware.soapui.model.testsuite.TestCaseRunContext;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.TestRunnable;
import com.eviware.soapui.model.testsuite.TestRunner;
import com.eviware.soapui.model.testsuite.TestRunner.Status;
import com.eviware.soapui.model.testsuite.TestStepResult;
import com.eviware.soapui.settings.HttpSettings;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.scripting.SoapUIScriptEngine;
import com.eviware.soapui.support.scripting.SoapUIScriptEngineRegistry;
import com.eviware.soapui.support.types.StringList;
import com.eviware.soapui.support.types.StringToObjectMap;
import org.apache.commons.collections.list.TreeList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * TestCase implementation for LoadTests
 *
 * @author Ole.Matzura
 * @todo add assertionFailed event to LoadTestListener
 * @todo create and return LoadTestAssertionResult from load-test assertions
 */

@SuppressWarnings("unchecked")
public class WsdlLoadTest extends AbstractWsdlModelItem<LoadTestConfig> implements LoadTest, TestRunnable {
    public final static String THREADCOUNT_PROPERTY = WsdlLoadTest.class.getName() + "@threadcount";
    public final static String STARTDELAY_PROPERTY = WsdlLoadTest.class.getName() + "@startdelay";
    public final static String TESTLIMIT_PROPERTY = WsdlLoadTest.class.getName() + "@testlimit";
    public final static String HISTORYLIMIT_PROPERTY = WsdlLoadTest.class.getName() + "@historylimit";
    public final static String LIMITTYPE_PROPERRY = WsdlLoadTest.class.getName() + "@limittype";
    public final static String SAMPLEINTERVAL_PROPERRY = WsdlLoadTest.class.getName() + "@sample-interval";
    public static final String MAXASSERTIONERRORS_PROPERTY = WsdlLoadTest.class.getName() + "@max-assertion-errors";
    public final static String SETUP_SCRIPT_PROPERTY = WsdlTestCase.class.getName() + "@setupScript";
    public final static String TEARDOWN_SCRIPT_PROPERTY = WsdlTestCase.class.getName() + "@tearDownScript";

    private final static Logger logger = LogManager.getLogger(WsdlLoadTest.class);
    public static final int DEFAULT_STRATEGY_INTERVAL = 500;
    public static final String ICON_NAME = "/loadTest.png";

    private InternalTestRunListener internalTestRunListener = new InternalTestRunListener();

    private WsdlTestCase testCase;
    private LoadTestStatistics statisticsModel;
    private LoadStrategy loadStrategy = new BurstLoadStrategy(this);
    private LoadTestLog loadTestLog;

    private LoadStrategyConfigurationChangeListener loadStrategyListener = new LoadStrategyConfigurationChangeListener();
    private List<LoadTestAssertion> assertions = new ArrayList<LoadTestAssertion>();
    private ConfigurationChangePropertyListener configurationChangeListener = new ConfigurationChangePropertyListener();
    private Set<LoadTestListener> loadTestListeners = new HashSet<LoadTestListener>();
    private Set<LoadTestRunListener> loadTestRunListeners = new HashSet<LoadTestRunListener>();
    private List<LoadTestLogErrorEntry> assertionErrors = new TreeList();
    private WsdlLoadTestRunner runner;
    private StatisticsLogger statisticsLogger = new StatisticsLogger();
    private SoapUIScriptEngine setupScriptEngine;
    private SoapUIScriptEngine tearDownScriptEngine;
    @SuppressWarnings("unused")
    private SimplePathPropertySupport logFolder;
    private LoadTestRunListener[] loadTestRunListenersArray;

    public WsdlLoadTest(WsdlTestCase testCase, LoadTestConfig config) {
        super(config, testCase, ICON_NAME);

        this.testCase = testCase;

        if (getConfig().getThreadCount() < 1) {
            getConfig().setThreadCount(5);
        }

        if (getConfig().getLimitType() == null) {
            getConfig().setLimitType(LoadTestLimitTypesConfig.TIME);
            getConfig().setTestLimit(60);
        }

        if (!getConfig().isSetHistoryLimit()) {
            getConfig().setHistoryLimit(-1);
        }

        addLoadTestRunListener(internalTestRunListener);

        LoadStrategyConfig ls = getConfig().getLoadStrategy();
        if (ls == null) {
            ls = getConfig().addNewLoadStrategy();
            ls.setType(SimpleLoadStrategy.STRATEGY_TYPE);
        }

        LoadStrategyFactory factory = LoadStrategyRegistry.getInstance().getFactory(ls.getType());
        if (factory == null) {
            ls.setType(SimpleLoadStrategy.STRATEGY_TYPE);
            factory = LoadStrategyRegistry.getInstance().getFactory(ls.getType());
        }

        loadStrategy = factory.build(ls.getConfig(), this);
        loadStrategy.addConfigurationChangeListener(loadStrategyListener);

        addLoadTestRunListener(loadStrategy);

        statisticsModel = new LoadTestStatistics(this);

        if (getConfig().xgetSampleInterval() == null) {
            setSampleInterval(LoadTestStatistics.DEFAULT_SAMPLE_INTERVAL);
        }

        statisticsModel.setUpdateFrequency(getSampleInterval());

        List<LoadTestAssertionConfig> assertionList = getConfig().getAssertionList();
        for (LoadTestAssertionConfig assertionConfig : assertionList) {
            AbstractLoadTestAssertion assertion = LoadTestAssertionRegistry.buildAssertion(assertionConfig, this);
            if (assertion != null) {
                assertions.add(assertion);
                assertion.addPropertyChangeListener(LoadTestAssertion.CONFIGURATION_PROPERTY, configurationChangeListener);
            } else {
                logger.warn("Failed to build LoadTestAssertion from getConfig() [" + assertionConfig + "]");
            }
        }

        if (getConfig().xgetResetStatisticsOnThreadCountChange() == null) {
            getConfig().setResetStatisticsOnThreadCountChange(true);
        }

        if (getConfig().xgetCalculateTPSOnTimePassed() == null) {
            getConfig().setCalculateTPSOnTimePassed(true);
        }

        if (!getConfig().isSetMaxAssertionErrors()) {
            getConfig().setMaxAssertionErrors(100);
        }

        if (getConfig().xgetCancelExcessiveThreads() == null) {
            getConfig().setCancelExcessiveThreads(true);
        }

        if (getConfig().xgetStrategyInterval() == null) {
            getConfig().setStrategyInterval(DEFAULT_STRATEGY_INTERVAL);
        }

        loadTestLog = new LoadTestLog(this);

        for (LoadTestRunListener listener : SoapUI.getListenerRegistry().getListeners(LoadTestRunListener.class)) {
            addLoadTestRunListener(listener);
        }

        // set close-connections to same as global so override works ok
        if (!getSettings().isSet(HttpSettings.CLOSE_CONNECTIONS)) {
            getSettings().setBoolean(HttpSettings.CLOSE_CONNECTIONS,
                    SoapUI.getSettings().getBoolean(HttpSettings.CLOSE_CONNECTIONS));
        }
    }

    public LoadTestStatistics getStatisticsModel() {
        return statisticsModel;
    }

    public StatisticsLogger getStatisticsLogger() {
        return statisticsLogger;
    }

    public long getThreadCount() {
        return getConfig().getThreadCount();
    }

    public void setThreadCount(long threadCount) {
        long oldCount = getThreadCount();
        if (threadCount == oldCount) {
            return;
        }

        if (getLogStatisticsOnThreadChange() && isRunning()) {
            statisticsLogger.logStatistics("ThreadCount change from " + oldCount + " to " + threadCount);
        }

        getConfig().setThreadCount((int) threadCount);
        notifyPropertyChanged(THREADCOUNT_PROPERTY, oldCount, threadCount);
    }

    public boolean getResetStatisticsOnThreadCountChange() {
        return getConfig().getResetStatisticsOnThreadCountChange();
    }

    public void setResetStatisticsOnThreadCountChange(boolean value) {
        getConfig().setResetStatisticsOnThreadCountChange(value);
    }

    public boolean getCancelOnReachedLimit() {
        return getConfig().getCancelOnReachedLimit();
    }

    public void setCancelOnReachedLimit(boolean value) {
        getConfig().setCancelOnReachedLimit(value);
    }

    public boolean getCancelExcessiveThreads() {
        return getConfig().getCancelExcessiveThreads();
    }

    public void setCancelExcessiveThreads(boolean value) {
        getConfig().setCancelExcessiveThreads(value);
    }

    public boolean getLogStatisticsOnThreadChange() {
        return getConfig().getLogStatisticsOnThreadChange();
    }

    public void setLogStatisticsOnThreadChange(boolean value) {
        getConfig().setLogStatisticsOnThreadChange(value);
    }

    public String getStatisticsLogFolder() {
        return getConfig().getStatisticsLogFolder();
    }

    public void setStatisticsLogFolder(String value) {
        getConfig().setStatisticsLogFolder(value);
    }

    public boolean getCalculateTPSOnTimePassed() {
        return getConfig().getCalculateTPSOnTimePassed();
    }

    public void setCalculateTPSOnTimePassed(boolean value) {
        getConfig().setCalculateTPSOnTimePassed(value);
    }

    public int getStartDelay() {
        return getConfig().getStartDelay();
    }

    public void setStartDelay(int startDelay) {
        if (startDelay < 0) {
            return;
        }

        int oldDelay = getStartDelay();
        getConfig().setStartDelay(startDelay);
        notifyPropertyChanged(STARTDELAY_PROPERTY, oldDelay, startDelay);
    }

    public long getHistoryLimit() {
        return getConfig().getHistoryLimit();
    }

    public void setHistoryLimit(long historyLimit) {
        long oldLimit = getHistoryLimit();
        getConfig().setHistoryLimit(historyLimit);
        if (historyLimit == 0)

        {
            notifyPropertyChanged(HISTORYLIMIT_PROPERTY, oldLimit, historyLimit);
        }
    }

    public long getTestLimit() {
        return getConfig().getTestLimit();
    }

    public void setTestLimit(long testLimit) {
        if (testLimit < 0) {
            return;
        }

        long oldLimit = getTestLimit();
        getConfig().setTestLimit(testLimit);
        notifyPropertyChanged(TESTLIMIT_PROPERTY, oldLimit, testLimit);
    }

    public long getMaxAssertionErrors() {
        return getConfig().getMaxAssertionErrors();
    }

    public void setMaxAssertionErrors(long testLimit) {
        if (testLimit < 0) {
            return;
        }

        long oldLimit = getMaxAssertionErrors();
        getConfig().setMaxAssertionErrors(testLimit);
        notifyPropertyChanged(MAXASSERTIONERRORS_PROPERTY, oldLimit, testLimit);
    }

    public long getStatisticsLogInterval() {
        return getConfig().getStatisticsLogInterval();
    }

    public void setStatisticsLogInterval(int sampleInterval) {
        if (sampleInterval < 0) {
            return;
        }

        long oldInterval = getStatisticsLogInterval();
        getConfig().setStatisticsLogInterval(sampleInterval);

        notifyPropertyChanged(SAMPLEINTERVAL_PROPERRY, oldInterval, sampleInterval);

        if (oldInterval == 0 && sampleInterval > 0 && isRunning()) {
            statisticsLogger.start();
        }
    }

    public long getSampleInterval() {
        return getConfig().getSampleInterval();
    }

    public void setSampleInterval(int sampleInterval) {
        if (sampleInterval < 0) {
            return;
        }

        long oldInterval = getSampleInterval();
        getConfig().setSampleInterval(sampleInterval);

        statisticsModel.setUpdateFrequency(sampleInterval);
        notifyPropertyChanged(SAMPLEINTERVAL_PROPERRY, oldInterval, sampleInterval);
    }

    public Enum getLimitType() {
        return getConfig().getLimitType();
    }

    public void setLimitType(Enum limitType) {
        if (limitType == null) {
            return;
        }

        Enum oldType = getLimitType();
        getConfig().setLimitType(limitType);
        notifyPropertyChanged(LIMITTYPE_PROPERRY, oldType, limitType);
    }

    public WsdlTestCase getTestCase() {
        return testCase;
    }

    public synchronized WsdlLoadTestRunner run() {
        getStatisticsModel().reset();
        if (runner != null && runner.getStatus() == Status.RUNNING) {
            return null;
        }

        if (runner != null) {
            runner.release();
        }

        assertionErrors.clear();
        runner = new WsdlLoadTestRunner(this);
        runner.start();
        return runner;
    }

    private class InternalTestRunListener extends LoadTestRunListenerAdapter {
        @Override
        public void afterLoadTest(LoadTestRunner loadTestRunner, LoadTestRunContext context) {
            statisticsLogger.finish();
        }

        @Override
        public void beforeLoadTest(LoadTestRunner loadTestRunner, LoadTestRunContext context) {
            statisticsLogger.init(context);

            if (getStatisticsLogInterval() > 0) {
                statisticsLogger.start();
            }
        }

        @Override
        public void afterTestCase(LoadTestRunner loadTestRunner, LoadTestRunContext context, TestCaseRunner testRunner,
                                  TestCaseRunContext runContext) {
            if (!assertions.isEmpty()) {
                for (LoadTestAssertion assertion : assertions) {
                    String error = assertion.assertResults(loadTestRunner, context, testRunner, runContext);
                    if (error != null) {
                        int threadIndex = 0;

                        try {
                            threadIndex = Integer.parseInt(runContext.getProperty("ThreadIndex").toString());
                        } catch (Throwable t) {
                        }

                        loadTestLog.addEntry(new LoadTestLogErrorEntry(assertion.getName(), error, assertion.getIcon(),
                                threadIndex));
                        statisticsModel.addError(LoadTestStatistics.TOTAL);
                    }
                }
            }
        }

        @Override
        public void afterTestStep(LoadTestRunner loadTestRunner, LoadTestRunContext context, TestCaseRunner testRunner,
                                  TestCaseRunContext runContext, TestStepResult result) {
            boolean added = false;

            if (!assertions.isEmpty()) {
                for (LoadTestAssertion assertion : assertions) {
                    String error = assertion.assertResult(loadTestRunner, context, result, testRunner, runContext);
                    if (error != null) {
                        int indexOfTestStep = testRunner.getTestCase().getIndexOfTestStep(result.getTestStep());
                        int threadIndex = 0;

                        try {
                            threadIndex = Integer.parseInt(runContext.getProperty("ThreadIndex").toString());
                        } catch (Throwable t) {
                        }

                        LoadTestLogErrorEntry errorEntry = new LoadTestLogErrorEntry(assertion.getName(), error, result,
                                assertion.getIcon(), threadIndex);

                        loadTestLog.addEntry(errorEntry);
                        statisticsModel.addError(indexOfTestStep);

                        long maxAssertionErrors = getMaxAssertionErrors();
                        if (maxAssertionErrors > 0) {
                            synchronized (assertionErrors) {
                                assertionErrors.add(errorEntry);
                                while (assertionErrors.size() > maxAssertionErrors) {
                                    assertionErrors.remove(0).discard();
                                }
                            }
                        }

                        added = true;
                    }
                }
            }

            // discard if set to discard and there were no errors
            if (!added) {
                if (getTestCase().getDiscardOkResults() || getTestCase().getMaxResults() == 0) {
                    result.discard();
                } else if (getTestCase().getMaxResults() > 0 && testRunner instanceof WsdlTestCaseRunner) {
                    ((WsdlTestCaseRunner) testRunner).enforceMaxResults(getTestCase().getMaxResults());
                }
            }
        }
    }

    public LoadStrategy getLoadStrategy() {
        return loadStrategy;
    }

    public void setLoadStrategy(LoadStrategy loadStrategy) {
        this.loadStrategy.removeConfigurationChangeListener(loadStrategyListener);
        removeLoadTestRunListener(this.loadStrategy);

        this.loadStrategy = loadStrategy;
        this.loadStrategy.addConfigurationChangeListener(loadStrategyListener);
        addLoadTestRunListener(this.loadStrategy);

        getConfig().getLoadStrategy().setType(loadStrategy.getType());
        getConfig().getLoadStrategy().setConfig(loadStrategy.getConfig());
    }

    private class LoadStrategyConfigurationChangeListener implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
            getConfig().getLoadStrategy().setConfig(loadStrategy.getConfig());
        }
    }

    public LoadTestAssertion addAssertion(String type, String targetStep, boolean showConfig) {
        LoadTestAssertion assertion = LoadTestAssertionRegistry.createAssertion(type, this);
        assertion.setTargetStep(targetStep);

        if (assertion instanceof Configurable && showConfig) {
            if (!((Configurable) assertion).configure()) {
                return null;
            }
        }

        assertions.add(assertion);

        getConfig().addNewAssertion().set(assertion.getConfiguration());
        assertion.addPropertyChangeListener(LoadTestAssertion.CONFIGURATION_PROPERTY, configurationChangeListener);
        fireAssertionAdded(assertion);

        return assertion;
    }

    public void removeAssertion(LoadTestAssertion assertion) {
        int ix = assertions.indexOf(assertion);
        if (ix >= 0) {
            try {
                assertions.remove(ix);
                fireAssertionRemoved(assertion);
            } finally {
                assertion.removePropertyChangeListener(configurationChangeListener);
                assertion.release();
                getConfig().removeAssertion(ix);
            }
        }
    }

    private void fireAssertionRemoved(LoadTestAssertion assertion) {
        if (!loadTestListeners.isEmpty()) {
            LoadTestListener[] l = loadTestListeners.toArray(new LoadTestListener[loadTestListeners.size()]);
            for (LoadTestListener listener : l) {
                listener.assertionRemoved(assertion);
            }
        }
    }

    private void fireAssertionAdded(LoadTestAssertion assertion) {
        if (!loadTestListeners.isEmpty()) {
            LoadTestListener[] l = loadTestListeners.toArray(new LoadTestListener[loadTestListeners.size()]);
            for (LoadTestListener listener : l) {
                listener.assertionAdded(assertion);
            }
        }
    }

    public int getAssertionCount() {
        return assertions.size();
    }

    public LoadTestAssertion getAssertionAt(int index) {
        return index < 0 || index >= assertions.size() ? null : assertions.get(index);
    }

    public LoadTestAssertion getAssertionByName(String name) {
        for (LoadTestAssertion assertion : assertions) {
            if (assertion.getName().equals(name)) {
                return assertion;
            }
        }
        return null;
    }

    private class ConfigurationChangePropertyListener implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
            int ix = assertions.indexOf(evt.getSource());
            if (ix >= 0) {
                getConfig().getAssertionArray(ix).set(assertions.get(ix).getConfiguration());
            }
        }
    }

    public LoadTestLog getLoadTestLog() {
        return loadTestLog;
    }

    public List<LoadTestAssertion> getAssertionList() {
        return assertions;
    }

    public void addLoadTestListener(LoadTestListener listener) {
        loadTestListeners.add(listener);
    }

    public void removeLoadTestListener(LoadTestListener listener) {
        loadTestListeners.remove(listener);
    }

    public void addLoadTestRunListener(LoadTestRunListener listener) {
        loadTestRunListeners.add(listener);
        loadTestRunListenersArray = null;
    }

    public void removeLoadTestRunListener(LoadTestRunListener listener) {
        loadTestRunListeners.remove(listener);
        loadTestRunListenersArray = null;
    }

    public LoadTestRunListener[] getLoadTestRunListeners() {
        if (loadTestRunListenersArray == null) {
            loadTestRunListenersArray = loadTestRunListeners
                    .toArray(new LoadTestRunListener[loadTestRunListeners.size()]);
        }

        return loadTestRunListenersArray;
    }

    /**
     * Release internal objects so they can remove listeners
     */

    @Override
    public void release() {
        super.release();

        statisticsModel.release();
        loadTestLog.release();

        for (LoadTestAssertion assertion : assertions) {
            assertion.release();
        }

        loadTestRunListeners.clear();
        loadTestListeners.clear();
    }

    public boolean isRunning() {
        return runner != null && runner.getStatus() == LoadTestRunner.Status.RUNNING;
    }

    public WsdlLoadTestRunner getRunner() {
        return runner;
    }

    public void resetConfigOnMove(LoadTestConfig config) {
        setConfig(config);

        loadStrategy.updateConfig(config.getLoadStrategy().getConfig());

        List<LoadTestAssertionConfig> assertionList = config.getAssertionList();
        for (int c = 0; c < assertionList.size(); c++) {
            assertions.get(c).updateConfiguration(assertionList.get(c));
        }
    }

    public class StatisticsLogger implements Runnable {
        private boolean stopped;
        private List<PrintWriter> writers = new ArrayList<PrintWriter>();
        private long startTime;

        public void run() {
            stopped = false;

            while (!stopped && getStatisticsLogInterval() > 0) {
                try {
                    long statisticsInterval = getStatisticsLogInterval();
                    Thread.sleep(statisticsInterval);
                    if (!stopped) {
                        logStatistics("Interval");
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        public void start() {
            new Thread(this, "Statistics Logger for LoadTest [" + getName() + "]").start();
        }

        public void init(LoadTestRunContext context) {
            writers.clear();

            String statisticsLogFolder = context.expand(getStatisticsLogFolder());
            if (StringUtils.isNullOrEmpty(statisticsLogFolder)) {
                return;
            }

            File folder = new File(statisticsLogFolder);
            if (!folder.exists()) {
                if (!folder.mkdirs()) {
                    SoapUI.logError(new Exception("Failed to create statistics log folder [" + statisticsLogFolder + "]"));
                    return;
                }
            }

            for (int c = 0; c < testCase.getTestStepCount(); c++) {
                try {
                    WsdlTestStep testStep = testCase.getTestStepAt(c);
                    String fileName = StringUtils.createFileName(testStep.getName(), '_') + ".log";
                    PrintWriter writer = new PrintWriter(new File(folder, fileName));
                    writers.add(writer);
                    addHeaders(writer);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    writers.add(null);
                }
            }

            // and one writer for the testcase..
            try {
                String fileName = StringUtils.createFileName(testCase.getName(), '_') + ".log";
                writers.add(new PrintWriter(new File(folder, fileName)));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            startTime = System.nanoTime();
        }

        private void addHeaders(PrintWriter writer) {
            writer.print("date,threads,elapsed,min,max,avg,last,cnt,tps,bytes,bps,err,reason\n");
        }

        public void finish() {
            stopped = true;

            logStatistics("Finished");
            for (PrintWriter writer : writers) {
                if (writer != null) {
                    writer.close();
                }
            }
        }

        private synchronized void logStatistics(String trigger) {
            if (writers.isEmpty()) {
                return;
            }

            long timestamp = System.nanoTime();
            String elapsedString = String.valueOf((timestamp - startTime) / 1000000);
            String dateString = new Date().toString();
            String threadCountString = String.valueOf(getThreadCount());

            StringList[] snapshot = statisticsModel.getSnapshot();
            for (int c = 0; c < snapshot.length; c++) {
                PrintWriter writer = writers.get(c);
                if (writer == null) {
                    continue;
                }

                StringList values = snapshot[c];
                writer.append(dateString).append(',');
                writer.append(threadCountString).append(',');
                writer.append(elapsedString);

                for (String value : values) {
                    writer.append(',').append(value);
                }

                writer.append(',').append(trigger).append('\n');
                writer.flush();
            }
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

    public Object runSetupScript(LoadTestRunContext runContext, LoadTestRunner runner) throws Exception {
        String script = getSetupScript();
        if (StringUtils.isNullOrEmpty(script)) {
            return null;
        }

        if (setupScriptEngine == null) {
            setupScriptEngine = SoapUIScriptEngineRegistry.create(this);
            setupScriptEngine.setScript(script);
        }

        setupScriptEngine.setVariable("context", runContext);
        setupScriptEngine.setVariable("loadTestRunner", runner);
        setupScriptEngine.setVariable("log", SoapUI.ensureGroovyLog());
        return setupScriptEngine.run();
    }

    public Object runTearDownScript(LoadTestRunContext runContext, LoadTestRunner runner) throws Exception {
        String script = getTearDownScript();
        if (StringUtils.isNullOrEmpty(script)) {
            return null;
        }

        if (tearDownScriptEngine == null) {
            tearDownScriptEngine = SoapUIScriptEngineRegistry.create(this);
            tearDownScriptEngine.setScript(script);
        }

        tearDownScriptEngine.setVariable("context", runContext);
        tearDownScriptEngine.setVariable("loadTestRunner", runner);
        tearDownScriptEngine.setVariable("log", SoapUI.ensureGroovyLog());
        return tearDownScriptEngine.run();
    }

    public int getStrategyInterval() {
        return getConfig().getStrategyInterval();
    }

    public void setStrategyInterval(int interval) {
        getConfig().setStrategyInterval(interval);
    }

    public boolean getUpdateStatisticsPerTestStep() {
        return getConfig().getUpdateStatisticsPerTestStep();
    }

    public void setUpdateStatisticsPerTestStep(boolean updateStatisticsPerTestStep) {
        getConfig().setUpdateStatisticsPerTestStep(updateStatisticsPerTestStep);
    }

    public TestRunner run(StringToObjectMap context, boolean async) {
        // TODO Auto-generated method stub
        return null;
    }
}
