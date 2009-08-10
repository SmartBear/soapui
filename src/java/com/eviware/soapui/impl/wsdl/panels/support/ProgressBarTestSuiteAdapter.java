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

package com.eviware.soapui.impl.wsdl.panels.support;

import java.awt.Color;

import javax.swing.JProgressBar;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.model.testsuite.LoadTestRunner;
import com.eviware.soapui.model.testsuite.TestCase;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.TestRunner;
import com.eviware.soapui.model.testsuite.TestSuiteRunContext;
import com.eviware.soapui.model.testsuite.TestSuiteRunListener;
import com.eviware.soapui.model.testsuite.TestSuiteRunner;
import com.eviware.soapui.model.testsuite.TestRunner.Status;
import com.eviware.soapui.monitor.support.TestMonitorListenerAdapter;

/**
 * Class that keeps a JProgressBars state in sync with a TestCase
 * 
 * @author Ole.Matzura
 */

public class ProgressBarTestSuiteAdapter
{
	private final JProgressBar progressBar;
	private final WsdlTestSuite testSuite;
	private InternalTestSuiteRunListener internalTestRunListener;
	private InternalTestMonitorListener internalTestMonitorListener;

	public ProgressBarTestSuiteAdapter( JProgressBar progressBar, WsdlTestSuite testSuite )
	{
		this.progressBar = progressBar;
		this.testSuite = testSuite;

		setLoadTestingState();

		internalTestRunListener = new InternalTestSuiteRunListener();
		testSuite.addTestSuiteRunListener( internalTestRunListener );
		internalTestMonitorListener = new InternalTestMonitorListener();
		SoapUI.getTestMonitor().addTestMonitorListener( internalTestMonitorListener );
	}

	public void release()
	{
		testSuite.removeTestSuiteRunListener( internalTestRunListener );
		SoapUI.getTestMonitor().removeTestMonitorListener( internalTestMonitorListener );
	}

	private void setLoadTestingState()
	{
		if( SoapUI.getTestMonitor().hasRunningLoadTest( testSuite ) )
		{
			progressBar.setIndeterminate( true );
			progressBar.setString( "loadTesting" );
		}
		else
		{
			progressBar.setIndeterminate( false );
			progressBar.setString( "" );
		}
	}

	private class InternalTestMonitorListener extends TestMonitorListenerAdapter
	{
		public void loadTestStarted( LoadTestRunner loadTestRunner )
		{
			setLoadTestingState();
		}

		public void loadTestFinished( LoadTestRunner loadTestRunner )
		{
			setLoadTestingState();
		}
	}

	public class InternalTestSuiteRunListener implements TestSuiteRunListener
	{
		public void beforeRun( TestSuiteRunner testRunner, TestSuiteRunContext runContext )
		{
			if( progressBar.isIndeterminate() )
				return;

			progressBar.getModel().setMaximum( testRunner.getTestSuite().getTestCaseCount() );
			progressBar.setForeground( Color.GREEN.darker() );
		}

		public void beforeTestCase( TestSuiteRunner testRunner, TestSuiteRunContext runContext, TestCase testCase )
		{
			if( progressBar.isIndeterminate() )
				return;

			progressBar.setString( testCase.getName() );
			progressBar.setValue( testRunner.getResults().size() );
		}

		public void afterTestCase( TestSuiteRunner testRunner, TestSuiteRunContext runContext, TestCaseRunner result )
		{
			if( progressBar.isIndeterminate() )
				return;

			if( result.getStatus() == TestRunner.Status.FAILED )
			{
				progressBar.setForeground( Color.RED );
			}
			else if( !testSuite.isFailOnErrors() )
			{
				progressBar.setForeground( Color.GREEN.darker() );
			}

			progressBar.setValue( testRunner.getResults().size() + 1 );
		}

		public void afterRun( TestSuiteRunner testRunner, TestSuiteRunContext runContext )
		{
			if( testRunner.getStatus() == Status.FAILED )
			{
				progressBar.setForeground( Color.RED );
			}
			else if( testRunner.getStatus() == Status.FINISHED )
			{
				progressBar.setForeground( Color.GREEN.darker() );
			}

			if( progressBar.isIndeterminate() )
				return;

			if( testRunner.getStatus() == TestCaseRunner.Status.FINISHED )
				progressBar.setValue( testRunner.getTestSuite().getTestCaseCount() );

			progressBar.setString( testRunner.getStatus().toString() );
		}
	}
}