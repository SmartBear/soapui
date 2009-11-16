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

import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.support.types.StringToStringMap;

public interface XForm
{
	public enum FieldType
	{
		TEXT, FOLDER, FILE, FILE_OR_FOLDER, URL, JAVA_PACKAGE, JAVA_CLASS, PASSWORD, PROJECT_FILE, PROJECT_FOLDER, TEXTAREA
	}

	public XFormTextField addTextField( String name, String description, FieldType type );

	public XFormField addCheckBox( String name, String description );

	public XFormOptionsField addComboBox( String name, Object[] values, String description );

	public void setOptions( String name, Object[] values );

	public void addSeparator( String label );

	public XFormField addComponent( String name, XFormField component );

	public StringToStringMap getValues();

	public void setValues( StringToStringMap values );

	public String getComponentValue( String name );

	public XFormField getComponent( String name );

	public enum ToolkitType
	{
		SWING, SWT
	}

	public String getName();

	public void setName( String name );

	public XFormField addNameSpaceTable( String label, Interface modelItem );

	public void addLabel( String name, String label );

	public XFormField[] getFormFields();

	public void setFormFieldProperty( String name, Object value );

	public void addSeparator();

	public Object[] getOptions( String name );

	public XFormField getFormField( String name );
}
