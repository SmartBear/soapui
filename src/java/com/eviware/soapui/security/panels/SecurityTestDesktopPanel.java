/*
 *  soapUI, copyright (C) 2004-2010 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.security.panels;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.ListModel;
import javax.swing.SwingConstants;
import javax.swing.text.Document;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.support.actions.ShowOnlineHelpAction;
import com.eviware.soapui.impl.wsdl.panels.support.MockTestRunContext;
import com.eviware.soapui.impl.wsdl.panels.support.MockTestRunner;
import com.eviware.soapui.impl.wsdl.panels.testcase.actions.SetCredentialsAction;
import com.eviware.soapui.impl.wsdl.panels.testcase.actions.SetEndpointAction;
import com.eviware.soapui.impl.wsdl.panels.teststeps.support.AbstractGroovyEditorModel;
import com.eviware.soapui.impl.wsdl.panels.teststeps.support.PropertyHolderTable;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.registry.WsdlTestStepFactory;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.testsuite.TestCaseRunContext;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.security.SecurityTest;
import com.eviware.soapui.security.SecurityTestRunnerInterface;
import com.eviware.soapui.security.SecurityTestRunnerImpl;
import com.eviware.soapui.security.actions.SecurityTestOptionsAction;
import com.eviware.soapui.security.log.JSecurityTestRunLog;
import com.eviware.soapui.security.support.ProgressBarSecurityTestAdapter;
import com.eviware.soapui.settings.UISettings;
import com.eviware.soapui.support.DocumentListenerAdapter;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.swing.SwingActionDelegate;
import com.eviware.soapui.support.components.GroovyEditorComponent;
import com.eviware.soapui.support.components.GroovyEditorInspector;
import com.eviware.soapui.support.components.JComponentInspector;
import com.eviware.soapui.support.components.JFocusableComponentInspector;
import com.eviware.soapui.support.components.JInspectorPanel;
import com.eviware.soapui.support.components.JInspectorPanelFactory;
import com.eviware.soapui.support.components.JUndoableTextArea;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.support.dnd.JListDragAndDropable;
import com.eviware.soapui.support.swing.ComponentBag;
import com.eviware.soapui.support.types.StringToObjectMap;
import com.eviware.soapui.ui.support.ModelItemDesktopPanel;

/**
 * SecurityTest desktop panel
 * 
 * 
 * this is just first rough version created by copy-pasting from
 * WsdlTestCaseDesktoppanel therefore a lot of variables have not been renamed
 * yet, and a lot of code my be unused, or missing
 * 
 * @author dragica.soldo
 */

public class SecurityTestDesktopPanel extends ModelItemDesktopPanel<SecurityTest>
{
	private JSecurityTestTestStepList testStepList;
	private JProgressBar progressBar;
	private JButton runButton;
	private JButton cancelButton;
	private SecurityTestRunnerInterface runner;
	private JButton setEndpointButton;
	private JButton setCredentialsButton;
	private JButton optionsButton;
	private JSecurityTestRunLog securitytestLog;
	private JToggleButton loopButton;
	private ProgressBarSecurityTestAdapter progressBarAdapter;
	private ComponentBag stateDependantComponents = new ComponentBag();
	public boolean canceled;
	private JTextArea descriptionArea;
	private PropertyHolderTable propertiesTable;
	private GroovyEditorComponent tearDownGroovyEditor;
	private GroovyEditorComponent setupGroovyEditor;
	private JInspectorPanel testStepListInspectorPanel;
	// private JButton createLoadTestButton;
	// private JButton createSecurityTestButton;
	private JInspectorPanel inspectorPanel;
	public SecurityTestRunnerInterface lastRunner;
	// private JButton runWithLoadUIButton;
	// private JButton synchronizeWithLoadUIButton;
	private SecurityTest securityTest;
	protected JXToolBar toolbar;

	public SecurityTestDesktopPanel( SecurityTest securityTest )
	{
		super( securityTest );

		buildUI();

		setPreferredSize( new Dimension( 400, 550 ) );
		this.securityTest = securityTest;
		progressBarAdapter = new ProgressBarSecurityTestAdapter( progressBar, securityTest );
	}

	private void buildUI()
	{
		JPanel panel = new JPanel( new BorderLayout() );

		panel.add( buildToolbar(), BorderLayout.PAGE_START );
		panel.add( buildRunnerBar(), BorderLayout.CENTER );

		add( panel, BorderLayout.NORTH );

		inspectorPanel = JInspectorPanelFactory.build( buildContent() );
		inspectorPanel.addInspector( new JComponentInspector<JComponent>( buildTestLog(), "Security Log",
				"Security Execution Log", true ) );
		inspectorPanel.setDefaultDividerLocation( 0.7F );
		inspectorPanel.setCurrentInspector( "Security Log" );

		if( StringUtils.hasContent( getModelItem().getDescription() )
				&& getModelItem().getSettings().getBoolean( UISettings.SHOW_DESCRIPTIONS ) )
		{
			testStepListInspectorPanel.setCurrentInspector( "Description" );
		}

		add( inspectorPanel.getComponent(), BorderLayout.CENTER );
	}

