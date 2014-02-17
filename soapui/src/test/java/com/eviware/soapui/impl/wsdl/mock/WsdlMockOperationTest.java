package com.eviware.soapui.impl.wsdl.mock;

import com.eviware.soapui.utils.ModelItemFactory;
import org.apache.commons.httpclient.HttpStatus;
import org.junit.Before;
import org.junit.Test;

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

	@Before
	public void setUp() throws Exception
	{
		restMockRequest = makeRestMockRequest();
	}

	@Test
	public void testDispatchRequest() throws Exception
	{
		WsdlMockResponse mockResponse = ModelItemFactory.makeWsdlMockResponse();
		WsdlMockOperation mockOperation = mockResponse.getMockOperation();
		mockOperation.addMockResponse( mockResponse );

		mockResponse.setResponseHttpStatus( HttpStatus.SC_BAD_REQUEST );

		WsdlMockResult mockResult = mockOperation.dispatchRequest( restMockRequest );

		assertThat(mockResult.getMockResponse().getResponseHttpStatus(), is(HttpStatus.SC_BAD_REQUEST));

	}

	private WsdlMockRequest makeRestMockRequest() throws Exception
	{
		HttpServletRequest request = mock( HttpServletRequest.class );
		Enumeration enumeration = mock( Enumeration.class );
		when(request.getHeaderNames()).thenReturn( enumeration );

		HttpServletResponse response = mock( HttpServletResponse.class );
		WsdlMockRunContext context = mock( WsdlMockRunContext.class );

		return new WsdlMockRequest( request, response, context );

	}
}
