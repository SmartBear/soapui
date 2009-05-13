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

package com.eviware.soapui.impl.wsdl.submit.transports.http.support.attachments;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HeaderElement;
import org.apache.commons.httpclient.NameValuePair;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.submit.transports.http.BaseHttpResponse;
import com.eviware.soapui.impl.wsdl.submit.transports.http.ExtendedHttpMethod;
import com.eviware.soapui.model.iface.Attachment;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContext;
import com.eviware.soapui.settings.HttpSettings;

/**
 * WsdlMockResponse for a MimeResponse
 * 
 * @author ole.matzura
 */

public class MimeMessageResponse extends BaseHttpResponse
{
	private long timeTaken;
	private long responseContentLength;
	private final String requestContent;
	private MultipartMessageSupport mmSupport;
	private PostResponseDataSource postResponseDataSource;

	public MimeMessageResponse( AbstractHttpRequest<?> httpRequest, ExtendedHttpMethod httpMethod,
			String requestContent, PropertyExpansionContext context )
	{
		super( httpMethod, httpRequest );

		this.requestContent = requestContent;

		try
		{
			postResponseDataSource = new PostResponseDataSource( httpMethod );
			responseContentLength = postResponseDataSource.getDataSize();

			Header h = httpMethod.getResponseHeader( "Content-Type" );
			HeaderElement[] elements = h.getElements();

			String rootPartId = null;

			for( HeaderElement element : elements )
			{
				String name = element.getName().toUpperCase();
				if( name.startsWith( "MULTIPART/" ) )
				{
					NameValuePair parameter = element.getParameterByName( "start" );
					if( parameter != null )
						rootPartId = parameter.getValue();
				}
			}

			mmSupport = new MultipartMessageSupport( postResponseDataSource, rootPartId, httpRequest.getOperation(),
					false, httpRequest.isPrettyPrint() );

			if( httpRequest.getSettings().getBoolean( HttpSettings.INCLUDE_RESPONSE_IN_TIME_TAKEN ) )
				this.timeTaken += httpMethod.getResponseReadTime();
		}
		catch( Exception e )
		{
			SoapUI.logError( e );
		}
	}

	protected MultipartMessageSupport getMmSupport()
	{
		return mmSupport;
	}

	public long getContentLength()
	{
		return responseContentLength;
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

	// public byte[] getRawRequestData()
	// {
	// return requestData;
	// }
	//
	// public byte[] getRawResponseData()
	// {
	// return postResponseDataSource.getData();
	// }
}