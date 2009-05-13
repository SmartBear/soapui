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

package com.eviware.x.impl.swing;

import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.x.form.XFormDialog;

public abstract class SwingXFormDialog implements XFormDialog
{
	private int returnValue;

	public int getReturnValue()
	{
		return returnValue;
	}

	public void setReturnValue( int returnValue )
	{
		this.returnValue = returnValue;
	}

	public synchronized StringToStringMap show( final StringToStringMap values )
	{
		setValues( values );
		setVisible( true );
		return getValues();
	}

	public boolean getBooleanValue( String name )
	{
		try
		{
			return Boolean.parseBoolean( getValue( name ) );
		}
		catch( NumberFormatException e )
		{
			return false;
		}
	}

	public int getIntValue( String name, int defaultValue )
	{
		try
		{
			return Integer.parseInt( getValue( name ) );
		}
		catch( NumberFormatException e )
		{
			return defaultValue;
		}
	}

	public void setBooleanValue( String name, boolean b )
	{
		setValue( name, Boolean.toString( b ) );
	}

	public void setIntValue( String name, int value )
	{
		setValue( name, Integer.toString( value ) );
	}
}
