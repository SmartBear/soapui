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
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.model.support.TestRunListenerAdapter;
import com.eviware.soapui.model.testsuite.LoadTestRunner;
import com.eviware.soapui.model.testsuite.TestCaseRunContext;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.TestStepResult;
import com.eviware.soapui.model.testsuite.TestRunner.Status;
import com.eviware.soapui.model.testsuite.TestStepResult.TestStepStatus;
import com.eviware.soapui.monitor.support.TestMonitorListenerAdapter;

/**
 * Class that keeps a JProgressBars state in sync with a TestCase
 * 
 * @author Ole.Matzura
 */

public class ProgressBarTestCaseAdapter
{
	private final JProgressBar progressBar;
	private final WsdlTestCase testCase;
	private InternalTestRunListener internalTestRunListener;
	private InternalTestMonitorListener internalTestMonitorListener;

	public ProgressBarTestCaseAdapter( JProgressBar progressBar, WsdlTestCase testCase )
	{
		this.progressBar = progressBar;
		this.testCase = testCase;

		setLoadTestingState();

		internalTestRunListener = new InternalTestRunListener();
		testCase.addTestRunListener( internalTestRunListener );
		internalTestMonitorListener = new InternalTestMonitorListener();
		SoapUI.getTestMonitor().addTestMonitorListener( internalTestMonitorListener );
	}

	public void release()
	{
		testCase.removeTestRunListener( internalTestRunListener );
		SoapUI.getTestMonitor().removeTestMonitorListener( internalTestMonitorListener );
	}

	private void setLoadTestingState()
	{
		if( SoapUI.getTestMonitor().hasRunningLoadTest( testCase ) )
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

	public class InternalTestRunListener extends TestRunListenerAdapter
	{
		public void beforeRun( TestCaseRunner testRunner, TestCaseRunContext runContext )
		{
			if( progressBar.isIndeterminate() )
				return;

			progressBar.getModel().setMaximum( testRunner.getTestCase().getTestStepCount() );
			progressBar.setForeground( Color.GREEN.darker() );
		}

		public void beforeStep( TestCaseRunner testRunner, TestCaseRunContext runContext, TestStep testStep )
		{
			if( progressBar.isIndeterminate() )
				return;

			if( testStep != null )
			{
				progressBar.setString( testStep.getName() );
				progressBar.setValue( runContext.getCurrentStepIndex() );
			}
		}

		public void afterStep( TestCaseRunner testRunner, TestCaseRunContext runContext, TestStepResult result )
		{
			if( progressBar.isIndeterminate() )
				return;

			if( result.getStatus() == TestStepStatus.FAILED )
			{
				progressBar.setForeground( Color.RED );
			}
			else if( !testCase.getFailTestCaseOnErrors() )
			{
				progressBar.setForeground( Color.GREEN.darker() );
			}

			progressBar.setValue( runContext.getCurrentStepIndex() + 1 );
		}

		public void afterRun( TestCaseRunner testRunner, TestCaseRunContext runContext )
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
				progressBar.setValue( testRunner.getTestCase().getTestStepCount() );

			progressBar.setString( testRunner.getStatus().toString() );
		}
	}
}