package com.eviware.soapui.impl.wsdl.mock;


public interface MockRunnerManager
{
	public WsdlMockService getMockService(int port, String path);

	public boolean isStarted();

	public void start() throws MockRunnerManagerException;
	public void stop();
}
