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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URL;

import org.apache.commons.httpclient.Header;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.model.iface.Attachment;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.settings.HttpSettings;
import com.eviware.soapui.support.types.StringToStringMap;

public abstract class BaseHttpResponse implements HttpResponse
{
	private StringToStringMap requestHeaders;
	private StringToStringMap responseHeaders;

	private long timeTaken;
	private long timestamp;
	private String contentType;
	private int statusCode;
	private SSLInfo sslInfo;
	private URL url;
	private WeakReference<AbstractHttpRequest<?>> httpRequest;
	private AbstractHttpRequest.RequestMethod method;
	private String version;
	private StringToStringMap properties;
	private byte[] rawRequestData;
	private byte[] rawResponseData;
	private int requestContentPos = -1;

	public BaseHttpResponse( ExtendedHttpMethod httpMethod, AbstractHttpRequest<?> httpRequest )
	{
		this.httpRequest = new WeakReference<AbstractHttpRequest<?>>( httpRequest );
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
				this.statusCode = httpMethod.getStatusCode();
				this.sslInfo = httpMethod.getSSLInfo();
			}
			catch( Throwable e )
			{
				e.printStackTrace();
			}
		}

		initHeaders( httpMethod );
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

			requestHeaders = new StringToStringMap();
			Header[] headers = httpMethod.getRequestHeaders();
			for( Header header : headers )
			{
				requestHeaders.put( header.getName(), header.getValue() );
				rawRequest.write( header.toExternalForm().getBytes() );
			}

			if( !httpMethod.isFailed() )
			{
				responseHeaders = new StringToStringMap();
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
					httpMethod.getRequestEntity().writeRequest( rawRequest );
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

	public StringToStringMap getRequestHeaders()
	{
		return requestHeaders;
	}

	public StringToStringMap getResponseHeaders()
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

	public AbstractHttpRequest<?> getRequest()
	{
		return httpRequest.get();
	}

	public int getStatusCode()
	{
		return statusCode;
	}

	public Attachment[] getAttachments()
	{
		return new Attachment[0];
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

	public AbstractHttpRequest.RequestMethod getMethod()
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
		return requestContentPos == -1 ? null : new String( rawRequestData, requestContentPos, rawRequestData.length
				- requestContentPos );
	}

}
