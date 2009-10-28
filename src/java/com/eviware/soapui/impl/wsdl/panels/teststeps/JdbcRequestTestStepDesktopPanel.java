/*
 *  soapUI Pro, copyright (C) 2007-2009 eviware software ab 
 */

package com.eviware.soapui.impl.wsdl.panels.teststeps;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.Connection;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.Document;

import org.apache.log4j.Logger;
import org.jdesktop.swingx.JXTable;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.JdbcRequestTestStepConfig;
import com.eviware.soapui.impl.rest.RestRequestInterface;
import com.eviware.soapui.impl.support.actions.ShowOnlineHelpAction;
import com.eviware.soapui.impl.support.components.ModelItemXmlEditor;
import com.eviware.soapui.impl.support.components.ResponseMessageXmlEditor;
import com.eviware.soapui.impl.support.panels.AbstractHttpRequestDesktopPanel;
import com.eviware.soapui.impl.wsdl.panels.teststeps.support.PropertyHolderTable;
import com.eviware.soapui.impl.wsdl.submit.transports.http.HttpResponse;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestRunContext;
import com.eviware.soapui.impl.wsdl.teststeps.JdbcRequestTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.TestAssertionRegistry;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.Submit;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.iface.SubmitListener;
import com.eviware.soapui.model.iface.Request.SubmitException;
import com.eviware.soapui.model.iface.Submit.Status;
import com.eviware.soapui.model.support.TestRunListenerAdapter;
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.model.testsuite.TestAssertion;
import com.eviware.soapui.model.testsuite.TestCaseRunContext;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.TestStepResult;
import com.eviware.soapui.settings.UISettings;
import com.eviware.soapui.support.DocumentListenerAdapter;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.actions.ChangeSplitPaneOrientationAction;
import com.eviware.soapui.support.components.JComponentInspector;
import com.eviware.soapui.support.components.JEditorStatusBarWithProgress;
import com.eviware.soapui.support.components.JInspectorPanel;
import com.eviware.soapui.support.components.JInspectorPanelFactory;
import com.eviware.soapui.support.components.JUndoableTextField;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.support.components.SimpleForm;
import com.eviware.soapui.support.editor.xml.support.AbstractXmlDocument;
import com.eviware.soapui.support.jdbc.JdbcUtils;
import com.eviware.soapui.support.propertyexpansion.PropertyExpansionPopupListener;
import com.eviware.soapui.support.swing.JXEditAreaPopupMenu;
import com.eviware.soapui.support.xml.JXEditTextArea;
import com.eviware.soapui.ui.support.ModelItemDesktopPanel;

public class JdbcRequestTestStepDesktopPanel extends ModelItemDesktopPanel<JdbcRequestTestStep> implements SubmitListener
{
	private final static Logger log = Logger.getLogger(AbstractHttpRequestDesktopPanel.class);
 	protected JPanel configPanel;
	private JXTable logTable;
	private JButton addAssertionButton;
	protected JInspectorPanel inspectorPanel;
	protected JdbcRequestTestStep jdbcRequestTestStep;
	protected JdbcRequestTestStepConfig jdbcRequestTestStepConfig;
	protected JComponentInspector<?> assertionInspector;
	protected AssertionsPanel assertionsPanel;
	protected JComponent requestEditor;
	protected ModelItemXmlEditor<?, ?> responseEditor;
	protected JPanel panel;
	protected SimpleForm configForm;
	protected static final String DRIVER_FIELD = "Driver";
	protected static final String CONNSTR_FIELD = "Connection String";
	protected static final String PASS_FIELD = "Password";
	public static final String QUERY_FIELD = "SQL Query";
	protected static final String STOREDPROCEDURE_FIELD = "Stored Procedure";
	protected static final String DATA_CONNECTION_FIELD = "Connection";

