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

package com.eviware.soapui.impl.wsdl.panels.testsuite;

import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.impl.wsdl.actions.testsuite.AddNewTestCaseAction;
import com.eviware.soapui.impl.wsdl.panels.support.ProgressBarTestCaseAdapter;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.support.TestSuiteListenerAdapter;
import com.eviware.soapui.model.testsuite.TestCase;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.swing.ActionList;
import com.eviware.soapui.support.action.swing.ActionListBuilder;
import com.eviware.soapui.support.action.swing.ActionSupport;
import com.eviware.soapui.support.action.swing.SwingActionDelegate;
import com.eviware.soapui.support.dnd.DropType;
import com.eviware.soapui.support.dnd.SoapUIDragAndDropHandler;
import com.eviware.soapui.support.dnd.SoapUIDragAndDropable;
import com.eviware.soapui.support.swing.AutoscrollSupport;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.dnd.Autoscroll;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragSource;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * A panel showing a scrollable list of TestCases in a TestSuite.
 *
 * @author Ole.Matzura
 */

public class JTestSuiteTestCaseList extends JPanel {
    private Map<TestCase, TestCaseListPanel> panels = new HashMap<TestCase, TestCaseListPanel>();
    private final WsdlTestSuite testSuite;
    private final InternalTestSuiteListener testSuiteListener = new InternalTestSuiteListener();
    private TestCaseListPanel selectedTestCase;

