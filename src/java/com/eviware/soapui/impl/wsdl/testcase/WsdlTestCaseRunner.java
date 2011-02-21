/*
 *  soapUI, copyright (C) 2004-2011 eviware.com 
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
import java.util.List;

import org.apache.commons.collections.list.TreeList;
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

	TestRunListener[] testRunListeners = new TestRunListener[0];
	@SuppressWarnings( "unchecked" )
	private List<TestStepResult> testStepResults = Collections.synchronizedList( new TreeList() );
	private int gotoStepIndex;
	private int resultCount;
	private int initCount;
	private int startStep = 0;

	public WsdlTestCaseRunner( WsdlTestCase testCase, StringToObjectMap properties )
	{
		super( testCase, properties );
	}

	public WsdlTestRunContext createContext( StringToObjectMap properties )
	{
		return new WsdlTestRunContext( this, properties );
	}

	public int getStartStep()
	{
		return startStep;
	}

	public void setStartStep( int startStep )
	{
		this.startStep = startStep;
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

		testRunListeners = testCase.getTestRunListeners();
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

		initCount = getStartStep();

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
		preserveContext( getRunContext() );
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
		testRunListeners = null;
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
		for( int i = 0; i < testRunListeners.length; i++ )
		{
			testRunListeners[i].beforeStep( this, getRunContext(), testStep );
			if( !isRunning() )
				return null;
		}

		TestStepResult stepResult = testStep.run( this, getRunContext() );

		testStepResults.add( stepResult );
		resultCount++ ;
		enforceMaxResults( getTestRunnable().getMaxResults() );

		for( int i = 0; i < testRunListeners.length; i++ )
		{
			testRunListeners[i].afterStep( this, getRunContext(), stepResult );
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
		preserveContext( getRunContext() );
		return stepResult;
	}

	/**
	 * create backup of context properties in WsdlTestCase. This is used for RUN
	 * FROM HERE action.
	 * 
	 * @param runContext
	 */
	private void preserveContext( WsdlTestRunContext runContext )
	{
		getTestCase().setRunFromHereContext( runContext.getProperties() );
	}

	protected void notifyAfterRun()
	{
		if( testRunListeners == null || testRunListeners.length == 0 )
			return;

		for( int i = 0; i < testRunListeners.length; i++ )
		{
			try
			{
				testRunListeners[i].afterRun( this, getRunContext() );
			}
			catch( Throwable t )
			{
				SoapUI.logError( t );
			}
		}
	}

	protected void notifyBeforeRun()
	{
		if( testRunListeners == null || testRunListeners.length == 0 )
			return;

		for( int i = 0; i < testRunListeners.length; i++ )
		{
			try
			{
				testRunListeners[i].beforeRun( this, getRunContext() );
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
