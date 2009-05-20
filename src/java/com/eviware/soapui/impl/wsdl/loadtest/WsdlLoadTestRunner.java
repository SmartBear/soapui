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

package com.eviware.soapui.impl.wsdl.loadtest;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.LoadTestConfig;
import com.eviware.soapui.config.LoadTestLimitTypesConfig;
import com.eviware.soapui.config.TestCaseConfig;
import com.eviware.soapui.impl.wsdl.loadtest.log.LoadTestLogMessageEntry;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCaseRunner;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.model.testsuite.LoadTestRunListener;
import com.eviware.soapui.model.testsuite.LoadTestRunner;
import com.eviware.soapui.model.testsuite.TestRunContext;
import com.eviware.soapui.model.testsuite.TestRunListener;
import com.eviware.soapui.model.testsuite.TestRunner;
import com.eviware.soapui.model.testsuite.TestStepResult;
import com.eviware.soapui.settings.HttpSettings;
import com.eviware.soapui.settings.WsdlSettings;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.types.StringToObjectMap;
import com.eviware.x.dialogs.Worker;
import com.eviware.x.dialogs.XProgressDialog;
import com.eviware.x.dialogs.XProgressMonitor;

/**
 * TestRunner for load-tests.
 * 
 * @author Ole.Matzura
 * @todo statistics should be calculated first after all threads have been
 *       started..
 */

public class WsdlLoadTestRunner implements LoadTestRunner
{
	private final WsdlLoadTest loadTest;
	private Set<TestCaseRunner> runners = new HashSet<TestCaseRunner>();
	private long startTime = 0;
	private InternalPropertyChangeListener internalPropertyChangeListener = new InternalPropertyChangeListener();
	private InternalTestRunListener testRunListener = new InternalTestRunListener();
	private long runCount;
	private Status status;
	private WsdlLoadTestContext context;
	private String reason;
	private int threadCount;
	private int threadsWaitingToStart;
	private int startedCount;
	private boolean hasTearedDown;
	private TestCaseStarter testCaseStarter;

	public WsdlLoadTestRunner( WsdlLoadTest test )
	{
		this.loadTest = test;
		status = Status.INITIALIZED;
	}

	public Status getStatus()
	{
		return status;
	}

	void start()
	{
		loadTest.getTestCase().beforeSave();
		startTime = System.currentTimeMillis();

		runners.clear();
		runCount = 0;
		threadCount = 0;
		threadsWaitingToStart = 0;
		startedCount = 0;
		context = new WsdlLoadTestContext( this );

		try
		{
			loadTest.runSetupScript( context, this );
		}
		catch( Exception e1 )
		{
			SoapUI.logError( e1 );
		}

		status = Status.RUNNING;

		for( LoadTestRunListener listener : loadTest.getLoadTestRunListeners() )
		{
			try
			{
				listener.beforeLoadTest( this, context );
			}
			catch( Throwable e )
			{
				SoapUI.logError( e );
			}
		}

		loadTest.addPropertyChangeListener( WsdlLoadTest.THREADCOUNT_PROPERTY, internalPropertyChangeListener );

		loadTest.getLoadTestLog()
				.addEntry( new LoadTestLogMessageEntry( "LoadTest started at " + new Date( startTime ) ) );

		int startDelay = loadTest.getStartDelay();
		if( startDelay >= 0 )
		{
			XProgressDialog progressDialog = UISupport.getDialogs().createProgressDialog( "Starting threads",
					( int )loadTest.getThreadCount(), "", true );
			try
			{
				testCaseStarter = new TestCaseStarter();
				progressDialog.run( testCaseStarter );
			}
			catch( Exception e )
			{
				SoapUI.logError( e );
			}
		}
		else
		{
			List<WsdlTestCase> testCases = new ArrayList<WsdlTestCase>();
			for( int c = 0; c < loadTest.getThreadCount(); c++ )
				testCases.add( createTestCase() );

			for( int c = 0; c < loadTest.getThreadCount(); c++ )
			{
				if( loadTest.getLimitType() == LoadTestLimitTypesConfig.COUNT && runners.size() >= loadTest.getTestLimit() )
					break;

				startTestCase( testCases.get( c ) );
			}
		}

		if( status == Status.RUNNING )
		{
			for( LoadTestRunListener listener : loadTest.getLoadTestRunListeners() )
			{
				listener.loadTestStarted( this, context );
			}

			startStrategyThread();
		}
		else
		{
			for( LoadTestRunListener listener : loadTest.getLoadTestRunListeners() )
			{
				listener.afterLoadTest( this, context );
			}

			tearDown();
		}
	}

	/**
	 * Starts thread the calls the current strategy to recalculate
	 */

