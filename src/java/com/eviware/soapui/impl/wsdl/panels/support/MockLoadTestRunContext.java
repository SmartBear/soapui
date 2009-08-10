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

package com.eviware.soapui.impl.wsdl.panels.support;

import com.eviware.soapui.impl.wsdl.loadtest.WsdlLoadTest;
import com.eviware.soapui.model.support.AbstractSubmitContext;
import com.eviware.soapui.model.testsuite.LoadTestRunContext;
import com.eviware.soapui.model.testsuite.LoadTestRunner;
import com.eviware.soapui.model.testsuite.TestCaseRunner;

public class MockLoadTestRunContext extends AbstractSubmitContext<WsdlLoadTest> implements LoadTestRunContext
{
	private final MockLoadTestRunner mockTestRunner;

	public MockLoadTestRunContext( MockLoadTestRunner mockTestRunner )
	{
		super( mockTestRunner.getLoadTest() );
		this.mockTestRunner = mockTestRunner;
	}

	public LoadTestRunner getLoadTestRunner()
	{
		return mockTestRunner;
	}

	public Object getProperty( String name )
	{
		if( "loadTestRunner".equals( name ) )
			return getLoadTestRunner();

		return get( name );
	}

	public Object getProperty( String testStep, String propertyName )
	{
		// TODO Auto-generated method stub
		return null;
	}

	public TestCaseRunner getTestRunner()
	{
		// TODO Auto-generated method stub
		return null;
	}
}
