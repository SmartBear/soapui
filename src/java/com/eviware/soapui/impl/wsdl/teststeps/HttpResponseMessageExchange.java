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

package com.eviware.soapui.impl.wsdl.teststeps;

import java.util.ArrayList;
import java.util.List;

import com.eviware.soapui.impl.support.http.HttpRequestInterface;
import com.eviware.soapui.impl.wsdl.submit.AbstractMessageExchange;
import com.eviware.soapui.impl.wsdl.submit.transports.http.HttpResponse;
import com.eviware.soapui.model.iface.Attachment;
import com.eviware.soapui.model.iface.Operation;
import com.eviware.soapui.support.types.StringToStringMap;

public class HttpResponseMessageExchange extends AbstractMessageExchange<HttpRequestInterface<?>>
{
	private HttpResponse response;
	private String requestContent;

	public HttpResponseMessageExchange( HttpRequestInterface<?> request )
	{
		super( request );

		response = request.getResponse();
		if( response != null )
		{
			for( String key : response.getPropertyNames() )
			{
				addProperty( key, response.getProperty( key ) );
			}
		}
	}

	public String getRequestContent()
	{
		if( requestContent != null )
			return requestContent;

		if( response == null )
			response = getModelItem().getResponse();

		return response == null ? getModelItem().getRequestContent() : response.getRequestContent();
	}

	@Override
	public String getResponseContentAsXml()
	{
		if( response == null )
			response = getModelItem().getResponse();

		return response.getContentAsXml();
	}

	public StringToStringMap getRequestHeaders()
	{
		return response == null ? getModelItem().getRequestHeaders() : response.getRequestHeaders();
	}

	public Attachment[] getRequestAttachments()
	{
		return getModelItem().getAttachments();
	}

	public Attachment[] getResponseAttachments()
	{
		if( response == null )
			response = getModelItem().getResponse();

		return response == null ? null : response.getAttachments();
	}

	public String getResponseContent()
	{
		if( response == null )
			response = getModelItem().getResponse();

		return response == null ? null : response.getContentAsString();
	}

	public HttpResponse getResponse()
	{
		return response;
	}

	public void setResponse( HttpResponse response )
	{
		this.response = response;
	}

	public StringToStringMap getResponseHeaders()
	{
		if( response == null )
			response = getModelItem().getResponse();

		return response == null ? null : response.getResponseHeaders();
	}

	public long getTimeTaken()
	{
		if( response == null )
			response = getModelItem().getResponse();

		return response == null ? 0 : response.getTimeTaken();
	}

	public long getTimestamp()
	{
		if( response == null )
			response = getModelItem().getResponse();

		return response == null ? 0 : response.getTimestamp();
	}

	public boolean isDiscarded()
	{
		return false;
	}

	public Operation getOperation()
	{
		return null;
	}

	public int getResponseStatusCode()
	{
		return response == null ? 0 : response.getStatusCode();
	}

	public String getResponseContentType()
	{
		return response == null ? null : response.getContentType();
	}

	public boolean hasRawData()
	{
		return false;
	}

	public byte[] getRawRequestData()
	{
		return null;
	}

	public byte[] getRawResponseData()
	{
		return null;
	}

	public Attachment[] getResponseAttachmentsForPart( String name )
	{
		List<Attachment> result = new ArrayList<Attachment>();

		for( Attachment attachment : getResponseAttachments() )
		{
			if( attachment.getPart().equals( name ) )
				result.add( attachment );
		}

		return result.toArray( new Attachment[result.size()] );
	}

	public Attachment[] getRequestAttachmentsForPart( String name )
	{
		List<Attachment> result = new ArrayList<Attachment>();

		for( Attachment attachment : getRequestAttachments() )
		{
			if( attachment.getPart().equals( name ) )
				result.add( attachment );
		}

		return result.toArray( new Attachment[result.size()] );
	}

	public boolean hasRequest( boolean ignoreEmpty )
	{
		String requestContent = getRequestContent();
		return !( requestContent == null || ( ignoreEmpty && requestContent.trim().length() == 0 ) );
	}

	public boolean hasResponse()
	{
		String responseContent = getResponseContent();
		return responseContent != null && responseContent.trim().length() > 0;
	}
}
