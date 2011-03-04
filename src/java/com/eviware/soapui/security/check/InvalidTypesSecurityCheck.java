/*
 *  soapUI, copyright (C) 2004-2011 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */
package com.eviware.soapui.security.check;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;

import org.apache.commons.collections.ArrayStack;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;

import com.eviware.soapui.config.InvalidSecurityCheckConfig;
import com.eviware.soapui.config.SchemaTypeForSecurityCheckConfig;
import com.eviware.soapui.config.SecurityCheckConfig;
import com.eviware.soapui.config.StrategyTypeConfig;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlResponseMessageExchange;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestRequestStep;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.MessageExchange;
import com.eviware.soapui.model.security.SecurityCheckedParameter;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.TestProperty;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.security.SecurityTestRunContext;
import com.eviware.soapui.security.SecurityTestRunner;
import com.eviware.soapui.security.ui.InvalidTypesTable;
import com.eviware.soapui.security.ui.SecurityCheckConfigPanel;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.xml.XmlObjectTreeModel;
import com.eviware.soapui.support.xml.XmlObjectTreeModel.XmlTreeNode;

public class InvalidTypesSecurityCheck extends AbstractSecurityCheckWithProperties
{

	public final static String TYPE = "InvalidTypesSecurityCheck";

	private boolean hasNext = false;

	private InvalidTypesForSOAP invalidTypes;

	private List<String> result = new ArrayList<String>();

	private InvalidSecurityCheckConfig invalidTypeConfig;

	private Map<SecurityCheckedParameter, ArrayList<String>> parameterMutations = new HashMap<SecurityCheckedParameter, ArrayList<String>>();

	private boolean generated;

	public InvalidTypesSecurityCheck( TestStep testStep, SecurityCheckConfig config, ModelItem parent, String icon )
	{
		super( testStep, config, parent, icon );

		if( config.getConfig() == null || !( config.getConfig() instanceof InvalidSecurityCheckConfig ) )
			initInvalidTypesConfig();
		else
			invalidTypeConfig = ( InvalidSecurityCheckConfig )config.getConfig();

	}

	public InvalidSecurityCheckConfig getInvalidTypeConfig()
	{
		if( invalidTypeConfig == null || getConfig().getConfig() == null
				|| !( getConfig().getConfig() instanceof InvalidSecurityCheckConfig ) )
			initInvalidTypesConfig();
		return invalidTypeConfig;
	}

	private void initInvalidTypesConfig()
	{
		getConfig().setConfig( InvalidSecurityCheckConfig.Factory.newInstance() );
		invalidTypeConfig = ( InvalidSecurityCheckConfig )getConfig().getConfig();
		invalidTypes = new InvalidTypesForSOAP();

		// add all types..
		for( int key : invalidTypes.getDefaultTypeMap().keySet() )
		{
			SchemaTypeForSecurityCheckConfig newType = invalidTypeConfig.addNewTypesList();
			newType.setValue( invalidTypes.getDefaultTypeMap().get( key ) );
			newType.setType( key );
		}
	}

	@Override
	public JComponent getAdvancedSettingsPanel()
	{
		return new InvalidTypesTable( getInvalidTypeConfig() );
	}

	@Override
	public boolean acceptsTestStep( TestStep testStep )
	{
		return testStep instanceof WsdlTestRequestStep;
	}

	/*
	 * There is no advanced settings/special for this security check
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.eviware.soapui.security.check.AbstractSecurityCheck#getComponent()
	 */
	@Override
	public SecurityCheckConfigPanel getComponent()
	{
		return null;
	}

	@Override
	public String getType()
	{
		return TYPE;
	}

	@Override
	public boolean isConfigurable()
	{
		return true;
	}

	@Override
	protected void execute( SecurityTestRunner securityTestRunner, TestStep testStep, SecurityTestRunContext context )
	{
		updateRequestContent( testStep );

		testStep.run( ( TestCaseRunner )securityTestRunner, context );

		createMessageExchange( testStep );
	}

	private void createMessageExchange( TestStep testStep )
	{
		MessageExchange messageExchange = new WsdlResponseMessageExchange( ( ( WsdlTestRequestStep )testStep )
				.getTestRequest() );
		getSecurityCheckRequestResult().setMessageExchange( messageExchange );
	}

