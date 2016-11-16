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

package com.eviware.soapui.actions;

import com.eviware.soapui.impl.wsdl.panels.teststeps.support.PropertyHolderTable;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionUtils;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.settings.GlobalPropertySettings;
import com.eviware.soapui.support.components.SimpleForm;
import com.eviware.soapui.support.types.StringToStringMap;

import java.awt.Dimension;

public class GlobalPropertiesPrefs implements Prefs {
    public final static String ENABLE_OVERRIDE = "Enable Override";
    private SimpleForm globalPropertiesForm;

    public SimpleForm getForm() {
        if (globalPropertiesForm == null) {
            globalPropertiesForm = new SimpleForm();

            PropertyHolderTable propertyHolderTable = new PropertyHolderTable(
                    PropertyExpansionUtils.getGlobalProperties());
            propertyHolderTable.setPreferredSize(new Dimension(200, 300));
            globalPropertiesForm.addComponent(propertyHolderTable);
            globalPropertiesForm.addSpace();
            globalPropertiesForm.appendCheckBox(ENABLE_OVERRIDE,
                    "Enables overriding of any property-reference with global properties", false);
        }

        return globalPropertiesForm;
    }

    public void getFormValues(Settings settings) {
        StringToStringMap values = new StringToStringMap();
        globalPropertiesForm.getValues(values);
        storeValues(values, settings);
    }

    public String getTitle() {
        return "Global Properties";
    }

    public StringToStringMap getValues(Settings settings) {
        StringToStringMap values = new StringToStringMap();
        values.put(ENABLE_OVERRIDE, settings.getBoolean(GlobalPropertySettings.ENABLE_OVERRIDE));
        return values;
    }

    public void setFormValues(Settings settings) {
        globalPropertiesForm.setValues(getValues(settings));
    }

    public void storeValues(StringToStringMap values, Settings settings) {
        settings.setBoolean(GlobalPropertySettings.ENABLE_OVERRIDE, values.getBoolean(ENABLE_OVERRIDE));
    }
}
