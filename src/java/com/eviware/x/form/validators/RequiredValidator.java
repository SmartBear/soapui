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

package com.eviware.x.form.validators;

import com.eviware.x.form.ValidationMessage;
import com.eviware.x.form.XFormField;
import com.eviware.x.form.XFormFieldValidator;

public class RequiredValidator implements XFormFieldValidator
{
	private boolean trim;
	private String message;

	public RequiredValidator()
	{
		this.message = "Field requires a value";
	}

	public RequiredValidator( String message )
	{
		this.message = message;
	}

	public ValidationMessage[] validateField( XFormField formField )
	{
		String value = formField.getValue();
		if( value == null || value.length() == 0 || ( trim && value.trim().length() == 0 ) )
		{
			return new ValidationMessage[] { new ValidationMessage( message, formField ) };
		}

		return null;
	}

	public boolean isTrim()
	{
		return trim;
	}

	public void setTrim( boolean trim )
	{
		this.trim = trim;
	}
}