	private Component buildRunnerBar()
	{
		// progressBar = new JProgressBar( 0, getModelItem().getTestStepCount() );
		progressBar = new JProgressBar( 0, getModelItem().getTestCase().getTestStepCount() );
		return UISupport.createProgressBarPanel( progressBar, 10, false );
	}

	private JComponent buildTestLog()
	{
		securitytestLog = new JSecurityTestRunLog( getModelItem() );
		stateDependantComponents.add( securitytestLog );
		return securitytestLog;
	}

	private JComponent buildContent()
	{
		JTabbedPane tabs = new JTabbedPane( JTabbedPane.TOP );
		testStepListInspectorPanel = JInspectorPanelFactory.build( buildTestStepList(), SwingConstants.BOTTOM );

		tabs.addTab( "TestSteps", testStepListInspectorPanel.getComponent() );

		addTabs( tabs, testStepListInspectorPanel );
		tabs.setTabLayoutPolicy( JTabbedPane.SCROLL_TAB_LAYOUT );

		return UISupport.createTabPanel( tabs, true );
	}

	protected JComponent buildTestStepList()
	{
		JPanel p = new JPanel( new BorderLayout() );
		JXToolBar toolbar = UISupport.createToolbar();

		p.add( toolbar, BorderLayout.NORTH );
		testStepList = new JSecurityTestTestStepList( getModelItem() );
		stateDependantComponents.add( testStepList );

		p.add( new JScrollPane( testStepList ), BorderLayout.CENTER );

		return p;
	}

	protected void addTabs( JTabbedPane tabs, JInspectorPanel inspectorPanel )
	{
		inspectorPanel.addInspector( new JFocusableComponentInspector<JPanel>( buildDescriptionPanel(), descriptionArea,
				"Description", "SecurityTest Description", true ) );
		inspectorPanel.addInspector( new JComponentInspector<JComponent>( buildPropertiesPanel(), "Properties",
				"SecurityTest level properties", true ) );
		inspectorPanel.addInspector( new GroovyEditorInspector( buildSetupScriptPanel(), "Setup Script",
				"Script to run before tunning a SecurityTest" ) );
		inspectorPanel.addInspector( new GroovyEditorInspector( buildTearDownScriptPanel(), "TearDown Script",
				"Script to run after a SecurityTest Run" ) );
	}

	protected GroovyEditorComponent buildTearDownScriptPanel()
	{
		tearDownGroovyEditor = new GroovyEditorComponent( new TearDownScriptGroovyEditorModel(), null );
		return tearDownGroovyEditor;
	}

	protected GroovyEditorComponent buildSetupScriptPanel()
	{
		setupGroovyEditor = new GroovyEditorComponent( new SetupScriptGroovyEditorModel(), null );
		return setupGroovyEditor;
	}

	protected JComponent buildPropertiesPanel()
	{
		JPanel panel = new JPanel( new BorderLayout() );
		propertiesTable = buildPropertiesTable();
		panel.add( propertiesTable, BorderLayout.CENTER );
		return panel;
	}

	protected PropertyHolderTable buildPropertiesTable()
	{
		return new PropertyHolderTable( getModelItem() );
	}

	private JPanel buildDescriptionPanel()
	{
		JPanel panel = new JPanel( new BorderLayout() );
		descriptionArea = new JUndoableTextArea( getModelItem().getDescription() );
		descriptionArea.getDocument().addDocumentListener( new DocumentListenerAdapter()
		{
			public void update( Document document )
			{
				getModelItem().setDescription( descriptionArea.getText() );
			}
		} );

		panel.setBorder( BorderFactory.createEmptyBorder( 2, 2, 2, 2 ) );
		panel.add( new JScrollPane( descriptionArea ), BorderLayout.CENTER );
		UISupport.addTitledBorder( panel, "SecurityTest Description" );

		return panel;
	}

