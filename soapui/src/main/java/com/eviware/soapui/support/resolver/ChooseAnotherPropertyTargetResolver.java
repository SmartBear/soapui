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

package com.eviware.soapui.support.resolver;

import com.eviware.soapui.impl.wsdl.teststeps.PropertyTransfer;
import com.eviware.soapui.impl.wsdl.teststeps.PropertyTransfersTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.model.TestModelItem;
import com.eviware.soapui.model.TestPropertyHolder;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansion;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.resolver.ResolveContext.Resolver;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JList;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class ChooseAnotherPropertyTargetResolver implements Resolver {

    private boolean resolved;
    private PropertyTransfer badTransfer = null;
    private PropertyTransfersTestStep parent = null;
    private ArrayList<Object> sources = new ArrayList<Object>();
    private ArrayList<String[]> properties = new ArrayList<String[]>();

    public ChooseAnotherPropertyTargetResolver(PropertyTransfer propertyTransfer, PropertyTransfersTestStep parent) {
        this.badTransfer = propertyTransfer;
        this.parent = parent;

        sources.add(PropertyExpansionUtils.getGlobalProperties());
        properties.add(PropertyExpansionUtils.getGlobalProperties().getPropertyNames());
        sources.add(parent.getTestCase().getTestSuite().getProject());
        properties.add(parent.getTestCase().getTestSuite().getProject().getPropertyNames());
        sources.add(parent.getTestCase().getTestSuite());
        properties.add(parent.getTestCase().getTestSuite().getPropertyNames());

        sources.add(parent.getTestCase());
        properties.add(parent.getTestCase().getPropertyNames());

        for (int c = 0; c < parent.getTestCase().getTestStepCount(); c++) {
            WsdlTestStep testStep = parent.getTestCase().getTestStepAt(c);
            if (testStep == parent) {
                continue;
            }

            sources.add(testStep);
            properties.add(testStep.getPropertyNames());
        }

    }

    public String getDescription() {
        return "Choose new target property";
    }

    @Override
    public String toString() {
        return getDescription();
    }

    public String getResolvedPath() {
        return null;
    }

    public boolean isResolved() {
        return resolved;
    }

    public boolean resolve() {
        PropertyChangeDialog propertyChangeDialog = new PropertyChangeDialog("Choose another property");
        propertyChangeDialog.showAndChoose();

        return resolved;
    }

    @SuppressWarnings("serial")
    private class PropertyChangeDialog extends JDialog {

        private JComboBox sourceStepCombo;
        private JComboBox propertiesCombo;
        private JButton okBtn = new JButton(" Ok ");
        private JButton cancelBtn = new JButton(" Cancel ");

        public PropertyChangeDialog(String title) {
            super(UISupport.getMainFrame(), title, true);
            init();
        }

        private void init() {
            FormLayout layout = new FormLayout("min,right:pref, 4dlu, 40dlu, 5dlu, 40dlu, min ",
                    "min, pref, 4dlu, pref, 4dlu, pref, min");
            CellConstraints cc = new CellConstraints();
            PanelBuilder panel = new PanelBuilder(layout);
            panel.addLabel("Source:", cc.xy(2, 2));
            DefaultComboBoxModel sourceStepComboModel = new DefaultComboBoxModel();
            sourceStepCombo = new JComboBox(sourceStepComboModel);
            sourceStepCombo.setRenderer(new StepComboRenderer());
            for (Object element : sources) {
                sourceStepComboModel.addElement(element);
            }

            sourceStepCombo.setSelectedIndex(0);
            panel.add(sourceStepCombo, cc.xyw(4, 2, 3));

            int index = sourceStepCombo.getSelectedIndex();

            propertiesCombo = new JComboBox(properties.get(index));
            panel.addLabel("Property:", cc.xy(2, 4));
            panel.add(propertiesCombo, cc.xyw(4, 4, 3));

            panel.add(okBtn, cc.xy(4, 6));
            panel.add(cancelBtn, cc.xy(6, 6));

            sourceStepCombo.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    int index = sourceStepCombo.getSelectedIndex();
                    propertiesCombo.removeAllItems();
                    if (properties.get(index).length > 0) {
                        propertiesCombo.setEnabled(true);
                        for (String str : properties.get(index)) {
                            propertiesCombo.addItem(str);
                        }
                    } else {
                        propertiesCombo.setEnabled(false);
                    }

                }

            });

            okBtn.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {

                    String name;
                    TestPropertyHolder sourceStep = (TestPropertyHolder) sourceStepCombo.getSelectedItem();
                    if (sourceStep == PropertyExpansionUtils.getGlobalProperties()) {
                        name = PropertyExpansion.GLOBAL_REFERENCE;
                    } else if (sourceStep == parent.getTestCase().getTestSuite().getProject()) {
                        name = PropertyExpansion.PROJECT_REFERENCE;
                    } else if (sourceStep == parent.getTestCase().getTestSuite()) {
                        name = PropertyExpansion.TESTSUITE_REFERENCE;
                    } else if (sourceStep == parent.getTestCase()) {
                        name = PropertyExpansion.TESTCASE_REFERENCE;
                    } else {
                        name = sourceStep.getModelItem().getName();
                    }

                    badTransfer.setTargetStepName(name);

                    badTransfer.setTargetPropertyName((String) propertiesCombo.getSelectedItem());

                    resolved = true;

                    setVisible(false);
                }

            });

            cancelBtn.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    resolved = false;

                    setVisible(false);
                }

            });

            setLocationRelativeTo(UISupport.getParentFrame(this));
            panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            this.add(panel.getPanel());
        }

        public void showAndChoose() {
            this.pack();
            this.setVisible(true);
        }
    }

    @SuppressWarnings("serial")
    private class StepComboRenderer extends DefaultListCellRenderer {
        @SuppressWarnings("finally")
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                                                      boolean cellHasFocus) {
            Component result = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            try {
                if (value instanceof TestModelItem) {
                    TestModelItem item = (TestModelItem) value;
                    setIcon(item.getIcon());
                    setText(item.getName());
                } else if (value == PropertyExpansionUtils.getGlobalProperties()) {
                    setText("Global");
                }

            } catch (Exception e) {
                setText("Removed element");
            } finally {
                return result;
            }
        }
    }
}
