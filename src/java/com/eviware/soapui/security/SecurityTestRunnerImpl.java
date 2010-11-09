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

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.model.testsuite.TestRunContext;
import com.eviware.soapui.model.testsuite.TestRunnable;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.security.log.SecurityTestLogMessageEntry;

public class SecurityTestRunnerImpl implements SecurityTestRunner
{

	private SecurityTest securityTest;
	private long startTime = 0;
	private Status status;
	private SecurityTestContext context;
	private boolean stopped;
	private boolean hasTornDown;

	public SecurityTestRunnerImpl( SecurityTest test )
	{
		this.securityTest = test;
		status = Status.INITIALIZED;
	}

	@Override
	public float getProgress()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	public SecurityTest getSecurityTest()
	{
		return securityTest;
	}

	@Override
	public void cancel( String reason )
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void fail( String reason )
	{
		// TODO Auto-generated method stub

	}

	@Override
	public String getReason()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TestRunContext getRunContext()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getStartTime()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Status getStatus()
	{
		return status;
	}

	@Override
	public TestRunnable getTestRunnable()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getTimeTaken()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Status waitUntilFinished()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Clones original TestStep for security modification this does not alter the
	 * original test step
	 * 
	 * @param sourceTestStep
	 * @return TestStep
	 */
	private TestStep cloneForSecurityCheck( TestStep sourceTestStep )
	{
		TestStep clonedTestStep = null;
		return clonedTestStep;
	}

	void start()
	{
		securityTest.getTestCase().beforeSave();

		context = new SecurityTestContext( this );

		try
		{
			securityTest.runStartupScript( context, this );
		}
		catch( Exception e1 )
		{
			SoapUI.logError( e1 );
		}

		// for( LoadTestRunListener listener : loadTest.getLoadTestRunListeners()
		// )
		// {
		// try
		// {
		// listener.beforeLoadTest( this, context );
		// }
		// catch( Throwable e )
		// {
		// SoapUI.logError( e );
		// }
		// }

		status = Status.RUNNING;

		// loadTest.addPropertyChangeListener( WsdlLoadTest.THREADCOUNT_PROPERTY,
		// internalPropertyChangeListener );

		// XProgressDialog progressDialog =
		// UISupport.getDialogs().createProgressDialog( "Starting threads",
		// ( int )loadTest.getThreadCount(), "", true );
		// try
		// {
		// testCaseStarter = new TestCaseStarter();
		// progressDialog.run( testCaseStarter );
		// }
		// catch( Exception e )
		// {
		// SoapUI.logError( e );
		// }

		// if( status == Status.RUNNING )
		// {
		// for( LoadTestRunListener listener : loadTest.getLoadTestRunListeners()
		// )
		// {
		// listener.loadTestStarted( this, context );
		// }
		//
		// startStrategyThread();
		// }
		// else
		// {
		// stop();
		// }
	}

	@Override
	public void start( boolean async )
	{
		start();
	}

	public void release()
	{
		// loadTest.removePropertyChangeListener(
		// WsdlLoadTest.THREADCOUNT_PROPERTY, internalPropertyChangeListener );
	}

	private synchronized void stop()
	{
		if( stopped )
			return;

		// securityTest.removePropertyChangeListener(
		// WsdlLoadTest.THREADCOUNT_PROPERTY, internalPropertyChangeListener );

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

		// for( LoadTestRunListener listener :
		// securityTest.getLoadTestRunListeners() )
		// {
		// try
		// {
		// // listener.afterLoadTest( this, context );
		// }
		// catch( Throwable e )
		// {
		// SoapUI.logError( e );
		// }
		// }

		context.clear();
		stopped = true;
		// blueprintConfig = null;
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
