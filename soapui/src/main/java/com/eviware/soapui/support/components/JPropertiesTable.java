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
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.swing.JTableFactory;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.TransferHandler;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * Table for displaying property name/value pairs
 *
 * @author Ole.Matzura
 */

@SuppressWarnings("serial")
public class JPropertiesTable<T> extends JPanel {
    public final static Object[] BOOLEAN_OPTIONS = new Object[]{Boolean.TRUE, Boolean.FALSE};

    private PropertiesTableModel<T> tableModel;
    private JTable table;

    private TitledBorder titledBorder;

    private String title;

    public JPropertiesTable(String title) {
        this(title, null);
    }

    public JPropertiesTable(String title, T propertyObject) {
        super(new BorderLayout());
        this.title = title;
        setBackground(Color.WHITE);
        tableModel = new PropertiesTableModel<T>(propertyObject);
        table = new PTable(tableModel);
        table.setBackground(Color.WHITE);
        table.getColumnModel().getColumn(0).setHeaderValue("Property");
        table.getColumnModel().getColumn(1).setHeaderValue("Value");
        table.getColumnModel().getColumn(0).setCellRenderer(new PropertiesTableCellRenderer());
        table.getColumnModel().getColumn(1).setCellRenderer(new PropertiesTableCellRenderer());


        add(new JScrollPane(table), BorderLayout.CENTER);
        titledBorder = BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), title);
        /*
		 * Java 7 issue
		 *
		 * old: titledBorder.setTitleFont( titledBorder.getTitleFont().deriveFont(
		 * Font.PLAIN, 11 ) ); titledBorder.getTitleFont() return null in Java 7
		 */
        Font defaultUIFont = getUIDefaultFont();
        if (defaultUIFont != null) {
            titledBorder.setTitleFont(getUIDefaultFont().deriveFont(Font.PLAIN, 11));
        }

        if (title != null) {
            setBorder(titledBorder);
        }

        table.setBackground(Color.WHITE);
        setPreferredSize(table.getPreferredSize());
    }

    private Font getUIDefaultFont() {
        UIDefaults uidefs = UIManager.getLookAndFeelDefaults();
        for (Object key : uidefs.keySet()) {
            if (uidefs.get(key) instanceof Font) {
                return uidefs.getFont(key);
            }
        }
        return null;
    }

    public void setTitle(String title) {
        this.title = title;
        titledBorder.setTitle(title);
        setBorder(titledBorder);
        repaint();
    }

    public String getTitle() {
        return title;
    }

    @Override
    public void removeNotify() {
        getTableModel().release();
        super.removeNotify();
    }

    @Override
    public void addNotify() {
        getTableModel().attach();
        super.addNotify();
    }

    public void setPropertyObject(T propertyObject) {
        if (table.isEditing()) {
            table.getCellEditor().stopCellEditing();
        }

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
            return propertyDescriptor;
        }

        public int getRowCount() {
            return properties.size();
        }

        public int getColumnCount() {
            return 2;
        }

        public boolean isCellEditable(int rowIndex, int columnIndex) {
            if (columnIndex == 0 || propertyObject == null) {
                return false;
            }
            return properties.get(rowIndex).isEditable()
                    && PropertyUtils.isWriteable(propertyObject, properties.get(rowIndex).getName());
        }

        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            try {
                if (propertyObject != null && columnIndex == 1 && properties.get(rowIndex).isEditable()) {
                    BeanUtils.setProperty(propertyObject, properties.get(rowIndex).getName(), aValue);
                }
            } catch (IllegalAccessException e) {
                SoapUI.logError(e);
            } catch (InvocationTargetException e) {
                SoapUI.logError(e);
            }
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

    public static class PropertyDescriptor {
        private final String caption;
        private final String name;
        private boolean editable;
        private PropertyFormatter formatter;
        private Object[] options;
        private DefaultCellEditor cellEditor;
        private String description;

        public PropertyDescriptor(String caption, String name, boolean editable, PropertyFormatter formatter) {
            this.caption = caption;
            this.name = name;
            this.editable = editable;
            this.formatter = formatter;

            JTextField textField = new JTextField();
            textField.setBorder(BorderFactory.createEmptyBorder());
            cellEditor = new DefaultCellEditor(textField);
        }

        public PropertyDescriptor(String caption, String name, Object[] options) {
            this.caption = caption;
            this.name = name;
            this.options = options;
            editable = true;

            JComboBox comboBox = new JComboBox(options);

            if (options.length > 0 && options[0] == null) {
                comboBox.setEditable(true);
                comboBox.removeItemAt(0);
            }

            comboBox.setBorder(null);
            cellEditor = new DefaultCellEditor(comboBox);
        }

        /**
         * For password field in table.
         *
         * @param caption
         * @param name
         * @param editable
         * @author robert nemet
         */
        public PropertyDescriptor(String caption, String name, boolean editable) {

            this.caption = caption;
            this.name = name;
            this.editable = editable;

            JPasswordField textField = new JPasswordField();
            textField.setBorder(BorderFactory.createEmptyBorder());
            cellEditor = new DefaultCellEditor(textField);
        }

        public void setFormatter(PropertyFormatter formatter) {
            this.formatter = formatter;
        }

        public PropertyFormatter getFormatter() {
            return formatter == null ? DefaultFormatter.getInstance() : formatter;
        }

        public String getCaption() {
            return caption;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public boolean isEditable() {
            return editable;
        }

        public Object[] getOptions() {
            return options;
        }

        public boolean hasOptions() {
            return options != null;
        }

        public String getName() {
            return name;
        }

        public TableCellEditor getCellEditor() {
            return cellEditor;
        }
    }

    private static class PropertiesTableCellRenderer extends DefaultTableCellRenderer {
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            Component component;
            DefaultCellEditor cellEditor = (DefaultCellEditor) table.getCellEditor(row, column);
            if (cellEditor.getComponent() instanceof JPasswordField && value instanceof String) {
                if (value != null && ((String) value).length() > 0) {
                    component = super.getTableCellRendererComponent(table, "**************", isSelected, hasFocus, row,
                            column);
                } else {
                    component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                }
            } else {
                component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
            if (component instanceof JComponent) {
                PropertyDescriptor descriptor = ((PropertiesTableModel<?>) table.getModel())
                        .getPropertyDescriptorAt(row);

                if (StringUtils.hasContent(descriptor.getDescription())) {
                    ((JComponent) component).setToolTipText(descriptor.getDescription());
                }
                // do not set tooltip as value for password field, it has no sense.
                else if (value != null && StringUtils.hasContent(value.toString())
                        && !(cellEditor.getComponent() instanceof JPasswordField)) {
                    ((JComponent) component).setToolTipText(value.toString());
                } else {
                    ((JComponent) component).setToolTipText(null);
                }
            }

            return component;
        }
    }

    /**
     * Formatter used for displaying property values
     *
     * @author Ole.Matzura
     */

    public interface PropertyFormatter {
        public Object format(String propertyName, Object value);
    }

    private static class DefaultFormatter implements PropertyFormatter {
        private static PropertyFormatter instance;

        public static PropertyFormatter getInstance() {
            if (instance == null) {
                instance = new DefaultFormatter();
            }

            return instance;
        }

        public Object format(String propertyName, Object value) {
            return value;
        }
    }

    public PropertyDescriptor addProperty(String caption, String name, Object[] options) {
        return tableModel.addProperty(caption, name, options);
    }

    private class PTable extends JTable {
        public PTable(TableModel tableModel) {
            super(tableModel);

            getActionMap().put(TransferHandler.getCopyAction().getValue(Action.NAME), new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    int row = getSelectedRow();
                    if (row == -1) {
                        return;
                    }

                    StringSelection selection = new StringSelection(getValueAt(row, 1).toString());
                    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
                }
            });

            putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
            if (UISupport.isMac()) {
                setShowGrid(false);
                setIntercellSpacing(new Dimension(0, 0));
            }
        }

        @Override
        public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
            Component defaultRenderer = super.prepareRenderer(renderer, row, column);
            if (UISupport.isMac()) {
                JTableFactory.applyStripesToRenderer(row, defaultRenderer);
            }
            return defaultRenderer;
        }

        @Override
        public boolean getShowVerticalLines() {
            return !UISupport.isMac();
        }

        public TableCellEditor getCellEditor(int row, int column) {
            if (column == 0) {
                return super.getCellEditor(row, column);
            } else {
                return tableModel.getPropertyDescriptorAt(row).getCellEditor();
            }
        }
    }

    /**
     * Value in this field will not be showen. It will be masked...
     *
     * @param caption
     * @param name
     * @param editable
     * @return
     * @author robert nemet
     */
    public PropertyDescriptor addPropertyShadow(String caption, String name, boolean editable) {
        return tableModel.addPropertyShadow(caption, name, editable);
    }
}
