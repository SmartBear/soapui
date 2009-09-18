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

package com.eviware.soapui.impl.wsdl.teststeps;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.text.Document;

import org.apache.log4j.Logger;

import com.eviware.soapui.config.JdbcRequestTestStepConfig;
import com.eviware.soapui.config.TestStepConfig;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.WsdlSubmit;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.propertyexpansion.DefaultPropertyExpansionContext;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContext;
import com.eviware.soapui.model.testsuite.TestCaseRunContext;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.TestStepResult;
import com.eviware.soapui.support.DocumentListenerAdapter;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.SimpleForm;
import com.eviware.soapui.support.swing.JXEditAreaPopupMenu;
import com.eviware.soapui.support.xml.JXEditTextArea;

/**
 * WsdlTestStep that executes a WsdlTestRequest
 * 
 * @author dragica.soldo
 */

public class JdbcRequestTestStep extends WsdlTestStepWithProperties
{
	private final static Logger log = Logger.getLogger(WsdlTestRequestStep.class);
	private JdbcRequestTestStepConfig jdbcRequestTestStepConfig;
	public final static String JDBCREQUEST = JdbcRequestTestStep.class.getName() + "@jdbcrequest";
	private WsdlSubmit<WsdlRequest> submit;
	private ImageIcon failedIcon;
	private ImageIcon okIcon;

	protected static final String DRIVER_FIELD = "Driver";
	protected static final String CONNSTR_FIELD = "Connection String";
	protected static final String PASS_FIELD = "Password";
	public static final String QUERY_FIELD = "SQL Query";
	protected static final String STOREDPROCEDURE_FIELD = "Stored Procedure";
	protected static final String DATA_CONNECTION_FIELD = "Connection";

	protected static final String QUERY_ELEMENT = "query";
	protected static final String STOREDPROCEDURE_ELEMENT = "stored-procedure";
	protected JPanel panel;
	protected String driver;
	public String getDriver()
	{
		return jdbcRequestTestStepConfig.getDriver();
	}

	public void setDriver(String driver)
	{
		jdbcRequestTestStepConfig.setDriver(driver);
	}

	public String getConnectionString()
	{
		return jdbcRequestTestStepConfig.getConnectionString();
	}

	public void setConnectionString(String connectionString)
	{
		jdbcRequestTestStepConfig.setConnectionString(connectionString);
	}

	public String getQuery()
	{
		return jdbcRequestTestStepConfig.getQuery();
	}

	public void setQuery(String query)
	{
		jdbcRequestTestStepConfig.setQuery(query);
	}
	protected String connectionString;
	protected String password;
	protected String query;
	protected SimpleForm form;
	protected Connection connection;
	protected JXEditTextArea queryArea;
	private JButton testConnectionButton;

	public JdbcRequestTestStepConfig getJdbcRequestTestStepConfig()
	{
		return jdbcRequestTestStepConfig;
	}

	public JdbcRequestTestStep(WsdlTestCase testCase, TestStepConfig config, boolean forLoadTest)
	{
		super(testCase, config, true, forLoadTest);

		if (!forLoadTest)
		{
			okIcon = UISupport.createImageIcon("/datasource.gif");
			failedIcon = UISupport.createImageIcon("/datasource_failed.gif");
			setIcon(okIcon);
		}
		if (getConfig().getConfig() != null)
		{
			jdbcRequestTestStepConfig = (JdbcRequestTestStepConfig) getConfig().getConfig().changeType(
					JdbcRequestTestStepConfig.type);

		}
		else
		{
			jdbcRequestTestStepConfig = (JdbcRequestTestStepConfig) getConfig().addNewConfig().changeType(
					JdbcRequestTestStepConfig.type);
		}
	}


