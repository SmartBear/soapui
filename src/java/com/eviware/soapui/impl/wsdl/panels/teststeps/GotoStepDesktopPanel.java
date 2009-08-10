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
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.Document;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.support.actions.ShowOnlineHelpAction;
import com.eviware.soapui.impl.support.http.HttpRequestTestStep;
import com.eviware.soapui.impl.wsdl.panels.support.TestRunComponentEnabler;
import com.eviware.soapui.impl.wsdl.panels.teststeps.support.GotoTestStepsComboBoxModel;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestRunContext;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlGotoTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlGotoTestStep.GotoCondition;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.support.TestRunListenerAdapter;
import com.eviware.soapui.model.testsuite.TestCaseRunContext;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.TestStepResult;
import com.eviware.soapui.support.DocumentListenerAdapter;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.JComponentInspector;
import com.eviware.soapui.support.components.JInspectorPanel;
import com.eviware.soapui.support.components.JInspectorPanelFactory;
import com.eviware.soapui.support.components.JUndoableTextArea;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.support.log.JLogList;
import com.eviware.soapui.support.xml.XmlUtils;
import com.eviware.soapui.ui.support.ModelItemDesktopPanel;

/**
 * DesktopPanel for WsdlGotoTestSteps
 * 
 * @author Ole.Matzura
 */

public class GotoStepDesktopPanel extends ModelItemDesktopPanel<WsdlGotoTestStep>
{
	private final WsdlGotoTestStep gotoStep;
	private DefaultListModel listModel;
	private JList conditionList;
	private JTextArea expressionArea;
	private JButton copyButton;
	private JButton deleteButton;
	private JButton declareButton;
	private GotoTestStepsComboBoxModel testStepsModel;
	private JComboBox testStepsCombo;
	private JButton testConditionButton;
	private TestRunComponentEnabler componentEnabler;
	private GotoCondition currentCondition;
	private JButton renameButton;
	private JButton runButton;
	private JButton addButton;
	private JLogList logList;
	private InternalTestRunListener testRunListener = new InternalTestRunListener();
	private JInspectorPanel inspectorPanel;

	public GotoStepDesktopPanel( WsdlGotoTestStep testStep )
	{
		super( testStep );
		this.gotoStep = testStep;
		componentEnabler = new TestRunComponentEnabler( testStep.getTestCase() );
		gotoStep.getTestCase().addTestRunListener( testRunListener );

		buildUI();
	}

	public TestRunComponentEnabler getComponentEnabler()
	{
		return componentEnabler;
	}

	public JList getConditionList()
	{
		return conditionList;
	}

	public JButton getCopyButton()
	{
		return copyButton;
	}

	public JButton getDeclareButton()
	{
		return declareButton;
	}

	public JButton getDeleteButton()
	{
		return deleteButton;
	}

	public JTextArea getExpressionArea()
	{
		return expressionArea;
	}

	public WsdlGotoTestStep getGotoStep()
	{
		return gotoStep;
	}

	public DefaultListModel getListModel()
	{
		return listModel;
	}

	public JButton getTestConditionButton()
	{
		return testConditionButton;
	}

	public JComboBox getTestStepsCombo()
	{
		return testStepsCombo;
	}

	public GotoTestStepsComboBoxModel getTestStepsModel()
	{
		return testStepsModel;
	}

	private void buildUI()
	{
		JSplitPane splitPane = UISupport.createHorizontalSplit();
		splitPane.setLeftComponent( buildConditionList() );

		splitPane.setRightComponent( buildExpressionArea() );
		splitPane.setResizeWeight( 0.1 );
		splitPane.setDividerLocation( 120 );

		inspectorPanel = JInspectorPanelFactory.build( splitPane );
		inspectorPanel.addInspector( new JComponentInspector<JComponent>( buildLog(), "Log",
				"A log of evaluated conditions", true ) );

		add( inspectorPanel.getComponent(), BorderLayout.CENTER );

		setBorder( BorderFactory.createEmptyBorder( 3, 3, 3, 3 ) );
		setPreferredSize( new Dimension( 550, 300 ) );

		if( listModel.getSize() > 0 )
			conditionList.setSelectedIndex( 0 );

		componentEnabler.add( conditionList );
		componentEnabler.add( expressionArea );
		componentEnabler.add( testStepsCombo );
		componentEnabler.add( testConditionButton );
		componentEnabler.add( copyButton );
		componentEnabler.add( declareButton );
		componentEnabler.add( deleteButton );
		componentEnabler.add( addButton );
		componentEnabler.add( runButton );
		componentEnabler.add( renameButton );
	}

