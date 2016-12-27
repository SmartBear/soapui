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

import com.eviware.x.form.validators.RequiredValidator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class AbstractXFormField<T> implements XFormField {
    private Set<XFormFieldListener> listeners;
    private List<XFormFieldValidator> validators;
    private RequiredValidator requiredValidator;
    private ComponentEnabler enabler = null;

    public AbstractXFormField() {
    }

    @Override
    public boolean isVisible() {
        return true;
    }

    public abstract T getComponent();

    public void addFormFieldListener(XFormFieldListener listener) {
        if (listeners == null) {
            listeners = new HashSet<XFormFieldListener>();
        }

        listeners.add(listener);
    }

    public void addFormFieldValidator(XFormFieldValidator validator) {
        if (validators == null) {
            validators = new ArrayList<XFormFieldValidator>();
        }

        validators.add(validator);
    }

    public void addComponentEnabler(XFormField tf, String value) {
        if (enabler == null) {
            enabler = new ComponentEnabler(this);
        }
        enabler.add(tf, value);
    }

    public boolean isRequired() {
        return requiredValidator != null;
    }

    public void removeFieldListener(XFormFieldListener listener) {
        if (listeners != null) {
            listeners.remove(listener);
        }
    }

    public void removeFormFieldValidator(XFormFieldValidator validator) {
        if (validators != null) {
            validators.remove(validator);
        }
    }

    public void setRequired(boolean required, String message) {
        if (requiredValidator != null) {
            removeFormFieldValidator(requiredValidator);
        }

        if (required) {
            requiredValidator = new RequiredValidator(message);
            addFormFieldValidator(requiredValidator);
        }
    }

    public ValidationMessage[] validate() {
        if (validators == null || validators.isEmpty()) {
            return null;
        }

        ArrayList<ValidationMessage> messages = new ArrayList<ValidationMessage>();

        for (XFormFieldValidator validator : validators) {
            ValidationMessage[] validateField = validator.validateField(this);
            if (validateField != null && validateField.length > 0) {
                messages.addAll(Arrays.asList(validateField));
            }
        }

        return messages.toArray(new ValidationMessage[messages.size()]);
    }

    protected void fireValueChanged(String newValue, String oldValue) {
        if (listeners == null) {
            return;
        }

        for (XFormFieldListener listener : listeners) {
            listener.valueChanged(this, newValue, oldValue);
        }
    }

    public Object getProperty(String name) {
        return null;
    }

    public abstract void setProperty(String name, Object value);

    public boolean isMultiRow() {
        return false;
    }
}
