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

package com.eviware.soapui.actions;

import com.eviware.soapui.impl.wsdl.panels.teststeps.support.DefaultPropertyHolderTableModel;
import com.eviware.soapui.impl.wsdl.panels.teststeps.support.PropertyHolderTable;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.support.SecurityScanUtil;
import com.eviware.soapui.support.components.SimpleForm;
import com.eviware.soapui.support.types.StringToStringMap;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.TransferHandler;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.Dimension;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;

public class SecurityScansPrefs implements Prefs {

    public static final String GLOBAL_SENSITIVE_INFORMATION_TOKENS = "Global Sensitive Information Tokens";
    private SimpleForm securityScansForm;
    private final String title;

    public SecurityScansPrefs(String title) {
        this.title = title;
    }

    public SimpleForm getForm() {
        if (securityScansForm == null) {
            securityScansForm = new SimpleForm();

            PropertyHolderTable propertyHolderTable = new PropertyHolderTable(
                    SecurityScanUtil.getGlobalSensitiveInformationExposureTokens()) {
                protected JTable buildPropertiesTable() {
                    propertiesModel = new DefaultPropertyHolderTableModel(holder) {
                        @Override
                        public String getColumnName(int columnIndex) {
                            switch (columnIndex) {
                                case 0:
                                    return "Token";
                                case 1:
                                    return "Description";
                            }

                            return null;
                        }

                    };
                    propertiesTable = new PropertiesHolderJTable();
                    propertiesTable.setSurrendersFocusOnKeystroke(true);

                    propertiesTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
                    propertiesTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
                        public void valueChanged(ListSelectionEvent e) {
                            int selectedRow = propertiesTable.getSelectedRow();
                            if (removePropertyAction != null) {
                                removePropertyAction.setEnabled(selectedRow != -1);
                            }

                            if (movePropertyUpAction != null) {
                                movePropertyUpAction.setEnabled(selectedRow > 0);
                            }

                            if (movePropertyDownAction != null) {
                                movePropertyDownAction.setEnabled(selectedRow >= 0
                                        && selectedRow < propertiesTable.getRowCount() - 1);
                            }
                        }
                    });

                    propertiesTable.setDragEnabled(true);
                    propertiesTable.setTransferHandler(new TransferHandler("testProperty"));

                    if (getHolder().getModelItem() != null) {
                        DropTarget dropTarget = new DropTarget(propertiesTable,
                                new PropertyHolderTablePropertyExpansionDropTarget());
                        dropTarget.setDefaultActions(DnDConstants.ACTION_COPY_OR_MOVE);
                    }

                    return propertiesTable;
                }
            };
            propertyHolderTable.setPreferredSize(new Dimension(200, 300));
            securityScansForm.append(new JLabel(title));
            securityScansForm.addSpace();
            securityScansForm.addComponent(propertyHolderTable);
        }

        return securityScansForm;
    }

    public void getFormValues(Settings settings) {
        SecurityScanUtil.saveGlobalSecuritySettings();
    }

    public String getTitle() {
        return GLOBAL_SENSITIVE_INFORMATION_TOKENS;
    }

    public StringToStringMap getValues(Settings settings) {
        return null;
    }

    public void setFormValues(Settings settings) {

    }

    public void storeValues(StringToStringMap values, Settings settings) {
    }
}