	private JComponent buildLog()
	{
		logList = new JLogList( "ConditionLog" + getModelItem().getName() );
		return logList;
	}

	private JPanel buildExpressionArea()
	{
		expressionArea = new JUndoableTextArea();
		expressionArea.setEnabled( false );
		expressionArea.getDocument().addDocumentListener( new SourceAreaDocumentListener() );

		JPanel expressionPanel = new JPanel( new BorderLayout() );
		JScrollPane scrollPane = new JScrollPane( expressionArea );
		UISupport.addTitledBorder( scrollPane, "Condition XPath Expression" );

		expressionPanel.add( scrollPane, BorderLayout.CENTER );
		expressionPanel.add( buildConditionToolbar(), BorderLayout.NORTH );
		expressionPanel.add( buildTargetToolbar(), BorderLayout.SOUTH );
		return expressionPanel;
	}

	private JPanel buildConditionList()
	{
		listModel = new DefaultListModel();

		for( int c = 0; c < gotoStep.getConditionCount(); c++ )
		{
			listModel.addElement( gotoStep.getConditionAt( c ).getName() );
		}

		conditionList = new JList( listModel );
		conditionList.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
		conditionList.addListSelectionListener( new ConditionListSelectionListener() );

		JScrollPane listScrollPane = new JScrollPane( conditionList );
		UISupport.addTitledBorder( listScrollPane, "Conditions" );

		JPanel p = new JPanel( new BorderLayout() );
		p.add( buildConditionListToolbar(), BorderLayout.NORTH );
		p.add( listScrollPane, BorderLayout.CENTER );
		return p;
	}

	private Component buildConditionListToolbar()
	{
		JXToolBar toolbar = UISupport.createSmallToolbar();

		addButton = UISupport.createToolbarButton( new AddAction() );
		toolbar.addFixed( addButton );
		copyButton = UISupport.createToolbarButton( new CopyAction() );
		copyButton.setEnabled( false );
		toolbar.addFixed( copyButton );
		deleteButton = UISupport.createToolbarButton( new DeleteAction() );
		deleteButton.setEnabled( false );
		toolbar.addFixed( deleteButton );
		renameButton = UISupport.createToolbarButton( new RenameAction() );
		renameButton.setEnabled( false );
		toolbar.addFixed( renameButton );
		return toolbar;
	}

	private Component buildConditionToolbar()
	{
		JXToolBar toolbar = UISupport.createSmallToolbar();

		declareButton = UISupport.createToolbarButton( new DeclareNamespacesAction() );
		declareButton.setEnabled( false );
		toolbar.addFixed( declareButton );
		runButton = UISupport.createToolbarButton( new RunAction() );
		toolbar.addFixed( runButton );

		toolbar.addGlue();
		toolbar.addFixed( UISupport.createToolbarButton( new ShowOnlineHelpAction( HelpUrls.GOTOSTEPEDITOR_HELP_URL ) ) );
		return toolbar;
	}

	protected JXToolBar buildTargetToolbar()
	{
		JXToolBar builder = UISupport.createSmallToolbar();
		testStepsModel = new GotoTestStepsComboBoxModel( gotoStep.getTestCase(), null );
		testStepsCombo = new JComboBox( testStepsModel );
		testStepsCombo.setToolTipText( "The step the test case will go to if the current condition is true" );
		testStepsCombo.setEnabled( false );
		builder.addFixed( new JLabel( "<html><b>Target step:</b></html>" ) );
		builder.addRelatedGap();
		builder.addFixed( testStepsCombo );
		builder.addGlue();
		testConditionButton = new JButton( new TestConditionAction() );
		testConditionButton.setEnabled( false );
		builder.addFixed( testConditionButton );
		builder.setBorder( BorderFactory.createEmptyBorder( 3, 3, 3, 3 ) );
		return builder;
	}

	private final class SourceAreaDocumentListener extends DocumentListenerAdapter
	{
		@Override
		public void update( Document document )
		{
			int ix = conditionList.getSelectedIndex();
			if( ix != -1 )
			{
				gotoStep.getConditionAt( ix ).setExpression( expressionArea.getText() );
			}
		}
	}

