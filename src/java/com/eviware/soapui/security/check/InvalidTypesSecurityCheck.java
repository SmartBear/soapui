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

import org.apache.commons.collections.ArrayStack;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;

import com.eviware.soapui.config.CheckedParametersListConfig;
import com.eviware.soapui.config.SecurityCheckConfig;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestRequestStep;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.security.SecurityCheckedParameter;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.TestProperty;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.security.SecurityTestRunContext;
import com.eviware.soapui.security.SecurityTestRunner;
import com.eviware.soapui.security.boundary.SchemeTypeExtractor;
import com.eviware.soapui.security.support.SecurityCheckedParameterImpl;
import com.eviware.soapui.security.ui.SecurityCheckConfigPanel;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.xml.XmlObjectTreeModel;
import com.eviware.soapui.support.xml.XmlObjectTreeModel.XmlTreeNode;

public class InvalidTypesSecurityCheck extends AbstractSecurityCheckWithProperties
{

	public final static String TYPE = "InvalidTypesSecurityCheck";

	private SchemeTypeExtractor extractor;

	private boolean hasNext = true;

	private InvalidTypesForSOAP invalidTypes;

	private List<String> result = new ArrayList<String>();

	private Map<String, ArrayList<SecurityCheckedParameter>> typeBuckets = new HashMap<String, ArrayList<SecurityCheckedParameter>>();

	public InvalidTypesSecurityCheck( TestStep testStep, SecurityCheckConfig config, ModelItem parent, String icon )
	{
		super( testStep, config, parent, icon );

		config.setConfig( CheckedParametersListConfig.Factory.newInstance() );
		extractor = new SchemeTypeExtractor( testStep );

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
		updateRequestContent();

		testStep.run( ( TestCaseRunner )securityTestRunner, context );
	}

