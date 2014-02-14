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
import com.eviware.soapui.impl.wsdl.mock.WsdlMockRequest;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockResponse;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockResult;
import com.eviware.soapui.impl.wsdl.submit.AbstractWsdlMessageExchange;
import com.eviware.soapui.model.iface.Attachment;
import com.eviware.soapui.model.iface.Response;
import com.eviware.soapui.support.types.StringToStringsMap;

/**
 * WsdlMessageExchange for a WsdlMockResponse, required for validations
 * 
 * @author ole.matzura
 */

public class WsdlMockResponseMessageExchange extends AbstractWsdlMessageExchange<WsdlMockResponse>
{
	public WsdlMockResponseMessageExchange( WsdlMockResponse mockResponse )
	{
		super( mockResponse );
	}

	public Attachment[] getRequestAttachments()
	{
		return null;
	}

	@Override
	public Response getResponse()
	{
		return null;
	}

	public String getEndpoint()
	{
		return getWsdlMockResult().getMockRequest().getHttpRequest().getRequestURI();
	}

	public String getRequestContent()
	{
		WsdlMockResult mockResult = getWsdlMockResult();
		WsdlMockRequest mockRequest = mockResult.getMockRequest();
		return mockRequest.getRequestContent();
	}

	public StringToStringsMap getRequestHeaders()
	{
		return null;
	}

	public Attachment[] getResponseAttachments()
	{
		return getModelItem().getAttachments();
	}

	public String getResponseContent()
	{
		return getModelItem().getResponseContent();
	}

	public StringToStringsMap getResponseHeaders()
	{
		return getModelItem().getResponseHeaders();
	}

	@Override
	public WsdlOperation getOperation()
	{
		return getModelItem().getMockOperation().getOperation();
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

	public Vector<?> getRequestWssResult()
	{
		return getWsdlMockResult().getRequestWssResult();
	}

	public Vector<?> getResponseWssResult()
	{
		return null;
	}

	public int getResponseStatusCode()
	{
		return getModelItem().getResponseHttpStatus();
	}

	public String getResponseContentType()
	{
		return getWsdlMockResult().getResponseContentType();
	}

	@Override
	public boolean hasRawData()
	{
		return true;
	}

	@Override
	public byte[] getRawResponseData()
	{
		return getWsdlMockResult().getRawResponseData();
	}

	public byte[] getRawRequestData()
	{
		return getModelItem().getMockResult().getMockRequest().getRawRequestData();
	}

	public WsdlMockResult getWsdlMockResult()
	{
		return (WsdlMockResult)getModelItem().getMockResult();
	}

}
