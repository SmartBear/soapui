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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
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

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.mortbay.util.IO;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.actions.monitor.SoapMonitorAction;
import com.eviware.soapui.impl.wsdl.actions.monitor.SoapMonitorAction.LaunchForm;
import com.eviware.soapui.impl.wsdl.monitor.CaptureInputStream;
import com.eviware.soapui.impl.wsdl.monitor.JProxyServletWsdlMonitorMessageExchange;
import com.eviware.soapui.impl.wsdl.monitor.SoapMonitor;
import com.eviware.soapui.impl.wsdl.submit.transports.http.ExtendedHttpMethod;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.methods.ExtendedGetMethod;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.methods.ExtendedPostMethod;
import com.eviware.soapui.impl.wsdl.support.http.HttpClientSupport;
import com.eviware.soapui.impl.wsdl.support.http.SoapUIHostConfiguration;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.support.types.StringToStringsMap;

public class ProxyServlet implements Servlet
{
	protected ServletConfig config;
	protected ServletContext context;
	protected SoapMonitor monitor;
	protected WsdlProject project;
	protected HttpContext httpState = new BasicHttpContext();
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
					method.setHeader( lhdr, val );
					xForwardedFor |= "X-Forwarded-For".equalsIgnoreCase( hdr );
				}
			}
		}

		// Proxy headers
		method.setHeader( "Via", "SoapUI Monitor" );
		if( !xForwardedFor )
			method.addHeader( "X-Forwarded-For", request.getRemoteAddr() );

		if( method instanceof ExtendedPostMethod )
		{
			( ( ExtendedPostMethod )method ).setEntity( new InputStreamEntity( capture, -1 ) );
		}

		StringBuffer url = new StringBuffer( "http://" );
		url.append( httpRequest.getServerName() );
		if( httpRequest.getServerPort() != 80 )
			url.append( ":" + httpRequest.getServerPort() );

		java.net.URI tempUri = null;

		if( httpRequest.getServletPath() != null )
		{
			url.append( httpRequest.getServletPath() );

			try
			{
				tempUri = new java.net.URI( url.toString() );
				method.setURI( tempUri );
			}
			catch( URISyntaxException e )
			{
				SoapUI.logError( e );
			}

			if( httpRequest.getQueryString() != null )
			{
				url.append( "?" + httpRequest.getQueryString() );

				try
				{
					tempUri = new java.net.URI( url.toString() );
					method.setURI( tempUri );
				}
				catch( URISyntaxException e )
				{
					SoapUI.logError( e );
				}
			}
		}

		SoapUIHostConfiguration hostConfiguration = new SoapUIHostConfiguration();
		if( tempUri != null )
		{
			hostConfiguration.setHttpHost( new HttpHost( tempUri.getHost(), tempUri.getPort(), tempUri.getScheme() ) );
		}

		HttpResponse httpResponse = null;

		// SoapUI.log("PROXY to:" + url);

		monitor.fireBeforeProxy( request, response, method, hostConfiguration );

		if( settings.getBoolean( LaunchForm.SSLTUNNEL_REUSESTATE ) )
		{
			if( httpState == null )
				httpState = new BasicHttpContext();
			httpResponse = HttpClientSupport.execute( hostConfiguration, method, httpState );
		}
		else
		{
			httpResponse = HttpClientSupport.execute( hostConfiguration, method );
		}

		// wait for transaction to end and store it.
		capturedData.stopCapture();

		capturedData.setRequest( capture.getCapturedData() );
		capturedData.setRawResponseBody( method.getResponseBody() );
		capturedData.setResponseHeader( method );
		capturedData.setRawRequestData( getRequestToBytes( request.toString(), capture ) );
		capturedData.setRawResponseData( getResponseToBytes( response.toString(), httpResponse,
				capturedData.getRawResponseBody() ) );
		capturedData.setResponseContent( new String( method.getDecompressedResponseBody() ) );

		monitor.fireAfterProxy( request, response, method, capturedData );

		if( !response.isCommitted() )
		{
			StringToStringsMap responseHeaders = capturedData.getResponseHeaders();
			// capturedData = null;

			// copy headers to response
			HttpServletResponse httpServletResponse = ( HttpServletResponse )response;
			for( String name : responseHeaders.keySet() )
			{
				for( String header : responseHeaders.get( name ) )
					httpServletResponse.addHeader( name, header );
			}

			IO.copy( new ByteArrayInputStream( capturedData.getRawResponseBody() ), httpServletResponse.getOutputStream() );
		}

		synchronized( this )
		{
			if( checkContentType( httpResponse ) )
			{
				monitor.addMessageExchange( capturedData );
			}
		}
	}

	private boolean checkContentType( HttpResponse httpResponse )
	{
		String[] contentTypes = settings
				.getString( LaunchForm.SET_CONTENT_TYPES, SoapMonitorAction.defaultContentTypes() ).split( "," );
		List<String> contentTypelist = new ArrayList<String>();
		for( String ct : contentTypes )
		{
			contentTypelist.add( ct.trim().replace( "*", "" ) );
		}

		Header[] headers = httpResponse.getHeaders( "Content-Type" );
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

	private byte[] getResponseToBytes( String status, HttpResponse httpResponse, byte[] res )
	{
		String response = status.trim() + "\r\n";

		Header[] headers = httpResponse.getAllHeaders();
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

	private byte[] getRequestToBytes( String footer, CaptureInputStream capture )
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		try
		{
			out.write( footer.trim().getBytes() );
			out.write( "\r\n\r\n".getBytes() );
			out.write( capture.getCapturedData() );
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}

		return out.toByteArray();
	}

}
