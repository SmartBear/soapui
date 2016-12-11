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

package com.eviware.x.form.support;

import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.x.form.XFormOptionsField;
import com.eviware.x.impl.swing.AbstractSwingXFormField;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Swing-Specific multi-select list
 *
 * @author ole.matzura
 */

public class XFormMultiSelectList extends AbstractSwingXFormField<JPanel> implements XFormOptionsField {
    private JList list;
    private DefaultListModel listModel;
    private List<Boolean> selected = new ArrayList<Boolean>();

    PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private int[] defaultIndex;
    private Color defaultColor;
    private SelectAllAction selectAllAction;
    private UnselectAllAction unselectAllAction;

    public XFormMultiSelectList(String[] values) {
        super(new JPanel(new BorderLayout()));

        listModel = new DefaultListModel();
        if (values != null) {
            for (String value : values) {
                selected.add(false);
                listModel.addElement(value);
            }
        }
        list = new JList(listModel);
        list.setCellRenderer(new CheckListCellRenderer());
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                int index = list.locationToIndex(e.getPoint());

                if (index != -1 && list.isEnabled()) {
                    int[] oldValue = getSelectedIndexes();
                    selected.set(index, !selected.get(index));
                    pcs.firePropertyChange("select", oldValue, getSelectedIndexes());
                    list.repaint();
                }
            }
        });

        getComponent().add(new JScrollPane(list), BorderLayout.CENTER);
        getComponent().add(buildToolbar(), BorderLayout.NORTH);
        getComponent().setSize(new Dimension(400, 120));
        getComponent().setMaximumSize(new Dimension(400, 120));
        getComponent().setPreferredSize(new Dimension(400, 120));
        getComponent().setMinimumSize(new Dimension(400, 120));
    }

    private Component buildToolbar() {
        JXToolBar toolbar = UISupport.createSmallToolbar();

        toolbar.addFixed(new JButton(selectAllAction = new SelectAllAction()));
        toolbar.addRelatedGap();
        toolbar.addFixed(new JButton(unselectAllAction = new UnselectAllAction()));

        return toolbar;
    }

    public String getValue() {
        return String.valueOf(list.getSelectedValue());
    }

    public void setValue(String value) {
        int index = listModel.indexOf(value);
        selected.set(index, true);
        list.setSelectedIndex(index);
    }

    public void addItem(Object value) {
        listModel.addElement(value);
        selected.add(false);
    }

    public Object[] getOptions() {
        Object[] options = new Object[listModel.size()];
        for (int c = 0; c < options.length; c++) {
            options[c] = listModel.get(c);
        }
        return options;
    }

    public Object[] getSelectedOptions() {
        List<Object> result = new ArrayList<Object>();

        for (int c = 0; c < selected.size(); c++) {
            if (selected.get(c)) {
                result.add(listModel.get(c));
            }
        }

        return result.toArray();
    }

    public void setOptions(Object[] values) {
        listModel.clear();
        selected.clear();
        for (Object value : values) {
            selected.add(false);
            listModel.addElement(value);
        }
    }

    public class CheckListCellRenderer extends JCheckBox implements ListCellRenderer {
        public CheckListCellRenderer() {
            setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        }

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                                                      boolean cellHasFocus) {
            setText(value.toString());
            setSelected(selected.get(index));

            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                if (isDefault(index)) {
                    setBackground(defaultColor);
                } else {
                    setBackground(list.getBackground());
                }
                setForeground(list.getForeground());
            }

            return this;
        }
    }

    public void setSelectedOptions(Object[] options) {
        List<Object> asList = Arrays.asList(options);

        for (int c = 0; c < selected.size(); c++) {
            selected.set(c, asList.contains(listModel.get(c)));
        }

        list.repaint();
    }

    private class SelectAllAction extends AbstractAction {
        public SelectAllAction() {
            super("Select all");
            putValue(SHORT_DESCRIPTION, "Selects all items in the list");
        }

        public void actionPerformed(ActionEvent e) {
            int[] oldValue = getSelectedIndexes();
            setSelectedOptions(getOptions());
            pcs.firePropertyChange("select", oldValue, getSelectedIndexes());
        }
    }

    private class UnselectAllAction extends AbstractAction {
        public UnselectAllAction() {
            super("Unselect all");
            putValue(SHORT_DESCRIPTION, "Unselects all items in the list");
        }

        public void actionPerformed(ActionEvent e) {
            int[] oldValue = getSelectedIndexes();
            setSelectedOptions(new String[0]);
            pcs.firePropertyChange("select", oldValue, getSelectedIndexes());
        }
    }

    public int[] getSelectedIndexes() {
        int cnt = 0;

        for (int c = 0; c < selected.size(); c++) {
            if (selected.get(c)) {
                cnt++;
            }
        }

        int[] result = new int[cnt];
        cnt = 0;

        for (int c = 0; c < selected.size(); c++) {
            if (selected.get(c)) {
                result[cnt++] = c;
            }
        }

        return result;
    }

    public void setDefault(Color color, int... defIndex) {
        this.defaultIndex = defIndex;
        this.defaultColor = color;
    }

    private boolean isDefault(int index) {
        if (defaultIndex == null) {
            return false;
        }

        for (int i : defaultIndex) {
            if (index == i) {
                return true;
            }
        }
        return false;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        list.setEnabled(enabled);
        selectAllAction.setEnabled(enabled);
        unselectAllAction.setEnabled(enabled);
    }
}
