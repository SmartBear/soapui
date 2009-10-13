package com.eviware.soapui.impl.wsdl.teststeps.assertions.jdbc;

import com.eviware.soapui.config.TestAssertionConfig;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlMessageAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.AbstractTestAssertionFactory;
import com.eviware.soapui.model.iface.MessageExchange;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.model.testsuite.AssertionException;
import com.eviware.soapui.model.testsuite.ResponseAssertion;

public class JdbcResponseAssertion extends WsdlMessageAssertion implements ResponseAssertion
{

	public static final String ID = "JDBC Assertion";
	public static final String LABEL = "JDBC Assertion";
	
	public JdbcResponseAssertion(TestAssertionConfig assertionConfig, Assertable modelItem)
	{
//		super(assertionConfig, modelItem, cloneable, configurable, multiple, requiresResponseContent);
		super(assertionConfig, modelItem, false, false, false, true);
	}

	@Override
	protected String internalAssertRequest(MessageExchange messageExchange, SubmitContext context)
			throws AssertionException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String internalAssertResponse(MessageExchange messageExchange, SubmitContext context)
			throws AssertionException
	{
		// TODO Auto-generated method stub
		String respContent = messageExchange.getResponseContent();
		if (respContent.contains("John"))
		{
			return "All well";
		} else {
		return null;
		}
	}
	public static class Factory extends AbstractTestAssertionFactory
	{
		public Factory()
		{
			super(JdbcResponseAssertion.ID, JdbcResponseAssertion.LABEL, JdbcResponseAssertion.class);
		}
	}

}
