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

package com.eviware.x.form.validators;

import com.eviware.soapui.support.StringUtils;
import com.eviware.x.form.ValidationMessage;
import com.eviware.x.form.XFormField;
import com.eviware.x.form.XFormFieldValidator;
import com.eviware.x.form.XFormOptionsField;

public class RequiredValidator implements XFormFieldValidator {
    private boolean trim;
    private String message;

    public RequiredValidator() {
        this.message = "Field requires a value";
    }

    public RequiredValidator(String message) {
        this.message = message;
    }

    public ValidationMessage[] validateField(XFormField formField) {
        String value = null;

        if (formField instanceof XFormOptionsField) {
            value = ((XFormOptionsField) formField).getSelectedIndexes().length == 0 ? null : "check";
        } else {
            value = formField.getValue();
        }

        if (!StringUtils.hasContent(value)) {
            return new ValidationMessage[]{new ValidationMessage(message, formField)};
        }

        return null;
    }

    public boolean isTrim() {
        return trim;
    }

    public void setTrim(boolean trim) {
        this.trim = trim;
    }
}
