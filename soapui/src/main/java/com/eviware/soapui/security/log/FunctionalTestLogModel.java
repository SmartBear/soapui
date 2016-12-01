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

package com.eviware.soapui.security.log;

import com.eviware.soapui.model.testsuite.TestStepResult;
import org.apache.commons.collections.list.TreeList;

import javax.swing.AbstractListModel;
import java.lang.ref.SoftReference;
import java.util.Collections;
import java.util.List;

/**
 * SecurityTest - Functional log
 *
 * @author SoapUI team
 */
@SuppressWarnings("serial")
public class FunctionalTestLogModel extends AbstractListModel {
    private List<Object> items = Collections.synchronizedList(new TreeList());
    private List<SoftReference<TestStepResult>> results = Collections.synchronizedList(new TreeList());
    private int maxSize = 100;
    private int stepCount;

    public synchronized Object getElementAt(int arg0) {
        try {
            return items.get(arg0);
        } catch (Throwable e) {
            return null;
        }
    }

    @Override
    public int getSize() {
        return items.size();
    }

    public synchronized void addText(String msg) {
        items.add(msg);
        results.add(null);
        fireIntervalAdded(this, items.size() - 1, items.size() - 1);

        enforceMaxSize();
    }

    public synchronized TestStepResult getTestStepResultAt(int index) {
        if (index >= results.size()) {
            return null;
        }

        SoftReference<TestStepResult> result = results.get(index);
        return result == null ? null : result.get();
    }

    public synchronized void addSecurityTestFunctionalStepResult(TestStepResult result) {
        stepCount++;
        int size = items.size();
        SoftReference<TestStepResult> stepResultRef = new SoftReference<TestStepResult>(result);

        items.add("Step " + stepCount + " [" + result.getTestStep().getName() + "] " + result.getStatus() + ": took "
                + result.getTimeTaken() + " ms");
        results.add(stepResultRef);

        for (String msg : result.getMessages()) {
            items.add(" -> " + msg);
            results.add(stepResultRef);
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

    private synchronized void enforceMaxSize() {
        while (items.size() > maxSize) {
            items.remove(0);
            results.remove(0);
            fireIntervalRemoved(this, 0, 0);
        }
    }

}
