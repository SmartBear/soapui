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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.eviware.soapui.config.SettingConfig;
import com.eviware.soapui.config.SettingsConfig;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.model.settings.SettingsListener;
import com.eviware.soapui.support.types.StringToStringMap;

/**
 * Settings implementation for XmlBeans generated SettingsConfig
 * 
 * @author Ole.Matzura
 */

public class XmlBeansSettingsImpl implements Settings
{
	private final Settings parent;
	private final SettingsConfig config;
	private final Map<String, SettingConfig> values = Collections.synchronizedMap( new HashMap<String, SettingConfig>() );
	private final Map<String, String> valueCache = Collections.synchronizedMap( new StringToStringMap() );
	private final Set<SettingsListener> listeners = new HashSet<SettingsListener>();
	private final ModelItem item;
	private final SettingsListener settingsListener = new WeakSettingsListener( new InternalSettingsListener() );

	public XmlBeansSettingsImpl( ModelItem item, Settings parent, SettingsConfig config )
	{
		this.item = item;
		this.config = config;
		this.parent = parent;

		List<SettingConfig> settingList = config.getSettingList();
		for( SettingConfig setting : settingList )
		{
			values.put( setting.getId(), setting );
		}

		if( parent != null )
		{
			parent.addSettingsListener( settingsListener );
		}
	}

	public boolean isSet( String id )
	{
		return values.containsKey( id );
	}

	public String getString( String id, String defaultValue )
	{
		String cachedValue = valueCache.get( id );
		if( cachedValue != null )
		{
			return cachedValue;
		}
		else
		{
			SettingConfig setting = values.get( id );
			if( setting != null )
			{
				String value = setting.getStringValue();
				valueCache.put( id, value );
				return value;
			}
		}

		return parent == null ? defaultValue : parent.getString( id, defaultValue );
	}

	public void setString( String id, String value )
	{
		String oldValue = getString( id, null );

		if( oldValue == null && value == null )
			return;

		if( value != null && value.equals( oldValue ) )
			return;

		if( value == null )
		{
			clearSetting( id );
		}
		else
		{
			if( !values.containsKey( id ) )
			{
				SettingConfig setting = config.addNewSetting();
				setting.setId( id );
				values.put( id, setting );
			}

			values.get( id ).setStringValue( value );
			valueCache.put( id, value );
		}

		notifySettingChanged( id, value, oldValue );
	}

	private void notifySettingChanged( String id, String value, String oldValue )
	{
		SettingsListener[] l = listeners.toArray( new SettingsListener[listeners.size()] );
		for( SettingsListener listener : l )
		{
			listener.settingChanged( id, value, oldValue );
		}
	}

	@Override
	public void reloadSettings()
	{
		notifySettingsReloaded();

	}

	private void notifySettingsReloaded()
	{
		SettingsListener[] l = listeners.toArray( new SettingsListener[listeners.size()] );
		for( SettingsListener listener : l )
		{
			listener.settingsReloaded();
		}
	}

	public boolean getBoolean( String id )
	{
		String value = getString( id, null );

		if( value != null )
			return Boolean.parseBoolean( value );

		return parent == null ? false : parent.getBoolean( id );
	}

	public long getLong( String id, long defaultValue )
	{
		String value = getString( id, null );

		if( value != null )
		{
			try
			{
				return Long.parseLong( value );
			}
			catch( NumberFormatException e )
			{
			}
		}

		return parent == null ? defaultValue : parent.getLong( id, defaultValue );
	}

	public void setBoolean( String id, boolean value )
	{
		if( !value )
			setString( id, "false" );
		else
			setString( id, "true" );
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
		if( values.containsKey( id ) )
		{
			int ix = config.getSettingList().indexOf( values.get( id ) );
			config.removeSetting( ix );
			values.remove( id );
			valueCache.remove( id );
		}
	}

	public ModelItem getModelItem()
	{
		return item;
	}

	public void release()
	{
		if( listeners != null )
			listeners.clear();

		if( parent != null )
			parent.removeSettingsListener( settingsListener );
	}

	private final class InternalSettingsListener implements SettingsListener
	{
		public void settingChanged( String name, String newValue, String oldValue )
		{
			if( !values.containsKey( name ) )
			{
				notifySettingChanged( name, newValue, oldValue );
			}
		}

		@Override
		public void settingsReloaded()
		{
			notifySettingsReloaded();
		}
	}

	public void setLong( String id, long value )
	{
		setString( id, Long.toString( value ) );
	}

	public void setConfig( SettingsConfig soapuiSettings )
	{
		StringToStringMap changed = new StringToStringMap();

		for( SettingConfig config : soapuiSettings.getSettingList() )
		{
			if( !config.getStringValue().equals( getString( config.getId(), null ) ) )
				changed.put( config.getId(), getString( config.getId(), null ) );
		}

		values.clear();

		config.set( soapuiSettings );
		List<SettingConfig> settingList = config.getSettingList();
		for( SettingConfig setting : settingList )
		{
			values.put( setting.getId(), setting );
		}

		for( String key : changed.keySet() )
		{
			notifySettingChanged( key, getString( key, null ), changed.get( key ) );
		}
		notifySettingsReloaded();
	}

}
