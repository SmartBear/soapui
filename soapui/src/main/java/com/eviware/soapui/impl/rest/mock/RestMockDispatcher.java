package com.eviware.soapui.impl.rest.mock;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.mock.DispatchException;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockRunContext;
import com.eviware.soapui.model.mock.MockResult;
import com.eviware.soapui.model.support.AbstractMockDispatcher;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
	{
		RestMockRequest restMockRequest = new RestMockRequest( request, response, mockContext );

		Object result = null;
		try
		{
			result = mockService.runOnRequestScript( mockContext, restMockRequest );

			if( !( result instanceof MockResult ) )
			{
				result = getMockResult( restMockRequest );
			}

			mockService.runAfterRequestScript( mockContext, ( MockResult )result );
			return ( MockResult )result;
		}
		catch( Exception e )
		{
			SoapUI.logError( e, "got an exception while dispatching - returning a default 500 response" );
			return createServerErrorMockResult( restMockRequest );
		}
		finally
		{
			mockService.fireOnMockResult( result );
		}
	}

	private MockResult createServerErrorMockResult( RestMockRequest restMockRequest )
	{
		restMockRequest.getHttpResponse().setStatus( HttpStatus.SC_INTERNAL_SERVER_ERROR );
		return new RestMockResult( restMockRequest );
	}

	private MockResult getMockResult( RestMockRequest restMockRequest ) throws DispatchException
	{
		RestMockAction mockAction = ( RestMockAction )mockService.findBestMatchedOperation( restMockRequest.getPath(), restMockRequest.getMethod() );

		if( mockAction != null )
		{
			return mockAction.dispatchRequest( restMockRequest );
		}
		else
		{
			return createNotFoundResponse( restMockRequest );
		}

	}

	private RestMockResult createNotFoundResponse( RestMockRequest restMockRequest )
	{
		restMockRequest.getHttpResponse().setStatus( HttpStatus.SC_NOT_FOUND );
		return new RestMockResult( restMockRequest );
	}
}
