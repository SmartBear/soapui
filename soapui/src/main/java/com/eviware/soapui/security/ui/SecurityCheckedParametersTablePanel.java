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

import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.model.security.SecurityParametersTableModel;
import com.eviware.soapui.model.testsuite.TestProperty;
import com.eviware.soapui.security.actions.CloneParametersAction;
import com.eviware.soapui.security.scan.AbstractSecurityScanWithProperties;
import com.eviware.soapui.security.scan.BoundarySecurityScan;
import com.eviware.soapui.security.scan.InvalidTypesSecurityScan;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.swing.DefaultActionList;
import com.eviware.soapui.support.components.JUndoableTextArea;
import com.eviware.soapui.support.components.JUndoableTextField;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.support.swing.JTableFactory;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.XFormField;
import com.eviware.x.form.XFormFieldListener;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AField.AFieldType;
import com.eviware.x.form.support.AForm;
import com.eviware.x.impl.swing.JComboBoxFormField;
import com.eviware.x.impl.swing.JFormDialog;
import com.eviware.x.impl.swing.JTextFieldFormField;
import com.eviware.x.impl.swing.SwingXFormDialog;
import org.jdesktop.swingx.JXTable;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("serial")
public class SecurityCheckedParametersTablePanel extends JPanel implements ListSelectionListener {

    protected static final int LABEL_NAME_COLUMN_WIDTH = 120;
    protected static final int ENABLE_COLUMN_WIDTH = 70;
    static final String CHOOSE_TEST_PROPERTY = "Choose Test Property";
    protected SecurityParametersTableModel model;
    protected JXToolBar toolbar;
    protected JXTable table;
    protected Map<String, TestProperty> properties;
    protected DefaultActionList actionList;
    protected JUndoableTextArea pathPane;
    protected XFormDialog dialog;
    protected AbstractSecurityScanWithProperties securityScan;

    public SecurityCheckedParametersTablePanel(SecurityParametersTableModel model,
                                               Map<String, TestProperty> properties, AbstractSecurityScanWithProperties securityCheck) {
        this.securityScan = securityCheck;
        this.model = model;
        initRequestPartProperties(properties);
        init();
        defineColumnWidth();
        setPreferredSize(new Dimension(100, 100));
    }

    private void initRequestPartProperties(Map<String, TestProperty> properties) {
        this.properties = new HashMap<String, TestProperty>();
        for (Map.Entry<String, TestProperty> entry : properties.entrySet()) {
            if (properties.get(entry.getKey()).isRequestPart()) {
                this.properties.put(entry.getKey(), entry.getValue());
            }
        }
    }

    protected void init() {

        setLayout(new BorderLayout());
        toolbar = UISupport.createToolbar();

        toolbar.add(UISupport.createToolbarButton(new AddNewParameterAction()));
        toolbar.add(UISupport.createToolbarButton(new RemoveParameterAction()));
        toolbar.add(UISupport.createToolbarButton(new CopyParameterAction()));
        toolbar.add(UISupport.createToolbarButton(new CloneParametersAction(securityScan)));
        toolbar.addGlue();

        add(toolbar, BorderLayout.NORTH);
        table = JTableFactory.getInstance().makeJXTable(model);
        table.getSelectionModel().addListSelectionListener(this);
        defineColumnWidth();
        table.setDefaultEditor(String.class, getDefaultCellEditor());

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        pathPane = new JUndoableTextArea();
        if (securityScan instanceof BoundarySecurityScan) {
            ((BoundarySecurityScan) securityScan).refreshRestrictionLabel(-1);
        }
        if (securityScan instanceof InvalidTypesSecurityScan) {
            ((InvalidTypesSecurityScan) securityScan).refreshRestrictionLabel(-1);
        }
    }

    /**
     *
     */
    protected void defineColumnWidth() {
        // enable column
        TableColumn col = table.getColumnModel().getColumn(3);
        col.setMaxWidth(ENABLE_COLUMN_WIDTH);
        col.setPreferredWidth(ENABLE_COLUMN_WIDTH);
        col.setMinWidth(ENABLE_COLUMN_WIDTH);
        // label
        col = table.getColumnModel().getColumn(0);
        col.setMaxWidth(LABEL_NAME_COLUMN_WIDTH);
        col.setPreferredWidth(LABEL_NAME_COLUMN_WIDTH);
        col.setMinWidth(LABEL_NAME_COLUMN_WIDTH);
        // name
        col = table.getColumnModel().getColumn(1);
        col.setMaxWidth(LABEL_NAME_COLUMN_WIDTH);
        col.setPreferredWidth(LABEL_NAME_COLUMN_WIDTH);
        col.setMinWidth(LABEL_NAME_COLUMN_WIDTH);
    }

