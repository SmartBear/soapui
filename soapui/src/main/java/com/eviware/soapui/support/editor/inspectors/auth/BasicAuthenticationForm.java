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

import com.eviware.soapui.config.CredentialsConfig;
import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.support.components.SimpleBindingForm;
import com.jgoodies.binding.PresentationModel;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class BasicAuthenticationForm<T extends AbstractHttpRequest> extends AbstractAuthenticationForm {
    protected T request;
    private JRadioButton globalButton;
    private JRadioButton preemptiveButton;

    public BasicAuthenticationForm(T request) {
        this.request = request;
    }

    @Override
    protected JPanel buildUI() {
        SimpleBindingForm basicAuthenticationForm = new SimpleBindingForm(new PresentationModel<T>(request));
        populateBasicForm(basicAuthenticationForm);

        JPanel panel = basicAuthenticationForm.getPanel();
        setBorderAndBackgroundColorOnPanel(panel);

        return panel;
    }

    public void setButtonGroupVisibility(boolean visible) {
        globalButton.setVisible(visible);
        preemptiveButton.setVisible(visible);
    }

    protected void populateBasicForm(SimpleBindingForm basicConfigurationForm) {
        initForm(basicConfigurationForm);

        basicConfigurationForm.addSpace(TOP_SPACING);

        basicConfigurationForm.appendTextField("username", "Username", "The username to use for HTTP Authentication");
        basicConfigurationForm.appendPasswordField("password", "Password", "The password to use for HTTP Authentication");
        basicConfigurationForm.appendTextField("domain", "Domain", "The domain to use for Authentication(NTLM/Kerberos)");

        ButtonGroup buttonGroup = new ButtonGroup();
        globalButton = basicConfigurationForm.appendRadioButton("Pre-emptive auth", "Use global preference", buttonGroup, false);
        globalButton.setBackground(CARD_BACKGROUND_COLOR);
        preemptiveButton = basicConfigurationForm.appendRadioButton("", "Authenticate pre-emptively", buttonGroup, false);
        preemptiveButton.setBackground(CARD_BACKGROUND_COLOR);

        selectCorrectRadioButton();

        globalButton.addActionListener(new UseGlobalSettingsRadioButtonListener(globalButton));
        preemptiveButton.addActionListener(new PreemptiveRadioButtonListener(preemptiveButton));
    }

    private void selectCorrectRadioButton() {
        if (request.getPreemptive()) {
            preemptiveButton.setSelected(true);
        } else {
            globalButton.setSelected(true);
        }
    }

    private class PreemptiveRadioButtonListener implements ActionListener {
        private final JRadioButton preemptiveButton;

        public PreemptiveRadioButtonListener(JRadioButton preemptiveButton) {
            this.preemptiveButton = preemptiveButton;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (preemptiveButton.isSelected()) {
                request.setSelectedAuthProfileAndAuthType(AbstractHttpRequest.BASIC_AUTH_PROFILE,
                        CredentialsConfig.AuthType.PREEMPTIVE);
                request.setPreemptive(true);
            }
        }
    }

    private class UseGlobalSettingsRadioButtonListener implements ActionListener {
        private final JRadioButton globalButton;

        public UseGlobalSettingsRadioButtonListener(JRadioButton globalButton) {
            this.globalButton = globalButton;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (globalButton.isSelected()) {
                request.setSelectedAuthProfileAndAuthType(AbstractHttpRequest.BASIC_AUTH_PROFILE,
                        CredentialsConfig.AuthType.GLOBAL_HTTP_SETTINGS);
                request.setPreemptive(false);
            }
        }
    }

}
