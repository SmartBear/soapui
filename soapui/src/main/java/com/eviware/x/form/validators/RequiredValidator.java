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

package com.eviware.x.form.validators;

import com.eviware.soapui.support.StringUtils;
import com.eviware.x.form.ValidationMessage;
import com.eviware.x.form.XFormField;
import com.eviware.x.form.XFormFieldValidator;
import com.eviware.x.form.XFormOptionsField;

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
		String value = null;

		if( formField instanceof XFormOptionsField )
		{
			value = ( ( XFormOptionsField )formField ).getSelectedIndexes().length == 0 ? null : "check";
		}
		else
		{
			value = formField.getValue();
		}

		if( !StringUtils.hasContent( value ) )
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
