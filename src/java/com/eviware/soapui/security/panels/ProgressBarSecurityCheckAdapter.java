package com.eviware.soapui.security.panels;

import java.awt.Color;

import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;

import com.eviware.soapui.model.security.SecurityCheck;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.security.SecurityTest;
import com.eviware.soapui.security.SecurityTestRunContext;
import com.eviware.soapui.security.result.SecurityCheckRequestResult;
import com.eviware.soapui.security.result.SecurityCheckResult;
import com.eviware.soapui.security.result.SecurityResult.SecurityStatus;
import com.eviware.soapui.security.support.SecurityTestRunListenerAdapter;

public class ProgressBarSecurityCheckAdapter extends SecurityTestRunListenerAdapter
{

	private static final Color UNKNOWN_COLOR = new Color( 255, 255, 204 );

	private static final Color OK_COLOR = new Color( 0, 205, 102 );

	private static final Color FAILED_COLOR = new Color( 255, 102, 0 );

	private JTree tree;
	private SecurityCheckNode node;
	private JProgressBar progressBar;
	private SecurityCheck securityCheck;
	private SecurityTest securityTest;
	private int alertsCounter = 0;

	private JLabel cntLabel;

	public ProgressBarSecurityCheckAdapter( JTree tree, SecurityCheckNode node, JProgressBar progressBar,
			SecurityCheck securityCheck, SecurityTest securityTest, JLabel cntLabel )
	{
		this.tree = tree;
		this.node = node;
		this.progressBar = progressBar;
		this.progressBar.setMaximum( 100 );
		this.securityCheck = securityCheck;
		this.securityTest = securityTest;
		if ( securityCheck.getAssertionsSupport().getAssertionCount() == 0 ) {
			progressBar.setForeground( new Color(204, 153, 255));
			progressBar.setString( "No Assertions" );
			progressBar.setValue( 100 );
		} else {
			this.securityTest.addSecurityTestRunListener( this );
		}

		this.cntLabel = cntLabel;
	}

	public void release()
	{
		// securityTest.removeSecurityTestRunListener( this );
	}

	@Override
	public void afterSecurityCheckRequest( TestCaseRunner testRunner, SecurityTestRunContext runContext,
			SecurityCheckRequestResult securityCheckReqResult )
	{
		if( securityCheckReqResult.getSecurityCheck().getTestStep().getId().equals(
				this.securityCheck.getTestStep().getId() )
				&& this.securityCheck.getName().equals( securityCheckReqResult.getSecurityCheck().getName() ) )
		{
			progressBar.setIndeterminate( false );
			if( securityCheckReqResult.getStatus() == SecurityStatus.FAILED )
			{
				progressBar.setForeground( FAILED_COLOR );
				progressBar.setString( "FAILED" );
				cntLabel.setBackground( FAILED_COLOR );
				alertsCounter ++;
			}
			else if( securityCheckReqResult.getStatus() == SecurityStatus.OK )
			{
				progressBar.setForeground( OK_COLOR );
				progressBar.setString( "OK" );
			}
			else if( securityCheckReqResult.getStatus() == SecurityStatus.UNKNOWN )
			{
				progressBar.setForeground( UNKNOWN_COLOR );
				progressBar.setString( "UNKNOWN" );
			}

			cntLabel.setText( " " + alertsCounter + " " );
			progressBar.setValue( progressBar.getValue() + 1 );
			( ( DefaultTreeModel )tree.getModel() ).nodeChanged( node );
		}
	}

	@Override
	public void beforeRun( TestCaseRunner testRunner, SecurityTestRunContext runContext )
	{
		progressBar.setIndeterminate( false );

		progressBar.setForeground( UNKNOWN_COLOR );
		progressBar.setValue( 0 );
		progressBar.setString( "" );
		cntLabel.setBackground( OK_COLOR );
		cntLabel.setText( " 0 " );
		alertsCounter = 0;
		( ( DefaultTreeModel )tree.getModel() ).nodeChanged( node );
	}

	@Override
	public void afterSecurityCheck( TestCaseRunner testRunner, SecurityTestRunContext runContext,
			SecurityCheckResult securityCheckResult )
	{
		if( securityCheckResult.getSecurityCheck().getTestStep().getId()
				.equals( this.securityCheck.getTestStep().getId() )
				&& this.securityCheck.getName().equals( securityCheckResult.getSecurityCheck().getName() ) )
		{

			progressBar.setValue( 100 );
			( ( DefaultTreeModel )tree.getModel() ).nodeChanged( node );
		}
	}
}
