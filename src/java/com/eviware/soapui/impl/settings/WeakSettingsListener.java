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

package com.eviware.soapui.impl.settings;

import java.lang.ref.WeakReference;

import com.eviware.soapui.model.settings.SettingsListener;

/**
 * Weak listener to settingChanged
 * 
 * @author ole.matzura
 */

public final class WeakSettingsListener implements SettingsListener
{
	private final WeakReference<SettingsListener> listenerReference;

	public WeakSettingsListener( SettingsListener listener )
	{
		listenerReference = new WeakReference<SettingsListener>( listener );
	}

	public void settingChanged( String name, String newValue, String oldValue )
	{
		if( listenerReference.get() != null )
			listenerReference.get().settingChanged( name, newValue, oldValue );
	}
}