	private Component buildToolbar()
	{
		toolbar = UISupport.createToolbar();

		runButton = UISupport.createToolbarButton( new RunSecurityTestAction() );
		optionsButton = UISupport.createToolbarButton( SwingActionDelegate.createDelegate(
				SecurityTestOptionsAction.SOAPUI_ACTION_ID, getModelItem(), null, "/options.gif" ) );
		optionsButton.setText( null );
		cancelButton = UISupport.createToolbarButton( new CancelRunSecuritytestAction(), false );

		loopButton = new JToggleButton( UISupport.createImageIcon( "/loop.gif" ) );
		loopButton.setPreferredSize( UISupport.getPreferredButtonSize() );
		loopButton.setToolTipText( "Loop TestCase continuously" );

		setCredentialsButton = UISupport.createToolbarButton( new SetCredentialsAction( getModelItem().getTestCase() ) );
		setEndpointButton = UISupport.createToolbarButton( new SetEndpointAction( getModelItem().getTestCase() ) );

		stateDependantComponents.add( runButton );
		stateDependantComponents.add( optionsButton );
		stateDependantComponents.add( cancelButton );
		stateDependantComponents.add( setCredentialsButton );
		stateDependantComponents.add( setEndpointButton );

		addToolbarActions( toolbar );

		toolbar.addSeparator();
		toolbar.add( Box.createHorizontalGlue() );
		toolbar.add( UISupport.createToolbarButton( new ShowOnlineHelpAction( HelpUrls.TESTCASEEDITOR_HELP_URL ) ) );

		return toolbar;
	}

	protected void addToolbarActions( JToolBar toolbar )
	{
		toolbar.add( runButton );
		toolbar.add( cancelButton );
		toolbar.add( loopButton );
		toolbar.addSeparator();
		toolbar.add( setCredentialsButton );
		toolbar.add( setEndpointButton );
		toolbar.addSeparator();
		// toolbar.add( createLoadTestButton );
		// toolbar.add( createSecurityTestButton );
		toolbar.add( optionsButton );
		
		// toolbar.add( runWithLoadUIButton );
		// toolbar.add( convertToLoadUIButton );
		// toolbar.add( synchronizeWithLoadUIButton );
	}

	protected void runSecurityTest()
	{
		if( canceled )
		{

			// make sure state is correct
			runButton.setEnabled( true );
			cancelButton.setEnabled( false );
			// testStepList.setEnabled( true );
			return;
		}

		StringToObjectMap properties = new StringToObjectMap();
		properties.put( "loopButton", loopButton );
		properties.put( TestCaseRunContext.INTERACTIVE, Boolean.TRUE );
		lastRunner = null;
		SecurityTestRunnerImpl testRunner = new SecurityTestRunnerImpl( securityTest );

		// testRunner.run();

		runner = getModelItem().run( properties, true );
	}

	public class RunSecurityTestAction extends AbstractAction
	{
		public RunSecurityTestAction()
		{
			putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/run_testcase.gif" ) );
			putValue( Action.SHORT_DESCRIPTION, "Runs this securitytest" );
		}

		public void actionPerformed( ActionEvent e )
		{
			canceled = false;
			runSecurityTest();
		}
	}

	public class CancelRunSecuritytestAction extends AbstractAction
	{
		public CancelRunSecuritytestAction()
		{
			putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/stop_testcase.gif" ) );
			putValue( Action.SHORT_DESCRIPTION, "Stops running this securitytest" );
		}

		public void actionPerformed( ActionEvent e )
		{
			if( runner != null )
				runner.cancel( "canceled in UI" );

			canceled = true;
		}
	}

	public boolean onClose( boolean canCancel )
	{
		if( canCancel )
		{
			if( runner != null && runner.getStatus() == TestCaseRunner.Status.RUNNING )
			{
				Boolean retval = UISupport.confirmOrCancel( "Cancel running SecurityTest?", "Cancel Run" );

				if( retval == null )
					return false;
				if( retval.booleanValue() )
				{
					runner.cancel( null );
				}
			}
		}
		else
		{
			if( runner != null && runner.getStatus() == TestCaseRunner.Status.RUNNING )
			{
				runner.cancel( null );
			}
		}

		// SoapUI.getTestMonitor().removeTestMonitorListener( testMonitorListener
		// );
		// getModelItem().removeTestRunListener( testRunListener );
		// testStepList.release();
		progressBarAdapter.release();
		propertiesTable.release();
		inspectorPanel.release();

		setupGroovyEditor.getEditor().release();
		tearDownGroovyEditor.getEditor().release();

		securitytestLog.release();
		lastRunner = null;

		return release();
	}

	// public boolean dependsOn( ModelItem modelItem )
	// {
	// return modelItem == getModelItem() || modelItem ==
	// getModelItem().getTestSuite()
	// || modelItem == getModelItem().getTestSuite().getProject();
	// }

	protected void beforeRun()
	{
	}

	protected void afterRun()
	{
		// runButton.setEnabled( true );
		// cancelButton.setEnabled( false );
		// testStepList.setEnabled( true );
	}

