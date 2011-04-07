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
import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.security.SecurityTest;
import com.eviware.soapui.security.SecurityTestRunContext;
import com.eviware.soapui.security.SecurityTestRunnerImpl;
import com.eviware.soapui.security.check.AbstractSecurityCheck;
import com.eviware.soapui.security.check.AbstractSecurityCheckWithProperties;
import com.eviware.soapui.security.result.SecurityCheckRequestResult;
import com.eviware.soapui.security.result.SecurityCheckResult;
import com.eviware.soapui.security.result.SecurityTestStepResult;
import com.eviware.soapui.security.result.SecurityResult.SecurityStatus;

/**
 * Class that keeps a JProgressBars state in sync with a SecurityTest
 * 
 */

public class ProgressBarSecurityTestStepAdapter
{
	private JProgressBar progressBar;
	private TestStep testStep;
	private SecurityTest securityTest;
	private InternalTestRunListener internalTestRunListener;
	private JTree tree;
	private DefaultMutableTreeNode node;
	private JLabel counterLabel;
	private static final Color OK_COLOR = new Color( 0, 204, 102 );
	private static final Color FAILED_COLOR = new Color( 255, 102, 0 );
	private static final Color UNKNOWN_COLOR = new Color( 240, 240, 240 );
	private static final Color MISSING_ASSERTION_COLOR = new Color( 204, 153, 255 );

	private static final String STATE_RUN = "In progress";
	private static final String STATE_DONE = "Done";
	private static final String STATE_CANCEL = "Canceled";
	private static final String STATE_MISSING_ASSERTIONS = "Missing Assertions";
	private static final String STATE_MISSING_PARAMETERS = "Missing Parameters";

	public ProgressBarSecurityTestStepAdapter( JTree tree, DefaultMutableTreeNode node, JProgressBar progressBar,
			SecurityTest securityTest, WsdlTestStep testStep, JLabel cntLabel )
	{
		this.tree = tree;
		this.node = node;
		this.progressBar = progressBar;
		this.testStep = testStep;
		this.securityTest = securityTest;

		this.counterLabel = cntLabel;
		internalTestRunListener = new InternalTestRunListener();
		if( progressBar != null && cntLabel != null )
		{
			this.progressBar.setBackground( UNKNOWN_COLOR );
			this.counterLabel.setPreferredSize( new Dimension( 50, 18 ) );
			this.counterLabel.setHorizontalTextPosition( SwingConstants.CENTER );
			this.counterLabel.setHorizontalAlignment( SwingConstants.CENTER );
			this.securityTest.addSecurityTestRunListener( internalTestRunListener );
		}
	}

	public void release()
	{
		securityTest.removeSecurityTestRunListener( internalTestRunListener );

		securityTest = null;
		testStep = null;
	}

	public class InternalTestRunListener extends SecurityTestRunListenerAdapter
	{

		private int totalAlertsCounter;

		@Override
		public void beforeStep( TestCaseRunner testRunner, SecurityTestRunContext runContext, TestStep ts )
		{
			if( ts.getId().equals( testStep.getId() ) )
			{
				progressBar.getModel().setMaximum(
						( ( SecurityTestRunnerImpl )testRunner ).getSecurityTest().getTestStepSecurityChecksCount(
								testStep.getId() ) );

				progressBar.setString( STATE_RUN );
				progressBar.setValue( 0 );
				counterLabel.setText( "" );
				counterLabel.setOpaque( false );

				totalAlertsCounter = 0;
				( ( DefaultTreeModel )tree.getModel() ).nodeChanged( node );
			}
		}

		@Override
		public void beforeRun( TestCaseRunner testRunner, SecurityTestRunContext runContext )
		{

			progressBar.setString( "" );
			progressBar.setValue( 0 );
			progressBar.setForeground( UNKNOWN_COLOR );
			counterLabel.setText( "" );
			counterLabel.setOpaque( false );

			totalAlertsCounter = 0;
			( ( DefaultTreeModel )tree.getModel() ).nodeChanged( node );
		}

		@Override
		public void afterStep( TestCaseRunner testRunner, SecurityTestRunContext runContext, SecurityTestStepResult result )
		{
			if( runContext.getCurrentStep().getId().equals( testStep.getId() ) )
			{
				if( !( progressBar.getString().equals( STATE_CANCEL )
						|| progressBar.getString().equals( STATE_MISSING_ASSERTIONS ) || progressBar.getString().equals(
						STATE_MISSING_PARAMETERS ) ) )
					progressBar.setString( STATE_DONE );
			}
		}

		@Override
		public void beforeSecurityCheck( TestCaseRunner testRunner, SecurityTestRunContext runContext,
				AbstractSecurityCheck securityCheck )
		{
			if( securityCheck.getTestStep().getId().equals( testStep.getId() ) )
			{
				if( progressBar.getString().equals( "" ) )
					progressBar.setString( STATE_RUN );
				if( securityCheck.getAssertionCount() == 0 )
				{
					progressBar.setString( STATE_MISSING_PARAMETERS );
					progressBar.setForeground( MISSING_ASSERTION_COLOR );
				}
				if( securityCheck instanceof AbstractSecurityCheckWithProperties
						&& ( ( AbstractSecurityCheckWithProperties )securityCheck ).getParameterHolder().getParameterList()
								.size() == 0 )
				{
					progressBar.setString( STATE_MISSING_ASSERTIONS );
					progressBar.setForeground( MISSING_ASSERTION_COLOR );
				}
			}
		}

		public void afterSecurityCheck( TestCaseRunner testRunner, SecurityTestRunContext runContext,
				SecurityCheckResult securityCheckResult )
		{

			if( securityCheckResult.getSecurityCheck().getTestStep().getId().equals( testStep.getId() ) )
			{

				if( securityCheckResult.getStatus() == SecurityStatus.CANCELED )
				{
					progressBar.setString( STATE_CANCEL );
				}
				else if( !progressBar.getForeground().equals( MISSING_ASSERTION_COLOR ) )
				{
					// progressbar can change its color only if not missing
					// assertions or parameters
					if( securityCheckResult.getStatus() == SecurityStatus.FAILED )
					{
						progressBar.setForeground( FAILED_COLOR );
					}
					else if( securityCheckResult.getStatus() == SecurityStatus.OK )
					{
						// can not change to OK color if any of previous checks
						// failed.
						if( !progressBar.getForeground().equals( FAILED_COLOR ) )
						{
							progressBar.setForeground( OK_COLOR );
						}
					}
				}

				progressBar.setValue( ( ( SecurityTestRunContext )runContext ).getCurrentCheckIndex() + 1 );
				( ( DefaultTreeModel )tree.getModel() ).nodeChanged( node );
			}
		}

		@Override
		public void afterSecurityCheckRequest( TestCaseRunner testRunner, SecurityTestRunContext runContext,
				SecurityCheckRequestResult securityCheckReqResult )
		{

			if( securityCheckReqResult.getSecurityCheck().getTestStep().getId().equals( testStep.getId() ) )
			{
				if( securityCheckReqResult.getStatus() == SecurityStatus.FAILED )
				{
					counterLabel.setOpaque( true );
					counterLabel.setBackground( FAILED_COLOR );
					totalAlertsCounter++ ;
					counterLabel.setText( " " + totalAlertsCounter + " " );
					( ( DefaultTreeModel )tree.getModel() ).nodeChanged( node );
				}
			}
		}

	}

}
