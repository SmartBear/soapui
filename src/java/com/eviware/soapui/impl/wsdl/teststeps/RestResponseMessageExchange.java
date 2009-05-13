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

import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.impl.wsdl.submit.AbstractRestMessageExchange;
import com.eviware.soapui.impl.wsdl.submit.transports.http.HttpResponse;
import com.eviware.soapui.model.iface.Attachment;
import com.eviware.soapui.model.iface.Operation;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.soapui.support.xml.XmlUtils;

public class RestResponseMessageExchange extends AbstractRestMessageExchange<RestRequest>
{
	private HttpResponse response;
	private String requestContent;

	public RestResponseMessageExchange( RestRequest request )
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

	public String getRequestContentAsXml()
	{
		String result = getRequestContent();
		return XmlUtils.seemsToBeXml( result ) ? result : "<not-xml/>";
	}

	public void setResponse( HttpResponse response )
	{
		this.response = response;
	}

	public String getResponseContentAsXml()
	{
		if( response == null )
			response = getModelItem().getResponse();

		return response == null ? null : response.getProperty( RestRequest.REST_XML_RESPONSE );
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

	public void setRequestContent( String requestContent )
	{
		this.requestContent = requestContent;
	}

	public boolean isDiscarded()
	{
		return false;
	}

	public RestResource getResource()
	{
		return getModelItem().getResource();
	}

	public RestRequest getRestRequest()
	{
		return getModelItem();
	}

	public Operation getOperation()
	{
		return getResource();
	}

	public int getResponseStatusCode()
	{
		return response == null ? 0 : response.getStatusCode();
	}

	public String getResponseContentType()
	{
		return response == null ? null : response.getContentType();
	}
}
