/*
 * SoapUI, Copyright (C) 2004-2019 SmartBear Software
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

package com.eviware.soapui.settings;

import com.eviware.soapui.settings.Setting.SettingType;

public interface SecuritySettings {

    @Setting(name = "Password", description = "password for shadowing proxy password in settings file", type = SettingType.PASSWORD)
    String SHADOW_PASSWORD = SecuritySettings.class.getSimpleName() + "@" + "shadowProxyPassword";

    @Setting(name = "Disable the Load and Save scripts", description = "Do not run the Load and Save scripts on opening and saving projects", type = SettingType.BOOLEAN)
    String DISABLE_PROJECT_LOAD_SAVE_SCRIPTS = SecuritySettings.class.getSimpleName() + "@disable_project_load_save_scripts";
}
