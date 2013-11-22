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

package com.eviware.soapui.impl.wsdl.panels.testcase;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.TestStepConfig;
import com.eviware.soapui.impl.support.actions.ShowOnlineHelpAction;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.actions.testcase.AddNewLoadTestAction;
import com.eviware.soapui.impl.wsdl.actions.testcase.AddNewSecurityTestAction;
import com.eviware.soapui.impl.wsdl.actions.testcase.TestCaseOptionsAction;
import com.eviware.soapui.impl.wsdl.panels.support.MockTestRunContext;
import com.eviware.soapui.impl.wsdl.panels.support.MockTestRunner;
import com.eviware.soapui.impl.wsdl.panels.support.ProgressBarTestCaseAdapter;
import com.eviware.soapui.impl.wsdl.panels.testcase.actions.SetCredentialsAction;
import com.eviware.soapui.impl.wsdl.panels.testcase.actions.SetEndpointAction;
import com.eviware.soapui.impl.wsdl.panels.teststeps.support.AbstractGroovyEditorModel;
import com.eviware.soapui.impl.wsdl.panels.teststeps.support.PropertyHolderTable;
import com.eviware.soapui.impl.wsdl.support.AbstractTestCaseRunner;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestRunContext;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.registry.*;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.support.TestRunListenerAdapter;
import com.eviware.soapui.model.testsuite.*;
import com.eviware.soapui.monitor.support.TestMonitorListenerAdapter;
import com.eviware.soapui.security.SecurityTestRunner;
import com.eviware.soapui.settings.UISettings;
import com.eviware.soapui.support.DateUtil;
import com.eviware.soapui.support.DocumentListenerAdapter;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.swing.SwingActionDelegate;
import com.eviware.soapui.support.components.*;
import com.eviware.soapui.support.dnd.DropType;
import com.eviware.soapui.support.dnd.JListDragAndDropable;
import com.eviware.soapui.support.dnd.SoapUIDragAndDropHandler;
import com.eviware.soapui.support.swing.ComponentBag;
import com.eviware.soapui.support.types.StringToObjectMap;
import com.eviware.soapui.ui.support.KeySensitiveModelItemDesktopPanel;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragSource;
import java.awt.event.ActionEvent;
import java.util.Date;

/**
 * WsdlTestCase desktop panel
 * 
 * @author Ole.Matzura
 */

@SuppressWarnings( "serial" )
public class WsdlTestCaseDesktopPanel extends KeySensitiveModelItemDesktopPanel<WsdlTestCase>
{
	private JProgressBar progressBar;
	private JTestStepList testStepList;
	private InternalTestRunListener testRunListener = new InternalTestRunListener();
	private JButton runButton;
	private JButton cancelButton;
	private TestCaseRunner runner;
	private JButton setEndpointButton;
	private JButton setCredentialsButton;
	private JButton optionsButton;
	private ComponentBag stateDependantComponents = new ComponentBag();
	private JTestRunLog testCaseLog;
	private JToggleButton loopButton;
	private ProgressBarTestCaseAdapter progressBarAdapter;
	private InternalTestMonitorListener testMonitorListener;
	public boolean canceled;
	protected JTextArea descriptionArea;
	private PropertyHolderTable propertiesTable;
	private GroovyEditorComponent tearDownGroovyEditor;
	private GroovyEditorComponent setupGroovyEditor;
	private JInspectorPanel testStepListInspectorPanel;
	private JButton createLoadTestButton;
	private JButton createSecurityTestButton;
	private JInspectorPanel inspectorPanel;
	public TestCaseRunner lastRunner;
	private WsdlTestCase testCase;
	protected TestOnDemandPanel testOnDemandPanel;

