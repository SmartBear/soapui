package com.eviware.soapui.impl.wsdl.teststeps.assertions.jdbc;

import com.eviware.soapui.config.TestAssertionConfig;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.panels.teststeps.JdbcSubmit;
import com.eviware.soapui.impl.wsdl.teststeps.JdbcRequestTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlMessageAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.AbstractTestAssertionFactory;
import com.eviware.soapui.model.iface.MessageExchange;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.model.testsuite.AssertionError;
import com.eviware.soapui.model.testsuite.AssertionException;
import com.eviware.soapui.model.testsuite.RequestAssertion;
import com.eviware.soapui.model.testsuite.ResponseAssertion;

public class JdbcTimeoutAssertion extends WsdlMessageAssertion implements ResponseAssertion, RequestAssertion
{
	public static final String ID = "JDBC Timeout";
	public static final String LABEL = "JDBC Timeout";

	public JdbcTimeoutAssertion( TestAssertionConfig assertionConfig, Assertable assertable )
	{
		super( assertionConfig, assertable, false, false, false, true );
	}

	@Override
	protected String internalAssertResponse( MessageExchange messageExchange, SubmitContext context )
			throws AssertionException
	{

		if( context.getProperty( JdbcSubmit.JDBC_TIMEOUT ) != null )
		{
			Long timeout = Long.valueOf( context.getProperty( JdbcSubmit.JDBC_TIMEOUT ).toString() );
			throw new AssertionException( new AssertionError( "JDBC Request timeout error! Query not executed in "
					+ timeout + " ms." ) );
		}

		return "JDBC Timeout OK";
	}

	@Override
	protected String internalAssertRequest( MessageExchange messageExchange, SubmitContext context )
			throws AssertionException
	{
		return "JDBC Timeout OK";
	}

	public static class Factory extends AbstractTestAssertionFactory
	{
		public Factory()
		{
			super( JdbcTimeoutAssertion.ID, JdbcTimeoutAssertion.LABEL, JdbcTimeoutAssertion.class, WsdlRequest.class );
		}

		@Override
		public boolean canAssert( Assertable assertable )
		{
			return assertable instanceof JdbcRequestTestStep;
		}
	}
}
