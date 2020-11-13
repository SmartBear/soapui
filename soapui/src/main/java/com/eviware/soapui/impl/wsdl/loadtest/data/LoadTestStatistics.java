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

package com.eviware.soapui.impl.wsdl.loadtest.data;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.loadtest.ColorPalette;
import com.eviware.soapui.impl.wsdl.loadtest.WsdlLoadTest;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.model.support.LoadTestRunListenerAdapter;
import com.eviware.soapui.model.support.TestSuiteListenerAdapter;
import com.eviware.soapui.model.testsuite.LoadTestRunContext;
import com.eviware.soapui.model.testsuite.LoadTestRunner;
import com.eviware.soapui.model.testsuite.TestCase;
import com.eviware.soapui.model.testsuite.TestCaseRunContext;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.TestRunner;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.TestStepResult;
import com.eviware.soapui.support.types.StringList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.table.AbstractTableModel;
import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Model holding statistics.. should be refactored into interface for different
 * statistic models
 *
 * @author Ole.Matzura
 */

public final class LoadTestStatistics extends AbstractTableModel implements Runnable {
    public final static String NO_STATS_TESTCASE_CANCEL_REASON = "NO_STATS_TESTCASE_CANCEL_REASON";
    private final static Logger log = LogManager.getLogger(LoadTestStatistics.class);

    private final WsdlLoadTest loadTest;
    private long[][] data;

    private final static int MIN_COLUMN = 0;
    private final static int MAX_COLUMN = 1;
    private final static int AVG_COLUMN = 2;
    private final static int LAST_COLUMN = 3;
    private final static int CNT_COLUMN = 4;
    private final static int TPS_COLUMN = 5;
    private final static int BYTES_COLUMN = 6;
    private final static int BPS_COLUMN = 7;
    private final static int ERR_COLUMN = 8;
    private final static int SUM_COLUMN = 9;
    private final static int CURRENT_CNT_COLUMN = 10;
    private final static int RATIO_COLUMN = 11;

    public static final int TOTAL = -1;

    public static final int DEFAULT_SAMPLE_INTERVAL = 250;

    private InternalTestRunListener testRunListener;
    private InternalTestSuiteListener testSuiteListener;
    private InternalPropertyChangeListener propertyChangeListener;

    private StatisticsHistory history;

    private boolean changed;
    private long updateFrequency = DEFAULT_SAMPLE_INTERVAL;
    private Queue<SamplesHolder> samplesStack = new ConcurrentLinkedQueue<SamplesHolder>();
    private long currentThreadCountStartTime;
    private long totalAverageSum;
    private boolean resetStatistics;
    private boolean running;
    private boolean adding;

    public LoadTestStatistics(WsdlLoadTest loadTest) {
        this.loadTest = loadTest;

        testRunListener = new InternalTestRunListener();
        testSuiteListener = new InternalTestSuiteListener();
        propertyChangeListener = new InternalPropertyChangeListener();

        WsdlTestCase testCase = loadTest.getTestCase();
        loadTest.addPropertyChangeListener(propertyChangeListener);
        loadTest.addLoadTestRunListener(testRunListener);
        testCase.getTestSuite().addTestSuiteListener(testSuiteListener);

        for (TestStep testStep : testCase.getTestStepList()) {
            testStep.addPropertyChangeListener(propertyChangeListener);
        }

        history = new StatisticsHistory(this);

        init();
    }

    private void init() {
        data = new long[getRowCount()][11];
    }

    public StatisticsHistory getHistory() {
        return history;
    }

    public int getRowCount() {
        return loadTest.getTestCase().getTestStepCount() + 1;
    }

    public WsdlLoadTest getLoadTest() {
        return loadTest;
    }

    public int getColumnCount() {
        return 12;
    }

