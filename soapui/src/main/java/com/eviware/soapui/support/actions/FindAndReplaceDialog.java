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

package com.eviware.soapui.support.actions;

import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.xml.ProxyFindAndReplacable;
import com.jgoodies.forms.builder.ButtonBarBuilder;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;

/**
 * Find-and-Replace dialog for a JXmlTextArea
 *
 * @author Ole.Matzura
 */

public class FindAndReplaceDialog extends AbstractAction {
    private final ProxyFindAndReplacable target;
    private JDialog dialog;
    private JCheckBox caseCheck;
    private JRadioButton allButton;
    private JRadioButton selectedLinesButton;
    private JRadioButton forwardButton;
    private JRadioButton backwardButton;
    private JCheckBox wholeWordCheck;
    private JButton findButton;
    private JButton replaceButton;
    private JButton replaceAllButton;
    private JComboBox findCombo;
    private JComboBox replaceCombo;
    private JCheckBox wrapCheck;

    public FindAndReplaceDialog(FindAndReplaceable target) {
        super("Find / Replace");
        if (UISupport.isMac()) {
            putValue(Action.ACCELERATOR_KEY, UISupport.getKeyStroke("meta F"));
        } else {
            putValue(Action.ACCELERATOR_KEY, UISupport.getKeyStroke("control F"));
        }
        this.target = new ProxyFindAndReplacable(target);
    }

    public void actionPerformed(ActionEvent e) {
        show();
    }

    public void show() {
        if (dialog == null) {
            buildDialog();
        }

        target.getEditComponent().requestFocusInWindow();

        replaceCombo.setEnabled(target.isEditable());
        replaceAllButton.setEnabled(target.isEditable());
        replaceButton.setEnabled(target.isEditable());

        UISupport.showDialog(dialog);
        findCombo.getEditor().selectAll();
        findCombo.requestFocus();
    }

