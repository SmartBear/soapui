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

import com.eviware.soapui.model.security.SecurityScan;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.security.result.SecurityResult;
import com.eviware.soapui.security.result.SecurityResult.ResultStatus;
import com.eviware.soapui.security.result.SecurityScanRequestResult;
import com.eviware.soapui.security.result.SecurityScanResult;
import com.eviware.soapui.security.result.SecurityTestStepResult;
import com.eviware.soapui.security.scan.AbstractSecurityScan;
import org.apache.commons.collections.list.TreeList;

import javax.swing.AbstractListModel;
import java.lang.ref.SoftReference;
import java.util.Collections;
import java.util.List;

/**
 * SecurityTestLog
 *
 * @author SoapUI team
 */
@SuppressWarnings({"serial", "unchecked"})
public class SecurityTestLogModel extends AbstractListModel {
    private List<Object> items = Collections.synchronizedList(new TreeList());
    private List<SoftReference<SecurityResult>> results = Collections.synchronizedList(new TreeList());
    private int maxSize = 100;
    private int stepCount;
    private int checkCount;
    private int requestCount;
    private int currentCheckEntriesCount;
    private int currentStepEntriesCount;

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

    public synchronized SecurityResult getTestStepResultAt(int index) {
        if (index >= results.size()) {
            return null;
        }

        SoftReference<SecurityResult> result = results.get(index);
        return result == null ? null : result.get();
    }

    /**
     * called before TestStep SecurityScans just to mark beginning of TestStep
     * SecurityLog part to be updated after step scans finish with proper values
     *
     * @return true - if added, false - otherwise
     */
    public synchronized boolean addSecurityTestStepResult(TestStep testStep) {
        stepCount++;
        checkCount = 0;

        currentStepEntriesCount = 1;
        int size = items.size();
        if (AbstractSecurityScan.isSecurable(testStep)) {
            SecurityTestStepResult result = new SecurityTestStepResult(testStep, null);
            SoftReference<SecurityResult> stepResultRef = new SoftReference<SecurityResult>(result);
            items.add("Step " + stepCount + " [" + result.getTestStep().getName() + "] ");
            results.add(stepResultRef);

            fireIntervalAdded(this, size, items.size() - 1);
            enforceMaxSize();
            return true;
        } else {
            return false;
        }
    }

    // called after whole security teststep finished to delete start line in case
    // only errors are beeing displayed
    public synchronized void updateSecurityTestStepResult(SecurityTestStepResult result, boolean errorsOnly,
                                                          boolean hasChecksToProcess, boolean startStepLogEntryAdded) {
        int startStepIndex = 0;
        if (items.size() > currentStepEntriesCount) {
            if (currentStepEntriesCount > 0) {
                startStepIndex = items.size() - currentStepEntriesCount;
            } else {
                startStepIndex = items.size();
            }
        }
        if ((errorsOnly && !result.isHasScansWithWarnings()) || (startStepLogEntryAdded && !hasChecksToProcess)) {
            // stepCount-- ;
            int size = items.size() - 1;
            while (size >= startStepIndex) {
                items.remove(size);
                results.remove(size);
                size--;
            }
            if (startStepIndex > 0 && size > 0) {
                fireIntervalRemoved(this, startStepIndex, size);
            } else {
                fireIntervalRemoved(this, 0, size);
            }
        } else if (startStepLogEntryAdded) {

            try {
                if (startStepIndex > 0 && startStepIndex < maxSize) {
                    String statusToDisplay = getStatusToDisplay(result.getExecutionProgressStatus());
                    items.set(startStepIndex, "Step " + stepCount + " [" + result.getTestStep().getName() + "] "
                            + statusToDisplay + ": took " + result.getTimeTaken() + " ms");
                    SoftReference<SecurityResult> stepResultRef = new SoftReference<SecurityResult>(result);
                    results.set(startStepIndex, stepResultRef);
                    fireContentsChanged(this, startStepIndex, startStepIndex);
                }
            } catch (IndexOutOfBoundsException e) {
                // when log max size is exceeded skip updating the raw since it
                // won't be visible anyway
            }
        }
        currentStepEntriesCount = 0;
    }

