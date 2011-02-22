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

import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.security.SecurityCheckResult;
import com.eviware.soapui.security.SecurityTest;
import com.eviware.soapui.security.SecurityTestRunContext;
import com.eviware.soapui.security.SecurityTestRunnerImpl;
import com.eviware.soapui.security.SecurityTestStepResult;
import com.eviware.soapui.security.SecurityCheckRequestResult.SecurityStatus;
import com.eviware.soapui.security.check.AbstractSecurityCheck;

/**
 * Class that keeps a JProgressBars state in sync with a SecurityTest
 * 
 * @author dragica.soldo
 */

public class ProgressBarSecurityTestStepAdapter
{
	private final JProgressBar progressBar;
	private final TestStep testStep;
	private final SecurityTest securityTest;
	private InternalTestRunListener internalTestRunListener;

	public ProgressBarSecurityTestStepAdapter( JProgressBar progressBar, SecurityTest securityTest, TestStep testStep )
	{
		this.progressBar = progressBar;
		this.testStep = testStep;
		this.securityTest = securityTest;

		internalTestRunListener = new InternalTestRunListener();
		securityTest.addTestStepRunListener( testStep, internalTestRunListener );
	}

	public void release()
	{
		securityTest.removeTestStepRunListener( testStep, internalTestRunListener );
	}

	public class InternalTestRunListener extends SecurityTestRunListenerAdapter
	{
		@Override
		public void beforeStep( TestCaseRunner testRunner, SecurityTestRunContext runContext, TestStep testStep )
		{
			if( progressBar.isIndeterminate() )
				return;

			progressBar.getModel().setMaximum(
					( ( SecurityTestRunnerImpl )testRunner ).getSecurityTest().getTestStepSecurityChecksCount(
							testStep.getId() ) );
			// progressBar.setForeground( Color.GREEN.darker() );
			progressBar.setString( "" );
		}

		public void beforeSecurityCheck( TestCaseRunner testRunner, SecurityTestRunContext runContext,
				AbstractSecurityCheck securityCheck )
		{
			if( progressBar.isIndeterminate() )
				return;

			if( securityCheck != null )
			{
				progressBar.setString( securityCheck.getName() );
				progressBar.setValue( ( ( SecurityTestRunContext )runContext ).getCurrentCheckIndex() );
			}
		}

		public void afterSecurityCheck( TestCaseRunner testRunner, SecurityTestRunContext runContext,
				SecurityCheckResult securityCheckResult )
		{
			if( progressBar.isIndeterminate() )
				return;

			if( securityCheckResult.getStatus() == SecurityStatus.FAILED )
			{
				progressBar.setForeground( Color.RED );
			}
			else if( securityCheckResult.getStatus() == SecurityStatus.OK )
			{
				progressBar.setForeground( Color.GREEN.darker() );
			}

			progressBar.setValue( ( ( SecurityTestRunContext )runContext ).getCurrentCheckIndex() + 1 );
		}

		public void afterStep( TestCaseRunner testRunner, SecurityTestRunContext runContext, SecurityTestStepResult result )
		{
			if( progressBar.isIndeterminate() )
				return;

			if( result.getStatus() == SecurityStatus.FAILED )
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
