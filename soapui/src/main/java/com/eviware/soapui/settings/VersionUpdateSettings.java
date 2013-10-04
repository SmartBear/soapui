/*
 *  SoapUI, copyright (C) 2004-2012 smartbear.com
 *
 *  SoapUI is free software; you can redistribute it and/or modify it under the
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  SoapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.settings;

import com.eviware.soapui.settings.Setting.SettingType;

/**
 * Web Recording related settings constants
 * 
 * @author dragica.soldo
 */

public interface VersionUpdateSettings
{
	@Setting( name = "Check for new versions", description = "Automatically check for new versions of SoapUI", type = SettingType.BOOLEAN )
	public final static String AUTO_CHECK_VERSION_UPDATE = VersionUpdateSettings.class.getSimpleName() + "@"
			+ "auto-check-version-update";

}
