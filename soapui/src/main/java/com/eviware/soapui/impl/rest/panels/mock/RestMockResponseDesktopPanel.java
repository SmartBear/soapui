package com.eviware.soapui.impl.rest.panels.mock;

import com.eviware.soapui.impl.rest.mock.RestMockResponse;
import com.eviware.soapui.ui.support.AbstractMockResponseDesktopPanel;
import com.eviware.soapui.model.mock.MockResponse;

public class RestMockResponseDesktopPanel extends
		AbstractMockResponseDesktopPanel<RestMockResponse, MockResponse>
{
	public RestMockResponseDesktopPanel( MockResponse mockResponse )
	{
		super( ( RestMockResponse )mockResponse );

		init( mockResponse );
	}
}
