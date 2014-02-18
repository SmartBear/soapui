package com.eviware.soapui.impl.wsdl.mock;

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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class WsdlMockOperationTest
{
	WsdlMockRequest restMockRequest;
	WsdlMockResponse mockResponse;
	WsdlMockOperation mockOperation;


	@Before
	public void setUp() throws Exception
	{
		restMockRequest = makeWsdlMockRequest();
		mockResponse = ModelItemFactory.makeWsdlMockResponse();
		mockOperation = mockResponse.getMockOperation();
		mockOperation.addMockResponse( mockResponse );
	}

	@Test
	public void testDispatchRequestReturnsHttpStatus() throws Exception
	{
		mockResponse.setResponseHttpStatus( HttpStatus.SC_BAD_REQUEST );

		WsdlMockResult mockResult = mockOperation.dispatchRequest( restMockRequest );

		assertThat( mockResult.getMockResponse().getResponseHttpStatus(), is( HttpStatus.SC_BAD_REQUEST ) );
	}

	@Test
	public void testDispatchRequestReturnsResponseContent() throws Exception
	{
		String responseContent = "mock response content";
		mockResponse.setResponseContent( responseContent );

		WsdlMockResult mockResult = mockOperation.dispatchRequest( restMockRequest );

		assertThat( mockResult.getMockResponse().getResponseContent(), is( responseContent ) );
	}

	private WsdlMockRequest makeWsdlMockRequest() throws Exception
	{
		HttpServletRequest request = mock( HttpServletRequest.class );
		Enumeration enumeration = mock( Enumeration.class );
		when( request.getHeaderNames() ).thenReturn( enumeration );

		HttpServletResponse response = mock( HttpServletResponse.class );
		ServletOutputStream os = mock( ServletOutputStream.class );
		when( response.getOutputStream() ).thenReturn( os );

		WsdlMockRunContext context = mock( WsdlMockRunContext.class );

		return new WsdlMockRequest( request, response, context );
	}
}
