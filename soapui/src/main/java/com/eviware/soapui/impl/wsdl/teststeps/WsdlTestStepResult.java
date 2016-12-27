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

package com.eviware.soapui.impl.wsdl.teststeps;

import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.TestStepResult;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.swing.ActionList;
import com.eviware.soapui.support.action.swing.DefaultActionList;

import javax.swing.AbstractAction;
import javax.swing.Action;
import java.awt.event.ActionEvent;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Default implementation of TestStepResult interface
 *
 * @author Ole.Matzura
 */

public class WsdlTestStepResult implements TestStepResult {
    private static final String[] EMPTY_MESSAGES = new String[0];
    private final WsdlTestStep testStep;
    private List<String> messages = new ArrayList<String>();
    private Throwable error;
    private TestStepStatus status = TestStepStatus.UNKNOWN;
    private long timeTaken;
    private long timeStamp;
    private long size;
    private DefaultActionList actionList;
    private long startTime;
    private boolean discarded;
    private String testStepName;

    private static DefaultActionList discardedActionList = new DefaultActionList(null);

    static {
        discardedActionList.setDefaultAction(new AbstractAction() {
            public void actionPerformed(ActionEvent arg0) {
                UISupport.showErrorMessage("Result has been discarded");
            }
        });
    }

    public WsdlTestStepResult(WsdlTestStep testStep) {
        this.testStep = testStep;
        testStepName = testStep.getName();
        timeStamp = System.currentTimeMillis();
    }

    public TestStepStatus getStatus() {
        return status;
    }

    public void setStatus(TestStepStatus status) {
        this.status = status;
    }

    public TestStep getTestStep() {
        try {
            if (testStep != null) {
                testStep.getName();
            }

            return testStep;
        } catch (Throwable t) {
        }

        return null;
    }

    public ActionList getActions() {
        if (isDiscarded()) {
            return discardedActionList;
        }

        if (actionList == null) {
            actionList = new DefaultActionList(testStepName);
            actionList.setDefaultAction(new AbstractAction() {

                public void actionPerformed(ActionEvent e) {
                    if (getMessages().length > 0) {
                        StringBuffer buf = new StringBuffer("<html><body>");
                        if (getError() != null) {
                            buf.append(getError().toString()).append("<br/>");
                        }

                        for (String s : getMessages()) {
                            buf.append(s).append("<br/>");
                        }

                        UISupport.showExtendedInfo("TestStep Result", "Step [" + testStepName + "] ran with status ["
                                + getStatus() + "]", buf.toString(), null);
                    } else if (getError() != null) {
                        UISupport.showExtendedInfo("TestStep Result", "Step [" + testStepName + "] ran with status ["
                                + getStatus() + "]", getError().toString(), null);
                    } else {
                        UISupport.showInfoMessage("Step [" + testStepName + "] ran with status [" + getStatus() + "]",
                                "TestStep Result");
                    }
                }
            });
        }

        return actionList;
    }

    public void addAction(Action action, boolean isDefault) {
        if (isDiscarded()) {
            return;
        }

        if (actionList == null) {
            actionList = new DefaultActionList(testStepName);
        }

        actionList.addAction(action);
        if (isDefault) {
            actionList.setDefaultAction(action);
        }
    }

    public Throwable getError() {
        return error;
    }

    public void setError(Throwable error) {
        this.error = error;
    }

    public String[] getMessages() {
        return messages == null ? EMPTY_MESSAGES : messages.toArray(new String[messages.size()]);
    }

    public void addMessage(String message) {
        if (messages != null) {
            messages.add(message);
        }
    }

    public long getTimeTaken() {
        return timeTaken;
    }

    public void setTimeTaken(long timeTaken) {
        this.timeTaken = timeTaken;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getSize() {
        return size;
    }

    public void writeTo(PrintWriter writer) {
        writer.println("Status: " + getStatus());
        writer.println("Time Taken: " + getTimeTaken());
        writer.println("Size: " + getSize());
        writer.println("Timestamp: " + new Date(getTimeStamp()).toString());
        writer.println("TestStep: " + getTestStep().getName());
        if (error != null) {
            writer.println("Error:" + error.toString());
        }

        if (messages != null) {
            writer.println("\r\n----------------- Messages ------------------------------");
            for (String message : messages) {
                if (message != null) {
                    writer.println(message);
                }
            }
        }

        if (isDiscarded()) {
            writer.println("Result has been Discarded!");
        }
    }

    public void startTimer() {
        startTime = System.nanoTime();
    }

    public void stopTimer() {
        timeTaken = ((System.nanoTime() - startTime) / 1000000);
    }

    public void discard() {
        discarded = true;

        messages = null;
        error = null;
        actionList = null;
    }

    public boolean isDiscarded() {
        return discarded;
    }

    public void addMessages(String[] messages) {
        if (this.messages != null) {
            this.messages.addAll(Arrays.asList(messages));
        }
    }
}
