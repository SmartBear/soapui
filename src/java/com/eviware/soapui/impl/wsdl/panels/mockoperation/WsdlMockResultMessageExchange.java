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

package com.eviware.soapui.impl.wsdl.panels.mockoperation;

import java.util.Vector;

import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockResponse;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockResult;
import com.eviware.soapui.impl.wsdl.submit.AbstractWsdlMessageExchange;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.Attachment;
import com.eviware.soapui.support.types.StringToStringMap;

/**
 * WsdlMessageExchange for a WsdlMockResult, required for validations
 * 
 * @author ole.matzura
 */

public class WsdlMockResultMessageExchange extends AbstractWsdlMessageExchange<ModelItem>
{
	private final WsdlMockResult mockResult;
	private WsdlMockResponse mockResponse;

	public WsdlMockResultMessageExchange( WsdlMockResult mockResult, WsdlMockResponse mockResponse )
	{
		super( mockResponse );

		this.mockResult = mockResult;
		this.mockResponse = mockResponse;
	}

	public ModelItem getModelItem()
	{
		return mockResponse == null ? mockResult.getMockOperation() : mockResponse;
	}

	public Attachment[] getRequestAttachments()
	{
		return mockResult.getMockRequest().getRequestAttachments();
	}

	public String getRequestContent()
	{
		if( mockResult == null || mockResult.getMockRequest() == null )
			return null;

		return mockResult.getMockRequest().getRequestContent();
	}

	public StringToStringMap getRequestHeaders()
	{
		return mockResult == null ? null : mockResult.getMockRequest().getRequestHeaders();
	}

	public Attachment[] getResponseAttachments()
	{
		return mockResult == null || mockResponse == null ? new Attachment[0] : mockResult.getMockResponse()
				.getAttachments();
	}

	public String getResponseContent()
	{
		return mockResult == null ? null : mockResult.getResponseContent();
	}

	public StringToStringMap getResponseHeaders()
	{
		return mockResult == null ? null : mockResult.getResponseHeaders();
	}

	public WsdlOperation getOperation()
	{
		if( mockResult.getMockOperation() != null )
			return mockResult.getMockOperation().getOperation();

		return mockResponse == null ? null : mockResponse.getMockOperation().getOperation();
	}

	public long getTimeTaken()
	{
		return mockResult == null ? -1 : mockResult.getTimeTaken();
	}

	public long getTimestamp()
	{
		return mockResult == null ? -1 : mockResult.getTimestamp();
	}

	public boolean isDiscarded()
	{
		return mockResponse == null;
	}

	public void discard()
	{
		mockResponse = null;
	}

	public Vector<?> getRequestWssResult()
	{
		return mockResult == null ? null : mockResult.getRequestWssResult();
	}

	public Vector<?> getResponseWssResult()
	{
		return null;
	}

	public int getResponseStatusCode()
	{
		return mockResult.getResponseStatus();
	}

	public String getResponseContentType()
	{
		return mockResult.getResponseContentType();
	}
}
