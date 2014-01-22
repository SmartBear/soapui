package com.eviware.soapui.impl.rest.mock;

import com.eviware.soapui.impl.wsdl.mock.DispatchException;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockRequest;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockRunContext;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockService;
import com.eviware.soapui.model.mock.MockDispatcher;
import com.eviware.soapui.model.mock.MockResult;
import com.eviware.soapui.model.mock.MockRunListener;
import com.eviware.soapui.model.support.AbstractMockDispatcher;
import com.eviware.soapui.support.types.StringToStringMap;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

public class RestMockDispatcher extends AbstractMockDispatcher
{

	private RestMockService mockService;
	private WsdlMockRunContext mockContext;

	private final static Logger log = Logger.getLogger( RestMockDispatcher.class );

	public RestMockDispatcher( RestMockService mockService, WsdlMockRunContext mockContext )
	{
		this.mockService = mockService;
		this.mockContext = mockContext;
	}

	@Override
	public MockResult dispatchRequest( HttpServletRequest request, HttpServletResponse response )
			throws DispatchException
	{
		try
		{
			RestMockRequest restMockRequest  = new RestMockRequest( request, response, mockContext  );
			RestMockAction mockAction = mockService.getMockOperationAt( 0 ); //TODO in SOAP-1334
			return mockAction.dispatchRequest( restMockRequest );
		}
		catch(Exception e)
		{
			throw new DispatchException( e );
		}
	}
}
