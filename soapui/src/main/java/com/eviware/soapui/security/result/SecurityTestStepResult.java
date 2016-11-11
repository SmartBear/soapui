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

import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.TestStepResult;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.swing.ActionList;
import com.eviware.soapui.support.action.swing.DefaultActionList;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Security result of a TestStep represents summary result of all TestStep
 * security scans
 *
 * @author dragica.soldo
 */

public class SecurityTestStepResult implements SecurityResult {
    private ResultStatus status = ResultStatus.UNKNOWN;
    public static final String TYPE = "SecurityTestStepResult";
    private TestStep testStep;
    private long size;
    private List<SecurityScanResult> securityScanResultList;
    private boolean discarded;
    private long timeTaken = 0;
    private long timeStamp;
    private StringBuffer testLog = new StringBuffer();
    private TestStepResult originalTestStepResult;
    private DefaultActionList actionList;
    private boolean hasAddedRequests;
    private ResultStatus executionProgressStatus = ResultStatus.UNKNOWN;
    ;
    private ResultStatus logIconStatus = ResultStatus.UNKNOWN;
    // indicates if log entries need to be deleted when logging only warnings
    // (status not suitable since can be canceled with warnings)
    private boolean hasScansWithWarnings;

    public SecurityTestStepResult(TestStep testStep, TestStepResult originalResult) {
        this.testStep = testStep;
        executionProgressStatus = ResultStatus.INITIALIZED;
        securityScanResultList = new ArrayList<SecurityScanResult>();
        timeStamp = System.currentTimeMillis();
        this.originalTestStepResult = originalResult;
    }

    public List<SecurityScanResult> getSecurityScanResultList() {
        return securityScanResultList;
    }

    public ResultStatus getStatus() {
        return status;
    }

    public void setStatus(ResultStatus status) {
        this.status = status;
    }

    /**
     * Returns a list of actions that can be applied to this result
     */

    public ActionList getActions() {
        if (actionList == null) {
            actionList = new DefaultActionList(getTestStep().getName());
            actionList.setDefaultAction(new AbstractAction() {

                public void actionPerformed(ActionEvent e) {
                    UISupport.showInfoMessage("Step [" + getTestStep().getName() + "] ran with security status ["
                            + getExecutionProgressStatus() + "]", "TestStep Result");
                }
            });
        }

        return actionList;
    }

    public void addSecurityScanResult(SecurityScanResult securityScanResult) {
        if (securityScanResultList != null) {
            securityScanResultList.add(securityScanResult);
        }
        timeTaken += securityScanResult.getTimeTaken();

        if (!hasAddedRequests) {
            status = securityScanResult.getStatus();
        } else if (status != ResultStatus.FAILED) {
            status = securityScanResult.getStatus();
        }

        securityScanResult.detectMissingItems();
        if (!hasAddedRequests) {
            executionProgressStatus = securityScanResult.getExecutionProgressStatus();
        } else {
            if (securityScanResult.getExecutionProgressStatus().equals(ResultStatus.CANCELED)) {
                executionProgressStatus = securityScanResult.getExecutionProgressStatus();
            } else if (securityScanResult.getExecutionProgressStatus().equals(ResultStatus.MISSING_PARAMETERS)
                    && executionProgressStatus != ResultStatus.CANCELED) {
                executionProgressStatus = ResultStatus.MISSING_PARAMETERS;
            } else if (securityScanResult.getExecutionProgressStatus().equals(ResultStatus.MISSING_ASSERTIONS)
                    && executionProgressStatus != ResultStatus.CANCELED
                    && executionProgressStatus != ResultStatus.MISSING_PARAMETERS) {
                executionProgressStatus = ResultStatus.MISSING_ASSERTIONS;
            } else if (securityScanResult.getExecutionProgressStatus().equals(ResultStatus.FAILED)
                    && executionProgressStatus != ResultStatus.CANCELED
                    && executionProgressStatus != ResultStatus.MISSING_PARAMETERS
                    && executionProgressStatus != ResultStatus.MISSING_ASSERTIONS) {
                executionProgressStatus = ResultStatus.FAILED;
            } else if (securityScanResult.getExecutionProgressStatus().equals(ResultStatus.OK)
                    && executionProgressStatus != ResultStatus.CANCELED
                    && executionProgressStatus != ResultStatus.MISSING_PARAMETERS
                    && executionProgressStatus != ResultStatus.MISSING_ASSERTIONS
                    && executionProgressStatus != ResultStatus.FAILED) {
                executionProgressStatus = ResultStatus.OK;
            }
        }
        if (securityScanResult.getLogIconStatus().equals(ResultStatus.FAILED)) {
            logIconStatus = securityScanResult.getLogIconStatus();
        } else if ((securityScanResult.getLogIconStatus().equals(ResultStatus.MISSING_ASSERTIONS) || securityScanResult
                .getLogIconStatus().equals(ResultStatus.MISSING_PARAMETERS))
                && logIconStatus != ResultStatus.FAILED) {
            logIconStatus = securityScanResult.getLogIconStatus();
        } else if (securityScanResult.getLogIconStatus().equals(ResultStatus.OK) && logIconStatus != ResultStatus.FAILED
                && logIconStatus != ResultStatus.MISSING_ASSERTIONS && logIconStatus != ResultStatus.MISSING_PARAMETERS) {
            logIconStatus = ResultStatus.OK;
        }

        // TODO check and finish this - seems it's used for reports
        // this.testLog.append( "SecurityScan " ).append(
        // securityCheckResultList.indexOf( securityCheckResult ) ).append(
        // securityCheckResult.getStatus().toString() ).append( ": took " )
        // .append( securityCheckResult.getTimeTaken() ).append( " ms" );
        this.testLog.append(securityScanResult.getSecurityTestLog());

        hasAddedRequests = true;

        if (securityScanResult.isHasRequestsWithWarnings()) {
            hasScansWithWarnings = true;
        }
    }