	private void startStrategyThread()
	{
		new Thread( new Runnable()
		{
			public void run()
			{
				while( getStatus() == Status.RUNNING )
				{
					try
					{
						loadTest.getLoadStrategy().recalculate( WsdlLoadTestRunner.this, context );

						long strategyInterval = loadTest.getStrategyInterval();
						if( strategyInterval < 1 )
							strategyInterval = WsdlLoadTest.DEFAULT_STRATEGY_INTERVAL;

						Thread.sleep( strategyInterval );
					}
					catch( InterruptedException e )
					{
						e.printStackTrace();
					}
				}
			}
		} ).start();
	}

	private TestCaseRunner startTestCase( WsdlTestCase testCase )
	{
		TestCaseRunner testCaseRunner = new TestCaseRunner( testCase, threadCount++ );

		SoapUI.getThreadPool().submit( testCaseRunner );
		runners.add( testCaseRunner );
		return testCaseRunner;
	}

	public synchronized void cancel( String reason )
	{
		if( status != Status.RUNNING )
			return;

		this.reason = reason;
		status = Status.CANCELED;

		if( testCaseStarter != null )
			testCaseStarter.stop();

		TestCaseRunner[] r = runners.toArray( new TestCaseRunner[runners.size()] );

		for( TestCaseRunner runner : r )
		{
			runner.cancel( reason, true );
		}

		String msg = "LoadTest [" + loadTest.getName() + "] canceled";
		if( reason != null )
			msg += "; " + reason;

		loadTest.getLoadTestLog().addEntry( new LoadTestLogMessageEntry( msg ) );

		for( LoadTestRunListener listener : loadTest.getLoadTestRunListeners() )
		{
			listener.loadTestStopped( this, context );
		}

		tearDown();
	}

	public synchronized void fail( String reason )
	{
		if( status != Status.RUNNING )
			return;

		this.reason = reason;
		status = Status.FAILED;

		if( testCaseStarter != null )
			testCaseStarter.stop();

		String msg = "LoadTest [" + loadTest.getName() + "] failed";
		if( reason != null )
			msg += "; " + reason;

		loadTest.getLoadTestLog().addEntry( new LoadTestLogMessageEntry( msg ) );

		for( LoadTestRunListener listener : loadTest.getLoadTestRunListeners() )
		{
			listener.loadTestStopped( this, context );
		}

		TestCaseRunner[] r = runners.toArray( new TestCaseRunner[runners.size()] );

		for( TestCaseRunner runner : r )
		{
			runner.cancel( reason, true );
		}

		tearDown();
	}

	private synchronized void tearDown()
	{
		if( hasTearedDown )
			return;

		try
		{
			loadTest.runTearDownScript( context, this );
		}
		catch( Exception e1 )
		{
			SoapUI.logError( e1 );
		}
		;

		hasTearedDown = true;
	}

	public void waitUntilFinished()
	{
		while( runners.size() > 0 || threadsWaitingToStart > 0 )
		{
			try
			{
				Thread.sleep( 200 );
			}
			catch( InterruptedException e )
			{
				SoapUI.logError( e );
			}
		}
	}

	public void finishTestCase( String reason, WsdlTestCase testCase )
	{
		for( TestCaseRunner runner : runners )
		{
			if( runner.getTestCase() == testCase )
			{
				runner.cancel( reason, false );
				break;
			}
		}
	}

	public synchronized void finishRunner( TestCaseRunner runner )
	{
		if( !runners.contains( runner ) )
		{
			throw new RuntimeException( "Trying to finish unknown runner.. " );
		}

		runners.remove( runner );
		if( runners.size() == 0 && ( getProgress() >= 1 || status != Status.RUNNING ) )
		{
			loadTest.removePropertyChangeListener( WsdlLoadTest.THREADCOUNT_PROPERTY, internalPropertyChangeListener );

			if( testCaseStarter != null )
				testCaseStarter.stop();
			
			if( status == Status.RUNNING )
				status = Status.FINISHED;

			loadTest.getLoadTestLog().addEntry(
					new LoadTestLogMessageEntry( "LoadTest ended at " + new Date( System.currentTimeMillis() ) ) );

			for( LoadTestRunListener listener : loadTest.getLoadTestRunListeners() )
			{
				listener.afterLoadTest( this, context );
			}

			tearDown();

			context.clear();
		}
	}

	public int getRunningThreadCount()
	{
		return runners.size();
	}

	public float getProgress()
	{
		long testLimit = loadTest.getTestLimit();
		if( testLimit == 0 )
			return -1;

		if( loadTest.getLimitType() == LoadTestLimitTypesConfig.COUNT )
			return ( float )runCount / ( float )testLimit;

		if( loadTest.getLimitType() == LoadTestLimitTypesConfig.TIME )
			return ( float )getTimeTaken() / ( float )( testLimit * 1000 );

		return -1;
	}