	public WsdlTestCaseDesktopPanel( WsdlTestCase testCase )
	{
		super( testCase );

		buildUI();

		setPreferredSize( new Dimension( 400, 550 ) );
		setRunningState();

		testCase.addTestRunListener( testRunListener );
		progressBarAdapter = new ProgressBarTestCaseAdapter( progressBar, testCase );
		testMonitorListener = new InternalTestMonitorListener();

		SoapUI.getTestMonitor().addTestMonitorListener( testMonitorListener );

		DragSource dragSource = DragSource.getDefaultDragSource();
		SoapUIDragAndDropHandler dragAndDropHandler = new SoapUIDragAndDropHandler( new ModelItemListDragAndDropable(
				getTestStepList().getTestStepList(), testCase ), DropType.BEFORE_AND_AFTER );

		dragSource.createDefaultDragGestureRecognizer( getTestStepList().getTestStepList(),
				DnDConstants.ACTION_COPY_OR_MOVE, dragAndDropHandler );
		// needed for synchronizing with loadUI
		this.testCase = testCase;
	}

	/**
	 * There are three states: - enabled, no testcases or testschedules running -
	 * enabled, standalone testcase running - disabled, testschedule is running
	 */

	private void setRunningState()
	{
		boolean hasRunningTests = SoapUI.getTestMonitor().hasRunningLoadTest( getModelItem() )
				|| SoapUI.getTestMonitor().hasRunningSecurityTest( getModelItem() );
		stateDependantComponents.setEnabled( !hasRunningTests );

		//disable setting endpoint if in environment mode
		WsdlProject project = getModelItem().getTestSuite().getProject();
		if( project.isEnvironmentMode() )
		{
			getSetEndpointButton().setEnabled( false );
		}
		else
		{
			getSetEndpointButton().setEnabled( !hasRunningTests );
		}
	}

	private void buildUI()
	{
		JPanel panel = new JPanel( new BorderLayout() );

		panel.add( buildToolbar(), BorderLayout.PAGE_START );
		panel.add( buildRunnerBar(), BorderLayout.CENTER );

		add( panel, BorderLayout.NORTH );

		inspectorPanel = JInspectorPanelFactory.build( buildContent() );
		inspectorPanel.addInspector( new JComponentInspector<JComponent>( buildTestLog(), "TestCase Log",
				"TestCase Execution Log", true ) );
		inspectorPanel.setDefaultDividerLocation( 0.7F );
		inspectorPanel.setCurrentInspector( "TestCase Log" );

		if( StringUtils.hasContent( getModelItem().getDescription() )
				&& getModelItem().getSettings().getBoolean( UISettings.SHOW_DESCRIPTIONS ) )
		{
			testStepListInspectorPanel.setCurrentInspector( "Description" );
		}

		add( inspectorPanel.getComponent(), BorderLayout.CENTER );
	}

	protected JTestStepList getTestStepList()
	{
		return testStepList;
	}

	private JComponent buildTestLog()
	{
		testCaseLog = new JTestRunLog( getModelItem().getSettings() );
		stateDependantComponents.add( testCaseLog );
		return testCaseLog;
	}

	private JComponent buildContent()
	{
		JTabbedPane tabs = new JTabbedPane( JTabbedPane.TOP );

		testStepListInspectorPanel = JInspectorPanelFactory.build( buildTestStepList(), SwingConstants.BOTTOM );

		tabs.addTab( "TestSteps", testStepListInspectorPanel.getComponent() );

		addTabs( tabs, testStepListInspectorPanel );

		tabs.setTabLayoutPolicy( JTabbedPane.SCROLL_TAB_LAYOUT );

		tabs.addChangeListener( new TestOnDemandTabChangeListener() );

		return UISupport.createTabPanel( tabs, true );
	}

