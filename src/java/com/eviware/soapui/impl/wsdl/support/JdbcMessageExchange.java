package com.eviware.soapui.impl.wsdl.support;

import com.eviware.soapui.impl.wsdl.teststeps.JdbcRequestTestStep;

public class JdbcMessageExchange extends AbstractNonHttpMessageExchange<JdbcRequestTestStep>
{

	public JdbcMessageExchange( JdbcRequestTestStep modelItem )
	{
		super( modelItem );
	}

	public String getRequestContent()
	{
		return null;
	}

	public String getResponseContent()
	{
		return null;
	}

	public long getTimeTaken()
	{
		return 0;
	}

	public long getTimestamp()
	{
		return 0;
	}

	public boolean hasRequest( boolean ignoreEmpty )
	{
		return false;
	}

	public boolean hasResponse()
	{
		return false;
	}

	public boolean isDiscarded()
	{
		return false;
	}
}
