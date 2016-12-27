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

public interface XFormField {
    public final static String CURRENT_DIRECTORY = XFormField.class.getName() + "@currentDirectory";

    public void setValue(String value);

    public String getValue();

    public void setEnabled(boolean enabled);

    public boolean isEnabled();

    public void setRequired(boolean required, String message);

    public boolean isRequired();

    public void setToolTip(String tooltip);

    public void addFormFieldListener(XFormFieldListener listener);

    public void removeFieldListener(XFormFieldListener listener);

    public void addFormFieldValidator(XFormFieldValidator validator);

    public void removeFormFieldValidator(XFormFieldValidator validator);

    public void addComponentEnabler(XFormField tf, String value);

    public void setProperty(String name, Object value);

    public Object getProperty(String name);

    public ValidationMessage[] validate();

    boolean isVisible();
}
