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

import javax.swing.JLabel;

public class JLabelFormField extends AbstractSwingXFormField<JLabel>
{
	public JLabelFormField( String label )
	{
		super( new JLabel() );
		getComponent().setText( label );
	}

	public void setValue( String value )
	{
		getComponent().setText( value );
	}

	public String getValue()
	{
		return getComponent().getText();
	}

	public boolean showLabel()
	{
		return false;
	}
}
