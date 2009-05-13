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

package com.eviware.soapui.monitor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.model.mock.MockRunner;
import com.eviware.soapui.model.mock.MockService;
import com.eviware.soapui.model.project.Project;
import com.eviware.soapui.model.support.LoadTestRunListenerAdapter;
import com.eviware.soapui.model.support.MockRunListenerAdapter;
import com.eviware.soapui.model.support.ProjectListenerAdapter;
import com.eviware.soapui.model.support.TestRunListenerAdapter;
import com.eviware.soapui.model.support.TestSuiteListenerAdapter;
import com.eviware.soapui.model.support.WorkspaceListenerAdapter;
import com.eviware.soapui.model.testsuite.LoadTest;
import com.eviware.soapui.model.testsuite.LoadTestRunContext;
import com.eviware.soapui.model.testsuite.LoadTestRunner;
import com.eviware.soapui.model.testsuite.TestCase;
import com.eviware.soapui.model.testsuite.TestRunContext;
import com.eviware.soapui.model.testsuite.TestRunner;
import com.eviware.soapui.model.testsuite.TestSuite;
import com.eviware.soapui.model.workspace.Workspace;

/**
 * Global class for monitoring ongoing test runs (both functional and loadtests)
 * 
 * @author Ole.Matzura
 */

public class TestMonitor
{
	private Set<TestMonitorListener> listeners = new HashSet<TestMonitorListener>();
	private InternalWorkspaceListener workspaceListener = new InternalWorkspaceListener();
	private InternalProjectListener projectListener = new InternalProjectListener();
	private InternalTestSuiteListener testSuiteListener = new InternalTestSuiteListener();
	private InternalTestRunListener testRunListener = new InternalTestRunListener();
	private InternalMockRunListener mockRunListener = new InternalMockRunListener();
	private InternalLoadTestRunListener loadTestRunListener = new InternalLoadTestRunListener();
	private Set<TestRunner> runningTestCases = new HashSet<TestRunner>();
	private Set<LoadTestRunner> runningLoadTests = new HashSet<LoadTestRunner>();
	private Set<MockRunner> runningMockServices = new HashSet<MockRunner>();
	private Map<String, TestRunner.Status> runStatusHistory = new HashMap<String, TestRunner.Status>();

	public TestMonitor()
	{
	}

	public TestRunner.Status getLastRunStatus( TestCase testCase )
	{
		return runStatusHistory.get( testCase.getId() );
	}

	protected void notifyLoadTestStarted( LoadTestRunner runner )
	{
		runningLoadTests.add( runner );

		if( listeners.isEmpty() )
			return;

		TestMonitorListener[] l = listeners.toArray( new TestMonitorListener[listeners.size()] );
		for( int c = 0; c < l.length; c++ )
		{
			l[c].loadTestStarted( runner );
		}
	}

	protected void notifyLoadTestFinished( LoadTestRunner runner )
	{
		runningLoadTests.remove( runner.getLoadTest().getTestCase() );

		if( listeners.isEmpty() )
			return;

		TestMonitorListener[] l = listeners.toArray( new TestMonitorListener[listeners.size()] );
		for( int c = 0; c < l.length; c++ )
		{
			l[c].loadTestFinished( runner );
		}
	}

	protected void notifyTestCaseStarted( TestRunner runner )
	{
		if( listeners.isEmpty() )
			return;

		TestMonitorListener[] l = listeners.toArray( new TestMonitorListener[listeners.size()] );
		for( int c = 0; c < l.length; c++ )
		{
			l[c].testCaseStarted( runner );
		}
	}

	protected void notifyTestCaseFinished( TestRunner runner )
	{
		if( listeners.isEmpty() )
			return;

		TestMonitorListener[] l = listeners.toArray( new TestMonitorListener[listeners.size()] );
		for( int c = 0; c < l.length; c++ )
		{
			l[c].testCaseFinished( runner );
		}
	}

	protected void notifyMockServiceStarted( MockRunner runner )
	{
		if( listeners.isEmpty() )
			return;

		TestMonitorListener[] l = listeners.toArray( new TestMonitorListener[listeners.size()] );
		for( int c = 0; c < l.length; c++ )
		{
			l[c].mockServiceStarted( runner );
		}
	}

