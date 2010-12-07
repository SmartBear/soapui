/*
 *  soapUI, copyright (C) 2004-2010 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */
/**
 * 
 * 
 * @author soapUI team
 */

package com.eviware.soapui.security;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.TestStepConfig;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCaseRunner;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestRunContext;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.registry.WsdlTestStepFactory;
import com.eviware.soapui.impl.wsdl.teststeps.registry.WsdlTestStepRegistry;
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.model.testsuite.TestAssertion;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.security.check.SecurityCheck;
import com.eviware.soapui.security.log.SecurityTestLogMessageEntry;
import com.eviware.soapui.support.types.StringToObjectMap;

public class SecurityTestRunnerImpl extends WsdlTestCaseRunner implements SecurityTestRunner
{

	private SecurityTest securityTest;
	private long startTime = 0;
	private Status status;
	private WsdlTestRunContext context;
	private boolean stopped;
	private boolean hasTornDown;
	private String reason;

	public SecurityTestRunnerImpl( SecurityTest test )
	{
		super( test.getTestCase(), new StringToObjectMap() );
		this.securityTest = test;
		status = Status.INITIALIZED;
	}

	@Override
	public float getProgress()
	{
		return 0;
	}

	public SecurityTest getSecurityTest()
	{
		return securityTest;
	}

	public synchronized void cancel( String reason )
	{
		if( status != Status.RUNNING )
			return;

		this.reason = reason;
		status = Status.CANCELED;

		String msg = "SecurityTest [" + securityTest.getName() + "] canceled";
		if( reason != null )
			msg += "; " + reason;

		securityTest.getSecurityTestLog().addEntry( new SecurityTestLogMessageEntry( msg ) );

		status = Status.CANCELED;

		stop();
	}

	@Override
	public void fail( String reason )
	{

		status = Status.FAILED;

	}

	@Override
	public String getReason()
	{
		return reason;
	}

	// @Override
	// public TestRunContext getRunContext()
	// {
	// return context;
	// }

	@Override
	public long getStartTime()
	{
		return startTime;
	}

	@Override
	public Status getStatus()
	{
		return status;
	}

	// @Override
	// public TestRunnable getTestRunnable()
	// {
	// return securityTest;
	// }

	@Override
	public long getTimeTaken()
	{
		return System.currentTimeMillis() - startTime;
	}

	@Override
	public Status waitUntilFinished()
	{
		return null;
	}

	/**
	 * Clones original TestStep for security modification this does not alter the
	 * original test step
	 * 
	 * @param sourceTestStep
	 * @return TestStep
	 */
	private TestStep cloneForSecurityCheck( WsdlTestStep sourceTestStep )
	{
		WsdlTestStep clonedTestStep = null;
		TestStepConfig testStepConfig = ( TestStepConfig )sourceTestStep.getConfig().copy();
		WsdlTestStepFactory factory = WsdlTestStepRegistry.getInstance().getFactory( testStepConfig.getType() );
		if( factory != null )
		{
			clonedTestStep = factory.buildTestStep( securityTest.getTestCase(), testStepConfig, false );
			if( clonedTestStep instanceof Assertable )
			{
				for( TestAssertion assertion : ( ( Assertable )clonedTestStep ).getAssertionList() )
				{
					( ( Assertable )clonedTestStep ).removeAssertion( assertion );
				}
			}
		}
		return clonedTestStep;
	}

	public void start()
	{
		securityTest.getTestCase().beforeSave();
		hasTornDown = false;

		context = new WsdlTestRunContext( this, new StringToObjectMap() );

		try
		{
			securityTest.runStartupScript( context, this );
		}
		catch( Exception e1 )
		{
			SoapUI.logError( e1 );
		}

		status = Status.RUNNING;

		if( status == Status.RUNNING )
		{
			WsdlTestCase testCase = securityTest.getTestCase();
			List<TestStep> testStepsList = testCase.getTestStepList();
			HashMap<String, List<SecurityCheck>> secCheckMap = securityTest.getSecurityChecksMap();
			WsdlTestCaseRunner testCaseRunner = new WsdlTestCaseRunner( testCase, new StringToObjectMap() );
			for( TestStep testStep : testStepsList )
			{
				if( !testStep.isDisabled() )
				{
					if( secCheckMap.containsKey( testStep.getName() ) )
					{
						List<SecurityCheck> testStepChecksList = secCheckMap.get( testStep.getName() );
						for( SecurityCheck securityCheck : testStepChecksList )
						{
							if( securityCheck.acceptsTestStep( testStep ) )
								securityCheck.run( cloneForSecurityCheck( ( WsdlTestStep )testStep ), context, securityTest
										.getSecurityTestLog() );
						}
					}
					else
					{
						testCaseRunner.runTestStepByName( testStep.getName() );
					}
				}
			}
//			testCase.release();
		}
		stop();
	}

	@Override
	public void start( boolean async )
	{
		start();
	}

	public void release()
	{

	}

	private synchronized void stop()
	{
		if( stopped )
			return;

		if( status == Status.RUNNING )
			status = Status.FINISHED;

		securityTest.getSecurityTestLog().addEntry(
				new SecurityTestLogMessageEntry( "SecurityTest ended at " + new Date( System.currentTimeMillis() ) ) );

		try
		{
			tearDown();
		}
		catch( Throwable e )
		{
			SoapUI.logError( e );
		}

		context.clear();
		stopped = true;
	}

	public boolean hasStopped()
	{
		return stopped;
	}

	private synchronized void tearDown()
	{
		if( hasTornDown )
			return;

		try
		{
			securityTest.runTearDownScript( context, this );
		}
		catch( Exception e1 )
		{
			SoapUI.logError( e1 );
		}

		hasTornDown = true;
	}
}
