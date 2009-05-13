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

package com.eviware.soapui.support.xml;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;

/**
 * Support class for reading XmlObject based configurations..
 * 
 * @author Ole.Matzura
 */

public class XmlObjectConfigurationReader
{
	private final XmlObject config;

	public XmlObjectConfigurationReader( XmlObject config )
	{
		this.config = config;
	}

	public int readInt( String name, int def )
	{
		if( config == null )
			return def;

		try
		{
			String str = readString( name, null );
			return str == null ? def : Integer.parseInt( str );
		}
		catch( NumberFormatException e )
		{
		}

		return def;
	}

	public long readLong( String name, int def )
	{
		if( config == null )
			return def;

		try
		{
			String str = readString( name, null );
			return str == null ? def : Long.parseLong( str );
		}
		catch( NumberFormatException e )
		{
		}

		return def;
	}

	public float readFloat( String name, float def )
	{
		if( config == null )
			return def;

		try
		{
			String str = readString( name, null );
			return str == null ? def : Float.parseFloat( str );
		}
		catch( NumberFormatException e )
		{
		}

		return def;
	}

	public String readString( String name, String def )
	{
		if( config == null )
			return def;

		XmlObject[] paths = config.selectPath( "$this/" + name );
		if( paths.length == 1 )
		{
			XmlCursor cursor = paths[0].newCursor();
			String textValue = cursor.getTextValue();
			cursor.dispose();
			return textValue;
		}

		return def;
	}

	public String[] readStrings( String name )
	{
		if( config == null )
			return null;

		XmlObject[] paths = config.selectPath( "$this/" + name );
		String[] result = new String[paths.length];

		for( int c = 0; c < paths.length; c++ )
		{
			XmlCursor cursor = paths[c].newCursor();
			result[c] = cursor.getTextValue();
			cursor.dispose();
		}

		return result;
	}

	public boolean readBoolean( String name, boolean def )
	{
		try
		{
			return Boolean.valueOf( readString( name, String.valueOf( def ) ) );
		}
		catch( Exception e )
		{
			return def;
		}
	}
}