	protected JComponent buildTestStepList()
	{
		JPanel p = new JPanel( new BorderLayout() );
		JXToolBar toolbar = UISupport.createToolbar();

		WsdlTestStepFactory[] factories = WsdlTestStepRegistry.getInstance().getFactories();
		for( WsdlTestStepFactory factory : factories )
		{
			JButton testStepButton = UISupport.createToolbarButton( new AddWsdlTestStepAction( factory ) );
			if( factory instanceof ProPlaceholderStepFactory )
				testStepButton.setEnabled( false );
			toolbar.addFixed( testStepButton );
			String type = factory.getType();
			if( type.equals( JdbcRequestTestStepFactory.JDBC_TYPE )
					|| type.equals( PropertyTransfersStepFactory.TRANSFER_TYPE )
					|| type.equals( "datasourceloop" )
					|| type.equals( RunTestCaseStepFactory.RUNTESTCASE_TYPE )
					|| type.equals( ManualTestStepFactory.MANUAL_TEST_STEP ) )
			{
				toolbar.addRelatedGap();
			}
		}

		p.add( toolbar, BorderLayout.NORTH );
		testStepList = new JTestStepList( getModelItem() );
		stateDependantComponents.add( testStepList );

		p.add( new JScrollPane( testStepList ), BorderLayout.CENTER );

		return p;
	}

