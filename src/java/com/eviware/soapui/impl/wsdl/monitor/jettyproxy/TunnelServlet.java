/*
 *  soapUI, copyright (C) 2004-2011 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.wsdl.monitor.jettyproxy;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.util.Enumeration;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.protocol.BasicHttpContext;
import org.mortbay.util.IO;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.actions.monitor.SoapMonitorAction.LaunchForm;
import com.eviware.soapui.impl.wsdl.actions.monitor.SoapMonitorAction.SecurityTabForm;
import com.eviware.soapui.impl.wsdl.monitor.CaptureInputStream;
import com.eviware.soapui.impl.wsdl.monitor.JProxyServletWsdlMonitorMessageExchange;
import com.eviware.soapui.impl.wsdl.monitor.SoapMonitor;
import com.eviware.soapui.impl.wsdl.submit.transports.http.BaseHttpRequestTransport;
import com.eviware.soapui.impl.wsdl.submit.transports.http.ExtendedHttpMethod;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.methods.ExtendedGetMethod;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.methods.ExtendedPostMethod;
import com.eviware.soapui.impl.wsdl.support.http.HttpClientSupport;
import com.eviware.soapui.impl.wsdl.support.http.ProxyUtils;
import com.eviware.soapui.impl.wsdl.support.http.SoapUIHostConfiguration;
import com.eviware.soapui.model.propertyexpansion.DefaultPropertyExpansionContext;
import com.eviware.soapui.support.types.StringToStringsMap;
import com.eviware.soapui.support.xml.XmlUtils;

public class TunnelServlet extends ProxyServlet
{
	private String sslEndPoint;
	private int sslPort = 443;
	private String prot = "https://";

	public TunnelServlet( SoapMonitor soapMonitor, String sslEndpoint )
	{
		super( soapMonitor );

		if( !sslEndpoint.startsWith( "https" ) )
		{
			this.prot = "http://";
		}
		int prefix = sslEndpoint.indexOf( "://" );
		int c = sslEndpoint.indexOf( prefix, ':' );
		if( c > 0 )
		{
			this.sslPort = Integer.parseInt( sslEndpoint.substring( c + 1 ) );
			this.sslEndPoint = sslEndpoint.substring( prefix, c );
		}
		else
		{
			if( prefix > 0 )
				this.sslEndPoint = sslEndpoint.substring( prefix + 3 );
		}
	}

	@Override
	public void init( ServletConfig config ) throws ServletException
	{
		this.config = config;
		this.context = config.getServletContext();
	}

	public void service( ServletRequest request, ServletResponse response ) throws ServletException, IOException
	{
		monitor.fireOnRequest( request, response );
		if( response.isCommitted() )
			return;

		ExtendedHttpMethod postMethod;

		// for this create ui server and port, properties.
		InetSocketAddress inetAddress = new InetSocketAddress( sslEndPoint, sslPort );
		HttpServletRequest httpRequest = ( HttpServletRequest )request;
		if( httpRequest.getMethod().equals( "GET" ) )
			postMethod = new ExtendedGetMethod();
		else
			postMethod = new ExtendedPostMethod();

		JProxyServletWsdlMonitorMessageExchange capturedData = new JProxyServletWsdlMonitorMessageExchange( project );
		capturedData.setRequestHost( httpRequest.getRemoteHost() );
		capturedData.setRequestHeader( httpRequest );
		capturedData.setTargetURL( this.prot + inetAddress.getHostName() );

		CaptureInputStream capture = new CaptureInputStream( httpRequest.getInputStream() );

		// copy headers
		Enumeration<?> headerNames = httpRequest.getHeaderNames();
		while( headerNames.hasMoreElements() )
		{
			String hdr = ( String )headerNames.nextElement();
			String lhdr = hdr.toLowerCase();

			if( "content-length".equals( lhdr ) )
				continue;

			if( "host".equals( lhdr ) )
			{
				Enumeration<?> vals = httpRequest.getHeaders( hdr );
				while( vals.hasMoreElements() )
				{
					String val = ( String )vals.nextElement();
					if( val.startsWith( "127.0.0.1" ) )
					{
						postMethod.addHeader( hdr, sslEndPoint );
					}
				}
				continue;
			}

			Enumeration<?> vals = httpRequest.getHeaders( hdr );
			while( vals.hasMoreElements() )
			{
				String val = ( String )vals.nextElement();
				if( val != null )
				{
					postMethod.addHeader( hdr, val );
				}
			}
		}

		if( postMethod instanceof ExtendedPostMethod )
		{
			InputStreamEntity entity = new InputStreamEntity( capture, -1 );
			entity.setContentType( request.getContentType() );
			( ( ExtendedPostMethod )postMethod ).setEntity( entity );
		}

		java.net.URI uri = null;
		SoapUIHostConfiguration hostConfiguration = new SoapUIHostConfiguration();
		try
		{
			uri = new java.net.URI( this.prot + sslEndPoint );
			hostConfiguration.setHttpHost( new HttpHost( uri.getHost(), uri.getPort(), uri.getScheme() ) );
		}
		catch( URISyntaxException e )
		{
			SoapUI.logError( e );
		}

		postMethod.getParams().setParameter(
				BaseHttpRequestTransport.SOAPUI_SSL_CONFIG,
				settings.getString( SecurityTabForm.SSLTUNNEL_KEYSTOREPATH, "" ) + " "
						+ settings.getString( SecurityTabForm.SSLTUNNEL_KEYSTOREPASSWORD, "" ) );

		ProxyUtils.initProxySettings( settings, postMethod, httpState, prot + sslEndPoint,
				new DefaultPropertyExpansionContext( project ) );

		String path = null;
		if( sslEndPoint.indexOf( "/" ) < 0 )
			path = "/";
		else
			path = sslEndPoint.substring( sslEndPoint.indexOf( "/" ), sslEndPoint.length() );

		if( uri != null )
		{
			try
			{
				postMethod.setURI( URIUtils.createURI( uri.getScheme(), uri.getHost(), uri.getPort(), path, uri.getQuery(),
						uri.getFragment() ) );
			}
			catch( URISyntaxException e )
			{
				SoapUI.logError( e );
			}
		}

		monitor.fireBeforeProxy( request, response, postMethod, hostConfiguration );

		if( settings.getBoolean( LaunchForm.SSLTUNNEL_REUSESTATE ) )
		{
			if( httpState == null )
				httpState = new BasicHttpContext();
			HttpClientSupport.execute( hostConfiguration, postMethod, httpState );
		}
		else
		{
			HttpClientSupport.execute( hostConfiguration, postMethod );
		}
		capturedData.stopCapture();

		capturedData.setRequest( capture.getCapturedData() );
		capturedData.setRawResponseBody( postMethod.getResponseBody() );
		capturedData.setResponseHeader( postMethod );
		capturedData.setRawRequestData( getRequestToBytes( request.toString(), postMethod, capture ) );
		capturedData.setRawResponseData( getResponseToBytes( response.toString(), postMethod,
				capturedData.getRawResponseBody() ) );

		monitor.fireAfterProxy( request, response, postMethod, capturedData );

		StringToStringsMap responseHeaders = capturedData.getResponseHeaders();
		// copy headers to response
		HttpServletResponse httpServletResponse = ( HttpServletResponse )response;
		for( String name : responseHeaders.keySet() )
		{
			for( String header : responseHeaders.get( name ) )
				httpServletResponse.addHeader( name, header );

		}

		IO.copy( new ByteArrayInputStream( capturedData.getRawResponseBody() ), httpServletResponse.getOutputStream() );

		synchronized( this )
		{
			monitor.addMessageExchange( capturedData );
		}

		capturedData = null;
	}

	private byte[] getResponseToBytes( String footer, ExtendedHttpMethod postMethod, byte[] res )
	{
		String response = footer;

		Header[] headers = postMethod.getAllHeaders();
		for( Header header : headers )
		{
			response += header.toString().trim() + "\n";
		}
		response += "\n";
		response += XmlUtils.prettyPrintXml( new String( res ) );

		return response.getBytes();
	}

	private byte[] getRequestToBytes( String footer, ExtendedHttpMethod postMethod, CaptureInputStream capture )
	{
		String request = footer;

		// Header[] headers = postMethod.getRequestHeaders();
		// for (Header header : headers)
		// {
		// request += header.toString();
		// }
		request += "\n";
		request += XmlUtils.prettyPrintXml( new String( capture.getCapturedData() ) );

		return request.getBytes();
	}

}
