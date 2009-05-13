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

package com.eviware.soapui.support.components;

import java.util.Map;

import com.eviware.soapui.support.action.swing.ActionList;

/**
 * Behavior of a configuration dialog
 * 
 * @author Ole.Matzura
 */

public interface ConfigurationDialog
{
	public boolean show( Map<String, String> values );

	public void hide();

	public void addTextField( String name, String tooltip );

	public void addTextField( String name, String tooltip, FieldType type );

	public void addCheckBox( String caption, String label, boolean selected );

	public void addComboBox( String label, Object[] objects, String tooltip );

	public void setValues( String id, String[] values );

	public void addComboBox( String label, String tooltip );

	public ActionList getActions();

	public void getValues( Map<String, String> values );

	public enum FieldType
	{
		TEXT, DIRECTORY, FILE, URL, JAVA_PACKAGE, JAVA_CLASS
	}
}