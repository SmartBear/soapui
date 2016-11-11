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

import com.eviware.soapui.impl.support.actions.ShowOnlineHelpAction;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.swing.JTextComponentPopupMenu;
import com.google.common.base.Preconditions;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import org.apache.commons.lang.StringUtils;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.text.JTextComponent;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Utility-class for creating JGoodies forms
 */

public class SimpleForm {
    public static final int DEFAULT_COMPONENT_COLUMN = 4;
    public static final int DEFAULT_LABEL_COLUMN = 2;
    public static final int SHORT_TEXT_FIELD_COLUMNS = 20;
    public static final int MEDIUM_TEXT_FIELD_COLUMNS = 30;
    public static final int LONG_TEXT_FIELD_COLUMNS = 50;
    public static final Color HINT_TEXT_COLOR = new Color(113, 102, 102);

    protected static final String DEFAULT_COMPONENT_ALIGNMENT = "left,bottom";
    protected static final int DEFAULT_TEXT_FIELD_COLUMNS = MEDIUM_TEXT_FIELD_COLUMNS;
    protected static final String ENABLED_PROPERTY_NAME = "enabled";

    private static final int DEFAULT_COLUMN_SPAN = 1;
    private static final String DEFAULT_COLUMN_SPECS = "5px:none,left:pref,10px,left:default,5px:grow(1.0)";

    private JPanel panel;
    private CellConstraints cc = new CellConstraints();
    private FormLayout layout;
    private RowSpec rowSpec;
    private int rowSpacing = 5;
    private Map<String, JComponent> components = new HashMap<String, JComponent>();
    private Map<JComboBox, Object[]> comboBoxMaps = new HashMap<JComboBox, Object[]>();
    private String rowAlignment = "top";
    private Map<String, String> hiddenValues;
    private boolean appended;
    private Font labelFont;
    private int defaultTextAreaColumns = 30;
    private int defaultTextAreaRows = 3;
    private int defaultTextFieldColumns = DEFAULT_TEXT_FIELD_COLUMNS;

    public SimpleForm() {
        this(DEFAULT_COLUMN_SPECS);
    }

    public SimpleForm(String columnSpec) {
        this(new FormLayout(columnSpec), BorderFactory.createEmptyBorder());
    }

    public SimpleForm(String columnSpec, Border border) {
        this(new FormLayout(columnSpec), border);
    }

    private SimpleForm(FormLayout layout, Border border) {
        this.layout = layout;
        panel = new JPanel(layout);
        panel.setBorder(border);
        rowSpec = new RowSpec(rowAlignment + ":pref");
    }

    public JPanel getPanel() {
        return panel;
    }

    public boolean hasComponents() {
        return !components.isEmpty();
    }

    public int getRowCount() {
        return layout.getRowCount();
    }

    public String getRowAlignment() {
        return rowAlignment;
    }

    public Font getLabelFont() {
        return labelFont;
    }

    public void setLabelFont(Font labelFont) {
        this.labelFont = labelFont;
    }

    public void setRowAlignment(String rowAlignment) {
        this.rowAlignment = rowAlignment;
        rowSpec = new RowSpec(rowAlignment + ":pref");
    }

    public void setRowAlignment(String alignment, String size, String resize) {
        this.rowAlignment = alignment + ":" + size + ":" + resize;
        rowSpec = new RowSpec(rowAlignment);
    }

    public int getRowSpacing() {
        return rowSpacing;
    }

    public void setRowSpacing(int rowSpacing) {
        this.rowSpacing = rowSpacing;
    }

    public String getComponentValue(String label) {
        JComponent component = getComponent(label);
        if (component == null) {
            return (String) (hiddenValues == null ? null : hiddenValues.get(label));
        }

        if (component instanceof JTextComponent) {
            return ((JTextComponent) component).getText();
        }

        if (component instanceof JComboBox) {
            JComboBox comboBox = ((JComboBox) component);
            int selectedIndex = comboBox.getSelectedIndex();
            if (selectedIndex != -1) {
                if (comboBoxMaps.containsKey(component)) {
                    Object[] keys = (Object[]) comboBoxMaps.get(comboBox);
                    Object value = keys[selectedIndex];
                    return (String) value == null ? null : value.toString();
                } else {
                    Object value = comboBox.getSelectedItem();
                    return (String) value == null ? null : value.toString();
                }
            }
        }

        if (component instanceof JList) {
            return (String) ((JList) component).getSelectedValue();
        }

        if (component instanceof JCheckBox) {
            return String.valueOf(((JCheckBox) component).isSelected());
        } else if (component instanceof JFormComponent) {
            return ((JFormComponent) component).getValue();
        }

        return null;
    }

