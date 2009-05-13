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

package com.eviware.x.form;

import java.util.ArrayList;

public class ComponentEnabler implements XFormFieldListener
{
	private final XFormField formField;

	// Cannot use HashMap, because the XFormField may be a Proxy.
	private ArrayList<FieldValue> fields = new ArrayList<FieldValue>();

	private static class FieldValue
	{
		XFormField field;
		String value;

		public FieldValue( XFormField field, String value )
		{
			this.field = field;
			this.value = value;
		}
	}

	public ComponentEnabler( XFormField formField )
	{
		this.formField = formField;

		formField.addFormFieldListener( this );
	}

	/**
	 * This should not be called directly from the dialog builders, because
	 * <code>field</code> may be a Proxy (on the Eclipse platform). Instead, call
	 * <code>addComponentEnablesFor(field, value)</code> on the combo box.
	 * 
	 * @param field
	 * @param value
	 */
	void add( XFormField field, String value )
	{
		String fieldValue = formField.getValue();
		boolean enable = ( fieldValue == null ? value == null : fieldValue.equals( value ) );
		field.setEnabled( enable );
		fields.add( new FieldValue( field, value ) );
	}

	public void valueChanged( XFormField sourceField, String newValue, String oldValue )
	{
		for( FieldValue f : fields )
		{
			boolean enable = newValue.equals( f.value );
			f.field.setEnabled( enable );
		}
	}
}