    public boolean isHasScansWithWarnings() {
        return hasScansWithWarnings;
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
        for (SecurityScanResult scanResult : securityScanResultList) {
            int i = 0;
            for (SecurityScanRequestResult scanRequestResult : scanResult.getSecurityRequestResultList()) {
                writer.println();
                writer.println("----------------------------------------------------------------------------------");
                writer.println(scanRequestResult.getChangedParamsInfo(i));
                for (String message : scanRequestResult.getMessages()) {
                    writer.println("->" + message);
                }
                writer.println();
                writer.println("Properties -----------------------------------------------------------------------");
                writer.println();
                for (String name : scanRequestResult.getMessageExchange().getProperties().keySet()) {
                    if (scanRequestResult.getMessageExchange().getProperties().get(name) != null) {
                        writer.println(name + " = " + scanRequestResult.getMessageExchange().getProperties().get(name));
                    }
                }
                writer.println();
                writer.println("Request ---------------------------------------------------------------------------");
                writer.println();
                writer.println(new String(scanRequestResult.getMessageExchange().getRawRequestData()));
                writer.println();
                writer.println("Response --------------------------------------------------------------------------");
                writer.println();
                writer.println(new String(scanRequestResult.getMessageExchange().getRawResponseData()));
                writer.println("-----------------------------------------------------------------------------------");
                writer.println();
                writer.println();
                i++;
            }

        }

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

    public TestStepResult getOriginalTestStepResult() {
        return originalTestStepResult;
    }

    public void setOriginalTestStepResult(TestStepResult originalTestStepResult) {
        this.originalTestStepResult = originalTestStepResult;
    }

    public TestStep getTestStep() {
        return testStep;
    }

    /**
     * Raturns Security Test Log
     */
    public String getSecurityTestLog() {
        StringBuffer tl = new StringBuffer().append("Step ").append(" [").append(testStep.getName()).append("] ")
                .append(getExecutionProgressStatus().toString()).append(": took ").append(
                        getOriginalTestStepResult().getTimeTaken()).append(" ms");
        tl.append(testLog);
        return tl.toString();
    }

    @Override
    public String getResultType() {
        return TYPE;
    }

    @Override
    public ResultStatus getExecutionProgressStatus() {
        return executionProgressStatus;
    }

    public void setExecutionProgressStatus(ResultStatus status) {
        executionProgressStatus = status;
    }

    @Override
    public ResultStatus getLogIconStatus() {
        return logIconStatus;
    }

    public String getSecurityTestStepName() {
        return getTestStep().getName();
    }

    public String getLogIconStatusString() {
        return logIconStatus.toString();
    }

    public String getStatusString() {
        return status.toString();
    }

    public void release() {
        if (securityScanResultList != null) {
            securityScanResultList.clear();
        }
    }

}
