/*
 *  soapUI, copyright (C) 2004-2009 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.wsdl.submit.transports.http;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpState;
import org.apache.log4j.Logger;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.submit.RequestFilter;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.attachments.MimeMessageResponse;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.methods.ExtendedDeleteMethod;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.methods.ExtendedGetMethod;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.methods.ExtendedPostMethod;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.methods.ExtendedPutMethod;
import com.eviware.soapui.impl.wsdl.support.PathUtils;
import com.eviware.soapui.impl.wsdl.support.http.HttpClientSupport;
import com.eviware.soapui.impl.wsdl.support.http.SoapUIHostConfiguration;
import com.eviware.soapui.impl.wsdl.support.wss.WssCrypto;
import com.eviware.soapui.model.iface.Response;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionUtils;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.model.support.ModelSupport;
import com.eviware.soapui.settings.HttpSettings;
import com.eviware.soapui.support.types.StringToStringMap;

/**
 * HTTP transport that uses HttpClient to send/receive SOAP messages
 * 
 * @author Ole.Matzura
 */

public class HttpClientRequestTransport implements BaseHttpRequestTransport
{
	private List<RequestFilter> filters = new ArrayList<RequestFilter>();
	private final static Logger log = Logger.getLogger( HttpClientRequestTransport.class );

	public HttpClientRequestTransport()
	{
	}

	public void addRequestFilter( RequestFilter filter )
	{
		filters.add( filter );
	}

	public void removeRequestFilter( RequestFilter filter )
	{
		filters.remove( filter );
	}

	public void abortRequest( SubmitContext submitContext )
	{
		HttpMethodBase postMethod = ( HttpMethodBase )submitContext.getProperty( HTTP_METHOD );
		if( postMethod != null )
			postMethod.abort();
	}

