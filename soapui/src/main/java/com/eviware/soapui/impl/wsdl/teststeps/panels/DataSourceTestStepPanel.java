/*
 * SoapUI, Copyright (C) 2004-2022 SmartBear Software
 *
 * Licensed under the EUPL, Version 1.1 or - as soon as they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * express or implied. See the Licence for the specific language governing permissions and limitations
 * under the Licence.
 */

package com.eviware.soapui.impl.wsdl.teststeps.panels;

import com.eviware.soapui.impl.wsdl.teststeps.DataSourceTestStep;
import com.eviware.soapui.model.testsuite.TestProperty;
import com.eviware.soapui.ui.support.ModelItemDesktopPanel;

import javax.swing.*;
import java.util.List;
import java.awt.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class DataSourceTestStepPanel extends ModelItemDesktopPanel<DataSourceTestStep> {

    private JComboBox<String> typeCombo;
    private JTextField fileField;
    private JButton browseButton;

    public DataSourceTestStepPanel(DataSourceTestStep testStep) {
        super(testStep);
        buildUI();
        addListeners();
        updateUI();
    }

    public void updateUI() {
        typeCombo.setSelectedItem(getModelItem().getType());
        fileField.setText(getModelItem().getFile());
    }

    private void addListeners() {
        typeCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                getModelItem().setType((String) typeCombo.getSelectedItem());
            }
        });

        fileField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateFile();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateFile();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateFile();
            }

            private void updateFile() {
                getModelItem().setFile(fileField.getText());
            }
        });
    }

    private void buildUI() {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        mainPanel.add(new JLabel("Data Source Type:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        typeCombo = new JComboBox<>(new String[]{"CSV", "JSON", "XML"});
        mainPanel.add(typeCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        mainPanel.add(new JLabel("File:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        fileField = new JTextField(30);
        mainPanel.add(fileField, gbc);

        gbc.gridx = 2;
        gbc.gridy = 1;
        browseButton = new JButton("Browse");
        mainPanel.add(browseButton, gbc);

        browseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                if (fileChooser.showOpenDialog(DataSourceTestStepPanel.this) == JFileChooser.APPROVE_OPTION) {
                    fileField.setText(fileChooser.getSelectedFile().getAbsolutePath());
                }
            }
        });

        JButton loadButton = new JButton("Load");
        gbc.gridx = 2;
        gbc.gridy = 0;
        mainPanel.add(loadButton, gbc);

        setLayout(new BorderLayout());
        add(mainPanel, BorderLayout.NORTH);

        JTable dataTable = new JTable();
        add(new JScrollPane(dataTable), BorderLayout.CENTER);

        loadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    getModelItem().run(null, null);
                    List<TestProperty> properties = getModelItem().getPropertyList();
                    if (properties.isEmpty()) {
                        return;
                    }
                    String[] columnNames = new String[properties.size()];
                    for (int i = 0; i < properties.size(); i++) {
                        columnNames[i] = properties.get(i).getName();
                    }
                    String[][] data = new String[1][properties.size()];
                    for (int i = 0; i < properties.size(); i++) {
                        data[0][i] = properties.get(i).getValue();
                    }
                    dataTable.setModel(new javax.swing.table.DefaultTableModel(data, columnNames));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }
}
