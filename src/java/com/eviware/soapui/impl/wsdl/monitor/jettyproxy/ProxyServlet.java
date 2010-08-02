/*
 *  soapUI, copyright (C) 2004-2010 eviware.com 
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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;

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
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.mortbay.util.IO;

import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.actions.monitor.SoapMonitorAction;
import com.eviware.soapui.impl.wsdl.actions.monitor.SoapMonitorAction.LaunchForm;
import com.eviware.soapui.impl.wsdl.monitor.JProxyServletWsdlMonitorMessageExchange;
import com.eviware.soapui.impl.wsdl.monitor.SoapMonitor;
import com.eviware.soapui.impl.wsdl.submit.transports.http.ExtendedHttpMethod;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.methods.ExtendedGetMethod;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.methods.ExtendedPostMethod;
import com.eviware.soapui.impl.wsdl.support.http.HttpClientSupport;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.support.types.StringToStringsMap;
import com.eviware.soapui.support.xml.XmlUtils;

public class ProxyServlet implements Servlet
{

	protected ServletConfig config;
	protected ServletContext context;
	protected SoapMonitor monitor;
	protected WsdlProject project;
	protected HttpState httpState = new HttpState();
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
	}

	public void service( ServletRequest request, ServletResponse response ) throws ServletException, IOException
	{
		monitor.fireOnRequest( request, response );
		if( response.isCommitted() )
			return;

		ExtendedHttpMethod method;
		HttpServletRequest httpRequest = ( HttpServletRequest )request;
		if( httpRequest.getMethod().equals( "GET" ) )
			method = new ExtendedGetMethod();
		else
			method = new ExtendedPostMethod();

		method.setDecompress( false );

		// for this create ui server and port, properties.
		JProxyServletWsdlMonitorMessageExchange capturedData = new JProxyServletWsdlMonitorMessageExchange( project );
		capturedData.setRequestHost( httpRequest.getServerName() );
		capturedData.setRequestMethod( httpRequest.getMethod() );
		capturedData.setRequestHeader( httpRequest );
		capturedData.setHttpRequestParameters( httpRequest );
		capturedData.setTargetURL( httpRequest.getRequestURL().toString() );

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
			( ( ExtendedPostMethod )method ).setRequestEntity( new InputStreamRequestEntity( capture, request
					.getContentType() ) );

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

		// SoapUI.log("PROXY to:" + url);

		monitor.fireBeforeProxy( request, response, method, hostConfiguration );

		if( settings.getBoolean( LaunchForm.SSLTUNNEL_REUSESTATE ) )
		{
			if( httpState == null )
				httpState = new HttpState();
			HttpClientSupport.getHttpClient().executeMethod( hostConfiguration, method, httpState );
		}
		else
		{
			HttpClientSupport.getHttpClient().executeMethod( hostConfiguration, method );
		}

		// wait for transaction to end and store it.
		capturedData.stopCapture();

		capturedData.setRequest( capture.getCapturedData() );
		capturedData.setRawResponseBody( method.getResponseBody() );
		capturedData.setResponseHeader( method );
		capturedData.setRawRequestData( getRequestToBytes( request.toString(), method, capture ) );
		capturedData.setRawResponseData( getResponseToBytes( response.toString(), method, capturedData
				.getRawResponseBody() ) );
		capturedData.setResponseContent( new String( method.getDecompressedResponseBody() ) );

		monitor.fireAfterProxy( request, response, method, capturedData );

		if( !response.isCommitted() )
		{
			StringToStringsMap responseHeaders = capturedData.getResponseHeaders();
			// capturedData = null;

			// copy headers to response
			HttpServletResponse httpResponse = ( HttpServletResponse )response;
			for( String name : responseHeaders.keySet() )
			{
				for( String header : responseHeaders.get( name ) )
					httpResponse.addHeader( name, header );
			}

			IO.copy( new ByteArrayInputStream( capturedData.getRawResponseBody() ), httpResponse.getOutputStream() );
		}

		synchronized( this )
		{
			if( checkContentType( method ) )
			{
				monitor.addMessageExchange( capturedData );
			}
		}
	}

	private boolean checkContentType( ExtendedHttpMethod method )
	{
		String[] contentTypes = settings
				.getString( LaunchForm.SET_CONTENT_TYPES, SoapMonitorAction.defaultContentTypes() ).split( "," );
		List<String> contentTypelist = new ArrayList<String>();
		for( String ct : contentTypes )
		{
			contentTypelist.add( ct.trim().replace( "*", "" ) );
		}

		Header[] headers = method.getResponseHeaders( "Content-Type" );
		for( Header header : headers )
		{
			for( String contentType : contentTypelist )
			{
				if( header.getValue().indexOf( contentType ) > 0 )
				{
					return true;
				}
			}
		}
		return false;
	}

	private byte[] getResponseToBytes( String status, ExtendedHttpMethod postMethod, byte[] res )
	{
		String response = status.trim() + "\r\n";

		Header[] headers = postMethod.getResponseHeaders();
		for( Header header : headers )
		{
			response += header.toString().trim() + "\r\n";
		}
		response += "\r\n";

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try
		{
			out.write( response.getBytes() );
			out.write( res );
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}
		return out.toByteArray();
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
