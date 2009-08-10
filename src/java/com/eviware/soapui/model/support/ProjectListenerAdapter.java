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

package com.eviware.soapui.model.support;

import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.model.mock.MockService;
import com.eviware.soapui.model.project.Project;
import com.eviware.soapui.model.project.ProjectListener;
import com.eviware.soapui.model.testsuite.TestSuite;

/**
 * Adapter for ProjectListener implementations
 * 
 * @author Ole.Matzura
 */

public class ProjectListenerAdapter implements ProjectListener
{
	public void interfaceAdded( Interface iface )
	{
	}

	public void interfaceRemoved( Interface iface )
	{
	}

	public void testSuiteAdded( TestSuite testSuite )
	{
	}

	public void testSuiteRemoved( TestSuite testSuite )
	{
	}

	public void testSuiteMoved( TestSuite testSuite, int index, int offset )
	{

	}

	public void mockServiceAdded( MockService mockService )
	{
	}

	public void mockServiceRemoved( MockService mockService )
	{
	}

	public void interfaceUpdated( Interface iface )
	{
	}

	public void afterLoad( Project project )
	{
	}

	public void beforeSave( Project project )
	{
	}
}
