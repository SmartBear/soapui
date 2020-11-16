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
import com.eviware.soapui.impl.wsdl.loadtest.WsdlLoadTest;
import com.eviware.soapui.impl.wsdl.loadtest.data.LoadTestStatistics.Statistic;
import com.eviware.soapui.model.support.LoadTestRunListenerAdapter;
import com.eviware.soapui.model.testsuite.LoadTestRunContext;
import com.eviware.soapui.model.testsuite.LoadTestRunner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Collector of statistics to be exposed as TableModels
 *
 * @author Ole.Matzura
 */

public class StatisticsHistory {
    private final LoadTestStatistics statistics;
    private List<long[][]> data = new ArrayList<long[][]>();
    private List<Long> threadCounts = new ArrayList<Long>();
    private Map<Integer, TestStepStatisticsHistory> testStepStatisticHistories = new HashMap<Integer, TestStepStatisticsHistory>();
    private EnumMap<Statistic, StatisticsValueHistory> statisticsValueHistories = new EnumMap<Statistic, StatisticsValueHistory>(
            Statistic.class);

    @SuppressWarnings("unused")
    private final static Logger logger = LogManager.getLogger(StatisticsHistory.class);
    private long resolution = 0;
    private InternalTableModelListener internalTableModelListener = new InternalTableModelListener();
    private Updater updater = new Updater();

    public StatisticsHistory(LoadTestStatistics statistics) {
        this.statistics = statistics;

        statistics.addTableModelListener(internalTableModelListener);
        statistics.getLoadTest().addLoadTestRunListener(new LoadTestRunListenerAdapter() {

            public void beforeLoadTest(LoadTestRunner loadTestRunner, LoadTestRunContext context) {
                if (resolution > 0) {
                    new Thread(updater, StatisticsHistory.this.statistics.getLoadTest().getName()
                            + " StatisticsHistory Updater").start();
                }
            }
        });
    }

    public Map<Integer, TestStepStatisticsHistory> getTestStepStatisticHistories() {
        return testStepStatisticHistories;
    }

    public long getResolution() {
        return resolution;
    }

    public void setResolution(long resolution) {
        long old = this.resolution;
        this.resolution = resolution;

        if (resolution > 0 && old == 0 && statistics.getLoadTest().getHistoryLimit() != 0) {
            new Thread(updater, statistics.getLoadTest().getName() + " StatisticsHistory Updater").start();
        }
    }

    public int getRowCount() {
        return data.size();
    }

    public long[][] getHistoryAt(int index) {
        return data.get(index);
    }

    public long getThreadCountAt(int index) {
        return threadCounts.get(index);
    }

    public StatisticsHistoryModel getTestStepHistory(int testStepIndex) {
        if (!testStepStatisticHistories.containsKey(testStepIndex)) {
            testStepStatisticHistories.put(testStepIndex, new TestStepStatisticsHistory(testStepIndex));
        }

        return testStepStatisticHistories.get(testStepIndex);
    }

    public StatisticsHistoryModel getStatisticsValueHistory(Statistic statistic) {
        if (!statisticsValueHistories.containsKey(statistic)) {
            statisticsValueHistories.put(statistic, new StatisticsValueHistory(statistic));
        }

        return statisticsValueHistories.get(statistic);
    }

    public void reset() {
        data.clear();
        threadCounts.clear();

        for (StatisticsValueHistory history : statisticsValueHistories.values()) {
            history.fireTableDataChanged();
            history.fireTableStructureChanged();
        }

        for (TestStepStatisticsHistory history : testStepStatisticHistories.values()) {
            history.fireTableDataChanged();
            history.fireTableStructureChanged();
        }
    }

