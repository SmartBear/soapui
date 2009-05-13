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
import java.util.Enumeration;
import java.util.HashSet;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.mortbay.util.IO;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.actions.monitor.SoapMonitorAction.LaunchForm;
import com.eviware.soapui.impl.wsdl.monitor.JProxyServletWsdlMonitorMessageExchange;
import com.eviware.soapui.impl.wsdl.monitor.SoapMonitor;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.methods.ExtendedGetMethod;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.methods.ExtendedPostMethod;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.support.types.StringToStringMap;

public class ProxyServlet implements Servlet
{

	protected ServletConfig config;
	protected ServletContext context;
	protected HttpClient client;
	protected JProxyServletWsdlMonitorMessageExchange capturedData;
	protected SoapMonitor monitor;
	protected WsdlProject project;
	protected HttpState httpState = null;
	protected Settings settings;

	static HashSet<String> dontProxyHeaders = new HashSet<String>();
	{
		dontProxyHeaders.add( "proxy-connection" );
		dontProxyHeaders.add( "connection" );
		dontProxyHeaders.add( "keep-alive" );
		dontProxyHeaders.add( "transfer-encoding" );
		dontProxyHeaders.add( "te" );
		dontProxyHeaders.add( "trailer" );
		dontProxyHeaders.add( "proxy-authorization" );
		dontProxyHeaders.add( "proxy-authenticate" );
		dontProxyHeaders.add( "upgrade" );
	}

	public ProxyServlet( SoapMonitor soapMonitor )
	{
		this.monitor = soapMonitor;
		this.project = monitor.getProject();
		settings = project.getSettings();
	}

	public void destroy()
	{
	}

	public ServletConfig getServletConfig()
	{
		return config;
	}

	public String getServletInfo()
	{
		return "SoapUI Monitor";
	}

	public void init( ServletConfig config ) throws ServletException
	{

		this.config = config;
		this.context = config.getServletContext();

		client = new HttpClient();

	}

	public synchronized void service( ServletRequest request, ServletResponse response ) throws ServletException,
			IOException
	{

		HttpMethodBase method;
		HttpServletRequest httpRequest = ( HttpServletRequest )request;
		if( httpRequest.getMethod().equals( "GET" ) )
			method = new ExtendedGetMethod();
		else
			method = new ExtendedPostMethod();

		// for this create ui server and port, properties.

		if( capturedData == null )
		{
			capturedData = new JProxyServletWsdlMonitorMessageExchange( project );
			capturedData.setRequestHost( httpRequest.getServerName() );
			capturedData.setRequestHeader( httpRequest );
			capturedData.setTargetURL( httpRequest.getRequestURL().toString() );
		}

		CaptureInputStream capture = new CaptureInputStream( httpRequest.getInputStream() );

		// check connection header
		String connectionHeader = httpRequest.getHeader( "Connection" );
		if( connectionHeader != null )
		{
			connectionHeader = connectionHeader.toLowerCase();
			if( connectionHeader.indexOf( "keep-alive" ) < 0 && connectionHeader.indexOf( "close" ) < 0 )
				connectionHeader = null;
		}

		// copy headers
		boolean xForwardedFor = false;
		@SuppressWarnings( "unused" )
		long contentLength = -1;
		Enumeration<?> headerNames = httpRequest.getHeaderNames();
		while( headerNames.hasMoreElements() )
		{
			String hdr = ( String )headerNames.nextElement();
			String lhdr = hdr.toLowerCase();

			if( dontProxyHeaders.contains( lhdr ) )
				continue;
			if( connectionHeader != null && connectionHeader.indexOf( lhdr ) >= 0 )
				continue;

			if( "content-length".equals( lhdr ) )
				contentLength = request.getContentLength();

			Enumeration<?> vals = httpRequest.getHeaders( hdr );
			while( vals.hasMoreElements() )
			{
				String val = ( String )vals.nextElement();
				if( val != null )
				{
					method.setRequestHeader( lhdr, val );
					xForwardedFor |= "X-Forwarded-For".equalsIgnoreCase( hdr );
				}
			}
		}

		// Proxy headers
		method.setRequestHeader( "Via", "SoapUI Monitor" );
		if( !xForwardedFor )
			method.addRequestHeader( "X-Forwarded-For", request.getRemoteAddr() );

		if( method instanceof ExtendedPostMethod )
			( ( ExtendedPostMethod )method ).setRequestEntity( new InputStreamRequestEntity( capture,
					"text/xml; charset=utf-8" ) );

		HostConfiguration hostConfiguration = new HostConfiguration();

		StringBuffer url = new StringBuffer( "http://" );
		url.append( httpRequest.getServerName() );
		if( httpRequest.getServerPort() != 80 )
			url.append( ":" + httpRequest.getServerPort() );
		if( httpRequest.getServletPath() != null )
		{
			url.append( httpRequest.getServletPath() );
			method.setPath( httpRequest.getServletPath() );
			if( httpRequest.getQueryString() != null )
			{
				url.append( "?" + httpRequest.getQueryString() );
				method.setPath( httpRequest.getServletPath() + "?" + httpRequest.getQueryString() );
			}
		}
		hostConfiguration.setHost( new URI( url.toString(), true ) );

		SoapUI.log( "PROXY to:" + url );

		if( settings.getBoolean( LaunchForm.SSLTUNNEL_REUSESTATE ) )
		{
			if( httpState == null )
				httpState = new HttpState();
			client.executeMethod( hostConfiguration, method, httpState );
		}
		else
		{
			client.executeMethod( hostConfiguration, method );
		}

		// wait for transaction to end and store it.
		capturedData.stopCapture();

		byte[] res = method.getResponseBody();
		// IO.copy(new ByteArrayInputStream(method.getResponseBody()),
		// response.getOutputStream());
		capturedData.setRequest( capture.getCapturedData() );
		capturedData.setResponse( res );
		capturedData.setResponseHeader( method );
		capturedData.setRawRequestData( getRequestToBytes( method, capture ) );
		capturedData.setRawResponseData( getResponseToBytes( method, res ) );
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

		method.releaseConnection();
	}

	private byte[] getResponseToBytes( HttpMethodBase postMethod, byte[] res )
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

	private byte[] getRequestToBytes( HttpMethodBase postMethod, CaptureInputStream capture )
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
