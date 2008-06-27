/*
 *  soapUI, copyright (C) 2004-2008 eviware.com 
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

import com.eviware.soapui.impl.wsdl.panels.teststeps.support.GroovyEditor;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansion;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.xml.JXEditTextArea;

public class GroovyEditorPropertyExpansionTarget extends AbstractPropertyExpansionTarget
{
	private final JXEditTextArea textField;

	public GroovyEditorPropertyExpansionTarget( GroovyEditor textField, ModelItem modelItem )
	{
		super( modelItem );
		this.textField = textField.getEditArea();
	}

	public void insertPropertyExpansion( PropertyExpansion expansion, Point pt )
	{
		int pos = pt == null ? -1 : textField.pointToOffset( pt );
		if( pos == -1 )
			pos = textField.getCaretPosition();
		
		String name = expansion.getProperty().getName();
		String javaName = createJavaName( name );
		
		javaName = UISupport.prompt( "Specify name of variable for property", "Get Property", javaName );
		if( javaName == null )
			return;
		
		String txt = "def " + javaName + " = context.expand( '" + expansion +"' )\n";
		
		int line = textField.getLineOfOffset( pos );
		pos = textField.getLineStartOffset( line );
		
		textField.setCaretPosition( pos );
		textField.setSelectedText( txt );
		textField.requestFocusInWindow();
	}

	private String createJavaName( String name )
	{
		StringBuffer buf = new StringBuffer();
		for( int c = 0; c < name.length(); c++ )
		{
			char ch = c == 0 ? name.toLowerCase().charAt( c ) : name.charAt( c );
			if( buf.length() == 0 && Character.isJavaIdentifierStart( ch ))
				buf.append( ch );
			else if( buf.length() > 0 && Character.isJavaIdentifierPart( ch ))
				buf.append( ch );
		}
		
		return buf.toString();
	}

	public String getValueForCreation()
	{
		return textField.getSelectedText();
	}

	public String getNameForCreation()
	{
		return null;
	}
}