	protected void addTabs( JTabbedPane tabs, JInspectorPanel inspectorPanel )
	{
		inspectorPanel.addInspector( new JFocusableComponentInspector<JPanel>( buildDescriptionPanel(), descriptionArea,
				"Description", "TestCase Description", true ) );
		inspectorPanel.addInspector( new JComponentInspector<JComponent>( buildPropertiesPanel(), "Properties",
				"TestCase level properties", true ) );
		inspectorPanel.addInspector( new GroovyEditorInspector( buildSetupScriptPanel(), "Setup Script",
				"Script to run before running a TestCase" ) );
		inspectorPanel.addInspector( new GroovyEditorInspector( buildTearDownScriptPanel(), "TearDown Script",
				"Script to run after a TestCase Run" ) );

		tabs.addTab( "Test On Demand", buildTestOnDemandPanel() );
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

	protected JPanel buildDescriptionPanel()
	{
		JPanel panel = new JPanel( new BorderLayout() );
		descriptionArea = new JUndoableTextArea( getModelItem().getDescription() );
		descriptionArea.getDocument().addDocumentListener( new DocumentListenerAdapter()
		{
			@Override
			public void update( Document document )
			{
				getModelItem().setDescription( descriptionArea.getText() );
			}
		} );

		panel.setBorder( BorderFactory.createEmptyBorder( 2, 2, 2, 2 ) );
		panel.add( new JScrollPane( descriptionArea ), BorderLayout.CENTER );
		UISupport.addTitledBorder( panel, "TestCase Description" );

		return panel;
	}

	protected Component buildToolbar()
	{
		JXToolBar toolbar = UISupport.createToolbar();

		runButton = UISupport.createToolbarButton( new RunTestCaseAction() );
		optionsButton = UISupport.createToolbarButton( SwingActionDelegate.createDelegate(
				TestCaseOptionsAction.SOAPUI_ACTION_ID, getModelItem(), null, "/options.gif" ) );
		optionsButton.setText( null );
		cancelButton = UISupport.createToolbarButton( new CancelRunTestCaseAction(), false );

		loopButton = new JToggleButton( UISupport.createImageIcon( "/loop.gif" ) );
		loopButton.setPreferredSize( UISupport.getPreferredButtonSize() );
		loopButton.setToolTipText( "Loop TestCase continuously" );

		setCredentialsButton = UISupport.createToolbarButton( new SetCredentialsAction( getModelItem() ) );
		setEndpointButton = UISupport.createToolbarButton( new SetEndpointAction( getModelItem() ) );

		stateDependantComponents.add( runButton );
		stateDependantComponents.add( optionsButton );
		stateDependantComponents.add( cancelButton );
		stateDependantComponents.add( setCredentialsButton );
		stateDependantComponents.add( setEndpointButton );

		createLoadTestButton = UISupport.createToolbarButton( SwingActionDelegate.createDelegate(
				AddNewLoadTestAction.SOAPUI_ACTION_ID, getModelItem(), null, "/loadTest.gif" ) );

		createSecurityTestButton = UISupport.createToolbarButton( SwingActionDelegate.createDelegate(
				AddNewSecurityTestAction.SOAPUI_ACTION_ID, getModelItem(), null, "/security_test.gif" ) );

		addToolbarActions( toolbar );

		toolbar.add( Box.createHorizontalGlue() );
		toolbar.add( UISupport.createToolbarButton( new ShowOnlineHelpAction( HelpUrls.TESTCASEEDITOR_HELP_URL ) ) );

		return toolbar;
	}

	protected JButton getSetEndpointButton()
	{
		return setEndpointButton;
	}

	protected void addToolbarActions( JXToolBar toolbar )
	{
		toolbar.add( runButton );
		toolbar.add( cancelButton );
		toolbar.add( loopButton );
		toolbar.addSeparator();
		toolbar.add( setCredentialsButton );
		toolbar.add( setEndpointButton );
		toolbar.addSeparator();
		toolbar.add( createLoadTestButton );
		toolbar.add( createSecurityTestButton );
		toolbar.addSeparator();
		toolbar.add( optionsButton );
	}

	private Component buildRunnerBar()
	{
		progressBar = new JProgressBar( 0, getModelItem().getTestStepCount() );
		return UISupport.createProgressBarPanel( progressBar, 10, false );
	}

	private final class TestOnDemandTabChangeListener implements ChangeListener
	{
		@Override
		public void stateChanged( ChangeEvent evt )
		{
			JTabbedPane pane = ( JTabbedPane )evt.getSource();
			if( pane.getSelectedComponent().equals( testOnDemandPanel ) )
			{
				testOnDemandPanel.initializeLocationsCache();
			}
		}
	}

	private final class InternalTestMonitorListener extends TestMonitorListenerAdapter
	{
		@Override
		public void loadTestStarted( LoadTestRunner runner )
		{
			setRunningState();
		}

		@Override
		public void loadTestFinished( LoadTestRunner runner )
		{
			setRunningState();
		}

		@Override
		public void securityTestStarted( SecurityTestRunner runner )
		{
			setRunningState();
		}

		@Override
		public void securityTestFinished( SecurityTestRunner runner )
		{
			setRunningState();
		}
	}

	public class InternalTestRunListener extends TestRunListenerAdapter
	{

		public InternalTestRunListener()
		{
		}

		@Override
		public void beforeRun( TestCaseRunner testRunner, TestCaseRunContext runContext )
		{
			if( SoapUI.getTestMonitor().hasRunningLoadTest( getModelItem() )
					|| SoapUI.getTestMonitor().hasRunningSecurityTest( getModelItem() ) )
				return;

			runButton.setEnabled( false );
			cancelButton.setEnabled( true );
			testStepList.setEnabled( false );
			testStepList.setSelectedIndex( -1 );
			testCaseLog.clear();

			testCaseLog.addText( "Test started at " + DateUtil.formatExtraFull( new Date() ) );

			WsdlTestCaseDesktopPanel.this.beforeRun();

			if( runner == null )
				runner = testRunner;
		}

		@Override
		public synchronized void beforeStep( TestCaseRunner testRunner, TestCaseRunContext runContext,
				final TestStep testStep )
		{
			if( SoapUI.getTestMonitor().hasRunningLoadTest( getModelItem() )
					|| SoapUI.getTestMonitor().hasRunningSecurityTest( getModelItem() ) )
				return;

			if( testStep != null )
			{
				if( SwingUtilities.isEventDispatchThread() )
				{
					testStepList.setSelectedValue( testStep, true );
				}
				else
				{
					SwingUtilities.invokeLater( new Runnable()
					{
						public void run()
						{
							testStepList.setSelectedValue( testStep, true );
						}
					} );
				}
			}
		}

		@Override
		public void afterRun( TestCaseRunner testRunner, TestCaseRunContext runContext )
		{
			if( SoapUI.getTestMonitor().hasRunningLoadTest( getModelItem() )
					|| SoapUI.getTestMonitor().hasRunningSecurityTest( getModelItem() ) )
				return;

			AbstractTestCaseRunner<TestRunnable, WsdlTestRunContext> wsdlRunner = ( AbstractTestCaseRunner<TestRunnable, WsdlTestRunContext> )testRunner;

			if( testRunner.getStatus() == TestCaseRunner.Status.CANCELED )
				testCaseLog.addText( "TestCase canceled [" + testRunner.getReason() + "], time taken = "
						+ wsdlRunner.getTimeTaken() );
			else if( testRunner.getStatus() == TestCaseRunner.Status.FAILED )
			{
				String msg = wsdlRunner.getReason();
				if( wsdlRunner.getError() != null )
				{
					if( msg != null )
						msg += ":";

					msg += wsdlRunner.getError();
				}

				testCaseLog.addText( "TestCase failed [" + msg + "], time taken = " + wsdlRunner.getTimeTaken() );
			}
			else
				testCaseLog.addText( "TestCase finished with status [" + testRunner.getStatus() + "], time taken = "
						+ wsdlRunner.getTimeTaken() );

			lastRunner = runner;
			runner = null;

			JToggleButton loopButton = ( JToggleButton )runContext.getProperty( "loopButton" );
			if( loopButton != null && loopButton.isSelected() && testRunner.getStatus() == TestCaseRunner.Status.FINISHED )
			{
				SwingUtilities.invokeLater( new Runnable()
				{
					public void run()
					{
						runTestCase();
					}
				} );
			}
			else
			{
				WsdlTestCaseDesktopPanel.this.afterRun();
			}
		}

		@Override
		public void afterStep( TestCaseRunner testRunner, TestCaseRunContext runContext, TestStepResult stepResult )
		{
			if( SoapUI.getTestMonitor().hasRunningLoadTest( getModelItem() )
					|| SoapUI.getTestMonitor().hasRunningSecurityTest( getModelItem() ) )
				return;

			testCaseLog.addTestStepResult( stepResult );
		}
	}

	protected void runTestCase()
	{
		if( canceled )
		{
			// make sure state is correct
			runButton.setEnabled( true );
			cancelButton.setEnabled( false );
			testStepList.setEnabled( true );
			return;
		}

		StringToObjectMap properties = new StringToObjectMap();
		properties.put( "loopButton", loopButton );
		properties.put( TestCaseRunContext.INTERACTIVE, Boolean.TRUE );
		lastRunner = null;
		runner = getModelItem().run( properties, true );
	}

	public class RunTestCaseAction extends AbstractAction
	{
		public RunTestCaseAction()
		{
			putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/run_testcase.gif" ) );
			putValue( Action.SHORT_DESCRIPTION, "Runs this testcase" );
		}

		public void actionPerformed( ActionEvent e )
		{
			canceled = false;
			runTestCase();
		}
	}

	public class CancelRunTestCaseAction extends AbstractAction
	{
		public CancelRunTestCaseAction()
		{
			putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/stop_testcase.gif" ) );
			putValue( Action.SHORT_DESCRIPTION, "Stops running this testcase" );
		}

		public void actionPerformed( ActionEvent e )
		{
			if( runner != null )
				runner.cancel( "canceled in UI" );

			canceled = true;
		}
	}

	@Override
	public boolean onClose( boolean canCancel )
	{
		if( canCancel )
		{
			if( runner != null && runner.getStatus() == TestCaseRunner.Status.RUNNING )
			{
				Boolean retval = UISupport.confirmOrCancel( "Cancel running TestCase?", "Cancel Run" );

				if( retval == null )
					return false;
				if( retval )
				{
					if( runner != null )
						runner.cancel( null );
				}
			}
		}
		else
		{
			if( runner != null && runner.getStatus() == TestCaseRunner.Status.RUNNING )
			{
				if( runner != null )
					runner.cancel( null );
			}
		}

		SoapUI.getTestMonitor().removeTestMonitorListener( testMonitorListener );
		getModelItem().removeTestRunListener( testRunListener );
		testStepList.release();
		progressBarAdapter.release();
		propertiesTable.release();
		inspectorPanel.release();

		setupGroovyEditor.getEditor().release();
		tearDownGroovyEditor.getEditor().release();

		testCaseLog.release();
		lastRunner = null;

		if( testOnDemandPanel != null )
		{
			testOnDemandPanel.release();
		}

		return release();
	}

	@Override
	public boolean dependsOn( ModelItem modelItem )
	{
		return modelItem == getModelItem() || modelItem == getModelItem().getTestSuite()
				|| modelItem == getModelItem().getTestSuite().getProject();
	}

	protected void beforeRun()
	{
	}

	protected void afterRun()
	{
		runButton.setEnabled( true );
		cancelButton.setEnabled( false );
		testStepList.setEnabled( true );
	}

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
						MockTestRunner mockTestRunner = new MockTestRunner( WsdlTestCaseDesktopPanel.this.getModelItem(),
								SoapUI.ensureGroovyLog() );
						WsdlTestCaseDesktopPanel.this.getModelItem().runSetupScript(
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
			super( new String[] { "log", "testCase", "context", "testRunner" }, WsdlTestCaseDesktopPanel.this
					.getModelItem(), "Setup" );
		}

		@Override
		public String getScript()
		{
			return WsdlTestCaseDesktopPanel.this.getModelItem().getSetupScript();
		}

		@Override
		public void setScript( String text )
		{
			WsdlTestCaseDesktopPanel.this.getModelItem().setSetupScript( text );
		}
	}

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
						MockTestRunner mockTestRunner = new MockTestRunner( WsdlTestCaseDesktopPanel.this.getModelItem(),
								SoapUI.ensureGroovyLog() );
						WsdlTestCaseDesktopPanel.this.getModelItem().runTearDownScript(
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
			super( new String[] { "log", "testCase", "context", "testRunner" }, WsdlTestCaseDesktopPanel.this
					.getModelItem(), "TearDown" );
		}