    public void setComponentValue(String label, String value) {
        JComponent component = getComponent(label);

        if (component instanceof JScrollPane) {
            component = (JComponent) ((JScrollPane) component).getViewport().getComponent(0);
        }

        if (component instanceof JTextComponent) {
            ((JTextComponent) component).setText(value);
        } else if (component instanceof JComboBox) {
            JComboBox comboBox = ((JComboBox) component);
            comboBox.setSelectedItem(value);
        } else if (component instanceof JList) {
            ((JList) component).setSelectedValue(value, true);
        } else if (component instanceof JCheckBox) {
            ((JCheckBox) component).setSelected(Boolean.valueOf(value));
        } else if (component instanceof JFormComponent) {
            ((JFormComponent) component).setValue(value);
        } else if (component instanceof RSyntaxTextArea) {
            ((RSyntaxTextArea) component).setText(value);
        }
    }

    public void getValues(Map<String, String> values) {
        for (Iterator<String> i = components.keySet().iterator(); i.hasNext(); ) {
            String key = i.next();
            values.put(key, getComponentValue(key));
        }
    }

    public void setValues(Map<String, String> values) {
        for (Map.Entry<String, String> entry : values.entrySet()) {
            setComponentValue(entry.getKey(), entry.getValue());
        }
    }

    public void setEnabled(boolean b) {
        for (JComponent component : components.values()) {
            if (component instanceof JScrollPane) {
                ((JScrollPane) component).getViewport().getView().setEnabled(b);
            }

            component.setEnabled(b);
        }
    }

    public JComponent getComponent(String label) {
        return components.get(label);
    }

    public void setBorder(Border border) {
        panel.setBorder(border);
    }

    public void addInputFieldHintText(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(HINT_TEXT_COLOR);

        addComponentWithoutLabel(label);
    }

    private void setToolTip(JComponent component, String tooltip) {
        component.setToolTipText(StringUtils.defaultIfEmpty(tooltip, null));
    }

    public int getDefaultTextAreaColumns() {
        return defaultTextAreaColumns;
    }

    /**
     * @param defaultTextFieldColumns Should be a constant defined in SimpleForm
     * @see com.eviware.soapui.support.components.SimpleForm
     */
    public void setDefaultTextFieldColumns(int defaultTextFieldColumns) {
        this.defaultTextFieldColumns = defaultTextFieldColumns;
    }

    public void setDefaultTextAreaColumns(int defaultTextAreaColumns) {
        this.defaultTextAreaColumns = defaultTextAreaColumns;
    }

    public int getDefaultTextAreaRows() {
        return defaultTextAreaRows;
    }

    public void setDefaultTextAreaRows(int defaultTextAreaRows) {
        this.defaultTextAreaRows = defaultTextAreaRows;
    }


    // -- Custom components -- //

    /**
     * Appens a label with hyperlink behaviour that streches from the default label column to the end of the form
     *
     * @param url  The URL to open when clicking on the label
     * @param text The text of the label
     */
    public void appendLabelAsLink(String url, String text) {
        JLabel label = UISupport.createLabelLink(url, text);
        append(null, null, label, null, DEFAULT_LABEL_COLUMN, getColumnSpanToTheEnd(DEFAULT_LABEL_COLUMN));
    }

    /**
     * Appends a heading with bold text that streches from the default label column to the end of the form
     *
     * @param text The text of the heading
     */
    public void appendHeading(String text) {
        JLabel label = new JLabel(text);
        Font font = label.getFont();
        Font fontBold = new Font(font.getName(), Font.BOLD, font.getSize());
        label.setFont(fontBold);
        append(null, null, label, null, DEFAULT_LABEL_COLUMN, getColumnSpanToTheEnd(DEFAULT_LABEL_COLUMN));
    }

    public void appendHeadingAndHelpButton(String text, String helpUrl) {
        JLabel label = new JLabel(text);
        Font font = label.getFont();
        Font fontBold = new Font(font.getName(), Font.BOLD, font.getSize());
        label.setFont(fontBold);
        JPanel innerPanel = new JPanel();
        innerPanel.setLayout(new BoxLayout(innerPanel, BoxLayout.X_AXIS));
        innerPanel.add(label);
        innerPanel.add(Box.createHorizontalGlue());
        innerPanel.add(UISupport.createFormButton(new ShowOnlineHelpAction(helpUrl)));
        append(null, null, innerPanel, null, DEFAULT_LABEL_COLUMN, getColumnSpanToTheEnd(DEFAULT_LABEL_COLUMN));
    }

