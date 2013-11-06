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

import java.io.UnsupportedEncodingException;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.impl.support.AbstractHttpRequestInterface;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContext;
import com.eviware.soapui.settings.WsdlSettings;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.xml.XmlUtils;

/**
 * Simple response to a request
 * 
 * @author ole.matzura
 */

public class SinglePartHttpResponse extends BaseHttpResponse
{
	private String responseContent;
	private String requestContent;
	private boolean prettyPrint;
	private long responseSize;

	public SinglePartHttpResponse( AbstractHttpRequestInterface<?> httpRequest, ExtendedHttpMethod httpMethod,
			String requestContent, PropertyExpansionContext context )
	{
		super( httpMethod, httpRequest, context );

		if( getRequestContent() == null || !getRequestContent().equals( requestContent ) )
			this.requestContent = requestContent;

		try
		{
			byte[] responseBody = httpMethod.getResponseBody();
			int contentOffset = 0;
			if( responseBody == null )
				responseBody = new byte[0];

			responseSize = responseBody.length;

			String contentType = httpMethod.getResponseContentType();
			String charset = httpMethod.getResponseCharSet();

			if( contentType != null && contentType.toLowerCase().endsWith( "xml" ) )
			{
				if( responseSize > 3 && responseBody[0] == ( byte )239 && responseBody[1] == ( byte )187
						&& responseBody[2] == ( byte )191 )
				{
					charset = "UTF-8";
					contentOffset = 3;
				}
			}

			if( charset == null )
				charset = httpRequest.getEncoding();

			charset = StringUtils.unquote( charset );

			try
			{
				responseContent = responseBody.length == 0 ? null : charset == null ? new String( responseBody,
						contentOffset, ( int )( responseSize - contentOffset ) ) : new String( responseBody, contentOffset,
						( int )( responseSize - contentOffset ), charset );
			}
			catch( UnsupportedEncodingException e )
			{
				SoapUI.getErrorLog().warn( e.toString() );
				responseContent = new String( responseBody, contentOffset, ( int )( responseSize - contentOffset ) );
			}

			prettyPrint = httpRequest.getSettings().getBoolean( WsdlSettings.PRETTY_PRINT_RESPONSE_MESSAGES );
		}
		catch( Exception e )
		{
			SoapUI.logError( e );
		}
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

	protected String getResponseContent()
	{
		return responseContent;
	}

	public long getContentLength()
	{
		return responseSize;
	}

	public String getRequestContent()
	{
		return requestContent == null ? super.getRequestContent() : requestContent;
	}

	public void setResponseContent( String responseContent )
	{
		String oldContent = this.responseContent;
		this.responseContent = responseContent;

		( ( AbstractHttpRequest<?> )getRequest() ).notifyPropertyChanged( WsdlRequest.RESPONSE_CONTENT_PROPERTY,
				oldContent, responseContent );
	}

	// public byte[] getRawRequestData()
	// {
	// return requestData;
	// }
	//
	// public byte[] getRawResponseData()
	// {
	// return responseBody;
	// }

}
