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
 * Support class for building XmlObject based configurations
 * 
 * @author Ole.Matzura
 */

public class XmlObjectConfigurationBuilder
{
	private XmlObject config;
	private XmlCursor cursor;

	public XmlObjectConfigurationBuilder( XmlObject config )
	{
		this.config = config;
		cursor = config.newCursor();
		cursor.toNextToken();
	}

	public XmlObjectConfigurationBuilder()
	{
		this( XmlObject.Factory.newInstance() );
		cursor = config.newCursor();
		cursor.toNextToken();
	}

	public XmlObjectConfigurationBuilder add( String name, String value )
	{
		cursor.insertElementWithText( name, value );
		return this;
	}

	public XmlObjectConfigurationBuilder add( String name, int value )
	{
		cursor.insertElementWithText( name, String.valueOf( value ) );
		return this;
	}

	public XmlObjectConfigurationBuilder add( String name, long value )
	{
		cursor.insertElementWithText( name, String.valueOf( value ) );
		return this;
	}

	public XmlObjectConfigurationBuilder add( String name, float value )
	{
		cursor.insertElementWithText( name, String.valueOf( value ) );
		return this;
	}

	public XmlObject finish()
	{
		cursor.dispose();
		return config;
	}

	public XmlObjectConfigurationBuilder add( String name, boolean value )
	{
		cursor.insertElementWithText( name, String.valueOf( value ) );
		return this;
	}

	public void add( String name, String[] values )
	{
		for( String value : values )
			add( name, value );
	}
}
