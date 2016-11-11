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

package com.eviware.soapui.support.editor.inspectors.auth;

import com.eviware.soapui.config.TimeUnitConfig;
import com.eviware.soapui.impl.rest.OAuth2Profile;
import org.apache.commons.lang.WordUtils;

import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

class ExpirationTimeChooser extends JPanel {

    static final String SERVER_EXPIRATION_RADIO_NAME = "serverExpirationRadio";
    static final String MANUAL_EXPIRATION_RADIO_NAME = "manualExpirationRadio";
    static final String TIME_FIELD_NAME = "timeField";
    static final String TIME_UNIT_COMBO_NAME = "timeUnitCombo";
    private static final String[] TIME_UNIT_OPTIONS = new String[]{"Seconds",
            "Minutes", "Hours"};
    private static final int TIME_FIELD_CHARACTER_LIMIT = 9;

    private JRadioButton serverExpirationTimeOption;
    private JRadioButton manualExpirationTimeOption;
    private JTextField timeTextField;
    private JComboBox timeUnitCombo;
    private OAuth2Profile profile;

    ExpirationTimeChooser(OAuth2Profile profile) {
        this.profile = profile;
        setLayout(new BorderLayout(0, 0));
        initializeRadioButtons();
        JPanel timeSelectionPanel = createTimeSelectionPanel();
        JPanel northPanel = new JPanel(new GridLayout(3, 1, 0, 0));
        northPanel.add(serverExpirationTimeOption);
        northPanel.add(manualExpirationTimeOption);
        northPanel.add(timeSelectionPanel);
        add(northPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout(0, 0));
        JLabel label = new JLabel("<html>Here you can set an expiry time if the OAuth 2 server doesn't,<br/>so that the token retrieval can be automated.</html>");
        label.setForeground(new Color(143, 143, 143));
        centerPanel.add(label, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);

//		JLabel helpLink = UISupport.createLabelLink( "http://www.soapui.org", "Learn how to use the token expiration time " );
//		add( helpLink, BorderLayout.SOUTH );
    }

    public String getAccessTokenExpirationTime() {
        return timeTextField.getText();
    }

    public TimeUnitConfig.Enum getAccessTokenExpirationTimeUnit() {
        String timeUnitString = timeUnitCombo.getSelectedItem().toString().toUpperCase();
        return TimeUnitConfig.Enum.forString(timeUnitString);
    }

    public boolean manualExpirationTimeIsSelected() {
        return manualExpirationTimeOption.isSelected();
    }

    private JPanel createTimeSelectionPanel() {
        JPanel timeSelectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 0));

        boolean enableManualTimeControls = profile.useManualAccessTokenExpirationTime();

        timeTextField = createTimeTextField(enableManualTimeControls);
        timeUnitCombo = createTimeUnitCombo(enableManualTimeControls);

        timeSelectionPanel.add(timeTextField);
        timeSelectionPanel.add(timeUnitCombo);

        return timeSelectionPanel;
    }

    private JTextField createTimeTextField(boolean enableManualTimeControls) {
        JTextField timeTextField = new JTextField(5);
        timeTextField.setName(TIME_FIELD_NAME);
        timeTextField.setHorizontalAlignment(JTextField.RIGHT);

        timeTextField.setEnabled(enableManualTimeControls);

        String manualAccessTokenExpirationTime = profile.getManualAccessTokenExpirationTime();
        if (manualAccessTokenExpirationTime == null) {
            timeTextField.setText("");
        } else {
            timeTextField.setText(manualAccessTokenExpirationTime);
        }
        return timeTextField;
    }

    private JComboBox createTimeUnitCombo(boolean enableManualTimeControls) {
        JComboBox timeUnitCombo = new JComboBox(TIME_UNIT_OPTIONS);
        timeUnitCombo.setName(TIME_UNIT_COMBO_NAME);
        timeUnitCombo.setEnabled(enableManualTimeControls);


        TimeUnitConfig.Enum timeUnit = profile.getManualAccessTokenExpirationTimeUnit();
        timeUnitCombo.setSelectedItem(WordUtils.capitalize(timeUnit.toString().toLowerCase()));

        return timeUnitCombo;
    }

    private void initializeRadioButtons() {
        long serverIssuedExpirationTime = profile.getAccessTokenExpirationTime();
        String serverIssuedExpirationTimeLabel;
        if (serverIssuedExpirationTime > 0) {
            serverIssuedExpirationTimeLabel = getMostLegibleTimeString(serverIssuedExpirationTime);
        } else {
            serverIssuedExpirationTimeLabel = "No expiration.";
        }

        serverExpirationTimeOption = new JRadioButton("Use expiration time from authorization server: " + serverIssuedExpirationTimeLabel);
        serverExpirationTimeOption.setName(SERVER_EXPIRATION_RADIO_NAME);
        ActionListener checkBoxMonitor = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                timeTextField.setEnabled(manualExpirationTimeOption.isSelected());
                timeUnitCombo.setEnabled(manualExpirationTimeOption.isSelected());
            }
        };
        serverExpirationTimeOption.addActionListener(checkBoxMonitor);

        manualExpirationTimeOption = new JRadioButton("Custom");
        manualExpirationTimeOption.setName(MANUAL_EXPIRATION_RADIO_NAME);
        manualExpirationTimeOption.addActionListener(checkBoxMonitor);

        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(serverExpirationTimeOption);
        buttonGroup.add(manualExpirationTimeOption);

        if (profile.useManualAccessTokenExpirationTime()) {
            manualExpirationTimeOption.setSelected(true);
        } else {
            serverExpirationTimeOption.setSelected(true);
        }
    }

    private String getMostLegibleTimeString(long seconds) {
        if (seconds % 3600 == 0) {
            return seconds / 3600 + " hour(s)";
        } else if (seconds % 60 == 0) {
            return seconds / 60 + " minute(s)";
        } else {
            return seconds + " second(s)";
        }
    }
}
