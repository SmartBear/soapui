/*
 *  soapUI Pro, copyright (C) 2007-2009 eviware software ab 
 */

package com.eviware.soapui.impl.wsdl.panels.teststeps;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import org.jdesktop.swingx.JXTable;

import com.eviware.soapui.impl.support.actions.ShowOnlineHelpAction;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.impl.wsdl.teststeps.JdbcRequestTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.JdbcAssertionRegistry;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.TestAssertionRegistry;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.model.testsuite.TestAssertion;
import com.eviware.soapui.support.ModelItemPropertyEditorModel;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.JComponentInspector;
import com.eviware.soapui.support.components.JInspectorPanel;
import com.eviware.soapui.support.components.JInspectorPanelFactory;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.ui.support.ModelItemDesktopPanel;

public class JdbcRequestTestStepDesktopPanel extends ModelItemDesktopPanel<JdbcRequestTestStep>
{
	private JPanel configPanel;
	private JXTable logTable;
//	private JList propertyList;
	private JButton runButton;
	private JButton addAssertionButton;
//	private JButton removeButton;
	private JLabel statusLabel;
   private JInspectorPanel inspectorPanel;
   private JdbcRequestTestStep jdbcRequestTestStep;
	private ModelItemPropertyEditorModel<JdbcRequestTestStep> resultEditorModel;
	private JComponentInspector<?> assertionInspector;
	private AssertionsPanel assertionsPanel;
   
   public ModelItemPropertyEditorModel<JdbcRequestTestStep> getResultEditorModel()
	{
		return resultEditorModel;
	}

	public JdbcRequestTestStepDesktopPanel( JdbcRequestTestStep modelItem )
	{
		super( modelItem );
		jdbcRequestTestStep = modelItem;
		
		buildUI();
		
		JdbcRequestTestStep jdbcRequest = modelItem;
//		runButton.setEnabled( jdbcRequest != null && modelItem.getPropertyCount() > 0 );
//		runButton.setEnabled( jdbcRequest.isRunnable());
		runButton.setEnabled(true);
//		removeButton.setEnabled( propertyList.getSelectedIndex() != -1 );
		
//		if( jdbcRequest != null )
//		{
//			comboBox.setSelectedItem( jdbcRequest.getType() );
//			configPanel.add( jdbcRequest.getComponent(), BorderLayout.CENTER );
//		}
//		else
//		{
//			comboBox.setSelectedItem( null );
//		}
		
	}

	protected JComponent buildToolbar()
	{
		JXToolBar toolbar = UISupport.createToolbar();

		toolbar.setBorder( BorderFactory.createEmptyBorder( 2, 2, 2, 2 ) );

		runButton = UISupport.createToolbarButton( new RunAction() );
//		runButton.setEnabled( transferList.getSelectedIndex() != -1 );
		toolbar.addFixed( runButton );

		addAssertionButton = UISupport.createToolbarButton( new AddAssertionAction(jdbcRequestTestStep) );
		addAssertionButton.setEnabled( true );
		toolbar.addFixed( addAssertionButton );

		toolbar.addGlue();
		toolbar.addFixed( UISupport
				.createToolbarButton( new ShowOnlineHelpAction( HelpUrls.TRANSFERSTEPEDITOR_HELP_URL ) ) );
		return toolbar;

	}
	protected AssertionsPanel buildAssertionsPanel()
	{
		return new JdbcAssertionsPanel( jdbcRequestTestStep )
		{
			protected void selectError( AssertionError error )
			{
//				ModelItemXmlEditor<?, ?> editor = ( ModelItemXmlEditor<?, ?> )getResultEditorModel();
//				editor.requestFocus();
			}
		};
	}
	private class JdbcAssertionsPanel extends AssertionsPanel {

