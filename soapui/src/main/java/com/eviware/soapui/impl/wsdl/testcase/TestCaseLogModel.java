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

package com.eviware.soapui.impl.wsdl.testcase;

import com.eviware.soapui.model.testsuite.TestStepResult;
import org.apache.commons.collections.list.TreeList;

import javax.swing.AbstractListModel;
import java.lang.ref.SoftReference;
import java.util.Collections;
import java.util.List;

/**
 * ListModel for the TestCaseLog
 *
 * @author ole.matzura
 */

@SuppressWarnings("unchecked")
public class TestCaseLogModel extends AbstractListModel {
    @SuppressWarnings("unchecked")
    private List<Object> items = Collections.synchronizedList(new TreeList());
    private List<SoftReference<TestStepResult>> results = Collections.synchronizedList(new TreeList());
    private int stepCount;
    private int maxSize = 0;

    public synchronized void addText(String msg) {
        items.add(msg);
        results.add(null);
        fireIntervalAdded(this, items.size() - 1, items.size() - 1);

        enforceMaxSize();
    }

    private synchronized void enforceMaxSize() {
        while (items.size() > maxSize) {
            items.remove(0);
            results.remove(0);
            fireIntervalRemoved(this, 0, 0);
        }
    }

    public synchronized void addTestStepResult(TestStepResult result) {
        stepCount++;

        int size = items.size();
        items.add("Step " + stepCount + " [" + result.getTestStep().getName() + "] " + result.getStatus() + ": took "
                + result.getTimeTaken() + " ms");
        SoftReference<TestStepResult> ref = new SoftReference<TestStepResult>(result);
        results.add(ref);
        for (String msg : result.getMessages()) {
            items.add(" -> " + msg);
            results.add(ref);
        }

        fireIntervalAdded(this, size, items.size() - 1);
        enforceMaxSize();
    }

    public synchronized void clear() {
        int sz = items.size();
        items.clear();
        results.clear();
        stepCount = 0;
        fireIntervalRemoved(this, 0, sz);
    }

    public int getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
        enforceMaxSize();
    }

    public int getSize() {
        return items.size();
    }

    public synchronized Object getElementAt(int arg0) {
        try {
            return items.get(arg0);
        } catch (Throwable e) {
            return null;
        }
    }

    public synchronized TestStepResult getResultAt(int index) {
        if (index >= results.size()) {
            return null;
        }

        SoftReference<TestStepResult> result = results.get(index);
        return result == null ? null : result.get();
    }

    public void setStepIndex(int i) {
        stepCount = i;
    }
}