	protected void notifyMockServiceStopped( MockRunner runner )
	{
		if( listeners.isEmpty() )
			return;

		TestMonitorListener[] l = listeners.toArray( new TestMonitorListener[listeners.size()] );
		for( int c = 0; c < l.length; c++ )
		{
			l[c].mockServiceStopped( runner );
		}
	}

	public boolean hasRunningLoadTest( TestCase testCase )
	{
		Iterator<LoadTestRunner> iterator = runningLoadTests.iterator();
		while( iterator.hasNext() )
		{
			if( iterator.next().getLoadTest().getTestCase() == testCase )
				return true;
		}

		return false;
	}

	public boolean hasRunningTestCase( TestCase testCase )
	{
		Iterator<TestRunner> iterator = runningTestCases.iterator();
		while( iterator.hasNext() )
		{
			if( iterator.next().getTestCase() == testCase )
				return true;
		}

		return false;
	}

	public void addTestMonitorListener( TestMonitorListener listener )
	{
		listeners.add( listener );
	}

	public void removeTestMonitorListener( TestMonitorListener listener )
	{
		listeners.remove( listener );
	}

	private class InternalWorkspaceListener extends WorkspaceListenerAdapter
	{
		public void projectRemoved( Project project )
		{
			unmonitorProject( project );
		}

		public void projectAdded( Project project )
		{
			monitorProject( project );
		}
	}

	private class InternalProjectListener extends ProjectListenerAdapter
	{
		public void testSuiteRemoved( TestSuite testSuite )
		{
			unmonitorTestSuite( testSuite );
		}

		public void testSuiteAdded( TestSuite testSuite )
		{
			monitorTestSuite( testSuite );
		}

		@Override
		public void mockServiceAdded( MockService mockService )
		{
			monitorMockService( mockService );
		}

		@Override
		public void mockServiceRemoved( MockService mockService )
		{
			unmonitorMockService( mockService );
		}
	}

	private class InternalTestSuiteListener extends TestSuiteListenerAdapter
	{
		public void testCaseAdded( TestCase testCase )
		{
			monitorTestCase( testCase );
		}

		public void testCaseRemoved( TestCase testCase )
		{
			unmonitorTestCase( testCase );
		}

		public void loadTestAdded( LoadTest loadTest )
		{
			monitorLoadTest( loadTest );
		}

		public void loadTestRemoved( LoadTest loadTest )
		{
			unmonitorLoadTest( loadTest );
		}
	}

	private class InternalTestRunListener extends TestRunListenerAdapter
	{
		public void afterRun( TestRunner testRunner, TestRunContext runContext )
		{
			runStatusHistory.put( testRunner.getTestCase().getId(), testRunner.getStatus() );

			runningTestCases.remove( testRunner );
			notifyTestCaseFinished( testRunner );
		}

		public void beforeRun( TestRunner testRunner, TestRunContext runContext )
		{
			runningTestCases.add( testRunner );
			notifyTestCaseStarted( testRunner );
		}
	}

	private class InternalMockRunListener extends MockRunListenerAdapter
	{
		@Override
		public void onMockRunnerStart( MockRunner mockRunner )
		{
			runningMockServices.add( mockRunner );
			notifyMockServiceStarted( mockRunner );
		}

		@Override
		public void onMockRunnerStop( MockRunner mockRunner )
		{
			runningMockServices.remove( mockRunner );
			notifyMockServiceStopped( mockRunner );
		}
	}

	private class InternalLoadTestRunListener extends LoadTestRunListenerAdapter
	{
		public void afterLoadTest( LoadTestRunner testRunner, LoadTestRunContext context )
		{
			runningLoadTests.remove( testRunner );
			notifyLoadTestFinished( testRunner );
		}

		public void beforeLoadTest( LoadTestRunner testRunner, LoadTestRunContext context )
		{
			runningLoadTests.add( testRunner );
			notifyLoadTestStarted( testRunner );
		}
	}

	public LoadTestRunner[] getRunningLoadTest()
	{
		return runningLoadTests.toArray( new LoadTestRunner[runningLoadTests.size()] );
	}

	public boolean hasRunningTest( TestCase testCase )
	{
		return hasRunningLoadTest( testCase ) || hasRunningTestCase( testCase );
	}

	public void init( Workspace workspace )
	{
		for( int c = 0; c < workspace.getProjectCount(); c++ )
		{
			Project project = workspace.getProjectAt( c );
			monitorProject( project );
		}

		workspace.addWorkspaceListener( workspaceListener );
	}

