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

package com.eviware.soapui.impl.wsdl.loadtest.log;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.loadtest.WsdlLoadTest;
import com.eviware.soapui.model.support.TestSuiteListenerAdapter;
import com.eviware.soapui.model.testsuite.TestStep;

import javax.swing.AbstractListModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Log for LoadTest events
 *
 * @author Ole.Matzura
 */

public class LoadTestLog extends AbstractListModel implements Runnable {
    private List<LoadTestLogEntry> entries = Collections.synchronizedList(new ArrayList<LoadTestLogEntry>());
    private final WsdlLoadTest loadTest;
    private int totalErrorCount;
    private Map<String, Integer> errorCounts = new HashMap<String, Integer>();
    private Queue<LoadTestLogEntry> entriesStack = new ConcurrentLinkedQueue<LoadTestLogEntry>();
    private Thread modelThread;
    private InternalTestSuiteListener testSuiteListener = new InternalTestSuiteListener();

    public LoadTestLog(WsdlLoadTest loadTest) {
        this.loadTest = loadTest;
        loadTest.getTestCase().getTestSuite().addTestSuiteListener(testSuiteListener);
    }

    public void release() {
        loadTest.getTestCase().getTestSuite().removeTestSuiteListener(testSuiteListener);
    }

    public int getSize() {
        return entries.size();
    }

    public Object getElementAt(int index) {
        return entries.get(index);
    }

    public synchronized void addEntry(LoadTestLogEntry entry) {
        entriesStack.add(entry);

        if (modelThread == null) {
            modelThread = new Thread(this, loadTest.getName() + " LoadTestLog Updater");
            modelThread.start();
        }
    }

    public void run() {
        // always run at least once
        while (true) {
            try {
                while (!entriesStack.isEmpty()) {
                    int cnt = 0;
                    while (cnt < 10 && !entriesStack.isEmpty()) {
                        LoadTestLogEntry entry = entriesStack.poll();
                        if (entry != null) {
                            entries.add(entry);
                            if (entry.isError()) {
                                totalErrorCount++;
                                String stepName = entry.getTargetStepName();

                                Integer errorCount = errorCounts.get(stepName);
                                if (errorCount == null) {
                                    errorCount = 1;
                                } else {
                                    errorCount = errorCount + 1;
                                }

                                errorCounts.put(stepName, errorCount);
                            }

                            cnt++;
                        }
                    }

                    if (cnt > 0) {
                        fireIntervalAdded(this, entries.size() - cnt, entries.size() - 1);
                    }
                }

                // break if load test is not running
                if (!loadTest.isRunning()) {
                    break;
                }

                Thread.sleep(200);
            } catch (Exception e) {
                SoapUI.logError(e);
            }
        }

        modelThread = null;
    }

    public void clear() {
        entriesStack.clear();

        if (!entries.isEmpty()) {
            int size = entries.size();
            entries.clear();
            fireIntervalRemoved(this, 0, size - 1);
            totalErrorCount = 0;
            errorCounts.clear();
        }
    }

    public void clearErrors() {
        int sz = entries.size();

        for (int c = 0; c < entries.size(); c++) {
            if (entries.get(c).isError()) {
                entries.remove(c);
                c--;
            }
        }

        totalErrorCount = 0;
        errorCounts.clear();

        if (sz > entries.size()) {
            fireIntervalRemoved(this, entries.size(), sz);
            fireContentsChanged(this, 0, entries.size());
        }
    }

    public void clearEntries(TestStep testStep) {
        int sz = entries.size();

        String testStepName = testStep.getName();
        for (int c = 0; c < entries.size(); c++) {
            if (testStepName.equals(entries.get(c).getTargetStepName())) {
                entries.remove(c);
                c--;
            }
        }

        if (errorCounts.containsKey(testStepName)) {
            totalErrorCount -= errorCounts.get(testStepName).intValue();
            errorCounts.remove(testStepName);
        }

        if (sz > entries.size()) {
            fireIntervalRemoved(this, entries.size(), sz);
            fireContentsChanged(this, 0, entries.size());
        }
    }

    public WsdlLoadTest getLoadTest() {
        return loadTest;
    }

    public int getErrorCount(String stepName) {
        if (stepName == null) {
            return totalErrorCount;
        }

        Integer counts = errorCounts.get(stepName);
        return counts == null ? 0 : counts;
    }

    private final class InternalTestSuiteListener extends TestSuiteListenerAdapter {
        public void testStepRemoved(TestStep testStep, int index) {
            if (testStep.getTestCase() == loadTest.getTestCase()) {
                clearEntries(testStep);
            }
        }
    }

    public List<LoadTestLogEntry> getEntries() {
        return entries;
    }
}