	// TODO - check complete logic!
	private class SetupScriptGroovyEditorModel extends AbstractGroovyEditorModel
	{
		@Override
		public Action createRunAction()
		{
			return new AbstractAction()
			{

				public void actionPerformed( ActionEvent e )
				{
					try
					{
						MockTestRunner mockTestRunner = new MockTestRunner( SecurityTestDesktopPanel.this.getModelItem()
								.getTestCase(), SoapUI.ensureGroovyLog() );
						SecurityTestDesktopPanel.this.getModelItem().getTestCase().runSetupScript(
								new MockTestRunContext( mockTestRunner, null ), mockTestRunner );
					}
					catch( Exception e1 )
					{
						UISupport.showErrorMessage( e1 );
					}
				}
			};
		}

		public SetupScriptGroovyEditorModel()
		{
			super( new String[] { "log", "testCase", "context", "testRunner" }, SecurityTestDesktopPanel.this
					.getModelItem(), "Setup" );
		}

		public String getScript()
		{
			return SecurityTestDesktopPanel.this.getModelItem().getStartupScript();
		}

		public void setScript( String text )
		{
			SecurityTestDesktopPanel.this.getModelItem().setStartupScript( text );
		}
	}

	// TODO - check complete logic!
	private class TearDownScriptGroovyEditorModel extends AbstractGroovyEditorModel
	{
		@Override
		public Action createRunAction()
		{
			return new AbstractAction()
			{

				public void actionPerformed( ActionEvent e )
				{
					try
					{
						MockTestRunner mockTestRunner = new MockTestRunner( SecurityTestDesktopPanel.this.getModelItem()
								.getTestCase(), SoapUI.ensureGroovyLog() );
						SecurityTestDesktopPanel.this.getModelItem().getTestCase().runTearDownScript(
								new MockTestRunContext( mockTestRunner, null ), mockTestRunner );
					}
					catch( Exception e1 )
					{
						UISupport.showErrorMessage( e1 );
					}
				}
			};
		}

		public TearDownScriptGroovyEditorModel()
		{
			super( new String[] { "log", "securityTest", "context", "testRunner" }, SecurityTestDesktopPanel.this
					.getModelItem(), "TearDown" );
		}

		public String getScript()
		{
			return SecurityTestDesktopPanel.this.getModelItem().getTearDownScript();
		}

		public void setScript( String text )
		{
			SecurityTestDesktopPanel.this.getModelItem().setTearDownScript( text );
		}
	}

	private class AddSecurityCheckAction extends AbstractAction implements Runnable
	{
		private final WsdlTestStepFactory factory;

		public AddSecurityCheckAction( WsdlTestStepFactory factory )
		{
			this.factory = factory;
			putValue( SMALL_ICON, UISupport.createImageIcon( factory.getTestStepIconPath() ) );
			putValue( SHORT_DESCRIPTION, "Create a new " + factory.getTestStepName() + " TestStep" );
		}

		public void actionPerformed( ActionEvent e )
		{
			SwingActionDelegate.invoke( this );
		}

		public void run()
		{
			// int ix = testStepList.getTestStepList().getSelectedIndex();
			//
			// String name = UISupport.prompt( "Specify name for new step", ix ==
			// -1 ? "Add Step" : "Insert Step", factory
			// .getTestStepName() );
			// if( name != null )
			// {
			// TestStepConfig newTestStepConfig = factory.createNewTestStep(
			// getModelItem(), name );
			// if( newTestStepConfig != null )
			// {
			// WsdlTestStep testStep = getModelItem().insertTestStep(
			// newTestStepConfig, ix );
			// if( testStep != null )
			// UISupport.selectAndShow( testStep );
			// }
			// }
		}
	}

	public static class ModelItemListDragAndDropable extends JListDragAndDropable<JList>
	{
		public ModelItemListDragAndDropable( JList list, WsdlTestCase testCase )
		{
			super( list, testCase );
		}

		@Override
		public ModelItem getModelItemAtRow( int row )
		{
			return ( ModelItem )getList().getModel().getElementAt( row );
		}

		@Override
		public int getModelItemRow( ModelItem modelItem )
		{
			ListModel model = getList().getModel();

			for( int c = 0; c < model.getSize(); c++ )
			{
				if( model.getElementAt( c ) == modelItem )
					return c;
			}

			return -1;
		}

		public Component getRenderer( ModelItem modelItem )
		{
			return getList().getCellRenderer().getListCellRendererComponent( getList(), modelItem,
					getModelItemRow( modelItem ), true, true );
		}

		@Override
		public void setDragInfo( String dropInfo )
		{
			super.setDragInfo( dropInfo == null || dropInfo.length() == 0 ? null : dropInfo );
		}
	}

	public SecurityTestRunnerInterface getSecurityTestRunner()
	{
		return runner == null ? lastRunner : runner;
	}
	public boolean dependsOn( ModelItem modelItem )
	{
		SecurityTest securityTest = getModelItem();

		return modelItem == securityTest || modelItem == securityTest.getTestCase()
				|| modelItem == securityTest.getTestCase().getTestSuite()
				|| modelItem == securityTest.getTestCase().getTestSuite().getProject();
	}


}
