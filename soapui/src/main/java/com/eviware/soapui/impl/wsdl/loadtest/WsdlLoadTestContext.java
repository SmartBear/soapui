/*
 *  SoapUI, copyright (C) 2004-2012 smartbear.com
 *
 *  SoapUI is free software; you can redistribute it and/or modify it under the
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  SoapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.wsdl.loadtest;

import com.eviware.soapui.model.propertyexpansion.DefaultPropertyExpansionContext;
import com.eviware.soapui.model.testsuite.LoadTestRunContext;
import com.eviware.soapui.model.testsuite.LoadTestRunner;
import com.eviware.soapui.model.testsuite.TestCaseRunner;

/**
 * LoadTestRunContext implementation for WsdlLoadTests
 * 
 * @author Ole.Matzura
 */

public class WsdlLoadTestContext extends DefaultPropertyExpansionContext implements LoadTestRunContext
{
	private final LoadTestRunner runner;

	public WsdlLoadTestContext( LoadTestRunner runner )
	{
		super( runner.getLoadTest().getTestCase() );
		this.runner = runner;
	}

	public LoadTestRunner getLoadTestRunner()
	{
		return runner;
	}

	@Override
	public Object get( Object key )
	{
		if( "loadTestRunner".equals( key ) )
			return runner;

		return super.get( key );
	}

	public Object getProperty( String testStep, String propertyName )
	{
		return null;
	}

	public TestCaseRunner getTestRunner()
	{
		return null;
	}
}
