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
import org.apache.commons.httpclient.HeaderElement;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.RequestEntity;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.model.iface.Attachment;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContext;
import com.eviware.soapui.settings.HttpSettings;
import com.eviware.soapui.support.types.StringToStringMap;

/**
 * WsdlMockResponse for a MimeResponse
 * 
 * @author ole.matzura
 */

public class MimeMessageResponse implements HttpResponse
{
	private final WeakReference<AbstractHttpRequest<?>> httpRequest;
	private long timeTaken;
	private long responseContentLength;
	private StringToStringMap requestHeaders;
	private StringToStringMap responseHeaders;
	private final String requestContent;
	private SSLInfo sslInfo;
	private MultipartMessageSupport mmSupport;
	private long timestamp;
	private PostResponseDataSource postResponseDataSource;
	private byte[] requestData;
	private String contentType;

	public MimeMessageResponse(AbstractHttpRequest<?> httpRequest, final ExtendedHttpMethod httpMethod, String requestContent, PropertyExpansionContext context)
	{
		this.httpRequest = new WeakReference<AbstractHttpRequest<?>>( httpRequest );
		this.requestContent = requestContent;
		this.timeTaken = httpMethod.getTimeTaken();
		this.timestamp = System.currentTimeMillis();
		this.contentType = httpMethod.getResponseContentType();
		
		try
		{
			initHeaders( httpMethod );
			sslInfo = httpMethod.getSSLInfo();
			postResponseDataSource = new PostResponseDataSource( httpMethod );
			responseContentLength = postResponseDataSource.getDataSize();
			
			Header h = httpMethod.getResponseHeader( "Content-Type" );
			HeaderElement[] elements = h.getElements();
			
			String rootPartId = null;
			
			for( HeaderElement element : elements )
			{
				String name = element.getName().toUpperCase();
				if( name.startsWith( "MULTIPART/" ))
				{
					NameValuePair parameter = element.getParameterByName("start");
					if (parameter != null)
						rootPartId = parameter.getValue();
				}
			}
			
			mmSupport = new MultipartMessageSupport( postResponseDataSource, rootPartId, httpRequest.getOperation(), false,
					httpRequest.isPrettyPrint());
			
			if (httpRequest.getSettings().getBoolean(HttpSettings.INCLUDE_RESPONSE_IN_TIME_TAKEN))
				this.timeTaken += httpMethod.getResponseReadTime();
			
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
		catch ( Exception e)
		{
			SoapUI.logError( e );
		}
	}

	protected MultipartMessageSupport getMmSupport()
	{
		return mmSupport;
	}

	public SSLInfo getSSLInfo()
	{
		return sslInfo;
	}

	public long getContentLength()
	{
		return responseContentLength;
	}

	public AbstractHttpRequest<?> getRequest()
	{
		return httpRequest.get();
	}

	public long getTimeTaken()
	{
		return timeTaken;
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
	
	public StringToStringMap getRequestHeaders()
	{
		return requestHeaders;
	}

	public StringToStringMap getResponseHeaders()
	{
		return responseHeaders;
	}
	
	public String getRequestContent()
	{
		return requestContent;
	}

	public void setResponseContent( String responseContent )
	{
		String oldContent = getContentAsString();
		mmSupport.setResponseContent( responseContent );
		
		getRequest().notifyPropertyChanged( WsdlRequest.RESPONSE_CONTENT_PROPERTY, oldContent, responseContent );
	}

	public Attachment[] getAttachments()
	{
		return mmSupport.getAttachments();
	}

	public Attachment[] getAttachmentsForPart( String partName )
	{
		return mmSupport.getAttachmentsForPart( partName );
	}

	public String getContentAsString()
	{
		return mmSupport.getContentAsString();
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
		return postResponseDataSource.getData();
	}

	public String getContentType()
	{
		return contentType;
	}
}