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

import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.model.support.AbstractSubmitContext;
import com.eviware.soapui.model.testsuite.TestCase;
import com.eviware.soapui.model.testsuite.TestRunner;
import com.eviware.soapui.model.testsuite.TestSuite;
import com.eviware.soapui.model.testsuite.TestSuiteRunContext;
import com.eviware.soapui.model.testsuite.TestSuiteRunner;

public class MockTestSuiteRunContext extends AbstractSubmitContext<WsdlTestSuite> implements TestSuiteRunContext
{
	private final MockTestSuiteRunner mockTestSuiteRunner;

	public MockTestSuiteRunContext( MockTestSuiteRunner mockTestSuiteRunner )
	{
		super( mockTestSuiteRunner.getTestRunnable() );
		this.mockTestSuiteRunner = mockTestSuiteRunner;
	}

	public TestCase getCurrentTestCase()
	{
		return null;
	}

	public int getCurrentTestCaseIndex()
	{
		return -1;
	}

	public TestSuite getTestSuite()
	{
		return getModelItem();
	}

	public TestSuiteRunner getTestSuiteRunner()
	{
		return mockTestSuiteRunner;
	}

	public TestRunner getTestRunner()
	{
		return mockTestSuiteRunner;
	}

	public Object getProperty( String name )
	{
		return getProperties().get( name );
	}

}
