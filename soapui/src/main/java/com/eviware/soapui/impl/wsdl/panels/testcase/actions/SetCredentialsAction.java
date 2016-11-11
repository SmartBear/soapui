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

package com.eviware.soapui.impl.wsdl.panels.testcase.actions;

import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestRequestStep;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.SimpleForm;
import com.jgoodies.forms.factories.ButtonBarFactory;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

/**
 * Set the credentials for all requests in a testcase
 *
 * @author Ole.Matzura
 */

public class SetCredentialsAction extends AbstractAction {
    private final WsdlTestCase testCase;
    private JDialog dialog;
    private SimpleForm form;

    private static final String DOMAIN = "Domain";
    private static final String PASSWORD = "Password";
    private static final String USERNAME = "Username";

    public SetCredentialsAction(WsdlTestCase testCase) {
        this.testCase = testCase;
        putValue(Action.SMALL_ICON, UISupport.createImageIcon("/set_credentials.png"));
        putValue(Action.SHORT_DESCRIPTION, "Sets the credentials for all requests in this testcase");
    }

    public void actionPerformed(ActionEvent e) {
        if (dialog == null) {
            buildDialog();
        }

        UISupport.showDialog(dialog);
    }

    private void buildDialog() {
        dialog = new JDialog(UISupport.getMainFrame(), "Set TestCase Credentials");
        form = new SimpleForm();
        form.appendTextField(USERNAME, "Username to use for authentication");
        form.appendPasswordField(PASSWORD, "Password to use for authentication");
        form.appendTextField(DOMAIN, "Domain to specify (for NTLM)");
        form.getPanel().setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(form.getPanel(), BorderLayout.CENTER);

        JPanel buttonBar = ButtonBarFactory.buildOKCancelBar(new JButton(new OkAction()), new JButton(
                new CancelAction()));
        panel.add(buttonBar, BorderLayout.SOUTH);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setPreferredSize(new Dimension(270, (int) panel.getPreferredSize().getHeight()));

        dialog.getContentPane().add(panel);
        dialog.pack();
    }

    private class OkAction extends AbstractAction {
        public OkAction() {
            super("OK");
        }

        public void actionPerformed(ActionEvent e) {
            for (int c = 0; c < testCase.getTestStepCount(); c++) {
                TestStep step = testCase.getTestStepAt(c);
                if (step instanceof WsdlTestRequestStep) {
                    WsdlTestRequestStep requestStep = (WsdlTestRequestStep) step;
                    requestStep.getTestRequest().setUsername(form.getComponentValue(USERNAME));
                    requestStep.getTestRequest().setPassword(form.getComponentValue(PASSWORD));
                    requestStep.getTestRequest().setDomain(form.getComponentValue(DOMAIN));
                }
            }

            dialog.setVisible(false);
        }
    }

    private class CancelAction extends AbstractAction {
        public CancelAction() {
            super("Cancel");
        }

        public void actionPerformed(ActionEvent e) {
            dialog.setVisible(false);
        }
    }
}
