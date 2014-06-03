/*
 * Copyright 2004-2014 SmartBear Software
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

package com.eviware.soapui.impl.actions;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.WorkspaceImpl;
import com.eviware.soapui.support.MessageSupport;
import com.eviware.soapui.support.SoapUIException;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.SoapUIAction;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;
import com.eviware.soapui.support.factory.SoapUIFactoryRegistry;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * Action class to create new REST project.
 *
 * @author Shadid Chowdhury
 */

public class NewProjectWizardAction extends AbstractSoapUIAction<WorkspaceImpl> {


    public static final String SOAPUI_ACTION_ID = "NewProjectWizardAction";

    private static final MessageSupport messages = MessageSupport.getMessages(NewProjectWizardAction.class);


    public NewProjectWizardAction() {
        super(messages.get("Title"), messages.get("Description"));
    }


    public void perform(final WorkspaceImpl workspace, final Object param) {
        final JDialog dialog = new JDialog(SoapUI.getFrame(), "New project", true);
        Container contentPane = dialog.getContentPane();
        contentPane.setLayout(new BorderLayout());
        final CreationTypeSelectionPanel creationTypeSelectionPanel = new CreationTypeSelectionPanel();
        contentPane.add(creationTypeSelectionPanel, BorderLayout.CENTER);
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.setVisible(false);
                dialog.dispose();
                creationTypeSelectionPanel.selectedAction().perform(workspace, param);
            }
        });
        buttonPanel.add(okButton);
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.setVisible(false);
                dialog.dispose();
            }
        });
        buttonPanel.add(cancelButton);
        contentPane.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setBounds(500, 500, 400, 300);
        dialog.setVisible(true);
    }

    private static class CreationTypeSelectionPanel extends JPanel {

        private ButtonGroup radioButtons = new ButtonGroup();
        private JComboBox<ImportMethod> importMethodsComboBox;
        private JComboBox<DiscoveryMethod> discoveryMethodsComboBox;
        private JRadioButton emptyProjectRadio;
        private JRadioButton importProjectRadio;
        private JRadioButton discoverResourcesRadio;
        private ActionListener comboBoxEnabledHandler;

        CreationTypeSelectionPanel() {
            super(new GridLayout(3, 1));
            initializeRadioButtons();
            this.add(emptyProjectRadio);
            JPanel importPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            ImportMethod[] importMethods = getAllImportMethods();
            importMethodsComboBox = new JComboBox<ImportMethod>(importMethods);
            importPanel.add(importProjectRadio);
            importPanel.add(importMethodsComboBox);
            this.add(importPanel);
            JPanel discoveryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            DiscoveryMethod[] discoveryMethods = getAllDiscoveryMethods();
            discoveryMethodsComboBox = new JComboBox<DiscoveryMethod>(discoveryMethods);
            discoveryPanel.add(discoverResourcesRadio);
            discoveryPanel.add(discoveryMethodsComboBox);
            this.add(discoveryPanel);
            enableAndDisableComboBoxes();
        }

        SoapUIAction<WorkspaceImpl> selectedAction() {
            if (emptyProjectRadio.isSelected()) {
                return new NewEmptyProjectAction();
            } else if (importProjectRadio.isSelected()) {
                return new ImportAction((ImportMethod) importMethodsComboBox.getSelectedItem());
            } else {
                return new DiscoveryAction((DiscoveryMethod) discoveryMethodsComboBox.getSelectedItem());
            }
        }

        private DiscoveryMethod[] getAllDiscoveryMethods() {
            List<DiscoveryMethodFactory> factories = SoapUI.getSoapUICore().getFactoryRegistry().getFactories(DiscoveryMethodFactory.class);
            DiscoveryMethod[] returnValue = new DiscoveryMethod[factories.size()];
            for (int i = 0; i < factories.size(); i++) {
                returnValue[i] = factories.get(i).createNewDiscoveryMethod();
            }
            return returnValue;
        }

        private ImportMethod[] getAllImportMethods() {
            List<ImportMethodFactory> factories = SoapUI.getSoapUICore().getFactoryRegistry().getFactories(ImportMethodFactory.class);
            ImportMethod[] returnValue = new ImportMethod[factories.size()];
            for (int i = 0; i < factories.size(); i++) {
                returnValue[i] = factories.get(i).createNewImportMethod();
            }
            return returnValue;
        }

        private void initializeRadioButtons() {
            comboBoxEnabledHandler = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    enableAndDisableComboBoxes();
                }
            };
            emptyProjectRadio = makeRadioButton("Create empty project");
            importProjectRadio = makeRadioButton("Import definition");
            discoverResourcesRadio = makeRadioButton("Discover resources");
            emptyProjectRadio.setSelected(true);
        }

        private void enableAndDisableComboBoxes() {
            importMethodsComboBox.setEnabled(importProjectRadio.isSelected());
            discoveryMethodsComboBox.setEnabled(discoverResourcesRadio.isSelected());
        }

        private JRadioButton makeRadioButton(String label) {
            JRadioButton radioButton = new JRadioButton(label);
            radioButton.addActionListener(comboBoxEnabledHandler);
            radioButtons.add(radioButton);
            return radioButton;
        }

        private class NewEmptyProjectAction extends AbstractSoapUIAction<WorkspaceImpl> {
            public NewEmptyProjectAction() {
                super("");
            }

            @Override
            public void perform(WorkspaceImpl target, Object param) {
                try {
                    ((WorkspaceImpl) target).createProject("Empty project");
                } catch (SoapUIException e) {
                    UISupport.showErrorMessage(e);
                }
            }
        }

        private class ImportAction extends AbstractSoapUIAction<WorkspaceImpl> {
            private ImportMethod selectedItem;

            public ImportAction(ImportMethod selectedItem) {
                super("");
                this.selectedItem = selectedItem;
            }

            @Override
            public void perform(WorkspaceImpl target, Object param) {
                selectedItem.getImportAction().perform(target, null);
            }
        }

        private class DiscoveryAction extends AbstractSoapUIAction<WorkspaceImpl> {
            private DiscoveryMethod discoveryMethod;

            public DiscoveryAction(DiscoveryMethod discoveryMethod) {
                super("");
                this.discoveryMethod = discoveryMethod;
            }

            @Override
            public void perform(WorkspaceImpl target, Object param) {
                if (discoveryMethod.isSynchronous()) {
                    discoveryMethod.discoverResourcesSynchronously(target);
                } else {
                    discoveryMethod.discoverResources(target);
                }
            }
        }
    }

    public static void main(String[] args) {
        SoapUI.initDefaultCore();
        SoapUIFactoryRegistry factoryRegistry = SoapUI.getSoapUICore().getFactoryRegistry();
        factoryRegistry.addFactory(ImportMethodFactory.class, new WsdlImportMethodFactory());
        factoryRegistry.addFactory(ImportMethodFactory.class, new WadlImportMethodFactory());
        factoryRegistry.addFactory(DiscoveryMethodFactory.class, new InternalBrowserDiscoveryFactory());
        JFrame main = new JFrame();
        main.getContentPane().add(new CreationTypeSelectionPanel());
        main.setSize(400, 300);
        main.setVisible(true);
    }


}
