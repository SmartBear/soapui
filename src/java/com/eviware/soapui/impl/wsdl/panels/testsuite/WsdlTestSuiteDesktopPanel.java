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

import com.eviware.soapui.impl.support.actions.ShowOnlineHelpAction;
import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.impl.wsdl.actions.testsuite.AddNewTestCaseAction;
import com.eviware.soapui.impl.wsdl.panels.support.MockTestSuiteRunner;
import com.eviware.soapui.impl.wsdl.panels.testcase.JTestRunLog;
import com.eviware.soapui.impl.wsdl.panels.testcase.TestRunLogTestRunListener;
import com.eviware.soapui.impl.wsdl.panels.teststeps.support.AbstractGroovyEditorModel;
import com.eviware.soapui.impl.wsdl.panels.teststeps.support.PropertyHolderTable;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestSuiteRunner;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.support.TestSuiteListenerAdapter;
import com.eviware.soapui.model.testsuite.TestCase;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.TestSuiteRunContext;
import com.eviware.soapui.model.testsuite.TestSuiteRunListener;
import com.eviware.soapui.model.testsuite.TestSuiteRunner;
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
import com.eviware.soapui.support.types.StringToObjectMap;
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
	private JToggleButton sequentialButton;
	private JToggleButton parallellButton;
	private final InternalTestSuiteListener testSuiteListener = new InternalTestSuiteListener();
	private final InternalTestSuiteRunListener testSuiteRunListener = new InternalTestSuiteRunListener();
	private JTextArea descriptionArea;
	private PropertyHolderTable propertiesTable;
	private JTestRunLog testRunLog;
	private GroovyEditorComponent tearDownGroovyEditor;
	private GroovyEditorComponent setupGroovyEditor;
	private JInspectorPanel testCaseListInspectorPanel;
	private JInspectorPanel inspectorPanel;
	private WsdlTestSuiteRunner testSuiteRunner;

	public WsdlTestSuiteDesktopPanel( WsdlTestSuite testSuite )
	{
		super( testSuite );

		buildUI();
		testSuite.addTestSuiteListener( testSuiteListener );
		testSuite.addTestSuiteRunListener( testSuiteRunListener );
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
		inspectorPanel.addInspector( new JComponentInspector<JComponent>( buildRunLog(), "TestSuite Log",
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
		testRunLog = new JTestRunLog( getModelItem().getSettings() );
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
		inspectorPanel.addInspector( new JComponentInspector<JComponent>( buildPropertiesPanel(), "Properties",
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
		testSuiteRunner = getModelItem().run( new StringToObjectMap(), true );

		// new Thread( testSuiteRunner, getModelItem().getName() +
		// " TestSuiteRunner" ).start();
	}

	protected void beforeRun()
	{
		runAction.setEnabled( false );
		cancelAction.setEnabled( testSuiteRunner != null );
		testCaseList.setEnabled( false );
		progressBar.setForeground( Color.GREEN.darker() );
	}

	protected void afterRun( WsdlTestSuiteRunner testSuiteRunner )
	{
		runAction.setEnabled( true );
		cancelAction.setEnabled( false );
		testCaseList.setEnabled( true );

		progressBar.setString( String.valueOf( testSuiteRunner.getStatus() ) );
		progressBar.setForeground( testSuiteRunner.isFailed() ? Color.RED : Color.GREEN.darker() );
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
			testSuiteRunner.cancel( "Cancelled from UI" );
		}
	}

	private class SetupScriptGroovyEditorModel extends AbstractGroovyEditorModel
	{
		public SetupScriptGroovyEditorModel()
		{
			super( new String[] { "log", "runner", "context", "testSuite" }, WsdlTestSuiteDesktopPanel.this.getModelItem(), "Setup" );
		}

		public String getScript()
		{
			return  WsdlTestSuiteDesktopPanel.this.getModelItem().getSetupScript();
		}

		public void setScript( String text )
		{
			 WsdlTestSuiteDesktopPanel.this.getModelItem().setSetupScript( text );
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
						MockTestSuiteRunner mockRunner = new MockTestSuiteRunner(  WsdlTestSuiteDesktopPanel.this.getModelItem() );
						 WsdlTestSuiteDesktopPanel.this.getModelItem().runSetupScript( ( TestSuiteRunContext )mockRunner.getRunContext(), mockRunner );
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
			super( new String[] { "log", "runner", "context", "testSuite" },  WsdlTestSuiteDesktopPanel.this.getModelItem(), "TearDown" );
		}

		public String getScript()
		{
			return  WsdlTestSuiteDesktopPanel.this.getModelItem().getTearDownScript();
		}

		public void setScript( String text )
		{
			 WsdlTestSuiteDesktopPanel.this.getModelItem().setTearDownScript( text );
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
						MockTestSuiteRunner mockRunner = new MockTestSuiteRunner(  WsdlTestSuiteDesktopPanel.this.getModelItem() );
						 WsdlTestSuiteDesktopPanel.this.getModelItem().runTearDownScript( ( TestSuiteRunContext )mockRunner.getRunContext(), mockRunner );
					}
					catch( Exception e1 )
					{
						UISupport.showErrorMessage( e1 );
					}
				}
			};
		}
	}

	private class InternalTestSuiteRunListener implements TestSuiteRunListener
	{
		private TestRunLogTestRunListener runLogListener;
		private int finishCount;

		public void afterRun( TestSuiteRunner testRunner, TestSuiteRunContext runContext )
		{
			WsdlTestSuiteDesktopPanel.this.afterRun( ( WsdlTestSuiteRunner )testRunner );
		}

		public void afterTestCase( TestSuiteRunner testRunner, TestSuiteRunContext runContext,
				TestCaseRunner testCaseRunner )
		{
			progressBar.setValue( ++finishCount );

			if( getModelItem().getRunType() == TestSuiteRunType.SEQUENTIAL )
				testCaseRunner.getTestCase().removeTestRunListener( runLogListener );
		}

		public void beforeRun( TestSuiteRunner testRunner, TestSuiteRunContext runContext )
		{
			WsdlTestSuiteDesktopPanel.this.beforeRun();

			testCaseList.reset();

			progressBar.setMaximum( getModelItem().getTestCaseCount() );
			progressBar.setValue( 0 );
			progressBar.setString( "" );
			finishCount = 0;

			if( runLogListener == null )
				runLogListener = new TestRunLogTestRunListener( testRunLog, false );

			testRunLog.clear();

			if( getModelItem().getRunType() == TestSuiteRunType.PARALLEL )
				testRunLog.addText( "<log disabled during parallell execution>" );
		}

		public void beforeTestCase( TestSuiteRunner testRunner, TestSuiteRunContext runContext, TestCase testCase )
		{
			if( getModelItem().getRunType() == TestSuiteRunType.SEQUENTIAL )
			{
				progressBar.setString( "Running " + testCase.getName() );
				testCase.addTestRunListener( runLogListener );
			}
			else
			{
				progressBar.setString( "Starting " + testCase.getName() );
			}
		}
	}
}
