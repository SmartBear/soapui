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
 * WS-A Settings constants
 * 
 * @author dragica.soldo
 */
public interface WsaSettings {

	@Setting( name="Use default Action", description="Use default Action generated from wsdl", type=SettingType.BOOLEAN )
	public final static String USE_DEFAULT_ACTION = WsaSettings.class.getSimpleName() + "@" + "useDefaultAction";
	
	@Setting( name="Generate MessageID", description="Randomly generate MessageID", type=SettingType.BOOLEAN )
	public final static String GENERATE_MESSAGE_ID = WsaSettings.class.getSimpleName() + "@" + "generateMessageID";
	
	@Setting( name="Use default ReplyTo", description="Use default 'anonimoys' for ReplyTo ", type=SettingType.BOOLEAN )
	public final static String USE_DEFAULT_REPLYTO = WsaSettings.class.getSimpleName() + "@" + "useDefaultReplyTo";
	
	@Setting( name="Use default RelationshipType", description="Use default 'reply' for RelationshipType", type=SettingType.BOOLEAN )
	public final static String USE_DEFAULT_RELATIONSHIP_TYPE = WsaSettings.class.getSimpleName() + "@" + "useDefaultRelationshipType";
	
	@Setting( name="Override existing headers", description="Overrides existing WS-A headers (or skips them if unchecked) ", type=SettingType.BOOLEAN )
	public final static String OVERRIDE_EXISTING_HEADERS = WsaSettings.class.getSimpleName() + "@" + "overrideExistingHeaders";
	
}
