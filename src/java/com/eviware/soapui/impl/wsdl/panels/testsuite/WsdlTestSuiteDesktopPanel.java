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

package com.eviware.soapui.impl.wsdl.panels.testsuite;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.text.Document;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.support.actions.ShowOnlineHelpAction;
import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.impl.wsdl.actions.testsuite.AddNewTestCaseAction;
import com.eviware.soapui.impl.wsdl.panels.testcase.TestRunLog;
import com.eviware.soapui.impl.wsdl.panels.testcase.TestRunLog.TestRunLogTestRunListener;
import com.eviware.soapui.impl.wsdl.panels.teststeps.support.AbstractGroovyEditorModel;
import com.eviware.soapui.impl.wsdl.panels.teststeps.support.PropertyHolderTable;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestScenario;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.propertyexpansion.DefaultPropertyExpansionContext;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContext;
import com.eviware.soapui.model.support.TestSuiteListenerAdapter;
import com.eviware.soapui.model.testsuite.TestCase;
import com.eviware.soapui.model.testsuite.TestRunner;
import com.eviware.soapui.model.testsuite.TestSuite.TestSuiteRunType;
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
import com.eviware.soapui.ui.support.ModelItemDesktopPanel;

/**
 * DesktopPanel for WsdlTestSuite
 * 
 * @author Ole.Matzura
 */

@SuppressWarnings( "serial" )
public class WsdlTestSuiteDesktopPanel extends ModelItemDesktopPanel<WsdlTestSuite>
{
	private JProgressBar progressBar;
	private JTestSuiteTestCaseList testCaseList;
	private RunAction runAction = new RunAction();
	private CancelAction cancelAction = new CancelAction();
	private TestSuiteRunner testSuiteRunner = new TestSuiteRunner();
	private JToggleButton sequentialButton;
	private JToggleButton parallellButton;
	private final InternalTestSuiteListener testSuiteListener = new InternalTestSuiteListener();
	private JTextArea descriptionArea;
	private boolean failedTests;
	private PropertyHolderTable propertiesTable;
	private TestRunLog testRunLog;
	private GroovyEditorComponent tearDownGroovyEditor;
	private GroovyEditorComponent setupGroovyEditor;
	private JInspectorPanel testCaseListInspectorPanel;
	private JInspectorPanel inspectorPanel;

	public WsdlTestSuiteDesktopPanel( WsdlTestSuite testSuite )
	{
		super( testSuite );

		buildUI();
		testSuite.addTestSuiteListener( testSuiteListener );
	}

	private void buildUI()
	{
		add( buildToolbar(), BorderLayout.NORTH );
		add( buildContent(), BorderLayout.CENTER );

		setPreferredSize( new Dimension( 500, 500 ) );
	}

	private JComponent buildContent()
	{
		inspectorPanel = JInspectorPanelFactory.build( buildTabs() );
		inspectorPanel.addInspector( new JComponentInspector( buildRunLog(), "TestSuite Log",
				"Log of executed TestCases and TestSteps", true ) );

		if( StringUtils.hasContent( getModelItem().getDescription() )
				&& getModelItem().getSettings().getBoolean( UISettings.SHOW_DESCRIPTIONS ) )
		{
			testCaseListInspectorPanel.setCurrentInspector( "Description" );
		}

		return inspectorPanel.getComponent();
	}

	private JComponent buildRunLog()
	{
		testRunLog = new TestRunLog( getModelItem().getSettings() );
		return testRunLog;
	}

	protected JTestSuiteTestCaseList getTestCaseList()
	{
		return testCaseList;
	}

	@Override
	public void addNotify()
	{
		super.addNotify();
		getModelItem().addTestSuiteListener( testSuiteListener );
	}

	@Override
	public void removeNotify()
	{
		super.removeNotify();
		getModelItem().removeTestSuiteListener( testSuiteListener );
	}

