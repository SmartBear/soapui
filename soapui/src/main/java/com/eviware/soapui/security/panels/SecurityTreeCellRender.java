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

package com.eviware.soapui.security.panels;

import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.security.SecurityScan;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.security.SecurityTest;
import com.eviware.soapui.security.scan.AbstractSecurityScan;
import com.eviware.soapui.security.support.ProgressBarSecurityScanAdapter;
import com.eviware.soapui.security.support.ProgressBarSecurityTestStepAdapter;
import com.eviware.soapui.support.UISupport;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("serial")
public class SecurityTreeCellRender implements TreeCellRenderer {

    Map<DefaultMutableTreeNode, Component> componentTree = new HashMap<DefaultMutableTreeNode, Component>();
    private JTree tree;
    Color selected = new Color(205, 205, 205);
    Color unselected = new Color(228, 228, 228);
    //	Color noSecurable = new Color( 102, 102, 102 );
    private boolean released;

    @Override
    public Component getTreeCellRendererComponent(JTree arg0, Object node, boolean sel, boolean exp, boolean leaf,
                                                  int arg5, boolean arg6) {
        Component result = null;
        if (released) {
            if (node instanceof TestStepNode) {
                result = getTreeCellRendererTestNode(arg0, (TestStepNode) node, sel, exp, leaf, arg5, arg6);
            }
            if (node instanceof SecurityScanNode) {
                result = getTreeCellRendererSecurityScanNode(arg0, (SecurityScanNode) node, sel, exp, leaf, arg5, arg6);
            }
            return result;
        }

        this.tree = arg0;

        if (componentTree.containsKey(node)) {
            result = componentTree.get(node);

            ((CustomTreeNode) result).setExpandedIcon(exp);
            ((CustomTreeNode) result).updateLabel();
            ((CustomTreeNode) result).setSelected(sel);
        } else {
            if (node instanceof TestStepNode) {
                result = getTreeCellRendererTestNode(arg0, (TestStepNode) node, sel, exp, leaf, arg5, arg6);
            }
            if (node instanceof SecurityScanNode) {
                result = getTreeCellRendererSecurityScanNode(arg0, (SecurityScanNode) node, sel, exp, leaf, arg5, arg6);
            }

            componentTree.put((DefaultMutableTreeNode) node, result);
        }
        return result;
    }

    private Component getTreeCellRendererSecurityScanNode(JTree arg0, SecurityScanNode node, boolean sel, boolean arg3,
                                                          boolean arg4, int arg5, boolean arg6) {
        return new SecurityScanCellRender(arg0, node, sel, arg3, arg4, arg5, arg6);
    }

    private Component getTreeCellRendererTestNode(JTree arg0, TestStepNode node, boolean sel, boolean arg3,
                                                  boolean arg4, int arg5, boolean arg6) {
        return new TestStepCellRender(arg0, node, sel, arg3, arg4, arg5, arg6);
    }

    public class TestStepCellRender extends JPanel implements PropertyChangeListener, CustomTreeNode, ReleasableNode {
        private WsdlTestStep testStep;
        private JProgressBar progressBar;
        private JLabel label;
        private ProgressBarSecurityTestStepAdapter progressBarAdapter;
        private SecurityTest securityTest;
        private Icon collapsed = UISupport.createImageIcon("/plus.gif");
        private Icon expanded = UISupport.createImageIcon("/minus.gif");
        private JLabel expandCollapseBtn;
        private DefaultMutableTreeNode node;
        private JPanel innerLeftPanel;
        private JPanel progressPanel;
        private JLabel cntLabel;

        public TestStepCellRender(final JTree tree, TestStepNode node, boolean sel, boolean exp, boolean leaf, int arg5,
                                  boolean arg6) {
            super(new BorderLayout());

            this.node = node;
            this.testStep = (WsdlTestStep) node.getTestStep();
            securityTest = ((SecurityTreeRootNode) node.getParent()).getSecurityTest();
            if (AbstractSecurityScan.isSecurable(testStep)) {
                if (securityTest.getSecurityScansMap().get(testStep.getId()) != null) {
                    String labelText = securityTest.getSecurityScansMap().get(testStep.getId()).size() == 1 ? securityTest
                            .getSecurityScansMap().get(testStep.getId()).size()
                            + " scan)" : securityTest.getSecurityScansMap().get(testStep.getId()).size() + " scans)";
                    label = new JLabel(testStep.getLabel() + " (" + labelText, SwingConstants.LEFT);
                } else {
                    label = new JLabel(testStep.getLabel() + " (0 scans)", SwingConstants.LEFT);
                }
            } else {
                label = new JLabel(testStep.getLabel(), SwingConstants.LEFT);
            }
            label.setIcon(testStep.getIcon());
            label.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            label.setEnabled(!testStep.isDisabled() && AbstractSecurityScan.isSecurable(testStep));
            testStep.addPropertyChangeListener(TestStep.ICON_PROPERTY, TestStepCellRender.this);
            testStep.addPropertyChangeListener(TestStep.DISABLED_PROPERTY, TestStepCellRender.this);
            innerLeftPanel = new JPanel(new BorderLayout());

            if (exp) {
                expandCollapseBtn = new JLabel(expanded);
            } else {
                expandCollapseBtn = new JLabel(collapsed);
            }

            expandCollapseBtn.setEnabled(false);

            if (securityTest.getSecurityScansMap().get(testStep.getId()) == null
                    || securityTest.getSecurityScansMap().get(testStep.getId()).size() == 0) {
                expandCollapseBtn.setVisible(false);
            } else {
                expandCollapseBtn.setVisible(true);
            }

            innerLeftPanel.add(expandCollapseBtn, BorderLayout.WEST);

            if (AbstractSecurityScan.isSecurable(testStep)) {
                progressBar = new JProgressBar();

                progressPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));