	private synchronized boolean afterRun( TestCaseRunner runner )
	{
		if( status != Status.RUNNING )
			return false;

		runCount++ ;

		if( loadTest.getTestLimit() < 1 )
			return true;

		if( loadTest.getLimitType() == LoadTestLimitTypesConfig.COUNT )
			return runCount + runners.size() + threadsWaitingToStart <= loadTest.getTestLimit();

		if( loadTest.getLimitType() == LoadTestLimitTypesConfig.TIME )
			return getTimeTaken() < loadTest.getTestLimit() * 1000;

		return true;
	}

	private final class TestCaseStarter extends Worker.WorkerAdapter
	{
		private List<WsdlTestCase> testCases = new ArrayList<WsdlTestCase>();
		private boolean canceled;

		public Object construct( XProgressMonitor monitor )
		{
			int startDelay = loadTest.getStartDelay();

			for( int c = 0; c < loadTest.getThreadCount() && !canceled; c++ )
			{
				monitor.setProgress( 1, "Creating Virtual User " + ( c + 1 ) );
				testCases.add( createTestCase() );
			}

			threadsWaitingToStart = testCases.size();
			int cnt = 0;
			while( !testCases.isEmpty() && !canceled )
			{
				if( startDelay > 0 )
				{
					try
					{
						Thread.sleep( startDelay );
					}
					catch( InterruptedException e )
					{
						SoapUI.logError( e );
					}
				}

				if( status != Status.RUNNING
						|| getProgress() >= 1
						|| ( loadTest.getLimitType() == LoadTestLimitTypesConfig.COUNT && runners.size() >= loadTest
								.getTestLimit() ) )
				{
					while( !testCases.isEmpty() )
						testCases.remove( 0 ).release();

					threadsWaitingToStart = 0;
					break;
				}
				
				// could have been canceled..
				if( !testCases.isEmpty() )
				{
					startTestCase( testCases.remove( 0 ) );
					monitor.setProgress( 1, "Started thread " + ( ++cnt ) );
					threadsWaitingToStart-- ;
				}
			}

			return null;
		}

		public boolean onCancel()
		{
			cancel( "Stopped from UI during start-up" );
			stop();

			return false;
		}

		public void stop()
		{
			if( !canceled )
			{
				canceled = true;
				while( !testCases.isEmpty() )
					testCases.remove( 0 ).release();

				threadsWaitingToStart = 0;
			}
		}
	}

	public class TestCaseRunner implements Runnable
	{
		private final WsdlTestCase testCase;
		private boolean canceled;
		private long runCount;
		private WsdlTestCaseRunner runner;
		private final int threadIndex;

		public TestCaseRunner( WsdlTestCase testCase, int threadIndex )
		{
			this.testCase = testCase;
			this.threadIndex = threadIndex;
		}

		public void run()
		{
			try
			{
				Thread.currentThread().setName(
						testCase.getName() + " - " + loadTest.getName() + " - ThreadIndex " + threadIndex );
				runner = new WsdlTestCaseRunner( testCase, new StringToObjectMap() );

				while( !canceled )
				{
					try
					{
						runner.getRunContext().reset();
						runner.getRunContext().setProperty( TestRunContext.THREAD_INDEX, threadIndex );
						runner.getRunContext().setProperty( TestRunContext.RUN_COUNT, runCount );
						runner.getRunContext().setProperty( TestRunContext.LOAD_TEST_RUNNER, WsdlLoadTestRunner.this );
						runner.getRunContext().setProperty( TestRunContext.LOAD_TEST_CONTEXT, context );
						synchronized( this )
						{
							runner.getRunContext().setProperty( TestRunContext.TOTAL_RUN_COUNT, startedCount++ );
						}

						runner.run();
					}
					catch( Throwable e )
					{
						System.err.println( "Error running testcase: " + e );
						SoapUI.logError( e );
					}

					runCount++ ;

					if( !afterRun( this ) )
						break;
				}
			}
			finally
			{
				finishRunner( this );
				testCase.release();
				testCase.removeTestRunListener( testRunListener );
			}
		}

		public void cancel( String reason, boolean cancelRunner )
		{
			if( runner != null && cancelRunner )
				runner.cancel( reason );

			this.canceled = true;
		}

		public boolean isCanceled()
		{
			return canceled;
		}

		public long getRunCount()
		{
			return runCount;
		}

		public WsdlTestCase getTestCase()
		{
			return testCase;
		}
	}

	public WsdlLoadTest getLoadTest()
	{
		return loadTest;
	}

	public class InternalPropertyChangeListener implements PropertyChangeListener
	{
		public void propertyChange( PropertyChangeEvent evt )
		{
			updateThreadCount();
		}
	}

