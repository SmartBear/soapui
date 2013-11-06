/*
 *  SoapUI, copyright (C) 2004-2012 smartbear.com
 *
 *  SoapUI is free software; you can redistribute it and/or modify it under the
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  SoapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.security;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.TestStepConfig;
import com.eviware.soapui.impl.wsdl.support.AbstractTestCaseRunner;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.registry.WsdlTestStepFactory;
import com.eviware.soapui.impl.wsdl.teststeps.registry.WsdlTestStepRegistry;
import com.eviware.soapui.model.security.SecurityScan;
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.model.testsuite.TestAssertion;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.TestStepResult;
import com.eviware.soapui.model.testsuite.TestStepResult.TestStepStatus;
import com.eviware.soapui.security.result.SecurityScanResult;
import com.eviware.soapui.security.result.SecurityTestStepResult;
import com.eviware.soapui.security.result.SecurityResult.ResultStatus;
import com.eviware.soapui.security.scan.AbstractSecurityScanWithProperties;
import com.eviware.soapui.security.support.SecurityTestRunListener;
import com.eviware.soapui.support.types.StringToObjectMap;

public class SecurityTestRunnerImpl extends AbstractTestCaseRunner<SecurityTest, SecurityTestRunContext> implements
		SecurityTestRunner
{

	private SecurityTest securityTest;
	// private boolean stopped;
	private SecurityTestRunListener[] securityTestListeners = new SecurityTestRunListener[0];
	private SecurityTestRunListener[] securityTestStepListeners = new SecurityTestRunListener[0];
	private long timeTaken;
	/**
	 * holds index of current securityScan out of summary number of scans on
	 * SecurityTest level used in main progress bar on SecurityTest
	 */
	private int currentScanOnSecurityTestIndex;

	public SecurityTestRunnerImpl( SecurityTest test, StringToObjectMap properties )
	{
		super( test, properties );
		this.securityTest = test;
		this.currentScanOnSecurityTestIndex = 0;
	}

	public SecurityTestRunContext createContext( StringToObjectMap properties )
	{
		return new SecurityTestRunContext( this, properties );
	}

	public SecurityTest getSecurityTest()
	{
		return getTestRunnable();
	}

	@Override
	public TestStepResult runTestStep( TestStep testStep, boolean discard, boolean process )
	{
		TestStepResult stepResult = testStep.run( this, getRunContext() );
		getResults().add( stepResult );
		setResultCount( getResultCount() + 1 );
		// enforceMaxResults( getTestRunnable().getMaxResults() );

		// discard?
		// if( discard && stepResult.getStatus() == TestStepStatus.OK &&
		// getTestRunnable().getDiscardOkResults()
		// && !stepResult.isDiscarded() )
		// {
		// stepResult.discard();
		// }

		return stepResult;
	}

	/**
	 * Clones original TestStep for security modification this does not alter the
	 * original test step
	 * 
	 * @param sourceTestStep
	 * @return TestStep
	 */
	public TestStep cloneForSecurityScan( WsdlTestStep sourceTestStep )
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

	/**
	 * Clones original TestStep for security modification this does not alter the
	 * original test step
	 * 
	 * @param sourceTestStep
	 * @return TestStep
	 */
	public static TestStep cloneTestStepForSecurityScan( WsdlTestStep sourceTestStep )
	{
		WsdlTestStep clonedTestStep = null;
		TestStepConfig testStepConfig = ( TestStepConfig )sourceTestStep.getConfig().copy();
		WsdlTestStepFactory factory = WsdlTestStepRegistry.getInstance().getFactory( testStepConfig.getType() );
		if( factory != null )
		{
			clonedTestStep = factory.buildTestStep( sourceTestStep.getTestCase(), testStepConfig, false );
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

	protected int runCurrentTestStep( SecurityTestRunContext runContext, int currentStepIndex )
	{
		// flag for detecting if running has been interrupted either by canceling
		// securityScanRequest
		// or if request result is null(backward compatibility for running
		// TestCase )
		boolean jumpExit = false;
		TestStep currentStep = runContext.getCurrentStep();
		securityTestStepListeners = securityTest.getTestStepRunListeners( currentStep );
		if( !currentStep.isDisabled() && !securityTest.skipTest( currentStep ) )
		{
			TestStepResult stepResult = runTestStep( currentStep, true, true );
			if( stepResult == null )
				jumpExit = true;
			// if( !isRunning() )
			// return -2;

			SecurityTestStepResult securityStepResult = new SecurityTestStepResult( currentStep, stepResult );
			for( int i = 0; i < securityTestListeners.length; i++ )
			{
				if( Arrays.asList( getSecurityTest().getSecurityTestRunListeners() ).contains( securityTestListeners[i] ) )
					securityTestListeners[i].afterOriginalStep( this, getRunContext(), securityStepResult );
			}

			for( int i = 0; i < securityTestListeners.length; i++ )
			{
				if( Arrays.asList( getSecurityTest().getSecurityTestRunListeners() ).contains( securityTestListeners[i] ) )
					securityTestListeners[i].beforeStep( this, getRunContext(), stepResult );
			}
			for( int i = 0; i < securityTestStepListeners.length; i++ )
			{
				if( Arrays.asList( getSecurityTest().getSecurityTestRunListeners() )
						.contains( securityTestStepListeners[i] ) )
					securityTestStepListeners[i].beforeStep( this, getRunContext(), stepResult );
			}
			Map<String, List<SecurityScan>> secScanMap = securityTest.getSecurityScansMap();
			if( secScanMap.containsKey( currentStep.getId() ) )
			{
				List<SecurityScan> testStepScansList = secScanMap.get( currentStep.getId() );
				for( int i = 0; i < testStepScansList.size(); i++ )
				{
					SecurityScan securityScan = testStepScansList.get( i );
					// if security scan is disabled skip it.
					if( securityScan.isDisabled() || securityScan.isSkipFurtherRunning() )
						continue;
					//if step is failed and scan not applicable to failed steps just set it to skipped
					//run scan otherwise
					if( stepResult.getStatus() == TestStepStatus.FAILED && !securityScan.isApplyForFailedStep() )
					{
						SecurityScanResult securityScanResult = new SecurityScanResult( securityScan );
						if( securityScan.getAssertionCount() > 0 )
							securityScanResult.setStatus( ResultStatus.OK );
						else if( securityScan instanceof AbstractSecurityScanWithProperties )
						{
							if( ( ( AbstractSecurityScanWithProperties )securityScan ).getParameterHolder().getParameterList()
									.size() > 0 )
								securityScanResult.setStatus( ResultStatus.OK );
							else
								securityScanResult.setStatus( ResultStatus.SKIPPED );
						}
						else
							securityScanResult.setStatus( ResultStatus.SKIPPED );
						securityStepResult.addSecurityScanResult( securityScanResult );

						runAfterListeners( runContext, securityScanResult );
					}
					else
					{
						runContext.setCurrentScanIndex( i );
						runContext.setCurrentScanOnSecurityTestIndex( currentScanOnSecurityTestIndex++ );
						SecurityScanResult securityScanResult = runTestStepSecurityScan( runContext, currentStep,
								securityScan );
						securityStepResult.addSecurityScanResult( securityScanResult );
						if( securityScanResult.isCanceled() )
						{
							jumpExit = true;
							break;
						}
						else if( securityScanResult.getStatus() == ResultStatus.FAILED )
						{
							if( getTestRunnable().getFailOnError() )
							{
								// setError( stepResult.getError() );
								fail( "Cancelling due to failed security scan" );
							}
							else
							{
								getRunContext().setProperty( SecurityTestRunner.Status.class.getName(),
										SecurityTestRunner.Status.FAILED );
							}
						}
					}
				}
				// in case no security scan is executed
				if( securityStepResult.getStatus() == ResultStatus.INITIALIZED )
				{
					securityStepResult.setStatus( ResultStatus.UNKNOWN );
				}
				securityTest.putSecurityTestStepResult( currentStep, securityStepResult );
				timeTaken += securityStepResult.getTimeTaken();
			}
			for( int i = 0; i < securityTestStepListeners.length; i++ )
			{
				if( Arrays.asList( getSecurityTest().getSecurityTestRunListeners() )
						.contains( securityTestStepListeners[i] ) )
					securityTestStepListeners[i].afterStep( this, getRunContext(), securityStepResult );
			}
			for( int i = 0; i < securityTestListeners.length; i++ )
			{
				if( Arrays.asList( getSecurityTest().getSecurityTestRunListeners() ).contains( securityTestListeners[i] ) )
					securityTestListeners[i].afterStep( this, getRunContext(), securityStepResult );
			}
			if( jumpExit )
			{
				return -2;
			}
			else if( getGotoStepIndex() != -1 )
			{
				currentStepIndex = getGotoStepIndex() - 1;
				gotoStep( -1 );
			}
		}

		runContext.setCurrentStep( currentStepIndex + 1 );
		return currentStepIndex;

	}

	/**
	 * @param runContext
	 * @param securityScanResult
	 */
	private void runAfterListeners( SecurityTestRunContext runContext, SecurityScanResult securityScanResult )
	{
		for( int j = 0; j < securityTestStepListeners.length; j++ )
		{
			if( Arrays.asList( getSecurityTest().getSecurityTestRunListeners() ).contains( securityTestStepListeners[j] ) )
				securityTestStepListeners[j].afterSecurityScan( this, runContext, securityScanResult );
		}
		for( int j = 0; j < securityTestListeners.length; j++ )
		{
			if( Arrays.asList( getSecurityTest().getSecurityTestRunListeners() ).contains( securityTestListeners[j] ) )
				securityTestListeners[j].afterSecurityScan( this, runContext, securityScanResult );
		}
	}

	public SecurityScanResult runTestStepSecurityScan( SecurityTestRunContext runContext, TestStep currentStep,
			SecurityScan securityScan )
	{
		SecurityScanResult result = null;
		for( int j = 0; j < securityTestStepListeners.length; j++ )
		{
			if( Arrays.asList( getSecurityTest().getSecurityTestRunListeners() ).contains( securityTestStepListeners[j] ) )
				securityTestStepListeners[j].beforeSecurityScan( this, runContext, securityScan );
		}
		for( int j = 0; j < securityTestListeners.length; j++ )
		{
			if( Arrays.asList( getSecurityTest().getSecurityTestRunListeners() ).contains( securityTestListeners[j] ) )
				securityTestListeners[j].beforeSecurityScan( this, runContext, securityScan );
		}
		result = securityScan.run( cloneForSecurityScan( ( WsdlTestStep )currentStep ), runContext, this );
		if( securityScan.isRunOnlyOnce() )
		{
			securityScan.setSkipFurtherRunning( true );
		}
		if( securityTest.getFailOnError() && result.getStatus() == ResultStatus.FAILED )
		{
			fail( "Cancelling due to failed security scan" );
		}
		runAfterListeners( runContext, result );
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.eviware.soapui.impl.wsdl.support.AbstractTestCaseRunner#notifyBeforeRun
	 * ()
	 * 
	 * The order of listeners notifications here is important, security listeners
	 * first, so while monitoring execution TestCase log is disabled before
	 * WsdlTestCaseDesktopPanel.InternalTestRunListener.beforeRun is executed
	 * otherwise SecurityTest execution will temper with functional log.
	 */
	protected void notifyBeforeRun()
	{
		reset();
		if( securityTestListeners == null || securityTestListeners.length == 0 )
			return;

		for( int i = 0; i < securityTestListeners.length; i++ )
		{
			try
			{
				if( Arrays.asList( getSecurityTest().getSecurityTestRunListeners() ).contains( securityTestListeners[i] ) )
					securityTestListeners[i].beforeRun( this, getRunContext() );
			}
			catch( Throwable t )
			{
				SoapUI.logError( t );
			}
		}
		super.notifyBeforeRun();

	}

	private void reset()
	{
		securityTest.resetAllScansSkipFurtherRunning();
		securityTest.clearSecurityTestStepResultMap();
		timeTaken = 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.eviware.soapui.impl.wsdl.support.AbstractTestCaseRunner#notifyAfterRun
	 * ()
	 * 
	 * The same as for notifyBeforeRun, security listeners come last
	 */
	protected void notifyAfterRun()
	{
		super.notifyAfterRun();
		if( securityTestListeners == null || securityTestListeners.length == 0 )
			return;

		for( int i = 0; i < securityTestListeners.length; i++ )
		{
			try
			{
				if( Arrays.asList( getSecurityTest().getSecurityTestRunListeners() ).contains( securityTestListeners[i] ) )
					securityTestListeners[i].afterRun( this, getRunContext() );
			}
			catch( Throwable t )
			{
				SoapUI.logError( t );
			}
		}
	}

	@Override
	public WsdlTestCase getTestCase()
	{
		return getTestRunnable().getTestCase();
	}

	@Override
	protected void clear( SecurityTestRunContext runContext )
	{
		super.clear( runContext );
		securityTestListeners = null;
		securityTestStepListeners = null;
	}

	@Override
	protected void runSetupScripts( SecurityTestRunContext runContext ) throws Exception
	{
		super.runSetupScripts( runContext );
		getTestRunnable().runStartupScript( runContext, this );
	}

	@Override
	protected void runTearDownScripts( SecurityTestRunContext runContext ) throws Exception
	{
		getTestRunnable().runTearDownScript( runContext, this );
		super.runTearDownScripts( runContext );
	}

	@Override
	protected void fillInTestRunnableListeners()
	{
		super.fillInTestRunnableListeners();
		securityTestListeners = getTestRunnable().getSecurityTestRunListeners();
	}

	@Override
	protected void failTestRunnableOnErrors( SecurityTestRunContext runContext )
	{
		if( runContext.getProperty( SecurityTestRunner.Status.class.getName() ) == SecurityTestRunner.Status.FAILED
				&& getTestRunnable().getFailSecurityTestOnScanErrors() )
		{
			fail( "Failing due to failed security scan" );
		}
	}

	public long getTimeTaken()
	{
		return timeTaken;
	}

	public long getFunctionalTimeTaken()
	{
		return super.getTimeTaken();
	}

}
