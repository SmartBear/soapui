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

package com.eviware.soapui.security.support;

import java.awt.Color;

import javax.swing.JProgressBar;

import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.TestStepResult;
import com.eviware.soapui.model.testsuite.TestStepResult.TestStepStatus;
import com.eviware.soapui.security.SecurityCheckRequestResult;
import com.eviware.soapui.security.SecurityCheckResult;
import com.eviware.soapui.security.SecurityTest;
import com.eviware.soapui.security.SecurityTestRunContext;
import com.eviware.soapui.security.SecurityTestRunner;
import com.eviware.soapui.security.SecurityCheckRequestResult.SecurityCheckStatus;
import com.eviware.soapui.security.check.AbstractSecurityCheck;

/**
 * Class that keeps a JProgressBars state in sync with a TestCase
 * 
 * @author Ole.Matzura
 */

public class ProgressBarSecurityTestStepAdapter
{
	private final JProgressBar progressBar;
	private final TestStep testStep;
	private final SecurityTest securityTest;
	private InternalTestRunListener internalTestRunListener;

	// private InternalTestMonitorListener internalTestMonitorListener;

	public ProgressBarSecurityTestStepAdapter( JProgressBar progressBar, SecurityTest securityTest, TestStep testStep )
	{
		this.progressBar = progressBar;
		this.testStep = testStep;
		this.securityTest = securityTest;

		// setLoadTestingState();

		internalTestRunListener = new InternalTestRunListener();
		securityTest.addTestStepRunListener( testStep, internalTestRunListener );
		// internalTestMonitorListener = new InternalTestMonitorListener();
		// SoapUI.getTestMonitor().addTestMonitorListener(
		// internalTestMonitorListener );
	}

	public void release()
	{
		securityTest.removeTestStepRunListener( testStep, internalTestRunListener );
		// SoapUI.getTestMonitor().removeTestMonitorListener(
		// internalTestMonitorListener );
	}

	// private void setLoadTestingState()
	// {
	// if( SoapUI.getTestMonitor().hasRunningLoadTest( securityTest ) )
	// {
	// progressBar.setIndeterminate( true );
	// progressBar.setString( "loadTesting" );
	// }
	// else
	// {
	// progressBar.setIndeterminate( false );
	// progressBar.setString( "" );
	// }
	// }

	// private class InternalTestMonitorListener extends
	// TestMonitorListenerAdapter
	// {
	// public void loadTestStarted( LoadTestRunner loadTestRunner )
	// {
	// setLoadTestingState();
	// }
	//
	// public void loadTestFinished( LoadTestRunner loadTestRunner )
	// {
	// setLoadTestingState();
	// }
	// }

	public class InternalTestRunListener extends SecurityTestStepRunListenerAdapter
	{
		public void beforeStep( SecurityTestRunner testRunner, SecurityTestRunContext runContext )
		{
			if( progressBar.isIndeterminate() )
				return;

			progressBar.getModel().setMaximum(
					testRunner.getSecurityTest().getTestStepSecurityChecksCount( testStep.getId() ) );
			progressBar.setForeground( Color.GREEN.darker() );
		}

		public void beforeSecurityCheck( SecurityTestRunner testRunner, SecurityTestRunContext runContext,
				AbstractSecurityCheck securityCheck )
		{
			if( progressBar.isIndeterminate() )
				return;

			if( securityCheck != null )
			{
				progressBar.setString( securityCheck.getName() );
				// TODO set current securityCheck
				progressBar.setValue( runContext.getCurrentCheckIndex() );
			}
		}

		public void afterSecurityCheck( SecurityTestRunner testRunner, SecurityTestRunContext runContext,
				SecurityCheckResult securityCheckResult )
		{
			if( progressBar.isIndeterminate() )
				return;

			if( securityCheckResult.getStatus() == SecurityCheckStatus.FAILED )
			{
				progressBar.setForeground( Color.RED );
			}
			else if( securityCheckResult.getStatus() == SecurityCheckStatus.OK )
			{
				progressBar.setForeground( Color.GREEN.darker() );
			}

			progressBar.setValue( runContext.getCurrentCheckIndex() + 1 );
		}

		public void afterStep( SecurityTestRunner testRunner, SecurityTestRunContext runContext,
				TestStepResult result )
		{
			if( progressBar.isIndeterminate() )
				return;

			if( result.getStatus() == TestStepStatus.FAILED )
			{
				progressBar.setForeground( Color.RED );
			}
			else if( !securityTest.getTestCase().getFailTestCaseOnErrors() )
			{
				progressBar.setForeground( Color.GREEN.darker() );
			}

			progressBar.setString( result.getStatus().toString() );
			progressBar.setValue( progressBar.getMaximum() );
		}
	}
}
