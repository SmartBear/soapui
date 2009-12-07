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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.httpclient.HttpState;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.support.AbstractTestRunner;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.TestRunListener;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.TestStepResult;
import com.eviware.soapui.model.testsuite.TestStepResult.TestStepStatus;
import com.eviware.soapui.support.types.StringToObjectMap;

/**
 * WSDL TestCase Runner - runs all steps in a testcase and collects performance
 * data
 * 
 * @author Ole.Matzura
 */

public class WsdlTestCaseRunner extends AbstractTestRunner<WsdlTestCase, WsdlTestRunContext> implements TestCaseRunner
{
	private TestRunListener[] listeners =new TestRunListener[0]; 
	private List<TestStepResult> testStepResults = Collections.synchronizedList( new LinkedList<TestStepResult>() );
	private int gotoStepIndex;
	private int resultCount;
	private int initCount;

	public WsdlTestCaseRunner( WsdlTestCase testCase, StringToObjectMap properties )
	{
		super( testCase, properties );
	}

	public WsdlTestRunContext createContext( StringToObjectMap properties )
	{
		return new WsdlTestRunContext( this, properties );
	}

	public void onCancel( String reason )
	{
		TestStep currentStep = getRunContext().getCurrentStep();
		if( currentStep != null )
			currentStep.cancel();
	}

	public void onFail( String reason )
	{
		TestStep currentStep = getRunContext().getCurrentStep();
		if( currentStep != null )
			currentStep.cancel();
	}

	public void internalRun( WsdlTestRunContext runContext ) throws Exception
	{
		WsdlTestCase testCase = getTestRunnable();
		gotoStepIndex = -1;
		testStepResults.clear();

		// create state for testcase if specified
		if( testCase.getKeepSession() )
		{
			runContext.setProperty( SubmitContext.HTTP_STATE_PROPERTY, new HttpState() );
		}

		listeners = testCase.getTestRunListeners();
		testCase.runSetupScript( runContext, this );
		if( !isRunning() )
			return;

		if( testCase.getTimeout() > 0 )
		{
			startTimeoutTimer( testCase.getTimeout() );
		}

		notifyBeforeRun();
		if( !isRunning() )
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

		int currentStepIndex = 0;

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

				if( gotoStepIndex != -1 )
				{
					currentStepIndex = gotoStepIndex - 1;
					gotoStepIndex = -1;
				}
			}

			runContext.setCurrentStep( currentStepIndex + 1 );
		}

		if( runContext.getProperty( TestCaseRunner.Status.class.getName() ) == TestCaseRunner.Status.FAILED
				&& testCase.getFailTestCaseOnErrors() )
		{
			fail( "Failing due to failed test step" );
		}
	}

	protected void internalFinally( WsdlTestRunContext runContext )
	{
		WsdlTestCase testCase = getTestRunnable();
		
		for( int c = 0; c < initCount && c < testCase.getTestStepCount(); c++ )
		{
			WsdlTestStep testStep = testCase.getTestStepAt( c );
			if( !testStep.isDisabled() )
				testStep.finish( this, runContext );
		}

		try
		{
			testCase.runTearDownScript( runContext, this );
		}
		catch( Exception e )
		{
			SoapUI.logError( e );
		}
		
		notifyAfterRun();

		runContext.clear();
		listeners = null;
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
			listeners[i].beforeStep( this, getRunContext(), testStep );
			if( !isRunning() )
				return null;
		}

		TestStepResult stepResult = testStep.run( this, getRunContext() );
		testStepResults.add( stepResult );
		resultCount++ ;
		enforceMaxResults( getTestRunnable().getMaxResults() );

		for( int i = 0; i < listeners.length; i++ )
		{
			listeners[i].afterStep( this, getRunContext(), stepResult );
		}

		// discard?
		if( discard && stepResult.getStatus() == TestStepStatus.OK && getTestRunnable().getDiscardOkResults()
				&& !stepResult.isDiscarded() )
		{
			stepResult.discard();
		}

		if( process && stepResult.getStatus() == TestStepStatus.FAILED )
		{
			if( getTestRunnable().getFailOnError() )
			{
				setError( stepResult.getError() );
				fail( "Cancelling due to failed test step" );
			}
			else
			{
				getRunContext().setProperty( TestCaseRunner.Status.class.getName(), TestCaseRunner.Status.FAILED );
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

	private void notifyBeforeRun()
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

	public WsdlTestCase getTestCase()
	{
		return getTestRunnable();
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

			while( testStepResults.size() > maxResults )
			{
				testStepResults.remove( 0 );
			}
		}

	public void gotoStepByName( String stepName )
	{
		TestStep testStep = getTestCase().getTestStepByName( stepName );
		if( testStep != null )
			gotoStep( getTestCase().getIndexOfTestStep( testStep ) );
	}
}