	public synchronized void updateThreadCount()
	{
		if( status != Status.RUNNING )
			return;

		long newCount = loadTest.getThreadCount();

		// get list of active runners
		Iterator<TestCaseRunner> iterator = runners.iterator();
		List<TestCaseRunner> activeRunners = new ArrayList<TestCaseRunner>();
		while( iterator.hasNext() )
		{
			TestCaseRunner runner = iterator.next();
			if( !runner.isCanceled() )
				activeRunners.add( runner );
		}

		long diff = newCount - activeRunners.size();

		if( diff == 0 )
			return;

		// cancel runners if thread count has been decreased
		if( diff < 0 && loadTest.getCancelExcessiveThreads() )
		{
			diff = Math.abs( diff );
			for( int c = 0; c < diff && c < activeRunners.size(); c++ )
			{
				activeRunners.get( c ).cancel( "excessive thread", false );
			}
		}
		// start new runners if thread count has been increased
		else if( diff > 0 )
		{
			for( int c = 0; c < diff; c++ )
			{
				int startDelay = loadTest.getStartDelay();
				if( startDelay > 0 )
				{
					try
					{
						Thread.sleep( startDelay );
					}
					catch( InterruptedException e )
					{
						SoapUI.logError( e );
					}
				}

				if( status == Status.RUNNING )
					startTestCase( createTestCase() );
			}
		}
	}

	/**
	 * Creates a copy of the underlying WsdlTestCase with all LoadTests removed
	 * and configured for LoadTesting
	 */

	private WsdlTestCase createTestCase()
	{
		WsdlTestCase testCase = loadTest.getTestCase();

		// clone config and remove and loadtests
		TestCaseConfig config = ( TestCaseConfig )testCase.getConfig().copy();
		config.setLoadTestArray( new LoadTestConfig[0] );

		// clone entire testCase
		WsdlTestCase tc = new WsdlTestCase( testCase.getTestSuite(), config, true );
		tc.afterLoad();
		tc.addTestRunListener( testRunListener );
		Settings settings = tc.getSettings();
		settings.setBoolean( HttpSettings.INCLUDE_REQUEST_IN_TIME_TAKEN, loadTest.getSettings().getBoolean(
				HttpSettings.INCLUDE_REQUEST_IN_TIME_TAKEN ) );
		settings.setBoolean( HttpSettings.INCLUDE_RESPONSE_IN_TIME_TAKEN, loadTest.getSettings().getBoolean(
				HttpSettings.INCLUDE_RESPONSE_IN_TIME_TAKEN ) );
		settings.setBoolean( HttpSettings.CLOSE_CONNECTIONS, loadTest.getSettings().getBoolean(
				HttpSettings.CLOSE_CONNECTIONS ) );

		// disable default pretty-printing since it takes time
		settings.setBoolean( WsdlSettings.PRETTY_PRINT_RESPONSE_MESSAGES, false );

		// dont discard.. the WsdlLoadTests internal listener will discard after
		// asserting..
		tc.setDiscardOkResults( false );
		tc.setMaxResults( 0 );
		return tc;
	}

	public String getReason()
	{
		return reason;
	}

	public long getTimeTaken()
	{
		return System.currentTimeMillis() - startTime;
	}

	private class InternalTestRunListener implements TestRunListener
	{
		public void beforeRun( TestRunner testRunner, TestRunContext runContext )
		{
			if( getProgress() > 1 && loadTest.getCancelOnReachedLimit() )
			{
				testRunner.cancel( "LoadTest Limit reached" );
			}
			else
				for( LoadTestRunListener listener : loadTest.getLoadTestRunListeners() )
				{
					listener.beforeTestCase( WsdlLoadTestRunner.this, context, testRunner, runContext );
				}
		}

		public void beforeStep( TestRunner testRunner, TestRunContext runContext )
		{
			if( getProgress() > 1 && loadTest.getCancelOnReachedLimit() )
			{
				testRunner.cancel( "LoadTest Limit reached" );
			}
			else if( runContext.getCurrentStep() != null )
			{
				for( LoadTestRunListener listener : loadTest.getLoadTestRunListeners() )
				{
					listener.beforeTestStep( WsdlLoadTestRunner.this, context, testRunner, runContext, runContext
							.getCurrentStep() );
				}
			}
		}

		public void afterStep( TestRunner testRunner, TestRunContext runContext, TestStepResult result )
		{
			for( LoadTestRunListener listener : loadTest.getLoadTestRunListeners() )
			{
				listener.afterTestStep( WsdlLoadTestRunner.this, context, testRunner, runContext, result );
			}
		}

		public void afterRun( TestRunner testRunner, TestRunContext runContext )
		{
			for( LoadTestRunListener listener : loadTest.getLoadTestRunListeners() )
			{
				listener.afterTestCase( WsdlLoadTestRunner.this, context, testRunner, runContext );
			}
		}
	}
}