	/*
	 * Set new value for request
	 */
	private void updateRequestContent( TestStep testStep )
	{

			if( !generated )
				try
				{
					mutateParameters();
				}
				catch( XmlException e1 )
				{
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			if( getExecutionStrategy().getStrategy() == StrategyTypeConfig.ONE_BY_ONE )
			{
				/*
				 * Idea is to drain for each parameter mutations.
				 */
				for( SecurityCheckedParameter param : getParameterHolder().getParameterList() )
				{
					if( parameterMutations.containsKey( param ) )
						if( parameterMutations.get( param ).size() > 0 )
						{
							try
							{
								TestProperty property = getTestStep().getProperties().get( param.getName() );
								String value = property.getValue();
								XmlObjectTreeModel model = new XmlObjectTreeModel( ( ( WsdlTestRequestStep )getTestStep() )
										.getOperation().getInterface().getDefinitionContext().getSchemaTypeSystem(),
										XmlObject.Factory.parse( value ) );
								XmlTreeNode[] nodes = model.selectTreeNodes( param.getXPath() );
								for( XmlTreeNode node : nodes )
									node.setValue( 1, parameterMutations.get( param ).get( 0 ) );
								parameterMutations.get( param ).remove( 0 );

								testStep.getProperties().get( param.getName() ).setValue( model.getXmlObject().toString() );

							}
							catch( Exception e )
							{
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							break;
						}
				}
			}
			else
			{
				for( TestProperty property : testStep.getPropertyList() )
				{

					String value = property.getValue();
					XmlObjectTreeModel model;
					if ( value == null )
						continue;
					try
					{
						model = new XmlObjectTreeModel( ( ( WsdlTestRequestStep )getTestStep() )
								.getOperation().getInterface().getDefinitionContext().getSchemaTypeSystem(), XmlObject.Factory
								.parse( value ) );
						for( SecurityCheckedParameter param : getParameterHolder().getParameterList() )
						{
							if( param.getName().equals( property.getName() ) )
							{
								XmlTreeNode[] nodes = model.selectTreeNodes( param.getXPath() );
								if( parameterMutations.containsKey( param ) )
									if( parameterMutations.get( param ).size() > 0 )
									{
										for( XmlTreeNode node : nodes )
											node.setValue( 1, parameterMutations.get( param ).get( 0 ) );
										parameterMutations.get( param ).remove( 0 );
									}
							}
						}
						property.setValue( model.getXmlObject().toString() );
					}
					catch( XmlException e )
					{
						// TODO Auto-generated catch block
//						e.printStackTrace();
						continue;
					}
					catch( Exception e )
					{
						// TODO Auto-generated catch block
//						e.printStackTrace();
						continue;
					}

				}
			}

	}

	/**
	 * generate set of requests with all variations
	 * 
	 * @throws XmlException
	 */
	private void mutateParameters() throws XmlException
	{

		if( !generated )
		{
			generated = true;
			hasNext = true;

			// for each parameter
			for( SecurityCheckedParameter parameter : getParameterHolder().getParameterList() )
			{

				TestProperty property = getTestStep().getProperties().get( parameter.getName() );
				// ignore if there is no value.
				if( property.getValue() == null && property.getDefaultValue() == null )
					continue;
				// get value of that property
				String value = property.getValue() == null ? property.getDefaultValue() : property.getValue();

				try
				{

					XmlObjectTreeModel model = new XmlObjectTreeModel( ( ( WsdlTestRequestStep )getTestStep() )
							.getOperation().getInterface().getDefinitionContext().getSchemaTypeSystem(), XmlObject.Factory
							.parse( value ) );

					XmlTreeNode[] nodes = model.selectTreeNodes( parameter.getXPath() );

					// for each invalid type set all nodes
					List<SchemaTypeForSecurityCheckConfig> invalidTypes = invalidTypeConfig.getTypesListList();

					for( SchemaTypeForSecurityCheckConfig type : invalidTypes )
					{

						if( nodes.length > 0 )
						{
							if( nodes[0].getSchemaType().getBuiltinTypeCode() != type.getType() )
							{
								if( !parameterMutations.containsKey( parameter ) )
									parameterMutations.put( parameter, new ArrayList<String>() );
								parameterMutations.get( parameter ).add( type.getValue() );
							}
						}

					}
				}
				catch( Exception e1 )
				{
					UISupport.showErrorMessage( "Failed to select XPath for source property value [" + value + "]" );
				}

			}

		}

	}

	@Override
	protected boolean hasNext( TestStep testStep, SecurityTestRunContext context )
	{
		boolean oldHasNext = hasNext;
		if( parameterMutations == null )
			hasNext = true;
		else
		{
			boolean haveMore = false;
			for( SecurityCheckedParameter param : parameterMutations.keySet() )
			{
				if( parameterMutations.get( param ).size() > 0 )
				{
					haveMore = true;
					break;
				}
			}
			hasNext = hasNext ? haveMore : true;
		}
		if( oldHasNext && !hasNext )
			generated = false;
		return hasNext;
	}

	/**
	 * 
	 * This is support class that should keep track of all simple types. Also it
	 * should provide values for creating invalid requests.
	 * 
	 * @author robert
	 * 
	 */
	private class InvalidTypesForSOAP
	{

		private int type;
		private ArrayStack stack;

		private HashMap<Integer, String> typeMap = new HashMap<Integer, String>();

		public InvalidTypesForSOAP()
		{
			generateInvalidTypes();
		}

		public void setType( int type )
		{
			this.type = type;
		}

		/*
		 * see http://www.w3.org/TR/xmlschema-0/#CreatDt
		 */
		private void generateInvalidTypes()
		{

			stack = new ArrayStack();

			// strings
			typeMap.put( SchemaType.BTC_STRING, "SoapUI is\t the\r best\n" );
			// no cr/lf/tab
			typeMap.put( SchemaType.BTC_NORMALIZED_STRING, "SoapUI is the best" );
			// no cr/lf/tab
			typeMap.put( SchemaType.BTC_TOKEN, "SoapUI is the best" );
			// base64Binary
			typeMap.put( SchemaType.BTC_BASE_64_BINARY, "GpM7" );
			// hexBinary
			typeMap.put( SchemaType.BTC_HEX_BINARY, "0FB7" );
			// integer - no min or max
			typeMap.put( SchemaType.BTC_INTEGER, "-1267896799" );
			// positive integer
			typeMap.put( SchemaType.BTC_POSITIVE_INTEGER, "1267896799" );
			// negative integer
			typeMap.put( SchemaType.BTC_NEGATIVE_INTEGER, "-1" );
			// non negative integer
			typeMap.put( SchemaType.BTC_NON_NEGATIVE_INTEGER, "1" );
			// non positive integer
			typeMap.put( SchemaType.BTC_NON_POSITIVE_INTEGER, "0" );
			// long
			typeMap.put( SchemaType.BTC_LONG, "-882223334991111111" );
			// unsigned long
			typeMap.put( SchemaType.BTC_UNSIGNED_LONG, "882223334991111111" );
			// int
			typeMap.put( SchemaType.BTC_INT, "-2147483647" );
			// unsigned int
			typeMap.put( SchemaType.BTC_UNSIGNED_INT, "294967295" );
			// short
			typeMap.put( SchemaType.BTC_SHORT, "-32768" );
			// unsigned short
			typeMap.put( SchemaType.BTC_UNSIGNED_SHORT, "65535" );
			// byte
			typeMap.put( SchemaType.BTC_BYTE, "127" );
			// unsigned byte
			typeMap.put( SchemaType.BTC_UNSIGNED_BYTE, "255" );
			// decimal
			typeMap.put( SchemaType.BTC_DECIMAL, "-1.23" );
			// float
			typeMap.put( SchemaType.BTC_FLOAT, "-1E4f" );
			// double
			typeMap.put( SchemaType.BTC_DOUBLE, "12.45E+12" );
			// boolean
			typeMap.put( SchemaType.BTC_BOOLEAN, "true" );
			// duration
			typeMap.put( SchemaType.BTC_DURATION, "P1Y2M3DT10H30M12.3S" );
			// date time
			typeMap.put( SchemaType.BTC_DATE_TIME, "1999-05-31T13:20:00.000-05:00" );
			// date
			typeMap.put( SchemaType.BTC_DATE, "1999-05-31" );

			// need to add more...

			stack.addAll( typeMap.values() );
		}

		public boolean hasNext()
		{
			return !stack.isEmpty();
		}

		public HashMap<Integer, String> getDefaultTypeMap()
		{
			return typeMap;
		}

	}

	@Override
	public String getConfigDescription()
	{
		return "Configures invalid type security check";
	}

	@Override
	public String getConfigName()
	{
		return "Invalid Types Security Check";
	}

	@Override
	public String getHelpURL()
	{
		return "http://www.soapui.org";
	}

}