	protected static final String QUERY_ELEMENT = "query";
	protected static final String STOREDPROCEDURE_ELEMENT = "stored-procedure";
	protected Connection connection;
	protected JXEditTextArea queryArea;
	protected JCheckBox isStoredProcedureCheckBox;
	protected JTextField driverTextField;
	protected JTextField connStrTextField;
	protected JButton testConnectionButton;
	protected JPasswordField passField;
	private InternalTestRunListener testRunListener = new InternalTestRunListener();
	private Submit submit;
	private SubmitAction submitAction;
	protected JButton submitButton;
	private JToggleButton tabsButton;
	private JTabbedPane requestTabs;
	private JPanel requestTabPanel;
	JdbcRequest jdbcRequest;
	private boolean responseHasFocus;
	private JSplitPane requestSplitPane;
	private JEditorStatusBarWithProgress statusBar;
	private JButton cancelButton;
	private JButton splitButton;
	protected JComponent propertiesTableComponent;

	public JdbcRequestTestStepDesktopPanel(JdbcRequestTestStep modelItem)
	{
		super(modelItem);
		jdbcRequestTestStep = modelItem;
		modelItem.getTestCase().addTestRunListener(testRunListener);
		jdbcRequest = new JdbcRequest(jdbcRequestTestStep);
		initConfig();
		initContent();
	}

	protected void initConfig()
	{
		jdbcRequestTestStepConfig = jdbcRequestTestStep.getJdbcRequestTestStepConfig();
		
	}
	private JComponent buildContent()
	{
		requestSplitPane = UISupport.createHorizontalSplit();
		requestSplitPane.setResizeWeight(0.5);
		requestSplitPane.setBorder(null);

		JComponent content;
		submitAction = new SubmitAction();
		submitButton = createActionButton(submitAction, true);
		submitButton.setEnabled(enableSubmit());

		cancelButton = createActionButton(new CancelAction(), false);
		tabsButton = new JToggleButton(new ChangeToTabsAction());
		tabsButton.setPreferredSize(UISupport.TOOLBAR_BUTTON_DIMENSION);
		splitButton = createActionButton(new ChangeSplitPaneOrientationAction(requestSplitPane), true);

		addAssertionButton = UISupport.createToolbarButton(new AddAssertionAction(jdbcRequestTestStep));
		addAssertionButton.setEnabled(true);

		requestTabs = new JTabbedPane();
		requestTabs.addChangeListener(new ChangeListener()
		{

			public void stateChanged(ChangeEvent e)
			{
				SwingUtilities.invokeLater(new Runnable()
				{

					public void run()
					{
						int ix = requestTabs.getSelectedIndex();
						if (ix == 0)
							requestEditor.requestFocus();
						else if (ix == 1 && responseEditor != null)
							responseEditor.requestFocus();
					}
				});
			}
		});

		addFocusListener(new FocusAdapter()
		{

			@Override
			public void focusGained(FocusEvent e)
			{
				if (requestTabs.getSelectedIndex() == 1 || responseHasFocus)
					responseEditor.requestFocusInWindow();
				else
					requestEditor.requestFocusInWindow();
			}
		});
		
		requestTabPanel = UISupport.createTabPanel(requestTabs, true);

		requestEditor = buildRequestConfigPanel();
		responseEditor = buildResponseEditor();
		if (jdbcRequest.getSettings().getBoolean(UISettings.START_WITH_REQUEST_TABS))
		{
			requestTabs.addTab("Request", requestEditor);
			if (responseEditor != null)
				requestTabs.addTab("Response", responseEditor);
			tabsButton.setSelected(true);
			splitButton.setEnabled(false);

			content =  requestTabPanel;
		}
		else
		{
			requestSplitPane.setTopComponent(requestEditor);
			requestSplitPane.setBottomComponent(responseEditor);
			requestSplitPane.setDividerLocation(0.5);
			content = requestSplitPane;
		}
		
		inspectorPanel = JInspectorPanelFactory.build(content);
		inspectorPanel.setDefaultDividerLocation(0.7F);
		add(buildToolbar(), BorderLayout.NORTH);
		add(inspectorPanel.getComponent(), BorderLayout.CENTER);
		assertionsPanel = buildAssertionsPanel();

		assertionInspector = new JComponentInspector<JComponent>(assertionsPanel, "Assertions ("
				+ getModelItem().getAssertionCount() + ")", "Assertions for this Test Request", true);

		inspectorPanel.addInspector(assertionInspector);
//		setPreferredSize(new Dimension(600, 450));
		return inspectorPanel.getComponent();
	}

