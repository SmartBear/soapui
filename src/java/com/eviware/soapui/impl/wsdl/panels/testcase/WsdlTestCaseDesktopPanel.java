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

package com.eviware.soapui.impl.wsdl.panels.testcase;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragSource;
import java.awt.event.ActionEvent;
import java.text.SimpleDateFormat;
import java.util.Date;

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
import javax.swing.SwingUtilities;
import javax.swing.text.Document;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.TestStepConfig;
import com.eviware.soapui.impl.support.actions.ShowOnlineHelpAction;
import com.eviware.soapui.impl.wsdl.actions.testcase.AddNewLoadTestAction;
import com.eviware.soapui.impl.wsdl.actions.testcase.TestCaseOptionsAction;
import com.eviware.soapui.impl.wsdl.panels.support.MockTestRunContext;
import com.eviware.soapui.impl.wsdl.panels.support.MockTestRunner;
import com.eviware.soapui.impl.wsdl.panels.support.ProgressBarTestCaseAdapter;
import com.eviware.soapui.impl.wsdl.panels.testcase.actions.SetCredentialsAction;
import com.eviware.soapui.impl.wsdl.panels.testcase.actions.SetEndpointAction;
import com.eviware.soapui.impl.wsdl.panels.teststeps.support.AbstractGroovyEditorModel;
import com.eviware.soapui.impl.wsdl.panels.teststeps.support.PropertyHolderTable;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCaseRunner;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.registry.WsdlTestStepFactory;
import com.eviware.soapui.impl.wsdl.teststeps.registry.WsdlTestStepRegistry;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.support.TestRunListenerAdapter;
import com.eviware.soapui.model.testsuite.LoadTestRunner;
import com.eviware.soapui.model.testsuite.TestCaseRunContext;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.TestStepResult;
import com.eviware.soapui.monitor.support.TestMonitorListenerAdapter;
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
import com.eviware.soapui.support.dnd.DropType;
import com.eviware.soapui.support.dnd.JListDragAndDropable;
import com.eviware.soapui.support.dnd.SoapUIDragAndDropHandler;
import com.eviware.soapui.support.swing.ComponentBag;
import com.eviware.soapui.support.types.StringToObjectMap;
import com.eviware.soapui.ui.support.ModelItemDesktopPanel;

/**
 * WsdlTestCase desktop panel
 * 
 * @author Ole.Matzura
 */

