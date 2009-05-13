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
import javax.swing.text.Document;

import com.eviware.soapui.support.DocumentListenerAdapter;
import com.eviware.soapui.support.components.JUndoableTextField;
import com.eviware.x.form.XFormTextField;

public class JTextFieldFormField extends AbstractSwingXFormField<JUndoableTextField> implements XFormTextField
{
	private boolean updating;
	private String oldValue;

	public JTextFieldFormField()
	{
		super( new JUndoableTextField() );

		getComponent().getDocument().addDocumentListener( new DocumentListenerAdapter()
		{

			@Override
			public void update( Document document )
			{
				String text = getComponent().getText();

				if( !updating )
					fireValueChanged( text, oldValue );

				oldValue = text;
			}
		} );
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
		updating = true;
		oldValue = null;
		getComponent().setText( value );
		updating = false;
	}

	public String getValue()
	{
		return getComponent().getText();
	}

	public void setWidth( int columns )
	{
		getComponent().setColumns( columns );
	}
}
