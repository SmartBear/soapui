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

package com.eviware.soapui.impl.wsdl.teststeps.assertions.basic;

import com.eviware.soapui.config.TestAssertionConfig;
import com.eviware.soapui.impl.wsdl.panels.assertions.AssertionCategoryMapping;
import com.eviware.soapui.impl.wsdl.panels.assertions.AssertionListEntry;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlMessageAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.AbstractTestAssertionFactory;
import com.eviware.soapui.model.TestPropertyHolder;
import com.eviware.soapui.model.iface.MessageExchange;
import com.eviware.soapui.model.iface.Response;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.propertyexpansion.PropertyExpander;
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.model.testsuite.AssertionError;
import com.eviware.soapui.model.testsuite.AssertionException;
import com.eviware.soapui.model.testsuite.ResponseAssertion;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.xml.XmlObjectConfigurationBuilder;
import com.eviware.soapui.support.xml.XmlObjectConfigurationReader;
import org.apache.xmlbeans.XmlObject;

/**
 * Assertion for verifiying that responses occurred in the desired amount of
 * time.
 * 
 * @author Cory Lewis cory.lewis@genworth.com
 * 
 *         with help from
 * @author Ole.Matzura
 */

public class ResponseSLAAssertion extends WsdlMessageAssertion implements ResponseAssertion
{
	public static final String ID = "Response SLA Assertion";
	public static final String LABEL = "Response SLA";
	public static final String DESCRIPTION = "Validates that the last received response time was within the defined limit. Applicable to Script TestSteps and TestSteps that send requests and receive responses.";
	private String SLA;

	/**
	 * Constructor for our assertion.
	 * 
	 * @param assertionConfig
	 * @param modelItem
	 */
	public ResponseSLAAssertion( TestAssertionConfig assertionConfig, Assertable modelItem )
	{
		super( assertionConfig, modelItem, false, true, false, false );
		XmlObjectConfigurationReader reader = new XmlObjectConfigurationReader( getConfiguration() );
		SLA = reader.readString( "SLA", "200" );
	}

	protected String internalAssertRequest( MessageExchange messageExchange, SubmitContext context )
			throws AssertionException
	{
		return null;
	}

	protected String internalAssertResponse( MessageExchange messageExchange, SubmitContext context )
			throws AssertionException
	{
		Response response = messageExchange.getResponse();
		long timeTaken = response == null ? messageExchange.getTimeTaken() : response.getTimeTaken();

		// assert!
		if( timeTaken > Long.parseLong( PropertyExpander.expandProperties( context, SLA ) ) )
		{
			throw new AssertionException( new AssertionError( "Response did not meet SLA " + timeTaken + "/" + SLA ) );
		}

		return "Response meets SLA";
	}

	@Override
	protected String internalAssertProperty( TestPropertyHolder source, String propertyName,
			MessageExchange messageExchange, SubmitContext context ) throws AssertionException
	{
		return null;
	}

	/**
	 * @see com.eviware.soapui.impl.wsdl.teststeps.WsdlMessageAssertion#configure()
	 */
	public boolean configure()
	{
		String value = SLA;

		if( value == null || value.trim().length() == 0 )
		{
			value = "200";
		}

		value = UISupport.prompt( "Specify maximum response time (ms)", "Configure Response SLA Assertion", value );

		try
		{
			Long.parseLong( value );
			SLA = value;

		}
		catch( Exception e )
		{
			return false;
		}

		setConfiguration( createConfiguration() );
		return true;
	}

	public String getSLA()
	{
		return SLA;
	}

	public void setSLA( String sla )
	{
		SLA = sla;
		setConfiguration( createConfiguration() );
	}

	/**
	 * @return XmlObject, our config chunk
	 */
	protected XmlObject createConfiguration()
	{
		XmlObjectConfigurationBuilder builder = new XmlObjectConfigurationBuilder();
		return builder.add( "SLA", SLA ).finish();
	}

	public static class Factory extends AbstractTestAssertionFactory
	{
		public Factory()
		{
			super( ResponseSLAAssertion.ID, ResponseSLAAssertion.LABEL, ResponseSLAAssertion.class );
		}

		@Override
		public String getCategory()
		{
			return AssertionCategoryMapping.SLA_CATEGORY;
		}

		@Override
		public Class<? extends WsdlMessageAssertion> getAssertionClassType()
		{
			return ResponseSLAAssertion.class;
		}

		@Override
		public AssertionListEntry getAssertionListEntry()
		{
			return new AssertionListEntry( ResponseSLAAssertion.ID, ResponseSLAAssertion.LABEL,
					ResponseSLAAssertion.DESCRIPTION );
		}
	}
}
