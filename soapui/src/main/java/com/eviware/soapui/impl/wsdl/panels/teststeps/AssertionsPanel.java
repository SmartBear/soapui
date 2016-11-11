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

package com.eviware.soapui.impl.wsdl.panels.teststeps;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.support.actions.ShowOnlineHelpAction;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.impl.wsdl.teststeps.actions.AddAssertionAction;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.model.testsuite.AssertionError;
import com.eviware.soapui.model.testsuite.AssertionsListener;
import com.eviware.soapui.model.testsuite.TestAssertion;
import com.eviware.soapui.model.testsuite.TestCase;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.swing.ActionList;
import com.eviware.soapui.support.action.swing.ActionListBuilder;
import com.eviware.soapui.support.action.swing.ActionSupport;
import com.eviware.soapui.support.components.JXToolBar;

import javax.swing.AbstractAction;
import javax.swing.AbstractListModel;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Seperate panel for holding/managing assertions
 *
 * @author ole.matzura
 */

public class AssertionsPanel extends JPanel {
    protected AssertionListModel assertionListModel;
    protected JList assertionList;
    private JPopupMenu assertionListPopup;
    private Assertable assertable;
    private AddAssertionAction addAssertionAction;
    private ConfigureAssertionAction configureAssertionAction;
    private RemoveAssertionAction removeAssertionAction;
    private MoveAssertionUpAction moveAssertionUpAction;
    private MoveAssertionDownAction moveAssertionDownAction;

