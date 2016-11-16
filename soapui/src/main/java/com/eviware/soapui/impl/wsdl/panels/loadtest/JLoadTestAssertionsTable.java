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

package com.eviware.soapui.impl.wsdl.panels.loadtest;

import com.eviware.soapui.analytics.Analytics;
import com.eviware.soapui.analytics.SoapUIActions;
import com.eviware.soapui.impl.support.actions.ShowOnlineHelpAction;
import com.eviware.soapui.impl.wsdl.loadtest.LoadTestAssertion;
import com.eviware.soapui.impl.wsdl.loadtest.LoadTestListener;
import com.eviware.soapui.impl.wsdl.loadtest.WsdlLoadTest;
import com.eviware.soapui.impl.wsdl.loadtest.assertions.LoadTestAssertionRegistry;
import com.eviware.soapui.impl.wsdl.support.Configurable;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.support.swing.JTableFactory;
import org.jdesktop.swingx.JXTable;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Table showing configured assertions for a WsdlLoadTest
 *
 * @author Ole.Matzura
 * @todo add popup menu
 */

public class JLoadTestAssertionsTable extends JPanel {
    private JXTable table;
    private final WsdlLoadTest loadTest;
    private ConfigureAssertionAction configureAssertionAction;
    private RemoveAssertionAction removeAssertionAction;
    private AddLoadTestAssertionAction addLoadTestAssertionAction;
    private LoadTestAssertionsTableModel tableModel;
    private JPopupMenu assertionPopup;
    private InternalLoadTestListener internalLoadTestListener = new InternalLoadTestListener();

