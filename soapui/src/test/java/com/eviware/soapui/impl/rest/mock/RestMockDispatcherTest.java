package com.eviware.soapui.impl.rest.mock;

import com.eviware.soapui.impl.wsdl.mock.WsdlMockRunContext;
import com.eviware.soapui.model.mock.MockRequest;
import com.eviware.soapui.model.mock.MockResult;
import org.apache.commons.httpclient.HttpStatus;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Enumeration;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;
import static com.eviware.soapui.impl.rest.RestRequestInterface.*;


public class RestMockDispatcherTest
{

	private HttpServletRequest request;
	private HttpServletResponse response;
	private RestMockDispatcher restMockDispatcher;
	private WsdlMockRunContext context;
	private RestMockService restMockService;

	@Before
	public void setUp()
	{
		createRestMockDispatcher();
	}

	@Test
	public void aferRequestScriptIsCalled() throws Exception
	{
		RestMockResult mockResult = ( RestMockResult )restMockDispatcher.dispatchRequest( request, response );

		verify( restMockService ).runAfterRequestScript( context, mockResult );
	}

	@Test
	public void onRequestScriptIsCalled() throws Exception
	{
		createRestMockDispatcher();
		RestMockResult mockResult = ( RestMockResult )restMockDispatcher.dispatchRequest( request, response );

		verify( restMockService ).runOnRequestScript( any( WsdlMockRunContext.class ), any( MockRequest.class ) );
	}

	@Test
	public void onRequestScriptOverridesRegularDispatching() throws Exception
	{
		/*
			When onRequestScript returns a MockResult instance then regular dispatching is ignored.
			This tests verify when script returns MokResult instance we bypass regular dispatching.

		 */

		createRestMockDispatcher();
		RestMockResult  restMockResult = mock(RestMockResult.class );
		when( restMockService.runOnRequestScript( any( WsdlMockRunContext.class ), any( MockRequest.class ))).thenReturn( restMockResult );

		restMockDispatcher.dispatchRequest( request, response );

		// we would like to verify that dispatchRequest is never called but it is hard so we verify on this instead
		verify( restMockService, never() ).findBestMatchedOperation( anyString(), any( HttpMethod.class ) );
	}

	@Test
	public void shouldReturnNoResponseFoundWhenThereIsNoMatchingAction() throws Exception
	{
		createRestMockDispatcher();
		when( restMockService.findBestMatchedOperation( anyString(), any( HttpMethod.class ) ) ).thenReturn( null );

		restMockDispatcher.dispatchRequest( request, response );

		verify( response ).setStatus( HttpStatus.SC_NOT_FOUND );
	}


	@Test
	public void returnsErrorOnrequestScriptException() throws Exception
	{
		createRestMockDispatcher();
		Exception runTimeException = new IllegalStateException( "wrong state" );
		when( restMockService.runOnRequestScript( any( WsdlMockRunContext.class ), any( MockRequest.class ))).thenThrow( runTimeException );

		restMockDispatcher.dispatchRequest( request, response );

		verify( response ).setStatus( HttpStatus.SC_INTERNAL_SERVER_ERROR );
	}


	private void createRestMockDispatcher()
	{
		request = mock( HttpServletRequest.class );
		Enumeration enumeration = mock( Enumeration.class );
		when( request.getHeaderNames() ).thenReturn( enumeration );
		when( request.getMethod() ).thenReturn( HttpMethod.DELETE.name() );

		response = mock( HttpServletResponse.class );
		restMockService = mock( RestMockService.class );
		context = mock( WsdlMockRunContext.class );

		restMockDispatcher = new RestMockDispatcher( restMockService, context );
	}
}