	protected JComponent buildRequestConfigPanel()
	{
		configPanel = UISupport.addTitledBorder(new JPanel(new BorderLayout()), "Configuration");
		if (panel == null)
		{
			panel = new JPanel(new BorderLayout());
			configForm = new SimpleForm();
			createSimpleJdbcConfigForm();
			addStoreProcedureChangeListener();

			panel.add(configForm.getPanel());
		}
		configPanel.add(panel, BorderLayout.CENTER);
		
		propertiesTableComponent = buildProperties();
		JSplitPane split = UISupport.createVerticalSplit( propertiesTableComponent, configPanel);
		split.setDividerLocation(120);
		
		//TODO add scrolling but without messing with the dimension - ask Ole
		return split;

	}
	
	protected void initContent()
	{
		jdbcRequest.addSubmitListener(this);
		
		add(buildContent(), BorderLayout.CENTER);
		add(buildToolbar(), BorderLayout.NORTH);
		add(buildStatusLabel(), BorderLayout.SOUTH);

		setPreferredSize(new Dimension(600, 500));

		addFocusListener(new FocusAdapter()
		{

			@Override
			public void focusGained(FocusEvent e)
			{
				if (requestTabs.getSelectedIndex() == 1 || responseHasFocus)
					responseEditor.requestFocusInWindow();
				else
					requestEditor.requestFocusInWindow();
			}
		});
	}

	protected JComponent buildStatusLabel()
	{
		statusBar = new JEditorStatusBarWithProgress();
		statusBar.setBorder(BorderFactory.createEmptyBorder(1, 0, 0, 0));

		return statusBar;
	}

	protected JComponent buildProperties()
	{
		PropertyHolderTable holderTable = new PropertyHolderTable(getModelItem());

		JUndoableTextField textField = new JUndoableTextField(true);

		PropertyExpansionPopupListener.enable(textField, getModelItem());
		holderTable.getPropertiesTable().setDefaultEditor(String.class, new DefaultCellEditor(textField));

		return holderTable;
	}

	protected JComponent buildToolbar()
	{
		JXToolBar toolbar = UISupport.createToolbar();

		toolbar.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

		toolbar.addFixed(submitButton);
		toolbar.add(cancelButton);
		toolbar.addFixed(addAssertionButton);

		toolbar.add(Box.createHorizontalGlue());
		toolbar.add(tabsButton);
		toolbar.add(splitButton);
		toolbar.addFixed(UISupport.createToolbarButton(new ShowOnlineHelpAction(HelpUrls.TRANSFERSTEPEDITOR_HELP_URL)));
		return toolbar;

	}

	public JdbcRequestTestStep getJdbcRequestTestStep()
	{
		return jdbcRequestTestStep;
	}


	public void setQuery(String query)
	{
		if (configForm != null)
		{
			configForm.setComponentValue(QUERY_FIELD, query);
			jdbcRequestTestStep.setQuery(query);
		}
		else
		{
			// this.query = query;
			jdbcRequestTestStep.setQuery(query);
		}
	}

	protected AssertionsPanel buildAssertionsPanel()
	{
		return new JdbcAssertionsPanel(jdbcRequestTestStep)
		{
			protected void selectError(AssertionError error)
			{
				// ModelItemXmlEditor<?, ?> editor = ( ModelItemXmlEditor<?, ?>
				// )getResultEditorModel();
				// editor.requestFocus();
			}
		};
	}

	protected class JdbcAssertionsPanel extends AssertionsPanel
	{

		public JdbcAssertionsPanel(Assertable assertable)
		{
			super(assertable);
			addAssertionAction = new AddAssertionAction(assertable);
			assertionListPopup.add(addAssertionAction);
		}

	}

