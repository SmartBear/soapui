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

import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlRunTestCaseTestStep;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.model.iface.Operation;
import com.eviware.soapui.model.testsuite.TestCase;
import com.eviware.soapui.model.testsuite.TestSuite;
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
import java.util.List;

public class ChooseAnotherTestCase implements Resolver {

    private boolean resolved;
    private WsdlRunTestCaseTestStep runTestStep;
    private WsdlProject project;
    private WsdlTestCase pickedTestCase;

    public ChooseAnotherTestCase(WsdlRunTestCaseTestStep wsdlRunTestCaseTestStep) {
        runTestStep = wsdlRunTestCaseTestStep;
        project = runTestStep.getTestCase().getTestSuite().getProject();
    }

    public String getDescription() {
        return "Choose another test step";
    }

    @Override
    public String toString() {
        return getDescription();
    }

    public String getResolvedPath() {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean isResolved() {
        return resolved;
    }

    public boolean resolve() {
        TestCaseChangeDialog dialog = new TestCaseChangeDialog("Choose another test case");
        dialog.showAndChoose();

        return resolved;
    }

    @SuppressWarnings("serial")
    private class TestCaseChangeDialog extends JDialog {

        private JComboBox tSuiteStepCombo;
        private JComboBox tCaseCombo;
        private JButton okBtn = new JButton(" Ok ");
        private JButton cancelBtn = new JButton(" Cancel ");

        public TestCaseChangeDialog(String title) {
            super(UISupport.getMainFrame(), title, true);
            init();
        }

        private void init() {
            FormLayout layout = new FormLayout("right:pref, 4dlu, 30dlu, 5dlu, 30dlu, min ",
                    "min, pref, 4dlu, pref, 4dlu, pref, min");
            CellConstraints cc = new CellConstraints();
            PanelBuilder panel = new PanelBuilder(layout);
            panel.addLabel("Interface:", cc.xy(1, 2));

            List<TestSuite> tSuites = project.getTestSuiteList();
            DefaultComboBoxModel sourceStepComboModel = new DefaultComboBoxModel();
            tSuiteStepCombo = new JComboBox(sourceStepComboModel);
            tSuiteStepCombo.setRenderer(new TestSuiteComboRenderer());
            for (TestSuite element : tSuites) {
                sourceStepComboModel.addElement(element);
            }

            tSuiteStepCombo.setSelectedIndex(0);
            panel.add(tSuiteStepCombo, cc.xyw(3, 2, 3));

            tCaseCombo = new JComboBox(((TestSuite) tSuiteStepCombo.getSelectedItem()).getTestCaseList().toArray());
            tCaseCombo.setRenderer(new TestCaseComboRender());

            panel.addLabel("Operation:", cc.xy(1, 4));
            panel.add(tCaseCombo, cc.xyw(3, 4, 3));

            panel.add(okBtn, cc.xy(3, 6));
            panel.add(cancelBtn, cc.xy(5, 6));

            tSuiteStepCombo.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    Interface iface = project.getInterfaceByName(((TestSuite) tSuiteStepCombo.getSelectedItem())
                            .getName());
                    tCaseCombo.removeAllItems();
                    if (iface != null) {
                        tCaseCombo.setEnabled(true);
                        for (Operation op : iface.getOperationList()) {
                            tCaseCombo.addItem(op);
                        }
                    } else {
                        tCaseCombo.setEnabled(false);
                    }

                }

            });

            okBtn.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {

                    pickedTestCase = (WsdlTestCase) tCaseCombo.getSelectedItem();
                    runTestStep.setTargetTestCase(pickedTestCase);
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
    private class TestSuiteComboRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                                                      boolean cellHasFocus) {
            Component result = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (value instanceof TestSuite) {
                TestSuite item = (TestSuite) value;
                setIcon(item.getIcon());
                setText(item.getName());
            }

            return result;
        }
    }

    @SuppressWarnings("serial")
    private class TestCaseComboRender extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                                                      boolean cellHasFocus) {
            Component result = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (value instanceof TestCase) {
                TestCase item = (TestCase) value;
                setIcon(item.getIcon());
                setText(item.getName());
            }

            return result;
        }

    }

    public WsdlTestCase getPickedTestCase() {
        return pickedTestCase;
    }

}
