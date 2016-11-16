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

package com.eviware.soapui.settings;

import com.eviware.soapui.settings.Setting.SettingType;

/**
 * Project settings constants
 *
 * @author Ole.Matzura
 */

public interface ProjectSettings {
    @Setting(name = "ProjectRoot", description = "root folder of associated external project")
    public final static String PROJECT_ROOT = ProjectSettings.class.getSimpleName() + "@projectRoot";

    public final static String PROJECT_NATURE = ProjectSettings.class.getSimpleName() + "@projectNature";

    @Setting(name = "Shadowing Password", description = "password for shadowing project password", type = SettingType.PASSWORD)
    public final static String SHADOW_PASSWORD = ProjectSettings.class.getSimpleName() + "@" + "shadowPassword";

    @Setting(name = "Hermes Config", description = "hermes", type = SettingType.FOLDER)
    public final static String HERMES_CONFIG = ProjectSettings.class.getSimpleName() + "@" + "hermesConfig";

}