	private JComponent buildToolbar()
	{
		cancelAction.setEnabled( false );
		runAction.setEnabled( getModelItem().getTestCaseCount() > 0 );

		JXToolBar toolbar = UISupport.createToolbar();

		addToolbarActions( toolbar );
		toolbar.addGlue();
		toolbar.add( UISupport.createToolbarButton( new ShowOnlineHelpAction( HelpUrls.TESTSUITE_HELP_URL ) ) );

		progressBar = new JProgressBar( 0, getModelItem().getTestCaseCount() );
		JPanel progressPanel = UISupport.createProgressBarPanel( progressBar, 10, false );

		JPanel panel = new JPanel( new BorderLayout() );

		panel.add( toolbar, BorderLayout.PAGE_START );
		panel.add( progressPanel, BorderLayout.CENTER );

		return panel;
	}

	protected void addToolbarActions( JXToolBar toolbar )
	{
		toolbar.add( UISupport.createToolbarButton( runAction ) );
		toolbar.add( UISupport.createToolbarButton( cancelAction ) );

		toolbar.addRelatedGap();

		ButtonGroup buttonGroup = new ButtonGroup();

		sequentialButton = new JToggleButton( UISupport.createImageIcon( "/sequential.gif" ), true );
		sequentialButton.setToolTipText( "The selected TestCases are run in sequence" );
		sequentialButton.setPreferredSize( UISupport.getPreferredButtonSize() );
		sequentialButton.setSelected( getModelItem().getRunType() == TestSuiteRunType.SEQUENTIAL );
		sequentialButton.addActionListener( new ActionListener()
		{

			public void actionPerformed( ActionEvent e )
			{
				getModelItem().setRunType( TestSuiteRunType.SEQUENTIAL );
			}
		} );

		buttonGroup.add( sequentialButton );

		parallellButton = new JToggleButton( UISupport.createImageIcon( "/parallell.gif" ) );
		parallellButton.setToolTipText( "The selected TestCases are run in parallel" );
		parallellButton.setPreferredSize( UISupport.getPreferredButtonSize() );
		parallellButton.setSelected( getModelItem().getRunType() == TestSuiteRunType.PARALLEL );
		parallellButton.addActionListener( new ActionListener()
		{

			public void actionPerformed( ActionEvent e )
			{
				getModelItem().setRunType( TestSuiteRunType.PARALLEL );
			}
		} );

		buttonGroup.add( parallellButton );

		toolbar.addUnrelatedGap();
		toolbar.add( sequentialButton );
		toolbar.addRelatedGap();
		toolbar.add( parallellButton );
	}

	private JComponent buildTabs()
	{
		JTabbedPane tabs = new JTabbedPane( JTabbedPane.TOP );
		testCaseListInspectorPanel = JInspectorPanelFactory.build( buildTestCaseList( getModelItem() ) );

		tabs.addTab( "TestCases", testCaseListInspectorPanel.getComponent() );

		addTabs( tabs, testCaseListInspectorPanel );
		tabs.setTabLayoutPolicy( JTabbedPane.SCROLL_TAB_LAYOUT );

		return UISupport.createTabPanel( tabs, true );
	}

	protected void addTabs( JTabbedPane tabs, JInspectorPanel inspectorPanel )
	{
		inspectorPanel.addInspector( new JFocusableComponentInspector<JPanel>( buildDescriptionPanel(), descriptionArea,
				"Description", "Description for this TestSuite", true ) );

		inspectorPanel.addInspector( new JComponentInspector( buildPropertiesPanel(), "Properties",
				"TestSuite level properties", true ) );
		inspectorPanel.addInspector( new GroovyEditorInspector( buildSetupScriptPanel(), "Setup Script",
				"Script to run before running TestSuite" ) );
		inspectorPanel.addInspector( new GroovyEditorInspector( buildTearDownScriptPanel(), "TearDown Script",
				"Script to run after running TestSuite" ) );
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

	private JComponent buildPropertiesPanel()
	{
		JPanel panel = new JPanel( new BorderLayout() );
		propertiesTable = createPropertyHolderTable();
		panel.add( new JScrollPane( propertiesTable ), BorderLayout.CENTER );
		return panel;
	}

	protected PropertyHolderTable createPropertyHolderTable()
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
		UISupport.addTitledBorder( panel, "TestSuite Description" );

		return panel;
	}

