package com.eviware.soapui.impl.rest.support.handlers;

import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.wsdl.submit.transports.http.BaseHttpRequestTransport;
import com.eviware.soapui.impl.wsdl.submit.transports.http.ExtendedHttpMethod;
import com.eviware.soapui.impl.wsdl.submit.transports.http.HttpResponse;
import com.eviware.soapui.impl.wsdl.submit.transports.http.SinglePartHttpResponse;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.metrics.SoapUIMetrics;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContext;
import org.apache.commons.httpclient.URI;
import org.apache.http.Header;
import org.apache.http.ProtocolVersion;
import org.apache.http.io.HttpTransportMetrics;
import org.junit.Test;

import java.net.URISyntaxException;

import static com.eviware.soapui.utils.ModelItemFactory.makeRestRequest;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created with IntelliJ IDEA.
 * User: manne
 * Date: 3/3/14
 * Time: 10:49 AM
 * To change this template use File | Settings | File Templates.
 */
public class JsonMediaTypeHandlerTest
{

	public static final String ENDPOINT = "http://somehost.com";

	@Test
	public void retainsOriginalUriAsNamespaceUri() throws Exception
	{
		RestRequest restRequest = makeRestRequest();
		restRequest.setEndpoint( ENDPOINT );
		String originalPath = "/original/path";
		restRequest.setPath( originalPath );

		SubmitContext mock = mock( SubmitContext.class );
		ExtendedHttpMethod httpMethod = prepareHttpMethodWith( originalPath );
		when(mock.getProperty( BaseHttpRequestTransport.HTTP_METHOD )).thenReturn( httpMethod );
		when(mock.getProperty( BaseHttpRequestTransport.REQUEST_URI )).thenReturn( new URI( ENDPOINT + originalPath ) );
		restRequest.submit( mock, false);
		HttpResponse response = makeResponseFor( restRequest, originalPath );

		JsonMediaTypeHandler handler = new JsonMediaTypeHandler();
		String originalXml = handler.createXmlRepresentation( response );
		System.out.println( "originalXml = " + originalXml );
		String anotherPath = "/another/path";
		restRequest.setPath( anotherPath );
		httpMethod = prepareHttpMethodWith( originalPath );
		when(mock.getProperty( BaseHttpRequestTransport.HTTP_METHOD )).thenReturn( httpMethod );
		when(mock.getProperty( BaseHttpRequestTransport.REQUEST_URI )).thenReturn(new URI( ENDPOINT + anotherPath ));
		restRequest.submit(mock(SubmitContext.class), false);
		HttpResponse responseWithNewPath = makeResponseFor( restRequest, anotherPath );
		assertThat(handler.createXmlRepresentation( responseWithNewPath), is(originalXml) );
	}

	private SinglePartHttpResponse makeResponseFor( RestRequest restRequest, String originalPath ) throws URISyntaxException
	{
		ExtendedHttpMethod httpMethod = prepareHttpMethodWith(originalPath);
		SinglePartHttpResponse response =
				new SinglePartHttpResponse( restRequest, httpMethod, null, mock( PropertyExpansionContext.class ) );
		response.setResponseContent( "{ firstName: 'Kalle', secondName: 'Ek' }"  );
		return response;
	}

	private ExtendedHttpMethod prepareHttpMethodWith( String path ) throws URISyntaxException
	{
		ExtendedHttpMethod httpMethod = mock(ExtendedHttpMethod.class);
		when(httpMethod.getResponseContentType()).thenReturn( "text/json" );
		when(httpMethod.getMethod()).thenReturn( "GET" );
		when(httpMethod.getProtocolVersion()).thenReturn( new ProtocolVersion( "http", 1, 1 ) );
		SoapUIMetrics soapUIMetrics = new SoapUIMetrics( mock( HttpTransportMetrics.class ),
				mock( HttpTransportMetrics.class ) );
		when(httpMethod.getMetrics()).thenReturn( soapUIMetrics );
		when(httpMethod.getAllHeaders()).thenReturn( new Header[0] );
		when( httpMethod.getResponseReadTime() ).thenReturn( 10L );
		when(httpMethod.getURI()).thenReturn( new java.net.URI( ENDPOINT + path) );
		return httpMethod;
	}
}
