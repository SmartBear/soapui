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

package com.eviware.x.impl.swing;

import com.eviware.x.form.XFormOptionsField;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public class JComboBoxFormField extends AbstractSwingXFormField<JComboBox> implements ItemListener, XFormOptionsField {
    public JComboBoxFormField(Object[] values) {
        super(new JComboBox());

        setOptions(values);

        getComponent().addItemListener(this);
    }

    public void setValue(String value) {
        getComponent().setSelectedItem(value);
    }

    public String getValue() {
        Object selectedItem = getComponent().getSelectedItem();
        return selectedItem == null ? null : selectedItem.toString();
    }

    public void itemStateChanged(ItemEvent e) {
        Object selectedItem = getComponent().getSelectedItem();
        fireValueChanged(selectedItem == null ? null : selectedItem.toString(), null);
    }

    public void addItem(Object value) {
        getComponent().addItem(value);
    }

    public void setOptions(Object[] values) {
        String selectedItem = getValue();
        DefaultComboBoxModel model = new DefaultComboBoxModel(values);

        if (values.length > 0 && values[0] == null) {
            model.removeElementAt(0);
            getComponent().setEditable(true);
        } else {
            getComponent().setEditable(false);
        }

        getComponent().setModel(model);

        if (selectedItem != null) {
            getComponent().setSelectedItem(selectedItem);
        } else if (getComponent().isEditable()) {
            getComponent().setSelectedItem("");
        }
    }

    public Object[] getOptions() {
        ComboBoxModel model = getComponent().getModel();

        Object[] result = new Object[model.getSize()];
        for (int c = 0; c < result.length; c++) {
            result[c] = model.getElementAt(c);
        }

        return result;
    }

    public Object[] getSelectedOptions() {
        return new Object[]{getComponent().getSelectedItem()};
    }

    public void setSelectedOptions(Object[] options) {
        getComponent().setSelectedItem(options.length > 0 ? options[0] : null);
    }

    public int[] getSelectedIndexes() {
        return new int[]{getComponent().getSelectedIndex()};
    }
}
