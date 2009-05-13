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
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.eviware.soapui.support.components.JUndoableTextArea;
import com.eviware.x.form.XFormTextField;

public class JTextAreaFormField extends AbstractSwingXFormField<JComponent> implements XFormTextField
{
	private JScrollPane scrollPane;

	public JTextAreaFormField()
	{
		super( new JUndoableTextArea() );

		scrollPane = new JScrollPane( super.getComponent() );
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

	public JTextArea getTextArea()
	{
		return ( JTextArea )super.getComponent();
	}

	public JComponent getComponent()
	{
		return scrollPane;
	}

	public void setValue( String value )
	{
		getTextArea().setText( value );
	}

	public String getValue()
	{
		return getTextArea().getText();
	}

	public void setWidth( int columns )
	{
		getTextArea().setColumns( columns );
	}

	@Override
	public boolean isMultiRow()
	{
		return true;
	}
}
