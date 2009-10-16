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

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.JdbcRequestTestStepConfig;
import com.eviware.soapui.config.PropertyConfig;
import com.eviware.soapui.config.TestAssertionConfig;
import com.eviware.soapui.config.TestStepConfig;
import com.eviware.soapui.impl.wsdl.MutableTestPropertyHolder;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.WsdlSubmit;
import com.eviware.soapui.impl.wsdl.panels.support.MockTestRunContext;
import com.eviware.soapui.impl.wsdl.panels.support.MockTestRunner;
import com.eviware.soapui.impl.wsdl.support.JdbcMessageExchange;
import com.eviware.soapui.impl.wsdl.support.XmlBeansPropertiesTestPropertyHolder;
import com.eviware.soapui.impl.wsdl.support.assertions.AssertableConfig;
import com.eviware.soapui.impl.wsdl.support.assertions.AssertionsSupport;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestRunContext;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.TestAssertionRegistry.AssertableType;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.propertyexpansion.PropertyExpander;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContext;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionUtils;
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.model.testsuite.AssertionsListener;
import com.eviware.soapui.model.testsuite.TestAssertion;
import com.eviware.soapui.model.testsuite.TestCaseRunContext;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.TestProperty;
import com.eviware.soapui.model.testsuite.TestPropertyListener;
import com.eviware.soapui.model.testsuite.TestStepResult;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.SimpleForm;
import com.eviware.soapui.support.xml.JXEditTextArea;
import com.eviware.soapui.support.xml.XmlUtils;

/**
 * WsdlTestStep that executes a WsdlTestRequest
 * 
 * @author dragica.soldo
 */

public class JdbcRequestTestStep extends WsdlTestStepWithProperties implements Assertable, MutableTestPropertyHolder
{
	private final static Logger log = Logger.getLogger(WsdlTestRequestStep.class);
	protected JdbcRequestTestStepConfig jdbcRequestTestStepConfig;
	public final static String JDBCREQUEST = JdbcRequestTestStep.class.getName() + "@jdbcrequest";
	public static final String STATUS_PROPERTY = WsdlTestRequest.class.getName() + "@status";
	public static final String RESPONSE_PROPERTY = WsdlTestRequest.class.getName() + "@response";
	private WsdlSubmit<WsdlRequest> submit;
	private ImageIcon failedIcon;
	private ImageIcon okIcon;
	private String xmlStringResult;
	private org.w3c.dom.Document xmlDocumentResult;
	protected static final String DRIVER_FIELD = "Driver";
	protected static final String CONNSTR_FIELD = "Connection String";
	protected static final String PASS_FIELD = "Password";
	public static final String PASS_TEMPLATE = "PASS_VALUE";
	public static final String QUERY_FIELD = "SQL Query";
	protected static final String STOREDPROCEDURE_FIELD = "Stored Procedure";
	protected static final String DATA_CONNECTION_FIELD = "Connection";

	protected static final String QUERY_ELEMENT = "query";
	protected static final String STOREDPROCEDURE_ELEMENT = "stored-procedure";
	protected JPanel panel;
	protected SimpleForm form;
	protected Connection connection;
	protected JXEditTextArea queryArea;
	private AssertionsSupport assertionsSupport;
	private PropertyChangeNotifier notifier;
   private XmlBeansPropertiesTestPropertyHolder propertyHolderSupport;

