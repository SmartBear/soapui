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

package com.eviware.soapui.security.support;

import java.awt.Color;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JProgressBar;

import com.eviware.soapui.model.security.SecurityScan;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.TestStepResult;
import com.eviware.soapui.model.testsuite.TestRunner.Status;
import com.eviware.soapui.security.SecurityTest;
import com.eviware.soapui.security.SecurityTestRunContext;
import com.eviware.soapui.security.SecurityTestRunnerImpl;
import com.eviware.soapui.security.result.SecurityScanRequestResult;
import com.eviware.soapui.security.result.SecurityScanResult;
import com.eviware.soapui.security.result.SecurityTestStepResult;
import com.eviware.soapui.security.result.SecurityResult.ResultStatus;

/**
 * Class that keeps a JProgressBars state in sync with a SecurityTest
 * 
 * @author dragica.soldo
 */

public class ProgressBarSecurityTestAdapter
{
	private final JProgressBar progressBar;
	private final SecurityTest securityTest;
	private InternalTestRunListener internalTestRunListener;
	private JLabel counterLabel;
	private static final Color OK_COLOR = new Color( 0, 204, 102 );
	private static final Color FAILED_COLOR = new Color( 255, 102, 0 );
	private static final Color UNKNOWN_COLOR = new Color( 240, 240, 240 );

	private static final String STATE_RUN = "In progress";
	private static final String STATE_DONE = "Done";
	private static final String STATE_CANCEL = "Canceled";
	private int alertsCounter;
	private int previousMaxCheckPosition;

	public ProgressBarSecurityTestAdapter( JProgressBar progressBar, SecurityTest securityTest, JLabel cntLabel )
	{
		this.progressBar = progressBar;
		this.securityTest = securityTest;

		progressBar.setBackground( UNKNOWN_COLOR );
		internalTestRunListener = new InternalTestRunListener();
		securityTest.addSecurityTestRunListener( internalTestRunListener );
		cntLabel.setOpaque( true );

		this.counterLabel = cntLabel;
	}

	public void release()
	{
		securityTest.removeSecurityTestRunListener( internalTestRunListener );
	}

	public class InternalTestRunListener extends SecurityTestRunListenerAdapter
	{

		public void beforeRun( TestCaseRunner testRunner, SecurityTestRunContext runContext )
		{

			int maximum = ( ( SecurityTestRunnerImpl )testRunner ).getSecurityTest().getSecurityScanCount();

			for( String key : securityTest.getSecurityScansMap().keySet() )
			{
				List<SecurityScan> securityCheckList = securityTest.getSecurityScansMap().get( key );
				if( securityCheckList.size() > 0 )
					if( securityCheckList.get( 0 ).getTestStep().isDisabled() )
						maximum -= securityCheckList.size();
			}
			progressBar.getModel().setMaximum( maximum );
			progressBar.setForeground( OK_COLOR );
			progressBar.setBackground( Color.white );
			progressBar.setValue( 0 );
			counterLabel.setOpaque( false );
			alertsCounter = 0;
			counterLabel.setText( "" );
		}

		@Override
		public void beforeSecurityScan( TestCaseRunner testRunner, SecurityTestRunContext runContext,
				SecurityScan securityCheck )
		{

			if( progressBar.isIndeterminate() )
				return;

			if( securityCheck != null )
			{
				progressBar.setString( STATE_RUN + ":" + securityCheck.getTestStep().getName() + " - "
						+ securityCheck.getName() );
				progressBar.setValue( runContext.getCurrentScanOnSecurityTestIndex() );
			}
		}

		@Override
		public void afterSecurityScan( TestCaseRunner testRunner, SecurityTestRunContext runContext,
				SecurityScanResult securityCheckResult )
		{
			if( securityCheckResult.getStatus() == ResultStatus.CANCELED )
			{
				progressBar.setString( STATE_CANCEL );
			}

			if( securityCheckResult.getStatus() == ResultStatus.CANCELED
					&& securityCheckResult.isHasRequestsWithWarnings() )
			{
				progressBar.setForeground( FAILED_COLOR );
			}
			else if( securityCheckResult.getStatus() == ResultStatus.FAILED )
			{
				progressBar.setForeground( FAILED_COLOR );
			}
			else if( securityCheckResult.getStatus() == ResultStatus.OK )
			{
				if( !progressBar.getForeground().equals( FAILED_COLOR ) )
					progressBar.setForeground( OK_COLOR );
			}

			// progressBar.setValue(
			// runContext.getCurrentCheckOnSecurityTestIndex() + 1 );
		}

		@Override
		public void beforeStep( TestCaseRunner testRunner, SecurityTestRunContext runContext,
				TestStepResult testStepResult )
		{
			previousMaxCheckPosition = progressBar.getValue();
		}

		@Override
		public void afterStep( TestCaseRunner testRunner, SecurityTestRunContext runContext, SecurityTestStepResult result )
		{
			int currentStepChecksCount = securityTest.getTestStepSecurityScansCount( result.getTestStep().getId() );
			progressBar.setValue( previousMaxCheckPosition + currentStepChecksCount );
		}

		public void afterRun( TestCaseRunner testRunner, SecurityTestRunContext runContext )
		{
			if( testRunner.getStatus() == Status.FAILED )
			{
				progressBar.setForeground( FAILED_COLOR );
			}
			else if( testRunner.getStatus() == Status.FINISHED )
			{
				if( !progressBar.getForeground().equals( FAILED_COLOR ) )
					progressBar.setForeground( OK_COLOR );
			}

			if( progressBar.isIndeterminate() )
				return;

			if( !progressBar.getString().equals( STATE_CANCEL ) )
				progressBar.setString( STATE_DONE );
			progressBar.setBackground( UNKNOWN_COLOR );
			// progressBar.setValue( progressBar.getMaximum() );
		}

		@Override
		public void afterSecurityScanRequest( TestCaseRunner testRunner, SecurityTestRunContext runContext,
				SecurityScanRequestResult securityCheckReqResult )
		{
			if( securityCheckReqResult.getStatus() == ResultStatus.FAILED )
			{
				counterLabel.setOpaque( true );
				counterLabel.setBackground( FAILED_COLOR );
				alertsCounter++ ;
				counterLabel.setText( " " + alertsCounter + " " );
				progressBar.setForeground( FAILED_COLOR );
			}
			else if( securityCheckReqResult.getStatus() == ResultStatus.CANCELED )
			{
				progressBar.setString( STATE_CANCEL );
			}

		}
	}
}
