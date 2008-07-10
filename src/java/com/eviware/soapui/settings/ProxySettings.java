/*
 *  soapUI, copyright (C) 2004-2008 eviware.com 
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
 * HTTP-Proxy settings constants
 * 
 * @author Ole.Matzura
 */

public interface ProxySettings
{
	@Setting( name="Host", description="proxy host to use" )
	public final static String HOST = ProxySettings.class.getSimpleName() + "@" + "host";
	
	@Setting( name="Port", description="proxy port to use", type=SettingType.INT )
	public final static String PORT = ProxySettings.class.getSimpleName() + "@" + "port";
	
	@Setting( name="Username", description="proxy username to use" )
	public final static String USERNAME = ProxySettings.class.getSimpleName() + "@" + "username";
	
	@Setting( name="Password", description="proxy password to use", type=SettingType.PASSWORD )
	public final static String PASSWORD = ProxySettings.class.getSimpleName() + "@" + "password";
	
	@Setting( name="Excludes", description="Comma-seperated list of hosts to exclude" )
	public final static String EXCLUDES = ProxySettings.class.getSimpleName() + "@" + "excludes";

	@Setting( name="Shadow Proxy Password", description="Enable shadowing proxy password", type=SettingType.BOOLEAN)
	public final static String ENABLE_SHADOWING = ProxySettings.class.getSimpleName() + "@" + "enableShadowProxyPassword";
	
	@Setting( name="Shadowing Password", description="password for shadowing proxy password in settings file", type=SettingType.PASSWORD )
	public final static String SHADOW_PASSWORD = ProxySettings.class.getSimpleName() + "@" + "shadowProxyPassword";
	
}