	protected void createSimpleJdbcConfigForm()
	{
		configForm.addSpace(5);

		configForm.setDefaultTextFieldColumns(50);

		driverTextField = configForm.appendTextField(DRIVER_FIELD, "JDBC Driver to use");
		driverTextField.setText(jdbcRequestTestStep.getDriver());
		PropertyExpansionPopupListener.enable(driverTextField, jdbcRequestTestStep);
		addDriverDocumentListener();

		connStrTextField = configForm.appendTextField(CONNSTR_FIELD, "JDBC Driver Connection String");
		connStrTextField.setText(jdbcRequestTestStep.getConnectionString());
		PropertyExpansionPopupListener.enable(connStrTextField, jdbcRequestTestStep);
		addConnStrDocumentListener();

		passField = configForm.appendPasswordField(PASS_FIELD, "Connection string Password");
		passField.setVisible(false);
		passField.setText(jdbcRequestTestStep.getPassword());
		addPasswordDocumentListener();

		testConnectionButton = configForm.appendButton("TestConnection", "Test selected database connection");
		testConnectionButton.setAction(new TestConnectionAction());
		testConnectionButton.setEnabled(enableTestConnection());
		submitButton.setEnabled(enableSubmit());
		
		queryArea = JXEditTextArea.createSqlEditor();
		JXEditAreaPopupMenu.add(queryArea);
		PropertyExpansionPopupListener.enable(queryArea, jdbcRequestTestStep);
		queryArea.setText(jdbcRequestTestStep.getQuery());
		JScrollPane scrollPane = new JScrollPane(queryArea);
		scrollPane.setPreferredSize(new Dimension(400, 150));
		configForm.append(QUERY_FIELD, scrollPane);
		queryArea.getDocument().addDocumentListener(new DocumentListenerAdapter()
		{

			@Override
			public void update(Document document)
			{
				jdbcRequestTestStep.setQuery(queryArea.getText());
				submitButton.setEnabled(enableSubmit());
			}
		});

		isStoredProcedureCheckBox = configForm.appendCheckBox(STOREDPROCEDURE_FIELD,
				"Select if this is a stored procedure", jdbcRequestTestStep.isStoredProcedure());
	}

	protected void addPasswordDocumentListener()
	{
		passField.getDocument().addDocumentListener(new DocumentListenerAdapter()
		{

			@Override
			public void update(Document document)
			{
				jdbcRequestTestStep.setPassword(configForm.getComponentValue(PASS_FIELD));
				testConnectionButton.setEnabled(enableTestConnection());
				submitButton.setEnabled(enableSubmit());
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
				jdbcRequestTestStep.setConnectionString(configForm.getComponentValue(CONNSTR_FIELD));
				testConnectionButton.setEnabled(enableTestConnection());
				submitButton.setEnabled(enableSubmit());
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
				jdbcRequestTestStep.setDriver(configForm.getComponentValue(DRIVER_FIELD));
				testConnectionButton.setEnabled(enableTestConnection());
				submitButton.setEnabled(enableSubmit());
			}
		});
	}

	protected void addStoreProcedureChangeListener()
	{
		isStoredProcedureCheckBox.addChangeListener(new ChangeListener()
		{
			public void stateChanged(ChangeEvent e)
			{
				jdbcRequestTestStep.setStoredProcedure(((JCheckBox) e.getSource()).isSelected());
			}
		});
	}

	protected boolean enableTestConnection()
	{
		if (StringUtils.isNullOrEmpty(jdbcRequestTestStep.getDriver())
				|| StringUtils.isNullOrEmpty(jdbcRequestTestStep.getConnectionString())
				|| (JdbcRequestTestStep.isNeededPassword(jdbcRequestTestStep.getConnectionString()) 
						&& StringUtils.isNullOrEmpty(jdbcRequestTestStep.getPassword())))
		{
			return false;
		} else {
			if (jdbcRequestTestStep.getConnectionString().contains(JdbcRequestTestStep.PASS_TEMPLATE))
			{
				return !StringUtils.isNullOrEmpty(jdbcRequestTestStep.getPassword());
			} else {
				return true;
			}
		}
	}
	protected boolean enableSubmit()
	{
		return enableTestConnection() && !StringUtils.isNullOrEmpty(jdbcRequestTestStep.getQuery());
	}
	
	protected ModelItemXmlEditor<?, ?> buildResponseEditor()
	{
		return new JdbcResponseMessageEditor();
	}