	private final class ConditionListSelectionListener implements ListSelectionListener
	{
		public void valueChanged( ListSelectionEvent e )
		{
			int ix = conditionList.getSelectedIndex();
			if( ix == -1 )
			{
				expressionArea.setText( "" );
				testStepsModel.setCondition( null );
				currentCondition = null;
			}
			else
			{
				currentCondition = gotoStep.getConditionAt( ix );
				expressionArea.setText( currentCondition.getExpression() );
				testStepsModel.setCondition( currentCondition );
			}

			boolean b = ix != -1;
			enableEditComponents( b );
		}
	}

	private final class AddAction extends AbstractAction
	{
		public AddAction()
		{
			putValue( Action.SHORT_DESCRIPTION, "Adds a new Conditionr" );
			putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/add_property.gif" ) );
		}

		public void actionPerformed( ActionEvent e )
		{
			String name = UISupport.prompt( "Specify name for condition", "Add Condition", "Condition "
					+ ( gotoStep.getConditionCount() + 1 ) );
			if( name == null || name.trim().length() == 0 )
				return;

			gotoStep.addCondition( name );

			listModel.addElement( name );
			conditionList.setSelectedIndex( listModel.getSize() - 1 );
		}
	}

	private final class CopyAction extends AbstractAction
	{
		public CopyAction()
		{
			putValue( Action.SHORT_DESCRIPTION, "Copies the selected Condition" );
			putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/clone_request.gif" ) );
		}

		public void actionPerformed( ActionEvent e )
		{
			int ix = conditionList.getSelectedIndex();
			GotoCondition config = gotoStep.getConditionAt( ix );

			String name = UISupport.prompt( "Specify name for condition", "Copy Condition", config.getName() );
			if( name == null || name.trim().length() == 0 )
				return;

			GotoCondition condition = gotoStep.addCondition( name );
			condition.setExpression( config.getExpression() );
			condition.setTargetStep( config.getTargetStep() );
			condition.setType( config.getType() );

			listModel.addElement( name );
			conditionList.setSelectedIndex( listModel.getSize() - 1 );
		}
	}

	private final class DeleteAction extends AbstractAction
	{
		public DeleteAction()
		{
			putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/remove_property.gif" ) );
			putValue( Action.SHORT_DESCRIPTION, "Deletes the selected Condition" );
		}

		public void actionPerformed( ActionEvent e )
		{
			if( UISupport.confirm( "Delete selected condition", "Delete Condition" ) )
			{
				int ix = conditionList.getSelectedIndex();

				conditionList.setSelectedIndex( -1 );

				gotoStep.removeConditionAt( ix );
				listModel.remove( ix );

				if( listModel.getSize() > 0 )
				{
					conditionList.setSelectedIndex( ix > listModel.getSize() - 1 ? listModel.getSize() - 1 : ix );
				}
			}
		}
	}

	private final class RenameAction extends AbstractAction
	{
		public RenameAction()
		{
			putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/rename.gif" ) );
			putValue( Action.SHORT_DESCRIPTION, "Renames the selected Condition" );
		}

		public void actionPerformed( ActionEvent e )
		{
			int ix = conditionList.getSelectedIndex();
			GotoCondition config = gotoStep.getConditionAt( ix );

			String name = UISupport.prompt( "Specify name for condition", "Copy Condition", config.getName() );
			if( name == null || name.trim().length() == 0 )
				return;

			config.setName( name );
			listModel.setElementAt( name, ix );
		}
	}

	private final class DeclareNamespacesAction extends AbstractAction
	{
		public DeclareNamespacesAction()
		{
			putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/declareNs.gif" ) );
			putValue( Action.SHORT_DESCRIPTION, "Declare available response namespaces in condition expression" );
		}

		public void actionPerformed( ActionEvent e )
		{
			try
			{
				HttpRequestTestStep previousStep = ( HttpRequestTestStep )gotoStep.getTestCase().findPreviousStepOfType(
						gotoStep, HttpRequestTestStep.class );

				if( previousStep != null )
				{
					String xml = previousStep.getHttpRequest().getResponse().getContentAsString();
					if( StringUtils.hasContent( xml ) )
					{
						expressionArea.setText( XmlUtils.declareXPathNamespaces( xml ) + expressionArea.getText() );
					}
					else
					{
						UISupport.showErrorMessage( "Missing response in previous request step [" + previousStep.getName()
								+ "]" );
					}
				}
				else
				{
					UISupport.showErrorMessage( "Missing previous request step" );
				}
			}
			catch( Exception e1 )
			{
				SoapUI.logError( e1 );
			}
		}
	}

