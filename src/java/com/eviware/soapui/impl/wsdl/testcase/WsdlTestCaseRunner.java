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

package com.eviware.soapui.impl.wsdl.testcase;

import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Future;

import org.apache.commons.httpclient.HttpState;
import org.apache.log4j.Logger;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.testsuite.TestCase;
import com.eviware.soapui.model.testsuite.TestRunListener;
import com.eviware.soapui.model.testsuite.TestRunner;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.TestStepResult;
import com.eviware.soapui.model.testsuite.TestStepResult.TestStepStatus;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.types.StringToObjectMap;

/**
 * WSDL TestCase Runner - runs all steps in a testcase and collects performance
 * data
 * 
 * @author Ole.Matzura
 */

public class WsdlTestCaseRunner implements Runnable, TestRunner
{
	private TestRunListener[] listeners;
	private final WsdlTestCase testCase;
	private Status status;
	private Throwable error;
	private WsdlTestRunContext runContext;
	private List<TestStepResult> testStepResults = new LinkedList<TestStepResult>();
	private int gotoStepIndex;
	private long startTime;
	private String reason;
	private volatile Future<?> future;
	private int id;
	private int resultCount;

	private final static Logger log = Logger.getLogger( WsdlTestCaseRunner.class );

	private static int idCounter = 0;
	private Timer timeoutTimer;
	private TimeoutTimerTask timeoutTimerTask;
	private Thread thread;

	public WsdlTestCaseRunner( WsdlTestCase testCase, StringToObjectMap properties )
	{
		this.testCase = testCase;
		status = Status.INITIALIZED;
		runContext = new WsdlTestRunContext( this, properties );
		id = ++idCounter;
	}

	public WsdlTestRunContext getRunContext()
	{
		return runContext;
	}

	public void start( boolean async )
	{
		status = Status.RUNNING;
		if( async )
			future = SoapUI.getThreadPool().submit( this );
		else
			run();
	}

	public void cancel( String reason )
	{
		if( status == Status.CANCELED || status == Status.FINISHED || status == Status.FAILED || runContext == null )
			return;
		TestStep currentStep = runContext.getCurrentStep();
		if( currentStep != null )
			currentStep.cancel();
		status = Status.CANCELED;
		this.reason = reason;
	}

	public void fail( String reason )
	{
		if( status == Status.CANCELED || status == Status.FAILED || runContext == null )
			return;
		TestStep currentStep = runContext.getCurrentStep();
		if( currentStep != null )
			currentStep.cancel();
		status = Status.FAILED;
		this.reason = reason;
	}

	public Status getStatus()
	{
		return status;
	}

	public int getId()
	{
		return id;
	}

	public Thread getThread()
	{
		return thread;
	}

	public void run()
	{
		int initCount = 0;

		if( future != null )
		{
			thread = Thread.currentThread();
			thread.setName( "TestCaseRunner Thread for " + testCase.getName() );
		}

		try
		{
			gotoStepIndex = -1;
			testStepResults.clear();

			// create state for testcase if specified
			if( testCase.getKeepSession() )
			{
				runContext.setProperty( SubmitContext.HTTP_STATE_PROPERTY, new HttpState() );
			}

			testCase.runSetupScript( runContext, this );

			status = Status.RUNNING;
			startTime = System.currentTimeMillis();

			if( testCase.getTimeout() > 0 )
			{
				timeoutTimer = new Timer();
				timeoutTimerTask = new TimeoutTimerTask();
				timeoutTimer.schedule( timeoutTimerTask, testCase.getTimeout() );
			}

			listeners = testCase.getTestRunListeners();
			notifyBeforeRun();

			for( ; initCount < testCase.getTestStepCount() && status == Status.RUNNING; initCount++ )
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
					status = Status.FAILED;
					SoapUI.logError( e );
					throw new Exception( "Failed to prepare testStep [" + testStep.getName() + "]; " + e.toString() );
				}
			}

			int currentStepIndex = 0;

			for( ; status == Status.RUNNING && currentStepIndex < testCase.getTestStepCount(); currentStepIndex++ )
			{
				TestStep currentStep = runContext.getCurrentStep();
				if( currentStep != null && !currentStep.isDisabled() )
				{
					TestStepResult stepResult = runTestStep( currentStep, true, true );
					if( stepResult == null )
						return;

					if( status == Status.CANCELED || status == Status.FAILED )
						return;

					if( gotoStepIndex != -1 )
					{
						currentStepIndex = gotoStepIndex - 1;
						gotoStepIndex = -1;
					}
				}

				runContext.setCurrentStep( currentStepIndex + 1 );
			}

