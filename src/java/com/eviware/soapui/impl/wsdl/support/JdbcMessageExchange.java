package com.eviware.soapui.impl.wsdl.support;

import com.eviware.soapui.impl.wsdl.teststeps.JdbcRequestTestStep;

public class JdbcMessageExchange extends AbstractNonHttpMessageExchange<JdbcRequestTestStep>{

	public JdbcMessageExchange(JdbcRequestTestStep modelItem) {
		super(modelItem);
	}

	@Override
	public String getRequestContent() {
		return null;
	}

	@Override
	public String getResponseContent() {
		return null;
	}

	@Override
	public long getTimeTaken() {
		return 0;
	}

	@Override
	public long getTimestamp() {
		return 0;
	}

	@Override
	public boolean hasRequest(boolean ignoreEmpty) {
		return false;
	}

	@Override
	public boolean hasResponse() {
		return false;
	}

	@Override
	public boolean isDiscarded() {
		return false;
	}
}
