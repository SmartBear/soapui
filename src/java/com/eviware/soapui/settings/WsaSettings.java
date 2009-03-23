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
 * WS-A Settings constants
 * 
 * @author dragica.soldo
 */
public interface WsaSettings
{
	@Setting( name = "Soap action overrides wsa action", description = "SOAP action if present overrides wsa action", type = SettingType.BOOLEAN )
	public final static String SOAP_ACTION_OVERRIDES_WSA_ACTION = WsaSettings.class.getSimpleName() + "@"
			+ "soapActionOverridesWsaAction";

	@Setting( name = "Use default RelationshipType", description = "Use default 'reply' for RelationshipType", type = SettingType.BOOLEAN )
	public final static String USE_DEFAULT_RELATIONSHIP_TYPE = WsaSettings.class.getSimpleName() + "@"
			+ "useDefaultRelationshipType";

	@Setting( name = "Use default RelatesTo", description = "Use default 'unspecified' for RelatesTo", type = SettingType.BOOLEAN )
	public final static String USE_DEFAULT_RELATES_TO = WsaSettings.class.getSimpleName() + "@" + "useDefaultRelatesTo";

	@Setting( name = "Override existing headers", description = "Replaces existing WS-A headers (or skips them if unchecked) ", type = SettingType.BOOLEAN )
	public final static String OVERRIDE_EXISTING_HEADERS = WsaSettings.class.getSimpleName() + "@"
			+ "overrideExistingHeaders";

	@Setting( name = "Enable for optional Addressing policy", description = "Enables WS-Addressing for Addressing Optional='true'", type = SettingType.BOOLEAN )
	public final static String ENABLE_FOR_OPTIONAL = WsaSettings.class.getSimpleName() + "@" + "enableForOptional";

}
