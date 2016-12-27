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

import com.eviware.soapui.impl.wsdl.loadtest.LoadTestAssertion;
import com.eviware.soapui.impl.wsdl.loadtest.data.actions.ExportLoadTestLogAction;
import com.eviware.soapui.impl.wsdl.loadtest.log.LoadTestLog;
import com.eviware.soapui.impl.wsdl.loadtest.log.LoadTestLogEntry;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.support.DateUtil;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.swing.ActionList;
import com.eviware.soapui.support.action.swing.ActionSupport;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.support.swing.JTableFactory;
import com.jgoodies.forms.builder.ButtonBarBuilder;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.Filter;
import org.jdesktop.swingx.decorator.FilterPipeline;
import org.jdesktop.swingx.decorator.PatternFilter;
import org.jdesktop.swingx.decorator.SortOrder;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Compound component for showing a LoadTestLog
 *
 * @author Ole.Matzura
 */

public class JLoadTestLogTable extends JPanel {
    private final LoadTestLog loadTestLog;
    private JXTable logTable;
    private PatternFilter stepFilter;
    private PatternFilter typeFilter;
    private JComboBox typesFilterComboBox;
    private JComboBox stepsFilterComboBox;
    private JButton clearErrorsButton;
    private JLabel rowCountLabel;
    @SuppressWarnings("unused")
    private JPopupMenu popup;
    private LoadTestLogTableModel logTableModel;
    private JButton exportButton;
    private LogTableModelListener logTableModelListener;

    public JLoadTestLogTable(LoadTestLog log) {
        super(new BorderLayout());

        loadTestLog = log;

        logTableModel = new LoadTestLogTableModel();
        logTable = JTableFactory.getInstance().makeJXTable(logTableModel);
        logTable.setHorizontalScrollEnabled(true);
        logTable.setColumnControlVisible(true);
        logTable.addMouseListener(new LoadTestLogTableMouseListener());

        TableColumnModel columnModel = logTable.getColumnModel();
        columnModel.getColumn(0).setMaxWidth(5);
        columnModel.getColumn(0).setCellRenderer(new IconTableCellRenderer());

        columnModel.getColumn(1).setPreferredWidth(120);
        columnModel.getColumn(1).setCellRenderer(new TimestampTableCellRenderer());

        columnModel.getColumn(2).setPreferredWidth(110);
        columnModel.getColumn(3).setPreferredWidth(110);
        columnModel.getColumn(4).setPreferredWidth(250);

        typeFilter = new PatternFilter(".*", 0, 2);
        typeFilter.setAcceptNull(true);
        stepFilter = new PatternFilter(".*", 0, 3);
        stepFilter.setAcceptNull(true);

        Filter[] filters = new Filter[]{typeFilter, // regex, matchflags, column
                stepFilter // regex, matchflags, column
        };

        FilterPipeline pipeline = new FilterPipeline(filters);
        logTable.setFilters(pipeline);

        JScrollPane scrollPane = new JScrollPane(logTable);
        add(scrollPane, BorderLayout.CENTER);
        add(buildToolbar(), BorderLayout.NORTH);
        add(buildStatus(), BorderLayout.SOUTH);

        logTableModelListener = new LogTableModelListener();
        logTable.getModel().addTableModelListener(logTableModelListener);

        logTable.setSortOrder(1, SortOrder.ASCENDING);
    }

    public void addNotify() {
        super.addNotify();
        if (logTableModelListener != null) {
            logTableModel.addTableModelListener(logTableModelListener);
        }

        loadTestLog.addListDataListener(logTableModel);
    }

    public void removeNotify() {
        super.removeNotify();
        logTableModel.removeTableModelListener(logTableModelListener);
        loadTestLog.removeListDataListener(logTableModel);
    }

    private JComponent buildStatus() {
        ButtonBarBuilder builder = new ButtonBarBuilder();
        rowCountLabel = new JLabel("0 entries");
        builder.addFixed(rowCountLabel);
        builder.addGlue();
        builder.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        return builder.getPanel();
    }

    protected void updateRowCountLabel() {
        int c = logTableModel.getRowCount();
        rowCountLabel.setText(c == 1 ? "1 entry" : c + " entries");
    }

