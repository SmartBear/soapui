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

import com.eviware.x.form.XFormTextField;

import javax.swing.Action;
import javax.swing.JButton;
import java.awt.Dimension;

public class ActionFormFieldComponent extends AbstractSwingXFormField<JButton> implements XFormTextField {
    public ActionFormFieldComponent(String name, String description) {
        super(new JButton(name));
    }

    public void setWidth(int columns) {
        getComponent().setPreferredSize(new Dimension(columns, 20));
    }

    public String getValue() {
        return null;
    }

    public void setValue(String value) {
    }

    @Override
    public void setProperty(String name, Object value) {
        if (name.equals("action")) {
            getComponent().setAction((Action) value);
        } else {
            super.setProperty(name, value);
        }
    }

}