    private synchronized void updateHistory() {
        if (statistics.getStatistic(LoadTestStatistics.TOTAL, Statistic.COUNT) == 0) {
            reset();
        } else {
            int columnCount = statistics.getColumnCount();
            int rowCount = statistics.getRowCount();

            long[][] values = new long[rowCount][columnCount - 2];

            for (int c = 0; c < rowCount; c++) {
                for (int i = 2; i < columnCount; i++) {
                    try {
                        values[c][i - 2] = Long.parseLong(statistics.getValueAt(c, i).toString());
                    } catch (NumberFormatException ex) {
                        values[c][i - 2] = (long) Float.parseFloat(statistics.getValueAt(c, i).toString());
                    }
                }
            }

            data.add(values);
            threadCounts.add(statistics.getLoadTest().getThreadCount());

            // notify!
            int sz = data.size() - 1;
            for (StatisticsValueHistory history : statisticsValueHistories.values()) {
                history.fireTableRowsInserted(sz, sz);
            }

            for (TestStepStatisticsHistory history : testStepStatisticHistories.values()) {
                history.fireTableRowsInserted(sz, sz);
            }
        }
    }

    public abstract class StatisticsHistoryModel extends AbstractTableModel {
        public abstract void release();
    }

    public class TestStepStatisticsHistory extends StatisticsHistoryModel {
        private final int testStepIndex;

        public TestStepStatisticsHistory(int testStepIndex) {
            this.testStepIndex = testStepIndex == -1 ? statistics.getRowCount() - 1 : testStepIndex;
        }

        public int getTestStepIndex() {
            return testStepIndex;
        }

        public int getRowCount() {
            return data.size();
        }

        public int getColumnCount() {
            return statistics.getColumnCount() - 1;
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            if (columnIndex == 0) {
                return threadCounts.get(rowIndex);
            }

            // tolerance..
            if (rowIndex < data.size()) {
                return data.get(rowIndex)[testStepIndex][columnIndex - 1];
            } else {
                return new Long(0);
            }
        }

        public Class<?> getColumnClass(int columnIndex) {
            return Long.class;
        }

        public String getColumnName(int column) {
            return column == 0 ? "ThreadCount" : Statistic.forIndex(column - 1).getName();
        }

        public void release() {
            testStepStatisticHistories.remove(testStepIndex);
        }
    }

    private class StatisticsValueHistory extends StatisticsHistoryModel {
        private final Statistic statistic;

        public StatisticsValueHistory(Statistic statistic) {
            this.statistic = statistic;
        }

        @SuppressWarnings("unused")
        public Statistic getStatistic() {
            return statistic;
        }

        public int getRowCount() {
            return data.size();
        }

        public int getColumnCount() {
            return statistics.getRowCount() + 1;
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            if (columnIndex == 0) {
                return threadCounts.get(rowIndex);
            }

            return data.get(rowIndex)[columnIndex - 1][statistic.getIndex()];
        }

        public Class<?> getColumnClass(int columnIndex) {
            return Long.class;
        }

        public String getColumnName(int column) {
            if (column == 0) {
                return "ThreadCount";
            }

            if (column == statistics.getRowCount()) {
                return "Total";
            }

            return statistics.getLoadTest().getTestCase().getTestStepAt(column - 1).getName();
        }

        public void release() {
            statisticsValueHistories.remove(statistic);
        }
    }

    private class InternalTableModelListener implements TableModelListener {
        public synchronized void tableChanged(TableModelEvent e) {
            if ((resolution > 0 && statistics.getLoadTest().isRunning()) || e.getType() != TableModelEvent.UPDATE
                    || statistics.getLoadTest().getHistoryLimit() == 0) {
                return;
            }

            updateHistory();
        }
    }

    private final class Updater implements Runnable {
        public void run() {
            WsdlLoadTest loadTest = statistics.getLoadTest();

            while (resolution > 0 && loadTest.isRunning()) {
                try {
                    if (loadTest.getHistoryLimit() != 0) {
                        updateHistory();
                    }

                    // chunck wait so we can get canceled..
                    long res = resolution;
                    while (res > 100 && resolution > 0 && loadTest.isRunning()) {
                        Thread.sleep(res);
                        res -= 100;
                    }

                    if (resolution > 0 && loadTest.isRunning()) {
                        Thread.sleep(res);
                    }
                } catch (InterruptedException e) {
                    SoapUI.logError(e);
                    break;
                }
            }
        }
    }
}
