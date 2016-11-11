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
 * SSL-Proxy settings constants
 *
 * @author Ole.Matzura
 */

public interface SSLSettings {
    @Setting(name = "KeyStore", description = "local keyStore to use", type = SettingType.FILE)
    public final static String KEYSTORE = SSLSettings.class.getSimpleName() + "@" + "keyStore";

    @Setting(name = "KeyStore Password", description = "keyStore password", type = SettingType.PASSWORD)
    public final static String KEYSTORE_PASSWORD = SSLSettings.class.getSimpleName() + "@" + "keyStorePassword";

    @Setting(name = "Enable Mock SSL", description = "enable SSL for Mock Services", type = SettingType.BOOLEAN)
    public final static String ENABLE_MOCK_SSL = SSLSettings.class.getSimpleName() + "@" + "enableMockSSL";

    @Setting(name = "Mock Port", description = "local port to use for SSL mock services", type = SettingType.INT)
    public final static String MOCK_PORT = SSLSettings.class.getSimpleName() + "@" + "mockPort";

    @Setting(name = "Mock KeyStore", description = "local keyStore to use for mock services", type = SettingType.FILE)
    public final static String MOCK_KEYSTORE = SSLSettings.class.getSimpleName() + "@" + "mockKeyStore";

    @Setting(name = "Mock Password", description = "password for mock services", type = SettingType.PASSWORD)
    public final static String MOCK_PASSWORD = SSLSettings.class.getSimpleName() + "@" + "mockPassword";

    @Setting(name = "Mock Key Password", description = "password for mock keyStore", type = SettingType.PASSWORD)
    public final static String MOCK_KEYSTORE_PASSWORD = SSLSettings.class.getSimpleName() + "@" + "mockKeyStorePassword";

    @Setting(name = "Mock TrustStore", description = "mock trustStore to use", type = SettingType.FILE)
    public final static String MOCK_TRUSTSTORE = SSLSettings.class.getSimpleName() + "@" + "mockTrustStore";

    @Setting(name = "Mock TrustStore Password", description = "mock trustStore password", type = SettingType.PASSWORD)
    public final static String MOCK_TRUSTSTORE_PASSWORD = SSLSettings.class.getSimpleName() + "@"
            + "mockTrustStorePassword";

    @Setting(name = "Client Authentication", description = "requires client authentication", type = SettingType.BOOLEAN)
    public final static String CLIENT_AUTHENTICATION = SSLSettings.class.getSimpleName() + "@"
            + "needClientAuthentication";
}
