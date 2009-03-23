/*
 *  soapUI, copyright (C) 2004-2009 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.settings;

import com.eviware.soapui.settings.Setting.SettingType;

/**
 * SSL-Proxy settings constants
 * 
 * @author Ole.Matzura
 */

public interface SSLSettings
{
	@Setting( name = "KeyStore", description = "Local KeyStore to use", type = SettingType.FILE )
	public final static String KEYSTORE = SSLSettings.class.getSimpleName() + "@" + "keyStore";

	@Setting( name = "KeyStore Password", description = "KeyStore password", type = SettingType.PASSWORD )
	public final static String KEYSTORE_PASSWORD = SSLSettings.class.getSimpleName() + "@" + "keyStorePassword";

	@Setting( name = "Enable Mock SSL", description = "Enable SSL for Mock Services", type = SettingType.BOOLEAN )
	public final static String ENABLE_MOCK_SSL = SSLSettings.class.getSimpleName() + "@" + "enableMockSSL";

	@Setting( name = "Mock Port", description = "Local port to use for SSL mock services", type = SettingType.INT )
	public final static String MOCK_PORT = SSLSettings.class.getSimpleName() + "@" + "mockPort";

	@Setting( name = "Mock KeyStore", description = "Local KeyStore to use for mock services", type = SettingType.FILE )
	public final static String MOCK_KEYSTORE = SSLSettings.class.getSimpleName() + "@" + "mockKeyStore";

	@Setting( name = "Mock Password", description = "Password for Mock Services", type = SettingType.PASSWORD )
	public final static String MOCK_PASSWORD = SSLSettings.class.getSimpleName() + "@" + "mockPassword";

	@Setting( name = "Mock Key Password", description = "Password for Mock KeyStore", type = SettingType.PASSWORD )
	public final static String MOCK_KEYSTORE_PASSWORD = SSLSettings.class.getSimpleName() + "@" + "mockKeyStorePassword";

	@Setting( name = "Mock TrustStore", description = "Mock TrustStore to use", type = SettingType.FILE )
	public final static String MOCK_TRUSTSTORE = SSLSettings.class.getSimpleName() + "@" + "mockTrustStore";

	@Setting( name = "Mock TrustStore Password", description = "Mock TrustStore password", type = SettingType.PASSWORD )
	public final static String MOCK_TRUSTSTORE_PASSWORD = SSLSettings.class.getSimpleName() + "@"
			+ "mockTrustStorePassword";

	@Setting( name = "Client Authentication", description = "Requires client authentication", type = SettingType.BOOLEAN )
	public final static String CLIENT_AUTHENTICATION = SSLSettings.class.getSimpleName() + "@"
			+ "needClientAuthentication";
}
