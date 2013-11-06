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

package com.eviware.x.impl.swing;

import java.awt.Dimension;

import javax.swing.Action;
import javax.swing.JButton;

import com.eviware.x.form.XFormTextField;

public class ActionFormFieldComponent extends AbstractSwingXFormField<JButton> implements XFormTextField
{
	public ActionFormFieldComponent( String name, String description )
	{
		super( new JButton( name ) );
	}

	public void setWidth( int columns )
	{
		getComponent().setPreferredSize( new Dimension( columns, 20 ) );
	}

	public String getValue()
	{
		return null;
	}

	public void setValue( String value )
	{
	}

	@Override
	public void setProperty( String name, Object value )
	{
		if( name.equals( "action" ) )
		{
			getComponent().setAction( ( Action )value );
		}
		else
		{
			super.setProperty( name, value );
		}
	}

}
