package com.eviware.soapui.impl.wsdl.testcase;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.model.propertyexpansion.DefaultPropertyExpansionContext;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContext;
import com.eviware.soapui.model.support.TestRunListenerAdapter;
import com.eviware.soapui.model.testsuite.TestCase;
import com.eviware.soapui.model.testsuite.TestRunContext;
import com.eviware.soapui.model.testsuite.TestRunner;
import com.eviware.soapui.model.testsuite.TestSuite.TestSuiteRunType;

public class WsdlTestScenario implements Runnable
{
	private boolean canceled;
	private List<TestRunner> runners = new ArrayList<TestRunner>();
	private InternalTestRunListener internalTestRunListener = new InternalTestRunListener();
	private PropertyExpansionContext context;
	private List<TestCase> testCaseList = new ArrayList<TestCase>();
	private TestSuiteRunType runType;
	private boolean skipTestCasesWithRunningLoadTest = true;
	private boolean abortOnFail = false;
	private Set<TestScenarioListener> listeners = new HashSet<TestScenarioListener>();

	public WsdlTestScenario( TestSuiteRunType runType )
	{
		this.runType = runType;
	}

	public void cancel()
	{
		canceled = true;

		for( TestRunner runner : runners )
		{
			runner.cancel( "Canceled from TestSuite" );
		}
	}

	public void addTestCase( TestCase testCase )
	{
		testCaseList.add( testCase );
	}

	public void run()
	{
		canceled = false;

		context = createContext();

		beforeRun( context );

		try
		{
			for( int c = 0; c < testCaseList.size(); c++ )
			{
				TestCase testCase = testCaseList.get( c );
				if( testCase.isDisabled()
						|| ( skipTestCasesWithRunningLoadTest && SoapUI.getTestMonitor().hasRunningLoadTest( testCase ) ) )
				{
					continue;
				}

				if( runType == TestSuiteRunType.PARALLEL )
				{
					testCase.addTestRunListener( internalTestRunListener );
				}

				beforeTestCase( testCase );

				TestRunner runner = testCase.run( context.getProperties(), true );
				runners.add( runner );

				if( runType == TestSuiteRunType.SEQUENTIAL )
				{
					runner.waitUntilFinished();
					afterTestCase( testCase, runner );
					runners.remove( runner );

					if( abortOnFail && runner.getStatus() == TestRunner.Status.FAILED )
						break;
				}

				if( canceled )
					break;
			}
		}
		catch( Exception e )
		{
			SoapUI.logError( e );
		}
		finally
		{
			if( runners.isEmpty() )
				afterRun( context );
		}
	}

	protected PropertyExpansionContext createContext()
	{
		return new DefaultPropertyExpansionContext( null );
	}

	protected void afterTestCase( TestCase testCase, TestRunner runner )
	{
		for( TestScenarioListener listener : listeners.toArray( new TestScenarioListener[listeners.size()] ) )
		{
			listener.afterTestCase( testCase, runner );
		}
	}

	protected void beforeTestCase( TestCase testCase )
	{
		for( TestScenarioListener listener : listeners.toArray( new TestScenarioListener[listeners.size()] ) )
		{
			listener.beforeTestCase( testCase );
		}
	}

	protected void beforeRun( PropertyExpansionContext context )
	{
		for( TestScenarioListener listener : listeners.toArray( new TestScenarioListener[listeners.size()] ) )
		{
			listener.beforeRun( context );
		}
	}

	protected void afterRun( PropertyExpansionContext context )
	{
		for( TestScenarioListener listener : listeners.toArray( new TestScenarioListener[listeners.size()] ) )
		{
			listener.afterRun( context );
		}
	}

	/**
	 * Waits for running tests to finish when running in parallel
	 */

	private class InternalTestRunListener extends TestRunListenerAdapter
	{
		public void afterRun( TestRunner testRunner, TestRunContext runContext )
		{
			runners.remove( testRunner );

			WsdlTestCase testCase = ( WsdlTestCase )testRunner.getTestCase();

			testRunner.getTestCase().removeTestRunListener( this );
			afterTestCase( testCase, testRunner );

			if( runners.isEmpty() )
				WsdlTestScenario.this.afterRun( context );

			if( testRunner.getStatus() == TestRunner.Status.FAILED )
			{
				testCaseFailed( testCase );

				if( abortOnFail && !canceled )
					cancel();
			}
		}
	}

	public void testCaseFailed( WsdlTestCase testCase )
	{
	}

	public boolean isAbortOnFail()
	{
		return abortOnFail;
	}

	public void setAbortOnFail( boolean abortOnFail )
	{
		this.abortOnFail = abortOnFail;
	}

	public TestSuiteRunType getRunType()
	{
		return runType;
	}

	public void setRunType( TestSuiteRunType runType )
	{
		this.runType = runType;
	}

	public boolean isSkipTestCasesWithRunningLoadTest()
	{
		return skipTestCasesWithRunningLoadTest;
	}

	public void setSkipTestCasesWithRunningLoadTest( boolean skipTestCasesWithRunningLoadTest )
	{
		this.skipTestCasesWithRunningLoadTest = skipTestCasesWithRunningLoadTest;
	}

	public boolean isCanceled()
	{
		return canceled;
	}

	public List<TestRunner> getRunners()
	{
		return runners;
	}

	public void addTestScenarioListener( TestScenarioListener listener )
	{
		listeners.add( listener );
	}

	public void removeTestScenarioListener( TestScenarioListener listener )
	{
		listeners.remove( listener );
	}

	protected List<TestCase> getTestCaseList()
	{
		return testCaseList;
	}

	public int getTestCaseCount()
	{
		return testCaseList.size();
	}

	public TestCase getTestCaseAt( int index )
	{
		return testCaseList.get( index );
	}

	public void removeTestCase( TestCase testCase )
	{
		testCaseList.remove( testCase );
	}

	public void removeAllTestCases()
	{
		testCaseList.clear();
	}
}
