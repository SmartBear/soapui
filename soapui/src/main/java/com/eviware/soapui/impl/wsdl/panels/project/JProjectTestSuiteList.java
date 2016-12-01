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

package com.eviware.soapui.impl.wsdl.panels.project;

import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.impl.wsdl.actions.project.AddNewTestSuiteAction;
import com.eviware.soapui.impl.wsdl.panels.support.ProgressBarTestSuiteAdapter;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.support.ProjectListenerAdapter;
import com.eviware.soapui.model.testsuite.TestSuite;
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
import java.awt.Font;
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
 * A panel showing a scrollable list of TestSuites in a Project.
 *
 * @author Ole.Matzura
 */

public class JProjectTestSuiteList extends JPanel {
    private Map<TestSuite, TestSuiteListPanel> panels = new HashMap<TestSuite, TestSuiteListPanel>();
    private final WsdlProject project;
    private final InternalTestSuiteListener testSuiteListener = new InternalTestSuiteListener();
    private TestSuiteListPanel selectedTestSuite;

    public JProjectTestSuiteList(WsdlProject testSuite) {
        this.project = testSuite;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        for (int c = 0; c < testSuite.getTestSuiteCount(); c++) {
            TestSuiteListPanel testSuiteListPanel = createTestSuiteListPanel(testSuite.getTestSuiteAt(c));
            panels.put(testSuite.getTestSuiteAt(c), testSuiteListPanel);
            add(testSuiteListPanel);
        }

        add(Box.createVerticalGlue());
        setBackground(Color.WHITE);

        testSuite.addProjectListener(testSuiteListener);

        ActionList actions = ActionListBuilder.buildActions(testSuite);
        actions.removeAction(0);
        actions.removeAction(0);
        setComponentPopupMenu(ActionSupport.buildPopup(actions));

        DragSource dragSource = DragSource.getDefaultDragSource();

        SoapUIDragAndDropHandler dragAndDropHandler = new SoapUIDragAndDropHandler(new TestSuiteListDragAndDropable(
                this), DropType.AFTER);

        dragSource.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY_OR_MOVE, dragAndDropHandler);
    }

    public void reset() {
        for (TestSuiteListPanel testSuitePanel : panels.values()) {
            testSuitePanel.reset();
        }
    }

    @Override
    public void addNotify() {
        super.addNotify();
        project.addProjectListener(testSuiteListener);
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        project.removeProjectListener(testSuiteListener);
    }

    private final class InternalTestSuiteListener extends ProjectListenerAdapter {
        public void testSuiteAdded(TestSuite testSuite) {
            TestSuiteListPanel testSuiteListPanel = createTestSuiteListPanel(testSuite);
            panels.put(testSuite, testSuiteListPanel);
            add(testSuiteListPanel, testSuite.getProject().getIndexOfTestSuite(testSuite));
            revalidate();
            repaint();
        }

        public void testSuiteRemoved(TestSuite testSuite) {
            TestSuiteListPanel testSuiteListPanel = panels.get(testSuite);
            if (testSuiteListPanel != null) {
                remove(testSuiteListPanel);
                panels.remove(testSuite);
                revalidate();
                repaint();
            }
        }

        public void testSuiteMoved(TestSuite testSuite, int index, int offset) {
            TestSuiteListPanel testSuiteListPanel = panels.get(testSuite);
            if (testSuiteListPanel != null) {
                boolean hadFocus = testSuiteListPanel.hasFocus();

                remove(testSuiteListPanel);
                add(testSuiteListPanel, index + offset);

                revalidate();
                repaint();

                if (hadFocus) {
                    testSuiteListPanel.requestFocus();
                }
            }
        }
    }

    public final class TestSuiteListPanel extends JPanel implements Autoscroll {
        private final WsdlTestSuite testSuite;
        private JProgressBar progressBar;
        private JLabel label;
        private ProgressBarTestSuiteAdapter progressBarAdapter;
        private TestSuitePropertyChangeListener testSuitePropertyChangeListener;
        private AutoscrollSupport autoscrollSupport;

        public TestSuiteListPanel(WsdlTestSuite testSuite) {
            super(new BorderLayout());

            setFocusable(true);

            this.testSuite = testSuite;
            autoscrollSupport = new AutoscrollSupport(this);

            progressBar = new JProgressBar(0, 100) {
                protected void processMouseEvent(MouseEvent e) {
                    if (e.getID() == MouseEvent.MOUSE_PRESSED || e.getID() == MouseEvent.MOUSE_RELEASED) {
                        TestSuiteListPanel.this.processMouseEvent(translateMouseEvent(e));
                    }
                }

                protected void processMouseMotionEvent(MouseEvent e) {
                    TestSuiteListPanel.this.processMouseMotionEvent(translateMouseEvent(e));
                }

                /**
                 * Translates the given mouse event to the enclosing map panel's
                 * coordinate space.
                 */
                private MouseEvent translateMouseEvent(MouseEvent e) {
                    return new MouseEvent(TestSuiteListPanel.this, e.getID(), e.getWhen(), e.getModifiers(), e.getX()
                            + getX(), e.getY() + getY(), e.getClickCount(), e.isPopupTrigger(), e.getButton());
                }
            };

            JPanel progressPanel = UISupport.createProgressBarPanel(progressBar, 5, false);

            progressBar.setMinimumSize(new Dimension(0, 10));
            progressBar.setBackground(Color.WHITE);
            progressBar.setInheritsPopupMenu(true);

            label = new JLabel(testSuite.getLabel());
            label.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            label.setInheritsPopupMenu(true);
            label.setEnabled(!testSuite.isDisabled());

            if (UISupport.isMac()) {
                Font oldFont = label.getFont();
                Font newFont = new Font(oldFont.getName(), Font.BOLD, oldFont.getSize());
                label.setFont(newFont);
            }

            add(progressPanel, BorderLayout.CENTER);
            add(label, BorderLayout.NORTH);

            testSuitePropertyChangeListener = new TestSuitePropertyChangeListener();

            initPopup(testSuite);

            addMouseListener(new MouseAdapter() {

                @Override
                public void mousePressed(MouseEvent e) {
                    requestFocus();
                }

                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() < 2) {
                        if (selectedTestSuite != null) {
                            selectedTestSuite.setSelected(false);
                        }

                        setSelected(true);
                        selectedTestSuite = TestSuiteListPanel.this;
                        return;
                    }

                    UISupport.selectAndShow(TestSuiteListPanel.this.testSuite);
                }
            });

            addKeyListener(new TestSuiteListPanelKeyHandler());

            // init border
            setSelected(false);
        }

        public void reset() {
            progressBar.setValue(0);
            progressBar.setString("");
        }

        private void initPopup(WsdlTestSuite testSuite) {
            ActionList actions = ActionListBuilder.buildActions(testSuite);
            actions.insertAction(
                    SwingActionDelegate.createDelegate(AddNewTestSuiteAction.SOAPUI_ACTION_ID, project, null, null), 0);
            actions.insertAction(ActionSupport.SEPARATOR_ACTION, 1);

            setComponentPopupMenu(ActionSupport.buildPopup(actions));
        }

        public void addNotify() {
            super.addNotify();
            testSuite.addPropertyChangeListener(testSuitePropertyChangeListener);
            progressBarAdapter = new ProgressBarTestSuiteAdapter(progressBar, testSuite);
        }

        public void removeNotify() {
            super.removeNotify();
            if (progressBarAdapter != null) {
                testSuite.removePropertyChangeListener(testSuitePropertyChangeListener);
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
            return selectedTestSuite != null && selectedTestSuite.getTestSuite() == testSuite;
        }

        private final class TestSuitePropertyChangeListener implements PropertyChangeListener {
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals(TestSuite.LABEL_PROPERTY)) {
                    label.setEnabled(!testSuite.isDisabled());
                    label.setText(testSuite.getLabel());
                } else if (evt.getPropertyName().equals(TestSuite.DISABLED_PROPERTY)) {
                    initPopup(testSuite);
                }
            }
        }

        protected WsdlTestSuite getTestSuite() {
            return testSuite;
        }

        public ModelItem getModelItem() {
            return testSuite;
        }

        public void autoscroll(Point pt) {
            int ix = getIndexOf(this);
            if (pt.getY() < 12 && ix > 0) {
                Rectangle bounds = JProjectTestSuiteList.this.getComponent(ix - 1).getBounds();
                JProjectTestSuiteList.this.scrollRectToVisible(bounds);
            } else if (pt.getY() > getHeight() - 12 && ix < project.getTestSuiteCount() - 1) {
                Rectangle bounds = JProjectTestSuiteList.this.getComponent(ix + 1).getBounds();
                JProjectTestSuiteList.this.scrollRectToVisible(bounds);
            }
        }

        public Insets getAutoscrollInsets() {
            return autoscrollSupport.getAutoscrollInsets();
        }

        private final class TestSuiteListPanelKeyHandler extends KeyAdapter {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyChar() == KeyEvent.VK_ENTER) {
                    UISupport.selectAndShow(testSuite);
                    e.consume();
                } else {
                    ActionList actions = ActionListBuilder.buildActions(testSuite);
                    if (actions != null) {
                        actions.dispatchKeyEvent(e);
                    }
                }
            }
        }
    }

    protected int getIndexOf(TestSuiteListPanel panel) {
        return Arrays.asList(getComponents()).indexOf(panel);
    }

    protected TestSuiteListPanel createTestSuiteListPanel(TestSuite testSuite) {
        TestSuiteListPanel testSuiteListPanel = new TestSuiteListPanel((WsdlTestSuite) testSuite);

        DragSource dragSource = DragSource.getDefaultDragSource();

        SoapUIDragAndDropHandler dragAndDropHandler = new SoapUIDragAndDropHandler(
                new TestSuiteListPanelDragAndDropable(testSuiteListPanel), DropType.BEFORE_AND_AFTER);

        dragSource.createDefaultDragGestureRecognizer(testSuiteListPanel, DnDConstants.ACTION_COPY_OR_MOVE,
                dragAndDropHandler);

        return testSuiteListPanel;
    }

    private class TestSuiteListDragAndDropable implements SoapUIDragAndDropable<ModelItem> {
        private final JProjectTestSuiteList list;

        public TestSuiteListDragAndDropable(JProjectTestSuiteList list) {
            this.list = list;
        }

        public JComponent getComponent() {
            return list;
        }

        public Rectangle getModelItemBounds(ModelItem modelItem) {
            return list.getBounds();
        }

        public ModelItem getModelItemForLocation(int x, int y) {
            int testSuiteCount = project.getTestSuiteCount();
            return testSuiteCount == 0 ? project : project.getTestSuiteAt(testSuiteCount - 1);
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

    private static class TestSuiteListPanelDragAndDropable implements SoapUIDragAndDropable<ModelItem> {
        private final TestSuiteListPanel testSuitePanel;

        public TestSuiteListPanelDragAndDropable(TestSuiteListPanel testSuitePanel) {
            this.testSuitePanel = testSuitePanel;
        }

        public JComponent getComponent() {
            return testSuitePanel;
        }

        public void setDragInfo(String dropInfo) {
            testSuitePanel.setToolTipText(dropInfo.length() == 0 ? null : dropInfo);
        }

        public Rectangle getModelItemBounds(ModelItem path) {
            return new Rectangle(testSuitePanel.getSize());
        }

        public ModelItem getModelItemForLocation(int x, int y) {
            return testSuitePanel.getModelItem();
        }

        public Component getRenderer(ModelItem path) {
            return null;
        }

        public void selectModelItem(ModelItem path) {
            testSuitePanel.setSelected(!testSuitePanel.isSelected());
        }

        public void toggleExpansion(ModelItem last) {
        }
    }
}