    private void buildDialog() {
        Window window = SwingUtilities.windowForComponent(target.getEditComponent());

        dialog = new JDialog(window, "Find / Replace");
        dialog.setModal(false);

        JPanel panel = new JPanel(new BorderLayout());
        findCombo = new JComboBox();
        findCombo.setEditable(true);
        replaceCombo = new JComboBox();
        replaceCombo.setEditable(true);

        // create inputs
        GridLayout gridLayout = new GridLayout(2, 2);
        gridLayout.setVgap(5);
        JPanel inputPanel = new JPanel(gridLayout);
        inputPanel.add(new JLabel("Find:"));
        inputPanel.add(findCombo);
        inputPanel.add(new JLabel("Replace with:"));
        inputPanel.add(replaceCombo);
        inputPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        // create direction panel
        ButtonGroup directionGroup = new ButtonGroup();
        forwardButton = new JRadioButton("Forward", true);
        forwardButton.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        directionGroup.add(forwardButton);
        backwardButton = new JRadioButton("Backward");
        backwardButton.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        directionGroup.add(backwardButton);

        JPanel directionPanel = new JPanel(new GridLayout(2, 1));
        directionPanel.add(forwardButton);
        directionPanel.add(backwardButton);
        directionPanel.setBorder(BorderFactory.createTitledBorder("Direction"));

        // create scope panel
        ButtonGroup scopeGroup = new ButtonGroup();
        allButton = new JRadioButton("All", true);
        allButton.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        selectedLinesButton = new JRadioButton("Selected Lines");
        selectedLinesButton.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        scopeGroup.add(allButton);
        scopeGroup.add(selectedLinesButton);

        JPanel scopePanel = new JPanel(new GridLayout(2, 1));
        scopePanel.add(allButton);
        scopePanel.add(selectedLinesButton);
        scopePanel.setBorder(BorderFactory.createTitledBorder("Scope"));

        // create options
        caseCheck = new JCheckBox("Case Sensitive");
        caseCheck.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        wholeWordCheck = new JCheckBox("Whole Word");
        wholeWordCheck.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        wrapCheck = new JCheckBox("Wrap Search");
        wrapCheck.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        JPanel optionsPanel = new JPanel(new GridLayout(3, 1));
        optionsPanel.add(caseCheck);
        optionsPanel.add(wholeWordCheck);
        optionsPanel.add(wrapCheck);
        optionsPanel.setBorder(BorderFactory.createTitledBorder("Options"));

        // create panel with options
        JPanel options = new JPanel(new GridLayout(1, 2));

        JPanel radios = new JPanel(new GridLayout(2, 1));
        radios.add(directionPanel);
        radios.add(scopePanel);

        options.add(optionsPanel);
        options.add(radios);
        options.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));

        // create buttons
        ButtonBarBuilder builder = new ButtonBarBuilder();
        findButton = new JButton(new FindAction());
        builder.addFixed(findButton);
        builder.addRelatedGap();
        replaceButton = new JButton(new ReplaceAction());
        builder.addFixed(replaceButton);
        builder.addRelatedGap();
        replaceAllButton = new JButton(new ReplaceAllAction());
        builder.addFixed(replaceAllButton);
        builder.addUnrelatedGap();
        builder.addFixed(new JButton(new CloseAction()));
        builder.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        // tie it up!
        panel.add(inputPanel, BorderLayout.NORTH);
        panel.add(options, BorderLayout.CENTER);
        panel.add(builder.getPanel(), BorderLayout.SOUTH);

        dialog.getContentPane().add(panel);
        dialog.pack();
        UISupport.initDialogActions(dialog, null, findButton);
    }

    private int findNext(int pos, String txt, String value) {
        int ix = forwardButton.isSelected() ? txt.indexOf(value, pos) : txt.lastIndexOf(value, pos);

        if (selectedLinesButton.isSelected() && (ix < target.getSelectionStart() || ix > target.getSelectionEnd())) {
            ix = -1;
        }

        if (wholeWordCheck.isSelected()) {
            while (ix != -1
                    && ((ix > 0 && Character.isLetterOrDigit(txt.charAt(ix - 1))) || (ix < txt.length()
                    - value.length() - 1 && Character.isLetterOrDigit(txt.charAt(ix + value.length()))))) {
                ix = forwardButton.isSelected() ? ++ix : --ix;
                ix = forwardButton.isSelected() ? txt.indexOf(value, ix) : txt.lastIndexOf(value, ix);
            }
        }

        if (ix == -1 && wrapCheck.isSelected()) {
            if (forwardButton.isSelected() && pos > 0) {
                return findNext(0, txt, value);
            } else if (backwardButton.isSelected() && pos < txt.length() - 1) {
                return findNext(txt.length() - 1, txt, value);
            }
        }

        return ix;
    }

    private class FindAction extends AbstractAction {
        String lastSearchedItem = "";
        int lastPositionF = -1;

        public FindAction() {
            super("Find/Find Next");
        }

        public void actionPerformed(ActionEvent e) {
            int pos = tweakPosition();
            int lastpos = tweakLastPosition();

            String txt = target.getText();

            if (findCombo.getSelectedItem() == null) {
                return;
            }
            String value = findCombo.getSelectedItem().toString();
            if (value.length() == 0 || (pos == txt.length() && !wrapCheck.isSelected())) {
                return;
            }

            if (!caseCheck.isSelected()) {
                value = value.toLowerCase();
                txt = txt.toLowerCase();
            }

            if (pos == lastPositionF && value.equals(lastSearchedItem)) {
                if (forwardButton.isSelected()) {
                    pos += value.length() + 1;
                } else {
                    pos -= value.length() - 1;
                }
            }

            int ix = findNext(pos, txt, value);

            lastSearchedItem = value;
            lastPositionF = ix;

            if (ix != -1) {
                if (selectedLinesButton.isSelected()) {
                    target.select(ix, lastpos);
                } else {
                    target.select(ix, ix + value.length());
                }

                for (int c = 0; c < findCombo.getItemCount(); c++) {
                    if (findCombo.getItemAt(c).equals(value)) {
                        findCombo.removeItem(c);
                        break;
                    }
                }

                findCombo.insertItemAt(value, 0);
            } else {
                UISupport.showErrorMessage("String [" + value + "] not found");
            }
        }
    }

    private class ReplaceAction extends AbstractAction {
        String lastSearchedItem = "";
        int lastPositionF = -1;

        public ReplaceAction() {
            super("Replace/Replace Next");
        }

        public void actionPerformed(ActionEvent e) {
            int pos = tweakPosition();
            int lastpos = tweakLastPosition();

            String txt = target.getText();

            if (findCombo.getSelectedItem() == null) {
                return;
            }
            String value = findCombo.getSelectedItem().toString();
            if (value.length() == 0 || txt.length() == 0) {
                return;
            }

            String newValue = replaceCombo.getSelectedItem() == null ? "" : replaceCombo.getSelectedItem().toString();

            if (!caseCheck.isSelected()) {
                if (newValue.equalsIgnoreCase(value)) {
                    return;
                }
                value = value.toLowerCase();
                txt = txt.toLowerCase();
            } else if (newValue.equals(value)) {
                return;
            }

            if (pos == lastPositionF && value.equals(lastSearchedItem)) {
                if (forwardButton.isSelected()) {
                    pos += value.length() + 1;
                } else {
                    pos -= value.length() - 1;
                }
            }

            int ix = findNext(pos, txt, value);

            lastSearchedItem = value;
            lastPositionF = ix;

            int firstIx = ix;
            if (ix != -1) {
                // System.out.println( "found match at " + ix + ", " + firstIx +
                // ", " + valueInNewValueIx );
                target.select(ix, ix + value.length());

                target.setSelectedText(newValue);
                if (selectedLinesButton.isSelected()) {
                    target.select(ix, lastpos);
                } else {
                    target.select(ix, ix + newValue.length());
                }

                // adjust firstix
                if (ix < firstIx) {
                    firstIx += newValue.length() - value.length();
                }

                txt = target.getText();
                if (!caseCheck.isSelected()) {
                    txt = txt.toLowerCase();
                }

                if (forwardButton.isSelected()) {
                    ix = findNext(ix + newValue.length(), txt, value);
                } else {
                    ix = findNext(ix - 1, txt, value);
                }
            } else {
                UISupport.showErrorMessage("String [" + value + "] not found");
            }
        }
    }

    private class ReplaceAllAction extends AbstractAction {
        public ReplaceAllAction() {
            super("Replace All");
        }

        public void actionPerformed(ActionEvent e) {
            int pos = tweakPosition();
            String txt = target.getDialogText();

            if (findCombo.getSelectedItem() == null) {
                return;
            }
            String value = findCombo.getSelectedItem().toString();
            if (value.length() == 0 || txt.length() == 0) {
                return;
            }

            String newValue = replaceCombo.getSelectedItem() == null ? "" : replaceCombo.getSelectedItem().toString();

            if (!caseCheck.isSelected()) {
                if (newValue.equalsIgnoreCase(value)) {
                    return;
                }
                value = value.toLowerCase();
                txt = txt.toLowerCase();
            } else if (newValue.equals(value)) {
                return;
            }

            int ix = findNext(pos, txt, value);
            if (ix >= 0) {

                int firstIx = ix;
                int valueInNewValueIx = !caseCheck.isSelected() ? newValue.toLowerCase().indexOf(value) : newValue
                        .indexOf(value);

                target.setReplaceAll(true);
                target.setSBTarget();
                target.setNewValue(newValue);
                while (ix != -1) {
                    target.select(ix, ix + value.length());
                    target.setSelectedText(newValue);
                    target.select(ix, ix + newValue.length());

                    // adjust firstix
                    if (ix < firstIx) {
                        firstIx += newValue.length() - value.length();
                    }

                    txt = target.getText();
                    if (!caseCheck.isSelected()) {
                        txt = txt.toLowerCase();
                    }

                    if (forwardButton.isSelected()) {
                        ix = findNext(ix + newValue.length(), txt, value);
                    } else {
                        ix = findNext(ix - 1, txt, value);
                    }
                    if (wrapCheck.isSelected() && valueInNewValueIx != -1 && ix == firstIx + valueInNewValueIx) {
                        break;
                    }
                }
                target.flushSBText();
                target.setReplaceAll(false);
                target.setCarretPosition(forwardButton.isSelected());
            } else {
                UISupport.showErrorMessage("String [" + value + "] not found");
            }
        }

    }

    private class CloseAction extends AbstractAction {
        public CloseAction() {
            super("Close");
        }

        public void actionPerformed(ActionEvent e) {
            dialog.setVisible(false);
        }
    }

    private int tweakPosition() {
        int pos = target.getCaretPosition();
        if (selectedLinesButton.isSelected()) {
            if (forwardButton.isSelected()) {
                int selstart = target.getSelectionStart();
                if (selstart < pos && selstart != -1) {
                    pos = selstart;
                }
            } else {
                int selend = target.getSelectionEnd();
                if (selend > pos && selend != -1) {
                    pos = selend;
                }
            }
        } else {
            int selstart = target.getSelectionStart();
            if (selstart < pos && selstart != -1) {
                pos = selstart;
            }
        }
        return pos;
    }

    private int tweakLastPosition() {
        return forwardButton.isSelected() ? target.getSelectionEnd() : target.getSelectionStart();
    }

}
