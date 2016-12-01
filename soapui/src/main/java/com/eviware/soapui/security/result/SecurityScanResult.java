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

package com.eviware.soapui.security.result;

import com.eviware.soapui.model.security.SecurityScan;
import com.eviware.soapui.security.scan.AbstractSecurityScanWithProperties;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.swing.ActionList;
import com.eviware.soapui.support.action.swing.DefaultActionList;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * A SecurityScan result represents result of one request (modified by a
 * security scan and run)
 *
 * @author dragica.soldo
 */

public class SecurityScanResult implements SecurityResult {
    public final static String TYPE = "SecurityScanResult";
    /**
     * status is set to SecurityStatus.INITIALIZED but goes to
     * SecurityStatus.UNKNOWN first time any scanRequestResult is added.
     * INITIALIZED status is necessary to be able to detect when logging if
     * SecurityScan is just started and no status icon should be added, or it
     * went through execution and gone into any other status, including UNKNOWN
     * if no assertion is added, when status icon should be added to log
     */
    private ResultStatus status;
    public SecurityScan securityCheck;
    private long size;
    private boolean discarded;
    private List<SecurityScanRequestResult> securityRequestResultList;
    private long timeTaken = 0;
    private long timeStamp;
    public StringBuffer testLog = new StringBuffer();
    private DefaultActionList actionList;
    private boolean hasAddedRequests;
    // along with the status determines if canceled with or without warnings
    private boolean hasRequestsWithWarnings;
    private ResultStatus executionProgressStatus;
    private ResultStatus logIconStatus;
    private int requestCount = 0;
    public final static int MAX_REQ_LOG_ENTRY_LENGTH = 100;
    public final static int MAX_SECURITY_CHANGED_PARAMETERS_LENGTH = 100;

    public SecurityScanResult(SecurityScan securityCheck) {
        this.securityCheck = securityCheck;
        status = ResultStatus.INITIALIZED;
        executionProgressStatus = ResultStatus.INITIALIZED;
        logIconStatus = ResultStatus.UNKNOWN;
        securityRequestResultList = new ArrayList<SecurityScanRequestResult>();
        timeStamp = System.currentTimeMillis();
        requestCount = 0;
    }

    public List<SecurityScanRequestResult> getSecurityRequestResultList() {
        return securityRequestResultList;
    }

    public ResultStatus getStatus() {
        return this.status;
    }

    public void setStatus(ResultStatus status) {
        this.status = status;
    }

    public SecurityScan getSecurityScan() {
        return securityCheck;
    }

    /**
     * Returns a list of actions that can be applied to this result
     */

    public ActionList getActions() {
        if (actionList == null) {
            actionList = new DefaultActionList(getSecurityScan().getName());
            actionList.setDefaultAction(new AbstractAction() {

                public void actionPerformed(ActionEvent e) {
                    UISupport.showInfoMessage("Scan [" + getSecurityScan().getName() + "] ran with status ["
                            + getExecutionProgressStatus() + "]", "SecurityScan Result");
                }
            });
        }

        return actionList;
    }

    public void addSecurityRequestResult(SecurityScanRequestResult secReqResult) {
        if (securityRequestResultList != null) {
            securityRequestResultList.add(secReqResult);
        }

        timeTaken += secReqResult.getTimeTaken();
        requestCount++;

        if (!hasAddedRequests) {
            status = ResultStatus.UNKNOWN;
            if (secReqResult.getStatus() == ResultStatus.OK) {
                status = ResultStatus.OK;
            } else if (secReqResult.getStatus() == ResultStatus.FAILED) {
                hasRequestsWithWarnings = true;
                status = ResultStatus.FAILED;
            }
        } else if (secReqResult.getStatus() == ResultStatus.FAILED) {
            hasRequestsWithWarnings = true;
            status = ResultStatus.FAILED;
        } else if (secReqResult.getStatus() == ResultStatus.OK && status != ResultStatus.FAILED) {
            status = ResultStatus.OK;
        }
        logIconStatus = status;
        executionProgressStatus = status;

        this.testLog.append("\n").append(secReqResult.getChangedParamsInfo(requestCount));
        for (String s : secReqResult.getMessages()) {
            if (s.length() > MAX_REQ_LOG_ENTRY_LENGTH) {
                s = s.substring(0, MAX_REQ_LOG_ENTRY_LENGTH);
            }
            testLog.append("\n -> ").append(s);
        }

        hasAddedRequests = true;
    }

    public long getTimeTaken() {
        return timeTaken;
    }

    /**
     * Used for calculating the output
     *
     * @return the number of bytes in this result
     */

    public long getSize() {
        return size;
    }

    /**
     * Writes this result to the specified writer, used for logging.
     */

    public void writeTo(PrintWriter writer) {

    }

    /**
     * Can discard any result data that may be taking up memory. Timing-values
     * must not be discarded.
     */

    public void discard() {
    }

    public boolean isDiscarded() {
        return discarded;
    }

    /**
     * Returns time stamp when test is started.
     *
     * @return
     */
    public long getTimeStamp() {
        return timeStamp;
    }

    /**
     * Raturns Security Test Log
     */
    public String getSecurityTestLog() {
        StringBuffer tl = new StringBuffer().append("\nSecurityScan ").append(" [").append(securityCheck.getName())
                .append("] ").append(executionProgressStatus.toString()).append(": took ").append(timeTaken)
                .append(" ms");
        tl.append(testLog);
        return tl.toString();
    }

    @Override
    public String getResultType() {
        return TYPE;
    }

    public boolean isCanceled() {
        return status == ResultStatus.CANCELED;
    }

    public boolean isHasRequestsWithWarnings() {
        return hasRequestsWithWarnings;
    }

    @Override
    public ResultStatus getExecutionProgressStatus() {
        return executionProgressStatus;
    }

    public void setExecutionProgressStatus(ResultStatus status) {
        executionProgressStatus = status;
    }

    public void detectMissingItems() {
        SecurityScan securityCheck = getSecurityScan();
        if (getStatus().equals(ResultStatus.SKIPPED)) {
            executionProgressStatus = ResultStatus.SKIPPED;
        }
        if (securityCheck instanceof AbstractSecurityScanWithProperties
                && ((AbstractSecurityScanWithProperties) securityCheck).getParameterHolder().getParameterList().size() == 0) {
            logIconStatus = ResultStatus.MISSING_PARAMETERS;
            executionProgressStatus = ResultStatus.MISSING_PARAMETERS;
        }
        if (securityCheck.getAssertionCount() == 0) {
            logIconStatus = ResultStatus.MISSING_ASSERTIONS;
            executionProgressStatus = ResultStatus.MISSING_ASSERTIONS;
        }
        if (getStatus().equals(ResultStatus.CANCELED)) {
            executionProgressStatus = ResultStatus.CANCELED;
        }

    }

    @Override
    public ResultStatus getLogIconStatus() {
        return logIconStatus;
    }

    public String getSecurityScanName() {
        return getSecurityScan().getName();
    }

    public String getLogIconStatusString() {
        return logIconStatus.toString();
    }

    public String getStatusString() {
        return status.toString();
    }

    public void release() {
        if (securityRequestResultList != null) {
            securityRequestResultList.clear();
        }

        securityCheck = null;
    }

}
