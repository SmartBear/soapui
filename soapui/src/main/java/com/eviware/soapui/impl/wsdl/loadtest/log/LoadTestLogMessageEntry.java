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

import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.swing.ActionList;

import javax.swing.ImageIcon;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

/**
 * A simple message LoadTest Log entry
 *
 * @author Ole.Matzura
 */

public class LoadTestLogMessageEntry implements LoadTestLogEntry {
    private final String message;
    private long timestamp;
    private ImageIcon icon;
    private boolean discarded;

    public LoadTestLogMessageEntry(String message) {
        this.message = message;
        timestamp = System.currentTimeMillis();

        icon = UISupport.createImageIcon("/loadtest_log_message.gif");
    }

    public String getMessage() {
        return message;
    }

    public long getTimeStamp() {
        return timestamp;
    }

    public String getTargetStepName() {
        return null;
    }

    public ImageIcon getIcon() {
        return icon;
    }

    public String getType() {
        return "Message";
    }

    public boolean isError() {
        return false;
    }

    public ActionList getActions() {
        return null;
    }

    public void exportToFile(String fileName) throws IOException {
        PrintWriter writer = new PrintWriter(fileName);
        writer.write(new Date(timestamp).toString());
        writer.write(":");
        writer.write(message);
        writer.close();
    }

    public void discard() {
        discarded = true;
    }

    public boolean isDiscarded() {
        return discarded;
    }
}
