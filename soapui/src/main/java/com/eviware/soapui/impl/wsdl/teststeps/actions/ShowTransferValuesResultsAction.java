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

package com.eviware.soapui.impl.wsdl.teststeps.actions;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.teststeps.PropertyTransfersTestStep.PropertyTransferResult;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStepResult;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.swing.JTableFactory;
import com.eviware.soapui.ui.desktop.DesktopPanel;
import com.eviware.soapui.ui.support.DefaultDesktopPanel;
import org.jdesktop.swingx.JXTable;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.table.AbstractTableModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.Arrays;

/**
 * Shows a desktop-panel with the TestStepResult for a ValueTransferResult
 *
 * @author Ole.Matzura
 */

public class ShowTransferValuesResultsAction extends AbstractAction {
    private final PropertyTransferResult result;
    private DefaultDesktopPanel desktopPanel;

    public ShowTransferValuesResultsAction(WsdlTestStepResult result) {
        this.result = (PropertyTransferResult) result;
    }

    public void actionPerformed(ActionEvent e) {
        try {
            if (result.isDiscarded()) {
                UISupport.showInfoMessage("Request has been discarded..");
            } else {
                showDesktopPanel();
            }
        } catch (Exception ex) {
            SoapUI.logError(ex);
        }
    }

    public DesktopPanel showDesktopPanel() {
        return UISupport.showDesktopPanel(buildFrame());
    }

    private DesktopPanel buildFrame() {
        if (desktopPanel == null) {
            desktopPanel = new DefaultDesktopPanel("TestStep Result", "TestStep result for "
                    + result.getTestStep().getName(), buildContent());
        }

        return desktopPanel;
    }

    private JComponent buildContent() {
        JPanel panel = new JPanel(new BorderLayout());
        JXTable table = JTableFactory.getInstance().makeJXTable(new TransfersTableModel());

        table.setHorizontalScrollEnabled(true);
        table.packAll();

        Component descriptionPanel = UISupport.buildDescription("PropertyTransfer Results",
                "See the result of each performed transfer below", null);
        panel.add(descriptionPanel, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3),
                scrollPane.getBorder()));

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.setPreferredSize(new Dimension(550, 300));

        return panel;
    }

    private class TransfersTableModel extends AbstractTableModel {
        public int getRowCount() {
            return result.getTransferCount();
        }

        public int getColumnCount() {
            return 2;
        }

        public String getColumnName(int column) {
            switch (column) {
                case 0:
                    return "Transfer Name";
                case 1:
                    return "Transferred Values";
            }

            return null;
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return result.getTransferAt(rowIndex).getName();
                case 1:
                    return Arrays.toString(result.getTransferredValuesAt(rowIndex));
            }

            return null;
        }

    }

}
