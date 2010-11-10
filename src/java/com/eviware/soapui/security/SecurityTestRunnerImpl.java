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
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.registry.WsdlTestStepFactory;
import com.eviware.soapui.impl.wsdl.teststeps.registry.WsdlTestStepRegistry;
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.model.testsuite.TestAssertion;
import com.eviware.soapui.model.testsuite.TestRunContext;
import com.eviware.soapui.model.testsuite.TestRunnable;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.security.check.SecurityCheck;
import com.eviware.soapui.security.log.SecurityTestLogMessageEntry;
import com.eviware.soapui.support.types.StringToObjectMap;

public class SecurityTestRunnerImpl implements SecurityTestRunner
{

	private SecurityTest securityTest;
	private long startTime = 0;
	private Status status;
	private SecurityTestContext context;
	private boolean stopped;
	private boolean hasTornDown;
	private String reason;

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

		// for( LoadTestRunListener listener : loadTest.getLoadTestRunListeners()
		// )
		// {
		// listener.loadTestStopped( this, context );
		// }

		stop();
	}

	@Override
	public void fail( String reason )
	{
		// TODO Auto-generated method stub

	}

	@Override
	public String getReason()
	{
		return reason;
	}

	@Override
	public TestRunContext getRunContext()
	{
		return context;
	}

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

	@Override
	public TestRunnable getTestRunnable()
	{
		return securityTest;
	}

	@Override
	public long getTimeTaken()
	{
		return System.currentTimeMillis() - startTime;
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
	private TestStep cloneForSecurityCheck( WsdlTestStep sourceTestStep )
	{
		WsdlTestStep clonedTestStep = null;
		TestStepConfig testStepConfig = ( TestStepConfig )sourceTestStep.getConfig().copy();
		WsdlTestStepFactory factory = WsdlTestStepRegistry.getInstance().getFactory( testStepConfig.getType() );
		if( factory != null )
		{
			clonedTestStep = factory.buildTestStep( securityTest.getTestCase(), testStepConfig, false );
			if (clonedTestStep instanceof Assertable) {
				for (TestAssertion assertion : ((Assertable)clonedTestStep).getAssertionList()) {
					((Assertable)clonedTestStep).removeAssertion(assertion);
				}
			}
		}
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

		if( status == Status.RUNNING )
		{
			// for( LoadTestRunListener listener :
			// loadTest.getLoadTestRunListeners()
			// )
			// {
			// listener.loadTestStarted( this, context );
			// }
			//
			// startStrategyThread();
			// TODO start actual actions
			WsdlTestCase testCase = securityTest.getTestCase();
			List<TestStep> testStepsList = testCase.getTestStepList();
			HashMap<String, List<SecurityCheck>> secCheckMap = securityTest.getSecurityChecksMap();
			WsdlTestCaseRunner testCaseRunner = new WsdlTestCaseRunner( testCase, new StringToObjectMap() );
			for( TestStep testStep : testStepsList )
			{
				if( secCheckMap.containsKey( testStep.getName() ) )
				{
					List<SecurityCheck> testStepChecksList = secCheckMap.get( testStep.getName() );
					for( SecurityCheck securityCheck : testStepChecksList )
					{
						securityCheck.run( cloneForSecurityCheck((WsdlTestStep)testStep) );
					}
				}
				else
				{
//					System.out.print( "endpoint:" + ((WsdlTestRequestStep)testStep).getTestRequest().getEndpoint() );
					testCaseRunner.runTestStepByName( testStep.getName() );
				}
			}
			testCase.release();
		}
		else
		{
			stop();
		}
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
