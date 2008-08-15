/*
 *  soapUI, copyright (C) 2004-2008 eviware.com 
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
import java.lang.ref.WeakReference;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.methods.RequestEntity;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.model.iface.Attachment;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContext;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.settings.HttpSettings;
import com.eviware.soapui.settings.WsdlSettings;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.soapui.support.xml.XmlUtils;

/**
 * Simple response to a request
 * 
 * @author ole.matzura
 */

class SinglePartHttpResponse implements HttpResponse
{
	private final WeakReference<AbstractHttpRequest<?>> wsdlRequest;
	private final ExtendedHttpMethod httpMethod;
	private long timeTaken;
	private String responseContent;
	private StringToStringMap requestHeaders;
	private StringToStringMap responseHeaders;
	private final String requestContent;
	private boolean prettyPrint;
	private SSLInfo sslInfo;
	private long timestamp;
	private long responseSize;
	private byte[] requestData;
	private byte[] responseBody;
	
	public SinglePartHttpResponse(AbstractHttpRequest<?> wsdlRequest, ExtendedHttpMethod httpMethod, String requestContent, PropertyExpansionContext context )
	{
		this.wsdlRequest = new WeakReference<AbstractHttpRequest<?>>(wsdlRequest);
		this.httpMethod = httpMethod;
		this.requestContent = requestContent;
		this.timeTaken = httpMethod.getTimeTaken();
		this.sslInfo = httpMethod.getSSLInfo();
		this.timestamp = System.currentTimeMillis();
		
		// read response immediately since we need to release connection
		Settings settings = wsdlRequest.getSettings();
		
		try
		{
			responseBody = httpMethod.getResponseBody();
			int contentOffset = 0;
			if( responseBody == null )
				responseBody = new byte[0];
			
			responseSize = responseBody.length;
			if (settings.getBoolean(HttpSettings.INCLUDE_RESPONSE_IN_TIME_TAKEN))
				timeTaken += httpMethod.getResponseReadTime();
			
			String contentType = httpMethod.getResponseContentType();
			String charset = httpMethod.getResponseCharSet();
			
			if( contentType != null && contentType.toLowerCase().endsWith("xml"))
			{
				if( responseSize > 3 && responseBody[0] == (byte)239 && responseBody[1] == (byte)187 && responseBody[2] == (byte)191 )
				{
					charset = "UTF-8";
					contentOffset = 3;
				}
			}
			
			if( charset == null )
				charset = wsdlRequest.getEncoding();
			
			charset = StringUtils.unquote( charset );
				
			responseContent = responseBody.length == 0 ? null : charset == null ? new String(responseBody) : 
				new String(	responseBody, contentOffset, (int)(responseSize-contentOffset), charset );
			
			prettyPrint = wsdlRequest.getSettings().getBoolean( WsdlSettings.PRETTY_PRINT_RESPONSE_MESSAGES );
			
			initHeaders(httpMethod);
			
			RequestEntity requestEntity = httpMethod.getRequestEntity();
			if( requestEntity != null )
			{
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				requestEntity.writeRequest( out );
				requestData = out.toByteArray();
			}
			else
			{
				requestData = new byte[0];
			}
		}
		catch( Exception e )
		{
			SoapUI.logError( e );
		}
	}

	private void initHeaders(ExtendedHttpMethod postMethod)
	{
		requestHeaders = new StringToStringMap();
		Header[] headers = postMethod.getRequestHeaders();
		for( Header header : headers )
		{
			requestHeaders.put( header.getName(), header.getValue() );
		}
		
		responseHeaders = new StringToStringMap();
		headers = postMethod.getResponseHeaders();
		for( Header header : headers )
		{
			responseHeaders.put( header.getName(), header.getValue() );
		}
		
		responseHeaders.put( "#status#", postMethod.getStatusLine().toString() );
	}

	public String getContentAsString()
	{
		if( prettyPrint )
		{
			responseContent = XmlUtils.prettyPrintXml( responseContent );
			prettyPrint = false;
		}
		
		return responseContent;
	}

	public long getContentLength()
	{
		return responseSize;
	}

	public AbstractHttpRequest<?> getRequest()
	{
		return wsdlRequest.get();
	}

	public long getTimeTaken()
	{
		return timeTaken;
	}

	public Attachment[] getAttachments()
	{
		return new Attachment[0];
	}

	public StringToStringMap getRequestHeaders()
	{
		return requestHeaders;
	}

	public StringToStringMap getResponseHeaders()
	{
		return responseHeaders;
	}

	public Attachment[] getAttachmentsForPart(String partName)
	{
		return new Attachment[0];
	}

	public String getRequestContent()
	{
		return requestContent;
	}

	public void setResponseContent(String responseContent)
	{
		String oldContent = this.responseContent;
		this.responseContent = responseContent;
		
		getRequest().notifyPropertyChanged( WsdlRequest.RESPONSE_CONTENT_PROPERTY, oldContent, responseContent );
	}

	public SSLInfo getSSLInfo()
	{
		return sslInfo;
	}

	public long getTimestamp()
	{
		return timestamp;
	}

	public byte[] getRawRequestData()
	{
		return requestData;
	}

	public byte[] getRawResponseData()
	{
		return responseBody;
	}

	public String getContentType()
	{
		return httpMethod.getResponseContentType();
	}
}