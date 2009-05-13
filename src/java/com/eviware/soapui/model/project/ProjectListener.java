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

package com.eviware.soapui.model.project;

import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.model.mock.MockService;
import com.eviware.soapui.model.testsuite.TestSuite;

/**
 * Listener for Project-related events
 * 
 * @author Ole.Matzura
 */

public interface ProjectListener
{
	void interfaceAdded( Interface iface );

	void interfaceRemoved( Interface iface );

	void interfaceUpdated( Interface iface );

	void testSuiteAdded( TestSuite testSuite );

	void testSuiteRemoved( TestSuite testSuite );

	void mockServiceAdded( MockService mockService );

	void mockServiceRemoved( MockService mockService );

	void afterLoad( Project project );

	void beforeSave( Project project );
}
