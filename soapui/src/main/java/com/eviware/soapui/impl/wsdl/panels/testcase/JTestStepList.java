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

package com.eviware.soapui.impl.wsdl.panels.testcase;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.TestStepConfig;
import com.eviware.soapui.impl.wsdl.actions.teststep.RunFromTestStepAction;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.registry.WsdlTestStepFactory;
import com.eviware.soapui.impl.wsdl.teststeps.registry.WsdlTestStepRegistry;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.support.TestSuiteListenerAdapter;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.swing.ActionListBuilder;
import com.eviware.soapui.support.action.swing.ActionSupport;
import com.eviware.soapui.support.action.swing.SwingActionDelegate;
import com.eviware.soapui.support.swing.AutoscrollSupport;
import com.eviware.soapui.support.swing.ModelItemListKeyListener;
import com.eviware.soapui.support.swing.ModelItemListMouseListener;

import javax.swing.AbstractAction;
import javax.swing.AbstractListModel;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Insets;
import java.awt.Point;
import java.awt.dnd.Autoscroll;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Panel for displaying and editing a list of TestSteps
 *
 * @author Ole.Matzura
 */

public class JTestStepList extends JPanel {
    private TestStepListModel testStepListModel;
    private JList testStepList;
    private JPopupMenu testListPopup;
    private JMenu appendStepMenu;
    private final WsdlTestCase testCase;

    public JTestStepList(WsdlTestCase testCase) {
        super(new BorderLayout());
        setDoubleBuffered(true);
        this.testCase = testCase;

        buildUI();
    }

    public JList getTestStepList() {
        return testStepList;
    }

