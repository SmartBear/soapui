/*
 *  SoapUI, copyright (C) 2004-2012 smartbear.com
 *
 *  SoapUI is free software; you can redistribute it and/or modify it under the
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  SoapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.wsdl.teststeps.assertions.jdbc;

import com.eviware.soapui.config.TestAssertionConfig;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.panels.assertions.AssertionCategoryMapping;
import com.eviware.soapui.impl.wsdl.panels.assertions.AssertionListEntry;
import com.eviware.soapui.impl.wsdl.panels.teststeps.JdbcSubmit;
import com.eviware.soapui.impl.wsdl.teststeps.JdbcRequestTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlMessageAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.AbstractTestAssertionFactory;
import com.eviware.soapui.model.TestPropertyHolder;
import com.eviware.soapui.model.iface.MessageExchange;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.model.testsuite.AssertionError;
import com.eviware.soapui.model.testsuite.AssertionException;
import com.eviware.soapui.model.testsuite.RequestAssertion;
import com.eviware.soapui.model.testsuite.ResponseAssertion;

public class JdbcStatusAssertion extends WsdlMessageAssertion implements ResponseAssertion, RequestAssertion
{
	public static final String ID = "JDBC Status";
	public static final String LABEL = "JDBC Status";
	public static final String DESCRIPTION = "Validates that the JDBC statement of the target TestStep executed successfully. Applicable to JDBC TestSteps only.";

	public JdbcStatusAssertion( TestAssertionConfig assertionConfig, Assertable assertable )
	{
		super( assertionConfig, assertable, false, false, false, true );
	}

	@Override
	protected String internalAssertResponse( MessageExchange messageExchange, SubmitContext context )
			throws AssertionException
	{

		Exception exception = ( Exception )context.getProperty( JdbcSubmit.JDBC_ERROR );
		if( exception != null )
		{
			throw new AssertionException( new AssertionError( exception.getMessage() ) );
		}

		return "JDBC Status OK";
	}

	@Override
	protected String internalAssertRequest( MessageExchange messageExchange, SubmitContext context )
			throws AssertionException
	{
		return "JDBC Status OK";
	}

	protected String internalAssertProperty( TestPropertyHolder source, String propertyName,
			MessageExchange messageExchange, SubmitContext context ) throws AssertionException
	{
		return null;
	}

	public static class Factory extends AbstractTestAssertionFactory
	{
		public Factory()
		{
			super( JdbcStatusAssertion.ID, JdbcStatusAssertion.LABEL, JdbcStatusAssertion.class, WsdlRequest.class );
		}

		@Override
		public String getCategory()
		{
			return AssertionCategoryMapping.JDBC_CATEGORY;
		}

		@Override
		public boolean canAssert( Assertable assertable )
		{
			return assertable instanceof JdbcRequestTestStep;
		}

		@Override
		public Class<? extends WsdlMessageAssertion> getAssertionClassType()
		{
			return JdbcStatusAssertion.class;
		}

		@Override
		public AssertionListEntry getAssertionListEntry()
		{
			return new AssertionListEntry( JdbcStatusAssertion.ID, JdbcStatusAssertion.LABEL,
					JdbcStatusAssertion.DESCRIPTION );
		}
	}
}
