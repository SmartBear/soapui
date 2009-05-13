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

import javax.swing.Action;

import com.eviware.soapui.support.types.StringToStringMap;

public interface XFormDialog
{
	public final static int OK_OPTION = 1;
	public final static int CANCEL_OPTION = 2;

	public void setValues( StringToStringMap values );

	public StringToStringMap getValues();

	public void setVisible( boolean visible );

	public int getReturnValue();

	public void setValue( String field, String value );

	public String getValue( String field );

	public boolean show();

	public StringToStringMap show( StringToStringMap values );

	public boolean validate();

	public void setOptions( String field, Object[] options );

	public XFormField getFormField( String name );

	public void setFormFieldProperty( String name, Object value );

	public int getValueIndex( String name );

	public int getIntValue( String name, int defaultValue );

	public boolean getBooleanValue( String name );

	public void setBooleanValue( String name, boolean b );

	public void setIntValue( String name, int value );

	public void setWidth( int i );

	public void release();

	public void addAction( Action action );
}
