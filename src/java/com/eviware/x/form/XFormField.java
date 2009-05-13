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

public interface XFormField
{
	public final static String CURRENT_DIRECTORY = XFormField.class.getName() + "@currentDirectory";

	public void setValue( String value );

	public String getValue();

	public void setEnabled( boolean enabled );

	public boolean isEnabled();

	public void setRequired( boolean required, String message );

	public boolean isRequired();

	public void setToolTip( String tooltip );

	public void addFormFieldListener( XFormFieldListener listener );

	public void removeFieldListener( XFormFieldListener listener );

	public void addFormFieldValidator( XFormFieldValidator validator );

	public void removeFormFieldValidator( XFormFieldValidator validator );

	public void addComponentEnabler( XFormField tf, String value );

	public void setProperty( String name, Object value );

	public Object getProperty( String name );

	public ValidationMessage[] validate();
}
