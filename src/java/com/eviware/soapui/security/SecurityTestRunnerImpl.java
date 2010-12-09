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
	private Status status;
	private WsdlTestRunContext context;
	private boolean stopped;
	private boolean hasTornDown;
	private String reason;
	private SecurityTestRunListener[] listeners = new SecurityTestRunListener[0];
	private TestRunListener[] testCaseRunListeners = new TestRunListener[0];
	private int initCount;
	private int startStep = 0;
	private int gotoStepIndex;


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
		listeners = securityTest.getTestRunListeners();
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
			SecurityTestRunnerImpl testCaseRunner = new SecurityTestRunnerImpl( securityTest );

			notifyBeforeRun();
			
//			//copied from internal run
//
//			for( ; initCount < testCase.getTestStepCount() && isRunning(); initCount++ )
//			{
//				WsdlTestStep testStep = testCase.getTestStepAt( initCount );
//				if( testStep.isDisabled() )
//					continue;
//
//				try
//				{
//					testStep.prepare( this, context );
//				}
//				catch( Exception e )
//				{
//					setStatus( Status.FAILED );
//					SoapUI.logError( e );
////					throw new Exception( "Failed to prepare testStep [" + testStep.getName() + "]; " + e.toString() );
//				}
//			}
//
//			int currentStepIndex = startStep;
//			context.setCurrentStep( currentStepIndex );
//
//			for( ; isRunning() && currentStepIndex < testCase.getTestStepCount(); currentStepIndex++ )
//			{
//				TestStep currentStep = context.getCurrentStep();
//				if( !currentStep.isDisabled() )
//				{
//					TestStepResult stepResult = runTestStep( currentStep, true, true );
//					if( stepResult == null )
//						return;
//
//					if( !isRunning() )
//						return;
//
////					if( gotoStepIndex != -1 )
////					{
////						currentStepIndex = gotoStepIndex - 1;
////						gotoStepIndex = -1;
////					}
//				}
//
//				context.setCurrentStep( currentStepIndex + 1 );
//			}
//
//			
//			
//			//end copied from internal run
//			
			
			for( int j = 0; j < testStepsList.size(); j++ )
			{
				TestStep testStep = testStepsList.get( j );

				if( !testStep.isDisabled() )
				{
					for( int i = 0; i < listeners.length; i++ )
					{
						listeners[i].beforeStep( this, getRunContext(), testStep );
						if( !isRunning() )
							return;
					}
					TestStepResult stepResult = testCaseRunner.runTestStepByName( testStep.getName() );

					if( secCheckMap.containsKey( testStep.getId() ) )
					{
						List<SecurityCheck> testStepChecksList = secCheckMap.get( testStep.getId() );
						for( SecurityCheck securityCheck : testStepChecksList )
						{

							if( securityCheck.acceptsTestStep( testStep ) )
							{
								securityCheck.run( cloneForSecurityCheck( ( WsdlTestStep )testStep ), context, securityTest
										.getSecurityTestLog() );
							}
						}
					}
					for( int i = 0; i < listeners.length; i++ )
					{
						listeners[i].afterStep( this, getRunContext(), stepResult );
					}

				}
			}
			if( status == Status.RUNNING )
			{
				status = Status.FINISHED;
			}
			notifyAfterRun();

			// testCase.release();
		}
		stop();
	}

//	@Override
//	public void start( boolean async )
//	{
//		start();
//	}

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
	public void internalRun( WsdlTestRunContext runContext ) throws Exception
	{
		WsdlTestCase testCase = getTestRunnable();
//		gotoStepIndex = -1;
//		testStepResults.clear();

		// create state for testcase if specified
		if( testCase.getKeepSession() )
		{
			runContext.setProperty( SubmitContext.HTTP_STATE_PROPERTY, new HttpState() );
		}

		testCaseRunListeners = testCase.getTestRunListeners();
		testCase.runSetupScript( runContext, this );
		if( !super.isRunning() )
			return;

		if( testCase.getTimeout() > 0 )
		{
			startTimeoutTimer( testCase.getTimeout() );
		}

		super.notifyBeforeRun();
		if( !super.isRunning() )
			return;

		initCount = 0;

		setStartTime();
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
				TestStepResult stepResult = runTestStep( currentStep, true, true );
				if( stepResult == null )
					return;

				if( !isRunning() )
					return;

//				if( gotoStepIndex != -1 )
//				{
//					currentStepIndex = gotoStepIndex - 1;
//					gotoStepIndex = -1;
//				}
			}

			runContext.setCurrentStep( currentStepIndex + 1 );
		}

		if( runContext.getProperty( TestCaseRunner.Status.class.getName() ) == TestCaseRunner.Status.FAILED
				&& testCase.getFailTestCaseOnErrors() )
		{
			fail( "Failing due to failed test step" );
		}
	}

}
