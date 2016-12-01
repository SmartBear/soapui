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

import com.eviware.soapui.support.action.swing.ActionList;
import com.eviware.soapui.support.types.StringToStringMap;

import javax.swing.Action;

public interface XFormDialog {
    public final static int OK_OPTION = 1;
    public final static int CANCEL_OPTION = 2;

    public void setValues(StringToStringMap values);

    public StringToStringMap getValues();

    public void setVisible(boolean visible);

    public int getReturnValue();

    public void setValue(String field, String value);

    public String getValue(String field);

    public boolean show();

    public StringToStringMap show(StringToStringMap values);

    public boolean validate();

    public void setOptions(String field, Object[] options);

    public XFormField getFormField(String name);

    public void setFormFieldProperty(String name, Object value);

    public int getValueIndex(String name);

    public int getIntValue(String name, int defaultValue);

    public boolean getBooleanValue(String name);

    public void setBooleanValue(String name, boolean b);

    public void setIntValue(String name, int value);

    public void setWidth(int i);

    public void release();

    public void addAction(Action action);

    public XForm[] getForms();

    public void setSize(int i, int j);

    ActionList getActionsList();
}
