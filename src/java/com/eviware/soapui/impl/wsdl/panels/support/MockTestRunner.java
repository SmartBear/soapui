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

import org.apache.log4j.Logger;

import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.model.testsuite.TestCaseRunContext;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.TestStepResult;

/**
 * Dummy TestRunner used when executing TestSteps one by one
 * 
 * @author ole.matzura
 */

public class MockTestRunner extends AbstractMockTestRunner<WsdlTestCase> implements TestCaseRunner
{
	private MockTestRunContext mockRunContext;

	public MockTestRunner( WsdlTestCase testCase )
	{
		this( testCase, null );
	}

	public MockTestRunner( WsdlTestCase testCase, Logger logger )
	{
		super( testCase, logger );
	}

	public WsdlTestCase getTestCase()
	{
		return getTestRunnable();
	}

	public List<TestStepResult> getResults()
	{
		return new ArrayList<TestStepResult>();
	}

	public TestCaseRunContext getRunContext()
	{
		return mockRunContext;
	}
	
	public TestStepResult runTestStep( TestStep testStep )
	{
		return testStep.run( this, mockRunContext );
	}

	public TestStepResult runTestStepByName( String name )
	{
		return getTestCase().getTestStepByName( name ).run( this, mockRunContext );
	}

	public void gotoStep( int index )
	{
		getLog().info( "Going to step " + index + " [" + getTestCase().getTestStepAt( index ).getName() + "]" );
	}

	public void gotoStepByName( String stepName )
	{
		getLog().info( "Going to step [" + stepName + "]" );
	}

	public void setMockRunContext( MockTestRunContext mockRunContext )
	{
		this.mockRunContext = mockRunContext;
	}
}