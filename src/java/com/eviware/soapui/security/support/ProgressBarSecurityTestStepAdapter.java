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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JProgressBar;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.TestRunner.Status;
import com.eviware.soapui.security.SecurityTest;
import com.eviware.soapui.security.SecurityTestRunContext;
import com.eviware.soapui.security.SecurityTestRunnerImpl;
import com.eviware.soapui.security.check.AbstractSecurityCheck;
import com.eviware.soapui.security.result.SecurityCheckResult;
import com.eviware.soapui.security.result.SecurityTestStepResult;
import com.eviware.soapui.security.result.SecurityResult.SecurityStatus;

/**
 * Class that keeps a JProgressBars state in sync with a SecurityTest
 * 
 * @author dragica.soldo
 */

public class ProgressBarSecurityTestStepAdapter implements PropertyChangeListener
{
	private final JProgressBar progressBar;
	private final TestStep testStep;
	private final SecurityTest securityTest;
	private InternalTestRunListener internalTestRunListener;
	private JTree tree;
	private DefaultMutableTreeNode node;

	public ProgressBarSecurityTestStepAdapter( JProgressBar progressBar, SecurityTest securityTest, TestStep testStep )
	{
		this.progressBar = progressBar;
		this.testStep = testStep;
		this.securityTest = securityTest;

		internalTestRunListener = new InternalTestRunListener();
		securityTest.addTestStepRunListener( testStep, internalTestRunListener );
	}

	public ProgressBarSecurityTestStepAdapter( JTree tree, DefaultMutableTreeNode node, JProgressBar progressBar, SecurityTest securityTest,
			WsdlTestStep testStep )
	{
		this.tree = tree;
		this.node = node;
		this.progressBar = progressBar;
		this.testStep = testStep;
		this.securityTest = securityTest;

		internalTestRunListener = new InternalTestRunListener();
		securityTest.addTestStepRunListener( testStep, internalTestRunListener );
//		progressBar.addPropertyChangeListener( this );
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
			// progressBar.setForeground( Color.WHITE );
			progressBar.setString( "" );
			((DefaultTreeModel)tree.getModel()).nodeChanged( node );
		}

		public void beforeSecurityCheck( TestCaseRunner testRunner, SecurityTestRunContext runContext,
				AbstractSecurityCheck securityCheck )
		{
//			progressBar.setIndeterminate( true );
			// if( progressBar.isIndeterminate() )
			// return;

//			if( securityCheck != null )
//			{
//				progressBar.setString( securityCheck.getName() );
//				progressBar.setForeground( Color.GRAY );
//				progressBar.setValue( ( ( SecurityTestRunContext )runContext ).getCurrentCheckIndex() );
//			}
//			((DefaultTreeModel)tree.getModel()).nodeChanged( node );
		}

		public void afterSecurityCheck( TestCaseRunner testRunner, SecurityTestRunContext runContext,
				SecurityCheckResult securityCheckResult )
		{
			progressBar.setIndeterminate( false );

			if( securityCheckResult.getStatus() == SecurityStatus.CANCELED )
			{
				if( securityCheckResult.isHasRequestsWithWarnings() )
				{
					progressBar.setForeground( Color.RED );
				}
				else
				{
					progressBar.setForeground( Color.GREEN.darker() );
				}
			}
			else if( securityCheckResult.getStatus() == SecurityStatus.FAILED )
			{
				progressBar.setForeground( Color.RED );
			}
			else if( securityCheckResult.getStatus() == SecurityStatus.OK )
			{
				progressBar.setForeground( Color.GREEN.darker() );
			}
			else if( securityCheckResult.getStatus() == SecurityStatus.UNKNOWN )
			{
				progressBar.setForeground( Color.WHITE );
			}

			progressBar.setValue( ( ( SecurityTestRunContext )runContext ).getCurrentCheckIndex() + 1 );
			((DefaultTreeModel)tree.getModel()).nodeChanged( node );
		}

		public void afterStep( TestCaseRunner testRunner, SecurityTestRunContext runContext, SecurityTestStepResult result )
		{
			if( progressBar.isIndeterminate() )
				return;

			if( result.getStatus() == SecurityStatus.FAILED )
			{
				progressBar.setForeground( Color.RED );
			}
			else if( testRunner.getStatus() == Status.FINISHED )
			{
				progressBar.setForeground( Color.GREEN.darker() );
			}
			else if( testRunner.getStatus() != Status.RUNNING )
			{
				progressBar.setForeground( Color.WHITE );
			}

			progressBar.setString( result.getStatus().toString() );
			progressBar.setValue( progressBar.getMaximum() );
			((DefaultTreeModel)tree.getModel()).nodeChanged( node );
		}
	}

	@Override
	public void propertyChange( PropertyChangeEvent arg0 )
	{
		String name = arg0.getPropertyName();
//		System.out.println(name);
		if( name.equals("string" ) || name.equals("foreground" ) || name.equals("indeterminate") )
			((DefaultTreeModel)tree.getModel()).nodeChanged( node );
	}
}