	public class JdbcResponseMessageEditor extends ResponseMessageXmlEditor<JdbcRequestTestStep, JdbcResponseDocument>
	{
		public JdbcResponseMessageEditor()
		{
			super(new JdbcResponseDocument(), jdbcRequestTestStep);
		}
	}

	public boolean dependsOn(ModelItem modelItem)
	{
		return modelItem == getModelItem() || modelItem == getModelItem().getTestCase()
				|| modelItem == getModelItem().getTestCase().getTestSuite()
				|| modelItem == getModelItem().getTestCase().getTestSuite().getProject();
	}

	public boolean onClose(boolean canCancel)
	{
		configPanel.removeAll();
		inspectorPanel.release();

		return release();
	}

	public class AddAssertionAction extends AbstractAction
	{
		private final Assertable assertable;

		public AddAssertionAction(Assertable assertable)
		{
			super("Add Assertion");
			this.assertable = assertable;

			putValue(Action.SHORT_DESCRIPTION, "Adds an assertion to this item");
			putValue(Action.SMALL_ICON, UISupport.createImageIcon("/addAssertion.gif"));
		}

		public void actionPerformed(ActionEvent e)
		{
			String[] assertions = TestAssertionRegistry.getInstance().getAvailableAssertionNames(assertable);

			if (assertions == null || assertions.length == 0)
			{
				UISupport.showErrorMessage("No assertions available for this message");
				return;
			}

			String selection = (String) UISupport.prompt("Select assertion to add", "Select Assertion", assertions);
			if (selection == null)
				return;

			if (!TestAssertionRegistry.getInstance().canAddMultipleAssertions(selection, assertable))
			{
				UISupport.showErrorMessage("This assertion can only be added once");
				return;
			}

			TestAssertion assertion = assertable.addAssertion(selection);
			if (assertion == null)
			{
				UISupport.showErrorMessage("Failed to add assertion");
				return;
			}

			if (assertion.isConfigurable())
			{
				assertion.configure();
			}
		}
	}

	public class JdbcResponseDocument extends AbstractXmlDocument implements PropertyChangeListener
	{
		public JdbcResponseDocument()
		{
			jdbcRequestTestStep.addPropertyChangeListener(JdbcRequestTestStep.RESPONSE_PROPERTY, this);
		}

		public void propertyChange(PropertyChangeEvent evt)
		{
			fireXmlChanged(evt.getOldValue() == null ? null : ((String) evt.getOldValue()), getXml());
		}

		public String getXml()
		{
			return jdbcRequestTestStep.getXmlStringResult();
		}

		public void setXml(String xml)
		{
			if (jdbcRequestTestStep != null)
				jdbcRequestTestStep.setXmlStringResult(xml);
		}

		public void release()
		{
			super.release();
			jdbcRequestTestStep.removePropertyChangeListener(RestRequestInterface.RESPONSE_PROPERTY, this);
		}
	}

	public class TestConnectionAction extends AbstractAction
	{
		public TestConnectionAction()
		{
			putValue(Action.SMALL_ICON, UISupport.createImageIcon("/run_testcase.gif"));
			putValue(Action.SHORT_DESCRIPTION, "Test the current Connection");

			setEnabled(false);
		}

		public void actionPerformed(ActionEvent arg0)
		{
			try
			{
				JdbcUtils.testConnection(getModelItem(), jdbcRequestTestStep.getDriver(), jdbcRequestTestStep.getConnectionString(), jdbcRequestTestStep.getPassword());
				UISupport.showInfoMessage("The Connection Successfully Tested");
			}
			catch (Exception e)
			{
				UISupport.showErrorMessage("Can't get the Connection for specified properties; " + e.toString());
			}
		}
	}
	private class InternalTestRunListener extends TestRunListenerAdapter
	{
		@Override
		public void afterStep(TestCaseRunner testRunner, TestCaseRunContext runContext, TestStepResult result)
		{
			// TODO Auto-generated method stub
			super.afterStep(testRunner, runContext, result);
		}
	}

	public class SubmitAction extends AbstractAction
	{
		public SubmitAction()
		{
			putValue(Action.SMALL_ICON, UISupport.createImageIcon("/submit_request.gif"));
			putValue(Action.SHORT_DESCRIPTION, "Submit request to specified endpoint URL");
			putValue(Action.ACCELERATOR_KEY, UISupport.getKeyStroke("alt ENTER"));
		}