    // -- Standard components -- //

    public JComboBox appendComboBox(String label, Map<?, ?> values) {
        Object[] valueArray = new Object[values.size()];
        Object[] keyArray = new Object[values.size()];

        int ix = 0;
        for (Iterator<?> i = values.keySet().iterator(); i.hasNext(); ix++) {
            keyArray[ix] = i.next();
            valueArray[ix] = values.get(keyArray[ix]);
        }

        JComboBox comboBox = new JComboBox(valueArray);

        comboBoxMaps.put(comboBox, keyArray);

        append(label, comboBox);
        return comboBox;
    }

    public JComboBox appendComboBox(String label, Object[] values, String tooltip) {
        JComboBox comboBox = new JComboBox(values);
        comboBox.setToolTipText(StringUtils.defaultIfEmpty(tooltip, null));
        comboBox.getAccessibleContext().setAccessibleDescription(tooltip);
        append(label, comboBox);
        return comboBox;
    }

    public JComboBox appendComboBox(String label, ComboBoxModel model, String tooltip) {
        JComboBox comboBox = new JComboBox(model);
        comboBox.setToolTipText(StringUtils.defaultIfEmpty(tooltip, null));
        comboBox.getAccessibleContext().setAccessibleDescription(tooltip);
        append(label, comboBox);
        return comboBox;
    }

    public JCheckBox appendCheckBox(String caption, String label, boolean selected) {
        JCheckBox checkBox = new JCheckBox(label, selected);
        checkBox.getAccessibleContext().setAccessibleDescription(caption);
        components.put(caption, checkBox);
        append(caption, checkBox);
        return checkBox;
    }

    public JRadioButton appendRadioButton(String caption, String label, ButtonGroup group, boolean selected) {
        JRadioButton radioButton = new JRadioButton(label, selected);
        radioButton.getAccessibleContext().setAccessibleDescription(caption);
        if (group != null) {
            group.add(radioButton);
        }
        components.put(caption, radioButton);
        append(caption, radioButton);
        return radioButton;
    }

    public JButton addRightButton(Action action) {
        if (rowSpacing > 0 && !components.isEmpty()) {
            addSpace(rowSpacing);
        }

        layout.appendRow(rowSpec);
        int row = layout.getRowCount();

        JButton button = new JButton(action);
        panel.add(button, cc.xy(DEFAULT_COMPONENT_COLUMN, row, "right,bottom"));
        return button;
    }

    public JButton appendButtonWithoutLabel(String text, ActionListener actionListener) {
        JButton button = new JButton(text);
        button.addActionListener(actionListener);

        addComponentWithoutLabel(button);

        return button;
    }

    public JButton addButtonWithoutLabelToTheRight(String text, ActionListener actionListener) {
        JButton button = new JButton(text);
        button.addActionListener(actionListener);

        addComponentWithoutLabel(button, "right,bottom");

        return button;
    }

    public JButton appendButton(String label, String tooltip) {
        JButton button = new JButton();
        button.setToolTipText(StringUtils.defaultIfEmpty(tooltip, null));
        button.getAccessibleContext().setAccessibleDescription(tooltip);
        append(label, button);
        return button;
    }

    public JPasswordField appendPasswordField(String label, String tooltip) {
        JPasswordField textField = new JPasswordField();
        textField.setColumns(defaultTextFieldColumns);
        textField.setToolTipText(StringUtils.defaultIfEmpty(tooltip, null));
        textField.getAccessibleContext().setAccessibleDescription(tooltip);
        append(label, textField);
        return textField;
    }

    public JTextArea appendTextArea(String label, String tooltip) {
        JTextArea textArea = new JUndoableTextArea();
        textArea.setColumns(defaultTextAreaColumns);
        textArea.setRows(defaultTextAreaRows);
        textArea.setAutoscrolls(true);
        textArea.add(new JScrollPane());
        setToolTip(textArea, tooltip);
        textArea.getAccessibleContext().setAccessibleDescription(tooltip);
        JTextComponentPopupMenu.add(textArea);
        append(label, new JScrollPane(textArea));
        return textArea;
    }

    /**
     * Appends a label and a text field to the form
     *
     * @param label   The value of the label. Will also be the name of the text field.
     * @param tooltip The value of the text field tool tip
     * @return The text field
     */
    public JTextField appendTextField(String label, String tooltip) {
        return appendTextField(label, label, tooltip, defaultTextFieldColumns);
    }

