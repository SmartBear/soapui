/*
 * SoapUI, Copyright (C) 2004-2022 SmartBear Software
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

package com.eviware.soapui.support.swing;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.support.UISupport;
import org.jdesktop.swingx.JXTable;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;

/**
 * Factory class responsible for creation of JTable instances with a common style.
 */
public abstract class JTableFactory {

    public abstract JTable makeJTable(TableModel tableModel);

    public abstract JXTable makeJXTable(TableModel tableModel);

    public static JTableFactory getInstance() {
        return new DefaultJTableFactory();
    }

    private static class DefaultJTableFactory extends JTableFactory {
        @Override
        public JTable makeJTable(TableModel tableModel) {
            JTable table = UISupport.isMac() ? makeStripedTable(tableModel) : new JTable(tableModel);
            applyDarkModeToTable(table);
            return table;
        }

        @Override
        public JXTable makeJXTable(TableModel tableModel) {
            JXTable table = UISupport.isMac() ? makeStripedJXTable(tableModel) : new JXTable(tableModel);
            applyDarkModeToTable(table);
            return table;
        }

        private void applyDarkModeToTable(JTable table) {
            boolean isDarkMode = SoapUI.getSettings().getBoolean("UISettings.DARK_MODE", false);
            if (isDarkMode) {
                table.setBackground(new Color(60, 63, 65));
                table.setForeground(Color.LIGHT_GRAY);
                table.setGridColor(new Color(100, 100, 100));
                // Show grid lines in dark mode for better cell separation
                table.setShowGrid(true);
                table.setIntercellSpacing(new Dimension(1, 1));
                if (table.getTableHeader() != null) {
                    table.getTableHeader().setBackground(new Color(70, 70, 70));
                    table.getTableHeader().setForeground(Color.LIGHT_GRAY);
                }
            }
        }

        private JXTable makeStripedJXTable(final TableModel tableModel) {
            JXTable stripedJxTable = new JXTable(tableModel) {
                @Override
                public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                    Component defaultRenderer = super.prepareRenderer(renderer, row, column);
                    applyStripesToRenderer(row, defaultRenderer);
                    return defaultRenderer;
                }

                @Override
                public boolean getShowVerticalLines() {
                    // Show vertical lines in dark mode for better visibility
                    boolean isDarkMode = SoapUI.getSettings().getBoolean("UISettings.DARK_MODE", false);
                    return isDarkMode;
                }
            };
            setGridAttributes(stripedJxTable);
            return stripedJxTable;
        }

        private JTable makeStripedTable(final TableModel tableModel) {
            JTable stripedTable = new JTable(tableModel) {
                @Override
                public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                    Component defaultRenderer = super.prepareRenderer(renderer, row, column);
                    applyStripesToRenderer(row, defaultRenderer);
                    return defaultRenderer;
                }

                @Override
                public boolean getShowVerticalLines() {
                    // Show vertical lines in dark mode for better visibility
                    boolean isDarkMode = SoapUI.getSettings().getBoolean("UISettings.DARK_MODE", false);
                    return isDarkMode;
                }
            };
            setGridAttributes(stripedTable);
            return stripedTable;
        }

    }

    public static void setGridAttributes(JTable stripedTable) {
        boolean isDarkMode = SoapUI.getSettings().getBoolean("UISettings.DARK_MODE", false);
        if (isDarkMode) {
            // In dark mode, show grid for better cell separation
            stripedTable.setShowGrid(true);
            stripedTable.setIntercellSpacing(new Dimension(1, 1));
            stripedTable.setGridColor(new Color(100, 100, 100));
        } else {
            // Keep original behavior for light mode
            stripedTable.setShowGrid(false);
            stripedTable.setIntercellSpacing(new Dimension(0, 0));
        }
    }

    public static void applyStripesToRenderer(int row, Component defaultRenderer) {
        boolean isDarkMode = SoapUI.getSettings().getBoolean("UISettings.DARK_MODE", false);

        if (isDarkMode) {
            if (row % 2 == 0) {
                defaultRenderer.setBackground(new Color(65, 68, 70));
            } else {
                defaultRenderer.setBackground(new Color(60, 63, 65));
            }
            defaultRenderer.setForeground(Color.LIGHT_GRAY);
        } else {
            if (row % 2 == 0) {
                defaultRenderer.setBackground(new Color(241, 244, 247));
            } else {
                defaultRenderer.setBackground(Color.WHITE);
            }
            defaultRenderer.setForeground(Color.BLACK);
        }
    }
}
