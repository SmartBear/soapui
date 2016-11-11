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

package com.eviware.soapui.ui.support;

import com.eviware.soapui.support.UISupport;
import com.jgoodies.forms.builder.ButtonBarBuilder;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.SearchContext;
import org.fife.ui.rtextarea.SearchEngine;

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

public class FindAndReplaceDialogView extends AbstractAction {
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
    private RSyntaxTextArea editArea;

    public FindAndReplaceDialogView(RSyntaxTextArea editArea) {
        super("Find / Replace");
        if (UISupport.isMac()) {
            putValue(Action.ACCELERATOR_KEY, UISupport.getKeyStroke("meta F"));
        } else {
            putValue(Action.ACCELERATOR_KEY, UISupport.getKeyStroke("control F"));
        }
        this.editArea = editArea;
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        show();
    }

    public void show() {
        if (dialog == null) {
            buildDialog();
        }

        editArea.requestFocusInWindow();

        replaceCombo.setEnabled(editArea.isEditable());
        replaceAllButton.setEnabled(editArea.isEditable());
        replaceButton.setEnabled(editArea.isEditable());

        UISupport.showDialog(dialog);
        findCombo.getEditor().selectAll();
        findCombo.requestFocus();
    }

    private void buildDialog() {
        Window window = SwingUtilities.windowForComponent(editArea);

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
        JPanel optionsPanel = new JPanel(new GridLayout(2, 1));
        optionsPanel.add(caseCheck);
        optionsPanel.add(wholeWordCheck);
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
        findButton = new JButton(new FindAction(findCombo));
        builder.addFixed(findButton);
        builder.addRelatedGap();
        replaceButton = new JButton(new ReplaceAction());
        builder.addFixed(replaceButton);
        builder.addRelatedGap();
        replaceAllButton = new JButton(new ReplaceAllAction());
        builder.addFixed(replaceAllButton);
        builder.addUnrelatedGap();
        builder.addFixed(new JButton(new CloseAction(dialog)));
        builder.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        // tie it up!
        panel.add(inputPanel, BorderLayout.NORTH);
        panel.add(options, BorderLayout.CENTER);
        panel.add(builder.getPanel(), BorderLayout.SOUTH);

        dialog.getContentPane().add(panel);
        dialog.pack();
        UISupport.initDialogActions(dialog, null, findButton);
    }

    protected SearchContext createSearchAndReplaceContext() {
        if (findCombo.getSelectedItem() == null) {
            return null;
        }
        if (replaceCombo.getSelectedItem() == null) {
            return null;
        }

        String searchExpression = findCombo.getSelectedItem().toString();
        String replacement = replaceCombo.getSelectedItem().toString();

        SearchContext context = new SearchContext();
        context.setSearchFor(searchExpression);
        context.setReplaceWith(replacement);
        context.setRegularExpression(false);
        context.setSearchForward(forwardButton.isSelected());
        context.setWholeWord(wholeWordCheck.isSelected());
        context.setMatchCase(caseCheck.isSelected());
        return context;
    }

    protected SearchContext createSearchContext() {
        if (findCombo.getSelectedItem() == null) {
            return null;
        }

        String searchExpression = findCombo.getSelectedItem().toString();

        SearchContext context = new SearchContext();
        context.setSearchFor(searchExpression);
        context.setRegularExpression(false);
        context.setSearchForward(forwardButton.isSelected());
        context.setWholeWord(wholeWordCheck.isSelected());
        context.setMatchCase(caseCheck.isSelected());
        return context;
    }

    private class FindAction extends AbstractAction {
        public FindAction(JComboBox findCombo) {
            super("Find/Find Next");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            SearchContext context = createSearchContext();

            if (context == null) {
                return;
            }

            boolean found = SearchEngine.find(editArea, context);
            if (!found) {
                UISupport.showErrorMessage("String [" + context.getSearchFor() + "] not found");
            }
        }
    }

    private class ReplaceAction extends AbstractAction {
        public ReplaceAction() {
            super("Replace/Replace Next");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            SearchContext context = createSearchAndReplaceContext();

            if (context == null) {
                return;
            }

            boolean found = SearchEngine.replace(editArea, context);
            if (!found) {
                UISupport.showErrorMessage("String [" + context.getSearchFor() + "] not found");
            }
        }

    }

    private class ReplaceAllAction extends AbstractAction {
        public ReplaceAllAction() {
            super("Replace All");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            SearchContext context = createSearchAndReplaceContext();

            if (context == null) {
                return;
            }

            int replaceCount = SearchEngine.replaceAll(editArea, context);
            if (replaceCount <= 0) {
                UISupport.showErrorMessage("String [" + context.getSearchFor() + "] not found");
            }
        }

    }

    private class CloseAction extends AbstractAction {
        final JDialog dialog;

        public CloseAction(JDialog d) {
            super("Close");
            dialog = d;
        }

        public void actionPerformed(ActionEvent e) {
            dialog.setVisible(false);
        }
    }
}