    public String getColumnName(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return " ";
            case 1:
                return "Test Step";
            case 2:
                return Statistic.MININMUM.getName();
            case 3:
                return Statistic.MAXIMUM.getName();
            case 4:
                return Statistic.AVERAGE.getName();
            case 5:
                return Statistic.LAST.getName();
            case 6:
                return Statistic.COUNT.getName();
            case 7:
                return Statistic.TPS.getName();
            case 8:
                return Statistic.BYTES.getName();
            case 9:
                return Statistic.BPS.getName();
            case 10:
                return Statistic.ERRORS.getName();
            case 11:
                return Statistic.ERRORRATIO.getName();
        }
        return null;
    }

    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return Color.class;
            case 1:
                return String.class;
            case 4:
            case 7:
                return Float.class;
            default:
                return Long.class;
        }
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    public long getStatistic(int stepIndex, Statistic statistic) {
        if (stepIndex == TOTAL) {
            stepIndex = data.length - 1;
        }

        switch (statistic) {
            case TPS:
            case AVERAGE:
                return data[stepIndex][statistic.getIndex()] / 100;
            case ERRORRATIO:
                return data[stepIndex][Statistic.COUNT.getIndex()] == 0 ? 0
                        : (long) ((((float) data[stepIndex][Statistic.ERRORS.getIndex()] / (float) data[stepIndex][Statistic.COUNT
                        .getIndex()]) + 0.5) * 100);
            default:
                return data[stepIndex][statistic.getIndex()];
        }
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        WsdlTestCase testCase = loadTest.getTestCase();

        switch (columnIndex) {
            case 0:
                return rowIndex == testCase.getTestStepCount() ? null : ColorPalette.getColor(testCase
                        .getTestStepAt(rowIndex));
            case 1: {
                if (rowIndex == testCase.getTestStepCount()) {
                    return "TestCase:";
                } else {
                    return testCase.getTestStepAt(rowIndex).getLabel();
                }
            }
            case 4:
            case 7:
                return new Float((float) data[rowIndex][columnIndex - 2] / 100);
            case 11:
                return data[rowIndex][Statistic.COUNT.getIndex()] == 0 ? 0
                        : (long) (((float) data[rowIndex][Statistic.ERRORS.getIndex()] / (float) data[rowIndex][Statistic.COUNT
                        .getIndex()]) * 100);
            default: {
                return data == null || rowIndex >= data.length ? new Long(0) : new Long(data[rowIndex][columnIndex - 2]);
            }
        }
    }

    public void pushSamples(long[] samples, long[] sizes, long[] sampleCounts, long startTime, long timeTaken,
                            boolean complete) {
        if (!running || samples.length == 0 || sizes.length == 0) {
            return;
        }

        samplesStack.add(new SamplesHolder(samples, sizes, sampleCounts, startTime, timeTaken, complete));
    }

    public void run() {
        Thread.currentThread().setName(loadTest.getName() + " LoadTestStatistics");

        while (running || !samplesStack.isEmpty()) {
            try {
                while (!samplesStack.isEmpty()) {
                    SamplesHolder holder = samplesStack.poll();
                    if (holder != null) {
                        addSamples(holder);
                    }
                }

                Thread.sleep(200);
            } catch (EmptyStackException e) {
            } catch (Exception e) {
                SoapUI.logError(e);
            }
        }
    }

    private synchronized void addSamples(SamplesHolder holder) {
        if (adding) {
            throw new RuntimeException("Already adding!");
        }

        adding = true;

        int totalIndex = data.length - 1;
        if (holder.samples.length != totalIndex || holder.sizes.length != totalIndex) {
            adding = false;
            throw new RuntimeException("Unexpected number of samples: " + holder.samples.length + ", exptected "
                    + (totalIndex));
        }

        // discard "old" results
        if (holder.startTime < currentThreadCountStartTime) {
            adding = false;
            return;
        }

        // first check that this is not a
        long timePassed = (holder.startTime + holder.timeTaken) - currentThreadCountStartTime;

        if (resetStatistics) {
            for (int c = 0; c < data.length; c++) {
                data[c][CURRENT_CNT_COLUMN] = 0;
                data[c][AVG_COLUMN] = 0;
                data[c][SUM_COLUMN] = 0;
                data[c][TPS_COLUMN] = 0;
                data[c][BYTES_COLUMN] = 0;
            }

            totalAverageSum = 0;
            resetStatistics = false;
        }

        long totalMin = 0;
        long totalMax = 0;
        long totalBytes = 0;
        long totalAvg = 0;
        long totalSum = 0;
        long totalLast = 0;

        long threadCount = loadTest.getThreadCount();

        for (int c = 0; c < holder.samples.length; c++) {
            if (holder.sampleCounts[c] > 0) {
                // only update when appropriate
                if (holder.complete != loadTest.getUpdateStatisticsPerTestStep()) {
                    long sampleAvg = holder.samples[c] / holder.sampleCounts[c];

                    data[c][LAST_COLUMN] = sampleAvg;
                    data[c][CNT_COLUMN] += holder.sampleCounts[c];
                    data[c][CURRENT_CNT_COLUMN] += holder.sampleCounts[c];
                    data[c][SUM_COLUMN] += holder.samples[c];

                    if (sampleAvg > 0 && (sampleAvg < data[c][MIN_COLUMN] || data[c][MIN_COLUMN] == 0)) {
                        data[c][MIN_COLUMN] = sampleAvg;
                    }

                    if (sampleAvg > data[c][MAX_COLUMN]) {
                        data[c][MAX_COLUMN] = sampleAvg;
                    }

                    float average = (float) data[c][SUM_COLUMN] / (float) data[c][CURRENT_CNT_COLUMN];

                    data[c][AVG_COLUMN] = (long) (average * 100);
                    data[c][BYTES_COLUMN] += holder.sizes[c];

                    if (timePassed > 0) {
                        if (loadTest.getCalculateTPSOnTimePassed()) {
                            data[c][TPS_COLUMN] = (data[c][CURRENT_CNT_COLUMN] * 100000) / timePassed;
                            data[c][BPS_COLUMN] = (data[c][BYTES_COLUMN] * 1000) / timePassed;
                        } else {
                            data[c][TPS_COLUMN] = (long) (data[c][AVG_COLUMN] > 0 ? (100000F / average) * threadCount : 0);

                            long avgBytes = data[c][CNT_COLUMN] == 0 ? 0 : data[c][BYTES_COLUMN] / data[c][CNT_COLUMN];
                            data[c][BPS_COLUMN] = (avgBytes * data[c][TPS_COLUMN]) / 100;
                        }
                    }
                }

                totalMin += data[c][MIN_COLUMN] * holder.sampleCounts[c];
                totalMax += data[c][MAX_COLUMN] * holder.sampleCounts[c];
                totalBytes += data[c][BYTES_COLUMN] * holder.sampleCounts[c];
                totalAvg += data[c][AVG_COLUMN] * holder.sampleCounts[c];
                totalSum += data[c][SUM_COLUMN] * holder.sampleCounts[c];
                totalLast += data[c][LAST_COLUMN] * holder.sampleCounts[c];
            } else {
                totalMin += data[c][MIN_COLUMN];
                totalMax += data[c][MAX_COLUMN];
                totalBytes += data[c][BYTES_COLUMN];
            }
        }

        if (holder.complete) {
            data[totalIndex][CNT_COLUMN]++;
            data[totalIndex][CURRENT_CNT_COLUMN]++;

            totalAverageSum += totalLast * 100;
            data[totalIndex][AVG_COLUMN] = (long) ((float) totalAverageSum / (float) data[totalIndex][CURRENT_CNT_COLUMN]);
            data[totalIndex][BYTES_COLUMN] = totalBytes;

            if (timePassed > 0) {
                if (loadTest.getCalculateTPSOnTimePassed()) {
                    data[totalIndex][TPS_COLUMN] = (data[totalIndex][CURRENT_CNT_COLUMN] * 100000) / timePassed;
                    data[totalIndex][BPS_COLUMN] = (data[totalIndex][BYTES_COLUMN] * 1000) / timePassed;
                } else {
                    data[totalIndex][TPS_COLUMN] = (long) (data[totalIndex][AVG_COLUMN] > 0 ? (10000000F / data[totalIndex][AVG_COLUMN])
                            * threadCount
                            : 0);

                    long avgBytes = data[totalIndex][CNT_COLUMN] == 0 ? 0 : data[totalIndex][BYTES_COLUMN]
                            / data[totalIndex][CNT_COLUMN];

                    data[totalIndex][BPS_COLUMN] = (avgBytes * data[totalIndex][TPS_COLUMN]) / 100;
                }
            }

            data[totalIndex][MIN_COLUMN] = totalMin;
            data[totalIndex][MAX_COLUMN] = totalMax;
            data[totalIndex][SUM_COLUMN] = totalSum;
            data[totalIndex][LAST_COLUMN] = totalLast;
        }

        if (updateFrequency == 0) {
            fireTableDataChanged();
        } else {
            changed = true;
        }

        adding = false;
    }

    private final class Updater implements Runnable {
        public void run() {
            Thread.currentThread().setName(loadTest.getName() + " LoadTestStatistics Updater");

            // check all these for catching threading issues
            while (running || changed || !samplesStack.isEmpty()) {
                if (changed) {
                    fireTableDataChanged();
                    changed = false;
                }

                if (!running && samplesStack.isEmpty()) {
                    break;
                }

                try {
                    Thread.sleep(updateFrequency < 1 ? 1000 : updateFrequency);
                } catch (InterruptedException e) {
                    SoapUI.logError(e);
                }
            }
        }
    }

    private void stop() {
        running = false;
    }

    /**
     * Collect testresult samples
     *
     * @author Ole.Matzura
     */

    private class InternalTestRunListener extends LoadTestRunListenerAdapter {
        public void beforeLoadTest(LoadTestRunner loadTestRunner, LoadTestRunContext context) {
            samplesStack.clear();

            running = true;
            SoapUI.getThreadPool().submit(updater);
            SoapUI.getThreadPool().submit(LoadTestStatistics.this);

            currentThreadCountStartTime = System.currentTimeMillis();
            totalAverageSum = 0;
        }

        @Override
        public void afterTestStep(LoadTestRunner loadTestRunner, LoadTestRunContext context, TestCaseRunner testRunner,
                                  TestCaseRunContext runContext, TestStepResult testStepResult) {
            if (loadTest.getUpdateStatisticsPerTestStep()) {
                TestCase testCase = testRunner.getTestCase();

                if (testStepResult == null) {
                    log.warn("Result is null in TestCase [" + testCase.getName() + "]");
                    return;
                }

                long[] samples = new long[testCase.getTestStepCount()];
                long[] sizes = new long[samples.length];
                long[] sampleCounts = new long[samples.length];

                int index = testCase.getIndexOfTestStep(testStepResult.getTestStep());
                sampleCounts[index]++;

                samples[index] += testStepResult.getTimeTaken();
                sizes[index] += testStepResult.getSize();

                pushSamples(samples, sizes, sampleCounts, testRunner.getStartTime(), testRunner.getTimeTaken(), false);
            }
        }

        public void afterTestCase(LoadTestRunner loadTestRunner, LoadTestRunContext context, TestCaseRunner testRunner,
                                  TestCaseRunContext runContext) {
            if (testRunner.getStatus() == TestRunner.Status.CANCELED
                    && testRunner.getReason().equals(NO_STATS_TESTCASE_CANCEL_REASON)) {
                return;
            }

            List<TestStepResult> results = testRunner.getResults();
            TestCase testCase = testRunner.getTestCase();

            long[] samples = new long[testCase.getTestStepCount()];
            long[] sizes = new long[samples.length];
            long[] sampleCounts = new long[samples.length];

            for (int c = 0; c < results.size(); c++) {
                TestStepResult testStepResult = results.get(c);
                if (testStepResult == null) {
                    log.warn("Result [" + c + "] is null in TestCase [" + testCase.getName() + "]");
                    continue;
                }

                int index = testCase.getIndexOfTestStep(testStepResult.getTestStep());
                if (index >= 0) {
                    sampleCounts[index]++;

                    samples[index] += testStepResult.getTimeTaken();
                    sizes[index] += testStepResult.getSize();
                }
            }

            pushSamples(samples, sizes, sampleCounts, testRunner.getStartTime(), testRunner.getTimeTaken(), true);
        }

        @Override
        public void afterLoadTest(LoadTestRunner loadTestRunner, LoadTestRunContext context) {
            stop();
        }
    }

    public int getStepCount() {
        return loadTest.getTestCase().getTestStepCount();
    }

    public void reset() {
        init();
        fireTableDataChanged();
    }

    public void release() {
        reset();

        loadTest.removeLoadTestRunListener(testRunListener);
        loadTest.getTestCase().getTestSuite().removeTestSuiteListener(testSuiteListener);

        for (TestStep testStep : loadTest.getTestCase().getTestStepList()) {
            testStep.removePropertyChangeListener(propertyChangeListener);
        }
    }

    private class InternalTestSuiteListener extends TestSuiteListenerAdapter {
        public void testStepAdded(TestStep testStep, int index) {
            if (testStep.getTestCase() == loadTest.getTestCase()) {
                init();
                testStep.addPropertyChangeListener(TestStep.NAME_PROPERTY, propertyChangeListener);
                fireTableDataChanged();

                history.reset();
            }
        }

        public void testStepRemoved(TestStep testStep, int index) {
            if (testStep.getTestCase() == loadTest.getTestCase()) {
                init();
                testStep.removePropertyChangeListener(propertyChangeListener);
                fireTableDataChanged();

                history.reset();
            }
        }
    }

    private class InternalPropertyChangeListener implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getSource() == loadTest && evt.getPropertyName().equals(WsdlLoadTest.THREADCOUNT_PROPERTY)) {
                if (loadTest.getResetStatisticsOnThreadCountChange()) {
                    resetStatistics = true;
                    currentThreadCountStartTime = System.currentTimeMillis();
                }
            } else if (evt.getPropertyName().equals(TestStep.NAME_PROPERTY)
                    || evt.getPropertyName().equals(TestStep.DISABLED_PROPERTY)) {
                if (evt.getSource() instanceof TestStep) {
                    fireTableCellUpdated(loadTest.getTestCase().getIndexOfTestStep((TestStep) evt.getSource()), 1);
                }
            } else if (evt.getPropertyName().equals(WsdlLoadTest.HISTORYLIMIT_PROPERTY)) {
                if (loadTest.getHistoryLimit() == 0) {
                    history.reset();
                }
            }
        }
    }

    public TestStep getTestStepAtRow(int selectedRow) {
        if (selectedRow < getRowCount() - 1) {
            return loadTest.getTestCase().getTestStepAt(selectedRow);
        } else {
            return null;
        }
    }

    public long getUpdateFrequency() {
        return updateFrequency;
    }

    public void setUpdateFrequency(long updateFrequency) {
        this.updateFrequency = updateFrequency;
    }

    public void addError(int stepIndex) {
        if (stepIndex != -1) {
            data[stepIndex][ERR_COLUMN]++;
        }

        data[data.length - 1][ERR_COLUMN]++;
        changed = true;
    }

    public synchronized StringList[] getSnapshot() {
        long[][] clone = data.clone();

        StringList[] result = new StringList[getRowCount()];

        for (int c = 0; c < clone.length; c++) {
            StringList values = new StringList();

            for (int columnIndex = 2; columnIndex < getColumnCount(); columnIndex++) {
                switch (columnIndex) {
                    case 4:
                    case 7:
                        values.add(String.valueOf((float) data[c][columnIndex - 2] / 100));
                        break;
                    default:
                        values.add(String.valueOf(data[c][columnIndex - 2]));
                }
            }

            result[c] = values;
        }

        return result;
    }

    private final static Map<Integer, Statistic> statisticIndexMap = new HashMap<Integer, Statistic>();

    private Updater updater = new Updater();

    public enum Statistic {
        MININMUM(MIN_COLUMN, "min", "the minimum measured teststep time"), MAXIMUM(MAX_COLUMN, "max",
                "the maximum measured testste time"), AVERAGE(AVG_COLUMN, "avg", "the average measured teststep time"), LAST(
                LAST_COLUMN, "last", "the last measured teststep time"), COUNT(CNT_COLUMN, "cnt",
                "the number of teststep samples measured"), TPS(TPS_COLUMN, "tps",
                "the number of transactions per second for this teststep"), BYTES(BYTES_COLUMN, "bytes",
                "the total number of bytes returned by this teststep"), BPS(BPS_COLUMN, "bps",
                "the number of bytes per second returned by this teststep"), ERRORS(ERR_COLUMN, "err",
                "the total number of assertion errors for this teststep"), SUM(SUM_COLUMN, "sum", "internal sum"), CURRENT_CNT(
                CURRENT_CNT_COLUMN, "ccnt", "internal cnt"), ERRORRATIO(RATIO_COLUMN, "rat",
                "the ratio between exections and failures");

        private final String description;
        private final String name;
        private final int index;

        Statistic(int index, String name, String description) {
            this.index = index;
            this.name = name;
            this.description = description;

            statisticIndexMap.put(index, this);
        }

        public String getDescription() {
            return description;
        }

        public int getIndex() {
            return index;
        }

        public String getName() {
            return name;
        }

        public static Statistic forIndex(int column) {
            return statisticIndexMap.get(column);
        }
    }

    /**
     * Holds all sample values for a testcase run
     *
     * @author ole.matzura
     */

    private static final class SamplesHolder {
        private final long[] samples;
        private final long[] sizes;
        private final long[] sampleCounts;

        private final long startTime;
        private final long timeTaken;
        private final boolean complete;

        public SamplesHolder(long[] samples, long[] sizes, long[] sampleCounts, long startTime, long timeTaken,
                             boolean complete) {
            this.samples = samples;
            this.sizes = sizes;
            this.startTime = startTime;
            this.timeTaken = timeTaken;
            this.sampleCounts = sampleCounts;
            this.complete = complete;
        }
    }

    public synchronized void finish() {
        // push leftover samples
        while (!samplesStack.isEmpty()) {
            SamplesHolder holder = samplesStack.poll();
            if (holder != null) {
                addSamples(holder);
            }
        }
    }
}
