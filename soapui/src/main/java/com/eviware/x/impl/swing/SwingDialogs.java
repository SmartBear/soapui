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

package com.eviware.x.impl.swing;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.support.DefaultHyperlinkListener;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.ProgressDialog;
import com.eviware.x.dialogs.XDialogs;
import com.eviware.x.dialogs.XProgressDialog;
import com.jgoodies.forms.factories.ButtonBarFactory;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

public class SwingDialogs implements XDialogs {
    private Component parent;
    private JDialog extendedInfoDialog;
    private Boolean extendedInfoResult;

    public SwingDialogs(Component parent) {
        this.parent = parent;
    }

    public void showErrorMessage(final String message) {
        try {
            if (SwingUtilities.isEventDispatchThread()) {
                JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
                    }
                });
            }
        } catch (Exception e) {
            SoapUI.logError(e);
        }
    }

    public boolean confirm(String question, String title) {
        return JOptionPane.showConfirmDialog(this.parent, question, title, JOptionPane.YES_NO_OPTION) == JOptionPane.OK_OPTION;
    }

    @Override
    public boolean confirm(String question, String title, Component parent) {
        return JOptionPane.showConfirmDialog(parent, question, title, JOptionPane.YES_NO_OPTION) == JOptionPane.OK_OPTION;
    }

    public String prompt(String question, String title, String value) {
        return (String) JOptionPane.showInputDialog(parent, question, title, JOptionPane.QUESTION_MESSAGE, null, null,
                value);
    }

    public String prompt(String question, String title) {
        return JOptionPane.showInputDialog(parent, question, title, JOptionPane.QUESTION_MESSAGE);
    }

    public void showInfoMessage(String message) {
        showInfoMessage(message, "Information");
    }

    public void showInfoMessage(final String message, final String title) {
        try {
            if (SwingUtilities.isEventDispatchThread()) {
                JOptionPane.showMessageDialog(parent, message, title, JOptionPane.INFORMATION_MESSAGE);
            } else {
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.INFORMATION_MESSAGE);
                    }
                });
            }
        } catch (Exception e) {
            SoapUI.logError(e);
        }
    }

    public Object prompt(String question, String title, Object[] objects) {
        return JOptionPane.showInputDialog(parent, question, title, JOptionPane.OK_CANCEL_OPTION, null,
                objects, null);
    }

    public Object prompt(String question, String title, Object[] objects, String value) {
        return JOptionPane.showInputDialog(parent, question, title, JOptionPane.OK_CANCEL_OPTION, null,
                objects, value);
    }

    public Boolean confirmOrCancel(String question, String title) {
        int result = JOptionPane.showConfirmDialog(parent, question, title, JOptionPane.YES_NO_CANCEL_OPTION);

        if (result == JOptionPane.CANCEL_OPTION || result == JOptionPane.CLOSED_OPTION) {
            return null;
        }

        return result == JOptionPane.YES_OPTION;
    }

    public int yesYesToAllOrNo(String question, String title) {
        String[] buttons = {"Yes", "Yes to all", "No"};
        return JOptionPane.showOptionDialog(parent, question, title, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, buttons,
                buttons[0]);
    }

    public XProgressDialog createProgressDialog(String label, int length, String initialValue, boolean canCancel) {
        return new ProgressDialog("Progress", label, length, initialValue, canCancel);
    }

    public void showExtendedInfo(final String title, final String description, final String content, final Dimension size) {
        try {
            final JPanel buttonBar = ButtonBarFactory.buildRightAlignedBar(new JButton(new OkAction("OK")));
            if (SwingUtilities.isEventDispatchThread()) {
                showExtendedInfo(title, description, content, buttonBar, size);
            } else {
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        showExtendedInfo(title, description, content, buttonBar, size);
                    }
                });
            }
        } catch (Exception e) {
            SoapUI.logError(e);
        }
    }

    private void showExtendedInfo(String title, String description, String content, JPanel buttonBar, Dimension size) {
        extendedInfoDialog = new JDialog(UISupport.getMainFrame(), title);
        extendedInfoDialog.setModal(true);
        JPanel panel = new JPanel(new BorderLayout());

        if (description != null) {
            panel.add(UISupport.buildDescription(title, description, null), BorderLayout.NORTH);
        }

        JEditorPane editorPane = new JEditorPane("text/html", content);
        editorPane.setCaretPosition(0);
        editorPane.setEditable(false);
        editorPane.addHyperlinkListener(new DefaultHyperlinkListener(editorPane));

        JScrollPane scrollPane = new JScrollPane(editorPane);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5),
                scrollPane.getBorder()));

        panel.add(scrollPane);
        buttonBar.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 5));
        panel.add(buttonBar, BorderLayout.SOUTH);

        extendedInfoDialog.getRootPane().setContentPane(panel);
        if (size == null) {
            extendedInfoDialog.setSize(400, 300);
        } else {
            extendedInfoDialog.setSize(size);
        }

        extendedInfoResult = null;
        UISupport.showDialog(extendedInfoDialog);
    }

    public boolean confirmExtendedInfo(String title, String description, String content, Dimension size) {
        JPanel buttonBar = ButtonBarFactory.buildRightAlignedBar(new JButton(new OkAction("OK")), new JButton(
                new CancelAction("Cancel")));

        showExtendedInfo(title, description, content, buttonBar, size);

        return extendedInfoResult == null ? false : extendedInfoResult;
    }

    public Boolean confirmOrCancleExtendedInfo(String title, String description, String content, Dimension size) {
        JPanel buttonBar = ButtonBarFactory.buildRightAlignedBar(new JButton(new OkAction("Yes")), new JButton(
                new NoAction("No")), new JButton(new CancelAction("Cancel")));

        showExtendedInfo(title, description, content, buttonBar, size);

        return extendedInfoResult;
    }

    private final class OkAction extends AbstractAction {
        public OkAction(String name) {
            super(name);
        }

        public void actionPerformed(ActionEvent e) {
            extendedInfoResult = true;
            extendedInfoDialog.setVisible(false);
        }
    }

    private final class NoAction extends AbstractAction {
        public NoAction(String name) {
            super(name);
        }

        public void actionPerformed(ActionEvent e) {
            extendedInfoResult = false;
            extendedInfoDialog.setVisible(false);
        }
    }

    private final class CancelAction extends AbstractAction {
        public CancelAction(String name) {
            super(name);
        }

        public void actionPerformed(ActionEvent e) {
            extendedInfoResult = null;
            extendedInfoDialog.setVisible(false);
        }
    }

    public String selectXPath(String title, String info, String xml, String xpath) {
        return prompt("Specify XPath expression", "Select XPath", xpath);
    }

    @Override
    public String selectJsonPath(String title, String info, String json, String jsonPath) {
        return prompt("Specify JsonPath expression", "Select JsonPath", jsonPath);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.eviware.x.dialogs.XDialogs#promptPassword(java.lang.String,
     * java.lang.String)
     */
    public char[] promptPassword(String question, String title) {
        JPasswordField passwordField = new JPasswordField();
        passwordField.addAncestorListener(new RequestFocusListener());
        JLabel qLabel = new JLabel(question);
        JOptionPane.showConfirmDialog(parent, new Object[]{qLabel, passwordField}, title,
                JOptionPane.OK_CANCEL_OPTION);
        return passwordField.getPassword();
    }

    /*
     * Used to give focus to password field, instead of the default OK button in
     * the confirmation dialog.
     */
    private static class RequestFocusListener implements AncestorListener {
        public void ancestorAdded(final AncestorEvent e) {
            final AncestorListener al = this;
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    JComponent component = e.getComponent();
                    component.requestFocusInWindow();
                    component.removeAncestorListener(al);
                }
            });
        }

        public void ancestorMoved(AncestorEvent e) {
        }

        public void ancestorRemoved(AncestorEvent e) {
        }
    }
}
