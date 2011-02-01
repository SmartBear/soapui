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

package com.eviware.soapui.security;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.collections.list.TreeList;
import org.apache.commons.httpclient.HttpState;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.TestStepConfig;
import com.eviware.soapui.impl.wsdl.support.AbstractTestRunner;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.registry.WsdlTestStepFactory;
import com.eviware.soapui.impl.wsdl.teststeps.registry.WsdlTestStepRegistry;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.model.testsuite.TestAssertion;
import com.eviware.soapui.model.testsuite.TestCase;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.TestStepResult;
import com.eviware.soapui.model.testsuite.TestStepResult.TestStepStatus;
import com.eviware.soapui.security.SecurityCheckRequestResult.SecurityCheckStatus;
import com.eviware.soapui.security.check.AbstractSecurityCheck;
import com.eviware.soapui.security.log.SecurityTestLogMessageEntry;
import com.eviware.soapui.security.support.SecurityCheckRunListener;
import com.eviware.soapui.security.support.SecurityTestRunListener;
import com.eviware.soapui.security.support.SecurityTestStepRunListener;
import com.eviware.soapui.support.types.StringToObjectMap;

public class SecurityTestRunnerImpl extends AbstractTestRunner<SecurityTest, SecurityTestRunContext> implements
		TestCaseRunner, SecurityTestRunnerInterface
{

	private SecurityTest securityTest;
	private long startTime = 0;
	// private WsdlTestRunContext context;
	// private boolean stopped;
	private boolean hasTornDown;
	private String reason;
	private SecurityTestRunListener[] listeners = new SecurityTestRunListener[0];
	private SecurityTestStepRunListener[] securityTestStepListeners = new SecurityTestStepRunListener[0];
	private SecurityCheckRunListener[] seccheckListeners = new SecurityCheckRunListener[0];
	// private TestRunListener[] testCaseRunListeners = new TestRunListener[0];
	private int initCount;
	private int startStep = 0;
	private int gotoStepIndex;

	@SuppressWarnings( "unchecked" )
	private List<TestStepResult> testStepResults = Collections.synchronizedList( new TreeList() );
	private int resultCount;

	public SecurityTestRunnerImpl( SecurityTest test )
	{
		super( test, new StringToObjectMap() );
		this.securityTest = test;
		setStatus( Status.INITIALIZED );
	}

	public SecurityTest getSecurityTest()
	{
		return securityTest;
	}

	public SecurityTestRunContext createContext( StringToObjectMap properties )
	{
		return new SecurityTestRunContext( this, properties );
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
		// enforceMaxResults( getTestRunnable().getMaxResults() );

		for( int i = 0; i < listeners.length; i++ )
		{
			listeners[i].afterStep( this, getRunContext(), stepResult );
		}

		// discard?
		// if( discard && stepResult.getStatus() == TestStepStatus.OK &&
		// getTestRunnable().getDiscardOkResults()
		// && !stepResult.isDiscarded() )
		// {
		// stepResult.discard();
		// }

		if( process && stepResult.getStatus() == TestStepStatus.FAILED )
		{
			if( getTestRunnable().getFailSecurityTestOnCheckErrors() )
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

	@Override
	public void fail( String reason )
	{

		setStatus( Status.FAILED );

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

	public void internalRun( SecurityTestRunContext runContext ) throws Exception
	{
		securityTest.getTestCase().beforeSave();
		listeners = securityTest.getTestRunListeners();
		hasTornDown = false;
		startTime = System.currentTimeMillis();
		try
		{
			securityTest.runStartupScript( runContext, this );
		}
		catch( Exception e1 )
		{
			SoapUI.logError( e1 );
		}

		// status = Status.RUNNING;
		//
		// if( status == Status.RUNNING )
		// {
		WsdlTestCase testCase = securityTest.getTestCase();
		// List<TestStep> testStepsList = testCase.getTestStepList();
		HashMap<String, List<AbstractSecurityCheck>> secCheckMap = securityTest.getSecurityChecksMap();
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

		// testCaseRunListeners = testCase.getTestRunListeners();
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
		boolean testFailed = false;

		for( ; isRunning() && currentStepIndex < testCase.getTestStepCount(); currentStepIndex++ )
		{
			TestStep currentStep = runContext.getCurrentStep();
			securityTestStepListeners = securityTest.getTestStepRunListeners( currentStep );
			if( !currentStep.isDisabled() )
			{
				for( int i = 0; i < listeners.length; i++ )
				{
					listeners[i].beforeStep( this, getRunContext(), currentStep );
					if( !isRunning() )
						return;
				}
				for( int i = 0; i < securityTestStepListeners.length; i++ )
				{
					securityTestStepListeners[i].beforeStep( this, getRunContext() );
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
					List<AbstractSecurityCheck> testStepChecksList = secCheckMap.get( currentStep.getId() );
					for( int i = 0; i < testStepChecksList.size(); i++ )
					{
						AbstractSecurityCheck securityCheck = testStepChecksList.get( i );
						runContext.setCurrentCheckIndex( i );
						runTestStepSecurityCheck( runContext, currentStep, securityCheck );
					}
				}
				for( int i = 0; i < securityTestStepListeners.length; i++ )
				{
					securityTestStepListeners[i].afterStep( this, getRunContext(), stepResult );
					if( !isRunning() )
						return;
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
		if( runContext.getProperty( TestCaseRunner.Status.class.getName() ) == TestCaseRunner.Status.FAILED
				&& testCase.getFailTestCaseOnErrors() )
		{
			fail( "Failing due to failed test step" );
		}

		// testCase.release();
		// }
	}

	public void runTestStepSecurityCheck( SecurityTestRunContext runContext, TestStep currentStep,
			AbstractSecurityCheck securityCheck )
	{
		if( securityCheck.acceptsTestStep( currentStep ) )
		{
			seccheckListeners = securityTest.getSecurityCheckRunListeners();
			for( int j = 0; j < securityTestStepListeners.length; j++ )
			{
				securityTestStepListeners[j].beforeSecurityCheck( this, runContext, securityCheck );
			}
			SecurityCheckResult result = securityCheck.runNew( cloneForSecurityCheck( ( WsdlTestStep )currentStep ),
					runContext );
			// TODO check
			if( securityTest.getFailSecurityTestOnCheckErrors() && result.getStatus() == SecurityCheckStatus.FAILED )
			{
				fail( "Failing due to failed security check" );
			}
			for( int j = 0; j < securityTestStepListeners.length; j++ )
			{
				securityTestStepListeners[j].afterSecurityCheck( this, runContext, result );
			}
			for( int j = 0; j < seccheckListeners.length; j++ )
			{
				seccheckListeners[j].afterSecurityCheck( this, runContext, result );
			}

		}
	}

	protected void internalFinally( SecurityTestRunContext runContext )
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
//			securityTest.getSecurityTestLog().addEntry(
//					new SecurityTestLogMessageEntry( " SecurityTest ended at " + new Date( System.currentTimeMillis() ) ) );
		}
		catch( Exception e )
		{
			SoapUI.logError( e );
		}

		notifyAfterRun();

		runContext.clear();
		listeners = null;
		securityTestStepListeners = null;
	}

	public void release()
	{

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

	@Override
	public TestCase getTestCase()
	{
		return securityTest.getTestCase();
	}

}
