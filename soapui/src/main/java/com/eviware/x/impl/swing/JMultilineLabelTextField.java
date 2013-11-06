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

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.eviware.soapui.support.UISupport;
import com.eviware.x.form.XFormTextField;

public class JMultilineLabelTextField extends AbstractSwingXFormField<JComponent> implements XFormTextField
{
	private JScrollPane scrollPane;

	public JMultilineLabelTextField()
	{
		super( new JTextArea() );

		getTextArea().setEditable( false );
		getTextArea().setEnabled( false );

		scrollPane = new JScrollPane( getTextArea() );
		UISupport.setFixedSize( scrollPane, 300, 100 );
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