			if( runContext.getProperty( TestRunner.Status.class.getName() ) == TestRunner.Status.FAILED
					&& testCase.getFailTestCaseOnErrors() )
			{
				fail( "Failing due to failed test step" );
			}
		}
		catch( Throwable t )
		{
			log.error( "Exception during TestCase Execution", t );

			if( t instanceof OutOfMemoryError && UISupport.confirm( "Exit now without saving?", "Out of Memory Error" ) )
			{
				System.exit( 0 );
			}

			status = Status.FAILED;
			error = t;
			reason = t.toString();
		}
		finally
		{
			if( timeoutTimer != null )
			{
				timeoutTimer.cancel();
			}

			if( status == Status.RUNNING )
			{
				status = Status.FINISHED;
			}

			for( int c = 0; c < initCount && c < testCase.getTestStepCount(); c++ )
			{
				WsdlTestStep testStep = testCase.getTestStepAt( c );
				if( !testStep.isDisabled() )
					testStep.finish( this, runContext );
			}

			notifyAfterRun();

			try
			{
				testCase.runTearDownScript( runContext, this );
			}
			catch( Exception e )
			{
				SoapUI.logError( e );
			}

			runContext.clear();
			listeners = null;
		}
	}

	public TestStepResult runTestStepByName( String name )
	{
		return runTestStep( getTestCase().getTestStepByName( name ), true, true );
	}

	public TestStepResult runTestStep( TestStep testStep )
	{
		return runTestStep( testStep, true, true );
	}

	public TestStepResult runTestStep( TestStep testStep, boolean discard, boolean process )
	{
		for( int i = 0; i < listeners.length; i++ )
		{
			listeners[i].beforeStep( this, runContext );
			if( status == Status.CANCELED || status == Status.FAILED )
				return null;
		}

		TestStepResult stepResult = testStep.run( this, runContext );
		testStepResults.add( stepResult );
		resultCount++ ;
		enforceMaxResults( testCase.getMaxResults() );

		for( int i = 0; i < listeners.length; i++ )
		{
			listeners[i].afterStep( this, runContext, stepResult );
		}

		// discard?
		if( discard && stepResult.getStatus() == TestStepStatus.OK && testCase.getDiscardOkResults()
				&& !stepResult.isDiscarded() )
		{
			stepResult.discard();
		}

		if( process && stepResult.getStatus() == TestStepStatus.FAILED )
		{
			if( testCase.getFailOnError() )
			{
				error = stepResult.getError();
				fail( "Cancelling due to failed test step" );
			}
			else
			{
				runContext.setProperty( TestRunner.Status.class.getName(), TestRunner.Status.FAILED );
			}
		}

		return stepResult;
	}

	private void notifyAfterRun()
	{
		if( listeners == null || listeners.length == 0 )
			return;

		for( int i = 0; i < listeners.length; i++ )
		{
			listeners[i].afterRun( this, runContext );
		}
	}

	private void notifyBeforeRun()
	{
		if( listeners == null || listeners.length == 0 )
			return;

		for( int i = 0; i < listeners.length; i++ )
		{
			listeners[i].beforeRun( this, runContext );
		}
	}

	public TestCase getTestCase()
	{
		return testCase;
	}

	public synchronized Status waitUntilFinished()
	{
		if( future != null )
		{
			if( !future.isDone() )
			{
				try
				{
					future.get();
				}
				catch( Exception e )
				{
					SoapUI.logError( e );
				}
			}
		}
		else
			throw new RuntimeException( "cannot wait on null future" );

		return getStatus();
	}

	public long getTimeTaken()
	{
		long sum = 0;
		for( int c = 0; c < testStepResults.size(); c++ )
		{
			TestStepResult testStepResult = testStepResults.get( c );
			if( testStepResult != null )
				sum += testStepResult.getTimeTaken();
		}

		return sum;
	}

	public long getStartTime()
	{
		return startTime;
	}

	public Throwable getError()
	{
		return error;
	}

	public String getReason()
	{
		return reason == null ? error == null ? null : error.toString() : reason;
	}

	public List<TestStepResult> getResults()
	{
		return testStepResults;
	}

	public int getResultCount()
	{
		return resultCount;
	}

	public void gotoStep( int index )
	{
		gotoStepIndex = index;
	}

	public void enforceMaxResults( long maxResults )
	{
		if( maxResults < 1 )
			return;

		synchronized( this )
		{
			while( testStepResults.size() > maxResults )
			{
				testStepResults.remove( 0 );
			}
		}
	}

	public void gotoStepByName( String stepName )
	{
		TestStep testStep = getTestCase().getTestStepByName( stepName );
		if( testStep != null )
			gotoStep( getTestCase().getIndexOfTestStep( testStep ) );
	}

	private final class TimeoutTimerTask extends TimerTask
	{
		@Override
		public void run()
		{
			fail( "TestCase timed out" );
		}
	}
}