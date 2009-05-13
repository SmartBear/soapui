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

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JPasswordField;

import com.eviware.x.form.XFormTextField;

public class JPasswordFieldFormField extends AbstractSwingXFormField<JPasswordField> implements XFormTextField
{
	public JPasswordFieldFormField()
	{
		super( new JPasswordField( 15 ) );
	}

	public void setRequired( boolean required, String message )
	{
		super.setRequired( required, message );

		if( required )
			getComponent().setBorder(
					BorderFactory.createCompoundBorder( BorderFactory.createLineBorder( Color.RED ), BorderFactory
							.createEmptyBorder( 2, 2, 2, 2 ) ) );
		else
			getComponent().setBorder(
					BorderFactory.createCompoundBorder( BorderFactory.createLineBorder( Color.GRAY ), BorderFactory
							.createEmptyBorder( 2, 2, 2, 2 ) ) );
	}

	public void setValue( String value )
	{
		getComponent().setText( value );
	}

	public String getValue()
	{
		return new String( getComponent().getPassword() );
	}

	public void setWidth( int columns )
	{
		getComponent().setColumns( columns );
	}
}
