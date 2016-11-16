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

package com.eviware.soapui.impl.wsdl.support.wss.support;

import com.eviware.soapui.impl.wsdl.support.wss.entries.WssEntryBase;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.support.swing.JTableFactory;
import com.eviware.soapui.support.types.StringToStringMap;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.List;

/**
 * @author Erik R. Yverling
 *         <p/>
 *         Displays a table of SAML attribute values
 */
public class SAMLAttributeValuesTable extends JPanel {
    private final List<StringToStringMap> attributeValues;
    private WssEntryBase entry;
    private AttributeValuesTableModel attributeValuesTableModel;
    private JTable attributeValuesTable;
    private JButton removeAttributeValueButton;
    private JButton createAttributeValueButton;

    public SAMLAttributeValuesTable(List<StringToStringMap> attributeValues, WssEntryBase entry) {
        super(new BorderLayout());
        this.attributeValues = attributeValues;
        this.entry = entry;

        attributeValuesTableModel = new AttributeValuesTableModel();
        attributeValuesTable = JTableFactory.getInstance().makeJTable(attributeValuesTableModel);
        attributeValuesTable.setTableHeader(null);
        attributeValuesTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent e) {
                removeAttributeValueButton.setEnabled(attributeValuesTable.getSelectedRow() != -1);
            }
        });

        JScrollPane scrollPane = new JScrollPane(attributeValuesTable);
        scrollPane.setBackground(Color.WHITE);
        scrollPane.setOpaque(true);
        add(scrollPane, BorderLayout.CENTER);
        add(buildToolbar(), BorderLayout.NORTH);

        setPreferredSize(new Dimension(175, 150));
    }

    @Override
    public void setEnabled(boolean b) {
        attributeValuesTable.setEnabled(b);
        createAttributeValueButton.setEnabled(b);
        removeAttributeValueButton.setEnabled(b);
    }

    private Component buildToolbar() {
        JXToolBar toolbar = UISupport.createSmallToolbar();

        createAttributeValueButton = UISupport.createToolbarButton(new AddAttributeValueAction());
        toolbar.addFixed(createAttributeValueButton);
        removeAttributeValueButton = UISupport.createToolbarButton(new RemoveAttributeValueAction());
        toolbar.addFixed(removeAttributeValueButton);

        return toolbar;
    }

    private class AttributeValuesTableModel extends AbstractTableModel {
        public int getColumnCount() {
            return 1;
        }

        public int getRowCount() {
            return attributeValues.size();
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return true;
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            StringToStringMap attributeValue = attributeValues.get(rowIndex);
            if (aValue == null) {
                aValue = "";
            }

            fireTableCellUpdated(rowIndex, 1);
            attributeValue.put("value", aValue.toString());

            entry.saveConfig();
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            StringToStringMap attributeValue = attributeValues.get(rowIndex);
            return attributeValue.get("value");
        }

        public void remove(int row) {
            attributeValues.remove(row);
            fireTableRowsDeleted(row, row);
        }

        public void addAttributeValues(StringToStringMap map) {
            attributeValues.add(map);
            fireTableRowsInserted(attributeValues.size() - 1, attributeValues.size() - 1);
        }
    }

    private class AddAttributeValueAction extends AbstractAction {
        public AddAttributeValueAction() {
            putValue(SMALL_ICON, UISupport.createImageIcon("/add.png"));
            putValue(SHORT_DESCRIPTION, "Adds a new attribute value");
        }

        public void actionPerformed(ActionEvent e) {
            attributeValuesTableModel.addAttributeValues(new StringToStringMap());
            entry.saveConfig();
        }
    }

    private class RemoveAttributeValueAction extends AbstractAction {
        public RemoveAttributeValueAction() {
            putValue(SMALL_ICON, UISupport.createImageIcon("/delete.png"));
            putValue(SHORT_DESCRIPTION, "Removes the attribute value");
            setEnabled(false);
        }

        public void actionPerformed(ActionEvent e) {
            int row = attributeValuesTable.getSelectedRow();
            if (row == -1) {
                return;
            }

            if (UISupport.confirm("Remove selected attribute value?", "Remove attribute value")) {
                attributeValuesTableModel.remove(row);
                entry.saveConfig();
            }
        }
    }
}