    public JTestSuiteTestCaseList(WsdlTestSuite testSuite) {
        this.testSuite = testSuite;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        for (int c = 0; c < testSuite.getTestCaseCount(); c++) {
            TestCaseListPanel testCaseListPanel = createTestCaseListPanel(testSuite.getTestCaseAt(c));
            panels.put(testSuite.getTestCaseAt(c), testCaseListPanel);
            add(testCaseListPanel);
        }

        add(Box.createVerticalGlue());
        setBackground(Color.WHITE);

        testSuite.addTestSuiteListener(testSuiteListener);

        ActionList actions = ActionListBuilder.buildActions(testSuite);
        actions.removeAction(0);
        actions.removeAction(0);
        setComponentPopupMenu(ActionSupport.buildPopup(actions));

        DragSource dragSource = DragSource.getDefaultDragSource();

        SoapUIDragAndDropHandler dragAndDropHandler = new SoapUIDragAndDropHandler(
                new TestCaseListDragAndDropable(this), DropType.AFTER);

        dragSource.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY_OR_MOVE, dragAndDropHandler);
    }

    public void reset() {
        for (TestCaseListPanel testCasePanel : panels.values()) {
            testCasePanel.reset();
        }
    }

    @Override
    public void addNotify() {
        super.addNotify();
        testSuite.addTestSuiteListener(testSuiteListener);
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        testSuite.removeTestSuiteListener(testSuiteListener);
    }

    private final class InternalTestSuiteListener extends TestSuiteListenerAdapter {
        public void testCaseAdded(TestCase testCase) {
            TestCaseListPanel testCaseListPanel = createTestCaseListPanel(testCase);
            panels.put(testCase, testCaseListPanel);
            add(testCaseListPanel, testCase.getTestSuite().getIndexOfTestCase(testCase));
            revalidate();
            repaint();
        }

        public void testCaseRemoved(TestCase testCase) {
            TestCaseListPanel testCaseListPanel = panels.get(testCase);
            if (testCaseListPanel != null) {
                remove(testCaseListPanel);
                panels.remove(testCase);
                revalidate();
                repaint();
            }
        }

        @Override
        public void testCaseMoved(TestCase testCase, int index, int offset) {
            TestCaseListPanel testCaseListPanel = panels.get(testCase);
            if (testCaseListPanel != null) {
                boolean hadFocus = testCaseListPanel.hasFocus();

                remove(testCaseListPanel);
                add(testCaseListPanel, index + offset);

                revalidate();
                repaint();

                if (hadFocus) {
                    testCaseListPanel.requestFocus();
                }
            }
        }
    }

    public final class TestCaseListPanel extends JPanel implements Autoscroll {
        private final WsdlTestCase testCase;
        private JProgressBar progressBar;
        private JLabel label;
        private ProgressBarTestCaseAdapter progressBarAdapter;
        private TestCasePropertyChangeListener testCasePropertyChangeListener;
        private AutoscrollSupport autoscrollSupport;

        public TestCaseListPanel(WsdlTestCase testCase) {
            super(new BorderLayout());

            setFocusable(true);

            this.testCase = testCase;
            autoscrollSupport = new AutoscrollSupport(this);

            progressBar = new JProgressBar(0, 100) {
                protected void processMouseEvent(MouseEvent e) {
                    if (e.getID() == MouseEvent.MOUSE_PRESSED || e.getID() == MouseEvent.MOUSE_RELEASED) {
                        TestCaseListPanel.this.processMouseEvent(translateMouseEvent(e));
                    }
                }

                protected void processMouseMotionEvent(MouseEvent e) {
                    TestCaseListPanel.this.processMouseMotionEvent(translateMouseEvent(e));
                }

                /**
                 * Translates the given mouse event to the enclosing map panel's
                 * coordinate space.
                 */
                private MouseEvent translateMouseEvent(MouseEvent e) {
                    return new MouseEvent(TestCaseListPanel.this, e.getID(), e.getWhen(), e.getModifiers(), e.getX()
                            + getX(), e.getY() + getY(), e.getClickCount(), e.isPopupTrigger(), e.getButton());
                }
            };

            JPanel progressPanel = UISupport.createProgressBarPanel(progressBar, 5, false);

            progressBar.setMinimumSize(new Dimension(0, 10));
            progressBar.setBackground(Color.WHITE);
            progressBar.setInheritsPopupMenu(true);

            label = new JLabel(testCase.getLabel());
            label.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            label.setInheritsPopupMenu(true);
            label.setEnabled(!testCase.isDisabled());

            add(progressPanel, BorderLayout.CENTER);
            add(label, BorderLayout.NORTH);

            testCasePropertyChangeListener = new TestCasePropertyChangeListener();

            initPopup(testCase);

            addMouseListener(new MouseAdapter() {

                @Override
                public void mousePressed(MouseEvent e) {
                    requestFocus();
                }

                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() < 2) {
                        if (selectedTestCase != null) {
                            selectedTestCase.setSelected(false);
                        }

                        setSelected(true);
                        selectedTestCase = TestCaseListPanel.this;
                        return;
                    }

                    UISupport.selectAndShow(TestCaseListPanel.this.testCase);
                }
            });

            addKeyListener(new TestCaseListPanelKeyHandler());

            // init border
            setSelected(false);
        }

        public void reset() {
            progressBar.setValue(0);
            progressBar.setString("");
        }

        private void initPopup(WsdlTestCase testCase) {
            ActionList actions = ActionListBuilder.buildActions(testCase);
            actions.insertAction(
                    SwingActionDelegate.createDelegate(AddNewTestCaseAction.SOAPUI_ACTION_ID, testSuite, null, null), 0);
            actions.insertAction(ActionSupport.SEPARATOR_ACTION, 1);

            setComponentPopupMenu(ActionSupport.buildPopup(actions));
        }

        public void addNotify() {
            super.addNotify();
            testCase.addPropertyChangeListener(testCasePropertyChangeListener);
            progressBarAdapter = new ProgressBarTestCaseAdapter(progressBar, testCase);
        }

        public void removeNotify() {
            super.removeNotify();
            if (progressBarAdapter != null) {
                testCase.removePropertyChangeListener(testCasePropertyChangeListener);
                progressBarAdapter.release();

                progressBarAdapter = null;
            }
        }

        public Dimension getMaximumSize() {
            Dimension size = super.getMaximumSize();
            size.height = 50;
            return size;
        }

        public void setSelected(boolean selected) {
            if (selected) {
                setBorder(BorderFactory.createLineBorder(Color.GRAY));
            } else {
                setBorder(BorderFactory.createLineBorder(Color.WHITE));
            }
        }

        public boolean isSelected() {
            return selectedTestCase != null && selectedTestCase.getTestCase() == testCase;
        }

        private final class TestCasePropertyChangeListener implements PropertyChangeListener {
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals(TestCase.LABEL_PROPERTY)) {
                    label.setEnabled(!testCase.isDisabled());
                    label.setText(testCase.getLabel());
                } else if (evt.getPropertyName().equals(TestCase.DISABLED_PROPERTY)) {
                    initPopup(testCase);
                }
            }
        }

        protected TestCase getTestCase() {
            return testCase;
        }

        public ModelItem getModelItem() {
            return testCase;
        }

        public void autoscroll(Point pt) {
            int ix = getIndexOf(this);
            if (pt.getY() < 12 && ix > 0) {
                Rectangle bounds = JTestSuiteTestCaseList.this.getComponent(ix - 1).getBounds();
                JTestSuiteTestCaseList.this.scrollRectToVisible(bounds);
            } else if (pt.getY() > getHeight() - 12 && ix < testSuite.getTestCaseCount() - 1) {
                Rectangle bounds = JTestSuiteTestCaseList.this.getComponent(ix + 1).getBounds();
                JTestSuiteTestCaseList.this.scrollRectToVisible(bounds);
            }
        }

        public Insets getAutoscrollInsets() {
            return autoscrollSupport.getAutoscrollInsets();
        }

        private final class TestCaseListPanelKeyHandler extends KeyAdapter {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyChar() == KeyEvent.VK_ENTER) {
                    UISupport.selectAndShow(testCase);
                    e.consume();
                } else {
                    ActionList actions = ActionListBuilder.buildActions(testCase);
                    if (actions != null) {
                        actions.dispatchKeyEvent(e);
                    }
                }
            }
        }
    }

    protected int getIndexOf(TestCaseListPanel panel) {
        return Arrays.asList(getComponents()).indexOf(panel);
    }

    protected TestCaseListPanel createTestCaseListPanel(TestCase testCase) {
        TestCaseListPanel testCaseListPanel = new TestCaseListPanel((WsdlTestCase) testCase);

        DragSource dragSource = DragSource.getDefaultDragSource();

        SoapUIDragAndDropHandler dragAndDropHandler = new SoapUIDragAndDropHandler(new TestCaseListPanelDragAndDropable(
                testCaseListPanel), DropType.BEFORE_AND_AFTER);

        dragSource.createDefaultDragGestureRecognizer(testCaseListPanel, DnDConstants.ACTION_COPY_OR_MOVE,
                dragAndDropHandler);

        return testCaseListPanel;
    }

    private class TestCaseListDragAndDropable implements SoapUIDragAndDropable<ModelItem> {
        private final JTestSuiteTestCaseList list;

        public TestCaseListDragAndDropable(JTestSuiteTestCaseList list) {
            this.list = list;
        }

        public JComponent getComponent() {
            return list;
        }

        public Rectangle getModelItemBounds(ModelItem modelItem) {
            return list.getBounds();
        }

        public ModelItem getModelItemForLocation(int x, int y) {
            int testCaseCount = testSuite.getTestCaseCount();
            return testCaseCount == 0 ? testSuite : testSuite.getTestCaseAt(testCaseCount - 1);
        }

        public Component getRenderer(ModelItem modelItem) {
            return null;
        }

        public void selectModelItem(ModelItem modelItem) {
        }

        public void setDragInfo(String dropInfo) {
            list.setToolTipText(dropInfo);
        }

        public void toggleExpansion(ModelItem modelItem) {
        }
    }

    private static class TestCaseListPanelDragAndDropable implements SoapUIDragAndDropable<ModelItem> {
        private final TestCaseListPanel testCasePanel;

        public TestCaseListPanelDragAndDropable(TestCaseListPanel testCasePanel) {
            this.testCasePanel = testCasePanel;
        }

        public JComponent getComponent() {
            return testCasePanel;
        }

        public void setDragInfo(String dropInfo) {
            testCasePanel.setToolTipText(dropInfo.length() == 0 ? null : dropInfo);
        }

        public Rectangle getModelItemBounds(ModelItem path) {
            return new Rectangle(testCasePanel.getSize());
        }

        public ModelItem getModelItemForLocation(int x, int y) {
            return testCasePanel.getModelItem();
        }

        public Component getRenderer(ModelItem path) {
            return null;
        }

        public void selectModelItem(ModelItem path) {
            testCasePanel.setSelected(!testCasePanel.isSelected());
        }

        public void toggleExpansion(ModelItem last) {
        }
    }
}
