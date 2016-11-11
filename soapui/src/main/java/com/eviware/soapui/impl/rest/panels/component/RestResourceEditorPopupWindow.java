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

package com.eviware.soapui.impl.rest.panels.component;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.impl.rest.support.RestUtils;
import com.eviware.soapui.support.DocumentListenerAdapter;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.jgoodies.forms.factories.ButtonBarFactory;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Popup window displayed by the RestResourceEditor class
 */
class RestResourceEditorPopupWindow extends JDialog {

    private static final ImageIcon CONNECTOR_ICON = UISupport.createImageIcon("/connector.png");

    // package protected fields to facilitate unit testing
    JTextField basePathTextField;

    List<RestSubResourceTextField> restSubResourceTextFields;
    private RestResource targetResource;
    private RestResource focusedResource;


    RestResourceEditorPopupWindow(RestResource resource, RestResource focusedResource) {
        super(SoapUI.getFrame());
        this.targetResource = resource;
        this.focusedResource = focusedResource;
        setModal(true);
        setResizable(false);
        setMinimumSize(new Dimension(230, 0));

        JPanel contentPane = new JPanel(new BorderLayout());
        setContentPane(contentPane);


        JButton okButton = new JButton(new AbstractAction("OK") {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (basePathTextField != null) {
                    targetResource.getInterface().setBasePath(basePathTextField.getText().trim());
                }
                for (RestSubResourceTextField restSubResourceTextField : restSubResourceTextFields) {
                    restSubResourceTextField.getRestResource().setPath(restSubResourceTextField.getTextField().getText().trim());
                }
                RestResourceEditor.scanForTemplateParameters(targetResource);
                dispose();
            }
        });

        AbstractAction cancelAction = new AbstractAction("Cancel") {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        };
        JButton cancelButton = new JButton(cancelAction);
        cancelButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel");
        cancelButton.getActionMap().put("cancel", cancelAction);

        JPanel buttonBar = ButtonBarFactory.buildRightAlignedBar(okButton, cancelButton);
        buttonBar.setLayout(new FlowLayout(FlowLayout.RIGHT));
        contentPane.add(createResourceEditorPanel(focusedResource), BorderLayout.CENTER);
        contentPane.add(buttonBar, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(okButton);

        pack();
    }

    private JPanel createResourceEditorPanel(RestResource focusedResource) {
        final JPanel panel = new JPanel(new BorderLayout());

        Box contentBox = Box.createVerticalBox();

        final JLabel changeWarningLabel = new JLabel(" ");
        changeWarningLabel.setBorder(BorderFactory.createCompoundBorder(
                contentBox.getBorder(),
                BorderFactory.createEmptyBorder(10, 0, 0, 0)));
        addBasePathFieldIfApplicable(contentBox, changeWarningLabel);
        addResourceFields(focusedResource, contentBox, changeWarningLabel);

        panel.add(contentBox, BorderLayout.NORTH);

        panel.add(changeWarningLabel, BorderLayout.CENTER);

        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Color.BLACK),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        return panel;
    }

    private void addResourceFields(RestResource focusedResource, Box contentBox, JLabel changeWarningLabel) {
        restSubResourceTextFields = new ArrayList<RestSubResourceTextField>();
        int rowIndex = contentBox.getComponents().length;

        for (RestResource restResource : RestUtils.extractAncestorsParentFirst(targetResource)) {
            Box row = Box.createHorizontalBox();
            row.setAlignmentX(0);
            addConnectorIfApplicable(rowIndex, row);

            final RestSubResourceTextField restSubResourceTextField = new RestSubResourceTextField(restResource);
            final JTextField innerTextField = restSubResourceTextField.getTextField();

            innerTextField.getDocument().addDocumentListener(new PathChangeListener(changeWarningLabel, restResource));
            restSubResourceTextFields.add(restSubResourceTextField);

            Box textFieldBox = createBoxWith(innerTextField);
            row.add(textFieldBox);

            contentBox.add(row);
            if (restResource == focusedResource) {
                moveFocusToField(innerTextField);
            }

            rowIndex++;
        }
    }

    private void addConnectorIfApplicable(int rowIndex, Box row) {
        if (rowIndex > 1) {
            row.add(Box.createHorizontalStrut((rowIndex - 1) * CONNECTOR_ICON.getIconWidth()));
        }
        if (rowIndex >= 1) {
            row.add(new JLabel(CONNECTOR_ICON));
        }
    }

    private void moveFocusToField(final JTextField innerTextField) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                innerTextField.requestFocusInWindow();
                innerTextField.selectAll();
            }
        });
    }

    private Box createBoxWith(JTextField innerTextField) {
        Box textFieldBox = Box.createVerticalBox();
        textFieldBox.add(Box.createVerticalGlue());
        textFieldBox.add(innerTextField);
        return textFieldBox;
    }

    class RestSubResourceTextField {
        private RestResource restResource;
        private JTextField textField;

        private RestSubResourceTextField(RestResource restResource) {
            this.restResource = restResource;
            textField = new JTextField(restResource.getPath());
            textField.setMaximumSize(new Dimension(340, (int) textField.getPreferredSize().getHeight()));
            textField.setPreferredSize(new Dimension(340, (int) textField.getPreferredSize().getHeight()));
        }

        public JTextField getTextField() {
            return textField;
        }

        public RestResource getRestResource() {
            return restResource;
        }
    }

    private void addBasePathFieldIfApplicable(Box contentBox, JLabel changeWarningLabel) {
        if (!StringUtils.isNullOrEmpty(targetResource.getInterface().getBasePath())) {
            basePathTextField = new JTextField(targetResource.getInterface().getBasePath());
            basePathTextField.getDocument().addDocumentListener(new PathChangeListener(changeWarningLabel,
                    targetResource.getTopLevelResource()));
            basePathTextField.setMaximumSize(new Dimension(340, (int) basePathTextField.getPreferredSize().getHeight()));
            Box row = Box.createHorizontalBox();
            row.setAlignmentX(0);
            row.add(createBoxWith(basePathTextField));
            contentBox.add(row);
            if (focusedResource == null) {
                moveFocusToField(basePathTextField);
            }
        }
    }

    private class PathChangeListener extends DocumentListenerAdapter {
        private final JLabel changeWarningLabel;
        private RestResource affectedRestResource;

        public PathChangeListener(JLabel changeWarningLabel, RestResource affectedRestResource) {
            this.changeWarningLabel = changeWarningLabel;
            this.affectedRestResource = affectedRestResource;
        }

        @Override
        public void update(Document document) {
            int affectedRequestCount = getRequestCountForResource(affectedRestResource);
            if (affectedRequestCount > 0) {
                changeWarningLabel.setText(String.format("<html>Changes will affect <b>%d</b> request%s</html>",
                        affectedRequestCount, affectedRequestCount > 1 ? "s" : ""));
                changeWarningLabel.setVisible(true);
            } else {
                changeWarningLabel.setVisible(false);
            }
        }

        private int getRequestCountForResource(RestResource affectedRestResource) {
            int affectedRequestCount = affectedRestResource.getRequestCount();
            for (RestResource childResource : affectedRestResource.getAllChildResources()) {
                affectedRequestCount += childResource.getRequestCount();
            }
            return affectedRequestCount;
        }
    }
}
