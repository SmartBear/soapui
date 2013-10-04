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

package com.eviware.soapui.impl.wsdl.panels.mockoperation;

import java.util.Vector;

import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockOperation;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockRequest;
import com.eviware.soapui.impl.wsdl.submit.AbstractWsdlMessageExchange;
import com.eviware.soapui.model.iface.Attachment;
import com.eviware.soapui.model.iface.Response;
import com.eviware.soapui.support.types.StringToStringsMap;

public class WsdlMockRequestMessageExchange extends AbstractWsdlMessageExchange<WsdlMockOperation>
{
	private final WsdlMockRequest request;

	public WsdlMockRequestMessageExchange( WsdlMockRequest request, WsdlMockOperation mockOperation )
	{
		super( mockOperation );
		this.request = request;
	}

	public String getEndpoint()
	{
		return request.getHttpRequest().getRequestURI();
	}

	@Override
	public Response getResponse()
	{
		return null;
	}

	@Override
	public WsdlOperation getOperation()
	{
		return getModelItem().getOperation();
	}

	public Vector<?> getRequestWssResult()
	{
		return null;
	}

	public Vector<?> getResponseWssResult()
	{
		return null;
	}

	public Attachment[] getRequestAttachments()
	{
		return request.getRequestAttachments();
	}

	public String getRequestContent()
	{
		return request.getRequestContent();
	}

	public StringToStringsMap getRequestHeaders()
	{
		return request.getRequestHeaders();
	}

	public Attachment[] getResponseAttachments()
	{
		return null;
	}

	public String getResponseContent()
	{
		return null;
	}

	public StringToStringsMap getResponseHeaders()
	{
		return new StringToStringsMap();
	}

	public long getTimeTaken()
	{
		return 0;
	}

	public long getTimestamp()
	{
		return 0;
	}

	public boolean isDiscarded()
	{
		return false;
	}

	public int getResponseStatusCode()
	{
		return 0;
	}

	public String getResponseContentType()
	{
		return null;
	}
}