    /**
     * this will return cell editor when editing xpath
     *
     * @return
     */
    protected TableCellEditor getDefaultCellEditor() {
        return new XPathCellRender();
    }

    public XFormDialog getDialog() {
        return dialog;
    }

    /*
     * Creates dialog
     */
    protected XFormDialog createAddParameterDialog() {
        actionList = new DefaultActionList();
        AddAction addAction = new AddAction();
        actionList.addAction(addAction, true);
        AddAndCopy addAndCopy = new AddAndCopy();
        actionList.addAction(addAndCopy);
        Close closeAction = new Close();
        actionList.addAction(closeAction);

        dialog = ADialogBuilder.buildDialog(AddParameterDialog.class, actionList, false);

        dialog.getFormField(AddParameterDialog.PATH).setProperty("component", buildPathSelector());

        closeAction.setDialog(dialog);
        addAction.setDialog(dialog);
        addAndCopy.setDialog(dialog);

        final JTextFieldFormField labelField = (JTextFieldFormField) dialog.getFormField(AddParameterDialog.LABEL);
        labelField.getComponent().setColumns(30);
        labelField.setEnabled(false);
        JComboBoxFormField nameField = (JComboBoxFormField) dialog.getFormField(AddParameterDialog.NAME);
        enablePathField(false);
        nameField.addFormFieldListener(new XFormFieldListener() {

            @Override
            public void valueChanged(XFormField sourceField, String newValue, String oldValue) {
                if (!newValue.equals(CHOOSE_TEST_PROPERTY)) {
                    labelField.setEnabled(true);
                    enablePathField(true);
                } else {
                    labelField.setEnabled(false);
                    enablePathField(false);
                }

            }
        });
        ArrayList<String> options = new ArrayList<String>();
        options.add(CHOOSE_TEST_PROPERTY);
        options.addAll(properties.keySet());
        nameField.setOptions(options.toArray(new String[0]));

        ((JFormDialog) dialog).getDialog().setResizable(false);

        return dialog;
    }

    protected JPanel buildPathSelector() {
        JPanel sourcePanel = new JPanel(new BorderLayout());
        sourcePanel.add(new JScrollPane(pathPane), BorderLayout.CENTER);
        sourcePanel.setBorder(BorderFactory.createEmptyBorder(0, 3, 3, 3));
        return sourcePanel;
    }

    /**
     * @param pathField
     */
    protected void enablePathField(boolean enable) {
        pathPane.setEnabled(enable);
    }

    class AddNewParameterAction extends AbstractAction {
        public AddNewParameterAction() {
            putValue(Action.SMALL_ICON, UISupport.createImageIcon("/add.png"));
            putValue(Action.SHORT_DESCRIPTION, "Adds a parameter to security scan");
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            XFormDialog dialog = createAddParameterDialog();
            dialog.show();
            model.fireTableDataChanged();
        }
    }

    class RemoveParameterAction extends AbstractAction {
        public RemoveParameterAction() {
            putValue(Action.SMALL_ICON, UISupport.createImageIcon("/delete.png"));
            putValue(Action.SHORT_DESCRIPTION, "Removes parameter from security scan");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            model.removeRows(table.getSelectedRows());
            model.fireTableDataChanged();
        }

    }

    public class AddAndCopy extends AbstractAction {

        private XFormDialog dialog;

        public AddAndCopy() {
            super("Add&Copy");
        }

        public void setDialog(XFormDialog dialog) {
            this.dialog = dialog;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (dialog.getValue(AddParameterDialog.LABEL) == null
                    || dialog.getValue(AddParameterDialog.LABEL).trim().length() == 0) {
                UISupport.showErrorMessage("Label is required!");
            } else {
                if (!model.addParameter(dialog.getValue(AddParameterDialog.LABEL),
                        dialog.getValue(AddParameterDialog.NAME), pathPane.getText())) {
                    UISupport.showErrorMessage("Label have to be unique!");
                }
            }
        }

    }

