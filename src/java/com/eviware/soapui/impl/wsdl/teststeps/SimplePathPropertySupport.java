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

package com.eviware.soapui.impl.wsdl.teststeps;

import com.eviware.soapui.impl.wsdl.AbstractWsdlModelItem;

public class SimplePathPropertySupport extends AbstractPathPropertySupport
{
	private String value;

	public SimplePathPropertySupport( AbstractWsdlModelItem<?> modelItem )
	{
		super( modelItem, null );
	}

	public void setPropertyValue( String value )
	{
		this.value = value;
	}

	public String getPropertyValue()
	{
		return value;
	}

	public void set( String value )
	{
		super.set( value, false );
	}
}