		public JdbcAssertionsPanel(Assertable assertable)
		{
			super(assertable);
			addAssertionAction = new AddAssertionAction( assertable );
			assertionListPopup.add( addAssertionAction );		}
		
	}
	private void buildUI()
	{
      inspectorPanel = JInspectorPanelFactory.build( buildContent());
//		JComponentInspector<JComponent> insp = inspectorPanel.addInspector( new JComponentInspector<JComponent>( buildPreview(), "Data Log", 
//					"Read values", true ) );
		inspectorPanel.setDefaultDividerLocation( 0.7F  );
//		inspectorPanel.activate( insp );
		
		add( buildToolbar(), BorderLayout.NORTH );		
		add( inspectorPanel.getComponent(), BorderLayout.CENTER);
		assertionsPanel = buildAssertionsPanel();

		assertionInspector = new JComponentInspector<JComponent>( assertionsPanel, "Assertions ("
				+ getModelItem().getAssertionCount() + ")", "Assertions for this Test Request", true );

		inspectorPanel.addInspector( assertionInspector );

//		add( buildStatusBar(), BorderLayout.SOUTH );
		setPreferredSize( new Dimension( 600, 450 ));
	}

	private Component buildStatusBar()
	{
		JPanel statusBar = new JPanel( new BorderLayout() );
		statusBar.setBorder( BorderFactory.createEmptyBorder( 2, 2, 2, 2 ));
		statusLabel = new JLabel(" ");
		statusBar.add( statusLabel, BorderLayout.WEST );
		return statusBar;
	}

	private JComponent buildContent()
	{
		JSplitPane split = UISupport.createHorizontalSplit( buildConfigPanel(), buildResultEditor() );
		split.setDividerLocation( 180 );
		return split;
	}
	
	private JComponent buildConfigPanel()
	{
		configPanel = UISupport.addTitledBorder( new JPanel( new BorderLayout() ), "Configuration" );
		configPanel.add( jdbcRequestTestStep.getComponent(), BorderLayout.CENTER );
		return configPanel;

	}
	private Component buildResultEditor()
	{
		JPanel panel = new JPanel( new BorderLayout() );

		resultEditorModel = new ModelItemPropertyEditorModel<JdbcRequestTestStep>( getModelItem(), "xmlStringResult" );
		panel.add( UISupport.getEditorFactory().buildXmlEditor( resultEditorModel ), BorderLayout.CENTER );

		return panel;
	}

	public boolean dependsOn(ModelItem modelItem)
	{
		return modelItem == getModelItem() || modelItem == getModelItem().getTestCase() ||
				modelItem == getModelItem().getTestCase().getTestSuite() ||
				modelItem == getModelItem().getTestCase().getTestSuite().getProject();
	}

	public boolean onClose( boolean canCancel )
	{
		configPanel.removeAll();
		inspectorPanel.release();

		return release();
	}
	
	private final class RunAction extends AbstractAction
	{
		public RunAction()
		{
			putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/run.gif" ) );
			putValue( Action.SHORT_DESCRIPTION, "Runs selected JdbcRequest" );
		}

		public void actionPerformed(ActionEvent arg0)
		{
			try {
				jdbcRequestTestStep.runQuery();
			} catch (Exception e) {
				UISupport.showErrorMessage("There's been an error in executing query " + e.toString());
			}
		}
	}
	public class AddAssertionAction extends AbstractAction
	{
		private final Assertable assertable;

		public AddAssertionAction( Assertable assertable )
		{
			super( "Add Assertion" );
			this.assertable = assertable;

			putValue( Action.SHORT_DESCRIPTION, "Adds an assertion to this item" );
			putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/addAssertion.gif" ) );
		}

		public void actionPerformed( ActionEvent e )
		{
			String[] assertions = JdbcAssertionRegistry.getInstance().getAvailableAssertionNames( assertable );

			if( assertions == null || assertions.length == 0 )
			{
				UISupport.showErrorMessage( "No assertions available for this message" );
				return;
			}

			String selection = ( String )UISupport.prompt( "Select assertion to add", "Select Assertion", assertions );
			if( selection == null )
				return;

			if( !TestAssertionRegistry.getInstance().canAddMultipleAssertions( selection, assertable ) )
			{
				UISupport.showErrorMessage( "This assertion can only be added once" );
				return;
			}

			TestAssertion assertion = assertable.addAssertion( selection );
			if( assertion == null )
			{
				UISupport.showErrorMessage( "Failed to add assertion" );
				return;
			}

			if( assertion.isConfigurable() )
			{
				assertion.configure();
			}
		}
	}


	
	
}
