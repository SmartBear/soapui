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

package com.eviware.soapui.impl.wsdl.teststeps.assertions.soap;

import com.eviware.soapui.config.TestAssertionConfig;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.submit.WsdlMessageExchange;
import com.eviware.soapui.impl.wsdl.support.soap.SoapUtils;
import com.eviware.soapui.impl.wsdl.support.soap.SoapVersion;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlMessageAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.AbstractTestAssertionFactory;
import com.eviware.soapui.model.iface.MessageExchange;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.model.testsuite.AssertionError;
import com.eviware.soapui.model.testsuite.AssertionException;
import com.eviware.soapui.model.testsuite.ResponseAssertion;

/**
 * Assertion that checks that the associated WsdlTestRequests response is a SOAP
 * Fault
 * 
 * @author Ole.Matzura
 */

public class SoapFaultAssertion extends WsdlMessageAssertion implements ResponseAssertion
{
	public static final String ID = "Not SOAP Fault Assertion";
	public static final String LABEL = "SOAP Fault";

	public SoapFaultAssertion( TestAssertionConfig assertionConfig, Assertable assertable )
	{
		super( assertionConfig, assertable, false, false, false, true );
	}

	public String internalAssertResponse( MessageExchange messageExchange, SubmitContext context )
			throws AssertionException
	{
		String responseContent = messageExchange.getResponseContent();
		try
		{
			SoapVersion soapVersion = ( ( WsdlMessageExchange )messageExchange ).getOperation().getInterface()
					.getSoapVersion();

			if( !SoapUtils.isSoapFault( responseContent, soapVersion ) )
				throw new AssertionException( new AssertionError( "Response is not a SOAP Fault" ) );
		}
		catch( Exception e )
		{
			throw new AssertionException( new AssertionError( e.getMessage() ) );
		}

		return "Response is a SOAP Fault";
	}

	@Override
	protected String internalAssertRequest( MessageExchange messageExchange, SubmitContext context )
			throws AssertionException
	{
		return null;
	}

	public static class Factory extends AbstractTestAssertionFactory
	{
		public Factory()
		{
			super( SoapFaultAssertion.ID, SoapFaultAssertion.LABEL, SoapFaultAssertion.class, WsdlRequest.class );
		}
	}
}
