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

package com.eviware.soapui.impl.wsdl.loadtest.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.apache.log4j.Logger;

import com.eviware.soapui.model.support.LoadTestRunListenerAdapter;
import com.eviware.soapui.model.support.TestSuiteListenerAdapter;
import com.eviware.soapui.model.testsuite.LoadTest;
import com.eviware.soapui.model.testsuite.LoadTestRunContext;
import com.eviware.soapui.model.testsuite.LoadTestRunner;
import com.eviware.soapui.model.testsuite.TestRunContext;
import com.eviware.soapui.model.testsuite.TestRunner;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.TestStepResult;

/**
 * Collector of samples from a loadtest, exposed as TableModel
 * 
 * @author Ole.Matzura
 */

public class LoadTestSamples extends AbstractTableModel
{
	private final LoadTest loadTest;
	private List<List<LoadTestStepSample[]>> samples = new ArrayList<List<LoadTestStepSample[]>>();
	private List<Long> timestamps = new ArrayList<Long>();
	private InternalLoadTestRunListener loadTestRunListener = new InternalLoadTestRunListener();
	private InternalTestSuiteListener testSuiteListener = new InternalTestSuiteListener();
	private final static Logger log = Logger.getLogger( LoadTestSamples.class );

	public LoadTestSamples( LoadTest loadTest )
	{
		this.loadTest = loadTest;

		loadTest.addLoadTestRunListener( loadTestRunListener );
		loadTest.getTestCase().getTestSuite().addTestSuiteListener( testSuiteListener );
	}

	public int getRowCount()
	{
		return samples.size();
	}

	public void release()
	{
		loadTest.removeLoadTestRunListener( loadTestRunListener );
		loadTest.getTestCase().getTestSuite().removeTestSuiteListener( testSuiteListener );
	}

	public int getColumnCount()
	{
		return loadTest.getTestCase().getTestStepCount() + 1;
	}

	public Object getValueAt( int rowIndex, int columnIndex )
	{
		return columnIndex == 0 ? new Date( timestamps.get( rowIndex ) ) : samples.get( rowIndex ).get( columnIndex - 1 );
	}

	public Class<?> getColumnClass( int columnIndex )
	{
		return columnIndex == 0 ? Date.class : LoadTestStepSample[].class;
	}

	public String getColumnName( int columnIndex )
	{
		return columnIndex == 0 ? "Timestamp" : loadTest.getTestCase().getTestStepAt( columnIndex - 1 ).getName();
	}

	public synchronized void clear()
	{
		int size = samples.size();
		samples.clear();
		timestamps.clear();
		fireTableRowsDeleted( 0, size - 1 );
	}

	private final class InternalTestSuiteListener extends TestSuiteListenerAdapter
	{
		public void loadTestRemoved( LoadTest loadTest )
		{
			if( loadTest.equals( LoadTestSamples.this.loadTest ) )
			{
				loadTest.removeLoadTestRunListener( loadTestRunListener );
			}
		}

		public void testStepAdded( TestStep testStep, int index )
		{
			if( testStep.getTestCase() == loadTest.getTestCase() )
			{
				for( List<LoadTestStepSample[]> values : samples )
				{
					values.add( index, new LoadTestStepSample[0] );
				}
			}
		}

		public void testStepMoved( TestStep testStep, int fromIndex, int offset )
		{
			if( testStep.getTestCase() == loadTest.getTestCase() )
			{
				for( List<LoadTestStepSample[]> values : samples )
				{
					LoadTestStepSample[] s = values.remove( fromIndex );
					values.add( offset, s );
				}
			}
		}

		public void testStepRemoved( TestStep testStep, int index )
		{
			if( testStep.getTestCase() == loadTest.getTestCase() )
			{
				for( List<LoadTestStepSample[]> values : samples )
				{
					values.remove( index );
				}
			}
		}
	}

	private class InternalLoadTestRunListener extends LoadTestRunListenerAdapter
	{
		public void afterTestCase( LoadTestRunner loadTestRunner, LoadTestRunContext context, TestRunner testRunner,
				TestRunContext runContext )
		{
			long timestamp = System.currentTimeMillis();
			List<LoadTestStepSample[]> s = new ArrayList<LoadTestStepSample[]>();
			List<TestStepResult> testResults = testRunner.getResults();

			for( int c = 0; c < loadTest.getTestCase().getTestStepCount(); c++ )
			{
				TestStep testStep = loadTest.getTestCase().getTestStepAt( c );
				List<LoadTestStepSample> results = new ArrayList<LoadTestStepSample>();

				for( int i = 0; i < testResults.size(); i++ )
				{
					TestStepResult stepResult = testResults.get( i );
					if( stepResult == null )
					{
						log.warn( "Result [" + c + "] is null in TestCase [" + testRunner.getTestCase().getName() + "]" );
						continue;
					}

					if( stepResult.getTestStep().equals( testStep ) )
						results.add( new LoadTestStepSample( stepResult ) );
				}

				s.add( results.toArray( new LoadTestStepSample[results.size()] ) );
			}

			synchronized( this )
			{
				samples.add( s );
				timestamps.add( timestamp );
				fireTableRowsInserted( samples.size() - 1, samples.size() - 1 );
			}
		}
	}
}
