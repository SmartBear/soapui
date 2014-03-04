package com.eviware.soapui.impl.rest.mock;

import com.eviware.soapui.impl.support.BaseMockResult;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockOperation;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockRequest;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockResponse;
import com.eviware.soapui.model.mock.MockRequest;

public class RestMockResult extends BaseMockResult<RestMockRequest, RestMockAction, RestMockResponse>
{
	public RestMockResult( RestMockRequest request )
	{
		super( request );
	}
}
