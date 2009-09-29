/*
 * soapUI, copyright (C) 2004-2009 eviware.com
 *
 * soapUI is free software; you can redistribute it and/or modify it under the
 * terms of version 2.1 of the GNU Lesser General Public License as published by
 * the Free Software Foundation.
 *
 * soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.wsdl.teststeps.assertions;

import org.w3c.dom.Document;

import com.eviware.soapui.config.TestAssertionConfig;
import com.eviware.soapui.impl.wsdl.teststeps.JdbcRequestTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlMessageAssertion;
import com.eviware.soapui.model.iface.MessageExchange;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.model.testsuite.AssertionException;
import com.eviware.soapui.model.testsuite.RequestAssertion;
import com.eviware.soapui.model.testsuite.ResponseAssertion;
import com.eviware.x.form.XFormDialog;

/**
 * Assertion for verifying that WS-Addressing processing was ok
 * 
 * @author dragica.soldo
 */

public class JdbcXmlResponseAssertion extends WsdlMessageAssertion 
{
	public static final String ID = "Jdbc Xml Response Assertion";
	public static final String LABEL = "Jdbc Xml Response";
	private XFormDialog dialog;

	/**
	 * Constructor for our assertion.
	 * 
	 * @param assertionConfig
	 * @param modelItem
	 */
	public JdbcXmlResponseAssertion( TestAssertionConfig assertionConfig, Assertable modelItem )
	{
		super( assertionConfig, modelItem, false, false, false, true );

//		XmlObjectConfigurationReader reader = new XmlObjectConfigurationReader( getConfiguration() );
	}

	public static class Factory extends AbstractTestAssertionFactory
	{
		public Factory()
		{
			super( JdbcXmlResponseAssertion.ID, JdbcXmlResponseAssertion.LABEL, JdbcXmlResponseAssertion.class, JdbcRequestTestStep.class );
		}
	}

	@Override
	protected String internalAssertRequest( MessageExchange messageExchange, SubmitContext context )
			throws AssertionException
	{
		return null;
	}

	@Override
	protected String internalAssertResponse( MessageExchange messageExchange, SubmitContext context )
			throws AssertionException
	{
//		if( !messageExchange.hasResponse() )
//			return "Missing Response";
//		else
//			return assertContent( messageExchange.getResponseContentAsXml(), context, "Response" );
		return null;
	}

	public String assertContent( Document xmlDocResponse, SubmitContext context ) throws AssertionException
	{
		return "TestJdbcAssertion";
	}

}
