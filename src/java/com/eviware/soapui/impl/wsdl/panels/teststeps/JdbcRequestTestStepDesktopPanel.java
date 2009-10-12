/*
 *  soapUI Pro, copyright (C) 2007-2009 eviware software ab 
 */

package com.eviware.soapui.impl.wsdl.panels.teststeps;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.Connection;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.Document;

import org.jdesktop.swingx.JXTable;

import com.eviware.soapui.config.JdbcRequestTestStepConfig;
import com.eviware.soapui.impl.support.actions.ShowOnlineHelpAction;
import com.eviware.soapui.impl.support.components.ModelItemXmlEditor;
import com.eviware.soapui.impl.support.components.ResponseMessageXmlEditor;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.impl.wsdl.teststeps.JdbcRequestTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.TestAssertionRegistry;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.model.testsuite.TestAssertion;
import com.eviware.soapui.support.DocumentListenerAdapter;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.JComponentInspector;
import com.eviware.soapui.support.components.JInspectorPanel;
import com.eviware.soapui.support.components.JInspectorPanelFactory;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.support.components.SimpleForm;
import com.eviware.soapui.support.editor.xml.support.AbstractXmlDocument;
import com.eviware.soapui.support.jdbc.JdbcUtils;
import com.eviware.soapui.support.propertyexpansion.PropertyExpansionPopupListener;
import com.eviware.soapui.support.swing.JXEditAreaPopupMenu;
import com.eviware.soapui.support.xml.JXEditTextArea;
import com.eviware.soapui.ui.support.ModelItemDesktopPanel;

public class JdbcRequestTestStepDesktopPanel extends ModelItemDesktopPanel<JdbcRequestTestStep>
{
	protected JPanel configPanel;
	private JXTable logTable;
	// private JList propertyList;
	protected JButton runButton;
	private JButton addAssertionButton;
	// private JButton removeButton;
	private JLabel statusLabel;
	protected JInspectorPanel inspectorPanel;
	protected JdbcRequestTestStep jdbcRequestTestStep;
	protected JdbcRequestTestStepConfig jdbcRequestTestStepConfig;
	protected JComponentInspector<?> assertionInspector;
	protected AssertionsPanel assertionsPanel;
	protected ModelItemXmlEditor<?, ?> responseEditor;
	protected JPanel panel;
	protected SimpleForm configForm;
	protected boolean runnable;
	protected static final String DRIVER_FIELD = "Driver";
	protected static final String CONNSTR_FIELD = "Connection String";
	protected static final String PASS_FIELD = "Password";
	public static final String QUERY_FIELD = "SQL Query";
	protected static final String STOREDPROCEDURE_FIELD = "Stored Procedure";
	protected static final String DATA_CONNECTION_FIELD = "Connection";

	protected static final String QUERY_ELEMENT = "query";
	protected static final String STOREDPROCEDURE_ELEMENT = "stored-procedure";
	protected String driver;
	protected String connectionString;
	protected String password;
	protected String query;
	protected boolean storedProcedure;
	protected Connection connection;
	protected JXEditTextArea queryArea;
	protected JCheckBox isStoredProcedureCheckBox;
	protected JTextField driverTextField;
	protected JTextField connStrTextField;
	protected JButton testConnectionButton;
	protected JPasswordField passField ;
	
	public JdbcRequestTestStepDesktopPanel( JdbcRequestTestStep modelItem )
	{
		super( modelItem );
		jdbcRequestTestStep = modelItem;
		buildUI();

		runButton.setEnabled( true );
	}
	protected void init() {
		jdbcRequestTestStepConfig = jdbcRequestTestStep.getJdbcRequestTestStepConfig();
		this.driver = jdbcRequestTestStepConfig.getDriver();
		this.connectionString = jdbcRequestTestStepConfig.getConnectionString();
		this.query = jdbcRequestTestStepConfig.getQuery();
		this.password = jdbcRequestTestStepConfig.getPassword();

	}
	protected void buildUI()
	{
		init();
		JSplitPane split = UISupport.createHorizontalSplit( buildConfigPanel(), buildResponseEditor() );
		split.setDividerLocation( 180 );

		inspectorPanel = JInspectorPanelFactory.build( split );
		inspectorPanel.setDefaultDividerLocation( 0.7F );
		add( buildToolbar(), BorderLayout.NORTH );
		add( inspectorPanel.getComponent(), BorderLayout.CENTER );
		assertionsPanel = buildAssertionsPanel();

		assertionInspector = new JComponentInspector<JComponent>( assertionsPanel, "Assertions ("
				+ getModelItem().getAssertionCount() + ")", "Assertions for this Test Request", true );

		inspectorPanel.addInspector( assertionInspector );
		setPreferredSize( new Dimension( 600, 450 ) );
	}

