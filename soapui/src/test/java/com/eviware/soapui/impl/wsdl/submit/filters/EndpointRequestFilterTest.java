package com.eviware.soapui.impl.wsdl.submit.filters;

import com.eviware.soapui.impl.settings.XmlBeansSettingsImpl;
import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.impl.support.http.HttpRequest;
import com.eviware.soapui.impl.wsdl.submit.transports.http.BaseHttpRequestTransport;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.settings.HttpSettings;
import org.apache.http.client.methods.HttpRequestBase;
import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.net.URI;
import java.net.URISyntaxException;

public class EndpointRequestFilterTest
{
	EndpointRequestFilter endpointRequestFilter;

	@Before
	public void setUp()
	{
		endpointRequestFilter = new EndpointRequestFilter();
	}

	@Test
	public void doesNotDoubleEncodeAlreadyEncodedUri() throws URISyntaxException
	{
		String encodedUri = "http://google.se/search?q=%3F";

		HttpRequestBase httpMethod = Mockito.mock( HttpRequestBase.class );

		SubmitContext context = mockContext( httpMethod );
		AbstractHttpRequest<?> request = mockRequest( encodedUri, mockSettings() );

		endpointRequestFilter.filterAbstractHttpRequest( context, request );

		ArgumentCaptor<URI> httpMethodUri = ArgumentCaptor.forClass( URI.class );
		Mockito.verify( httpMethod ).setURI( httpMethodUri.capture() );
		Assert.assertThat( httpMethodUri.getValue(), Is.is( new URI( encodedUri ) ) );
	}

	private SubmitContext mockContext( HttpRequestBase httpMethod )
	{
		SubmitContext context = Mockito.mock( SubmitContext.class );
		Mockito.when( context.getProperty( BaseHttpRequestTransport.HTTP_METHOD ) ).thenReturn( httpMethod );
		return context;
	}

	private AbstractHttpRequest<?> mockRequest( String encodedUri, XmlBeansSettingsImpl settings )
	{
		AbstractHttpRequest<?> request = Mockito.mock( HttpRequest.class );
		Mockito.when( request.getEndpoint() ).thenReturn( encodedUri );
		Mockito.when( request.getSettings() ).thenReturn( settings );
		return request;
	}

	private XmlBeansSettingsImpl mockSettings()
	{
		XmlBeansSettingsImpl settings = Mockito.mock( XmlBeansSettingsImpl.class );
		Mockito.when( settings.getBoolean( HttpSettings.ENCODED_URLS ) ).thenReturn( true );
		return settings;
	}
}