    private String getStatusToDisplay(ResultStatus result) {
        String statusToDisplay = "";
        switch (result) {
            case FAILED:
                statusToDisplay = "Alerts";
                break;
            case OK:
                statusToDisplay = "No Alerts";
                break;
            case SKIPPED:
                statusToDisplay = "Skipped";
                break;
            case MISSING_ASSERTIONS:
                statusToDisplay = "Missing Assertions";
                break;
            case MISSING_PARAMETERS:
                statusToDisplay = "Missing Parameters";
                break;
        }
        return statusToDisplay;
    }

    public synchronized void addSecurityScanResult(SecurityScan securityCheck) {
        int size = items.size();
        checkCount++;
        requestCount = 0;

        SecurityScanResult securityCheckResult = securityCheck.getSecurityScanResult();
        SoftReference<SecurityResult> checkResultRef = new SoftReference<SecurityResult>(securityCheckResult);

        items.add("SecurityScan " + checkCount + " [" + securityCheck.getName() + "] ");
        results.add(checkResultRef);
        currentCheckEntriesCount = 1;
        currentStepEntriesCount++;

        fireIntervalAdded(this, size, items.size() - 1);
        enforceMaxSize();
    }

    // updates log entry for security scan with the status, time taken, and
    // similar info known only after finished
    public synchronized void updateSecurityScanResult(SecurityScanResult securityCheckResult, boolean errorsOnly) {
        int startCheckIndex = 0;
        if (items.size() > currentCheckEntriesCount) {
            if (currentCheckEntriesCount > 0) {
                startCheckIndex = items.size() - currentCheckEntriesCount;
            } else {
                startCheckIndex = items.size();
            }
        }
        if (errorsOnly && !securityCheckResult.isHasRequestsWithWarnings()) {
            // remove all entries for the securityCheck that had no warnings
            checkCount--;
            int size = items.size() - 1;
            while (size >= startCheckIndex) {
                items.remove(size);
                results.remove(size);
                size--;
            }
            if (startCheckIndex > 0) {
                fireIntervalRemoved(this, startCheckIndex, size);
            } else {
                fireIntervalRemoved(this, startCheckIndex, size);
            }

        } else {
            SecurityScan securityCheck = securityCheckResult.getSecurityScan();
            securityCheckResult.detectMissingItems();
            StringBuilder outStr = new StringBuilder("SecurityScan ");
            String statusToDisplay = getStatusToDisplay(securityCheckResult.getExecutionProgressStatus());
            outStr.append(checkCount).append(" [").append(securityCheck.getName()).append("] ").append(
                    statusToDisplay).append(", took = ").append(securityCheckResult.getTimeTaken());
            try {
                if (startCheckIndex > 0 && startCheckIndex < maxSize) {
                    items.set(startCheckIndex, outStr.toString());
                    SoftReference<SecurityResult> checkResultRef = new SoftReference<SecurityResult>(securityCheckResult);
                    results.set(startCheckIndex, checkResultRef);
                    currentCheckEntriesCount = 0;
                    fireContentsChanged(this, startCheckIndex, startCheckIndex);
                }
            } catch (IndexOutOfBoundsException e) {
                // when log max size is exceeded skip updating the raw since it
                // won't be visible anyway
            }

        }
    }

    public synchronized void addSecurityScanRequestResult(SecurityScanRequestResult securityCheckRequestResult) {
        int size = items.size();
        requestCount++;

        SoftReference<SecurityResult> checkReqResultRef = new SoftReference<SecurityResult>(securityCheckRequestResult);

        items.add(securityCheckRequestResult.getChangedParamsInfo(requestCount));
        results.add(checkReqResultRef);
        currentCheckEntriesCount++;
        currentStepEntriesCount++;

        for (String msg : securityCheckRequestResult.getMessages()) {
            items.add(" -> " + msg);
            results.add(checkReqResultRef);
            currentCheckEntriesCount++;
            currentStepEntriesCount++;
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

    public synchronized int getIndexOfSecurityScan(SecurityScan check) {
        for (int i = 0; i < results.size(); i++) {
            SoftReference<SecurityResult> result = results.get(i);
            if (result != null) {
                SecurityResult referent = result.get();
                if (referent instanceof SecurityScanResult) {
                    if (((SecurityScanResult) referent).getSecurityScan() == check) {
                        return i;
                    }
                }
            }
        }
        return -1;
    }
}