	public JdbcRequestTestStep(WsdlTestCase testCase, TestStepConfig config, boolean forLoadTest)
	{
		super(testCase, config, true, forLoadTest);

		if (!forLoadTest)
		{
			okIcon = UISupport.createImageIcon("/jdbcrequest.gif");
			failedIcon = UISupport.createImageIcon("/jdbcrequest_failed.gif");
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
		if( jdbcRequestTestStepConfig.getProperties() == null )
			jdbcRequestTestStepConfig.addNewProperties();
		
		propertyHolderSupport = new XmlBeansPropertiesTestPropertyHolder( this, jdbcRequestTestStepConfig.getProperties() );
		initAssertions();
	}

	public org.w3c.dom.Document getXmlDocumentResult()
	{
		return xmlDocumentResult;
	}

	public String getXmlStringResult()
	{
		return xmlStringResult;
	}

	public void setXmlStringResult(String xmlResult)
	{
		this.xmlStringResult = xmlResult;
	}

	private boolean runnable;

	public boolean isRunnable()
	{
		return runnable;
	}


	public JdbcRequestTestStepConfig getJdbcRequestTestStepConfig()
	{
		return jdbcRequestTestStepConfig;
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

	// running
	protected ResultSet resultSet;
	protected Statement statement;

	public void runQuery(String driver, String connStr)
	{
		MockTestRunner mockRunner = new MockTestRunner(getTestCase());
		MockTestRunContext mockContext = new MockTestRunContext(mockRunner, this);
		try
		{
			prepare(mockRunner, mockContext, driver, connStr);
			List<String> properties = new ArrayList<String>();
			load(mockRunner, mockContext, properties);
			createXmlResult();
		}
		catch (Exception e)
		{
			UISupport.showErrorMessage(e);
		}
	}

	protected void getDatabaseConnection(PropertyExpansionContext context, String drvr, String connStr) throws Exception, SQLException
	{
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
				throw new Exception("Failed to init connection for drvr [" + drvr + "], connectionString ["
						+ jdbcRequestTestStepConfig.getConnectionString() + "]");
			}
		}

		resultSet = null;
		connection = DriverManager.getConnection(connStr);
	}

	public void load(TestCaseRunner testRunner, TestCaseRunContext context, List<String> properties) throws Exception
	{
		if (jdbcRequestTestStepConfig.getStoredProcedure())
		{
			((CallableStatement) statement).execute();
		}
		else
		{
			List<PropertyConfig> props = jdbcRequestTestStepConfig.getProperties().getPropertyList();
			//TODO 
			/*
			 * Since ((PreparedStatement)statement).getParameterMetaData() is not implemented in specific drivers
			 * (except for mysql for now)
			 * number of parameters should match number of properties in exact order
			*/
			
//			int parameterCount = ((PreparedStatement)statement).getParameterMetaData().getParameterCount();
//			for (int i = 0; i < parameterCount; i++)
//			{
//				String paramName = ((PreparedStatement)statement).getParameterMetaData().getParameterTypeName(i);
				for (int j = 0; j < props.size(); j++)
				{
					PropertyConfig property = props.get(j);
					((PreparedStatement) statement).setString(j+1, property.getValue());
				}
//			}
			((PreparedStatement)statement).execute();

		}

		// getColumnNamesForCurrentResultSet();
		// resultSetCount = resultSet == null ? 0 : 1;
	}


	public void prepare(TestCaseRunner testRunner, TestCaseRunContext context, String drvr, String connStr) throws Exception {
		getDatabaseConnection(context, drvr, connStr);
		prepare(testRunner, context);
	}

