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

import com.eviware.soapui.support.components.StringListFormComponent;
import com.eviware.x.form.XFormTextField;

public class JStringListFormField extends AbstractSwingXFormField<StringListFormComponent> implements XFormTextField
{
	public JStringListFormField( String tooltip )
	{
		this( tooltip, null );
	}

	public JStringListFormField( String tooltip, String defaultValue )
	{
		super( new StringListFormComponent( tooltip, false, defaultValue ) );
	}

	public void setValue( String value )
	{
		getComponent().setValue( value );
	}

	public String getValue()
	{
		return getComponent().getValue();
	}

	public void setWidth( int columns )
	{
	}
}