		@Override
		public String getScript()
		{
			return WsdlTestCaseDesktopPanel.this.getModelItem().getTearDownScript();
		}

		@Override
		public void setScript( String text )
		{
			WsdlTestCaseDesktopPanel.this.getModelItem().setTearDownScript( text );
		}
	}

	private class AddWsdlTestStepAction extends AbstractAction implements Runnable
	{
		private final WsdlTestStepFactory factory;

		public AddWsdlTestStepAction( WsdlTestStepFactory factory )
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
			if( !factory.canAddTestStepToTestCase( testCase ) )
				return;

			int ix = testStepList.getTestStepList().getSelectedIndex();

			String name = UISupport.prompt( "Specify name for new step", ix == -1 ? "Add Step" : "Insert Step",
					factory.getTestStepName() );
			if( name != null )
			{
				TestStepConfig newTestStepConfig = factory.createNewTestStep( getModelItem(), name );
				if( newTestStepConfig != null )
				{
					WsdlTestStep testStep = getModelItem().insertTestStep( newTestStepConfig, ix );
					if( testStep != null )
						UISupport.selectAndShow( testStep );
				}
			}
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

	public TestCaseRunner getTestCaseRunner()
	{
		return runner == null ? lastRunner : runner;
	}

	@Override
	protected void renameModelItem()
	{
		SoapUI.getActionRegistry().performAction( "RenameTestCaseAction", getModelItem(), null );
	}

	@Override
	protected void cloneModelItem()
	{
		SoapUI.getActionRegistry().performAction( "CloneTestCaseAction", getModelItem(), null );
	}

	protected Component buildTestOnDemandPanel()
	{
		testOnDemandPanel = new TestOnDemandPanel( getModelItem() );
		return testOnDemandPanel;
	}
}
