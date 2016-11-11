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

package com.eviware.soapui.impl.wsdl.loadtest.data.actions;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.loadtest.log.LoadTestLog;
import com.eviware.soapui.impl.wsdl.loadtest.log.LoadTestLogEntry;
import com.eviware.soapui.support.DateUtil;
import com.eviware.soapui.support.UISupport;
import org.jdesktop.swingx.JXTable;

import javax.swing.AbstractAction;
import javax.swing.Action;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

/**
 * Simple loadtest log exporter, creates a comma-separated file containing a
 * header row and values for each log entry
 *
 * @author Ole.Matzura
 */

public class ExportLoadTestLogAction extends AbstractAction {
    private final LoadTestLog loadTestLog;
    private final JXTable logTable;

    public ExportLoadTestLogAction(LoadTestLog loadTestLog, JXTable logTable) {
        this.loadTestLog = loadTestLog;
        this.logTable = logTable;
        putValue(Action.SMALL_ICON, UISupport.createImageIcon("/export.png"));
        putValue(Action.SHORT_DESCRIPTION, "Export current loadtest log to a file");
    }

    public void actionPerformed(ActionEvent e) {
        try {
            if (loadTestLog.getSize() == 0 || (logTable != null && logTable.getRowCount() == 0)) {
                UISupport.showErrorMessage("No data to export!");
                return;
            }

            File file = UISupport.getFileDialogs().saveAs(this, "Select file for log export");
            if (file == null) {
                return;
            }

            int cnt = exportToFile(file);

            UISupport.showInfoMessage("Saved " + cnt + " log entries to file [" + file.getName() + "]");
        } catch (IOException e1) {
            SoapUI.logError(e1);
        }
    }

    public int exportToFile(File file) throws IOException {
        PrintWriter writer = new PrintWriter(file);
        writeHeader(writer);
        int cnt = writeLog(writer);
        writer.flush();
        writer.close();
        return cnt;
    }

    private int writeLog(PrintWriter writer) {
        int cnt = 0;
        for (int c = 0; c < loadTestLog.getSize(); c++) {
            if (logTable != null) {
                int index = logTable.getFilters().convertRowIndexToView(c);
                if (index == -1) {
                    continue;
                }
            }

            LoadTestLogEntry logEntry = (LoadTestLogEntry) loadTestLog.getElementAt(c);
            writer.write(DateUtil.formatFull(new Date(logEntry.getTimeStamp())));
            writer.write(',');
            writer.write(logEntry.getType());
            writer.write(',');
            String targetStepName = logEntry.getTargetStepName();
            writer.write(targetStepName == null ? "" : targetStepName);
            writer.write(",\"");
            writer.write(logEntry.getMessage());
            writer.write('"');
            writer.println();
            cnt++;
        }

        return cnt;
    }

    private void writeHeader(PrintWriter writer) {
        writer.println("time,type,step,message");
    }
}
