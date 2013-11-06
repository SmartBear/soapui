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

package com.eviware.soapui.impl.wsdl.teststeps.assertions.soap;

import java.util.Vector;

import com.eviware.soapui.config.TestAssertionConfig;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.panels.assertions.AssertionCategoryMapping;
import com.eviware.soapui.impl.wsdl.panels.assertions.AssertionListEntry;
import com.eviware.soapui.impl.wsdl.submit.WsdlMessageExchange;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlMessageAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlMockResponseTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.AbstractTestAssertionFactory;
import com.eviware.soapui.model.TestPropertyHolder;
import com.eviware.soapui.model.iface.MessageExchange;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.model.testsuite.AssertionError;
import com.eviware.soapui.model.testsuite.AssertionException;
import com.eviware.soapui.model.testsuite.RequestAssertion;
import com.eviware.soapui.model.testsuite.ResponseAssertion;

/**
 * Assertion for verifiying that WS-Security processing was ok
 * 
 * @author Ole.Matzura
 */

public class WSSStatusAssertion extends WsdlMessageAssertion implements ResponseAssertion, RequestAssertion
{
	public static final String ID = "WSS Status Assertion";
	public static final String LABEL = "WS-Security Status";
	public static final String DESCRIPTION = "Validates that the last received message contained valid WS-Security headers. Applicable to SOAP TestSteps.";

	/**
	 * Constructor for our assertion.
	 * 
	 * @param assertionConfig
	 * @param modelItem
	 */
	public WSSStatusAssertion( TestAssertionConfig assertionConfig, Assertable modelItem )
	{
		super( assertionConfig, modelItem, false, false, false, true );
	}

	protected String internalAssertRequest( MessageExchange messageExchange, SubmitContext context )
			throws AssertionException
	{
		Vector<?> result = ( ( WsdlMessageExchange )messageExchange ).getRequestWssResult();

		if( result == null || result.isEmpty() )
			throw new AssertionException( new AssertionError( "Missing WS-Security results" ) );

		for( int c = 0; c < result.size(); c++ )
		{
			if( result.get( c ) instanceof Exception )
			{
				throw new AssertionException( new AssertionError( "WS-Security validation failed: " + result.get( c ) ) );
			}
		}

		return "WS-Security status OK";
	}

	protected String internalAssertResponse( MessageExchange messageExchange, SubmitContext context )
			throws AssertionException
	{
		Vector<?> result = ( ( WsdlMessageExchange )messageExchange ).getResponseWssResult();

		if( result == null || result.isEmpty() )
			throw new AssertionException( new AssertionError( "Missing WS-Security results" ) );

		for( int c = 0; c < result.size(); c++ )
		{
			if( result.get( c ) instanceof Exception )
			{
				throw new AssertionException( new AssertionError( "WS-Security validation failed: " + result.get( c ) ) );
			}
		}

		return "WS-Security status OK";
	}

	protected String internalAssertProperty( TestPropertyHolder source, String propertyName,
			MessageExchange messageExchange, SubmitContext context ) throws AssertionException
	{
		return null;
	}

	public static class Factory extends AbstractTestAssertionFactory
	{
		@SuppressWarnings( "unchecked" )
		public Factory()
		{
			super( WSSStatusAssertion.ID, WSSStatusAssertion.LABEL, WSSStatusAssertion.class, new Class[] {
					WsdlRequest.class, WsdlMockResponseTestStep.class } );
		}

		@Override
		public String getCategory()
		{
			return AssertionCategoryMapping.STATUS_CATEGORY;
		}

		@Override
		public Class<? extends WsdlMessageAssertion> getAssertionClassType()
		{
			return WSSStatusAssertion.class;
		}

		@Override
		public AssertionListEntry getAssertionListEntry()
		{
			return new AssertionListEntry( WSSStatusAssertion.ID, WSSStatusAssertion.LABEL, WSSStatusAssertion.DESCRIPTION );
		}
	}
}