	protected JComponent buildTestCaseList( WsdlTestSuite testSuite )
	{
		testCaseList = new JTestSuiteTestCaseList( testSuite );

		JPanel p = new JPanel( new BorderLayout() );

		p.add( buildTestCaseListToolbar(), BorderLayout.NORTH );
		p.add( new JScrollPane( testCaseList ), BorderLayout.CENTER );

		return p;
	}

	private Component buildTestCaseListToolbar()
	{
		JXToolBar toolbar = UISupport.createToolbar();
		toolbar.add( UISupport.createToolbarButton( SwingActionDelegate.createDelegate(
				AddNewTestCaseAction.SOAPUI_ACTION_ID, getModelItem(), null, "/testCase.gif" ) ) );
		toolbar.addGlue();
		toolbar.add( UISupport.createToolbarButton( new ShowOnlineHelpAction( HelpUrls.TESTSUITEEDITOR_HELP_URL ) ) );
		return toolbar;
	}

	public boolean onClose( boolean canCancel )
	{
		propertiesTable.release();
		inspectorPanel.release();
		testCaseListInspectorPanel.release();

		setupGroovyEditor.getEditor().release();
		tearDownGroovyEditor.getEditor().release();

		testRunLog.release();

		return super.release();
	}

	public JComponent getComponent()
	{
		return this;
	}

	public boolean dependsOn( ModelItem modelItem )
	{
		return modelItem == getModelItem() || modelItem == getModelItem().getProject();
	}

	protected void runTestSuite()
	{
		new Thread( testSuiteRunner, getModelItem().getName() + " TestSuiteRunner" ).start();
	}

	protected void beforeRun()
	{
		runAction.setEnabled( false );
		cancelAction.setEnabled( true );
		testCaseList.setEnabled( false );
		progressBar.setForeground( Color.GREEN.darker() );

		failedTests = false;
	}

	protected void afterRun()
	{
		runAction.setEnabled( true );
		cancelAction.setEnabled( false );
		testCaseList.setEnabled( true );

		progressBar.setString( failedTests ? "Failed" : testSuiteRunner.isCanceled() ? "Canceled" : "Passed" );
		progressBar.setForeground( failedTests ? Color.RED : Color.GREEN.darker() );
	}

	private final class InternalTestSuiteListener extends TestSuiteListenerAdapter
	{
		public void testCaseAdded( TestCase testCase )
		{
			runAction.setEnabled( getModelItem().getTestCaseCount() > 0 );
		}

		public void testCaseRemoved( TestCase testCase )
		{
			runAction.setEnabled( getModelItem().getTestCaseCount() > 0 );
		}
	}

	private class RunAction extends AbstractAction
	{
		public RunAction()
		{
			putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/run_testcase.gif" ) );
			putValue( Action.SHORT_DESCRIPTION, "Runs the selected TestCases" );
		}

		public void actionPerformed( ActionEvent e )
		{
			runTestSuite();
		}
	}

	private class CancelAction extends AbstractAction
	{
		public CancelAction()
		{
			putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/stop_testcase.gif" ) );
			putValue( Action.SHORT_DESCRIPTION, "Cancels ongoing TestCase runs" );
		}

		public void actionPerformed( ActionEvent e )
		{
			testSuiteRunner.cancel();
		}
	}

	/**
	 * Runs the selected testsuites..
	 * 
	 * @author Ole.Matzura
	 */

	public class TestSuiteRunner extends WsdlTestScenario
	{
		private TestRunLogTestRunListener runLogListener;
		private int finishCount;

