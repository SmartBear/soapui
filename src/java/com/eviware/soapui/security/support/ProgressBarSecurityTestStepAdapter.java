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
import com.eviware.soapui.security.result.SecurityCheckRequestResult;
import com.eviware.soapui.security.result.SecurityCheckResult;
import com.eviware.soapui.security.result.SecurityResult.SecurityStatus;

/**
 * Class that keeps a JProgressBars state in sync with a SecurityTest
 * 
 * @author dragica.soldo
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
	private static final Color OK_COLOR = new Color( 0, 205, 102 );
	private static final Color FAILED_COLOR = new Color( 255, 102, 0 );
	private static final Color UNKNOWN_COLOR = new Color( 255, 255, 204 );

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
		if( progressBar != null && cntLabel != null ) {
			this.counterLabel.setPreferredSize( new Dimension( 50, 18 ) );
			this.counterLabel.setHorizontalTextPosition( SwingConstants.CENTER );
			this.counterLabel.setHorizontalAlignment( SwingConstants.CENTER );
			this.securityTest.addSecurityTestRunListener( internalTestRunListener );
		}
	}

	public void release()
	{
		// securityTest.removeSecurityTestRunListener( internalTestRunListener );
	}

	public class InternalTestRunListener extends SecurityTestRunListenerAdapter
	{

		private int totalAlertsCounter;

		@Override
		public void beforeRun( TestCaseRunner testRunner, SecurityTestRunContext runContext )
		{
			progressBar.getModel().setMaximum(
					( ( SecurityTestRunnerImpl )testRunner ).getSecurityTest().getTestStepSecurityChecksCount(
							testStep.getId() ) );

			progressBar.setString( "" );
			progressBar.setForeground( Color.white );
			counterLabel.setText( " 0 " );
			counterLabel.setOpaque( false );

			totalAlertsCounter = 0;
			( ( DefaultTreeModel )tree.getModel() ).nodeChanged( node );
		}

		@Override
		public void beforeSecurityCheck( TestCaseRunner testRunner, SecurityTestRunContext runContext,
				AbstractSecurityCheck securityCheck )
		{
			if( securityCheck.getTestStep().getId().equals( testStep.getId() ) )
			{
				if( progressBar.getString().length() == 0 )
				{
					progressBar.setString( "UNKNOWN" );
					progressBar.setForeground( UNKNOWN_COLOR );
					counterLabel.setOpaque( true );
					counterLabel.setBackground( OK_COLOR );
				}
			}
		}

		public void afterSecurityCheck( TestCaseRunner testRunner, SecurityTestRunContext runContext,
				SecurityCheckResult securityCheckResult )
		{

			if( securityCheckResult.getSecurityCheck().getTestStep().getId().equals( testStep.getId() ) )
			{
				progressBar.setIndeterminate( false );

				if( !progressBar.getString().equals( "FAILED" ) )
					if( securityCheckResult.getStatus() == SecurityStatus.CANCELED )
					{
						if( securityCheckResult.isHasRequestsWithWarnings() )
						{
							progressBar.setForeground( FAILED_COLOR );
							progressBar.setString( "FAILED" );
						}
						else
						{
							if( !progressBar.getString().equals( "FAILED" ) )
							{
								progressBar.setForeground( OK_COLOR );
								progressBar.setString( "OK" );
							}
						}
					}
					else if( securityCheckResult.getStatus() == SecurityStatus.FAILED )
					{
						progressBar.setForeground( FAILED_COLOR );
						progressBar.setString( "FAILED" );
					}
					else if( securityCheckResult.getStatus() == SecurityStatus.OK )
					{
						if( !progressBar.getString().equals( "FAILED" ) )
						{
							progressBar.setForeground( OK_COLOR );
							progressBar.setString( "OK" );
						}
					}
					else if( securityCheckResult.getStatus() == SecurityStatus.UNKNOWN )
					{
						if( progressBar.getString().equals( "" ) )
						{
							progressBar.setForeground( UNKNOWN_COLOR );
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
				}

				counterLabel.setText( " " + totalAlertsCounter + " " );
				( ( DefaultTreeModel )tree.getModel() ).nodeChanged( node );
			}
		}

	}

}
