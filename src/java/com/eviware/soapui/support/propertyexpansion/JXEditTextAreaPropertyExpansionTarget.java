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

package com.eviware.soapui.support.propertyexpansion;

import java.awt.Point;

import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansion;
import com.eviware.soapui.support.xml.JXEditTextArea;

public class JXEditTextAreaPropertyExpansionTarget extends AbstractPropertyExpansionTarget
{
	private final JXEditTextArea textField;

	public JXEditTextAreaPropertyExpansionTarget( JXEditTextArea textField, ModelItem modelItem )
	{
		super( modelItem );
		this.textField = textField;
	}

	public void insertPropertyExpansion( PropertyExpansion expansion, Point pt )
	{
		int pos = pt == null ? -1 : textField.pointToOffset( pt );
		if( pos == -1 )
			pos = textField.getCaretPosition();

		textField.setSelectedText( expansion.toString() );

		if( pos >= 0 )
		{
			textField.setCaretPosition( pos );
			textField.requestFocusInWindow();
		}
	}

	public String getValueForCreation()
	{
		return textField.getSelectedText();
	}

	public String getNameForCreation()
	{
		return textField.getName();
	}
}