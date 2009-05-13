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

package com.eviware.soapui.model.testsuite;

public interface TestPropertyListener
{
	public void propertyAdded( String name );

	public void propertyRemoved( String name );

	public void propertyRenamed( String oldName, String newName );

	public void propertyValueChanged( String name, String oldValue, String newValue );

	public void propertyMoved( String name, int oldIndex, int newIndex );
}