    private class Close extends AbstractAction {

        private XFormDialog dialog;

        public Close() {
            super("Close");
        }

        public void setDialog(XFormDialog dialog) {
            this.dialog = dialog;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (dialog != null) {
                ((SwingXFormDialog) dialog).setReturnValue(XFormDialog.CANCEL_OPTION);

                JComboBoxFormField nameField = (JComboBoxFormField) dialog.getFormField(AddParameterDialog.NAME);
                nameField.setSelectedOptions(new Object[]{nameField.getOptions()[0]});
                dialog.setValue(AddParameterDialog.LABEL, "");
                pathPane.setText("");

                dialog.setVisible(false);
            }

        }

    }

    class CopyParameterAction extends AbstractAction {

        public CopyParameterAction() {
            putValue(Action.SMALL_ICON, UISupport.createImageIcon("/copy.png"));
            putValue(Action.SHORT_DESCRIPTION, "Copies parameter");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (table.getSelectedRow() > -1) {
                XFormDialog dialog = createAddParameterDialog();

                int row = table.getSelectedRow();
                initDialogForCopy(dialog, row);

                dialog.show();
                model.fireTableDataChanged();
            }
        }

    }

    private void initDialogForCopy(XFormDialog dialog, int row) {
        dialog.setValue(AddParameterDialog.LABEL, (String) model.getValueAt(row, 0));
        dialog.setValue(AddParameterDialog.NAME, (String) model.getValueAt(row, 1));
        pathPane.setText((String) model.getValueAt(row, 2));
    }

    public JUndoableTextArea getPathPane() {
        return pathPane;
    }

    private class AddAction extends AbstractAction {

        private XFormDialog dialog;

        public AddAction() {
            super("Add");
        }

        public void setDialog(XFormDialog dialog) {
            this.dialog = dialog;
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            if (dialog.getValue(AddParameterDialog.LABEL) == null
                    || dialog.getValue(AddParameterDialog.LABEL).trim().length() == 0) {
                UISupport.showErrorMessage("Label is required!");
            } else {
                if (model.addParameter(dialog.getValue(AddParameterDialog.LABEL),
                        dialog.getValue(AddParameterDialog.NAME), pathPane.getText())) {
                    JComboBoxFormField nameField = (JComboBoxFormField) dialog.getFormField(AddParameterDialog.NAME);
                    nameField.setSelectedOptions(new Object[]{nameField.getOptions()[0]});
                    dialog.setValue(AddParameterDialog.LABEL, "");
                    pathPane.setText("");
                } else {
                    UISupport.showErrorMessage("Label have to be unique!");
                }
            }
        }

    }

    public JUndoableTextField getLabel() {
        return ((JTextFieldFormField) dialog.getFormField(AddParameterDialog.LABEL)).getComponent();
    }

    @AForm(description = "Add New Security Test Step Parameter", name = "Configure Security Test Step Parameters", helpUrl = HelpUrls.SECURITY_SCANS_OVERVIEW)
    interface AddParameterDialog {
        @AField(description = "Parameter Name", name = "Parameter Name", type = AFieldType.ENUMERATION)
        static String NAME = "Parameter Name";

        @AField(description = "Parameter Label", name = "Parameter Label", type = AFieldType.STRING)
        static String LABEL = "Parameter Label";

        @AField(description = "Parameter XPath", name = "XPath", type = AFieldType.COMPONENT)
        static String PATH = "XPath";
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event
     * .ListSelectionEvent)
     */
    @Override
    public void valueChanged(ListSelectionEvent lse) {
        DefaultListSelectionModel dlsm = ((DefaultListSelectionModel) lse.getSource());
        if (securityScan instanceof BoundarySecurityScan) {
            ((BoundarySecurityScan) securityScan).refreshRestrictionLabel(dlsm.getAnchorSelectionIndex());
        }
        if (securityScan instanceof InvalidTypesSecurityScan) {
            ((InvalidTypesSecurityScan) securityScan).refreshRestrictionLabel(dlsm.getAnchorSelectionIndex());
        }
    }
}
