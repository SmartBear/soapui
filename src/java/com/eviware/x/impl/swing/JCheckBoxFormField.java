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

import javax.swing.JCheckBox;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class JCheckBoxFormField extends AbstractSwingXFormField<JCheckBox> implements ChangeListener
{
	public JCheckBoxFormField( String description )
	{
		super( new JCheckBox() );
		getComponent().setText( description );
		getComponent().addChangeListener( this );
	}

	public void setValue( String value )
	{
		getComponent().setSelected( Boolean.parseBoolean( value ) );
	}

	public String getValue()
	{
		return Boolean.toString( getComponent().isSelected() );
	}

	public void stateChanged( ChangeEvent e )
	{
		fireValueChanged( Boolean.toString( getComponent().isSelected() ), null );
	}

	public boolean showLabel( String label )
	{
		return !label.equals( getComponent().getText() );
	}

}
