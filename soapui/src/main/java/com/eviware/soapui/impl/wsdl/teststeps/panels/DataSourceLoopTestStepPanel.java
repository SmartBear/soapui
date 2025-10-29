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

import com.eviware.soapui.impl.wsdl.teststeps.DataSourceLoopTestStep;
import com.eviware.soapui.ui.support.ModelItemDesktopPanel;

import javax.swing.*;
import java.awt.*;

import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.DataSourceTestStep;
import com.eviware.soapui.model.testsuite.TestStep;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class DataSourceLoopTestStepPanel extends ModelItemDesktopPanel<DataSourceLoopTestStep> {

    private JComboBox<String> dataSourceStepCombo;

    public DataSourceLoopTestStepPanel(DataSourceLoopTestStep testStep) {
        super(testStep);
        buildUI();
        addListeners();
        updateUI();
    }

    public void updateUI() {
        dataSourceStepCombo.setSelectedItem(getModelItem().getDataSourceStep());
    }

    private void addListeners() {
        dataSourceStepCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                getModelItem().setDataSourceStep((String) dataSourceStepCombo.getSelectedItem());
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
        mainPanel.add(new JLabel("Data Source Step:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        dataSourceStepCombo = new JComboBox<>(getDataSourceStepNames());
        mainPanel.add(dataSourceStepCombo, gbc);

        setLayout(new BorderLayout());
        add(mainPanel, BorderLayout.CENTER);
    }

    private String[] getDataSourceStepNames() {
        List<String> names = new ArrayList<>();
        WsdlTestCase testCase = getModelItem().getTestCase();
        for (TestStep testStep : testCase.getTestStepList()) {
            if (testStep instanceof DataSourceTestStep) {
                names.add(testStep.getName());
            }
        }
        return names.toArray(new String[0]);
    }
}
