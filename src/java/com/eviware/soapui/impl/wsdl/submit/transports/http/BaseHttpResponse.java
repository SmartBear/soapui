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

package com.eviware.soapui.impl.wsdl.submit.transports.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.Date;
import java.util.List;

import org.apache.commons.httpclient.Header;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.rest.RestRequestInterface;
import com.eviware.soapui.impl.rest.support.MediaTypeHandler;
import com.eviware.soapui.impl.rest.support.MediaTypeHandlerRegistry;
import com.eviware.soapui.impl.support.AbstractHttpRequestInterface;
import com.eviware.soapui.impl.support.http.HttpRequest;
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
	private int statusCode;
	private SSLInfo sslInfo;
	private URL url;
	private WeakReference<AbstractHttpRequestInterface<?>> httpRequest;
	private RestRequestInterface.RequestMethod method;
	private String version;
	private StringToStringMap properties;
	private byte[] rawRequestData;
	private byte[] rawResponseData;
	private int requestContentPos = -1;
	private String xmlContent;
	private boolean downloadIncludedResources;
	private Attachment[] attachments = new Attachment[0];
	protected HTMLPageSourceDownloader downloader;

	public BaseHttpResponse( ExtendedHttpMethod httpMethod, AbstractHttpRequestInterface<?> httpRequest,
			PropertyExpansionContext context )
	{
		this.httpRequest = new WeakReference<AbstractHttpRequestInterface<?>>( httpRequest );
		this.timeTaken = httpMethod.getTimeTaken();

		method = httpMethod.getMethod();
		version = httpMethod.getParams().getVersion().toString();
		try
		{
			this.url = new URL( httpMethod.getURI().toString() );
		}
		catch( Exception e1 )
		{
			SoapUI.logError( e1 );
		}

		if( !httpMethod.isFailed() )
		{
			Settings settings = httpRequest.getSettings();
			if( settings.getBoolean( HttpSettings.INCLUDE_RESPONSE_IN_TIME_TAKEN ) )
			{
				try
				{
					httpMethod.getResponseBody();
				}
				catch( IOException e )
				{
					e.printStackTrace();
				}
				timeTaken += httpMethod.getResponseReadTime();
			}

			try
			{
				this.timestamp = System.currentTimeMillis();
				this.contentType = httpMethod.getResponseContentType();

				if( httpMethod.hasResponse() )
				{
					this.statusCode = httpMethod.getStatusCode();
					this.sslInfo = httpMethod.getSSLInfo();
				}

				this.url = new URL( httpMethod.getURI().toString() );
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
			downloadIncludedResources = ( ( HttpRequest )this.httpRequest.get() ).getDownloadIncludedResources();
			if( downloadIncludedResources )
			{
				long before = ( new Date() ).getTime();
				addIncludedContentsAsAttachments();
				long after = ( new Date() ).getTime();
				timeTaken += ( after - before );
				context.setProperty( HTMLPageSourceDownloader.MISSING_RESOURCES_LIST, downloader.getMissingResourcesList() );
			}
		}
	}

	private void addIncludedContentsAsAttachments()
	{
		downloader = new HTMLPageSourceDownloader();
		try
		{
			List<Attachment> attachmentList = downloader.downloadCssAndImages( url.toString(), ( HttpRequest )httpRequest
					.get() );
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

			if( !httpMethod.isFailed() )
			{
				rawResponse.write( String.valueOf( httpMethod.getStatusLine() ).getBytes() );
				rawResponse.write( "\r\n".getBytes() );
			}

			rawRequest.write( ( method + " " + String.valueOf( url ) + " " + version + "\r\n" ).getBytes() );

			requestHeaders = new StringToStringsMap();
			Header[] headers = httpMethod.getRequestHeaders();
			for( Header header : headers )
			{
				requestHeaders.put( header.getName(), header.getValue() );
				rawRequest.write( header.toExternalForm().getBytes() );
			}

			responseHeaders = new StringToStringsMap();

			if( !httpMethod.isFailed() )
			{
				headers = httpMethod.getResponseHeaders();
				for( Header header : headers )
				{
					responseHeaders.put( header.getName(), header.getValue() );
					rawResponse.write( header.toExternalForm().getBytes() );
				}

				responseHeaders.put( "#status#", String.valueOf( httpMethod.getStatusLine() ) );
			}

			if( httpMethod.getRequestEntity() != null )
			{
				rawRequest.write( "\r\n".getBytes() );
				if( httpMethod.getRequestEntity().isRepeatable() )
				{
					requestContentPos = rawRequest.size();
					MaxSizeByteArrayOutputStream tempOut = new MaxSizeByteArrayOutputStream( SoapUI.getSettings().getLong(
							UISettings.RAW_REQUEST_MESSAGE_SIZE, 0 ) );
					httpMethod.getRequestEntity().writeRequest( tempOut );
					tempOut.writeTo( rawRequest );
				}
				else
					rawResponse.write( "<request data not available>".getBytes() );
			}

			if( !httpMethod.isFailed() )
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
			Header[] headers = httpMethod.getRequestHeaders();
			for( Header header : headers )
			{
				requestHeaders.put( header.getName(), header.getValue() );
			}

			if( !httpMethod.isFailed() )
			{
				responseHeaders = new StringToStringsMap();
				headers = httpMethod.getResponseHeaders();
				for( Header header : headers )
				{
					responseHeaders.put( header.getName(), header.getValue() );
				}

				responseHeaders.put( "#status#", String.valueOf( httpMethod.getStatusLine() ) );
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

	public RestRequestInterface.RequestMethod getMethod()
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
}
