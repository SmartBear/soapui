package com.eviware.soapui.monitor;

import com.eviware.soapui.model.mock.MockRunner;
import com.eviware.soapui.model.mock.MockService;

public interface MockEngine
{
	public boolean hasRunningMock( MockService mockService );

	public void startMockService( MockRunner runner ) throws Exception;

	public void stopMockService( MockRunner runner );

	public MockRunner[] getMockRunners();
}