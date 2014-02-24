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

package com.eviware.soapui.impl.settings;

import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.model.settings.SettingsListener;
import com.eviware.soapui.support.types.StringToStringMap;

import java.util.HashSet;
import java.util.Set;

/**
 * Default Settings implementation
 * 
 * @author Ole.Matzura
 */

public class SettingsImpl implements Settings
{
	private final Settings parent;
	private final StringToStringMap values = new StringToStringMap();
	private final Set<SettingsListener> listeners = new HashSet<SettingsListener>();

	public SettingsImpl()
	{
		this( null );
	}

	public SettingsImpl( Settings parent )
	{
		this.parent = parent;
	}

	public boolean isSet( String id )
	{
		return values.containsKey( id );
	}

	public String getString( String id, String defaultValue )
	{
		if( values.containsKey( id ) )
			return values.get( id );
		return parent == null ? defaultValue : parent.getString( id, defaultValue );
	}

	public void setString( String id, String value )
	{
		String oldValue = getString( id, null );
		values.put( id, value );

		for( SettingsListener listener : listeners )
		{
			listener.settingChanged( id, value, oldValue );
		}
	}

	public void reloadSettings()
	{
		for( SettingsListener listener : listeners )
		{
			listener.settingsReloaded();
		}
	}

	public boolean getBoolean( String id )
	{
		return getBoolean( id, false );
	}

	@Override
	public boolean getBoolean( String id, boolean defaultValue )
	{
		if( values.containsKey( id ) )
			return Boolean.parseBoolean( values.get( id ) );
		return parent == null ? defaultValue : parent.getBoolean( id );
	}

	public void setBoolean( String id, boolean value )
	{
		String oldValue = getString( id, null );
		values.put( id, Boolean.toString( value ) );

		for( SettingsListener listener : listeners )
		{
			listener.settingChanged( id, Boolean.toString( value ), oldValue );
		}
	}

	public long getLong( String id, long defaultValue )
	{
		if( values.containsKey( id ) )
		{
			try
			{
				return Long.parseLong( values.get( id ) );
			}
			catch( NumberFormatException e )
			{
			}
		}

		return defaultValue;
	}

	public void addSettingsListener( SettingsListener listener )
	{
		listeners.add( listener );
	}

	public void removeSettingsListener( SettingsListener listener )
	{
		listeners.remove( listener );
	}

	public void clearSetting( String id )
	{
		values.remove( id );
	}

	public void setLong( String id, long value )
	{
		values.put( id, Long.toString( value ) );
	}
}
