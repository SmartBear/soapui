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

import java.util.ArrayList;
import java.util.List;

import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.TestSuite;
import com.eviware.soapui.model.testsuite.TestSuiteRunner;

public class MockTestSuiteRunner extends AbstractMockTestRunner<WsdlTestSuite> implements TestSuiteRunner
{
	public MockTestSuiteRunner( WsdlTestSuite testSuite )
	{
		super( testSuite, null );
		setRunContext( new MockTestSuiteRunContext(this) );
	}

	public List<TestCaseRunner> getResults()
	{
		return new ArrayList<TestCaseRunner>();
	}

	public TestSuite getTestSuite()
	{
		return getTestRunnable();
	}
}