    public JLoadTestAssertionsTable(WsdlLoadTest wsdlLoadTest) {
        super(new BorderLayout());
        this.loadTest = wsdlLoadTest;

        loadTest.addLoadTestListener(internalLoadTestListener);

        tableModel = new LoadTestAssertionsTableModel();
        table = JTableFactory.getInstance().makeJXTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        TableColumnModel columnModel = table.getColumnModel();
        columnModel.getColumn(0).setMaxWidth(16);
        columnModel.getColumn(0).setCellRenderer(new IconTableCellRenderer());
        columnModel.getColumn(1).setPreferredWidth(100);
        columnModel.getColumn(2).setPreferredWidth(100);
        columnModel.getColumn(3).setPreferredWidth(200);

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        table.addMouseListener(new MouseAdapter() {

            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() < 2) {
                    return;
                }

                int ix = table.getSelectedRow();
                if (ix == -1) {
                    return;
                }
                ix = table.convertRowIndexToModel(ix);

                Object obj = loadTest.getAssertionAt(ix);
                if (obj instanceof Configurable) {
                    ((Configurable) obj).configure();
                } else {
                    Toolkit.getDefaultToolkit().beep();
                }
            }
        });

        add(buildToolbar(), BorderLayout.NORTH);

        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent e) {
                int ix = table.getSelectedRow();

                configureAssertionAction.setEnabled(ix >= 0);
                removeAssertionAction.setEnabled(ix >= 0);

                if (ix == -1) {
                    return;
                }

                ix = table.convertRowIndexToModel(ix);
                configureAssertionAction.setEnabled(loadTest.getAssertionAt(ix) instanceof Configurable);
            }
        });

        assertionPopup = new JPopupMenu();
        assertionPopup.add(configureAssertionAction);
        assertionPopup.addSeparator();
        assertionPopup.add(addLoadTestAssertionAction);
        assertionPopup.add(removeAssertionAction);

        setComponentPopupMenu(assertionPopup);

        scrollPane.setInheritsPopupMenu(true);
        table.setComponentPopupMenu(assertionPopup);
    }

    public void release() {
        tableModel.release();
        loadTest.removeLoadTestListener(internalLoadTestListener);
    }

    private JComponent buildToolbar() {
        configureAssertionAction = new ConfigureAssertionAction();
        removeAssertionAction = new RemoveAssertionAction();
        addLoadTestAssertionAction = new AddLoadTestAssertionAction();

        JXToolBar toolbar = UISupport.createSmallToolbar();

        JButton button = UISupport.createToolbarButton(addLoadTestAssertionAction);
        toolbar.addFixed(button);
        button = UISupport.createToolbarButton(removeAssertionAction);
        toolbar.addFixed(button);
        button = UISupport.createToolbarButton(configureAssertionAction);
        toolbar.addFixed(button);
        toolbar.addGlue();
        toolbar.add(new ShowOnlineHelpAction(HelpUrls.LOADTEST_ASSERTIONS_URL));

        return toolbar;
    }

    private class LoadTestAssertionsTableModel extends AbstractTableModel implements PropertyChangeListener {
        public LoadTestAssertionsTableModel() {
            for (int c = 0; c < loadTest.getAssertionCount(); c++) {
                loadTest.getAssertionAt(c).addPropertyChangeListener(LoadTestAssertion.CONFIGURATION_PROPERTY, this);
            }
        }

        public void release() {
            for (int c = 0; c < loadTest.getAssertionCount(); c++) {
                loadTest.getAssertionAt(c).removePropertyChangeListener(LoadTestAssertion.CONFIGURATION_PROPERTY, this);
            }
        }

        public int getRowCount() {
            return loadTest.getAssertionCount();
        }

        public int getColumnCount() {
            return 4;
        }

        public Class<?> getColumnClass(int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return ImageIcon.class;
                default:
                    return String.class;
            }
        }

        public String getColumnName(int column) {
            switch (column) {
                case 0:
                    return " ";
                case 1:
                    return "Name";
                case 2:
                    return "Step";
                case 3:
                    return "Details";
            }

            return null;
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            LoadTestAssertion assertion = loadTest.getAssertionAt(rowIndex);

            switch (columnIndex) {
                case 0:
                    return assertion.getIcon();
                case 1:
                    return assertion.getName();
                case 2:
                    return assertion.getTargetStep();
                case 3:
                    return assertion.getDescription();
            }

            return null;
        }

        public void assertionRemoved(LoadTestAssertion assertion) {
            assertion.removePropertyChangeListener(LoadTestAssertion.CONFIGURATION_PROPERTY, this);
            fireTableDataChanged();
        }

        public void assertionAdded(LoadTestAssertion assertion) {
            assertion.addPropertyChangeListener(LoadTestAssertion.CONFIGURATION_PROPERTY, this);
            fireTableRowsInserted(getRowCount() - 1, getRowCount() - 1);
        }

        public void propertyChange(PropertyChangeEvent evt) {
            fireTableDataChanged();
        }
    }

    private static final class IconTableCellRenderer extends DefaultTableCellRenderer {
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            if (value != null) {
                setIcon((Icon) value);
            }

            if (isSelected) {
                setBackground(table.getSelectionBackground());
                setForeground(table.getSelectionForeground());
            } else {
                setBackground(table.getBackground());
                setForeground(table.getForeground());
            }

            return this;
        }
    }

    public class AddLoadTestAssertionAction extends AbstractAction {
        public AddLoadTestAssertionAction() {
            super("Add Assertion");

            putValue(Action.SHORT_DESCRIPTION, "Adds an assertion to this LoadTest");
            putValue(Action.SMALL_ICON, UISupport.createImageIcon("/add.png"));
        }

        public void actionPerformed(ActionEvent e) {
            String[] types = LoadTestAssertionRegistry.getAvailableAssertions();
            String type = (String) UISupport.prompt("Select assertion type to add", "Add Assertion", types);
            if (type != null) {
                loadTest.addAssertion(type, LoadTestAssertion.ANY_TEST_STEP, true);
                Analytics.trackAction(SoapUIActions.ADD_LOAD_TEST_ASSERTION.getActionName());
            }
        }
    }

    public class ConfigureAssertionAction extends AbstractAction {
        ConfigureAssertionAction() {
            super("Configure");
            putValue(Action.SHORT_DESCRIPTION, "Configures the selection assertion");
            putValue(Action.SMALL_ICON, UISupport.createImageIcon("/preferences.png"));
            setEnabled(false);
        }

        public void actionPerformed(ActionEvent e) {
            int ix = table.getSelectedRow();
            if (ix == -1) {
                return;
            }
            ix = table.convertRowIndexToModel(ix);

            Object obj = loadTest.getAssertionAt(ix);
            if (obj instanceof Configurable) {
                ((Configurable) obj).configure();
                tableModel.fireTableRowsUpdated(ix, ix);
            } else {
                Toolkit.getDefaultToolkit().beep();
            }
        }
    }

    public class RemoveAssertionAction extends AbstractAction {
        public RemoveAssertionAction() {
            super("Remove Assertion");
            putValue(Action.SHORT_DESCRIPTION, "Removes the selected assertion from this LoadTest");
            putValue(Action.SMALL_ICON, UISupport.createImageIcon("/remove_assertion.gif"));
            setEnabled(false);
        }

        public void actionPerformed(ActionEvent e) {
            int ix = table.getSelectedRow();
            if (ix == -1) {
                return;
            }
            ix = table.convertRowIndexToModel(ix);

            LoadTestAssertion assertion = loadTest.getAssertionAt(ix);
            if (UISupport.confirm("Remove assertion [" + assertion.getName() + "]", "Remove Assertion")) {
                loadTest.removeAssertion(assertion);
            }
        }
    }

    public class InternalLoadTestListener implements LoadTestListener {
        public void assertionAdded(LoadTestAssertion assertion) {
            tableModel.assertionAdded(assertion);
            table.getSelectionModel().setSelectionInterval(tableModel.getRowCount() - 1, tableModel.getRowCount() - 1);
        }

        public void assertionRemoved(LoadTestAssertion assertion) {
            tableModel.assertionRemoved(assertion);
        }
    }
}
