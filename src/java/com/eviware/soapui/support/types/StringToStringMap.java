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

package com.eviware.soapui.support.types;

import java.util.HashMap;
import java.util.Map;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.StringToStringMapConfig;
import com.eviware.soapui.config.StringToStringMapConfig.Entry;

/**
 * HashMap&lt;String,String&gt;
 * 
 * @author Ole.Matzura
 */

public class StringToStringMap extends HashMap<String, String>
{
	private boolean equalsOnThis;

	public StringToStringMap()
	{
		super();
	}

	public StringToStringMap( int initialCapacity, float loadFactor )
	{
		super( initialCapacity, loadFactor );
	}

	public StringToStringMap( int initialCapacity )
	{
		super( initialCapacity );
	}

	public StringToStringMap( Map<? extends String, ? extends String> m )
	{
		super( m );
	}

	public String get( String key, String defaultValue )
	{
		String value = get( key );
		return value == null ? defaultValue : value;
	}

	/**
	 * Get the inverse of this map.
	 */
	public StringToStringMap inverse()
	{
		StringToStringMap inverse = new StringToStringMap();
		for( String key : keySet() )
		{
			String value = get( key );
			inverse.put( value, key );
		}
		return inverse;
	}

	public String toXml()
	{
		StringToStringMapConfig xmlConfig = StringToStringMapConfig.Factory.newInstance();

		for( String key : keySet() )
		{
			Entry entry = xmlConfig.addNewEntry();
			entry.setKey( key );
			entry.setValue( get( key ) );
		}

		return xmlConfig.toString();
	}

	public static StringToStringMap fromXml( String value )
	{
		if( value == null || value.trim().length() == 0 )
			return new StringToStringMap();

		try
		{
			StringToStringMapConfig nsMapping = StringToStringMapConfig.Factory.parse( value );

			return fromXml( nsMapping );
		}
		catch( Exception e )
		{
			SoapUI.logError( e );
		}

		return new StringToStringMap();
	}

	public static StringToStringMap fromXml( StringToStringMapConfig nsMapping )
	{
		StringToStringMap result = new StringToStringMap();
		for( Entry entry : nsMapping.getEntryList() )
		{
			result.put( entry.getKey(), entry.getValue() );
		}
		return result;
	}

	public final boolean getBoolean( String key )
	{
		return Boolean.parseBoolean( get( key ) );
	}

	public boolean hasValue( String key )
	{
		return containsKey( key ) && get( key ).length() > 0;
	}

	public void putIfMissing( String key, String value )
	{
		if( !containsKey( key ) )
			put( key, value );
	}

	public void put( String key, boolean value )
	{
		put( key, Boolean.toString( value ) );
	}

	public static StringToStringMap fromHttpHeader( String value )
	{
		StringToStringMap result = new StringToStringMap();

		int ix = value.indexOf( ';' );
		while( ix > 0 )
		{
			extractNVPair( value.substring( 0, ix ), result );
			value = value.substring( ix + 1 );
			ix = value.indexOf( ';' );
		}

		if( value.length() > 2 )
		{
			extractNVPair( value, result );
		}

		return result;
	}

	private static void extractNVPair( String value, StringToStringMap result )
	{
		int ix;
		ix = value.indexOf( '=' );
		if( ix != -1 )
		{
			String str = value.substring( ix + 1 ).trim();
			if( str.startsWith( "\"" ) && str.endsWith( "\"" ) )
				str = str.substring( 1, str.length() - 1 );

			result.put( value.substring( 0, ix ).trim(), str );
		}
	}

	public void setEqualsOnThis( boolean equalsOnThis )
	{
		this.equalsOnThis = equalsOnThis;
	}

	@Override
	public boolean equals( Object o )
	{
		return equalsOnThis ? this == o : super.equals( o );
	}

	public int getInt( String key, int def )
	{
		try
		{
			return Integer.parseInt( get( key ) );
		}
		catch( Exception e )
		{
			return def;
		}
	}

	public String[] getKeys()
	{
		return keySet().toArray( new String[size()] );
	}
}
