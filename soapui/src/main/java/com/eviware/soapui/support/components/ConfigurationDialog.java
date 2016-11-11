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

package com.eviware.soapui.support.components;

import com.eviware.soapui.support.action.swing.ActionList;

import java.util.Map;

/**
 * Behavior of a configuration dialog
 *
 * @author Ole.Matzura
 */

public interface ConfigurationDialog {
    public boolean show(Map<String, String> values);

    public void hide();

    public void addTextField(String name, String tooltip);

    public void addTextField(String name, String tooltip, FieldType type);

    public void addCheckBox(String caption, String label, boolean selected);

    public void addComboBox(String label, Object[] objects, String tooltip);

    public void setValues(String id, String[] values);

    public void addComboBox(String label, String tooltip);

    public ActionList getActions();

    public void getValues(Map<String, String> values);

    public enum FieldType {
        TEXT, DIRECTORY, FILE, URL, JAVA_PACKAGE, JAVA_CLASS
    }
}
