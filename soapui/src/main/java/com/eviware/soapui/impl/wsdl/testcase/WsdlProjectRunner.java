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

package com.eviware.soapui.impl.wsdl.testcase;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.impl.wsdl.support.AbstractTestRunner;
import com.eviware.soapui.model.project.Project;
import com.eviware.soapui.model.propertyexpansion.DefaultPropertyExpansionContext;
import com.eviware.soapui.model.support.TestSuiteRunListenerAdapter;
import com.eviware.soapui.model.testsuite.ProjectRunListener;
import com.eviware.soapui.model.testsuite.ProjectRunner;
import com.eviware.soapui.model.testsuite.TestSuite;
import com.eviware.soapui.model.testsuite.TestSuite.TestSuiteRunType;
import com.eviware.soapui.model.testsuite.TestSuiteRunContext;
import com.eviware.soapui.model.testsuite.TestSuiteRunListener;
import com.eviware.soapui.model.testsuite.TestSuiteRunner;
import com.eviware.soapui.support.types.StringToObjectMap;

public class WsdlProjectRunner extends AbstractTestRunner<WsdlProject, WsdlProjectRunContext> implements ProjectRunner
{
	private ProjectRunListener[] listeners;
	private Set<TestSuiteRunner> finishedRunners = new HashSet<TestSuiteRunner>();
	private Set<TestSuiteRunner> activeRunners = new HashSet<TestSuiteRunner>();
	private int currentTestSuiteIndex;
	private WsdlTestSuite currentTestSuite;
	private TestSuiteRunListener internalTestRunListener = new InternalTestSuiteRunListener();

	public WsdlProjectRunner( WsdlProject project, StringToObjectMap properties )
	{
		super( project, properties );
	}

	public WsdlProjectRunContext createContext( StringToObjectMap properties )
	{
		return new WsdlProjectRunContext( this, properties );
	}

	public void onCancel( String reason )
	{
		for( TestSuiteRunner runner : activeRunners.toArray( new TestSuiteRunner[activeRunners.size()] ) )
		{
			runner.cancel( reason );
		}
	}

	public void onFail( String reason )
	{
		for( TestSuiteRunner runner : activeRunners.toArray( new TestSuiteRunner[activeRunners.size()] ) )
		{
			runner.fail( reason );
		}
	}

	public void internalRun( WsdlProjectRunContext runContext ) throws Exception
	{
		WsdlProject project = getTestRunnable();

		listeners = project.getProjectRunListeners();
		project.runBeforeRunScript( runContext, this );
		if( !isRunning() )
		{
			return;
		}

		if( project.getTimeout() > 0 )
		{
			startTimeoutTimer( project.getTimeout() );
		}

		notifyBeforeRun();
		if( !isRunning() )
			return;

		if( project.getRunType() == TestSuiteRunType.SEQUENTIAL )
		{
			runSequential( project, runContext );
		}
		else if( project.getRunType() == TestSuiteRunType.PARALLEL )
		{
			runParallel( project, runContext );
		}
	}

	private void runParallel( WsdlProject project, WsdlProjectRunContext runContext )
	{
		currentTestSuiteIndex = -1;
		currentTestSuite = null;

		for( TestSuite testSuite : project.getTestSuiteList() )
		{
			if( !testSuite.isDisabled() )
			{
				testSuite.addTestSuiteRunListener( internalTestRunListener );
				notifyBeforeRunTestSuite( testSuite );
				runTestSuite( ( WsdlTestSuite )testSuite, true );
			}
		}

		try
		{
			synchronized( activeRunners )
			{
				activeRunners.wait();
			}
		}
		catch( InterruptedException e )
		{
			e.printStackTrace();
		}
	}

	private void runSequential( WsdlProject project, WsdlProjectRunContext runContext )
	{
		currentTestSuiteIndex = 0;
		for( ; isRunning() && currentTestSuiteIndex < project.getTestSuiteCount(); currentTestSuiteIndex++ )
		{
			currentTestSuite = ( WsdlTestSuite )project.getTestSuiteAt( currentTestSuiteIndex );
			if( !currentTestSuite.isDisabled() )
			{
				notifyBeforeRunTestSuite( currentTestSuite );
				WsdlTestSuiteRunner testSuiteRunner = runTestSuite( currentTestSuite, false );
				activeRunners.remove( testSuiteRunner );
				finishedRunners.add( testSuiteRunner );
				notifyAfterRunTestSuite( testSuiteRunner );
			}
		}

		updateStatus();
	}

	private void updateStatus()
	{
		for( TestSuiteRunner runner : finishedRunners )
		{
			if( runner.getStatus() == Status.FAILED )
			{
				setStatus( Status.FAILED );
				break;
			}
		}
	}

	private WsdlTestSuiteRunner runTestSuite( WsdlTestSuite testSuite, boolean async )
	{
		DefaultPropertyExpansionContext properties = ( DefaultPropertyExpansionContext )getRunContext().getProperties();
		properties.put( "#ProjectRunner#", this );

		// this is here for backwards compatibility, should be removed eventually
		properties.put( "#TestSuiteRunner#", this );

		WsdlTestSuiteRunner currentRunner = testSuite.run( properties, true );
		activeRunners.add( currentRunner );
		if( !async )
			currentRunner.waitUntilFinished();

		return currentRunner;
	}

	protected void internalFinally( WsdlProjectRunContext runContext )
	{
		WsdlProject project = getTestRunnable();

		try
		{
			project.runAfterRunScript( runContext, this );
		}
		catch( Exception e )
		{
			SoapUI.logError( e );
		}

		notifyAfterRun();

		runContext.clear();
		listeners = null;
	}

	private void notifyAfterRun()
	{
		if( listeners == null || listeners.length == 0 )
			return;

		for( int i = 0; i < listeners.length; i++ )
		{
			listeners[i].afterRun( this, getRunContext() );
		}
	}

	private void notifyBeforeRun()
	{
		if( listeners == null || listeners.length == 0 )
			return;

		for( int i = 0; i < listeners.length; i++ )
		{
			listeners[i].beforeRun( this, getRunContext() );
		}
	}

	private void notifyAfterRunTestSuite( TestSuiteRunner testSuiteRunner )
	{
		if( listeners == null || listeners.length == 0 )
			return;

		for( int i = 0; i < listeners.length; i++ )
		{
			listeners[i].afterTestSuite( this, getRunContext(), testSuiteRunner );
		}
	}

	private void notifyBeforeRunTestSuite( TestSuite testSuite )
	{
		if( listeners == null || listeners.length == 0 )
			return;

		for( int i = 0; i < listeners.length; i++ )
		{
			listeners[i].beforeTestSuite( this, getRunContext(), testSuite );
		}
	}

	public List<TestSuiteRunner> getResults()
	{
		return Arrays.asList( finishedRunners.toArray( new TestSuiteRunner[finishedRunners.size()] ) );
	}

	protected void finishRunner( TestSuiteRunner testRunner )
	{
		notifyAfterRunTestSuite( testRunner );

		activeRunners.remove( testRunner );
		finishedRunners.add( testRunner );

		testRunner.getTestSuite().removeTestSuiteRunListener( internalTestRunListener );

		if( activeRunners.isEmpty() )
		{
			updateStatus();

			synchronized( activeRunners )
			{
				activeRunners.notify();
			}
		}
	}

	private class InternalTestSuiteRunListener extends TestSuiteRunListenerAdapter
	{
		@Override
		public void afterRun( TestSuiteRunner testRunner, TestSuiteRunContext runContext )
		{
			finishRunner( testRunner );
		}
	}

	public Project getProject()
	{
		return getTestRunnable();
	}
}
