package com.eviware.soapui.impl.rest.mock;

import com.eviware.soapui.impl.wsdl.mock.WsdlMockRunContext;
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

public class RestMockActionTest
{


	RestMockRequest restMockRequest;

	@Before
	public void setUp() throws Exception
	{
		restMockRequest = makeRestMockRequest();
	}

	@Test
	public void testDispatchRequest() throws Exception
	{
		RestMockAction mockAction = ModelItemFactory.makeRestMockAction( );
		RestMockResponse mockResponse = mockAction.addNewMockResponse( "response 1" );
		mockResponse.setResponseHttpStatus( HttpStatus.SC_BAD_REQUEST );

		RestMockResult mockResult = mockAction.dispatchRequest( restMockRequest );

		assertThat(mockResult.getMockResponse().getResponseHttpStatus(), is(HttpStatus.SC_BAD_REQUEST));

	}

	private RestMockRequest makeRestMockRequest() throws Exception
	{
		HttpServletRequest request = mock( HttpServletRequest.class );
		Enumeration enumeration = mock( Enumeration.class );
		when(request.getHeaderNames()).thenReturn( enumeration );

		HttpServletResponse response = mock( HttpServletResponse.class );
		WsdlMockRunContext context = mock( WsdlMockRunContext.class );

		return new RestMockRequest( request, response, context );

	}
}
