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

package com.eviware.soapui.impl.wsdl.panels.mockoperation;

import java.util.Vector;

import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockRequest;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockResponse;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockResult;
import com.eviware.soapui.impl.wsdl.submit.AbstractWsdlMessageExchange;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.Attachment;
import com.eviware.soapui.support.types.StringToStringMap;

/**
 * WsdlMessageExchange for a WsdlMockResponse, required for validations
 * 
 * @author ole.matzura
 */

public class WsdlMockResponseMessageExchange extends AbstractWsdlMessageExchange
{
	private final WsdlMockResponse mockResponse;

	public WsdlMockResponseMessageExchange( WsdlMockResponse mockResponse )
	{
		this.mockResponse = mockResponse;
	}
	
	public Attachment[] getRequestAttachments()
	{
		return null;
	}

	public ModelItem getModelItem()
	{
	   return mockResponse;
	}
	
	public String getRequestContent()
	{
		WsdlMockResult mockResult = mockResponse.getMockResult();
      WsdlMockRequest mockRequest = mockResult.getMockRequest();
      return mockRequest.getRequestContent();
	}

	public StringToStringMap getRequestHeaders()
	{
		return null;
	}

	public Attachment[] getResponseAttachments()
	{
		return mockResponse.getAttachments();
	}

	public String getResponseContent()
	{
		return mockResponse.getResponseContent();
	}

	public StringToStringMap getResponseHeaders()
	{
		return mockResponse.getResponseHeaders();
	}

	@Override
   public WsdlOperation getOperation()
	{
		return mockResponse.getMockOperation().getOperation();
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
		return mockResponse.getMockResult().getRequestWssResult();
	}

	public Vector<?> getResponseWssResult()
	{
		return null;
	}
}
