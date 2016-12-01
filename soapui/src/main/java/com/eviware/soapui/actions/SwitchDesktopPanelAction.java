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

import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.ui.JDesktopPanelsList;
import com.eviware.soapui.ui.desktop.DesktopPanel;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class SwitchDesktopPanelAction extends AbstractAction {
    private JDialog dialog;
    private final JDesktopPanelsList desktopPanelsList;

    public SwitchDesktopPanelAction(JDesktopPanelsList desktopPanelsList) {
        super("Switch Window");
        this.desktopPanelsList = desktopPanelsList;

        putValue(SHORT_DESCRIPTION, "Prompts to switch to an open editor window");
        putValue(ACCELERATOR_KEY, UISupport.getKeyStroke("menu W"));
    }

    public void actionPerformed(ActionEvent e) {
        if (dialog == null) {
            desktopPanelsList.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));

            dialog = new JDialog(UISupport.getMainFrame(), "Switch Window", false);
            dialog.getContentPane().add(UISupport.buildDescription(null, "Select the window to switch to below", null),
                    BorderLayout.NORTH);
            dialog.getContentPane().add(desktopPanelsList, BorderLayout.CENTER);

            UISupport.initDialogActions(null, dialog);
            dialog.addWindowListener(new WindowAdapter() {
                @Override
                public void windowOpened(WindowEvent e) {
                    initOnOpen();
                }

                private void initOnOpen() {
                    SwingUtilities.invokeLater(new Runnable() {

                        public void run() {
                            desktopPanelsList.getDesktopPanelsList().requestFocus();
                            if (desktopPanelsList.getDesktopPanels().size() > 0) {
                                desktopPanelsList.getDesktopPanelsList().setSelectedIndex(0);
                            }
                        }
                    });
                }

                @Override
                public void windowDeactivated(WindowEvent e) {
                    dialog.setVisible(false);
                }

                @Override
                public void windowLostFocus(WindowEvent e) {
                    dialog.setVisible(false);
                }

            });
            dialog.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    dialog.setVisible(false);
                }
            });

            desktopPanelsList.getDesktopPanelsList().addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyChar() == '\n') {
                        DesktopPanel dp = (DesktopPanel) desktopPanelsList.getDesktopPanelsList().getSelectedValue();
                        if (dp != null) {
                            UISupport.showDesktopPanel(dp);
                            dialog.setVisible(false);
                        }
                    }
                }
            });

            desktopPanelsList.getDesktopPanelsList().addMouseListener(new MouseAdapter() {

                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() > 1) {
                        DesktopPanel dp = (DesktopPanel) desktopPanelsList.getDesktopPanelsList().getSelectedValue();
                        if (dp != null) {
                            UISupport.showDesktopPanel(dp);
                            dialog.setVisible(false);
                        }
                    }
                }
            });
        }

        dialog.setSize(new Dimension(300, 120 + desktopPanelsList.getItemsCount() * 20));

        UISupport.showDialog(dialog);
    }
}
