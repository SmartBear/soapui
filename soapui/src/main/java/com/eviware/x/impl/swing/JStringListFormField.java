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

import com.eviware.soapui.support.components.StringListFormComponent;
import com.eviware.x.form.XFormOptionsField;
import com.eviware.x.form.XFormTextField;

public class JStringListFormField extends AbstractSwingXFormField<StringListFormComponent> implements XFormTextField,
        XFormOptionsField {
    public JStringListFormField(String tooltip) {
        this(tooltip, null);
    }

    public JStringListFormField(String tooltip, String defaultValue) {
        super(new StringListFormComponent(tooltip, false, defaultValue));
    }

    public void setValue(String value) {
        getComponent().setValue(value);
    }

    public String getValue() {
        return getComponent().getValue();
    }

    public void setWidth(int columns) {
    }

    @Override
    public void addItem(Object value) {
        getComponent().addItem(String.valueOf(value));
    }

    @Override
    public String[] getOptions() {
        return getComponent().getData();
    }

    @Override
    public int[] getSelectedIndexes() {
        return new int[0];
    }

    @Override
    public Object[] getSelectedOptions() {
        return new Object[0];
    }

    @Override
    public void setOptions(Object[] values) {
        String[] data = new String[values.length];
        for (int c = 0; c < values.length; c++) {
            data[c] = String.valueOf(values[c]);
        }

        getComponent().setData(data);
    }

    @Override
    public void setSelectedOptions(Object[] options) {
    }
}