	@Override
	public void prepare(TestCaseRunner testRunner, TestCaseRunContext context) throws Exception
	{
		if (jdbcRequestTestStepConfig.getStoredProcedure())
		{
			String sql = PropertyExpander.expandProperties(context, jdbcRequestTestStepConfig.getQuery());

			if (!sql.startsWith("{call ") && !sql.endsWith("}"))
				sql = "{call " + sql + "}";

			statement = connection.prepareCall(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		}
		else
		{
			String sql = PropertyExpander.expandProperties(context, jdbcRequestTestStepConfig.getQuery());
			statement = connection.prepareStatement(sql);
		}
		super.prepare(testRunner, context);
	}

	public void addResultSetResults() {
		
	}
	public void createXmlResult()
	{
		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			xmlDocumentResult = builder.newDocument();
			Element results = xmlDocumentResult.createElement("Results");
			xmlDocumentResult.appendChild(results);

			resultSet = statement.getResultSet();
			addResultSetXmlPart(results, statement.getResultSet());
			while (statement.getMoreResults())
			{
				addResultSetXmlPart(results, statement.getResultSet());
			}
			String oldRes = getXmlStringResult();
			xmlStringResult = XmlUtils.getDocumentAsString(xmlDocumentResult);
			setXmlStringResult(xmlStringResult);
			notifyPropertyChanged(RESPONSE_PROPERTY, oldRes, xmlStringResult);

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if (connection != null)
					connection.close();
				if (statement != null)
					statement.close();
				if (resultSet != null)
					resultSet.close();
			}
			catch (Exception e)
			{
			}
		}
	}

	private void addResultSetXmlPart(Element results, ResultSet rs) throws SQLException
	{
//		resultSet = statement.getResultSet();
		// connection to an ACCESS MDB
		ResultSetMetaData rsmd = rs.getMetaData();
		int colCount = rsmd.getColumnCount();
		while (rs.next())
		{
			Element row = xmlDocumentResult.createElement("Row");
			results.appendChild(row);
			for (int ii = 1; ii <= colCount; ii++)
			{
				String columnName = "";
				if (!StringUtils.isNullOrEmpty(rsmd.getTableName(ii)))
				{
					columnName += (rsmd.getTableName(ii)).toUpperCase() + ".";
				}
				columnName += (rsmd.getColumnName(ii)).toUpperCase();
				String value = rs.getString(ii);
				Element node = xmlDocumentResult.createElement(columnName);
				if (!StringUtils.isNullOrEmpty(value))
				{
					node.appendChild(xmlDocumentResult.createTextNode(value.toString()));
				}
				row.appendChild(node);
			}
		}
	}

	private void initAssertions()
	{
		assertionsSupport = new AssertionsSupport(this, new AssertableConfig()
		{

			public TestAssertionConfig addNewAssertion()
			{
				return getJdbcRequestTestStepConfig().addNewAssertion();
			}

			public List<TestAssertionConfig> getAssertionList()
			{
				return getJdbcRequestTestStepConfig().getAssertionList();
			}

			public void removeAssertion(int ix)
			{
				getJdbcRequestTestStepConfig().removeAssertion(ix);
			}

			public TestAssertionConfig insertAssertion(TestAssertionConfig source, int ix)
			{
				TestAssertionConfig conf = getJdbcRequestTestStepConfig().insertNewAssertion(ix);
				conf.set(source);
				return conf;
			}
		});
	}

	private class PropertyChangeNotifier
	{
		private AssertionStatus oldStatus;
		private ImageIcon oldIcon;

		public PropertyChangeNotifier()
		{
			oldStatus = getAssertionStatus();
			oldIcon = getIcon();
		}

		public void notifyChange()
		{
			AssertionStatus newStatus = getAssertionStatus();
			ImageIcon newIcon = getIcon();

			if (oldStatus != newStatus)
				notifyPropertyChanged(STATUS_PROPERTY, oldStatus, newStatus);

			if (oldIcon != newIcon)
				notifyPropertyChanged(ICON_PROPERTY, oldIcon, getIcon());

			oldStatus = newStatus;
			oldIcon = newIcon;
		}
	}

	public TestAssertion addAssertion(String assertionLabel)
	{
		PropertyChangeNotifier notifier = new PropertyChangeNotifier();

		try
		{
			WsdlMessageAssertion assertion = assertionsSupport.addWsdlAssertion(assertionLabel);
			if (assertion == null)
				return null;

			if (getXmlStringResult() != null)
			{
				assertion.assertResponse(new JdbcMessageExchange( this ), new WsdlTestRunContext( this ) );
				notifier.notifyChange();
			}

			return assertion;
		}
		catch (Exception e)
		{
			SoapUI.logError(e);
			return null;
		}
	}

	public void addAssertionsListener(AssertionsListener listener)
	{
		assertionsSupport.addAssertionsListener(listener);
	}

	public TestAssertion cloneAssertion(TestAssertion source, String name)
	{
		return assertionsSupport.cloneAssertion(source, name);
	}

	public String getAssertableContent()
	{
		return getXmlStringResult();
	}

	public AssertableType getAssertableType()
	{
		return AssertableType.RESPONSE;
	}

	public TestAssertion getAssertionAt(int c)
	{
		return assertionsSupport.getAssertionAt(c);
	}

	public TestAssertion getAssertionByName(String name)
	{
		return assertionsSupport.getAssertionByName(name);
	}

	public int getAssertionCount()
	{
		return assertionsSupport.getAssertionCount();
	}

	public List<TestAssertion> getAssertionList()
	{
		return null;
	}

	public AssertionStatus getAssertionStatus()
	{
		return AssertionStatus.UNKNOWN;
	}

	public Map<String, TestAssertion> getAssertions()
	{
		return assertionsSupport.getAssertions();
	}

	public String getDefaultAssertableContent()
	{
		return null;
	}

	public Interface getInterface()
	{
		return null;
	}

	public TestAssertion moveAssertion(int ix, int offset)
	{
		return assertionsSupport.moveAssertion(ix, offset);
	}

	public void removeAssertion(TestAssertion assertion)
	{
		assertionsSupport.removeAssertion((WsdlMessageAssertion) assertion);
	}

	public void removeAssertionsListener(AssertionsListener listener)
	{
		assertionsSupport.removeAssertionsListener(listener);
	}

	public void assertResponse(SubmitContext context)
	{
		try
		{
			if (notifier == null)
				notifier = new PropertyChangeNotifier();

			JdbcMessageExchange messageExchange = new JdbcMessageExchange( this );

			if (this != null)
			{
				// assert!
				for (WsdlMessageAssertion assertion : assertionsSupport.getAssertionList())
				{
					assertion.assertResponse(messageExchange, context);
				}
			}

			notifier.notifyChange();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

   public TestProperty addProperty( String name )
   {
      return propertyHolderSupport.addProperty( name );
   }

	public TestProperty removeProperty(String propertyName)
	{
		return propertyHolderSupport.removeProperty( propertyName );
	}

	public boolean renameProperty(String name, String newName)
	{
		return PropertyExpansionUtils.renameProperty( propertyHolderSupport.getProperty( name ), newName, getTestCase() ) != null;
	}
   public void addTestPropertyListener( TestPropertyListener listener )
   {
      propertyHolderSupport.addTestPropertyListener( listener );
   }

   public Map<String, TestProperty> getProperties()
   {
      return propertyHolderSupport.getProperties();
   }

   public TestProperty getProperty( String name )
   {
      return propertyHolderSupport.getProperty( name );
   }

   public TestProperty getPropertyAt( int index )
   {
      return propertyHolderSupport.getPropertyAt( index );
   }

   public int getPropertyCount()
   {
      return propertyHolderSupport.getPropertyCount();
   }

   public List<TestProperty> getPropertyList()
   {
   	return propertyHolderSupport.getPropertyList();
   }

   public String[] getPropertyNames()
   {
      return propertyHolderSupport.getPropertyNames();
   }

   public String getPropertyValue( String name )
   {
      return propertyHolderSupport.getPropertyValue( name );
   }
   public void removeTestPropertyListener( TestPropertyListener listener )
   {
      propertyHolderSupport.removeTestPropertyListener( listener );
   }

   public boolean hasProperty( String name )
   {
      return propertyHolderSupport.hasProperty( name );
   }

   public void setPropertyValue( String name, String value )
   {
      propertyHolderSupport.setPropertyValue( name, value );
   }
   
   public void setPropertyValue( String name, Object value )
   {
   	setPropertyValue( name, String.valueOf( value ));
   }

   public void moveProperty( String propertyName, int targetIndex )
   {
      propertyHolderSupport.moveProperty( propertyName, targetIndex );
   }

}
