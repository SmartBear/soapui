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
package com.eviware.soapui.impl.wsdl.monitor.jettyproxy;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Enumeration;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.mortbay.util.IO;

import com.eviware.soapui.impl.wsdl.actions.monitor.SoapMonitorAction.LaunchForm;
import com.eviware.soapui.impl.wsdl.monitor.JProxyServletWsdlMonitorMessageExchange;
import com.eviware.soapui.impl.wsdl.monitor.SoapMonitor;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.methods.ExtendedPostMethod;
import com.eviware.soapui.impl.wsdl.support.http.HttpClientSupport;
import com.eviware.soapui.impl.wsdl.support.http.SoapUIHostConfiguration;
import com.eviware.soapui.support.types.StringToStringMap;

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

		client = HttpClientSupport.getHttpClient();
	}

	public void service( ServletRequest request, ServletResponse response ) throws ServletException, IOException
	{

		HttpServletRequest httpRequest = ( HttpServletRequest )request;

		// for this create ui server and port, properties.
		InetSocketAddress inetAddress = new InetSocketAddress( sslEndPoint, sslPort );
		ExtendedPostMethod postMethod = new ExtendedPostMethod();

		if( capturedData == null )
		{
			capturedData = new JProxyServletWsdlMonitorMessageExchange( project );
			capturedData.setRequestHost( httpRequest.getRemoteHost() );
			capturedData.setRequestHeader( httpRequest );
			capturedData.setTargetURL( this.prot + inetAddress.getHostName() );
		}

		CaptureInputStream capture = new CaptureInputStream( httpRequest.getInputStream() );

		// copy headers
		Enumeration<?> headerNames = httpRequest.getHeaderNames();
		while( headerNames.hasMoreElements() )
		{
			String hdr = ( String )headerNames.nextElement();
			String lhdr = hdr.toLowerCase();

			if( "host".equals( lhdr ) )
			{
				Enumeration<?> vals = httpRequest.getHeaders( hdr );
				while( vals.hasMoreElements() )
				{
					String val = ( String )vals.nextElement();
					if( val.startsWith( "127.0.0.1" ) )
					{
						postMethod.addRequestHeader( hdr, sslEndPoint );
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
					postMethod.addRequestHeader( hdr, val );
				}
			}

		}

		postMethod.setRequestEntity( new InputStreamRequestEntity( capture, "text/xml; charset=utf-8" ) );

		HostConfiguration hostConfiguration = new HostConfiguration();

		httpRequest.getProtocol();
		hostConfiguration.getParams().setParameter(
				SoapUIHostConfiguration.SOAPUI_SSL_CONFIG,
				settings.getString( LaunchForm.SSLTUNNEL_KEYSTOREPATH, "" ) + " "
						+ settings.getString( LaunchForm.SSLTUNNEL_KEYSTOREPASSWORD, "" ) );
		hostConfiguration.setHost( new URI( this.prot + sslEndPoint, true ) );

		postMethod.setPath( sslEndPoint.substring( sslEndPoint.indexOf( "/" ), sslEndPoint.length() ) );

		if( settings.getBoolean( LaunchForm.SSLTUNNEL_REUSESTATE ) )
		{
			if( httpState == null )
				httpState = new HttpState();
			client.executeMethod( hostConfiguration, postMethod, httpState );
		}
		else
		{
			client.executeMethod( hostConfiguration, postMethod );
		}
		capturedData.stopCapture();

		byte[] res = postMethod.getResponseBody();
		capturedData.setRequest( capture.getCapturedData() );
		capturedData.setResponse( res );
		capturedData.setResponseHeader( postMethod );
		capturedData.setRawRequestData( getRequestToBytes( postMethod, capture ) );
		capturedData.setRawResponseData( getResponseToBytes( postMethod, res ) );
		monitor.addMessageExchange( capturedData );

		StringToStringMap responseHeaders = capturedData.getResponseHeaders();
		capturedData = null;
		// copy headers to response
		HttpServletResponse httpResponse = ( HttpServletResponse )response;
		for( String name : responseHeaders.keySet() )
		{
			String header = responseHeaders.get( name );
			httpResponse.addHeader( name, header );

		}

		IO.copy( new ByteArrayInputStream( res ), httpResponse.getOutputStream() );

		postMethod.releaseConnection();

	}

	private byte[] getResponseToBytes( ExtendedPostMethod postMethod, byte[] res )
	{
		String response = "";

		Header[] headers = postMethod.getResponseHeaders();
		for( Header header : headers )
		{
			response += header.toString();
		}
		response += "\n";
		response += new String( res );

		return response.getBytes();
	}

	private byte[] getRequestToBytes( ExtendedPostMethod postMethod, CaptureInputStream capture )
	{
		String request = "";

		Header[] headers = postMethod.getRequestHeaders();
		for( Header header : headers )
		{
			request += header.toString();
		}
		request += "\n";
		request += new String( capture.getCapturedData() );

		return request.getBytes();
	}

}
