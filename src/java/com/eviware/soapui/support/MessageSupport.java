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

package com.eviware.soapui.support;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public final class MessageSupport
{
	private static final Map<String, ResourceBundle> bundles = new HashMap<String, ResourceBundle>();
	private final Class<? extends Object> clazz;

	public MessageSupport( Class<? extends Object> clazz )
	{
		this.clazz = clazz;
	}

	public static String get( Class<? extends Object> clazz, String key, Object... args )
	{
		String result = get( clazz, key );
		return MessageFormat.format( result, args );
	}

	public static String get( Class<? extends Object> clazz, String key )
	{
		ResourceBundle bundle = null;

		try
		{
			bundle = getResourceBundleForClass( clazz );

			if( bundle == null )
				return key;

			String name = clazz.isMemberClass() ? clazz.getEnclosingClass().getSimpleName() : clazz.getSimpleName();
			return bundle.getString( name + '.' + key );
		}
		catch( MissingResourceException e )
		{
			try
			{
				return bundle.getString( key );
			}
			catch( MissingResourceException e1 )
			{
				return key;
			}
		}
	}

	public static String[] getArray( Class<? extends Object> clazz, String key )
	{
		ResourceBundle bundle = null;

		try
		{
			bundle = getResourceBundleForClass( clazz );

			if( bundle == null )
				return new String[] { key };

			String name = clazz.isMemberClass() ? clazz.getEnclosingClass().getSimpleName() : clazz.getSimpleName();

			return bundle.getStringArray( name + '.' + key );
		}
		catch( MissingResourceException e )
		{
			try
			{
				return bundle.getStringArray( key );
			}
			catch( MissingResourceException e1 )
			{
				if( clazz.isMemberClass() )
					return getArray( clazz.getEnclosingClass(), key );
				else
					return new String[] { key };
			}
		}
	}

	private static ResourceBundle getResourceBundleForClass( Class<? extends Object> clazz )
	{
		String packageName = clazz.getPackage().getName();

		if( !bundles.containsKey( packageName ) )
		{
			try
			{
				bundles.put( packageName, ResourceBundle.getBundle( packageName + ".messages" ) );
			}
			catch( MissingResourceException e )
			{
				try
				{
					bundles.put( packageName, ResourceBundle.getBundle( packageName + ".Bundle" ) );
				}
				catch( MissingResourceException e2 )
				{
				}
			}
		}

		return bundles.get( packageName );
	}

	public static MessageSupport getMessages( Class<? extends Object> name )
	{
		return new MessageSupport( name );
	}

	public String get( String key )
	{
		return MessageSupport.get( clazz, key );
	}

	public String get( String key, Object... args )
	{
		return MessageSupport.get( clazz, key, args );
	}

	public String[] getArray( String key )
	{
		return MessageSupport.getArray( clazz, key );
	}

	public boolean contains( String key )
	{
		ResourceBundle bundle = getResourceBundleForClass( clazz );
		if( bundle == null )
			return false;

		try
		{
			return bundle.getString( key ) != null;
		}
		catch( MissingResourceException e )
		{
			return false;
		}
	}

	public String[] getArray( String[] strings )
	{
		if( strings == null || strings.length == 0 )
			return strings;

		String[] array = getArray( strings[0] );
		if( array[0].equals( strings[0] ) )
			return strings;

		return array;
	}
}