    public AssertionsPanel(Assertable assertable) {
        super(new BorderLayout());
        this.assertable = assertable;

        initListAndModel();

        assertionList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        assertionListPopup = new JPopupMenu();
        addAssertionAction = new AddAssertionAction(assertable);
        assertionListPopup.add(addAssertionAction);

        assertionListPopup.addPopupMenuListener(new PopupMenuListener() {

            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                while (assertionListPopup.getComponentCount() > 1) {
                    assertionListPopup.remove(1);
                }

                int ix = assertionList.getSelectedIndex();
                if (ix == -1) {
                    assertionListPopup.addSeparator();
                    assertionListPopup.add(new ShowOnlineHelpAction(getHelpUrl()));
                    return;
                }
                int[] indices = assertionList.getSelectedIndices();
                if (indices.length == 1) {
                    TestAssertion assertion = assertionListModel.getAssertionAt(ix);
                    ActionSupport.addActions(ActionListBuilder.buildActions(assertion), assertionListPopup);

                } else {
                    TestAssertion[] testAssertion = new TestAssertion[indices.length];
                    for (int c = 0; c < indices.length; c++) {
                        testAssertion[c] = assertionListModel.getAssertionAt(indices[c]);
                    }

                    ActionSupport.addActions(ActionListBuilder.buildMultiActions(testAssertion), assertionListPopup);
                }
            }

            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
            }

            public void popupMenuCanceled(PopupMenuEvent e) {
            }
        });

        assertionList.setComponentPopupMenu(assertionListPopup);

        assertionList.addMouseListener(new MouseAdapter() {

            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() < 2) {
                    return;
                }

                int ix = assertionList.getSelectedIndex();
                if (ix == -1) {
                    return;
                }

                Object obj = assertionList.getModel().getElementAt(ix);
                if (obj instanceof TestAssertion) {
                    TestAssertion assertion = (TestAssertion) obj;
                    if (assertion.isConfigurable()) {
                        assertion.configure();
                    }

                    return;
                }

                if (obj instanceof AssertionError) {
                    AssertionError error = (AssertionError) obj;
                    if (error.getLineNumber() >= 0) {
                        selectError(error);
                    } else {
                        Toolkit.getDefaultToolkit().beep();
                    }
                } else {
                    Toolkit.getDefaultToolkit().beep();
                }
            }
        });

        assertionList.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                int ix = assertionList.getSelectedIndex();
                if (ix == -1) {
                    return;
                }

                int[] indices = assertionList.getSelectedIndices();
                if (indices.length == 1) {
                    TestAssertion assertion = assertionListModel.getAssertionAt(ix);
                    if (e.getKeyChar() == KeyEvent.VK_ENTER) {
                        if (assertion.isConfigurable()) {
                            assertion.configure();
                        }
                    } else {
                        ActionList actions = ActionListBuilder.buildActions(assertion);
                        if (actions != null) {
                            actions.dispatchKeyEvent(e);
                        }
                    }
                } else {
                    TestAssertion[] testAssertion = new TestAssertion[indices.length];
                    for (int c = 0; c < indices.length; c++) {
                        testAssertion[c] = assertionListModel.getAssertionAt(indices[c]);
                    }

                    ActionList actions = ActionListBuilder.buildMultiActions(testAssertion);
                    ActionSupport.addActions(actions, assertionListPopup);

                    if (actions != null) {
                        actions.dispatchKeyEvent(e);
                    }
                }
            }
        });

        add(new JScrollPane(assertionList), BorderLayout.CENTER);
        add(buildToolbar(), BorderLayout.NORTH);
    }

    /**
     *
     */
    protected void initListAndModel() {
        assertionListModel = new AssertionListModel();
        assertionList = new JList(assertionListModel);
        assertionList.setToolTipText("Assertions for this request");
        assertionList.setCellRenderer(new AssertionCellRenderer());
    }

    private JComponent buildToolbar() {
        configureAssertionAction = new ConfigureAssertionAction();
        removeAssertionAction = new RemoveAssertionAction();
        moveAssertionUpAction = new MoveAssertionUpAction();
        moveAssertionDownAction = new MoveAssertionDownAction();

        JXToolBar toolbar = UISupport.createToolbar();
        addToolbarButtons(toolbar);

        toolbar.addGlue();
        toolbar.add(new ShowOnlineHelpAction(getHelpUrl()));

        assertionList.addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent e) {
                int ix = assertionList.getSelectedIndex();

                configureAssertionAction.setEnabled(ix >= 0);
                removeAssertionAction.setEnabled(ix >= 0);
                moveAssertionUpAction.setEnabled(ix >= 0);
                moveAssertionDownAction.setEnabled(ix >= 0);

                if (ix == -1) {
                    return;
                }
                TestAssertion assertion = assertionListModel.getAssertionAt(ix);
                configureAssertionAction.setEnabled(assertion != null && assertion.isConfigurable());
            }
        });

        return toolbar;
    }

    protected void addToolbarButtons(JXToolBar toolbar) {
        toolbar.addFixed(UISupport.createToolbarButton(addAssertionAction));
        toolbar.addFixed(UISupport.createToolbarButton(configureAssertionAction));
        toolbar.addFixed(UISupport.createToolbarButton(removeAssertionAction));
        toolbar.addFixed(UISupport.createToolbarButton(moveAssertionUpAction));
        toolbar.addFixed(UISupport.createToolbarButton(moveAssertionDownAction));
    }

    public void setEnabled(boolean enabled) {
        assertionList.setEnabled(enabled);
    }

    protected void selectError(AssertionError error) {
    }

    private static class AssertionCellRenderer extends JLabel implements ListCellRenderer {
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                                                      boolean cellHasFocus) {
            setEnabled(list.isEnabled());

            if (value instanceof TestAssertion) {
                TestAssertion assertion = (TestAssertion) value;
                setText(assertion.getLabel() + " - " + assertion.getStatus().toString());
                setIcon(assertion.getIcon());

                if (assertion.isDisabled() && isEnabled()) {
                    setEnabled(false);
                }
            } else if (value instanceof AssertionError) {
                AssertionError assertion = (AssertionError) value;
                setText(" -> " + assertion.toString());
                setIcon(null);
            } else if (value instanceof String) {
                setText(value.toString());
            }

            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }

            setFont(list.getFont());
            setOpaque(true);

            return this;
        }
    }

    protected class AssertionListModel extends AbstractListModel implements PropertyChangeListener, AssertionsListener {
        protected List<Object> items = new ArrayList<Object>();

        public AssertionListModel() {
            init();
        }

        public int getSize() {
            return items.size();
        }

        public Object getElementAt(int index) {
            return index >= items.size() ? null : items.get(index);
        }

        public TestAssertion getAssertionAt(int index) {
            Object object = items.get(index);
            while (!(object instanceof TestAssertion) && index > 0) {
                object = items.get(--index);
            }

            return (TestAssertion) ((object instanceof TestAssertion) ? object : null);
        }

        public void refresh() {
            synchronized (this) {
                release();
                init();
                fireContentsChanged(this, 0, getSize() - 1);
            }
        }

        private void init() {
            assertable.addAssertionsListener(this);

            for (int c = 0; c < assertable.getAssertionCount(); c++) {
                TestAssertion assertion = assertable.getAssertionAt(c);
                addAssertion(assertion);
            }
        }

        public void release() {
            items.clear();

            for (int c = 0; c < assertable.getAssertionCount(); c++) {
                TestAssertion assertion = assertable.getAssertionAt(c);
                assertion.removePropertyChangeListener(this);
            }

            assertable.removeAssertionsListener(this);
        }

        public synchronized void propertyChange(PropertyChangeEvent evt) {
            if (SwingUtilities.isEventDispatchThread()) {
                refresh();
            } else {
                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        refresh();
                    }
                });
            }
        }

        public void assertionAdded(TestAssertion assertion) {
            synchronized (this) {
                int sz = getSize();
                addAssertion(assertion);

                fireIntervalAdded(this, sz, items.size() - 1);
            }
        }

        protected void addAssertion(TestAssertion assertion) {
            assertion.addPropertyChangeListener(this);
            items.add(assertion);

            AssertionError[] errors = assertion.getErrors();
            if (errors != null) {
                for (int i = 0; i < errors.length; i++) {
                    items.add(errors[i]);
                }
            }
        }

        public void assertionRemoved(TestAssertion assertion) {
            synchronized (this) {
                int ix = items.indexOf(assertion);
                if (ix == -1) {
                    return;
                }

                assertion.removePropertyChangeListener(this);
                items.remove(ix);
                fireIntervalRemoved(this, ix, ix);

                // remove associated errors
                while (ix < items.size() && items.get(ix) instanceof AssertionError) {
                    items.remove(ix);
                    fireIntervalRemoved(this, ix, ix);
                }
            }
        }

        public void assertionMoved(TestAssertion newAssertion, int ix, int offset) {
            synchronized (this) {
                // int ix = items.indexOf( assertion );
                TestAssertion assertion = (TestAssertion) items.get(ix);
                // if first selected can't move up and if last selected can't move
                // down
                if ((ix == 0 && offset == -1) || (ix == items.size() - 1 && offset == 1)) {
                    return;
                }

                assertion.removePropertyChangeListener(this);
                items.remove(ix);
                fireIntervalRemoved(this, ix, ix);

                // remove associated errors
                while (ix < items.size() && items.get(ix) instanceof AssertionError) {
                    items.remove(ix);
                    fireIntervalRemoved(this, ix, ix);
                }
                newAssertion.addPropertyChangeListener(this);
                items.add(ix + offset, newAssertion);
                fireIntervalAdded(this, ix + offset, ix + offset);
                // add associated errors
                while (ix < items.size() && items.get(ix) instanceof AssertionError) {
                    items.add(newAssertion);
                    fireIntervalAdded(this, ix + offset, ix + offset);
                }
            }
        }

    }

    public void release() {
        assertionListModel.release();
        addAssertionAction.release();
        assertable = null;
    }

    public class ConfigureAssertionAction extends AbstractAction {
        ConfigureAssertionAction() {
            super("Configure");
            putValue(Action.SHORT_DESCRIPTION, "Configures the selection assertion");
            putValue(Action.SMALL_ICON, UISupport.createImageIcon("/preferences.png"));
            setEnabled(false);
        }

        public void actionPerformed(ActionEvent e) {
            int ix = assertionList.getSelectedIndex();
            if (ix == -1) {
                return;
            }

            TestAssertion assertion = assertionListModel.getAssertionAt(ix);
            if (assertion.isConfigurable()) {
                assertion.configure();
            } else {
                Toolkit.getDefaultToolkit().beep();
            }
        }
    }

    public class RemoveAssertionAction extends AbstractAction {
        public RemoveAssertionAction() {
            super("Remove Assertion");
            putValue(Action.SHORT_DESCRIPTION, "Removes the selected assertion");
            putValue(Action.SMALL_ICON, UISupport.createImageIcon("/remove_assertion.gif"));
            setEnabled(false);
        }

        public void actionPerformed(ActionEvent e) {

            List<TestAssertion> removeAssertionList = new ArrayList<TestAssertion>();
            int indices[] = assertionList.getSelectedIndices();

            if (indices.length == 0) {
                return;
            }

            if (hasRunningTestCase(indices[0])) {
                return;
            }

            for (int i : indices) {
                removeAssertionList.add(assertionListModel.getAssertionAt(i));
            }

            if (removeAssertionList.size() == 1) {
                int selectedIndex = assertionList.getSelectedIndex();
                removeSingleAssertion(removeAssertionList.get(0));
                if (assertionList.getLastVisibleIndex() >= selectedIndex) {
                    assertionList.setSelectedIndex(selectedIndex);
                }
            } else {
                removeMultipleAssertions(removeAssertionList);
            }

        }

        private boolean hasRunningTestCase(int assertionIndex) {
            if (assertionListModel.getAssertionAt(assertionIndex).getParent().getParent() instanceof TestCase) {
                if (SoapUI.getTestMonitor().hasRunningTestCase(
                        (TestCase) assertionListModel.getAssertionAt(assertionIndex).getParent().getParent())) {
                    UISupport.showInfoMessage("Can not remove assertion(s) while test case is running");
                    return true;
                }
            }
            if (assertionListModel.getAssertionAt(assertionIndex).getParent().getParent().getParent() instanceof TestCase) {
                if (SoapUI.getTestMonitor().hasRunningSecurityTest(
                        (TestCase) assertionListModel.getAssertionAt(assertionIndex).getParent().getParent().getParent())) {
                    UISupport.showInfoMessage("Can not remove assertion(s) while test case is running");
                    return true;
                }
            }
            return false;
        }

        private void removeMultipleAssertions(List<TestAssertion> removeAssertionList) {
            if (UISupport.confirm("Remove all selected assertions?", "Remove Multiple Assertions")) {
                // remove duplicates
                Set<TestAssertion> assertions = new HashSet<TestAssertion>();

                for (ModelItem target : removeAssertionList) {
                    assertions.add((TestAssertion) target);
                }

                for (TestAssertion assertion : assertions) {
                    ((Assertable) assertion.getParent()).removeAssertion(assertion);
                }
            }
        }

        private void removeSingleAssertion(TestAssertion assertion) {
            if (UISupport.confirm("Remove assertion [" + assertion.getName() + "]", "Remove Assertion")) {
                assertable.removeAssertion(assertion);
            }
        }
    }

    private class MoveAssertionUpAction extends AbstractAction {
        public MoveAssertionUpAction() {
            super("Move Assertion Up");
            putValue(Action.SHORT_DESCRIPTION, "Moves selected asertion up one row");
            putValue(Action.SMALL_ICON, UISupport.createImageIcon("/up_arrow.gif"));
            setEnabled(false);

        }

        public void actionPerformed(ActionEvent e) {
            int ix = assertionList.getSelectedIndex();
            TestAssertion assertion = assertionListModel.getAssertionAt(ix);
            if (ix != -1) {
                assertion = assertable.moveAssertion(ix, -1);
            }
            assertionList.setSelectedValue(assertion, true);
        }
    }

    private class MoveAssertionDownAction extends AbstractAction {
        public MoveAssertionDownAction() {
            super("Move Assertion Down");
            putValue(Action.SHORT_DESCRIPTION, "Moves selected asertion down one row");
            putValue(Action.SMALL_ICON, UISupport.createImageIcon("/down_arrow.gif"));
            setEnabled(false);

        }

        public void actionPerformed(ActionEvent e) {
            int ix = assertionList.getSelectedIndex();
            TestAssertion assertion = assertionListModel.getAssertionAt(ix);
            if (ix != -1) {
                assertion = assertable.moveAssertion(ix, 1);
            }
            assertionList.setSelectedValue(assertion, true);
        }
    }

    public JList getAssertionsList() {
        return assertionList;
    }

    public String getHelpUrl() {
        return HelpUrls.RESPONSE_ASSERTIONS_HELP_URL;
    }
}