    /**
     * Appends a label and a text field to the form
     *
     * @param label            The value of the label
     * @param name             The name of the text field
     * @param tooltip          The value of the text field tool tip
     * @param textFieldColumns The number of columns to display for the text field. Should be a constant defined in SimpleForm
     * @return The text field
     * @see com.eviware.soapui.support.components.SimpleForm
     */
    public JTextField appendTextField(String label, String name, String tooltip, int textFieldColumns) {
        JTextField textField = new JUndoableTextField();
        textField.setName(name);
        textField.setColumns(textFieldColumns);
        setToolTip(textField, tooltip);
        textField.getAccessibleContext().setAccessibleDescription(tooltip);
        JTextComponentPopupMenu.add(textField);
        append(label, textField);
        return textField;
    }

    public void addHiddenValue(String name, String value) {
        if (hiddenValues == null) {
            hiddenValues = new HashMap<String, String>();
        }

        hiddenValues.put(name, value);
    }

    public void addSpace() {
        addSpace(rowSpacing);
    }

    public void addSpace(int size) {
        if (size > 0) {
            layout.appendRow(new RowSpec(size + "px"));
        }
    }

    public void appendSeparator() {
        if (appended && rowSpacing > 0) {
            addSpace(rowSpacing);
        }

        layout.appendRow(rowSpec);
        int row = layout.getRowCount();

        panel.add(new JSeparator(), cc.xywh(DEFAULT_LABEL_COLUMN, row, 4, 1));
        appended = true;
    }

    public void addComponentWithoutLabel(JComponent component) {
        addComponentWithoutLabel(component, DEFAULT_COMPONENT_ALIGNMENT);
    }

    private void addComponentWithoutLabel(JComponent component, String alignment) {
        if (rowSpacing > 0 && !components.isEmpty()) {
            addSpace(rowSpacing);
        }

        layout.appendRow(rowSpec);
        int row = layout.getRowCount();

        panel.add(component, cc.xy(DEFAULT_COMPONENT_COLUMN, row, alignment));
    }

    public <T extends JComponent> T addLeftComponent(T component) {
        if (rowSpacing > 0 && !components.isEmpty()) {
            addSpace(rowSpacing);
        }

        layout.appendRow(rowSpec);

        int row = layout.getRowCount();
        panel.add(component, cc.xy(DEFAULT_COMPONENT_COLUMN, row));

        return component;
    }

    public void addRightComponent(JComponent component) {
        if (rowSpacing > 0 && !components.isEmpty()) {
            addSpace(rowSpacing);
        }

        layout.appendRow(rowSpec);

        int row = layout.getRowCount();
        panel.add(component, cc.xy(DEFAULT_COMPONENT_COLUMN, row, "right,bottom"));
    }

    public void addComponent(JComponent component) {
        layout.appendRow(rowSpec);
        int row = layout.getRowCount();

        panel.add(component, cc.xyw(DEFAULT_LABEL_COLUMN, row, 4));
    }

    public void removeComponent(JComponent component) {
        panel.remove(component);
    }

    public void appendFixed(String label, JComponent component) {
        append(label, component, "left:pref");
    }

    public void append(JComponent component) {
        int spaceRowIndex = -1;

        if (rowSpacing > 0 && appended) {
            addSpace(rowSpacing);
            spaceRowIndex = layout.getRowCount();
        }

        layout.appendRow(rowSpec);
        int row = layout.getRowCount();

        panel.add(component, cc.xyw(DEFAULT_LABEL_COLUMN, row, 4));

        component.addComponentListener(new LabelHider(null, spaceRowIndex));

        appended = true;
    }

    public void append(String label, JComponent component) {
        append(label, component, null);
    }

    public <T extends JComponent> T append(String label, T component, String alignments) {
        JLabel jlabel = null;
        if (label != null) {
            jlabel = new JLabel(label.endsWith(":") || label.isEmpty() ? label : label + ":");
            jlabel.setBorder(BorderFactory.createEmptyBorder(3, 0, 0, 0));
            if (labelFont != null) {
                jlabel.setFont(labelFont);
            }
        }

        return append(label, jlabel, component, alignments, DEFAULT_COMPONENT_COLUMN, getColumnSpanToTheEnd(DEFAULT_COMPONENT_COLUMN));
    }


