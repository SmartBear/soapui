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
import com.eviware.soapui.impl.wsdl.loadtest.WsdlLoadTest;
import com.eviware.soapui.impl.wsdl.loadtest.assertions.LoadTestAssertionRegistry;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.support.action.swing.ActionList;
import com.eviware.soapui.support.action.swing.ActionListBuilder;
import com.eviware.soapui.support.action.swing.ActionSupport;
import com.eviware.soapui.support.swing.JTableFactory;
import org.jdesktop.swingx.JXTable;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Table for displaying real-time LoadTest Statistics
 *
 * @author Ole.Matzura
 */

public class JStatisticsTable extends JPanel {
    private final WsdlLoadTest loadTest;
    private JXTable statisticsTable;
    private JPopupMenu popup;

    public JStatisticsTable(WsdlLoadTest loadTest) {
        super(new BorderLayout());
        this.loadTest = loadTest;

        statisticsTable = JTableFactory.getInstance().makeJXTable(loadTest.getStatisticsModel());
        statisticsTable.setColumnControlVisible(true);
        statisticsTable.getTableHeader().setReorderingAllowed(false);

        statisticsTable.addMouseListener(new StatisticsTableMouseListener());

        TableColumnModel columnModel = statisticsTable.getColumnModel();
        columnModel.getColumn(0).setMaxWidth(5);
        columnModel.getColumn(0).setCellRenderer(new ColorLabelTableCellRenderer());
        columnModel.getColumn(1).setPreferredWidth(150);
        columnModel.getColumn(2).setPreferredWidth(20);
        columnModel.getColumn(3).setPreferredWidth(20);
        columnModel.getColumn(4).setPreferredWidth(20);
        columnModel.getColumn(5).setPreferredWidth(20);
        columnModel.getColumn(6).setPreferredWidth(20);
        columnModel.getColumn(7).setPreferredWidth(20);
        columnModel.getColumn(8).setPreferredWidth(20);
        columnModel.getColumn(9).setPreferredWidth(20);
        columnModel.getColumn(10).setPreferredWidth(20);
        columnModel.getColumn(11).setPreferredWidth(20);

        JScrollPane scrollPane = new JScrollPane(statisticsTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        add(scrollPane, BorderLayout.CENTER);

        JMenu assertionsMenu = new JMenu("Add Assertion");
        for (String assertion : LoadTestAssertionRegistry.getAvailableAssertions()) {
            assertionsMenu.add(new AddAssertionAction(assertion));
        }

        popup = new JPopupMenu();
        popup.add(assertionsMenu);
        popup.setInvoker(statisticsTable);
    }

    public void release() {
        loadTest.getStatisticsModel().removeTableModelListener(statisticsTable);
    }

    private final class StatisticsTableMouseListener extends MouseAdapter {
        public void mouseClicked(MouseEvent e) {
            if (statisticsTable.getSelectedColumn() == 1 && e.getClickCount() > 1) {
                int row = statisticsTable.getSelectedRow();
                if (row < 0) {
                    return;
                }

                row = statisticsTable.convertRowIndexToModel(row);

                ModelItem modelItem = row == statisticsTable.getRowCount() - 1 ? loadTest.getTestCase() : loadTest
                        .getStatisticsModel().getTestStepAtRow(row);

                ActionList actions = ActionListBuilder.buildActions(modelItem);
                if (actions != null) {
                    actions.performDefaultAction(new ActionEvent(statisticsTable, 0, null));
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

    private static final class ColorLabelTableCellRenderer extends JPanel implements TableCellRenderer {
        private Color bgColor;

        public ColorLabelTableCellRenderer() {
            super();

            bgColor = getBackground();
        }

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            if (value instanceof Color) {
                setBackground((Color) value);
            } else {
                setBackground(bgColor);
            }

            return this;
        }
    }

    public void showPopup(MouseEvent e) {
        int row = statisticsTable.rowAtPoint(e.getPoint());
        if (row == -1) {
            return;
        }

        if (statisticsTable.getSelectedRow() != row) {
            statisticsTable.getSelectionModel().setSelectionInterval(row, row);
        }

        row = statisticsTable.convertRowIndexToModel(row);

        while (popup.getComponentCount() > 1) {
            popup.remove(1);
        }

        if (row < statisticsTable.getRowCount() - 1) {
            TestStep testStep = loadTest.getStatisticsModel().getTestStepAtRow(row);
            ActionSupport.addActions(ActionListBuilder.buildActions(testStep), popup);
        }

        popup.setLocation((int) (statisticsTable.getLocationOnScreen().getX() + e.getPoint().getX()),
                (int) (statisticsTable.getLocationOnScreen().getY() + e.getPoint().getY()));
        popup.setVisible(true);
    }

    private class AddAssertionAction extends AbstractAction {
        private final String type;

        public AddAssertionAction(String type) {
            super(type);
            this.type = type;
        }

        public void actionPerformed(ActionEvent e) {
            int row = statisticsTable.getSelectedRow();
            if (row == -1) {
                return;
            }

            String target = LoadTestAssertion.ANY_TEST_STEP;

            row = statisticsTable.convertRowIndexToModel(row);

            if (row == statisticsTable.getRowCount() - 1) {
                target = LoadTestAssertion.ALL_TEST_STEPS;
            } else if (row >= 0) {
                target = loadTest.getTestCase().getTestStepAt(row).getName();
            }

            loadTest.addAssertion(type, target, true);
        }
    }
}
