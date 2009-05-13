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

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.model.testsuite.TestCase;
import com.eviware.soapui.model.testsuite.TestRunContext;
import com.eviware.soapui.model.testsuite.TestRunner;
import com.eviware.soapui.model.testsuite.TestStepResult;

/**
 * Dummy TestRunner used when executing TestSteps one by one
 * 
 * @author ole.matzura
 */

public class MockTestRunner implements TestRunner
{
	private long startTime;
	private String reason;
	private final WsdlTestCase testCase;
	private final Logger logger;
	private Status status = Status.RUNNING;
	private MockTestRunContext mockRunContext;

	public MockTestRunner( WsdlTestCase testCase )
	{
		this( testCase, null );
	}

	public MockTestRunner( WsdlTestCase testCase, Logger logger )
	{
		this.testCase = testCase;
		this.logger = logger == null ? SoapUI.ensureGroovyLog() : logger;
		startTime = System.currentTimeMillis();
	}

	public Logger getLog()
	{
		return logger;
	}

	public TestCase getTestCase()
	{
		return testCase;
	}

	public List<TestStepResult> getResults()
	{
		return new ArrayList<TestStepResult>();
	}

	public Status getStatus()
	{
		return status;
	}

	public void start( boolean async )
	{

	}

	public TestRunContext getRunContext()
	{
		return mockRunContext;
	}

	public TestStepResult runTestStepByName( String name )
	{
		return testCase.getTestStepByName( name ).run( this, mockRunContext );
	}

	public long getTimeTaken()
	{
		return System.currentTimeMillis() - startTime;
	}

	public Status waitUntilFinished()
	{
		status = Status.FINISHED;
		return status;
	}

	public void cancel( String reason )
	{
		this.reason = reason;
		status = Status.CANCELED;
		logger.info( "Canceled with reason [" + reason + "]" );
	}

	public void gotoStep( int index )
	{
		logger.info( "Going to step " + index + " [" + testCase.getTestStepAt( index ).getName() + "]" );
	}

	public void gotoStepByName( String stepName )
	{
		logger.info( "Going to step [" + stepName + "]" );
	}

	public void fail( String reason )
	{
		this.reason = reason;
		status = Status.FAILED;
		logger.error( "Failed with reason [" + reason + "]" );
	}

	public long getStartTime()
	{
		return startTime;
	}

	public String getReason()
	{
		return reason;
	}

	public void setMockRunContext( MockTestRunContext mockRunContext )
	{
		this.mockRunContext = mockRunContext;
	}
}