		public TestSuiteRunner()
		{
			super( TestSuiteRunType.SEQUENTIAL );
		}

		public void run()
		{
			setRunType( getModelItem().getRunType() );

			removeAllTestCases();

			testCaseList.reset();

			for( TestCase testCase : getModelItem().getTestCaseList() )
			{
				if( !testCase.isDisabled() )
					addTestCase( testCase );
			}

			super.run();
		}

		protected PropertyExpansionContext createContext()
		{
			return new DefaultPropertyExpansionContext( getModelItem() );
		}

		public void beforeRun( PropertyExpansionContext context )
		{
			super.beforeRun( context );

			WsdlTestSuiteDesktopPanel.this.beforeRun();

			progressBar.setMaximum( getTestCaseCount() );
			progressBar.setValue( 0 );
			progressBar.setString( "" );
			finishCount = 0;

			if( runLogListener == null )
				runLogListener = new TestRunLog.TestRunLogTestRunListener( testRunLog, false );

			testRunLog.clear();

			if( getRunType() == TestSuiteRunType.PARALLEL )
				testRunLog.addText( "<log disabled during parallell execution>" );

			try
			{
				getModelItem().runSetupScript( context );
			}
			catch( Exception e )
			{
				SoapUI.logError( e );
			}
		}

		@Override
		protected void afterTestCase( TestCase testCase, TestRunner runner )
		{
			super.afterTestCase( testCase, runner );
			progressBar.setValue( ++finishCount );
			if( runner.getStatus() == TestRunner.Status.FAILED )
				failedTests = true;

			if( getRunType() == TestSuiteRunType.SEQUENTIAL )
				testCase.removeTestRunListener( runLogListener );
		}

		@Override
		protected void beforeTestCase( TestCase testCase )
		{
			super.beforeTestCase( testCase );
			progressBar.setString( "Running " + testCase.getName() );

			if( getRunType() == TestSuiteRunType.SEQUENTIAL )
				testCase.addTestRunListener( runLogListener );
		}

		protected void afterRun( PropertyExpansionContext context )
		{
			super.afterRun( context );

			try
			{
				getModelItem().runTearDownScript( context );
			}
			catch( Exception e )
			{
				SoapUI.logError( e );
			}
			finally
			{
				WsdlTestSuiteDesktopPanel.this.afterRun();
			}
		}
	}

	private class SetupScriptGroovyEditorModel extends AbstractGroovyEditorModel
	{
		public SetupScriptGroovyEditorModel()
		{
			super( new String[] { "log", "context", "testSuite" }, getModelItem().getSettings(), "Setup" );
		}

		public String getScript()
		{
			return getModelItem().getSetupScript();
		}

		public void setScript( String text )
		{
			getModelItem().setSetupScript( text );
		}

		@Override
		public Action createRunAction()
		{
			return new AbstractAction()
			{

				public void actionPerformed( ActionEvent e )
				{
					try
					{
						getModelItem().runSetupScript( null );
					}
					catch( Exception e1 )
					{
						UISupport.showErrorMessage( e1 );
					}
				}
			};
		}
	}

	private class TearDownScriptGroovyEditorModel extends AbstractGroovyEditorModel
	{
		public TearDownScriptGroovyEditorModel()
		{
			super( new String[] { "log", "context", "testSuite" }, getModelItem().getSettings(), "TearDown" );
		}

		public String getScript()
		{
			return getModelItem().getTearDownScript();
		}

		public void setScript( String text )
		{
			getModelItem().setTearDownScript( text );
		}

		@Override
		public Action createRunAction()
		{
			return new AbstractAction()
			{

				public void actionPerformed( ActionEvent e )
				{
					try
					{
						getModelItem().runTearDownScript( null );
					}
					catch( Exception e1 )
					{
						UISupport.showErrorMessage( e1 );
					}
				}
			};
		}
	}
}
