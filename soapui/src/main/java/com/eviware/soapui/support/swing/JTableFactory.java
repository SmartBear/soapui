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

package com.eviware.soapui.support.swing;

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
            return UISupport.isMac() ? makeStripedTable(tableModel) : new JTable(tableModel);
        }

        @Override
        public JXTable makeJXTable(TableModel tableModel) {
            return UISupport.isMac() ? makeStripedJXTable(tableModel) : new JXTable(tableModel);
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
                    return false;
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
                    return false;
                }
            };
            setGridAttributes(stripedTable);
            return stripedTable;
        }

    }

    public static void setGridAttributes(JTable stripedTable) {
        stripedTable.setShowGrid(false);
        stripedTable.setIntercellSpacing(new Dimension(0, 0));
    }

    public static void applyStripesToRenderer(int row, Component defaultRenderer) {
        if (row % 2 == 0) {
            defaultRenderer.setBackground(new Color(241, 244, 247));
        } else {
            defaultRenderer.setBackground(Color.WHITE);
        }
        defaultRenderer.setForeground(Color.BLACK);
    }
}