	protected JComponent buildToolbar()
	{
		JXToolBar toolbar = UISupport.createToolbar();

		toolbar.setBorder( BorderFactory.createEmptyBorder( 2, 2, 2, 2 ) );

		runButton = UISupport.createToolbarButton( new RunAction() );
		// runButton.setEnabled( transferList.getSelectedIndex() != -1 );
		toolbar.addFixed( runButton );

		addAssertionButton = UISupport.createToolbarButton( new AddAssertionAction( jdbcRequestTestStep ) );
		addAssertionButton.setEnabled( true );
		toolbar.addFixed( addAssertionButton );

		toolbar.addGlue();
		toolbar.addFixed( UISupport
				.createToolbarButton( new ShowOnlineHelpAction( HelpUrls.TRANSFERSTEPEDITOR_HELP_URL ) ) );
		return toolbar;

	}

	public JdbcRequestTestStep getJdbcRequestTestStep()
	{
		return jdbcRequestTestStep;
	}

	public boolean isRunnable()
	{
		return runnable;
	}

	public void setQuery( String query )
	{
		if( configForm != null )
		{
			configForm.setComponentValue( QUERY_FIELD, query );
			jdbcRequestTestStepConfig.setQuery( query );
		}
		else
		{
			this.query = query;
			jdbcRequestTestStepConfig.setQuery( query );
		}
	}

	

	protected AssertionsPanel buildAssertionsPanel()
	{
		return new JdbcAssertionsPanel( jdbcRequestTestStep )
		{
			protected void selectError( AssertionError error )
			{
				// ModelItemXmlEditor<?, ?> editor = ( ModelItemXmlEditor<?, ?>
				// )getResultEditorModel();
				// editor.requestFocus();
			}
		};
	}

	protected class JdbcAssertionsPanel extends AssertionsPanel
	{

		public JdbcAssertionsPanel( Assertable assertable )
		{
			super( assertable );
			addAssertionAction = new AddAssertionAction( assertable );
			assertionListPopup.add( addAssertionAction );
		}

	}

	protected Component buildStatusBar()
	{
		JPanel statusBar = new JPanel( new BorderLayout() );
		statusBar.setBorder( BorderFactory.createEmptyBorder( 2, 2, 2, 2 ) );
		statusLabel = new JLabel( " " );
		statusBar.add( statusLabel, BorderLayout.WEST );
		return statusBar;
	}

	protected JComponent buildConfigPanel()
	{
		configPanel = UISupport.addTitledBorder( new JPanel( new BorderLayout() ), "Configuration" );
		if( panel == null )
		{
			panel = new JPanel( new BorderLayout() );
			configForm = new SimpleForm();
			createSimpleJdbcConfigForm();
			addStoreProcedureChangeListener();

			panel.add(configForm.getPanel());
		}
		configPanel.add( panel, BorderLayout.CENTER );
		return configPanel;

	}

	protected void createSimpleJdbcConfigForm()
	{
			configForm.addSpace(5);

			configForm.setDefaultTextFieldColumns(50);

			driverTextField = configForm.appendTextField(DRIVER_FIELD, "JDBC Driver to use");
			driverTextField.setText(jdbcRequestTestStepConfig.getDriver());
			PropertyExpansionPopupListener.enable( driverTextField,jdbcRequestTestStep);
			addDriverDocumentListener();

			connStrTextField = configForm.appendTextField(CONNSTR_FIELD, "JDBC Driver Connection String");
			connStrTextField.setText(jdbcRequestTestStepConfig.getConnectionString());
			PropertyExpansionPopupListener.enable( connStrTextField,jdbcRequestTestStep);
			addConnStrDocumentListener();

			 passField = configForm.appendPasswordField(PASS_FIELD,"Connection string Password");
			 passField.setText(jdbcRequestTestStepConfig.getPassword());
			 addPasswordDocumentListener();
			 
			testConnectionButton = configForm.appendButton("TestConnection", "Test selected database connection");
			testConnectionButton.setAction(new TestConnectionAction());
			if (enableTestConnection())
			{
				testConnectionButton.setEnabled(true);
			}
			else
			{
				testConnectionButton.setEnabled(false);
			}

			if (enableTestConnection() && !StringUtils.isNullOrEmpty(query))
			{
				runnable = true;
			}
			else
			{
				runnable = false;
			}
			queryArea = JXEditTextArea.createSqlEditor();
			JXEditAreaPopupMenu.add(queryArea);
			PropertyExpansionPopupListener.enable( queryArea, jdbcRequestTestStep );
			queryArea.setText(jdbcRequestTestStepConfig.getQuery());
			JScrollPane scrollPane = new JScrollPane(queryArea);
			scrollPane.setPreferredSize(new Dimension(400, 150));
			configForm.append(QUERY_FIELD, scrollPane);
			queryArea.getDocument().addDocumentListener(new DocumentListenerAdapter()
			{

				@Override
				public void update(Document document)
				{
					query = queryArea.getText();
					jdbcRequestTestStepConfig.setQuery(query);
				}
			});

			 isStoredProcedureCheckBox = configForm.appendCheckBox(STOREDPROCEDURE_FIELD,
					 "Select if this is a stored procedure", storedProcedure );
	}