		public void actionPerformed(ActionEvent e)
		{
			onSubmit();
		}
	}
	protected void onSubmit()
	{
		if (submit != null && submit.getStatus() == Submit.Status.RUNNING)
		{
			if (UISupport.confirm("Cancel current request?", "Submit Request"))
			{
				submit.cancel();
			}
			else
				return;
		}

		try
		{
			submit = doSubmit();
		}
		catch (SubmitException e1)
		{
			SoapUI.logError(e1);
		}
	}

	protected Submit doSubmit() throws SubmitException {
		return jdbcRequest.submit( new WsdlTestRunContext( getModelItem() ), true );
	}

	protected final class InputAreaFocusListener implements FocusListener
	{
		private final JComponent requestEditor;

		public InputAreaFocusListener(JComponent editor)
		{
			this.requestEditor = editor;
		}

		public void focusGained(FocusEvent e)
		{
			responseHasFocus = false;

//			statusBar.setTarget(sourceEditor.getInputArea());
			if (!splitButton.isEnabled())
			{
				requestTabs.setSelectedIndex(0);
				return;
			}

//			if (getModelItem().getSettings().getBoolean(UISettings.NO_RESIZE_REQUEST_EDITOR))
//				return;

//			// dont resize if split has been dragged
//			if (requestSplitPane.getUI() instanceof SoapUISplitPaneUI
//					&& ((SoapUISplitPaneUI) requestSplitPane.getUI()).hasBeenDragged())
//				return;
//
			int pos = requestSplitPane.getDividerLocation();
			if (pos >= 600)
				return;
			if (requestSplitPane.getMaximumDividerLocation() > 700)
				requestSplitPane.setDividerLocation(600);
			else
				requestSplitPane.setDividerLocation(0.8);
		}

		public void focusLost(FocusEvent e)
		{
		}
	}

	protected final class ResultAreaFocusListener implements FocusListener
	{
		private final ModelItemXmlEditor<?, ?> responseEditor;

		public ResultAreaFocusListener(ModelItemXmlEditor<?, ?> editor)
		{
			this.responseEditor = editor;
		}

		public void focusGained(FocusEvent e)
		{
			responseHasFocus = true;

//			statusBar.setTarget(sourceEditor.getInputArea());
			if (!splitButton.isEnabled())
			{
				requestTabs.setSelectedIndex(1);
				return;
			}
//
//			if (getModelItem().getSettings().getBoolean(UISettings.NO_RESIZE_REQUEST_EDITOR))
//				return;
//
//			// dont resize if split has been dragged or result is empty
//			if (requestSplitPane.getUI() instanceof SoapUISplitPaneUI
//					&& ((SoapUISplitPaneUI) requestSplitPane.getUI()).hasBeenDragged() || request.getResponse() == null)
//				return;
//
			int pos = requestSplitPane.getDividerLocation();
			int maximumDividerLocation = requestSplitPane.getMaximumDividerLocation();
			if (pos + 600 < maximumDividerLocation)
				return;

			if (maximumDividerLocation > 700)
				requestSplitPane.setDividerLocation(maximumDividerLocation - 600);
			else
				requestSplitPane.setDividerLocation(0.2);
		}

		public void focusLost(FocusEvent e)
		{
		}
	}

	private final class ChangeToTabsAction extends AbstractAction
	{
		public ChangeToTabsAction()
		{
			putValue(Action.SMALL_ICON, UISupport.createImageIcon("/toggle_tabs.gif"));
			putValue(Action.SHORT_DESCRIPTION, "Toggles to tab-based layout");
		}

