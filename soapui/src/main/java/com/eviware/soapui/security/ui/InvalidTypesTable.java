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

package com.eviware.soapui.security.ui;

import com.eviware.soapui.config.InvalidSecurityScanConfig;
import com.eviware.soapui.config.SchemaTypeForSecurityScanConfig;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.support.swing.JTableFactory;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AField.AFieldType;
import com.eviware.x.form.support.AForm;
import com.eviware.x.impl.swing.JComboBoxFormField;
import org.apache.xmlbeans.SchemaType;
import org.jdesktop.swingx.JXTable;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Table for handling schema types for InvalidTypes Security Scan
 *
 * @author robert
 */
@SuppressWarnings("serial")
public class InvalidTypesTable extends JPanel {

    private InvalidTypeTableModel model;
    private JXTable table;
    private JXToolBar toolbar;

    private Map<String, Integer> typeMap = new HashMap<String, Integer>() {
        {
            put("STRING", SchemaType.BTC_STRING);
            put("NORMALIZED_STRING", SchemaType.BTC_NORMALIZED_STRING);
            put("TOKEN", SchemaType.BTC_TOKEN);
            put("BASE_64_BINARY", SchemaType.BTC_BASE_64_BINARY);
            put("HEX_BINARY", SchemaType.BTC_HEX_BINARY);
            put("INTEGER", SchemaType.BTC_INTEGER);
            put("POSITIVE_INTEGER", SchemaType.BTC_POSITIVE_INTEGER);
            put("NEGATIVE_INTEGER", SchemaType.BTC_NEGATIVE_INTEGER);
            put("NON_NEGATIVE_INTEGER", SchemaType.BTC_NON_NEGATIVE_INTEGER);
            put("NON_POSITIVE_INTEGER", SchemaType.BTC_POSITIVE_INTEGER);
            put("LONG", SchemaType.BTC_LONG);
            put("UNSIGNED_LONG", SchemaType.BTC_UNSIGNED_LONG);
            put("UNSIGNED_INT", SchemaType.BTC_UNSIGNED_INT);
            put("SHORT", SchemaType.BTC_SHORT);
            put("UNSIGNED_SHORT", SchemaType.BTC_UNSIGNED_SHORT);
            put("BYTE", SchemaType.BTC_BYTE);
            put("UNSIGNED_BYTE", SchemaType.BTC_UNSIGNED_BYTE);
            put("DECIMAL", SchemaType.BTC_DECIMAL);
            put("FLOAT", SchemaType.BTC_FLOAT);
            put("BOOLEAN", SchemaType.BTC_BOOLEAN);
            put("DURATION", SchemaType.BTC_DURATION);
            put("DATE_TIME", SchemaType.BTC_DATE_TIME);
            put("DATE", SchemaType.BTC_DATE);
            put("TIME", SchemaType.BTC_TIME);
            put("G_YEAR", SchemaType.BTC_G_YEAR);
            put("G_YEAR_MONTH", SchemaType.BTC_G_YEAR_MONTH);
            put("G_MONTH", SchemaType.BTC_G_MONTH);
            put("G_MONTH_DAY", SchemaType.BTC_G_MONTH_DAY);
            put("G_DAY", SchemaType.BTC_G_DAY);

        }
    };

    public InvalidTypesTable(InvalidSecurityScanConfig invalidTypeConfig) {
        this.model = new InvalidTypeTableModel(invalidTypeConfig);
        init();
    }

    private void init() {
        setLayout(new BorderLayout());
        toolbar = UISupport.createToolbar();

        toolbar.add(UISupport.createToolbarButton(new AddNewTypeAction()));
        toolbar.add(UISupport.createToolbarButton(new RemoveTypeAction()));
        toolbar.addGlue();

        add(toolbar, BorderLayout.NORTH);
        table = JTableFactory.getInstance().makeJXTable(model);
        TableRowSorter<InvalidTypeTableModel> sorter = new TableRowSorter<InvalidTypeTableModel>(model);
        table.setRowSorter(sorter);
        table.toggleSortOrder(0);
        add(new JScrollPane(table), BorderLayout.CENTER);
        setPreferredSize(new Dimension(100, 200));

    }

    private class RemoveTypeAction extends AbstractAction {

        public RemoveTypeAction() {
            putValue(Action.SMALL_ICON, UISupport.createImageIcon("/delete.png"));
            putValue(Action.SHORT_DESCRIPTION, "Removes type from security scan");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            model.removeRows(table.getSelectedRows());
        }

    }

    private class AddNewTypeAction extends AbstractAction {

        public AddNewTypeAction() {
            putValue(Action.SMALL_ICON, UISupport.createImageIcon("/add.png"));
            putValue(Action.SHORT_DESCRIPTION, "Adds new type to use in security scan");
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            XFormDialog dialog = ADialogBuilder.buildDialog(AddParameterActionDialog.class);
            JComboBoxFormField chooser = (JComboBoxFormField) dialog.getFormField(AddParameterActionDialog.TYPE);
            chooser.setOptions(typeMap.keySet().toArray(new String[0]));
            if (dialog.show()) {
                model.addNewType(typeMap.get(chooser.getValue()), dialog.getValue(AddParameterActionDialog.VALUE));
            }
        }

    }

