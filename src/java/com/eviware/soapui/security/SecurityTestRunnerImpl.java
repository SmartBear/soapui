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

import java.util.List;
import java.util.Map;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.TestStepConfig;
import com.eviware.soapui.impl.wsdl.support.AbstractTestCaseRunner;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.registry.WsdlTestStepFactory;
import com.eviware.soapui.impl.wsdl.teststeps.registry.WsdlTestStepRegistry;
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.model.testsuite.TestAssertion;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.TestStepResult;
import com.eviware.soapui.security.SecurityCheckRequestResult.SecurityStatus;
import com.eviware.soapui.security.check.AbstractSecurityCheck;
import com.eviware.soapui.security.support.SecurityCheckRunListener;
import com.eviware.soapui.security.support.SecurityTestRunListener;
import com.eviware.soapui.support.types.StringToObjectMap;

public class SecurityTestRunnerImpl extends AbstractTestCaseRunner<SecurityTest, SecurityTestRunContext> implements
		SecurityTestRunner
{

	private SecurityTest securityTest;
	// private boolean stopped;
	private SecurityTestRunListener[] securityTestListeners = new SecurityTestRunListener[0];
	private SecurityTestRunListener[] securityTestStepListeners = new SecurityTestRunListener[0];
	private SecurityCheckRunListener[] securityCheckListeners = new SecurityCheckRunListener[0];
	/**
	 * holds index of current securityCheck out of summary number of checks on
	 * SecxurityTest level used in main progress bar on SecurityTest
	 */
	private int currentCheckOnSecurityTestIndex;

	public SecurityTestRunnerImpl( SecurityTest test, StringToObjectMap properties )
	{
		super( test, properties );
		this.securityTest = test;
		this.currentCheckOnSecurityTestIndex = 0;
		setStatus( Status.INITIALIZED );
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

	protected int runCurrentTestStep( SecurityTestRunContext runContext, int currentStepIndex )
	{
		TestStep currentStep = runContext.getCurrentStep();
		securityTestStepListeners = securityTest.getTestStepRunListeners( currentStep );
		if( !currentStep.isDisabled() )
		{
			for( int i = 0; i < securityTestListeners.length; i++ )
			{
				securityTestListeners[i].beforeStep( this, getRunContext(), currentStep );
			}
			for( int i = 0; i < securityTestStepListeners.length; i++ )
			{
				securityTestStepListeners[i].beforeStep( this, getRunContext(), currentStep );
			}
			TestStepResult stepResult = runTestStep( currentStep, true, true );
			if( stepResult == null )
				return -2;
			// if( !isRunning() )
			// return -2;

			SecurityTestStepResult securityStepResult = new SecurityTestStepResult( currentStep, stepResult );

			Map<String, List<AbstractSecurityCheck>> secCheckMap = securityTest.getSecurityChecksMap();
			if( secCheckMap.containsKey( currentStep.getId() ) )
			{
				List<AbstractSecurityCheck> testStepChecksList = secCheckMap.get( currentStep.getId() );
				for( int i = 0; i < testStepChecksList.size(); i++ )
				{
					AbstractSecurityCheck securityCheck = testStepChecksList.get( i );
					runContext.setCurrentCheckIndex( i );
					runContext.setCurrentCheckOnSecurityTestIndex( currentCheckOnSecurityTestIndex++ );
					SecurityCheckResult securityCheckResult = runTestStepSecurityCheck( runContext, currentStep,
							securityCheck );
					securityStepResult.addSecurityCheckResult( securityCheckResult );
					if( securityCheckResult.getStatus() == SecurityStatus.FAILED )
					{
						if( getTestRunnable().getFailOnError() )
						{
							// setError( stepResult.getError() );
							fail( "Cancelling due to failed security check" );
						}
						else
						{
							getRunContext().setProperty( SecurityTestRunner.Status.class.getName(),
									SecurityTestRunner.Status.FAILED );
						}
					}
				}
				securityTest.putSecurityTestStepResult( currentStep, securityStepResult );
			}
			for( int i = 0; i < securityTestStepListeners.length; i++ )
			{
				securityTestStepListeners[i].afterStep( this, getRunContext(), securityStepResult );
			}
			for( int i = 0; i < securityTestListeners.length; i++ )
			{
				securityTestListeners[i].afterStep( this, getRunContext(), securityStepResult );
			}
			if( getGotoStepIndex() != -1 )
			{
				currentStepIndex = getGotoStepIndex() - 1;
				gotoStep( -1 );
			}
		}

		runContext.setCurrentStep( currentStepIndex + 1 );
		return currentStepIndex;

	}

	public SecurityCheckResult runTestStepSecurityCheck( SecurityTestRunContext runContext, TestStep currentStep,
			AbstractSecurityCheck securityCheck )
	{
		SecurityCheckResult result = new SecurityCheckResult( securityCheck );
		if( securityCheck.acceptsTestStep( currentStep ) )
		{
			securityCheckListeners = securityTest.getSecurityCheckRunListeners();
			for( int j = 0; j < securityTestStepListeners.length; j++ )
			{
				securityTestStepListeners[j].beforeSecurityCheck( this, runContext, securityCheck );
			}
			for( int j = 0; j < securityTestListeners.length; j++ )
			{
				securityTestListeners[j].beforeSecurityCheck( this, runContext, securityCheck );
			}
			result = securityCheck.run( cloneForSecurityCheck( ( WsdlTestStep )currentStep ), runContext, this );
			if( securityTest.getFailOnError() && result.getStatus() == SecurityStatus.FAILED )
			{
				fail( "Cancelling due to failed security check" );
			}
			for( int j = 0; j < securityCheckListeners.length; j++ )
			{
				securityCheckListeners[j].afterSecurityCheck( this, runContext, result );
			}
			for( int j = 0; j < securityTestStepListeners.length; j++ )
			{
				securityTestStepListeners[j].afterSecurityCheck( this, runContext, result );
			}
			for( int j = 0; j < securityTestListeners.length; j++ )
			{
				securityTestListeners[j].afterSecurityCheck( this, runContext, result );
			}
		}
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
		if( securityTestListeners == null || securityTestListeners.length == 0 )
			return;

		for( int i = 0; i < securityTestListeners.length; i++ )
		{
			try
			{
				securityTestListeners[i].beforeRun( this, getRunContext() );
			}
			catch( Throwable t )
			{
				SoapUI.logError( t );
			}
		}
		super.notifyBeforeRun();

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
				&& getTestRunnable().getFailSecurityTestOnCheckErrors() )
		{
			fail( "Failing due to failed security check" );
		}
	}

}