	private final class RunAction extends AbstractAction
	{
		public RunAction()
		{
			putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/run_all.gif" ) );
			putValue( Action.SHORT_DESCRIPTION, "Runs the current conditions against the previous response" );
		}

		public void actionPerformed( ActionEvent e )
		{
			if( listModel.getSize() == 0 )
			{
				UISupport.showErrorMessage( "Missing conditions!" );
				return;
			}

			HttpRequestTestStep previousStep = gotoStep.getTestCase().findPreviousStepOfType( gotoStep,
					HttpRequestTestStep.class );

			if( previousStep == null )
			{
				UISupport.showErrorMessage( "Missing previous request step" );
			}
			else
			{
				if( previousStep.getHttpRequest().getResponse() == null
						|| StringUtils.isNullOrEmpty( previousStep.getHttpRequest().getResponse().getContentAsXml() ) )
				{
					UISupport.showErrorMessage( "Missing response in previous message" );
					return;
				}

				WsdlTestRunContext context = new WsdlTestRunContext( gotoStep );
				GotoCondition target = gotoStep.runConditions( previousStep, context );
				if( target == null )
				{
					logList.addLine( "No condition true for current response in [" + previousStep.getName() + "]" );
				}
				else
				{
					logList.addLine( "Condition triggered for go to [" + target.getTargetStep() + "]" );
				}

				inspectorPanel.setCurrentInspector( "Log" );
			}
		}
	}

	private final class TestConditionAction extends AbstractAction
	{
		public TestConditionAction()
		{
			putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/run.gif" ) );
			putValue( Action.SHORT_DESCRIPTION,
					"Runs the current condition against the previous response and shows the result" );
		}

		public void actionPerformed( ActionEvent e )
		{
			HttpRequestTestStep previousStep = gotoStep.getTestCase().findPreviousStepOfType( gotoStep,
					HttpRequestTestStep.class );

			if( previousStep == null )
			{
				UISupport.showErrorMessage( "Missing previous request step" );
			}
			else
			{
				if( previousStep.getHttpRequest().getResponse() == null
						|| !StringUtils.hasContent( previousStep.getHttpRequest().getResponse().getContentAsXml() ) )
				{
					UISupport
							.showErrorMessage( "Missing response in previous request step [" + previousStep.getName() + "]" );
					return;
				}

				try
				{
					GotoCondition condition = gotoStep.getConditionAt( conditionList.getSelectedIndex() );
					WsdlTestRunContext context = new WsdlTestRunContext( gotoStep );
					boolean evaluate = condition.evaluate( previousStep, context );
					if( !evaluate )
					{
						UISupport.showInfoMessage( "Condition not true for current response in [" + previousStep.getName()
								+ "]" );
					}
					else
					{
						UISupport.showInfoMessage( "Condition true for current response in [" + previousStep.getName() + "]" );
					}
				}
				catch( Exception e1 )
				{
					UISupport.showErrorMessage( "Error checking condition: " + e1.getMessage() );
				}
			}
		}
	}

	public boolean onClose( boolean canCancel )
	{
		super.release();
		componentEnabler.release();
		gotoStep.getTestCase().removeTestRunListener( testRunListener );
		testStepsModel.release();
		inspectorPanel.release();

		return true;
	}

	public JComponent getComponent()
	{
		return this;
	}

	public boolean dependsOn( ModelItem modelItem )
	{
		return modelItem == gotoStep || modelItem == gotoStep.getTestCase()
				|| modelItem == gotoStep.getTestCase().getTestSuite()
				|| modelItem == gotoStep.getTestCase().getTestSuite().getProject();
	}

	public GotoCondition getCurrentCondition()
	{
		return currentCondition;
	}

	protected void enableEditComponents( boolean b )
	{
		expressionArea.setEnabled( b );
		testStepsCombo.setEnabled( b );
		copyButton.setEnabled( b );
		deleteButton.setEnabled( b );
		declareButton.setEnabled( b );
		testConditionButton.setEnabled( b );
		renameButton.setEnabled( b );
	}

	private class InternalTestRunListener extends TestRunListenerAdapter
	{
		@Override
		public void afterStep( TestCaseRunner testRunner, TestCaseRunContext runContext, TestStepResult result )
		{
			if( result.getTestStep() == gotoStep )
			{
				logList.addLine( new Date( result.getTimeStamp() ).toString() + ": " + result.getMessages()[0] );
				inspectorPanel.setCurrentInspector( "Log" );
			}
		}
	}
}
