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

package com.eviware.soapui.model;

import java.util.List;
import java.util.Map;

import com.eviware.soapui.model.testsuite.TestProperty;
import com.eviware.soapui.model.testsuite.TestPropertyListener;

public interface TestPropertyHolder
{
	public String[] getPropertyNames();

	public void setPropertyValue( String name, String value );

	public String getPropertyValue( String name );

	public TestProperty getProperty( String name );

	public Map<String, TestProperty> getProperties();

	public void addTestPropertyListener( TestPropertyListener listener );

	public void removeTestPropertyListener( TestPropertyListener listener );

	public boolean hasProperty( String name );

	public ModelItem getModelItem();

	public int getPropertyCount();

	public List<TestProperty> getPropertyList();

	public TestProperty getPropertyAt( int index );

	public String getPropertiesLabel();
}