	/*
	 * Set new value for request
	 */
	private void updateRequestContent()
	{

		try
		{
			generateRequests();
			if( result.size() > 0 )
			{
				getTestStep().getProperty( "Request" ).setValue( result.get( 0 ) );
				result.remove( 0 );
			}
			if( result.size() == 0 )
				hasNext = false;

		}
		catch( XmlException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * generate set of requests with all variations
	 * 
	 * @throws XmlException
	 */
	private void generateRequests() throws XmlException
	{

		createBuckets();

		if( result.size() == 0 )
		{
			hasNext = true;

			String templateRequest = getTestStep().getProperty( "Request" ).getValue();

			// XmlObjectTreeModel model = new XmlObjectTreeModel(
			// request.getOperation().getInterface().getDefinitionContext()
			// .getSchemaTypeSystem(), templateRequest );

			for( String shemaType : typeBuckets.keySet() )
			{
				for( SecurityCheckedParameter parameter : typeBuckets.get( shemaType ) )
				{

				}
			}

		}
	}

	/*
	 * Here create buckets. Each bucket will hold parameters with same type.
	 */
	private void createBuckets()
	{
		// clear buckets
		typeBuckets.clear();

		for( SecurityCheckedParameter param : getParameterHolder().getParameterList() )
		{
			TestProperty property = getTestStep().getProperties().get( param.getName() );
			// ignore if there is no value.
			if( property.getValue() == null && property.getDefaultValue() == null )
				continue;

			String value = property.getValue() == null ? property.getDefaultValue() : property.getValue();

			try
			{
				XmlObjectTreeModel model = new XmlObjectTreeModel( ( ( WsdlTestRequestStep )getTestStep() ).getOperation()
						.getInterface().getDefinitionContext().getSchemaTypeSystem(), XmlObject.Factory.parse( value ) );

				XmlTreeNode[] nodes = model.selectTreeNodes( param.getXPath() );
				if( nodes != null && nodes.length > 0 )
				{
					( ( SecurityCheckedParameterImpl )param ).setType( nodes[0].getSchemaType() );
					// if bucket do not exists add one.
					if( !typeBuckets.containsKey( param.getType() ) )
						typeBuckets.put( param.getType(), new ArrayList<SecurityCheckedParameter>() );
					// add parameter to bucket
					typeBuckets.get( param.getType() ).add( param );
				}
			}
			catch( Exception e1 )
			{
				UISupport.showErrorMessage( "Failed to select XPath for source property value [" + value + "]" );
			}

		}
	}

	@Override
	protected boolean hasNext()
	{
		return hasNext;
	}

	private class InvalidTypesForSOAP
	{

		private int type;
		private ArrayStack stack;
		private ArrayList<InvalidType> invalidTypesList;

		public InvalidTypesForSOAP( int type )
		{
			this.type = type;
			generateInvalidTypes();

		}

		/*
		 * see http://www.w3.org/TR/xmlschema-0/#CreatDt
		 */
		private void generateInvalidTypes()
		{

			stack = new ArrayStack();
			this.invalidTypesList = new ArrayList<InvalidType>();

			// strings
			invalidTypesList.add( new InvalidType<String>( SchemaType.BTC_STRING, "SoapUI is\t the\r best\n" ) );
			// no cr/lf/tab
			invalidTypesList.add( new InvalidType<String>( SchemaType.BTC_NORMALIZED_STRING, "SoapUI is the best" ) );
			// no cr/lf/tab
			invalidTypesList.add( new InvalidType<String>( SchemaType.BTC_TOKEN, "SoapUI is the best" ) );
			// base64Binary
			invalidTypesList.add( new InvalidType<String>( SchemaType.BTC_BASE_64_BINARY, "GpM7" ) );
			// hexBinary
			invalidTypesList.add( new InvalidType<String>( SchemaType.BTC_HEX_BINARY, "0FB7" ) );
			// integer - no min or max
			invalidTypesList.add( new InvalidType<Integer>( SchemaType.BTC_INTEGER, -1267896799 ) );
			// positive integer
			invalidTypesList.add( new InvalidType<Integer>( SchemaType.BTC_POSITIVE_INTEGER, 1267896799 ) );
			// negative integer
			invalidTypesList.add( new InvalidType<Integer>( SchemaType.BTC_NEGATIVE_INTEGER, -1 ) );
			// non negative integer
			invalidTypesList.add( new InvalidType<Integer>( SchemaType.BTC_NON_NEGATIVE_INTEGER, 1 ) );
			// non positive integer
			invalidTypesList.add( new InvalidType<Integer>( SchemaType.BTC_NON_POSITIVE_INTEGER, 0 ) );
			// long
			invalidTypesList.add( new InvalidType<Long>( SchemaType.BTC_LONG, -882223334991111111L ) );
			// unsigned long
			invalidTypesList.add( new InvalidType<Long>( SchemaType.BTC_UNSIGNED_LONG, 882223334991111111L ) );
			// int
			invalidTypesList.add( new InvalidType<Integer>( SchemaType.BTC_INT, -2147483647 ) );
			// unsigned int
			invalidTypesList.add( new InvalidType<Integer>( SchemaType.BTC_UNSIGNED_INT, 294967295 ) );
			// short
			invalidTypesList.add( new InvalidType<Short>( SchemaType.BTC_SHORT, ( short )-32768 ) );
			// unsigned short
			invalidTypesList.add( new InvalidType<Short>( SchemaType.BTC_UNSIGNED_SHORT, ( short )65535 ) );
			// byte
			invalidTypesList.add( new InvalidType<Byte>( SchemaType.BTC_BYTE, ( byte )127 ) );
			// unsigned byte
			invalidTypesList.add( new InvalidType<Byte>( SchemaType.BTC_UNSIGNED_BYTE, ( byte )255 ) );
			// decimal
			invalidTypesList.add( new InvalidType<Float>( SchemaType.BTC_DECIMAL, -1.23f ) );
			// float
			invalidTypesList.add( new InvalidType<Float>( SchemaType.BTC_FLOAT, -1E4f ) );
			// double
			invalidTypesList.add( new InvalidType<Double>( SchemaType.BTC_DOUBLE, 12.45E+12 ) );
			// boolean
			invalidTypesList.add( new InvalidType<Boolean>( SchemaType.BTC_BOOLEAN, new Boolean( true ) ) );
			// duration
			invalidTypesList.add( new InvalidType<String>( SchemaType.BTC_DURATION, "P1Y2M3DT10H30M12.3S" ) );
			// date time
			invalidTypesList.add( new InvalidType<String>( SchemaType.BTC_DATE_TIME, "1999-05-31T13:20:00.000-05:00" ) );
			// date
			invalidTypesList.add( new InvalidType<String>( SchemaType.BTC_DATE, "1999-05-31" ) );

			// need to add more...

			stack.addAll( invalidTypesList );
		}

		public Object getNext()
		{
			InvalidType result = ( InvalidType )stack.pop();
			if( result.getType() == type )
				return ( ( InvalidType )stack.pop() ).getValue();
			else
				return result.getValue();
		}

		public boolean hasNext()
		{
			return !stack.isEmpty();
		}

		class InvalidType<T>
		{

			public int type;
			public T value;

			public InvalidType( int type, T value )
			{
				this.type = type;
				this.value = value;
			}

			public int getType()
			{
				return type;
			}

			public T getValue()
			{
				return value;
			}

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
