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

import com.eviware.soapui.support.action.swing.ActionList;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.x.form.XFormDialog;

public abstract class SwingXFormDialog implements XFormDialog {
    private int returnValue;

    public int getReturnValue() {
        return returnValue;
    }

    public void setReturnValue(int returnValue) {
        this.returnValue = returnValue;
    }

    public synchronized StringToStringMap show(final StringToStringMap values) {
        setValues(values);
        setVisible(true);
        return getValues();
    }

    public boolean getBooleanValue(String name) {
        try {
            return Boolean.parseBoolean(getValue(name));
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public int getIntValue(String name, int defaultValue) {
        try {
            return Integer.parseInt(getValue(name));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public void setBooleanValue(String name, boolean b) {
        setValue(name, Boolean.toString(b));
    }

    public void setIntValue(String name, int value) {
        setValue(name, Integer.toString(value));
    }

    @Override
    public ActionList getActionsList() {
        return null;
    }
}
