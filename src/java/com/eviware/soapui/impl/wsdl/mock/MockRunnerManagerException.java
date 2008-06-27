package com.eviware.soapui.impl.wsdl.mock;

@SuppressWarnings("serial")
public class MockRunnerManagerException extends Throwable
{
	public MockRunnerManagerException(String msg)
	{
		super(msg);
	}
	
	public MockRunnerManagerException(String msg, Throwable ex)
	{
		super(msg, ex);
	}
	
}