                progressBar.setValue(0);
                progressBar.setStringPainted(true);
                progressBar.setString("");
                progressBar.setIndeterminate(false);

                progressBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Color.LIGHT_GRAY));

                progressPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 0));
                progressPanel.add(progressBar);

                progressBar.setMinimumSize(new Dimension(0, 200));
                progressBar.setInheritsPopupMenu(true);

                cntLabel = new JLabel("");
                cntLabel.setForeground(Color.white);
                cntLabel.setBackground(selected);
                cntLabel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

                progressPanel.add(cntLabel);
                add(progressPanel, BorderLayout.LINE_END);
                expandCollapseBtn.setVisible(true);

                innerLeftPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
            } else {
                expandCollapseBtn.setVisible(false);
                innerLeftPanel.setBorder(BorderFactory.createEmptyBorder(0, 21, 0, 0));
            }
            innerLeftPanel.add(label, BorderLayout.CENTER);
            add(innerLeftPanel, BorderLayout.LINE_START);

            setSelected(sel);
            setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.black));
            progressBarAdapter = new ProgressBarSecurityTestStepAdapter(tree, node, progressBar, securityTest, testStep,
                    cntLabel);
        }

        public void reset() {
            progressBar.setValue(0);
            progressBar.setString("");
        }

        public void setSelected(boolean sel) {
            if (AbstractSecurityScan.isSecurable(testStep)) {
                if (sel) {
                    this.setBackground(selected);
                    this.label.setBackground(selected);
                    this.innerLeftPanel.setBackground(selected);
                    expandCollapseBtn.setBackground(selected);
                    progressPanel.setBackground(selected);

                } else {
                    this.setBackground(unselected);
                    this.label.setBackground(unselected);
                    this.innerLeftPanel.setBackground(unselected);
                    expandCollapseBtn.setBackground(unselected);
                    progressPanel.setBackground(unselected);
                }
            } else {
                this.setBackground(unselected);
                this.label.setBackground(unselected);
                this.innerLeftPanel.setBackground(unselected);
                expandCollapseBtn.setBackground(unselected);
            }
        }

        protected TestStep getTestStep() {
            return testStep;
        }

        public ModelItem getModelItem() {
            return testStep;
        }

        @Override
        public void propertyChange(PropertyChangeEvent arg0) {
            label.setIcon(testStep.getIcon());
            label.setEnabled(!testStep.isDisabled() && AbstractSecurityScan.isSecurable(testStep));
            updateLabel();
            ((DefaultTreeModel) tree.getModel()).nodeChanged(node);
        }

        @Override
        public void setExpandedIcon(boolean exp) {
            if (securityTest.getSecurityScansMap().get(testStep.getId()) == null
                    || securityTest.getSecurityScansMap().get(testStep.getId()).size() == 0) {
                expandCollapseBtn.setVisible(false);
                innerLeftPanel.setBorder(BorderFactory.createEmptyBorder(0, 21, 0, 0));
            } else {
                expandCollapseBtn.setVisible(true);
                innerLeftPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
            }
            if (exp) {
                expandCollapseBtn.setIcon(expanded);
            } else {
                expandCollapseBtn.setIcon(collapsed);
            }
        }

        @Override
        public void updateLabel() {
            if (AbstractSecurityScan.isSecurable(testStep)) {
                if (securityTest.getSecurityScansMap().get(testStep.getId()) != null) {
                    String labelText = securityTest.getSecurityScansMap().get(testStep.getId()).size() == 1 ? securityTest
                            .getSecurityScansMap().get(testStep.getId()).size()
                            + " scan)" : securityTest.getSecurityScansMap().get(testStep.getId()).size() + " scans)";
                    label.setText(testStep.getLabel() + " (" + labelText);
                } else {
                    label.setText(testStep.getLabel() + " (0 scans)");
                }
            } else {
                label.setText(testStep.getLabel());
            }
        }

        public boolean isOnExpandButton(int x, int y) {
            y = y - 30 * (tree.getRowForLocation(x, y));
            if ((5 <= x) && (20 >= x) && (5 <= y) && (20 >= y)) {
                return true;
            }
            return false;
        }

        public void release() {
            testStep.removePropertyChangeListener(TestStep.ICON_PROPERTY, TestStepCellRender.this);
            testStep.removePropertyChangeListener(TestStep.DISABLED_PROPERTY, TestStepCellRender.this);
            progressBarAdapter.release();
            testStep = null;
            securityTest = null;
        }

    }

    public class SecurityScanCellRender extends JPanel implements PropertyChangeListener, CustomTreeNode, ReleasableNode {
        private SecurityScan securityCheck;
        private JProgressBar progressBar;
        private JLabel label;
        private ProgressBarSecurityScanAdapter progressBarAdapter;
        private JPanel progressPanel;
        private JLabel cntLabel;
        private SecurityScanNode node;
        private JPanel leftInnerPanel;

        public SecurityScanCellRender(JTree tree, SecurityScanNode node, boolean sel, boolean arg3, boolean arg4,
                                      int arg5, boolean arg6) {
            super(new BorderLayout());

            this.node = node;
            this.securityCheck = (SecurityScan) node.getSecurityScan();
            this.securityCheck.addPropertyChangeListener(this);
            label = new JLabel(securityCheck.getName(), SwingConstants.LEFT);
            String iconPath = UISupport.getIconPath(securityCheck.getIcon());
            label.setIcon(UISupport.createImageIcon(iconPath));
            label.setBorder(BorderFactory.createEmptyBorder(5, 45, 5, 5));
            label.setEnabled(!securityCheck.isDisabled());
            leftInnerPanel = new JPanel(new BorderLayout());
            leftInnerPanel.add(label, BorderLayout.CENTER);
            leftInnerPanel.setBackground(getBackground());

            progressBar = new JProgressBar();
            progressPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            progressBar.setValue(0);
            progressBar.setStringPainted(true);
            progressBar.setString("");
            progressBar.setIndeterminate(false);

            progressBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Color.LIGHT_GRAY));

            progressPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 0));
            progressPanel.add(progressBar);

            progressBar.setMinimumSize(new Dimension(0, 200));
            progressBar.setInheritsPopupMenu(true);

            cntLabel = new JLabel("");
            cntLabel.setForeground(Color.white);
            cntLabel.setBackground(selected);
            cntLabel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

            progressPanel.add(cntLabel);
            add(progressPanel, BorderLayout.LINE_END);
            add(leftInnerPanel, BorderLayout.LINE_START);
            setSelected(sel);
            setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.black));

            progressBarAdapter = new ProgressBarSecurityScanAdapter(tree, this.node, progressBar, securityCheck,
                    (SecurityTest) ((SecurityScan) securityCheck).getParent(), cntLabel);

        }

        public void release() {
            progressBarAdapter.release();
        }

        public void reset() {
            progressBar.setValue(0);
            progressBar.setString("");
        }

        public void setSelected(boolean sel) {
            if (sel) {
                this.setBackground(selected);
                this.label.setBackground(selected);
                progressPanel.setBackground(selected);
                leftInnerPanel.setBackground(selected);
            } else {
                this.setBackground(unselected);
                this.label.setBackground(unselected);
                progressPanel.setBackground(unselected);
                leftInnerPanel.setBackground(unselected);
            }
        }

        @Override
        public void propertyChange(PropertyChangeEvent arg0) {
            label.setEnabled(!securityCheck.isDisabled());
            ((DefaultTreeModel) tree.getModel()).nodeChanged(node);
        }

        @Override
        public void setExpandedIcon(boolean exp) {
            // TODO Auto-generated method stub

        }

        @Override
        public void updateLabel() {
            label.setEnabled(!securityCheck.isDisabled());
        }

    }

    public void remove(DefaultMutableTreeNode node) {
        Component component = componentTree.get(node);
        if (component instanceof ReleasableNode) {
            ((ReleasableNode) component).release();
        }
        componentTree.remove(node);
    }

    public boolean isOn(TestStepNode node, int x, int y) {
        TestStepCellRender component = (TestStepCellRender) componentTree.get(node);
        return component.isOnExpandButton(x, y);
    }

    public void release() {
        released = true;
        for (DefaultMutableTreeNode key : componentTree.keySet()) {
            if (componentTree.get(key) instanceof ReleasableNode) {
                ((ReleasableNode) componentTree.get(key)).release();
            }
        }
        componentTree.clear();
    }
}