	protected void addPasswordDocumentListener()
	{
		passField.getDocument().addDocumentListener( new DocumentListenerAdapter() {
				
			 @Override
			 public void update(Document document) {
			 password = configForm.getComponentValue(PASS_FIELD);
			 jdbcRequestTestStepConfig.setPassword(password);
			 if (StringUtils.isNullOrEmpty(driver) || StringUtils.isNullOrEmpty(connectionString) && 
					 StringUtils.isNullOrEmpty(password))
			 {
			 testConnectionButton.setEnabled(false);
			 } else {
			 testConnectionButton.setEnabled(true);
			 }
		 }
		 });
	}
	protected void addConnStrDocumentListener()
	{
		connStrTextField.getDocument().addDocumentListener(new DocumentListenerAdapter()
		{
			@Override
			public void update(Document document)
			{
				connectionString = configForm.getComponentValue(CONNSTR_FIELD);
				jdbcRequestTestStepConfig.setConnectionString(connectionString);
				if (enableTestConnection())
				{
					testConnectionButton.setEnabled(true);
				}
				else
				{
					testConnectionButton.setEnabled(false);
				}
				if (enableTestConnection()&& !StringUtils.isNullOrEmpty(query))
				{
					runnable = true;
				}
				else
				{
					runnable = false;
				}
			}
		});
	}
	protected void addDriverDocumentListener()
	{
		driverTextField.getDocument().addDocumentListener(new DocumentListenerAdapter()
		{

			@Override
			public void update(Document document)
			{
				driver = configForm.getComponentValue(DRIVER_FIELD);
				jdbcRequestTestStepConfig.setDriver(driver);
				if (enableTestConnection())
				{
					testConnectionButton.setEnabled(true);
				}
				else
				{
					testConnectionButton.setEnabled(false);
				}
				if (enableTestConnection() && !StringUtils.isNullOrEmpty(query))
				{
					runnable = true;
				}
				else
				{
					runnable = false;
				}
		}
		});
	}
	protected void addStoreProcedureChangeListener()
	{
		isStoredProcedureCheckBox.addChangeListener( new ChangeListener()
		 {
			 public void stateChanged( ChangeEvent e )
			 {
			 storedProcedure = ( (JCheckBox) e.getSource() ).isSelected();
			 jdbcRequestTestStepConfig.setStoredProcedure(storedProcedure);
			 }
		 } );
	}
	protected boolean enableTestConnection() {
		return !StringUtils.isNullOrEmpty(driver) && !StringUtils.isNullOrEmpty(connectionString) && 
		 !StringUtils.isNullOrEmpty(password) ;
	}

	protected ModelItemXmlEditor<?, ?> buildResponseEditor()
	{
		return new JdbcResponseMessageEditor();
	}

	public class JdbcResponseMessageEditor extends ResponseMessageXmlEditor<JdbcRequestTestStep, JdbcResponseDocument>
	{
		public JdbcResponseMessageEditor() 
		{
			super( new JdbcResponseDocument(), jdbcRequestTestStep );
		}
	}
	
	public boolean dependsOn( ModelItem modelItem )
	{
		return modelItem == getModelItem() || modelItem == getModelItem().getTestCase()
				|| modelItem == getModelItem().getTestCase().getTestSuite()
				|| modelItem == getModelItem().getTestCase().getTestSuite().getProject();
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

		public void actionPerformed( ActionEvent arg0 )
		{
			try
			{
				jdbcRequestTestStep.runQuery();
			}
			catch( Exception e )
			{
				UISupport.showErrorMessage( "There's been an error in executing query " + e.toString() );
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
			String[] assertions = TestAssertionRegistry.getInstance().getAvailableAssertionNames( assertable );

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

	public class JdbcResponseDocument extends AbstractXmlDocument implements PropertyChangeListener
	{
		public JdbcResponseDocument()
		{
			jdbcRequestTestStep.addPropertyChangeListener( JdbcRequestTestStep.RESPONSE_PROPERTY, this );
		}

		public void propertyChange( PropertyChangeEvent evt )
		{
			fireXmlChanged( evt.getOldValue() == null ? null : ( ( String )evt.getOldValue() ), getXml() );
		}

		public String getXml()
		{
			return jdbcRequestTestStep.getXmlStringResult();
		}

		public void setXml( String xml )
		{
			if( jdbcRequestTestStep != null )
				jdbcRequestTestStep.setXmlStringResult( xml );
		}

	}

	public class TestConnectionAction extends AbstractAction
	{
		public TestConnectionAction()
		{
			putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/run_testcase.gif" ) );
			putValue( Action.SHORT_DESCRIPTION, "Test the current Connection" );

			setEnabled( false );
		}

		public void actionPerformed( ActionEvent arg0 )
		{
			try
			{
				JdbcUtils.testConnection( getModelItem(), driver, connectionString, password );
				UISupport.showInfoMessage( "The Connection Successfully Tested" );
			}
			catch( Exception e )
			{
				UISupport.showErrorMessage( "Can't get the Connection for specified properties; " + e.toString() );
			}
		}
	}

}
