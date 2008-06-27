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

package com.eviware.soapui.impl.wsdl.teststeps;

import java.util.Vector;

import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.submit.AbstractWsdlMessageExchange;
import com.eviware.soapui.impl.wsdl.submit.transports.http.WsdlResponse;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.Attachment;
import com.eviware.soapui.support.types.StringToStringMap;

/**
 * WsdlMessageExchange for a WsdlRequest and its response
 *  
 * @author ole.matzura
 */

public class WsdlResponseMessageExchange extends AbstractWsdlMessageExchange
{
	private final WsdlRequest request;
	private WsdlResponse response;
	private String requestContent;

	public WsdlResponseMessageExchange( WsdlRequest request )
	{
		this.request = request;
		response = request.getResponse();
	}
	
   public WsdlRequest getRequest()
   {
      return request;
   }
   
   public WsdlResponse getResponse()
   {
      return response;
   }
   
   public ModelItem getModelItem()
   {
      return request;
   }
   
	public void setResponse( WsdlResponse response )
	{
		this.response = response;
	}

	public String getRequestContent()
	{
		if( requestContent != null )
			return requestContent;
		
		if( response == null )
			response = request.getResponse();
		
		return response == null ? request.getRequestContent() : response.getRequestContent(); 
	}

	public StringToStringMap getRequestHeaders()
	{
		return response == null ? request.getRequestHeaders() : response.getRequestHeaders(); 
	}
	
	public Attachment[] getRequestAttachments()
	{
		return request.getAttachments();
	}

	public Attachment[] getResponseAttachments()
	{
		if( response == null )
			response = request.getResponse();
		
		return response == null ? null : response.getAttachments();
	}

	public String getResponseContent()
	{
		if( response == null )
			response = request.getResponse();
		
		return response == null ? null : response.getContentAsString();
	}

	public StringToStringMap getResponseHeaders()
	{
		if( response == null )
			response = request.getResponse();
		
		return response == null ? null : response.getResponseHeaders();
	}

   public WsdlOperation getOperation()
	{
		return request.getOperation();
	}

	public long getTimeTaken()
	{
		if( response == null )
			response = request.getResponse();
		
		return response == null ? 0 : response.getTimeTaken();
	}

	public long getTimestamp()
	{
		if( response == null )
			response = request.getResponse();
		
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

	public Vector<?> getRequestWssResult()
	{
		return null;
	}

	public Vector<?> getResponseWssResult()
	{
		return response.getWssResult();
	}
}