    private JComponent buildToolbar() {
        JXToolBar toolbar = UISupport.createToolbar();

        clearErrorsButton = UISupport.createToolbarButton(new ClearErrorsAction());
        exportButton = UISupport.createToolbarButton(new ExportLoadTestLogAction(loadTestLog, logTable));

        toolbar.add(clearErrorsButton);
        toolbar.add(exportButton);
        toolbar.addGlue();

        List<Object> steps = new ArrayList<Object>();
        steps.add("- All -");
        steps.add("Message");
        for (LoadTestAssertion assertion : loadTestLog.getLoadTest().getAssertionList()) {
            steps.add(assertion.getName());
        }

        toolbar.add(new JLabel("Show Types:"));
        toolbar.addSeparator();
        typesFilterComboBox = new JComboBox(steps.toArray());
        typesFilterComboBox.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                int ix = typesFilterComboBox.getSelectedIndex();
                if (ix == -1) {
                    return;
                }

                typeFilter.setAcceptNull(ix == 0);

                if (ix == 0) {
                    typeFilter.setPattern(".*", 0);
                } else {
                    typeFilter.setPattern(typesFilterComboBox.getSelectedItem().toString(), 0);
                }

                updateRowCountLabel();
            }
        });

        toolbar.add(typesFilterComboBox);
        toolbar.addSeparator();

        List<Object> types = new ArrayList<Object>();
        types.add("- All -");
        for (TestStep testStep : loadTestLog.getLoadTest().getTestCase().getTestStepList()) {
            types.add(testStep.getName());
        }

        toolbar.addFixed(new JLabel("Show Steps:"));
        toolbar.addSeparator();
        stepsFilterComboBox = new JComboBox(types.toArray());
        stepsFilterComboBox.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                int ix = stepsFilterComboBox.getSelectedIndex();
                if (ix == -1) {
                    return;
                }

                stepFilter.setAcceptNull(ix == 0);

                if (ix == 0) {
                    stepFilter.setPattern(".*", 0);
                } else {
                    stepFilter.setPattern(stepsFilterComboBox.getSelectedItem().toString(), 0);
                }

                updateRowCountLabel();
            }
        });

        toolbar.addFixed(stepsFilterComboBox);
        // toolbar.setBorder( BorderFactory.createEmptyBorder( 0, 0, 2, 0 ));

        return toolbar; // builder.getPanel();
    }

    private final class LogTableModelListener implements TableModelListener {
        public void tableChanged(TableModelEvent e) {
            updateRowCountLabel();
        }
    }

	/*
     *
	 * private class SelectStepFilterAction extends AbstractAction { private
	 * final String filter;
	 * 
	 * public SelectStepFilterAction(String name, String filter) { super(name);
	 * this.filter = filter; }
	 * 
	 * public void actionPerformed(ActionEvent e) { stepFilter.setPattern(
	 * filter, 0 ); } }
	 * 
	 * private class SelectTypeFilterAction extends AbstractAction { private
	 * final String filter;
	 * 
	 * public SelectTypeFilterAction(String name, String filter) { super(name);
	 * this.filter = filter; }
	 * 
	 * public void actionPerformed(ActionEvent e) { typeFilter.setPattern(
	 * filter, 0 ); } }
	 */

    private class LoadTestLogTableModel extends AbstractTableModel implements ListDataListener {
        public LoadTestLogTableModel() {
        }

        public int getRowCount() {
            return loadTestLog.getSize();
        }

        public int getColumnCount() {
            return 5;
        }

        public Class<?> getColumnClass(int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return ImageIcon.class;
                case 1:
                    return Date.class;
                default:
                    return String.class;
            }
        }

        public String getColumnName(int column) {
            switch (column) {
                case 0:
                    return " ";
                case 1:
                    return "time";
                case 2:
                    return "type";
                case 3:
                    return "step";
                case 4:
                    return "message";
            }

            return null;
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            if (rowIndex == -1) {
                return null;
            }

            LoadTestLogEntry entry = (LoadTestLogEntry) loadTestLog.getElementAt(rowIndex);

            switch (columnIndex) {
                case 0:
                    return entry.getIcon();
                case 1:
                    return entry.getTimeStamp();
                case 2:
                    return entry.getType();
                case 3:
                    return entry.getTargetStepName();
                case 4:
                    return entry.getMessage();
            }

            return null;
        }

        public void intervalAdded(ListDataEvent e) {
            fireTableRowsInserted(e.getIndex0(), e.getIndex1());
        }

        public void intervalRemoved(ListDataEvent e) {
            fireTableRowsDeleted(e.getIndex0(), e.getIndex1());
        }

        public void contentsChanged(ListDataEvent e) {
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

    private static final class TimestampTableCellRenderer extends DefaultTableCellRenderer {

        private TimestampTableCellRenderer() {
        }

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            if (value != null) {
                setText(DateUtil.formatExtraFull(new Date((Long) value)));
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

    public class ClearErrorsAction extends AbstractAction {
        public ClearErrorsAction() {
            putValue(Action.SMALL_ICON, UISupport.createImageIcon("/clear_errors.gif"));
            putValue(Action.SHORT_DESCRIPTION, "Removes all errors from the LoadTest log");
        }

        public void actionPerformed(ActionEvent e) {
            loadTestLog.clearErrors();
        }
    }

    private final class LoadTestLogTableMouseListener extends MouseAdapter {
        public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() > 1) {
                int selectedRow = logTable.getSelectedRow();
                if (selectedRow < 0) {
                    return;
                }

                int row = logTable.convertRowIndexToModel(selectedRow);
                if (row < 0) {
                    return;
                }

                LoadTestLogEntry entry = (LoadTestLogEntry) loadTestLog.getElementAt(row);
                ActionList actions = entry.getActions();
                if (actions != null) {
                    actions.performDefaultAction(new ActionEvent(logTable, 0, null));
                }
            }
        }

        public void mousePressed(MouseEvent e) {
            if (e.isPopupTrigger()) {
                showPopup(e);
            }
        }

        public void mouseReleased(MouseEvent e) {
            if (e.isPopupTrigger()) {
                showPopup(e);
            }
        }
    }

    public void showPopup(MouseEvent e) {
        int selectedRow = logTable.rowAtPoint(e.getPoint());
        if (selectedRow == -1) {
            return;
        }

        if (logTable.getSelectedRow() != selectedRow) {
            logTable.getSelectionModel().setSelectionInterval(selectedRow, selectedRow);
        }

        int row = logTable.convertRowIndexToModel(selectedRow);
        if (row < 0) {
            return;
        }

        LoadTestLogEntry entry = (LoadTestLogEntry) loadTestLog.getElementAt(row);
        ActionList actions = entry.getActions();

        if (actions == null || actions.getActionCount() == 0) {
            return;
        }

        JPopupMenu popup = ActionSupport.buildPopup(actions);
        popup.setInvoker(logTable);

        popup.setLocation((int) (logTable.getLocationOnScreen().getX() + e.getPoint().getX()), (int) (logTable
                .getLocationOnScreen().getY() + e.getPoint().getY()));
        popup.setVisible(true);
    }
}
