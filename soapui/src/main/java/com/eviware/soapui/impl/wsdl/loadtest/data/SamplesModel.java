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

package com.eviware.soapui.impl.wsdl.loadtest.data;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

import org.apache.log4j.Logger;

import com.eviware.soapui.model.support.LoadTestRunListenerAdapter;
import com.eviware.soapui.model.support.TestSuiteListenerAdapter;
import com.eviware.soapui.model.testsuite.LoadTest;
import com.eviware.soapui.model.testsuite.LoadTestRunContext;
import com.eviware.soapui.model.testsuite.LoadTestRunner;
import com.eviware.soapui.model.testsuite.TestCase;
import com.eviware.soapui.model.testsuite.TestCaseRunContext;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.TestStepResult;

/**
 * TableModel holding loadtest samples
 */

public class SamplesModel extends AbstractTableModel
{
	private final LoadTest loadTest;
	private List<TestSample[]> samples = new ArrayList<TestSample[]>();
	private InternalTestRunListener testRunListener;
	private InternalTestSuiteListener testSuiteListener;
	private InternalPropertyChangeListener propertyChangeListener;
	private TestCase testCase;
	private final static Logger log = Logger.getLogger( SamplesModel.class );

	public SamplesModel( LoadTest loadTest )
	{
		this.loadTest = loadTest;

		testRunListener = new InternalTestRunListener();
		testSuiteListener = new InternalTestSuiteListener();
		propertyChangeListener = new InternalPropertyChangeListener();

		testCase = loadTest.getTestCase();
		loadTest.addLoadTestRunListener( testRunListener );
		testCase.getTestSuite().addTestSuiteListener( testSuiteListener );

		for( TestStep testStep : testCase.getTestStepList() )
		{
			testStep.addPropertyChangeListener( TestStep.NAME_PROPERTY, propertyChangeListener );
		}
	}

	public int getRowCount()
	{
		return samples.size();
	}

	public int getColumnCount()
	{
		return testCase.getTestStepCount();
	}

	public Object getValueAt( int rowIndex, int columnIndex )
	{
		TestSample[] testSamples = samples.get( rowIndex );
		return testSamples == null ? "discarded" : testSamples[columnIndex];
	}

	public void addSamples( TestSample[] newSamples )
	{
		if( newSamples.length != getColumnCount() )
			throw new RuntimeException( "Invalid number of samples reported: " + newSamples.length + ", expected "
					+ getColumnCount() );

		samples.add( newSamples );

		fireTableRowsInserted( samples.size() - 1, samples.size() - 1 );
	}

	public Class<?> getColumnClass( int columnIndex )
	{
		return TestSample.class;
	}

	public String getColumnName( int column )
	{
		return testCase.getTestStepAt( column ).getName();
	}

	public void clear()
	{
		int size = samples.size();
		if( size > 0 )
		{
			samples.clear();
			fireTableRowsDeleted( 0, size );
		}
	}

	/**
	 * Listener for collecting samples
	 * 
	 * @author Ole.Matzura
	 */

	private class InternalTestRunListener extends LoadTestRunListenerAdapter
	{
		public void afterTestCase( LoadTestRunner loadTestRunner, LoadTestRunContext context, TestCaseRunner testRunner,
				TestCaseRunContext runContext )
		{
			Map<TestStep, TestSample> samplesMap = new HashMap<TestStep, TestSample>();
			List<TestStepResult> results = testRunner.getResults();

			for( int c = 0; c < results.size(); c++ )
			{
				TestStepResult result = results.get( c );
				if( result == null )
				{
					log.warn( "Result [" + c + "] is null in TestCase [" + testCase.getName() + "]" );
					continue;
				}

				TestStep testStep = result.getTestStep();

				if( !samplesMap.containsKey( testStep ) )
				{
					samplesMap.put( testStep, new TestSample( testStep ) );
				}

				samplesMap.get( testStep ).addTestStepResult( result );
			}

			TestCase testCase = loadTest.getTestCase();

			TestSample[] samples = new TestSample[testCase.getTestStepCount()];
			for( int c = 0; c < samples.length; c++ )
			{
				samples[c] = samplesMap.get( testCase.getTestStepAt( c ) );
			}

			addSamples( samples );
		}
	}

	public List<TestSample[]> getSamples()
	{
		return samples;
	}

	public void release()
	{
		loadTest.removeLoadTestRunListener( testRunListener );
		loadTest.getTestCase().getTestSuite().removeTestSuiteListener( testSuiteListener );

		for( TestStep testStep : loadTest.getTestCase().getTestStepList() )
		{
			testStep.removePropertyChangeListener( propertyChangeListener );
		}
	}

	/**
	 * Holder for a TestSample
	 * 
	 * @author ole.matzura
	 */

	public static final class TestSample
	{
		private final TestStep testStep;
		private List<TestStepResult> results;

		public TestSample( TestStep testStep )
		{
			this.testStep = testStep;
		}

		public void addTestStepResult( TestStepResult result )
		{
			if( result.getTestStep() != testStep )
				throw new RuntimeException( "Trying to add sample for false testStep [" + result.getTestStep().getName()
						+ "], " + "expecting [" + testStep.getName() + "]" );

			if( results == null )
				results = new ArrayList<TestStepResult>();

			results.add( result );
		}

		public List<TestStepResult> getResults()
		{
			return results;
		}

		public int getResultCount()
		{
			return results == null ? 0 : results.size();
		}

		public long getResultAverage()
		{
			if( results == null )
				return 0;

			if( results.size() == 1 )
				return results.get( 0 ).getTimeTaken();

			long sum = 0;
			for( TestStepResult result : results )
				sum += result.getTimeTaken();

			return sum / results.size();
		}
	}

	private class InternalTestSuiteListener extends TestSuiteListenerAdapter
	{
		public void testStepAdded( TestStep testStep, int index )
		{
			if( testStep.getTestCase() == testCase )
			{
				testStep.addPropertyChangeListener( TestStep.NAME_PROPERTY, propertyChangeListener );

				// insert null entry in existing samples
				for( int i = 0; i < samples.size(); i++ )
				{
					TestSample[] testSamples = samples.get( i );
					TestSample[] newSamples = new TestSample[testSamples.length + 1];
					for( int c = 0; c < testSamples.length; c++ )
					{
						if( c < index )
						{
							newSamples[c] = testSamples[c];
						}
						else
						{
							newSamples[c + 1] = testSamples[c];
						}
					}

					samples.set( i, newSamples );
				}

				fireTableStructureChanged();
			}
		}

		public void testStepRemoved( TestStep testStep, int index )
		{
			if( testStep.getTestCase() == testCase )
			{
				testStep.removePropertyChangeListener( propertyChangeListener );

				// remove from samples
				for( int i = 0; i < samples.size(); i++ )
				{
					TestSample[] testSamples = samples.get( i );
					TestSample[] newSamples = new TestSample[testSamples.length - 1];
					for( int c = 0; c < testSamples.length; c++ )
					{
						if( c < index )
						{
							newSamples[c] = testSamples[c];
						}
						else if( c > index )
						{
							newSamples[c - 1] = testSamples[c];
						}
					}

					samples.set( i, newSamples );
				}

				fireTableStructureChanged();
			}
		}
	}

	private class InternalPropertyChangeListener implements PropertyChangeListener
	{
		public void propertyChange( PropertyChangeEvent evt )
		{
			fireTableStructureChanged();
		}
	}

}
