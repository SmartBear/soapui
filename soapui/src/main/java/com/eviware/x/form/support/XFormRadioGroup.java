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

import com.eviware.x.form.XFormOptionsField;
import com.eviware.x.impl.swing.AbstractSwingXFormField;

import javax.swing.AbstractButton;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Swing-specific RadioGroup
 *
 * @author ole.matzura
 */

public class XFormRadioGroup extends AbstractSwingXFormField<JPanel> implements XFormOptionsField {
    protected ButtonGroup buttonGroup;
    protected Map<String, ButtonModel> models = new HashMap<String, ButtonModel>();
    protected List<Object> items = new ArrayList<Object>();

    public XFormRadioGroup(String[] values) {
        super(new JPanel());

        buttonGroup = new ButtonGroup();
        getComponent().setLayout(new BoxLayout(getComponent(), BoxLayout.Y_AXIS));

        for (String value : values) {
            addItem(value);
        }
    }

    public String getValue() {
        ButtonModel selection = buttonGroup.getSelection();
        return selection == null ? null : selection.getActionCommand();
    }

    public void setValue(String value) {
        buttonGroup.setSelected(models.get(value), true);
    }

    public void addItem(Object value) {
        JRadioButton button;
        if (value instanceof Enum) {
            button = new JRadioButton(value.toString());
            button.setActionCommand(((Enum) value).name());
            models.put(((Enum) value).name(), button.getModel());
        } else {
            button = new JRadioButton(String.valueOf(value));
            button.setActionCommand(String.valueOf(value));
            models.put(String.valueOf(value), button.getModel());
        }

        button.setName(String.valueOf(value));
        button.setFocusPainted(false);
        button.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                fireValueChanged(e.getActionCommand(), null);
            }
        });

        getComponent().add(button);
        buttonGroup.add(button);
        items.add(value);
    }

    public Object[] getOptions() {
        return items.toArray();
    }

    public Object[] getSelectedOptions() {
        return new String[]{getValue()};
    }

    public void setOptions(Object[] values) {
        while (buttonGroup.getButtonCount() > 0) {
            buttonGroup.remove(buttonGroup.getElements().nextElement());
        }

        models.clear();
        items.clear();
        getComponent().removeAll();

        for (Object value : values) {
            addItem(value);
        }
    }

    public void setSelectedOptions(Object[] options) {

    }

    public int[] getSelectedIndexes() {
        return new int[]{items.indexOf(getValue())};
    }

    public void setDisabled() {
        for (Enumeration<AbstractButton> buttons = buttonGroup.getElements(); buttons.hasMoreElements(); ) {
            buttons.nextElement().setEnabled(false);
        }
    }
}