    private void buildUI() {
        testStepListModel = new TestStepListModel();
        testStepList = new TestStepJList(testStepListModel);
        testStepList.setCellRenderer(new TestStepCellRenderer());
        testStepList.setFixedCellHeight(22);
        testStepList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        testStepList.addKeyListener(new TestStepListKeyHandler());

        testStepList.addMouseListener(new ModelItemListMouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = testStepList.locationToIndex(e.getPoint());
                if (row != -1) {
                    ModelItem item = (ModelItem) testStepList.getModel().getElementAt(row);
                    if (item != null) {
                        UISupport.select(item);
                    }
                }

                super.mouseClicked(e);
            }
        });

        testListPopup = new JPopupMenu();
        testListPopup.addSeparator();

        appendStepMenu = new JMenu("Append Step");

        WsdlTestStepRegistry registry = WsdlTestStepRegistry.getInstance();
        WsdlTestStepFactory[] factories = (WsdlTestStepFactory[]) registry.getFactories();

        for (int c = 0; c < factories.length; c++) {
            if (factories[c].canCreate()) {
                appendStepMenu.add(new InsertTestStepAction(factories[c]));
            }
        }

        testListPopup.add(appendStepMenu);

        testListPopup.addPopupMenuListener(new StepListPopupMenuListener(testCase));
        testStepList.setComponentPopupMenu(testListPopup);

        add(testStepList, BorderLayout.CENTER);
    }

    public void setEnabled(boolean enabled) {
        testStepList.setEnabled(enabled);

        super.setEnabled(enabled);
    }

    private final class TestStepListKeyHandler extends ModelItemListKeyListener {
        @Override
        public ModelItem getModelItemAt(int ix) {
            return testCase.getTestStepAt(ix);
        }
    }

    private final class StepListPopupMenuListener implements PopupMenuListener {
        private StepListPopupMenuListener(WsdlTestCase case1) {
            super();
        }

        public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
            testListPopup.removeAll();

            if (SoapUI.getTestMonitor().hasRunningLoadTest(testCase)) {
                testListPopup.add("<disabled during LoadTest>").setEnabled(false);
                return;
            }

            if (SoapUI.getTestMonitor().hasRunningSecurityTest(testCase)) {
                testListPopup.add("<disabled during SecurityTest>").setEnabled(false);
                return;
            }

            Point location = testStepList.getMousePosition();
            int ix = -1;
            if (location != null) {
                int index = testStepList.locationToIndex(location);
                if (index != -1 && !testStepList.isSelectedIndex(index)
                        && testStepList.getCellBounds(index, index).contains(location)) {
                    testStepList.addSelectionInterval(index, index);
                    ix = index;
                } else if (index != -1 && testStepList.isSelectedIndex(index)
                        && testStepList.getCellBounds(index, index).contains(location)) {
                    ix = index;
                }
            }

            if (ix >= 0) {
                int[] indices = testStepList.getSelectedIndices();
                if (indices.length == 1) {
                    WsdlTestStep testStep = testCase.getTestStepAt(ix);
                    ActionSupport.addActions(ActionListBuilder.buildActions(testStep), testListPopup);

                    testListPopup.insert(SwingActionDelegate.createDelegate(new RunFromTestStepAction(), testStep), 0);
                    testListPopup.insert(new JSeparator(), 1);
                } else {
                    ModelItem[] modelItems = new ModelItem[indices.length];
                    for (int c = 0; c < indices.length; c++) {
                        modelItems[c] = testCase.getTestStepAt(indices[c]).getModelItem();
                    }

                    ActionSupport.addActions(ActionListBuilder.buildMultiActions(modelItems), testListPopup);
                }
            } else {
                testStepList.clearSelection();
                testListPopup.add(appendStepMenu);
            }
        }

        public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
        }

        public void popupMenuCanceled(PopupMenuEvent e) {
        }
    }

    // private final class StepListMouseListener extends MouseAdapter
    // {
    // public void mouseClicked(MouseEvent e)
    // {
    // if (e.getClickCount() < 2)
    // {
    // return;
    // }
    //
    // ModelItem modelItem = (ModelItem) testStepList.getSelectedValue();
    // if (modelItem == null)
    // return;
    //
    // Action defaultAction = ActionListBuilder.buildActions( modelItem
    // ).getDefaultAction();
    // if( defaultAction != null )
    // defaultAction.actionPerformed( new ActionEvent( TestStepList.this, 0, null
    // ));
    // }
    // }

    /**
     * Renderer which sets icon and wider border for teststeps
     *
     * @author Ole.Matzura
     */

    private final static class TestStepCellRenderer extends JLabel implements ListCellRenderer {
        public TestStepCellRenderer() {
            setOpaque(true);
            setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        }

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                                                      boolean cellHasFocus) {
            WsdlTestStep testStep = (WsdlTestStep) value;

            setText(testStep.getLabel());
            setIcon(testStep.getIcon());

            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }

            setEnabled(list.isEnabled() && !testStep.isDisabled());

            String toolTipText = list.getToolTipText();
            if (toolTipText == null) {
                setToolTipText(testStep.getDescription());
            } else {
                setToolTipText(toolTipText.length() == 0 ? null : toolTipText);
            }

            return this;
        }
    }

    private class TestStepListModel extends AbstractListModel implements PropertyChangeListener {
        private TestStepListTestSuiteListener testStepListTestSuiteListener = new TestStepListTestSuiteListener();

        public TestStepListModel() {
            for (int c = 0; c < getSize(); c++) {
                testCase.getTestStepAt(c).addPropertyChangeListener(this);
            }

            testCase.getTestSuite().addTestSuiteListener(testStepListTestSuiteListener);
        }

        public int getSize() {
            return testCase.getTestStepCount();
        }

        public Object getElementAt(int index) {
            return testCase.getTestStepAt(index);
        }

        public synchronized void propertyChange(PropertyChangeEvent arg0) {
            final int ix = testCase.getIndexOfTestStep((TestStep) arg0.getSource());
            if (ix == -1) {
                return;
            }

            if (!SwingUtilities.isEventDispatchThread()) {
                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        fireContentsChanged(this, ix, ix);
                    }
                });
            } else {
                fireContentsChanged(this, ix, ix);
            }
        }

        public void release() {
            testCase.getTestSuite().removeTestSuiteListener(testStepListTestSuiteListener);

            for (int c = 0; c < getSize(); c++) {
                testCase.getTestStepAt(c).removePropertyChangeListener(this);
            }
        }

        private class TestStepListTestSuiteListener extends TestSuiteListenerAdapter {
            public void testStepAdded(TestStep testStep, int ix) {
                if (testStep.getTestCase() == testCase) {
                    testStep.addPropertyChangeListener(TestStepListModel.this);
//					setSelectedValue( testStep, true );
                    fireIntervalAdded(TestStepListModel.this, ix, ix);
                }
            }

            public void testStepRemoved(TestStep testStep, int ix) {
                if (testStep.getTestCase() == testCase) {
                    testStep.removePropertyChangeListener(TestStepListModel.this);
                    fireIntervalRemoved(TestStepListModel.this, ix, ix);
                }
            }

            @Override
            public void testStepMoved(TestStep testStep, int fromIndex, int offset) {
                if (testStep.getTestCase() == testCase) {
                    fireContentsChanged(TestStepListModel.this, fromIndex, fromIndex + offset);
                    int selectedIndex = testStepList.getSelectedIndex();
                    if (selectedIndex == fromIndex) {
                        testStepList.setSelectedIndex(fromIndex + offset);
                    } else if (selectedIndex < fromIndex && selectedIndex >= fromIndex + offset) {
                        testStepList.setSelectedIndex(selectedIndex + 1);
                    } else if (selectedIndex > fromIndex && selectedIndex <= fromIndex + offset) {
                        testStepList.setSelectedIndex(selectedIndex - 1);
                    }
                }
            }
        }
    }

    public class InsertTestStepAction extends AbstractAction {
        private final WsdlTestStepFactory factory;

        public InsertTestStepAction(WsdlTestStepFactory factory) {
            super(factory.getTestStepName());
            putValue(Action.SHORT_DESCRIPTION, factory.getTestStepDescription());
            putValue(Action.SMALL_ICON, UISupport.createImageIcon(factory.getTestStepIconPath()));
            this.factory = factory;
        }

        public void actionPerformed(ActionEvent e) {
            if (!factory.canAddTestStepToTestCase(testCase)) {
                return;
            }

            String name = UISupport.prompt("Specify name for new step", "Insert Step", factory.getTestStepName());
            if (name != null) {
                TestStepConfig newTestStepConfig = factory.createNewTestStep(testCase, name);
                if (newTestStepConfig != null) {
                    WsdlTestStep testStep = testCase.addTestStep(newTestStepConfig);
                    UISupport.selectAndShow(testStep);
                }
            }
        }
    }

    public void setSelectedIndex(int i) {
        testStepList.setSelectedIndex(i);
    }

    public void setSelectedValue(TestStep testStep, boolean b) {
        try {
            testStepList.setSelectedValue(testStep, true);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    public void release() {
        testStepListModel.release();
    }

    private static class TestStepJList extends JList implements Autoscroll {
        private AutoscrollSupport autoscrollSupport;

        public TestStepJList(TestStepListModel testStepListModel) {
            super(testStepListModel);

            autoscrollSupport = new AutoscrollSupport(this, new Insets(10, 10, 10, 10));
        }

        public void autoscroll(Point cursorLoc) {
            autoscrollSupport.autoscroll(cursorLoc);
        }

        public Insets getAutoscrollInsets() {
            return autoscrollSupport.getAutoscrollInsets();
        }
    }
}
