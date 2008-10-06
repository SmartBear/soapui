/*
 *  soapUI Pro, copyright (C) 2007-2008 eviware software ab 
 */

package com.eviware.soapui.impl.wsdl.panels.teststeps;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.support.components.ModelItemXmlEditor;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockResponse;
import com.eviware.soapui.impl.wsdl.panels.mockoperation.AbstractWsdlMockResponseDesktopPanel;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlMockResponseTestStep;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.support.TestRunListenerAdapter;
import com.eviware.soapui.model.testsuite.AssertionError;
import com.eviware.soapui.model.testsuite.*;
import com.eviware.soapui.monitor.support.TestMonitorListenerAdapter;
import com.eviware.soapui.support.DocumentListenerAdapter;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.JXToolBar;

import javax.swing.*;
import javax.swing.text.Document;
import java.awt.*;
import java.util.Date;

public class WsdlMockResponseStepDesktopPanel extends AbstractWsdlMockResponseDesktopPanel<WsdlMockResponseTestStep,WsdlMockResponse>
{
	private JSplitPane outerSplit;
	private JTextArea logArea;
	private AssertionsPanel assertionsPanel;
	private JTextField portField;
	private JTextField pathField;
	private InternalTestRunListener testRunListener;
	private InternalTestMonitorListener testMonitorListener = new InternalTestMonitorListener();

	public WsdlMockResponseStepDesktopPanel( WsdlMockResponseTestStep mockResponseStep )
	{
		super( mockResponseStep );
		init( mockResponseStep.getMockResponse() );
		
		testRunListener = new InternalTestRunListener();
		mockResponseStep.getTestCase().addTestRunListener( testRunListener );
		
		SoapUI.getTestMonitor().addTestMonitorListener( testMonitorListener );
     	setEnabled( !SoapUI.getTestMonitor().hasRunningTest( mockResponseStep.getTestCase() ) );
	}

	@Override
	protected JComponent buildContent()
	{
		JComponent component = super.buildContent();
		outerSplit = UISupport.createVerticalSplit();
      outerSplit.setTopComponent( component );
      outerSplit.setBottomComponent( buildLogPanel() );
      outerSplit.setDividerLocation( 350 );
      outerSplit.setResizeWeight( 0.9 );
      outerSplit.setBorder( null );

      return outerSplit;
	}

   public void setContent(JComponent content)
	{
		outerSplit.setTopComponent(content);
	}

	public void removeContent(JComponent content)
	{
		outerSplit.setTopComponent(null);
	}

	@Override
	protected void createToolbar( JXToolBar toolbar )
	{
		toolbar.addUnrelatedGap();
		toolbar.addFixed( new JLabel( "Path" ));
		toolbar.addRelatedGap();
		pathField = new JTextField( getModelItem().getPath(), 15);
		pathField.getDocument().addDocumentListener( new DocumentListenerAdapter() {

			@Override
			public void update( Document document )
			{
				getModelItem().setPath( pathField.getText() );
			}} );
		
		toolbar.addFixed( pathField);
	
		toolbar.addUnrelatedGap();
		toolbar.addFixed( new JLabel( "Port" ));
		toolbar.addRelatedGap();
		portField = new JTextField( String.valueOf( getModelItem().getPort() ), 5);
		portField.getDocument().addDocumentListener( new DocumentListenerAdapter() {

			@Override
			public void update( Document document )
			{
				try
				{
					getModelItem().setPort( Integer.parseInt( portField.getText() ) );
				}
				catch( NumberFormatException e )
				{
				}
			}} );
		
		toolbar.addFixed( portField);
	}

	private Component buildLogPanel()
   {
      JTabbedPane tabbedPane = new JTabbedPane( JTabbedPane.RIGHT );
      tabbedPane.setTabLayoutPolicy( JTabbedPane.SCROLL_TAB_LAYOUT );
      
      logArea = new JTextArea();
      logArea.setEditable(false);
      logArea.setToolTipText("Response Log");
      
      JPanel panel = new JPanel(new BorderLayout());
      panel.add(new JScrollPane(logArea), BorderLayout.CENTER);
      
      tabbedPane.addTab( "Assertions", buildAssertionsPanel() );
      tabbedPane.addTab( "Response Log", panel );
      return UISupport.createTabPanel( tabbedPane, true );
   }
	
	private JComponent buildAssertionsPanel()
   {
   	assertionsPanel = new AssertionsPanel( getModelItem() )
   	{
			protected void selectError(AssertionError error)
			{
				ModelItemXmlEditor<?,?> editor = getResponseEditor();
				editor.requestFocus();
			}
   	};
   	
   	return assertionsPanel;
   }

	@Override
	public boolean onClose( boolean canCancel )
	{
		getModelItem().getTestCase().removeTestRunListener( testRunListener );
		SoapUI.getTestMonitor().removeTestMonitorListener( testMonitorListener );
		assertionsPanel.release();
		
		return super.onClose( canCancel );
	}
	
	public void setEnabled( boolean enabled )
	{
		super.setEnabled( enabled );
		
		pathField.setEnabled( enabled );
		portField.setEnabled( enabled );
	}
	
	public boolean dependsOn(ModelItem modelItem)
	{
		return modelItem == getModelItem() || modelItem == getModelItem().getTestCase() 
			|| modelItem == getModelItem().getOperation() || modelItem == getModelItem().getOperation().getInterface()
			|| modelItem == getModelItem().getTestCase().getTestSuite() 
			|| modelItem == getModelItem().getTestCase().getTestSuite().getProject();
	}

	public class InternalTestRunListener extends TestRunListenerAdapter
	{
		@Override
		public void afterRun( TestRunner testRunner, TestRunContext runContext )
		{
			setEnabled( true );
		}

		@Override
		public void beforeRun( TestRunner testRunner, TestRunContext runContext )
		{
			setEnabled( false );
		}

		@Override
		public void beforeStep( TestRunner testRunner, TestRunContext runContext )
		{
			if( runContext.getCurrentStep() == getModelItem() )
			{
				logArea.setText( logArea.getText() + new Date( System.currentTimeMillis() ).toString() + 
							": Waiting for request on http://127.0.0.1:" + getModelItem().getPort() + getModelItem().getPath() + "\r\n");
			}
		}

		@Override
		public void afterStep( TestRunner testRunner, TestRunContext runContext, TestStepResult result )
		{
			if( result.getTestStep() == getModelItem() )
			{
				String msg = new Date( result.getTimeStamp()).toString() + ": Handled request in " + result.getTimeTaken() + "ms";
				logArea.setText( logArea.getText() + msg + "\r\n" );
			}
		}
	}
	
	private class InternalTestMonitorListener extends TestMonitorListenerAdapter
	{
		public void loadTestFinished(LoadTestRunner runner)
		{
			setEnabled( !SoapUI.getTestMonitor().hasRunningTest( getModelItem().getTestCase() ) );
		}

		public void loadTestStarted(LoadTestRunner runner)
		{
			if( runner.getLoadTest().getTestCase() == getModelItem().getTestCase() )
				setEnabled( false );
		}

		public void testCaseFinished(TestRunner runner)
		{
			setEnabled(	!SoapUI.getTestMonitor().hasRunningTest( getModelItem().getTestCase() ) );
		}

		public void testCaseStarted(TestRunner runner)
		{
			if( runner.getTestCase() == getModelItem().getTestCase())
				setEnabled( false );
		}
	}
}
