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

package com.eviware.soapui.model.settings;

/**
 * Base interface for settings available in the soapui model
 * 
 * @author Ole.Matzura
 */

public interface Settings
{
	public String getString( String id, String defaultValue );

	public void setString( String id, String value );

	/**
	 * booleans always default to false..
	 */

	public boolean getBoolean( String id );

	public void setBoolean( String id, boolean value );

	public void addSettingsListener( SettingsListener listener );

	public void removeSettingsListener( SettingsListener listener );

	public void clearSetting( String id );

	public long getLong( String id, long defaultValue );

	public boolean isSet( String id );

	public void setLong( String id, long value );
}
