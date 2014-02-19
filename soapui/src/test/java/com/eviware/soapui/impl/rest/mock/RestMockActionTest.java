package com.eviware.soapui.impl.rest.mock;

import com.eviware.soapui.impl.wsdl.mock.WsdlMockRunContext;
import com.eviware.soapui.support.types.StringToStringsMap;
import com.eviware.soapui.utils.ModelItemFactory;
import org.apache.commons.httpclient.HttpStatus;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Enumeration;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class RestMockActionTest
{
	RestMockRequest restMockRequest;
	RestMockAction mockAction;
	RestMockResponse mockResponse;


	@Before
	public void setUp() throws Exception
	{
		restMockRequest = makeRestMockRequest();
		mockAction = ModelItemFactory.makeRestMockAction( );
		mockResponse = mockAction.addNewMockResponse( "response 1" );
	}

	@Test
	public void testDispatchRequestReturnsHttpStatus() throws Exception
	{
		mockResponse.setResponseHttpStatus( HttpStatus.SC_BAD_REQUEST );

		RestMockResult mockResult = mockAction.dispatchRequest( restMockRequest );

		// HttpResponse is the response transferred over the wire.
		// So here we making sure the http status is actually set on the HttpResponse.
		verify( mockResult.getMockRequest().getHttpResponse() ).setStatus( HttpStatus.SC_BAD_REQUEST );

		assertThat( mockResult.getMockResponse().getResponseHttpStatus(), is( HttpStatus.SC_BAD_REQUEST ));
	}

	@Test
	public void testDispatchRequestReturnsResponseContent() throws Exception
	{
		String responseContent = "response content";
		mockResponse.setResponseContent( responseContent );

		RestMockResult mockResult = mockAction.dispatchRequest( restMockRequest );

		assertThat( mockResult.getMockResponse().getResponseContent(), is( responseContent ) );
	}

	@Test
	public void testDispatchRequestReturnsHttpHeader() throws Exception
	{
		StringToStringsMap responseHeaders = mockResponse.getResponseHeaders();
		String headerKey = "awesomekey";
		String headerValue = "awesomevalue";
		responseHeaders.add( headerKey, headerValue );
		mockResponse.setResponseHeaders( responseHeaders );

		RestMockResult mockResult = mockAction.dispatchRequest( restMockRequest );

		// HttpResponse is the response transferred over the wire.
		// So here we making sure the header is actually set on the HttpResponse.
		verify( mockResult.getMockRequest().getHttpResponse() ).addHeader( headerKey, headerValue );

		assertThat( mockResult.getResponseHeaders().get( headerKey, "" ), is( headerValue ) );
		assertThat( mockResult.getMockResponse().getResponseHeaders().get( headerKey, "" ), is( headerValue ) );
	}

	private RestMockRequest makeRestMockRequest() throws Exception
	{
		HttpServletRequest request = mock( HttpServletRequest.class );
		Enumeration enumeration = mock( Enumeration.class );
		when( request.getHeaderNames() ).thenReturn( enumeration );

		HttpServletResponse response = mock( HttpServletResponse.class );
		ServletOutputStream os = mock( ServletOutputStream.class );
		when( response.getOutputStream() ).thenReturn( os );

		WsdlMockRunContext context = mock( WsdlMockRunContext.class );

		return new RestMockRequest( request, response, context );
	}
}
