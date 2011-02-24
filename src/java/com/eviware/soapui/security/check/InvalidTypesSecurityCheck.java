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

import org.apache.commons.collections.ArrayStack;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlException;

import com.eviware.soapui.config.CheckedParametersListConfig;
import com.eviware.soapui.config.SecurityCheckConfig;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestRequestStep;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.testsuite.TestCaseRunContext;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.security.SecurityTest;
import com.eviware.soapui.security.SecurityTestRunContext;
import com.eviware.soapui.security.SecurityTestRunner;
import com.eviware.soapui.security.SecurityTestRunnerImpl;
import com.eviware.soapui.security.boundary.SchemeTypeExtractor;
import com.eviware.soapui.security.ui.SecurityCheckConfigPanel;
import com.eviware.soapui.support.types.StringToObjectMap;
import com.eviware.x.form.XFormDialog;

public class InvalidTypesSecurityCheck extends AbstractSecurityCheck
{

	public final static String TYPE = "InvalidTypesSecurityCheck";

	private SchemeTypeExtractor extractor;

	XFormDialog dialog;

	private boolean hasNext;

	private InvalidTypes invalidTypes;

	private ArrayList<String> result = new ArrayList<String>();

	public InvalidTypesSecurityCheck( TestStep testStep, SecurityCheckConfig config, ModelItem parent, String icon )
	{
		super( testStep, config, parent, icon );

		config.setConfig( CheckedParametersListConfig.Factory.newInstance() );
		extractor = new SchemeTypeExtractor( testStep );

	}

	/**
	 * 
	 * Do we need this?
	 */
	// XXX: add rest and http.
	@Override
	public boolean acceptsTestStep( TestStep testStep )
	{
		return testStep instanceof WsdlTestRequestStep;
	}

	@Override
	public SecurityCheckConfigPanel getComponent()
	{
		// TODO Auto-generated method stub
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
	protected void execute(  SecurityTestRunner  securityTestRunner,TestStep testStep, SecurityTestRunContext context )
	{
		updateRequestContent();


		testStep.run( (TestCaseRunner)securityTestRunner, ( TestCaseRunContext )securityTestRunner.getRunContext() );
	}

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

		if( result.size() == 0 )
		{
			hasNext = true;
			String templateRequest = getTestStep().getProperty( "Request" ).getValue();
		}
	}

	@Override
	protected boolean hasNext()
	{
		return hasNext;
	}

	private class InvalidTypes
	{

		private int type;
		private ArrayStack stack;

		public InvalidTypes( int type )
		{
			this.type = type;
			generateInvalidTypes();

		}

		private void generateInvalidTypes()
		{

			stack = new ArrayStack();
			ArrayList<InvalidType> invalidTypes = new ArrayList<InvalidType>();

			stack.push( new InvalidType( SchemaType.BTC_BOOLEAN, new Boolean( true ) ) );
			stack.push( new InvalidType( SchemaType.BTC_INTEGER, new Integer( 10 ) ) );
			stack.push( new InvalidType( SchemaType.BTC_STRING, "simple" ) );

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
