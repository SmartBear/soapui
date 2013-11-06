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

package com.eviware.soapui.impl.wsdl.submit.transports.http.support.attachments;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.NameValuePair;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.support.AbstractHttpOperation;
import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.impl.support.AbstractHttpRequestInterface;
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
	private String requestContent;
	private MultipartMessageSupport mmSupport;
	private PostResponseDataSource postResponseDataSource;

	public MimeMessageResponse( AbstractHttpRequestInterface<?> httpRequest, ExtendedHttpMethod httpMethod,
			String requestContent, PropertyExpansionContext context )
	{
		super( httpMethod, httpRequest, context );

		if( getRequestContent() == null || !getRequestContent().equals( requestContent ) )
			this.requestContent = requestContent;

		try
		{
			postResponseDataSource = new PostResponseDataSource( httpMethod );
			responseContentLength = postResponseDataSource.getDataSize();

			Header h = null;
			if( httpMethod.hasHttpResponse() && httpMethod.getHttpResponse().getEntity() != null )
			{
				h = httpMethod.getHttpResponse().getEntity().getContentType();
			}

			if( h != null )
			{
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

				mmSupport = new MultipartMessageSupport( postResponseDataSource, rootPartId,
						( AbstractHttpOperation )httpRequest.getOperation(), false, httpRequest.isPrettyPrint() );

				if( httpRequest.getSettings().getBoolean( HttpSettings.INCLUDE_RESPONSE_IN_TIME_TAKEN ) )
					this.timeTaken += httpMethod.getResponseReadTime();
			}
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
		return requestContent == null ? super.getRequestContent() : requestContent;
	}

	public void setResponseContent( String responseContent )
	{
		String oldContent = getContentAsString();
		mmSupport.setResponseContent( responseContent );

		( ( AbstractHttpRequest<?> )getRequest() ).notifyPropertyChanged( WsdlRequest.RESPONSE_CONTENT_PROPERTY,
				oldContent, responseContent );
	}

	public Attachment[] getAttachments()
	{
		int lengthA = super.getAttachments().length;
		int lengthB = mmSupport.getAttachments().length;
		if( lengthA > 0 )
		{
			Attachment[] all = new Attachment[lengthA + lengthB];
			System.arraycopy( super.getAttachments(), 0, all, 0, lengthA );
			System.arraycopy( mmSupport.getAttachments(), 0, all, lengthA, lengthB );
			return all;
		}
		else
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
