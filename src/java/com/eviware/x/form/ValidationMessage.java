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

public class ValidationMessage
{
	private String message;
	private XFormField formField;

	public ValidationMessage( String message, XFormField formField )
	{
		super();
		this.message = message;
		this.formField = formField;
	}

	public XFormField getFormField()
	{
		return formField;
	}

	public String getMessage()
	{
		return message;
	}

	public void setFormField( XFormField formField )
	{
		this.formField = formField;
	}

	public void setMessage( String message )
	{
		this.message = message;
	}
}
