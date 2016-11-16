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

import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.support.types.StringToStringMap;

public interface XForm {
    public enum FieldType {
        TEXT, FOLDER, FILE, FILE_OR_FOLDER, URL, JAVA_PACKAGE, JAVA_CLASS, PASSWORD, PROJECT_FILE, PROJECT_FOLDER, TEXTAREA
    }

    public XFormTextField addTextField(String name, String description, FieldType type);

    public XFormField addCheckBox(String name, String description);

    public XFormOptionsField addComboBox(String name, Object[] values, String description);

    public void setOptions(String name, Object[] values);

    public void addSeparator(String label);

    public XFormField addComponent(String name, XFormField component);

    public StringToStringMap getValues();

    public void setValues(StringToStringMap values);

    public String getComponentValue(String name);

    public XFormField getComponent(String name);

    public enum ToolkitType {
        SWING, SWT
    }

    public String getName();

    public void setName(String name);

    public XFormField addNameSpaceTable(String label, Interface modelItem);

    public void addLabel(String name, String label);

    public XFormField[] getFormFields();

    public void setFormFieldProperty(String name, Object value);

    public void addSeparator();

    public Object[] getOptions(String name);

    public XFormField getFormField(String name);
}
