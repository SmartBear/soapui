package com.eviware.soapui.impl.rest.panels.mock;

import com.eviware.soapui.impl.rest.mock.RestMockResponse;
import com.eviware.soapui.ui.support.AbstractMockResponseDesktopPanel;
import com.eviware.soapui.model.mock.MockResponse;

public class RestMockResponseDesktopPanel extends
		AbstractMockResponseDesktopPanel<RestMockResponse, MockResponse>
{

	public static final String REST_MOCK_RESPONSE_PANEL_NAME = "rest-mock-response-panel";

	public RestMockResponseDesktopPanel( MockResponse mockResponse )
	{
		super( ( RestMockResponse )mockResponse );
		setName( REST_MOCK_RESPONSE_PANEL_NAME );

		init( mockResponse );
	}
}
