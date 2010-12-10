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

import org.apache.commons.httpclient.HttpState;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.TestStepConfig;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCaseRunner;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestRunContext;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.registry.WsdlTestStepFactory;
import com.eviware.soapui.impl.wsdl.teststeps.registry.WsdlTestStepRegistry;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.model.testsuite.TestAssertion;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.TestRunListener;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.TestStepResult;
import com.eviware.soapui.model.testsuite.TestRunner.Status;
import com.eviware.soapui.security.check.SecurityCheck;
import com.eviware.soapui.security.log.SecurityTestLogMessageEntry;
import com.eviware.soapui.security.support.SecurityTestRunListener;
import com.eviware.soapui.support.types.StringToObjectMap;

public class SecurityTestRunnerImpl extends WsdlTestCaseRunner implements SecurityTestRunner
{

	private SecurityTest securityTest;
	private long startTime = 0;
//	private WsdlTestRunContext context;
//	private boolean stopped;
	private boolean hasTornDown;
	private String reason;
	private SecurityTestRunListener[] listeners = new SecurityTestRunListener[0];
//	private TestRunListener[] testCaseRunListeners = new TestRunListener[0];
	private int initCount;
	private int startStep = 0;
	private int gotoStepIndex;

	public SecurityTestRunnerImpl( SecurityTest test )
	{
		super( test.getTestCase(), new StringToObjectMap() );
		this.securityTest = test;
		setStatus(Status.INITIALIZED);
	}

	public SecurityTest getSecurityTest()
	{
		return securityTest;
	}

//	public synchronized void cancel( String reason )
//	{
//		if( getStatus() != Status.RUNNING )
//			return;
//
//		this.reason = reason;
//		setStatus(Status.CANCELED);
//
//		String msg = "SecurityTest [" + securityTest.getName() + "] canceled";
//		if( reason != null )
//			msg += "; " + reason;
//
//		securityTest.getSecurityTestLog().addEntry( new SecurityTestLogMessageEntry( msg ) );
//
//		setStatus(Status.CANCELED);
//
//		stop();
//	}

