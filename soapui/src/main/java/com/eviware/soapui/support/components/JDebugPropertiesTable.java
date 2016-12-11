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

package com.eviware.soapui.support.components;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.support.PropertyChangeNotifier;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.JPropertiesTable.PropertyDescriptor;
import com.eviware.soapui.support.components.JPropertiesTable.PropertyFormatter;
import com.eviware.soapui.support.swing.JTableFactory;
import org.apache.commons.beanutils.PropertyUtils;
import org.jdesktop.swingx.JXTable;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * Table for displaying property name/value pairs
 */

@SuppressWarnings("serial")
public class JDebugPropertiesTable<T> {
    public final static Object[] BOOLEAN_OPTIONS = new Object[]{Boolean.TRUE, Boolean.FALSE};

    private PropertiesTableModel<T> tableModel;
    private JXTable table;

    public JDebugPropertiesTable(T propertyObject) {

        tableModel = new PropertiesTableModel<T>(propertyObject);
        table = new PTable(tableModel);

        table.getColumnModel().getColumn(0).setHeaderValue("Property");
        table.getColumnModel().getColumn(1).setHeaderValue("Value");
        table.getColumnModel().getColumn(0).setCellRenderer(new PropertiesTableCellRenderer());
        table.getColumnModel().getColumn(1).setCellRenderer(new PropertiesTableCellRenderer());

        table.setBackground(Color.WHITE);
    }

    public void removeNotify() {
        getTableModel().release();
        table.removeNotify();
    }

    public void addNotify() {
        getTableModel().attach();
        table.addNotify();
    }

    public void setPropertyObject(T propertyObject) {
        tableModel.setPropertyObject(propertyObject);
    }

    public PropertiesTableModel<?> getTableModel() {
        return tableModel;
    }

    public PropertyDescriptor addProperty(String caption, String name) {
        return addProperty(caption, name, false);
    }

    public PropertyDescriptor addProperty(String caption, String name, boolean editable) {
        return addProperty(caption, name, editable, null);
    }

    public PropertyDescriptor addProperty(String caption, String name, boolean editable, PropertyFormatter formatter) {
        return tableModel.addProperty(caption, name, editable, formatter);
    }

    public static final class PropertiesTableModel<T> extends AbstractTableModel implements PropertyChangeListener {
        private List<PropertyDescriptor> properties = new ArrayList<PropertyDescriptor>();
        private T propertyObject;
        private boolean attached;

        public PropertiesTableModel(T propertyObject) {
            this.propertyObject = propertyObject;
        }

        public void attach() {
            if (!attached && propertyObject instanceof PropertyChangeNotifier) {
                ((PropertyChangeNotifier) propertyObject).addPropertyChangeListener(this);
                attached = true;
            }
        }

        public void setPropertyObject(T propertyObject) {
            release();
            this.propertyObject = propertyObject;
            attach();
            fireTableDataChanged();
        }

        public PropertyDescriptor addProperty(String caption, String name, boolean editable, PropertyFormatter formatter) {
            PropertyDescriptor propertyDescriptor = new PropertyDescriptor(caption, name, editable, formatter);
            properties.add(propertyDescriptor);
            return propertyDescriptor;
        }

        public PropertyDescriptor addProperty(String caption, String name, Object[] options) {
            PropertyDescriptor propertyDescriptor = new PropertyDescriptor(caption, name, options);
            properties.add(propertyDescriptor);
            fireTableStructureChanged();
            return propertyDescriptor;
        }

        public int getRowCount() {
            return properties.size();
        }

        public int getColumnCount() {
            return 2;
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            if (propertyObject == null) {
                return null;
            }

            try {
                PropertyDescriptor propertyDescriptor = properties.get(rowIndex);
                switch (columnIndex) {
                    case 0:
                        return propertyDescriptor.getCaption();
                    case 1: {
                        Object value = PropertyUtils.getSimpleProperty(propertyObject, propertyDescriptor.getName());
                        return propertyDescriptor.getFormatter().format(propertyDescriptor.getName(), value);
                    }
                }
            } catch (IllegalAccessException e) {
                SoapUI.logError(e);
            } catch (InvocationTargetException e) {
                SoapUI.logError(e);
            } catch (NoSuchMethodException e) {
                SoapUI.logError(e);
            }

            return null;
        }

        public PropertyDescriptor getPropertyDescriptorAt(int row) {
            return properties.get(row);
        }

        public void propertyChange(PropertyChangeEvent evt) {
            fireTableDataChanged();
        }

        public void release() {
            if (propertyObject instanceof PropertyChangeNotifier && attached) {
                ((PropertyChangeNotifier) propertyObject).removePropertyChangeListener(this);
                attached = false;
            }
        }

        public PropertyDescriptor addPropertyShadow(String caption, String name, boolean editable) {
            PropertyDescriptor propertyDescriptor = new PropertyDescriptor(caption, name, editable);
            properties.add(propertyDescriptor);
            return propertyDescriptor;
        }
    }

    private class PropertiesTableCellRenderer extends DefaultTableCellRenderer {
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            JLabel component = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            component.setToolTipText(value.toString());
            Font newLabelFont = new Font(component.getFont().getName(), Font.BOLD, component.getFont().getSize());
            component.setFont(newLabelFont);

            return component;
        }
    }

    public PropertyDescriptor addProperty(String caption, String name, Object[] options) {
        return tableModel.addProperty(caption, name, options);
    }

    private class PTable extends JXTable {
        public PTable(TableModel tableModel) {
            super(tableModel);
            if (UISupport.isMac()) {
                JTableFactory.setGridAttributes(this);
            }
        }

        public TableCellEditor getCellEditor(int row, int column) {
            if (column == 0) {
                return super.getCellEditor(row, column);
            } else {
                return tableModel.getPropertyDescriptorAt(row).getCellEditor();
            }
        }

        @Override
        public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
            Component defaultRenderer = super.prepareRenderer(renderer, row, column);
            JTableFactory.applyStripesToRenderer(row, defaultRenderer);
            return defaultRenderer;
        }

        @Override
        public boolean getShowVerticalLines() {
            return UISupport.isMac() ? false : super.getShowVerticalLines();
        }
    }

    public PropertyDescriptor addPropertyShadow(String caption, String name, boolean editable) {
        return tableModel.addPropertyShadow(caption, name, editable);
    }

    public JTable getTable() {
        return table;
    }


}
