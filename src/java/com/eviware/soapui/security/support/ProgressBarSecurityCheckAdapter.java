package com.eviware.soapui.security.support;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.tree.DefaultTreeModel;

import com.eviware.soapui.model.security.SecurityCheck;
import com.eviware.soapui.model.testsuite.AssertionsListener;
import com.eviware.soapui.model.testsuite.TestAssertion;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.security.SecurityTest;
import com.eviware.soapui.security.SecurityTestRunContext;
import com.eviware.soapui.security.check.AbstractSecurityCheck;
import com.eviware.soapui.security.check.AbstractSecurityCheckWithProperties;
import com.eviware.soapui.security.panels.SecurityCheckNode;
import com.eviware.soapui.security.result.SecurityCheckRequestResult;
import com.eviware.soapui.security.result.SecurityCheckResult;
import com.eviware.soapui.security.result.SecurityResult.SecurityStatus;

public class ProgressBarSecurityCheckAdapter extends SecurityTestRunListenerAdapter implements AssertionsListener
{

	private static final Color OK_COLOR = new Color( 0, 204, 102 );
	private static final Color FAILED_COLOR = new Color( 255, 102, 0 );
//	private static final Color UNKNOWN_COLOR = new Color( 240, 240, 240 );
	private static final Color MISSING_ASSERTION_COLOR = new Color( 204, 153, 255 );

	private static final String STATE_RUN = "No Alerts";
	private static final String STATE_FAIL = "Alerts";
	private static final String STATE_CANCEL = "Canceled";
	private static final String STATE_MISSING_ASSERTIONS = "Missing Assertions";
	private static final String STATE_MISSING_PARAMETERS = "Missing Parameters";

	private JTree tree;
	private SecurityCheckNode node;
	private JProgressBar progressBar;
	private SecurityCheck securityCheck;
	private SecurityTest securityTest;
	private int alertsCounter = 0;
	private String prePostFix = " ";

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

		this.securityTest.addSecurityTestRunListener( this );

		this.cntLabel = cntLabel;
		this.cntLabel.setPreferredSize( new Dimension( 50, 18 ) );
		this.cntLabel.setHorizontalTextPosition( SwingConstants.CENTER );
		this.cntLabel.setHorizontalAlignment( SwingConstants.CENTER );
		( ( AbstractSecurityCheck )securityCheck ).addAssertionsListener( this );

	}

	public void release()
	{
		securityTest.removeSecurityTestRunListener( this );
		( ( AbstractSecurityCheck )securityCheck ).removeAssertionsListener( this );
		securityTest = null;
		securityCheck = null;
	}

	@Override
	public void afterSecurityCheckRequest( TestCaseRunner testRunner, SecurityTestRunContext runContext,
			SecurityCheckRequestResult securityCheckReqResult )
	{

		if( securityCheckReqResult.getSecurityCheck().getTestStep().getId().equals(
				this.securityCheck.getTestStep().getId() )
				&& this.securityCheck.getName().equals( securityCheckReqResult.getSecurityCheck().getName() ) )
		{
			if( securityCheck.getAssertionsSupport().getAssertionCount() == 0 )
			{
				progressBar.setForeground( MISSING_ASSERTION_COLOR );
				progressBar.setString( STATE_MISSING_ASSERTIONS );
				progressBar.setValue( 100 );
			}
			else if( this.securityCheck instanceof AbstractSecurityCheckWithProperties )
			{
				if( ( ( AbstractSecurityCheckWithProperties )this.securityCheck ).getParameterHolder() != null
						&& ( ( AbstractSecurityCheckWithProperties )this.securityCheck ).getParameterHolder()
								.getParameterList() != null
						&& ( ( AbstractSecurityCheckWithProperties )this.securityCheck ).getParameterHolder()
								.getParameterList().size() == 0 )
				{
					progressBar.setForeground( MISSING_ASSERTION_COLOR );
					progressBar.setString( STATE_MISSING_PARAMETERS );
					progressBar.setValue( 100 );
				}
				else
				{
					if( securityCheckReqResult.getStatus() == SecurityStatus.FAILED )
					{
						progressBar.setForeground( FAILED_COLOR );
						progressBar.setString( STATE_FAIL );
						alertsCounter++ ;
					}
					else if( securityCheckReqResult.getStatus() == SecurityStatus.OK )
					{
						if( !progressBar.getForeground().equals( FAILED_COLOR ) )
						{
							progressBar.setForeground( OK_COLOR );
							progressBar.setString( STATE_RUN );
						}
					}

					if( alertsCounter != 0 )
					{
						cntLabel.setOpaque( true );
						cntLabel.setBackground( FAILED_COLOR );
						cntLabel.setText( prePostFix + alertsCounter + prePostFix );
					}

					progressBar.setValue( progressBar.getValue() + 1 );
				}
			}
			( ( DefaultTreeModel )tree.getModel() ).nodeChanged( node );
		}
	}

	@Override
	public void beforeRun( TestCaseRunner testRunner, SecurityTestRunContext runContext )
	{
		progressBar.setIndeterminate( false );

		progressBar.setValue( 0 );
		progressBar.setString( "" );
		progressBar.setForeground( OK_COLOR );
		cntLabel.setOpaque( false );
		cntLabel.setText( "" );
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
			if ( securityCheckResult.getStatus() != SecurityStatus.CANCELED ) {
				if( securityCheck.getAssertionsSupport().getAssertionCount() == 0 )
				{
					progressBar.setForeground( MISSING_ASSERTION_COLOR );
					progressBar.setString( STATE_MISSING_ASSERTIONS );
				}
				else if( this.securityCheck instanceof AbstractSecurityCheckWithProperties )
				{
					if( ( ( AbstractSecurityCheckWithProperties )this.securityCheck ).getParameterHolder() != null
							&& ( ( AbstractSecurityCheckWithProperties )this.securityCheck ).getParameterHolder()
									.getParameterList() != null
							&& ( ( AbstractSecurityCheckWithProperties )this.securityCheck ).getParameterHolder()
									.getParameterList().size() == 0 )
					{
						progressBar.setForeground( MISSING_ASSERTION_COLOR );
						progressBar.setString( STATE_MISSING_PARAMETERS );
					}
				}
				progressBar.setValue( 100 );
			}else {
				progressBar.setString( STATE_CANCEL );
			}
			( ( DefaultTreeModel )tree.getModel() ).nodeChanged( node );
		}
	}

	/**
	 * 
	 */
	void updateProgressBar()
	{
//		if( ProgressBarSecurityCheckAdapter.this.securityCheck.getAssertionsSupport().getAssertionCount() == 0 )
//		{
//			ProgressBarSecurityCheckAdapter.this.progressBar.setForeground( MISSING_ASSERTION_COLOR );
//			ProgressBarSecurityCheckAdapter.this.progressBar.setString( STATE_MISSING_ASSERTIONS );
//			ProgressBarSecurityCheckAdapter.this.progressBar.setValue( 100 );
//		}
//		( ( DefaultTreeModel )ProgressBarSecurityCheckAdapter.this.tree.getModel() )
//				.nodeChanged( ProgressBarSecurityCheckAdapter.this.node );
	}

	@Override
	public void assertionAdded( TestAssertion assertion )
	{
		updateProgressBar();
	}

	@Override
	public void assertionMoved( TestAssertion assertion, int ix, int offset )
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void assertionRemoved( TestAssertion assertion )
	{
		updateProgressBar();
	}

}