	@Override
	public void fail( String reason )
	{

		setStatus(Status.FAILED);

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

//	@Override
//	public Status getStatus()
//	{
//		return status;
//	}

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

	// public void start()
	public void internalRun( WsdlTestRunContext runContext ) throws Exception
	{
		securityTest.getTestCase().beforeSave();
		listeners = securityTest.getTestRunListeners();
		hasTornDown = false;

		runContext = new WsdlTestRunContext( this, new StringToObjectMap() );

		try
		{
			securityTest.runStartupScript( runContext, this );
		}
		catch( Exception e1 )
		{
			SoapUI.logError( e1 );
		}

//		status = Status.RUNNING;
//
//		if( status == Status.RUNNING )
//		{
			WsdlTestCase testCase = securityTest.getTestCase();
			// List<TestStep> testStepsList = testCase.getTestStepList();
			HashMap<String, List<SecurityCheck>> secCheckMap = securityTest.getSecurityChecksMap();
			// SecurityTestRunnerImpl testCaseRunner = new SecurityTestRunnerImpl(
			// securityTest );

			notifyBeforeRun();

			// copied from internal run

			gotoStepIndex = -1;
			// testStepResults.clear();

			// create state for testcase if specified
			if( testCase.getKeepSession() )
			{
				runContext.setProperty( SubmitContext.HTTP_STATE_PROPERTY, new HttpState() );
			}

//			testCaseRunListeners = testCase.getTestRunListeners();
			testCase.runSetupScript( runContext, this );
			if( !super.isRunning() )
				return;

			if( testCase.getTimeout() > 0 )
			{
				startTimeoutTimer( testCase.getTimeout() );
			}
			for( ; initCount < testCase.getTestStepCount() && isRunning(); initCount++ )
			{
				WsdlTestStep testStep = testCase.getTestStepAt( initCount );
				if( testStep.isDisabled() )
					continue;

				try
				{
					testStep.prepare( this, runContext );
				}
				catch( Exception e )
				{
					setStatus( Status.FAILED );
					SoapUI.logError( e );
					throw new Exception( "Failed to prepare testStep [" + testStep.getName() + "]; " + e.toString() );
				}
			}

			int currentStepIndex = startStep;
			runContext.setCurrentStep( currentStepIndex );

			for( ; isRunning() && currentStepIndex < testCase.getTestStepCount(); currentStepIndex++ )
			{
				TestStep currentStep = runContext.getCurrentStep();
				if( !currentStep.isDisabled() )
				{
					for( int i = 0; i < listeners.length; i++ )
					{
						listeners[i].beforeStep( this, getRunContext(), currentStep );
						if( !isRunning() )
							return;
					}
					TestStepResult stepResult = runTestStep( currentStep, true, true );
					if( stepResult == null )
						return;

					if( !isRunning() )
						return;

					if( secCheckMap.containsKey( currentStep.getId() ) )
					{
						List<SecurityCheck> testStepChecksList = secCheckMap.get( currentStep.getId() );
						for( SecurityCheck securityCheck : testStepChecksList )
						{

							if( securityCheck.acceptsTestStep( currentStep ) )
							{
								securityCheck.run( cloneForSecurityCheck( ( WsdlTestStep )currentStep ), runContext, securityTest
										.getSecurityTestLog() );
							}
						}
					}
					for( int i = 0; i < listeners.length; i++ )
					{
						listeners[i].afterStep( this, getRunContext(), stepResult );
					}
					if( gotoStepIndex != -1 )
					{
						currentStepIndex = gotoStepIndex - 1;
						gotoStepIndex = -1;
					}
				}

				runContext.setCurrentStep( currentStepIndex + 1 );
			}

			// testCase.release();
//		}
//		stop();
	}
	
	protected void internalFinally( WsdlTestRunContext runContext )
	{
		WsdlTestCase testCase = securityTest.getTestCase();

		for( int c = 0; c < initCount && c < testCase.getTestStepCount(); c++ )
		{
			WsdlTestStep testStep = testCase.getTestStepAt( c );
			if( !testStep.isDisabled() )
				testStep.finish( this, runContext );
		}

		try
		{
			securityTest.runTearDownScript( runContext, this );
		}
		catch( Exception e )
		{
			SoapUI.logError( e );
		}

		notifyAfterRun();

		runContext.clear();
		listeners = null;
	}



	public void release()
	{

	}

//	private synchronized void stop()
//	{
//		if( stopped )
//			return;
//
//		if( getStatus() == Status.RUNNING )
//			setStatus(Status.FINISHED);
//
//		securityTest.getSecurityTestLog().addEntry(
//				new SecurityTestLogMessageEntry( "SecurityTest ended at " + new Date( System.currentTimeMillis() ) ) );
//
//		try
//		{
//			tearDown();
//		}
//		catch( Throwable e )
//		{
//			SoapUI.logError( e );
//		}
//
////		context.clear();
//		stopped = true;
//	}

//	public boolean hasStopped()
//	{
//		return stopped;
//	}

//	private synchronized void tearDown()
//	{
//		if( hasTornDown )
//			return;
//
//		try
//		{
//			securityTest.runTearDownScript( context, this );
//		}
//		catch( Exception e1 )
//		{
//			SoapUI.logError( e1 );
//		}
//
//		hasTornDown = true;
//	}

	protected void notifyBeforeRun()
	{
		if( listeners == null || listeners.length == 0 )
			return;

		for( int i = 0; i < listeners.length; i++ )
		{
			try
			{
				listeners[i].beforeRun( this, getRunContext() );
			}
			catch( Throwable t )
			{
				SoapUI.logError( t );
			}
		}
	}

	protected void notifyAfterRun()
	{
		if( listeners == null || listeners.length == 0 )
			return;

		for( int i = 0; i < listeners.length; i++ )
		{
			try
			{
				listeners[i].afterRun( this, getRunContext() );
			}
			catch( Throwable t )
			{
				SoapUI.logError( t );
			}
		}
	}

}
