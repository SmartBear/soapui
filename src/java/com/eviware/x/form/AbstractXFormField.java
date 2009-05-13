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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.eviware.x.form.validators.RequiredValidator;

public abstract class AbstractXFormField<T> implements XFormField
{
	private Set<XFormFieldListener> listeners;
	private List<XFormFieldValidator> validators;
	private RequiredValidator requiredValidator;
	private ComponentEnabler enabler = null;

	public AbstractXFormField()
	{
	}

	public abstract T getComponent();

	public void addFormFieldListener( XFormFieldListener listener )
	{
		if( listeners == null )
			listeners = new HashSet<XFormFieldListener>();

		listeners.add( listener );
	}

	public void addFormFieldValidator( XFormFieldValidator validator )
	{
		if( validators == null )
			validators = new ArrayList<XFormFieldValidator>();

		validators.add( validator );
	}

	public void addComponentEnabler( XFormField tf, String value )
	{
		if( enabler == null )
		{
			enabler = new ComponentEnabler( this );
		}
		enabler.add( tf, value );
	}

	public boolean isRequired()
	{
		return requiredValidator != null;
	}

	public void removeFieldListener( XFormFieldListener listener )
	{
		if( listeners != null )
			listeners.remove( listener );
	}

	public void removeFormFieldValidator( XFormFieldValidator validator )
	{
		if( validators != null )
			validators.remove( validator );
	}

	public void setRequired( boolean required, String message )
	{
		if( requiredValidator != null )
			removeFormFieldValidator( requiredValidator );

		if( required )
		{
			requiredValidator = new RequiredValidator( message );
			addFormFieldValidator( requiredValidator );
		}
	}

	public ValidationMessage[] validate()
	{
		if( validators == null || validators.isEmpty() )
			return null;

		ArrayList<ValidationMessage> messages = new ArrayList<ValidationMessage>();

		for( XFormFieldValidator validator : validators )
		{
			ValidationMessage[] validateField = validator.validateField( this );
			if( validateField != null && validateField.length > 0 )
				messages.addAll( Arrays.asList( validateField ) );
		}

		return messages.toArray( new ValidationMessage[messages.size()] );
	}

	protected void fireValueChanged( String newValue, String oldValue )
	{
		if( listeners == null )
			return;

		for( XFormFieldListener listener : listeners )
		{
			listener.valueChanged( this, newValue, oldValue );
		}
	}

	public Object getProperty( String name )
	{
		return null;
	}

	public abstract void setProperty( String name, Object value );

	public boolean isMultiRow()
	{
		return false;
	}
}
