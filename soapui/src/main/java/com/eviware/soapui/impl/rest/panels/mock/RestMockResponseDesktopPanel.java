package com.eviware.soapui.impl.rest.panels.mock;

import com.eviware.soapui.impl.rest.mock.RestMockResponse;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockResponse;
import com.eviware.soapui.impl.wsdl.panels.mockoperation.AbstractWsdlMockResponseDesktopPanel;
import com.eviware.soapui.model.mock.MockResponse;

public class RestMockResponseDesktopPanel extends
		AbstractWsdlMockResponseDesktopPanel<RestMockResponse, MockResponse>
{
	public RestMockResponseDesktopPanel( MockResponse mockResponse )
	{
		super( ( RestMockResponse )mockResponse );

		init( mockResponse );
	}
}
