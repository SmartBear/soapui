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
 * HTTP-Proxy settings constants
 *
 * @author Ole.Matzura
 */

public interface ProxySettings {
    @Setting(name = "Host", description = "proxy host to use")
    public final static String HOST = ProxySettings.class.getSimpleName() + "@" + "host";

    @Setting(name = "Port", description = "proxy port to use", type = SettingType.INT)
    public final static String PORT = ProxySettings.class.getSimpleName() + "@" + "port";

    @Setting(name = "Username", description = "proxy username to use")
    public final static String USERNAME = ProxySettings.class.getSimpleName() + "@" + "username";

    @Setting(name = "Password", description = "proxy password to use", type = SettingType.PASSWORD)
    public final static String PASSWORD = ProxySettings.class.getSimpleName() + "@" + "password";

    @Setting(name = "Excludes", description = "Comma-seperated list of hosts to exclude")
    public final static String EXCLUDES = ProxySettings.class.getSimpleName() + "@" + "excludes";

    @Setting(name = "Enable Proxy", description = "enable using proxy", type = SettingType.BOOLEAN, defaultValue = "true")
    public final static String ENABLE_PROXY = ProxySettings.class.getSimpleName() + "@" + "enableProxy";

    @Setting(name = "Auto Proxy", description = "use automatic proxy detection", type = SettingType.BOOLEAN, defaultValue = "true")
    public final static String AUTO_PROXY = ProxySettings.class.getSimpleName() + "@" + "autoProxy";
}
