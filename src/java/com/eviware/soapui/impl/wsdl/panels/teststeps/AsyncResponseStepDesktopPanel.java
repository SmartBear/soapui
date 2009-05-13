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

package com.eviware.soapui.impl.wsdl.panels.teststeps;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.Date;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.Document;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockResponse;
import com.eviware.soapui.impl.wsdl.panels.mockoperation.AbstractWsdlMockResponseDesktopPanel;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlAsyncResponseTestStep;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.support.TestRunListenerAdapter;
import com.eviware.soapui.model.testsuite.AssertionError;
import com.eviware.soapui.model.testsuite.LoadTestRunner;
import com.eviware.soapui.model.testsuite.TestRunContext;
import com.eviware.soapui.model.testsuite.TestRunner;
import com.eviware.soapui.model.testsuite.TestStepResult;
import com.eviware.soapui.monitor.support.TestMonitorListenerAdapter;
import com.eviware.soapui.support.DocumentListenerAdapter;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.JXToolBar;

@SuppressWarnings( "serial" )
public class AsyncResponseStepDesktopPanel extends
		AbstractWsdlMockResponseDesktopPanel<WsdlAsyncResponseTestStep, WsdlMockResponse>
{
	private InternalTestRunListener testRunListener;
	private InternalTestMonitorListener testMonitorListener;
	private JSplitPane verticalSplit;
	private JTextArea responseLog;
	private AssertionsPanel assertionsPanel;
	private JTextField portField;
	private JTextField pathField;
	private JTextField queryField;
	private JTextField valueField;

	/**
	 * 
	 *
	 */
	private class InternalTestMonitorListener extends TestMonitorListenerAdapter
	{
		private WsdlAsyncResponseTestStep step;

		public InternalTestMonitorListener()
		{
			step = getModelItem();
		}

		public void loadTestFinished( LoadTestRunner loadTestRunner )
		{
			setEnabled( !SoapUI.getTestMonitor().hasRunningTest( step.getTestCase() ) );
		}

		public void loadTestStarted( LoadTestRunner loadTestRunner )
		{
			if( loadTestRunner.getLoadTest().getTestCase() == step.getTestCase() )
			{
				setEnabled( false );
			}
		}

		public void testCaseFinished( TestRunner testRunner )
		{
			setEnabled( !SoapUI.getTestMonitor().hasRunningTest( step.getTestCase() ) );
		}

		public void testCaseStarted( TestRunner testRunner )
		{
			if( testRunner.getTestCase() == step.getTestCase() )
			{
				setEnabled( false );
			}
		}
	}

	/**
     * 
     *
     */
	public class InternalTestRunListener extends TestRunListenerAdapter
	{
		public void afterRun( TestRunner testRunner, TestRunContext testRunContext )
		{
			setEnabled( true );
		}

		public void beforeRun( TestRunner testRunner, TestRunContext testRunContext )
		{
			setEnabled( false );
		}

		public void beforeStep( TestRunner testRunner, TestRunContext testRunContext )
		{
			if( testRunContext.getCurrentStep() == getModelItem() )
			{
				WsdlAsyncResponseTestStep step = getModelItem();

				StringBuilder sb = new StringBuilder();
				sb.append( responseLog.getText() );
				sb.append( ( new Date( System.currentTimeMillis() ) ).toString() );
				sb.append( ": Waiting for request on http://127.0.0.1:" );
				sb.append( step.getPort() );
				sb.append( step.getPath() );
				sb.append( "\r\n" );

				responseLog.setText( sb.toString() );
			}
		}

		public void afterStep( TestRunner testRunner, TestRunContext testRunContext, TestStepResult result )
		{
			if( result.getTestStep() == getModelItem() )
			{
				StringBuilder sb = new StringBuilder();
				sb.append( responseLog.getText() );
				sb.append( ( new Date( result.getTimeStamp() ) ).toString() );
				sb.append( ": Handled request in " );
				sb.append( result.getTimeTaken() );
				sb.append( "ms" );
				sb.append( "\r\n" );

				responseLog.setText( sb.toString() );
			}
		}
	}

	/**
	 * Constructor
	 * 
	 * @param step
	 */
	public AsyncResponseStepDesktopPanel( WsdlAsyncResponseTestStep step )
	{
		super( step );

		testMonitorListener = new InternalTestMonitorListener();
		testRunListener = new InternalTestRunListener();

		init( step.getMockResponse() );

		step.getTestCase().addTestRunListener( testRunListener );
		SoapUI.getTestMonitor().addTestMonitorListener( testMonitorListener );

		setEnabled( !SoapUI.getTestMonitor().hasRunningTest( step.getTestCase() ) );
	}

	protected JComponent buildContent()
	{
		JComponent jcomponent = super.buildContent();

		verticalSplit = UISupport.createVerticalSplit();
		verticalSplit.setTopComponent( jcomponent );
		verticalSplit.setBottomComponent( createTabPanel() );
		verticalSplit.setDividerLocation( 350 );
		verticalSplit.setResizeWeight( 0.9 );
		verticalSplit.setBorder( null );

		return verticalSplit;
	}

	protected void createToolbar( JXToolBar toolbar )
	{
		WsdlAsyncResponseTestStep step = getModelItem();

		// Add text field for 'Path'
		// TODO Ericsson: Need more space for Path and Value fields. Move them to
		// a dialog?
		// TODO Ericsson: Better GUI to enter an XPath.
		toolbar.addUnrelatedGap();
		toolbar.addFixed( new JLabel( "Path" ) );
		toolbar.addRelatedGap();
		pathField = new JTextField( step.getPath(), 15 );
		pathField.getDocument().addDocumentListener( new DocumentListenerAdapter()
		{

			public void update( Document document )
			{
				WsdlAsyncResponseTestStep step = getModelItem();
				step.setPath( pathField.getText() );
			}
		} );
		toolbar.addFixed( pathField );

		// Add text field for 'Port'
		toolbar.addUnrelatedGap();
		toolbar.addFixed( new JLabel( "Port" ) );
		toolbar.addRelatedGap();
		portField = new JTextField( String.valueOf( step.getPort() ), 5 );
		portField.getDocument().addDocumentListener( new DocumentListenerAdapter()
		{

			public void update( Document document )
			{
				try
				{
					WsdlAsyncResponseTestStep step = getModelItem();

					step.setPort( Integer.parseInt( portField.getText() ) );
				}
				catch( NumberFormatException e )
				{
				}
			}
		} );
		toolbar.addFixed( portField );

		// Add text field for 'Request Query'
		toolbar.addUnrelatedGap();
		toolbar.addFixed( new JLabel( "Request Query" ) );
		toolbar.addRelatedGap();
		queryField = new JTextField( step.getRequestQuery(), 30 );
		queryField.getDocument().addDocumentListener( new DocumentListenerAdapter()
		{

			public void update( Document document )
			{
				WsdlAsyncResponseTestStep step = getModelItem();
				step.setRequestQuery( queryField.getText() );
			}
		} );
		toolbar.addFixed( queryField );

		// Add text field for 'Matching Value'
		toolbar.addUnrelatedGap();
		toolbar.addFixed( new JLabel( "Matching Value" ) );
		toolbar.addRelatedGap();
		valueField = new JTextField( step.getMatchingValue(), 15 );
		valueField.getDocument().addDocumentListener( new DocumentListenerAdapter()
		{

			public void update( Document document )
			{
				WsdlAsyncResponseTestStep step = getModelItem();
				step.setMatchingValue( valueField.getText() );
			}
		} );
		// valueField.addPropertyChangeListener(this);
		toolbar.addFixed( valueField );
	}

	private Component createTabPanel()
	{
		JTabbedPane pane = new JTabbedPane( 4 );
		pane.setTabLayoutPolicy( 1 );

		pane.addTab( "Assertions", createAssertionsPanel() );
		pane.addTab( "Response Log", createResponseLogPanel() );

		return UISupport.createTabPanel( pane, true );
	}

	private JPanel createResponseLogPanel()
	{
		responseLog = new JTextArea();
		responseLog.setEditable( false );
		responseLog.setToolTipText( "Response Log" );

		JPanel panel = new JPanel( new BorderLayout() );
		panel.add( new JScrollPane( responseLog ), "Center" );

		return panel;
	}

	private JComponent createAssertionsPanel()
	{
		assertionsPanel = new AssertionsPanel( getModelItem() )
		{

			protected void selectError( AssertionError assertionerror )
			{
				getResponseEditor().requestFocus();
			}
		};

		return assertionsPanel;
	}

	public boolean onClose( boolean flag )
	{
		WsdlAsyncResponseTestStep step = getModelItem();

		step.getTestCase().removeTestRunListener( testRunListener );
		SoapUI.getTestMonitor().removeTestMonitorListener( testMonitorListener );

		assertionsPanel.release();

		return super.onClose( flag );
	}

	public void setEnabled( boolean flag )
	{
		super.setEnabled( flag );

		pathField.setEnabled( flag );
		portField.setEnabled( flag );
	}

	public boolean dependsOn( ModelItem modelItem )
	{
		WsdlAsyncResponseTestStep step = getModelItem();

		return modelItem == getModelItem() || modelItem == step.getTestCase() || modelItem == step.getOperation()
				|| modelItem == step.getOperation().getInterface() || modelItem == step.getTestCase().getTestSuite()
				|| modelItem == step.getTestCase().getTestSuite().getProject();
	}
}