	public void monitorProject( Project project )
	{
		project.addProjectListener( projectListener );

		for( int i = 0; i < project.getTestSuiteCount(); i++ )
		{
			monitorTestSuite( project.getTestSuiteAt( i ) );
		}

		for( int i = 0; i < project.getMockServiceCount(); i++ )
		{
			monitorMockService( project.getMockServiceAt( i ) );
		}
	}

	private void monitorMockService( MockService mockService )
	{
		mockService.addMockRunListener( mockRunListener );
	}

	private void monitorTestSuite( TestSuite testSuite )
	{
		testSuite.addTestSuiteListener( testSuiteListener );

		for( int j = 0; j < testSuite.getTestCaseCount(); j++ )
		{
			monitorTestCase( testSuite.getTestCaseAt( j ) );
		}
	}

	private void monitorTestCase( TestCase testCase )
	{
		testCase.addTestRunListener( testRunListener );

		for( int v = 0; v < testCase.getLoadTestCount(); v++ )
		{
			testCase.getLoadTestAt( v ).addLoadTestRunListener( loadTestRunListener );
		}
	}

	private void monitorLoadTest( LoadTest loadTest )
	{
		loadTest.addLoadTestRunListener( loadTestRunListener );
	}

	public void unmonitorProject( Project project )
	{
		project.removeProjectListener( projectListener );

		for( int c = 0; c < project.getTestSuiteCount(); c++ )
		{
			TestSuite testSuite = project.getTestSuiteAt( c );
			unmonitorTestSuite( testSuite );
		}

		for( int c = 0; c < project.getMockServiceCount(); c++ )
		{
			unmonitorMockService( project.getMockServiceAt( c ) );
		}
	}

	private void unmonitorMockService( MockService mockService )
	{
		mockService.removeMockRunListener( mockRunListener );
	}

	private void unmonitorTestSuite( TestSuite testSuite )
	{
		testSuite.removeTestSuiteListener( testSuiteListener );

		for( int j = 0; j < testSuite.getTestCaseCount(); j++ )
		{
			TestCase testCase = testSuite.getTestCaseAt( j );
			unmonitorTestCase( testCase );
		}
	}

	private void unmonitorTestCase( TestCase testCase )
	{
		testCase.removeTestRunListener( testRunListener );

		for( int c = 0; c < testCase.getLoadTestCount(); c++ )
		{
			unmonitorLoadTest( testCase.getLoadTestAt( c ) );
		}
	}

	private void unmonitorLoadTest( LoadTest loadTest )
	{
		loadTest.removeLoadTestRunListener( loadTestRunListener );
	}

	public boolean hasRunningTests()
	{
		return runningLoadTests.size() + runningTestCases.size() > 0;
	}

	public boolean hasRunningMock( MockService mockService )
	{
		for( MockRunner runner : runningMockServices )
			if( runner.getMockService() == mockService )
				return true;

		return false;
	}

	public boolean hasRunningTests( WsdlProject project )
	{
		for( TestRunner testRunner : runningTestCases )
		{
			if( testRunner.getTestCase().getTestSuite().getProject() == project )
				return true;
		}

		for( LoadTestRunner loadTestRunner : runningLoadTests )
		{
			if( loadTestRunner.getLoadTest().getTestCase().getTestSuite().getProject() == project )
				return true;
		}

		// for( MockRunner mockRunner : runningMockServices )
		// {
		// if( mockRunner.getMockService().getProject() == project )
		// return true;
		// }

		return false;
	}

	public void cancelAllTests( String reason )
	{
		for( TestRunner testRunner : runningTestCases )
		{
			testRunner.cancel( reason );
		}

		for( LoadTestRunner loadTestRunner : runningLoadTests )
		{
			loadTestRunner.cancel( reason );
		}

		for( MockRunner mockRunner : runningMockServices )
		{
			mockRunner.stop();
		}
	}

	public TestRunner getTestRunner( WsdlTestCase testCase )
	{
		Iterator<TestRunner> iterator = runningTestCases.iterator();
		while( iterator.hasNext() )
		{
			TestRunner testRunner = iterator.next();
			if( testRunner.getTestCase() == testCase )
				return testRunner;
		}

		return null;
	}
}
