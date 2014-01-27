/*
 * soapUI, copyright (C) 2004-2014 smartbear.com
 *
 * soapUI is free software; you can redistribute it and/or modify it under the
 * terms of version 2.1 of the GNU Lesser General Public License as published by
 * the Free Software Foundation.
 *
 * soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details at gnu.org.
 *
 */

package com.eviware.soapui.model.support;

import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.model.iface.Operation;
import com.eviware.soapui.model.iface.Request;
import com.eviware.soapui.model.mock.MockOperation;
import com.eviware.soapui.model.mock.MockResponse;
import com.eviware.soapui.model.mock.MockService;
import com.eviware.soapui.model.project.Project;
import com.eviware.soapui.model.testsuite.LoadTest;
import com.eviware.soapui.model.testsuite.TestCase;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.TestSuite;

public class ModelItemUtils
{
	public static Project getProjectFromModelItem( ModelItem modelItem )
	{
		if( modelItem == null )
		{
			return null;
		}
		if( modelItem instanceof Project )
		{
			return ( Project )modelItem;
		}
		else if( modelItem instanceof Interface )
		{
			return ( ( Interface )modelItem ).getProject();
		}
		else if( modelItem instanceof Operation )
		{
			return ( ( Operation )modelItem ).getInterface().getProject();
		}
		else if( modelItem instanceof Request )
		{
			return ( ( Request )modelItem ).getOperation().getInterface().getProject();
		}
		else if( modelItem instanceof TestSuite )
		{
			return ( ( TestSuite )modelItem ).getProject();
		}
		else if( modelItem instanceof TestCase )
		{
			return ( ( TestCase )modelItem ).getTestSuite().getProject();
		}
		else if( modelItem instanceof TestStep )
		{
			return ( ( TestStep )modelItem ).getTestCase().getTestSuite().getProject();
		}
		else if( modelItem instanceof LoadTest )
		{
			return ( ( LoadTest )modelItem ).getTestCase().getTestSuite().getProject();
		}
		else if( modelItem instanceof MockService )
		{
			return ( ( MockService )modelItem ).getProject();
		}
		else if( modelItem instanceof MockOperation )
		{
			return ( ( MockOperation )modelItem ).getMockService().getProject();
		}
		else if( modelItem instanceof MockResponse )
		{
			return ( ( MockResponse )modelItem ).getMockOperation().getMockService().getProject();
		}
		else
		{
			return null;
		}
	}
}
