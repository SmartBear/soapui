/*
 * Copyright 2004-2014 SmartBear Software
 *
 * Licensed under the EUPL, Version 1.1 or - as soon as they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and limitations
 * under the Licence.
*/

package com.eviware.soapui.impl.wsdl.teststeps.assertions.jms;

import com.eviware.soapui.config.TestAssertionConfig;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.panels.assertions.AssertionCategoryMapping;
import com.eviware.soapui.impl.wsdl.panels.assertions.AssertionListEntry;
import com.eviware.soapui.impl.wsdl.submit.transports.jms.HermesJmsRequestTransport;
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

/**
 * Asserts JMS response within timeout
 * 
 * @author nebojsa.tasic
 */

public class JMSTimeoutAssertion extends WsdlMessageAssertion implements ResponseAssertion, RequestAssertion
{
	public static final String ID = "JMS Timeout";
	public static final String LABEL = "JMS Timeout";
	public static final String DESCRIPTION = "Validates that the JMS statement of the target TestStep did not take longer than the specified duration. Applicable to Request TestSteps with a JMS endpoint.";

	public JMSTimeoutAssertion( TestAssertionConfig assertionConfig, Assertable assertable )
	{
		super( assertionConfig, assertable, false, false, false, true );
	}

	@Override
	protected String internalAssertResponse( MessageExchange messageExchange, SubmitContext context )
			throws AssertionException
	{
		Boolean temp = ( Boolean )context.getProperty( HermesJmsRequestTransport.IS_JMS_MESSAGE_RECEIVED );
		Boolean messageReceived = temp != null ? temp : false;

		Long timeout = ( Long )context.getProperty( HermesJmsRequestTransport.JMS_RECEIVE_TIMEOUT );
		if( messageReceived != null && !messageReceived )
		{
			throw new AssertionException( new AssertionError( "JMS Message timeout error! Message is not received in "
					+ timeout + " ms." ) );
		}

		return "JMS Timeout OK";
	}

	@Override
	protected String internalAssertRequest( MessageExchange messageExchange, SubmitContext context )
			throws AssertionException
	{
		return "JMS Timeout OK";
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
			super( JMSTimeoutAssertion.ID, JMSTimeoutAssertion.LABEL, JMSTimeoutAssertion.class, WsdlRequest.class );
		}

		@Override
		public String getCategory()
		{
			return AssertionCategoryMapping.JMS_CATEGORY;
		}

		@Override
		public Class<? extends WsdlMessageAssertion> getAssertionClassType()
		{
			return JMSTimeoutAssertion.class;
		}

		@Override
		public AssertionListEntry getAssertionListEntry()
		{
			return new AssertionListEntry( JMSTimeoutAssertion.ID, JMSTimeoutAssertion.LABEL,
					JMSTimeoutAssertion.DESCRIPTION );
		}
	}
}
