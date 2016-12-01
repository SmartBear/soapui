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

package com.eviware.soapui.impl.wsdl.actions.iface.tools.support;

import com.eviware.soapui.support.UISupport;
import com.jgoodies.forms.builder.ButtonBarBuilder;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;

/**
 * Action to display the contents of a generated configuration file
 *
 * @author ole.matzura
 */

public abstract class ShowConfigFileAction extends AbstractAction {
    private ContentDialog dialog;
    private final String title;
    private final String description;

    public ShowConfigFileAction(String title, String description) {
        super("Show Config");

        this.title = title;
        this.description = description;
    }

    public void actionPerformed(ActionEvent e) {
        if (dialog == null) {
            dialog = new ContentDialog(title, description);
        }

        dialog.showDialog();
    }

    protected abstract String getConfigFile();

    public class ContentDialog extends JDialog {
        private JTextArea contentArea;

        public ContentDialog(String title, String description) throws HeadlessException {
            super(UISupport.getMainFrame());
            setTitle(title);
            setModal(true);

            getContentPane().setLayout(new BorderLayout());
            JLabel label = new JLabel(description);
            label.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
            getContentPane().add(label, BorderLayout.NORTH);
            getContentPane().add(buildContent(), BorderLayout.CENTER);

            ButtonBarBuilder builder = ButtonBarBuilder.createLeftToRightBuilder();
            builder.addGlue();
            JButton closeButton = new JButton(new CloseAction());
            builder.addFixed(closeButton);

            builder.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
            getContentPane().add(builder.getPanel(), BorderLayout.SOUTH);

            pack();

            UISupport.initDialogActions(this, null, closeButton);
        }

        public void showDialog() {
            contentArea.setText(getConfigFile());
            setVisible(true);
        }

        private Component buildContent() {
            contentArea = new JTextArea();
            contentArea.setEditable(false);
            contentArea.setBackground(Color.WHITE);
            JScrollPane scrollPane = new JScrollPane(contentArea);
            scrollPane.setPreferredSize(new Dimension(500, 300));

            return UISupport.wrapInEmptyPanel(scrollPane, BorderFactory.createEmptyBorder(10, 10, 10, 10));
        }

        private final class CloseAction extends AbstractAction {
            public CloseAction() {
                super("Close");
            }

            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        }
    }
}
