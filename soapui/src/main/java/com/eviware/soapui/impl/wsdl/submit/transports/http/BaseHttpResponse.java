/*
 *  SoapUI, copyright (C) 2004-2012 smartbear.com
 *
 *  SoapUI is free software; you can redistribute it and/or modify it under the
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  SoapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.wsdl.submit.transports.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.List;

import org.apache.http.Header;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.rest.support.MediaTypeHandler;
import com.eviware.soapui.impl.rest.support.MediaTypeHandlerRegistry;
import com.eviware.soapui.impl.support.AbstractHttpRequestInterface;
import com.eviware.soapui.impl.support.http.HttpRequest;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.metrics.SoapUIMetrics;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.TestRequest;
import com.eviware.soapui.model.iface.Attachment;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContext;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.model.testsuite.TestCase;
import com.eviware.soapui.settings.HttpSettings;
import com.eviware.soapui.settings.UISettings;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.soapui.support.types.StringToStringsMap;

public abstract class BaseHttpResponse implements HttpResponse
{

	private StringToStringsMap requestHeaders;
	private StringToStringsMap responseHeaders;

	private long timeTaken;
	private long timestamp;
	private String contentType;
	private SSLInfo sslInfo;
	private URL url;
	private WeakReference<AbstractHttpRequestInterface<?>> httpRequest;
	private String method;
	private String version;
	private StringToStringMap properties;
	private byte[] rawRequestData;
	private byte[] rawResponseData;
	private byte[] rawResponseBody;
	private int requestContentPos = -1;
	private String xmlContent;
	private boolean downloadIncludedResources;
	private Attachment[] attachments = new Attachment[0];
	protected HTMLPageSourceDownloader downloader;
	private int statusCode;

	public BaseHttpResponse( ExtendedHttpMethod httpMethod, AbstractHttpRequestInterface<?> httpRequest,
			PropertyExpansionContext context )
	{
		this.httpRequest = new WeakReference<AbstractHttpRequestInterface<?>>( httpRequest );
		this.timeTaken = httpMethod.getTimeTaken();

		SoapUIMetrics metrics = httpMethod.getMetrics();
		method = httpMethod.getMethod();
		version = httpMethod.getProtocolVersion().toString();

		try
		{
			this.url = httpMethod.getURI().toURL();
		}
		catch( Exception e1 )
		{
			SoapUI.logError( e1 );
		}

		if( !httpMethod.isFailed() )
		{
			Settings settings = httpRequest.getSettings();

			try
			{
				rawResponseBody = httpMethod.getResponseBody();
			}
			catch( IOException e )
			{
				e.printStackTrace();
			}

			if( settings.getBoolean( HttpSettings.INCLUDE_RESPONSE_IN_TIME_TAKEN ) )
			{
				timeTaken += httpMethod.getResponseReadTime();
			}

			// metrics.getReadTimer().add( httpMethod.getResponseReadTimeNanos() );
			// metrics.getTotalTimer().add( httpMethod.getResponseReadTimeNanos() );
			metrics.getReadTimer().add( httpMethod.getResponseReadTime() );
			metrics.getTotalTimer().add( httpMethod.getResponseReadTime() );

			try
			{
				this.timestamp = System.currentTimeMillis();
				this.contentType = httpMethod.getResponseContentType();
				this.statusCode = httpMethod.hasHttpResponse() ? httpMethod.getHttpResponse().getStatusLine()
						.getStatusCode() : 0;
				this.sslInfo = httpMethod.getSSLInfo();
				this.url = httpMethod.getURI().toURL();

				metrics.setTimestamp( getTimestamp() );
				metrics.setHttpStatus( getStatusCode() );
			}
			catch( Throwable e )
			{
				e.printStackTrace();
			}
		}

		if( httpRequest instanceof TestRequest )
		{
			TestCase tc = ( ( TestRequest )httpRequest ).getTestStep().getTestCase();
			if( tc instanceof WsdlTestCase && ( ( WsdlTestCase )tc ).isForLoadTest() )
			{
				initHeadersForLoadTest( httpMethod );
				return;
			}
		}

		initHeaders( httpMethod );

		if( this.httpRequest.get() instanceof HttpRequest )
		{
			downloadIncludedResources = ( HttpRequest )this.httpRequest.get() != null ? ( ( HttpRequest )this.httpRequest
					.get() ).getDownloadIncludedResources() : false;

			if( downloadIncludedResources )
			{
				long beforeNanos = System.nanoTime();
				addIncludedContentsAsAttachments();
				long afterNanos = System.nanoTime();
				timeTaken += ( ( afterNanos - beforeNanos ) / 1000000 );
				metrics.getTotalTimer().add( afterNanos - beforeNanos );
				context.setProperty( HTMLPageSourceDownloader.MISSING_RESOURCES_LIST, downloader.getMissingResourcesList() );
			}
		}
	}

	private void addIncludedContentsAsAttachments()
	{
		downloader = new HTMLPageSourceDownloader();
		try
		{
			List<Attachment> attachmentList = downloader.downloadCssAndImages( url.toString(),
					( HttpRequest )httpRequest.get() );
			attachments = attachmentList.toArray( new Attachment[attachmentList.size()] );
		}
		catch( ClassCastException cce )
		{
			attachments = new Attachment[1];
			try
			{
				attachments[0] = downloader.createAttachment( rawResponseData, url, ( HttpRequest )httpRequest.get() );
			}
			catch( IOException e )
			{
				SoapUI.log.error( e );
			}
		}
		catch( Exception e )
		{
			SoapUI.log.error( e );
		}
	}

	protected void initHeaders( ExtendedHttpMethod httpMethod )
	{
		try
		{
			ByteArrayOutputStream rawResponse = new ByteArrayOutputStream();
			ByteArrayOutputStream rawRequest = new ByteArrayOutputStream();

			if( !httpMethod.isFailed() && httpMethod.hasHttpResponse() )
			{
				try
				{
					rawResponse.write( String.valueOf( httpMethod.getHttpResponse().getStatusLine() ).getBytes() );
					rawResponse.write( "\r\n".getBytes() );
				}
				catch( Throwable e )
				{
				}
			}

			rawRequest.write( ( method + " " + String.valueOf( url ) + " " + version + "\r\n" ).getBytes() );

			requestHeaders = new StringToStringsMap();
			Header[] headers = httpMethod.getAllHeaders();

			for( Header header : headers )
			{
				requestHeaders.put( header.getName(), header.getValue() );
				rawRequest.write( toExternalForm( header ).getBytes() );
			}

			responseHeaders = new StringToStringsMap();

			if( !httpMethod.isFailed() && httpMethod.hasHttpResponse() )
			{
				headers = httpMethod.getHttpResponse().getAllHeaders();
				for( Header header : headers )
				{
					responseHeaders.put( header.getName(), header.getValue() );
					rawResponse.write( toExternalForm( header ).getBytes() );
				}

				responseHeaders.put( "#status#", String.valueOf( httpMethod.getHttpResponse().getStatusLine() ) );
			}

			if( httpMethod.getRequestEntity() != null )
			{
				rawRequest.write( "\r\n".getBytes() );
				if( httpMethod.getRequestEntity().isRepeatable() )
				{
					requestContentPos = rawRequest.size();
					MaxSizeByteArrayOutputStream tempOut = new MaxSizeByteArrayOutputStream( SoapUI.getSettings().getLong(
							UISettings.RAW_REQUEST_MESSAGE_SIZE, 0 ) );
					httpMethod.getRequestEntity().writeTo( tempOut );
					tempOut.writeTo( rawRequest );
				}
				else
					rawRequest.write( "<request data not available>".getBytes() );
			}

			if( !httpMethod.isFailed() && httpMethod.hasHttpResponse() && httpMethod.getResponseBody() != null )
			{
				rawResponse.write( "\r\n".getBytes() );
				rawResponse.write( httpMethod.getResponseBody() );
			}

			rawResponseData = rawResponse.toByteArray();
			rawRequestData = rawRequest.toByteArray();
		}
		catch( Throwable e )
		{
			e.printStackTrace();
		}
	}

	public static class MaxSizeByteArrayOutputStream extends ByteArrayOutputStream
	{
		private final long maxSize;

		public MaxSizeByteArrayOutputStream( long maxSize )
		{
			this.maxSize = maxSize;
		}

		@Override
		public synchronized void write( int b )
		{
			if( maxSize > 0 && size() < maxSize )
				super.write( b );
		}

		@Override
		public synchronized void write( byte[] b, int off, int len )
		{
			if( maxSize > 0 && size() < maxSize )
			{
				if( size() + len < maxSize )
					super.write( b, off, len );
				else
					super.write( b, off, ( int )( maxSize - size() ) );
			}
		}

		@Override
		public void write( byte[] b ) throws IOException
		{
			if( maxSize > 0 && size() < maxSize )
			{
				if( size() + b.length < maxSize )
					super.write( b );
				else
					super.write( b, 0, ( int )( maxSize - size() ) );
			}
		}

	}

	protected void initHeadersForLoadTest( ExtendedHttpMethod httpMethod )
	{
		try
		{
			requestHeaders = new StringToStringsMap();
			Header[] headers = httpMethod.getAllHeaders();
			for( Header header : headers )
			{
				requestHeaders.put( header.getName(), header.getValue() );
			}

			if( !httpMethod.isFailed() && httpMethod.hasHttpResponse() )
			{
				responseHeaders = new StringToStringsMap();
				headers = httpMethod.getHttpResponse().getAllHeaders();
				for( Header header : headers )
				{
					responseHeaders.put( header.getName(), header.getValue() );
				}

				responseHeaders.put( "#status#", String.valueOf( httpMethod.getHttpResponse().getStatusLine() ) );
			}
		}
		catch( Throwable e )
		{
			e.printStackTrace();
		}
	}

	public StringToStringsMap getRequestHeaders()
	{
		return requestHeaders;
	}

	public StringToStringsMap getResponseHeaders()
	{
		return responseHeaders;
	}

	public long getTimeTaken()
	{
		return timeTaken;
	}

	public SSLInfo getSSLInfo()
	{
		return sslInfo;
	}

	public long getTimestamp()
	{
		return timestamp;
	}

	public String getContentType()
	{
		return contentType;
	}

	public URL getURL()
	{
		return url;
	}

	public AbstractHttpRequestInterface<?> getRequest()
	{
		return httpRequest.get();
	}

	public int getStatusCode()
	{
		return statusCode;
	}

	public Attachment[] getAttachments()
	{
		return attachments;
	}

	public Attachment[] getAttachmentsForPart( String partName )
	{
		return new Attachment[0];
	}

	public byte[] getRawRequestData()
	{
		return rawRequestData;
	}

	public byte[] getRawResponseData()
	{
		return rawResponseData;
	}

	public byte[] getRawResponseBody()
	{
		return rawResponseBody;
	}

	public String getMethod()
	{
		return method;
	}

	public String getHttpVersion()
	{
		return version;
	}

	public void setProperty( String name, String value )
	{
		if( properties == null )
			properties = new StringToStringMap();

		properties.put( name, value );
	}

	public String getProperty( String name )
	{
		return properties == null ? null : properties.get( name );
	}

	public String[] getPropertyNames()
	{
		return properties == null ? new String[0] : properties.getKeys();
	}

	public String getRequestContent()
	{
		return requestContentPos == -1 || rawRequestData == null ? null : new String( rawRequestData, requestContentPos,
				rawRequestData.length - requestContentPos );
	}

	public String getContentAsXml()
	{
		if( xmlContent == null )
		{
			MediaTypeHandler typeHandler = MediaTypeHandlerRegistry.getTypeHandler( getContentType() );
			xmlContent = ( typeHandler == null ) ? "<xml/>" : typeHandler.createXmlRepresentation( this );
		}
		return xmlContent;
	}

	/**
	 * Returns a {@link String} representation of the header.
	 * 
	 * @return stringHEAD
	 */
	public String toExternalForm( Header header )
	{
		return( ( null == header.getName() ? "" : header.getName() ) + ": "
				+ ( null == header.getValue() ? "" : header.getValue() ) + "\r\n" );
	}

}