    public <T extends JComponent> T append(String name, JLabel label, T field) {
        return append(name, label, field, null, DEFAULT_COMPONENT_COLUMN, getColumnSpanToTheEnd(DEFAULT_COMPONENT_COLUMN));
    }

    /**
     * Appends a vararg of PropertyComponents to a single row in the form. It's assumed that every other column contains a component
     * and the others are used for spacing including the initial column.
     *
     * @param propertyComponents The PropertyComponents to be added
     */
    public void appendInOneRow(PropertyComponent... propertyComponents) {
        Preconditions.checkArgument(propertyComponents.length * 2 <= layout.getColumnCount() - 1, "There is not enough room for the components to be added");

        int currentColumn = DEFAULT_COMPONENT_COLUMN;
        for (int i = 0; i < propertyComponents.length; i++) {
            PropertyComponent propertyComponent = propertyComponents[i];
            if (i == 0) {
                append(null, null, propertyComponent.getComponent(), "left,center", currentColumn, DEFAULT_COLUMN_SPAN);
            } else {
                addComponentToRow(null, propertyComponent.getComponent(), "left,center", currentColumn, getRowCount(), DEFAULT_COLUMN_SPAN);
            }
            currentColumn += 2;
        }
    }

    private <T extends JComponent> T append(String name, JComponent label, T component, String alignments, int column, int columnSpan) {
        int spaceRowIndex = -1;

        if (rowSpacing > 0 && appended) {
            addSpace(rowSpacing);
            spaceRowIndex = layout.getRowCount();
        }

        layout.appendRow(rowSpec);
        int row = layout.getRowCount();

        if (label != null) {
            panel.add(label, cc.xy(DEFAULT_LABEL_COLUMN, row));
            component.addComponentListener(new LabelHider(label, spaceRowIndex));
            component.addPropertyChangeListener(ENABLED_PROPERTY_NAME, new LabelEnabler(label));

            if (label instanceof JLabel) {
                JLabel jl = ((JLabel) label);
                jl.setLabelFor(component);
                String text = jl.getText();
                int ix = text.indexOf('&');
                if (ix >= 0) {
                    jl.setText(text.substring(0, ix) + text.substring(ix + 1));
                    jl.setDisplayedMnemonicIndex(ix);
                    jl.setDisplayedMnemonic(text.charAt(ix + 1));
                }

                if (component.getAccessibleContext() != null) {
                    component.getAccessibleContext().setAccessibleName(text);
                }
            }
        } else {
            component.addComponentListener(new LabelHider(null, spaceRowIndex));
        }

        addComponentToRow(name, component, alignments, column, row, columnSpan);

        return component;
    }

    private <T extends JComponent> void addComponentToRow(String name, T component, String alignments, int column, int row, int columnSpan) {
        if (alignments == null) {
            panel.add(component, cc.xyw(column, row, columnSpan));
        } else {
            panel.add(component, cc.xyw(column, row, columnSpan, alignments));
        }

        components.put(name, component);
        appended = true;
    }

    /**
     * Returns a column span that stretched to the last component column if using a custom layout.
     * It's assumed that every other column contains a component
     * and the others are used for spacing including the initial column.
     *
     * @param startingColumn The column from where the column span should be calculated.
     *                       Should preferly be a constant defined in SimpleForm
     * @see com.eviware.soapui.support.components.SimpleForm
     */
    private int getColumnSpanToTheEnd(int startingColumn) {
        return layout.getColumnCount() - startingColumn;
    }


    private static class LabelEnabler implements PropertyChangeListener {

        private final JComponent label;

        public LabelEnabler(JComponent label) {
            this.label = label;
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            label.setEnabled((Boolean) evt.getNewValue());
        }

    }

    private final class LabelHider extends ComponentAdapter {

        private final JComponent jlabel;
        private final int rowIndex;

        public LabelHider(JComponent label, int i) {
            this.jlabel = label;
            this.rowIndex = i;
        }

        public void componentHidden(ComponentEvent e) {
            if (jlabel != null) {
                jlabel.setVisible(false);
            }

            if (rowIndex >= 0 && rowIndex < layout.getRowCount()) {
                layout.setRowSpec(rowIndex, new RowSpec("0px"));
            }
        }

        public void componentShown(ComponentEvent e) {
            if (jlabel != null) {
                jlabel.setVisible(true);
            }

            if (rowIndex >= 0 && rowIndex < layout.getRowCount()) {
                layout.setRowSpec(rowIndex, new RowSpec(rowSpacing + "px"));
            }
        }
    }
}