public class WsdlTestCaseDesktopPanel extends ModelItemDesktopPanel<WsdlTestCase>
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
	private JTestCaseTestRunLog testCaseLog;
	private JToggleButton loopButton;
	private ProgressBarTestCaseAdapter progressBarAdapter;
	private InternalTestMonitorListener testMonitorListener;
	public boolean canceled;
	private JTextArea descriptionArea;
	private PropertyHolderTable propertiesTable;
	private GroovyEditorComponent tearDownGroovyEditor;
	private GroovyEditorComponent setupGroovyEditor;
	private JInspectorPanel testStepListInspectorPanel;
	private JButton createLoadTestButton;
	private JInspectorPanel inspectorPanel;

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
	}

	/**
	 * There are three states: - enabled, no testcases or testschedules running -
	 * enabled, standalone testcase running - disabled, testschedule is running
	 */

	private void setRunningState()
	{
		stateDependantComponents.setEnabled( !SoapUI.getTestMonitor().hasRunningLoadTest( getModelItem() ) );
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
		testCaseLog = new JTestCaseTestRunLog( getModelItem() );
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

		return UISupport.createTabPanel( tabs, true );
	}

	protected JComponent buildTestStepList()
	{
		JPanel p = new JPanel( new BorderLayout() );
		JXToolBar toolbar = UISupport.createToolbar();

		WsdlTestStepFactory[] factories = WsdlTestStepRegistry.getInstance().getFactories();
		for( WsdlTestStepFactory factory : factories )
		{
			toolbar.addFixed( UISupport.createToolbarButton( new AddWsdlTestStepAction( factory ) ) );
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
				"Script to run before tunning a TestCase" ) );
		inspectorPanel.addInspector( new GroovyEditorInspector( buildTearDownScriptPanel(), "TearDown Script",
				"Script to run after a TestCase Run" ) );
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
		panel.add( new JScrollPane( propertiesTable ), BorderLayout.CENTER );
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
		UISupport.addTitledBorder( panel, "TestCase Description" );

		return panel;
	}

	private Component buildToolbar()
	{
		JToolBar toolbar = UISupport.createToolbar();

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

		addToolbarActions( toolbar );

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
		toolbar.add( createLoadTestButton );
		toolbar.addSeparator();
		toolbar.add( optionsButton );
	}

	private Component buildRunnerBar()
	{
		progressBar = new JProgressBar( 0, getModelItem().getTestStepCount() );
		return UISupport.createProgressBarPanel( progressBar, 10, false );
	}

	private final class InternalTestMonitorListener extends TestMonitorListenerAdapter
	{
		public void loadTestStarted( LoadTestRunner runner )
		{
			setRunningState();
		}

		public void loadTestFinished( LoadTestRunner runner )
		{
			setRunningState();
		}
	}

	public class InternalTestRunListener extends TestRunListenerAdapter
	{
		private SimpleDateFormat dateFormat;

		public InternalTestRunListener()
		{
			dateFormat = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss.SSS" );
		}

		public void beforeRun( TestCaseRunner testRunner, TestCaseRunContext runContext )
		{
			if( SoapUI.getTestMonitor().hasRunningLoadTest( getModelItem() ) )
				return;

			runButton.setEnabled( false );
			cancelButton.setEnabled( true );
			testStepList.setEnabled( false );
			testStepList.setSelectedIndex( -1 );
			testCaseLog.clear();

			testCaseLog.addText( "Test started at " + dateFormat.format( new Date() ) );

			WsdlTestCaseDesktopPanel.this.beforeRun();

			if( runner == null )
				runner = testRunner;
		}

		public void beforeStep( TestCaseRunner testRunner, TestCaseRunContext runContext, TestStep testStep )
		{
			if( SoapUI.getTestMonitor().hasRunningLoadTest( getModelItem() ) )
				return;

			if( testStep != null )
				testStepList.setSelectedValue( testStep, true );
		}

		public void afterRun( TestCaseRunner testRunner, TestCaseRunContext runContext )
		{
			if( SoapUI.getTestMonitor().hasRunningLoadTest( getModelItem() ) )
				return;

			WsdlTestCaseRunner wsdlRunner = ( WsdlTestCaseRunner )testRunner;

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

		public void afterStep( TestCaseRunner testRunner, TestCaseRunContext runContext, TestStepResult stepResult )
		{
			if( SoapUI.getTestMonitor().hasRunningLoadTest( getModelItem() ) )
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

	public boolean onClose( boolean canCancel )
	{
		if( canCancel )
		{
			if( runner != null && runner.getStatus() == TestCaseRunner.Status.RUNNING )
			{
				Boolean retval = UISupport.confirmOrCancel( "Cancel running TestCase?", "Cancel Run" );

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

		SoapUI.getTestMonitor().removeTestMonitorListener( testMonitorListener );
		getModelItem().removeTestRunListener( testRunListener );
		testStepList.release();
		progressBarAdapter.release();
		propertiesTable.release();
		inspectorPanel.release();

		setupGroovyEditor.getEditor().release();
		tearDownGroovyEditor.getEditor().release();

		testCaseLog.release();

		return release();
	}

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
						MockTestRunner mockTestRunner = new MockTestRunner( WsdlTestCaseDesktopPanel.this.getModelItem(), SoapUI.ensureGroovyLog() );
						WsdlTestCaseDesktopPanel.this.getModelItem().runSetupScript( new MockTestRunContext( mockTestRunner, null ), mockTestRunner );
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
			super( new String[] { "log", "testCase", "context", "testRunner" }, WsdlTestCaseDesktopPanel.this.getModelItem(), "Setup" );
		}

		public String getScript()
		{
			return WsdlTestCaseDesktopPanel.this.getModelItem().getSetupScript();
		}

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
						MockTestRunner mockTestRunner = new MockTestRunner( WsdlTestCaseDesktopPanel.this.getModelItem(), SoapUI.ensureGroovyLog() );
						WsdlTestCaseDesktopPanel.this.getModelItem().runTearDownScript( new MockTestRunContext( mockTestRunner, null ), mockTestRunner );
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
			super( new String[] { "log", "testCase", "context", "testRunner" }, WsdlTestCaseDesktopPanel.this.getModelItem(), "TearDown" );
		}

		public String getScript()
		{
			return WsdlTestCaseDesktopPanel.this.getModelItem().getTearDownScript();
		}

		public void setScript( String text )
		{
			WsdlTestCaseDesktopPanel.this.getModelItem().setTearDownScript( text );
		}
	}

	private class AddWsdlTestStepAction extends AbstractAction
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
			int ix = testStepList.getTestStepList().getSelectedIndex();

			String name = UISupport.prompt( "Specify name for new step", ix == -1 ? "Add Step" : "Insert Step", factory
					.getTestStepName() );
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
}