	public JComponent getComponent()
	{
		if (panel == null)
		{
			panel = new JPanel(new BorderLayout());

			form = new SimpleForm();
			form.addSpace(5);

			form.setDefaultTextFieldColumns(50);

			JTextField textField = form.appendTextField(DRIVER_FIELD, "JDBC Driver to use");
			textField.setText(getDriver());
			// PropertyExpansionPopupListener.enable( textField,
			// getDataSourceStep() );
			textField.getDocument().addDocumentListener(new DocumentListenerAdapter()
			{

				@Override
				public void update(Document document)
				{
					driver = form.getComponentValue(DRIVER_FIELD);
					setDriver(driver);
					if (StringUtils.isNullOrEmpty(driver) || StringUtils.isNullOrEmpty(connectionString))
					{
						testConnectionButton.setEnabled(false);
					}
					else
					{
						testConnectionButton.setEnabled(true);
					}
				}
			});

			textField = form.appendTextField(CONNSTR_FIELD, "JDBC Driver Connection String");
			textField.setText(getConnectionString());
			// PropertyExpansionPopupListener.enable( textField,
			// getDataSourceStep() );
			textField.getDocument().addDocumentListener(new DocumentListenerAdapter()
			{

				@Override
				public void update(Document document)
				{
					connectionString = form.getComponentValue(CONNSTR_FIELD);
					setConnectionString(connectionString);
					if (StringUtils.isNullOrEmpty(driver) || StringUtils.isNullOrEmpty(connectionString))
					{
						testConnectionButton.setEnabled(false);
					}
					else
					{
						testConnectionButton.setEnabled(true);
					}
				}
			});

			// JPasswordField passField = form.appendPasswordField(PASS_FIELD,
			// "Connection string Password");
			// passField.setText(password);
			// passField.getDocument().addDocumentListener(new
			// DocumentListenerAdapter() {
			//		
			// @Override
			// public void update(Document document) {
			// password = form
			// .getComponentValue(PASS_FIELD);
			// saveConfig();
			// if (StringUtils.isNullOrEmpty(driver) ||
			// StringUtils.isNullOrEmpty(connectionString) &&
			// (DatabaseConnection.isNeededPassword(connectionString) &&
			// StringUtils.isNullOrEmpty(password)))
			// {
			// testConnectionButton.setEnabled(false);
			// } else {
			// testConnectionButton.setEnabled(true);
			// }
			// }
			// });
			testConnectionButton = form.appendButton("TestConnection", "Test selected database connection");
			testConnectionButton.setAction(new TestConnectionAction());
			if (StringUtils.isNullOrEmpty(driver) || StringUtils.isNullOrEmpty(connectionString))
			{
				testConnectionButton.setEnabled(false);
			}
			else
			{
				testConnectionButton.setEnabled(true);
			}

			queryArea = JXEditTextArea.createSqlEditor();
			JXEditAreaPopupMenu.add(queryArea);
			// PropertyExpansionPopupListener.enable( queryArea,
			// getDataSourceStep() );
			queryArea.setText(getQuery());
			JScrollPane scrollPane = new JScrollPane(queryArea);
			scrollPane.setPreferredSize(new Dimension(400, 150));
			form.append(QUERY_FIELD, scrollPane);
			queryArea.getDocument().addDocumentListener(new DocumentListenerAdapter()
			{

				@Override
				public void update(Document document)
				{
					query = queryArea.getText();
					setQuery(query);
				}
			});

			// isStoredProcedureCheckBox = form.appendCheckBox(
			// STOREDPROCEDURE_FIELD,
			// "Select if this is a stored procedure", storedProcedure );
			// isStoredProcedureCheckBox.addChangeListener(
			// new ChangeListener()
			// {
			// public void stateChanged( ChangeEvent e )
			// {
			// storedProcedure = ( (JCheckBox) e.getSource() ).isSelected();
			// saveConfig();
			// }
			// } );

			panel.add(form.getPanel());
		}

		return panel;
	}

	@Override
	public WsdlTestStep clone(WsdlTestCase targetTestCase, String name)
	{
		beforeSave();

		TestStepConfig config = (TestStepConfig) getConfig().copy();
		JdbcRequestTestStepConfig stepConfig = (JdbcRequestTestStepConfig) config.getConfig().changeType(
				JdbcRequestTestStepConfig.type);

		JdbcRequestTestStep result = (JdbcRequestTestStep) targetTestCase.addTestStep(config);

		return result;
	}

	@Override
	public void release()
	{
		super.release();
	}

	public TestStepResult run(TestCaseRunner runner, TestCaseRunContext runContext)
	{
		WsdlTestStepResult testStepResult = new WsdlTestStepResult(this);

		return testStepResult;
	}

	@Override
	public boolean cancel()
	{
		if (submit == null)
			return false;

		submit.cancel();

		return true;
	}

	public String getDefaultSourcePropertyName()
	{
		return "Response";
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
			 testDatabaseConnection(getModelItem(), driver, connectionString);
		}
	}

	public void testDatabaseConnection(ModelItem testingModelItem, String driver, String connectionString) {
		try {
			testConn(testingModelItem, driver, connectionString, password);
			UISupport.showInfoMessage("The Connection Successfully Tested");
		} catch (Exception e) {
			UISupport.showErrorMessage("Can't get the Connection for specified properties; " + e.toString());
		}
	}
	public static Connection testConn(ModelItem modelItem, String driver, String connectionString, String password)
			throws Exception, SQLException
	{
		PropertyExpansionContext context = new DefaultPropertyExpansionContext(modelItem);

//		String drvr = PropertyExpander.expandProperties(context, driver).trim();
//		String connStr = PropertyExpander.expandProperties(context, connectionString).trim();
		String drvr = driver;
		String connStr = connectionString;
		try
		{
			DriverManager.getDriver(connStr);
		}
		catch (SQLException e)
		{
			try
			{
				Class.forName(drvr).newInstance();
			}
			catch (Exception e1)
			{
				throw new Exception("Failed to init connection for drvr [" + drvr + "], connectionString [" + connectionString
						+ "]");
			}
		}
		return DriverManager.getConnection(connStr);

	}

}