		public void actionPerformed(ActionEvent e)
		{
			if (splitButton.isEnabled())
			{
				splitButton.setEnabled(false);
				removeContent(requestSplitPane);
				setContent(requestTabPanel);
				requestTabs.addTab("Request", requestEditor);

				if (responseEditor != null)
					requestTabs.addTab("Response", responseEditor);

				if (responseHasFocus)
				{
					requestTabs.setSelectedIndex(1);
					requestEditor.requestFocus();
				}
				requestTabs.repaint();
			}
			else
			{
				int selectedIndex = requestTabs.getSelectedIndex();

				splitButton.setEnabled(true);
				removeContent(requestTabPanel);
				setContent(requestSplitPane);
				requestSplitPane.setTopComponent(requestEditor);
				if (responseEditor != null)
					requestSplitPane.setBottomComponent(responseEditor);
				requestSplitPane.setDividerLocation(0.5);

				if (selectedIndex == 0 || responseEditor == null)
					requestEditor.requestFocus();
				else
					responseEditor.requestFocus();
				requestSplitPane.repaint();
			}

			revalidate();
		}
	}
	public void setContent(JComponent content)
	{
		inspectorPanel.setContentComponent( content );
	}

	public void removeContent(JComponent content)
	{
		inspectorPanel.setContentComponent( null );
	}
	private class CancelAction extends AbstractAction
	{
		public CancelAction()
		{
			super();
			putValue(Action.SMALL_ICON, UISupport.createImageIcon("/cancel_request.gif"));
			putValue(Action.SHORT_DESCRIPTION, "Aborts ongoing request");
			putValue(Action.ACCELERATOR_KEY, UISupport.getKeyStroke("alt X"));
		}

		public void actionPerformed(ActionEvent e)
		{
			onCancel();
		}
	}
	protected void onCancel()
	{
		if (submit == null)
			return;

		cancelButton.setEnabled(false);
		submit.cancel();
		setEnabled(true);
		submit = null;
	}
	public void setEnabled(boolean enabled)
	{
		if (responseEditor != null)
			responseEditor.setEditable(enabled);

		submitButton.setEnabled(enabled);
		addAssertionButton.setEnabled(enabled);
		driverTextField.setEnabled(enabled);
		connStrTextField.setEnabled(enabled);
		passField.setEnabled(enabled);
		queryArea.setEnabledAndEditable(enabled);
		isStoredProcedureCheckBox.setEnabled(enabled);
		propertiesTableComponent.setEnabled(enabled);

		statusBar.setIndeterminate(!enabled);
	}

	public void afterSubmit(Submit submit, SubmitContext context)
	{
		if (submit.getRequest() != jdbcRequest)
			return;

		Status status = submit.getStatus();
		HttpResponse response = (HttpResponse) submit.getResponse();
//		if (status == Status.FINISHED)
//		{
//			jdbcRequest.setResponse(response, context);
//		}
//
//		if (hasClosed)
//		{
//			jdbcRequest.removeSubmitListener(this);
//			return;
//		}

		cancelButton.setEnabled(false);
		setEnabled(true);

		String message = null;
		String infoMessage = null;
		String requestName = jdbcRequest.getName();

		if (status == Status.CANCELED)
		{
			message = "CANCELED";
			infoMessage = "[" + requestName + "] - CANCELED";
		}
		else
		{
			if (status == Status.ERROR || response == null)
			{
				message = "Error getting response; " + submit.getError();
				infoMessage = "Error getting response for [" + requestName + "]; " + submit.getError();
			}
			else
			{
				message = "response time: " + response.getTimeTaken() + "ms (" + response.getContentLength() + " bytes)";
				infoMessage = "Got response for [" + requestName + "] in " + response.getTimeTaken() + "ms ("
						+ response.getContentLength() + " bytes)";

				if (!splitButton.isEnabled())
					requestTabs.setSelectedIndex(1);

				responseEditor.requestFocus();
			}
		}

		logMessages(message, infoMessage);

		if (getModelItem().getSettings().getBoolean(UISettings.AUTO_VALIDATE_RESPONSE))
			responseEditor.getSourceEditor().validate();

		JdbcRequestTestStepDesktopPanel.this.submit = null;
	}

	protected void logMessages(String message, String infoMessage)
	{
		log.info(infoMessage);
		statusBar.setInfo(message);
	}
	public boolean beforeSubmit(Submit submit, SubmitContext context)
	{
		if (submit.getRequest() != jdbcRequest)
			return true;

		
		setEnabled(false);
		cancelButton.setEnabled(JdbcRequestTestStepDesktopPanel.this.submit != null);
		return true;
	}





}
