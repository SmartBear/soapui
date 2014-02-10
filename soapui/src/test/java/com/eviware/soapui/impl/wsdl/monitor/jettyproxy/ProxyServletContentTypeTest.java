/*
 *  SoapUI, copyright (C) 2004-2014 smartbear.com 
 *
 *  SoapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  SoapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */
package com.eviware.soapui.impl.wsdl.monitor.jettyproxy;

import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.monitor.SoapMonitorListenerCallBack;
import com.eviware.soapui.impl.wsdl.submit.transports.http.ExtendedHttpMethod;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.message.BasicHeader;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * @author joel.jonsson
 */
public class ProxyServletContentTypeTest
{
	private ProxyServlet proxyServlet;

	@Mock
	private SoapMonitorListenerCallBack listenerCallBack;
	@Mock
	private WsdlProject project;

	@Before
	public void setUp() throws Exception
	{
		initMocks( this );
		proxyServlet = new ProxyServlet( project, listenerCallBack );
	}

	@Test
	public void noContentTypeMatchesRequestWithNoContentType()
	{
		String[] contentTypeMonitor = new String[]{};
		ExtendedHttpMethod request = createRequestWithContentTypes( );
		assertThat( proxyServlet.contentTypeMatches( contentTypeMonitor, request ), is(true));
	}

	@Test
	public void noContentTypeDoNotMatchRequestWithContentType()
	{
		String[] contentTypeMonitor = new String[]{};
		ExtendedHttpMethod request = createRequestWithContentTypes( "text/plain" );
		assertThat( proxyServlet.contentTypeMatches( contentTypeMonitor, request ), is( false ) );
	}

	@Test
	public void fullWildCardMatchesRequestWithContentType()
	{
		String[] contentTypeMonitor = new String[]{"*/*"};
		ExtendedHttpMethod request = createRequestWithContentTypes( "text/plain" );
		assertThat( proxyServlet.contentTypeMatches( contentTypeMonitor, request ), is( true ) );
	}

	@Test
	public void fullWildCardMatchesRequestWithContentTypeAndParameters()
	{
		String[] contentTypeMonitor = new String[]{"*/*"};
		ExtendedHttpMethod request = createRequestWithContentTypes( "text/plain; charset=utf-8" );
		assertThat( proxyServlet.contentTypeMatches( contentTypeMonitor, request ), is( true ) );
	}

	@Test
	public void wildCardPrimaryTypeMatchesRequestWithContentType()
	{
		String[] contentTypeMonitor = new String[]{"*/plain"};
		ExtendedHttpMethod request = createRequestWithContentTypes( "text/plain" );
		assertThat( proxyServlet.contentTypeMatches( contentTypeMonitor, request ), is( true ) );
	}

	@Test
	public void wildCardPrimaryTypeDoesNotMatchRequestWithOtherSubtype()
	{
		String[] contentTypeMonitor = new String[]{"*/xml"};
		ExtendedHttpMethod request = createRequestWithContentTypes( "text/plain" );
		assertThat( proxyServlet.contentTypeMatches( contentTypeMonitor, request ), is( false ) );
	}

	@Test
	public void wildCardSubTypeMatchesRequestWithContentType()
	{
		String[] contentTypeMonitor = new String[]{"text/*"};
		ExtendedHttpMethod request = createRequestWithContentTypes( "text/plain" );
		assertThat( proxyServlet.contentTypeMatches( contentTypeMonitor, request ), is( true ) );
	}

	@Test
	public void wildCardSubTypeDoesNotMatchRequestWithOtherPrimaryType()
	{
		String[] contentTypeMonitor = new String[]{"application/*"};
		ExtendedHttpMethod request = createRequestWithContentTypes( "text/plain" );
		assertThat( proxyServlet.contentTypeMatches( contentTypeMonitor, request ), is( false ) );
	}

	@Test
	public void equalContentTypesMatches()
	{
		String[] contentTypeMonitor = new String[]{"text/plain"};
		ExtendedHttpMethod request = createRequestWithContentTypes( "text/plain" );
		assertThat( proxyServlet.contentTypeMatches( contentTypeMonitor, request ), is( true ) );
	}

	@Test
	public void equalContentTypesMatchesEvenWithParameters()
	{
		String[] contentTypeMonitor = new String[]{"text/plain"};
		ExtendedHttpMethod request = createRequestWithContentTypes( "text/plain; charset=utf-8" );
		assertThat( proxyServlet.contentTypeMatches( contentTypeMonitor, request ), is( true ) );
	}

	@Test
	public void invalidContentTypeIsSilentlyIgnoredAndDoesNotMatch()
	{
		String[] contentTypeMonitor = new String[]{"hejhopp"};
		ExtendedHttpMethod request = createRequestWithContentTypes( "hejhopp/hejhopp" );
		assertThat( proxyServlet.contentTypeMatches( contentTypeMonitor, request ), is( false ) );
	}

	@Test
	public void invalidRetrievedContentTypeIsSilentlyIgnoredAndDoesNotMatch()
	{
		String[] contentTypeMonitor = new String[]{"*/*"};
		ExtendedHttpMethod request = createRequestWithContentTypes( "hejhopp" );
		assertThat( proxyServlet.contentTypeMatches( contentTypeMonitor, request ), is( false ) );
	}

	private ExtendedHttpMethod createRequestWithContentTypes( String... contentTypes )
	{
		ExtendedHttpMethod method = mock( ExtendedHttpMethod.class );
		HttpResponse httpResponse = mock( HttpResponse.class );
		when( method.hasHttpResponse() ).thenReturn( true );
		when( method.getHttpResponse() ).thenReturn( httpResponse );
		Header[] headers = new Header[contentTypes.length];
		for( int i = 0; i < headers.length; i++ )
		{
			headers[i] = new BasicHeader( "Content-Type", contentTypes[i] );
		}
		when( httpResponse.getHeaders( eq( "Content-Type" ) ) ).thenReturn( headers );
		return method;
	}
}
