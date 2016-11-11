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
import com.eviware.soapui.support.UISupport;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.table.TableModel;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Simple statistics exporter, creates a comma-separated file containing a
 * header row and values for each test step
 *
 * @author Ole.Matzura
 */

public class ExportStatisticsAction extends AbstractAction {
    private final TableModel model;

    public ExportStatisticsAction(TableModel model) {
        this.model = model;
        putValue(Action.SMALL_ICON, UISupport.createImageIcon("/export.png"));
        putValue(Action.SHORT_DESCRIPTION, "Export statistics to a file");
    }

    public void actionPerformed(ActionEvent e) {
        try {
            if (model.getRowCount() == 0) {
                UISupport.showErrorMessage("No data to export!");
                return;
            }

            File file = UISupport.getFileDialogs().saveAs(this, "Select file for export");
            if (file == null) {
                return;
            }

            int cnt = exportToFile(file);
            UISupport.showInfoMessage("Saved " + cnt + " rows to file [" + file.getName() + "]");
        } catch (IOException e1) {
            SoapUI.logError(e1);
        }
    }

    public int exportToFile(File file) throws IOException {
        PrintWriter writer = new PrintWriter(file);
        writerHeader(writer);
        int cnt = writeData(writer);
        writer.flush();
        writer.close();
        return cnt;
    }

    private int writeData(PrintWriter writer) {
        int c = 0;
        for (; c < model.getRowCount(); c++) {
            for (int i = 1; i < model.getColumnCount(); i++) {
                if (i > 1) {
                    writer.print(',');
                }

                writer.print(model.getValueAt(c, i));
            }

            writer.println();
        }

        return c;
    }

    private void writerHeader(PrintWriter writer) {
        for (int i = 1; i < model.getColumnCount(); i++) {
            if (i > 1) {
                writer.print(',');
            }

            writer.print(model.getColumnName(i));
        }

        writer.println();
    }
}
