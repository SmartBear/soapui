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

package com.eviware.x.form;

import java.util.ArrayList;

public class ComponentEnabler implements XFormFieldListener {
    private final XFormField formField;

    // Cannot use HashMap, because the XFormField may be a Proxy.
    private ArrayList<FieldValue> fields = new ArrayList<FieldValue>();

    private static class FieldValue {
        XFormField field;
        String value;

        public FieldValue(XFormField field, String value) {
            this.field = field;
            this.value = value;
        }
    }

    public ComponentEnabler(XFormField formField) {
        this.formField = formField;

        formField.addFormFieldListener(this);
    }

    /**
     * This should not be called directly from the dialog builders, because
     * <code>field</code> may be a Proxy (on the Eclipse platform). Instead, call
     * <code>addComponentEnablesFor(field, value)</code> on the combo box.
     *
     * @param field
     * @param value
     */
    void add(XFormField field, String value) {
        String fieldValue = formField.getValue();
        boolean enable = (fieldValue == null ? value == null : fieldValue.equals(value));
        field.setEnabled(enable);
        fields.add(new FieldValue(field, value));
    }

    public void valueChanged(XFormField sourceField, String newValue, String oldValue) {
        for (FieldValue f : fields) {
            boolean enable = newValue.equals(f.value);
            f.field.setEnabled(enable);
        }
    }
}
