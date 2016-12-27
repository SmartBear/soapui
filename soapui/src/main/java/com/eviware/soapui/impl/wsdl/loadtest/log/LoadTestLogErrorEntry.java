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

import com.eviware.soapui.model.testsuite.TestStepResult;
import com.eviware.soapui.support.action.swing.ActionList;

import javax.swing.ImageIcon;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

/**
 * An error entry in the LoadTest Log
 *
 * @author Ole.Matzura
 */

public class LoadTestLogErrorEntry implements LoadTestLogEntry {
    private final String error;
    private TestStepResult result;
    private String type;
    private ImageIcon icon;
    private long timestamp;
    private boolean discarded;
    private String targetStepName;
    private final int threadIndex;

    public LoadTestLogErrorEntry(String type, String error, TestStepResult result, ImageIcon icon, int threadIndex) {
        this.icon = icon;
        this.type = type;
        this.error = error;
        this.result = result;
        this.threadIndex = threadIndex;
        this.targetStepName = result == null ? null : result.getTestStep().getName();

        timestamp = result == null ? System.currentTimeMillis() : result.getTimeStamp();
    }

    public LoadTestLogErrorEntry(String type, String message, ImageIcon icon, int threadIndex) {
        this.type = type;
        this.error = message;
        this.icon = icon;
        this.threadIndex = threadIndex;

        targetStepName = "- Total -";

        timestamp = System.currentTimeMillis();
    }

    public String getMessage() {
        if (discarded) {
            return error + " [discarded]";
        } else {
            return error + " [threadIndex=" + threadIndex + "]";
        }
    }

    public TestStepResult getTestStepResult() {
        return result;
    }

    public long getTimeStamp() {
        return timestamp;
    }

    public String getTargetStepName() {
        return targetStepName;
    }

    public ImageIcon getIcon() {
        return icon;
    }

    public String getType() {
        return type;
    }

    public boolean isError() {
        return true;
    }

    public ActionList getActions() {
        return result == null ? null : result.getActions();
    }

    public void exportToFile(String fileName) throws IOException {
        PrintWriter writer = new PrintWriter(fileName);

        writer.write(new Date(timestamp).toString());
        writer.write(":");
        writer.write(targetStepName);
        writer.write(":");
        writer.write(error);
        writer.write(":");
        writer.print(threadIndex);
        writer.println();
        if (result != null) {
            writer.println("----------------------------------------------------");
            result.writeTo(writer);
        } else if (discarded) {
            writer.println("-> Discarded");
        }

        writer.close();
    }

    public void discard() {
        result = null;
        discarded = true;
    }

    public boolean isDiscarded() {
        return discarded;
    }
}