    private class InvalidTypeTableModel extends AbstractTableModel {

        private InvalidSecurityScanConfig data;
        private String[] columns = {"Type Name", "Type Value"};

        public InvalidTypeTableModel(InvalidSecurityScanConfig invalidTypeConfig) {
            this.data = invalidTypeConfig;
        }

        public void removeRows(int[] selectedRows) {
            @SuppressWarnings("rawtypes")
            List toRemove = new ArrayList();
            /*
			 * since we are using TableRowSorter you need to cpnvert between view
			 * and model.
			 */
            for (int i = 0; i < selectedRows.length; i++) {
                selectedRows[i] = table.convertRowIndexToModel(selectedRows[i]);
            }
            for (int index : selectedRows) {
                toRemove.add(data.getTypesListList().get(index));
            }
            data.getTypesListList().removeAll(toRemove);
            fireTableDataChanged();
        }

        public void addNewType(int type, String value) {
            SchemaTypeForSecurityScanConfig newtype = data.addNewTypesList();
            newtype.setType(type);
            newtype.setValue(value);

            fireTableDataChanged();
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == 1;
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            SchemaTypeForSecurityScanConfig paramType = data.getTypesListList().get(rowIndex);

            paramType.setValue((String) aValue);

            fireTableDataChanged();
        }

        @Override
        public String getColumnName(int column) {
            return columns[column];
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public int getRowCount() {
            if (data.getTypesListList() == null) {
                return 0;
            }
            return data.getTypesListList().size();
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (columnIndex == 0) {
                return getTypeName(data.getTypesListList().get(rowIndex).getType());
            } else {
                return data.getTypesListList().get(rowIndex).getValue();
            }
        }

        private String getTypeName(int type) {
            String result = "UNKNOWN";

            switch (type) {
                case SchemaType.BTC_STRING:
                    return "STRING";
                case SchemaType.BTC_NORMALIZED_STRING:
                    return "NORMALIZED_STRING";
                // no cr/lf/tab
                case SchemaType.BTC_TOKEN:
                    return "TOKEN";
                // base64Binary
                case SchemaType.BTC_BASE_64_BINARY:
                    return "BASE_64_BINARY";
                // hexBinary
                case SchemaType.BTC_HEX_BINARY:
                    return "HEX_BINARY";
                // integer - no min or max
                case SchemaType.BTC_INTEGER:
                    return "INTEGER";
                // positive integer
                case SchemaType.BTC_POSITIVE_INTEGER:
                    return "POSITIVE_INTEGER";
                // negative integer
                case SchemaType.BTC_NEGATIVE_INTEGER:
                    return "NEGATIVE_INTEGER";
                // non negative integer
                case SchemaType.BTC_NON_NEGATIVE_INTEGER:
                    return "NON_NEGATIVE_INTEGER";
                // non positive integer
                case SchemaType.BTC_NON_POSITIVE_INTEGER:
                    return "NON_POSITIVE_INTEGER";
                // long
                case SchemaType.BTC_LONG:
                    return "LONG";
                // unsigned long
                case SchemaType.BTC_UNSIGNED_LONG:
                    return "UNSIGNED_LONG";
                // int
                case SchemaType.BTC_INT:
                    return "INT";
                // unsigned int
                case SchemaType.BTC_UNSIGNED_INT:
                    return "UNSIGNED_INT";
                // short
                case SchemaType.BTC_SHORT:
                    return "SHORT";
                // unsigned short
                case SchemaType.BTC_UNSIGNED_SHORT:
                    return "UNSIGNED_SHORT";
                // byte
                case SchemaType.BTC_BYTE:
                    return "BYTE";
                // unsigned byte
                case SchemaType.BTC_UNSIGNED_BYTE:
                    return "UNSIGNED_BYTE";
                // decimal
                case SchemaType.BTC_DECIMAL:
                    return "DECIMAL";
                // float
                case SchemaType.BTC_FLOAT:
                    return "FLOAT";
                // double
                case SchemaType.BTC_DOUBLE:
                    return "DOUBLE";
                // boolean
                case SchemaType.BTC_BOOLEAN:
                    return "BOOLEAN";
                // duration
                case SchemaType.BTC_DURATION:
                    return "DURATION";
                // date time
                case SchemaType.BTC_DATE_TIME:
                    return "DATE_TIME";
                // date
                case SchemaType.BTC_DATE:
                    return "DATE";
                case SchemaType.BTC_TIME:
                    return "TIME";
                case SchemaType.BTC_G_YEAR:
                    return "G_YEAR";
                case SchemaType.BTC_G_YEAR_MONTH:
                    return "G_YEAR_MONTH";
                // ..needs more
                default:
                    break;
            }
            return result;
        }
    }

    @AForm(description = "Add new type", name = "Add new type", helpUrl = HelpUrls.SECURITY_SCANS_OVERVIEW)
    protected interface AddParameterActionDialog {

        @AField(description = "Choose Type", name = "Choose type", type = AFieldType.ENUMERATION)
        public final static String TYPE = "Choose type";

        @AField(description = "Set a value", name = "Value", type = AFieldType.STRING)
        public final static String VALUE = "Value";
    }
}
