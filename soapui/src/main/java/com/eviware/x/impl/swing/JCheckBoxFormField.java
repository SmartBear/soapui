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

import javax.swing.JCheckBox;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class JCheckBoxFormField extends AbstractSwingXFormField<JCheckBox> implements ChangeListener {
    public JCheckBoxFormField(String description) {
        super(new JCheckBox());
        getComponent().setText(description);
        getComponent().addChangeListener(this);
    }

    public void setValue(String value) {
        getComponent().setSelected(Boolean.parseBoolean(value));
    }

    public String getValue() {
        return Boolean.toString(getComponent().isSelected());
    }

    public void stateChanged(ChangeEvent e) {
        fireValueChanged(Boolean.toString(getComponent().isSelected()), null);
    }

    public boolean showLabel(String label) {
        return !label.equals(getComponent().getText());
    }

}