	public Response sendRequest( SubmitContext submitContext, AbstractHttpRequest<?> httpRequest ) throws Exception
	{
		HttpClient httpClient = HttpClientSupport.getHttpClient();
		ExtendedHttpMethod httpMethod = createHttpMethod( httpRequest );
		boolean createdState = false;

		HttpState httpState = ( HttpState )submitContext.getProperty( SubmitContext.HTTP_STATE_PROPERTY );
		if( httpState == null )
		{
			httpState = new HttpState();
			submitContext.setProperty( SubmitContext.HTTP_STATE_PROPERTY, httpState );
			createdState = true;
		}

		HostConfiguration hostConfiguration = new HostConfiguration();

		String localAddress = System.getProperty( "soapui.bind.address", httpRequest.getBindAddress() );
		if( localAddress == null || localAddress.trim().length() == 0 )
			localAddress = SoapUI.getSettings().getString( HttpSettings.BIND_ADDRESS, null );

		if( localAddress != null && localAddress.trim().length() > 0 )
		{
			try
			{
				hostConfiguration.setLocalAddress( InetAddress.getByName( localAddress ) );
			}
			catch( Exception e )
			{
				SoapUI.logError( e );
			}
		}

		submitContext.removeProperty( RESPONSE );
		submitContext.setProperty( HTTP_METHOD, httpMethod );
		submitContext.setProperty( POST_METHOD, httpMethod );
		submitContext.setProperty( HTTP_CLIENT, httpClient );
		submitContext.setProperty( REQUEST_CONTENT, httpRequest.getRequestContent() );
		submitContext.setProperty( HOST_CONFIGURATION, hostConfiguration );
		submitContext.setProperty( WSDL_REQUEST, httpRequest );
		submitContext.setProperty( RESPONSE_PROPERTIES, new StringToStringMap() );

		for( RequestFilter filter : filters )
		{
			filter.filterRequest( submitContext, httpRequest );
		}

		try
		{
			Settings settings = httpRequest.getSettings();

			// custom http headers last so they can be overridden
			StringToStringMap headers = httpRequest.getRequestHeaders();
			for( String header : headers.keySet() )
			{
				String headerValue = headers.get( header );
				headerValue = PropertyExpansionUtils.expandProperties( submitContext, headerValue );
				httpMethod.setRequestHeader( header, headerValue );
			}

			// do request
			WsdlProject project = ( WsdlProject )ModelSupport.getModelItemProject( httpRequest );
			if( project != null )
			{
				WssCrypto crypto = project.getWssContainer().getCryptoByName(
						PropertyExpansionUtils.expandProperties( submitContext, httpRequest.getSslKeystore() ) );

				if( crypto != null && WssCrypto.STATUS_OK.equals( crypto.getStatus() ) )
				{
					hostConfiguration.getParams().setParameter( SoapUIHostConfiguration.SOAPUI_SSL_CONFIG,
							crypto.getSource() + " " + crypto.getPassword() );
				}
			}

			// dump file?
			httpMethod.setDumpFile( PathUtils.expandPath( httpRequest.getDumpFile(), httpRequest, submitContext ) );

			// include request time?
			if( settings.getBoolean( HttpSettings.INCLUDE_REQUEST_IN_TIME_TAKEN ) )
				httpMethod.initStartTime();

			// submit!
			httpClient.executeMethod( hostConfiguration, httpMethod, httpState );
			httpMethod.getTimeTaken();
		}
		catch( Throwable t )
		{
			httpMethod.setFailed( t );

			if( t instanceof Exception )
				throw ( Exception )t;

			SoapUI.logError( t );
			throw new Exception( t );
		}
		finally
		{
			for( int c = filters.size() - 1; c >= 0; c-- )
			{
				filters.get( c ).afterRequest( submitContext, httpRequest );
			}

			if( !submitContext.hasProperty( RESPONSE ) )
			{
				createDefaultResponse( submitContext, httpRequest, httpMethod );
			}

			Response response = ( Response )submitContext.getProperty( BaseHttpRequestTransport.RESPONSE );
			StringToStringMap responseProperties = ( StringToStringMap )submitContext
					.getProperty( BaseHttpRequestTransport.RESPONSE_PROPERTIES );

			for( String key : responseProperties.keySet() )
			{
				response.setProperty( key, responseProperties.get( key ) );
			}

			if( httpMethod != null )
			{
				httpMethod.releaseConnection();
			}
			else
				log.error( "PostMethod is null" );

			if( createdState )
			{
				submitContext.setProperty( SubmitContext.HTTP_STATE_PROPERTY, null );
			}
		}

		return ( Response )submitContext.getProperty( BaseHttpRequestTransport.RESPONSE );
	}

	private void createDefaultResponse( SubmitContext submitContext, AbstractHttpRequest<?> httpRequest,
			ExtendedHttpMethod httpMethod )
	{
		String requestContent = ( String )submitContext.getProperty( BaseHttpRequestTransport.REQUEST_CONTENT );

		// check content-type for multiplart
		Header responseContentTypeHeader = httpMethod.getResponseHeader( "Content-Type" );
		Response response = null;

		if( responseContentTypeHeader != null
				&& responseContentTypeHeader.getValue().toUpperCase().startsWith( "MULTIPART" ) )
		{
			response = new MimeMessageResponse( httpRequest, httpMethod, requestContent, submitContext );
		}
		else
		{
			response = new SinglePartHttpResponse( httpRequest, httpMethod, requestContent, submitContext );
		}

		submitContext.setProperty( BaseHttpRequestTransport.RESPONSE, response );
	}

	private ExtendedHttpMethod createHttpMethod( AbstractHttpRequest<?> httpRequest )
	{
		if( httpRequest instanceof RestRequest )
		{
			RestRequest restRequest = ( RestRequest )httpRequest;
			switch( restRequest.getMethod() )
			{
			case GET :
				return new ExtendedGetMethod();
			case DELETE :
				return new ExtendedDeleteMethod();
			case PUT :
				return new ExtendedPutMethod();
			}
		}

		return new ExtendedPostMethod();
	}

}
