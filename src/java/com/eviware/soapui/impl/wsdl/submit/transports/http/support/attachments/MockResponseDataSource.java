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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataSource;

import com.eviware.soapui.impl.wsdl.mock.WsdlMockResponse;
import com.eviware.soapui.impl.wsdl.support.soap.SoapVersion;

/**
 * DataSource for an existing WsdlMockResponse
 * 
 * @author ole.matzura
 */

public class MockResponseDataSource implements DataSource
{
	private final String responseContent;
	private final boolean isXOP;
	private final WsdlMockResponse mockResponse;

	public MockResponseDataSource( WsdlMockResponse mockResponse, String responseContent, boolean isXOP )
	{
		this.mockResponse = mockResponse;
		this.responseContent = responseContent;
		this.isXOP = isXOP;
	}

	public String getContentType()
	{
		SoapVersion soapVersion = mockResponse.getSoapVersion();

		if( isXOP )
		{
			return AttachmentUtils.buildRootPartContentType( mockResponse.getMockOperation().getOperation().getName(),
					soapVersion );
		}
		else
			return soapVersion.getContentType() + "; charset=UTF-8";
	}

	public InputStream getInputStream() throws IOException
	{
		byte[] bytes = responseContent.getBytes( "UTF-8" );
		return new ByteArrayInputStream( bytes );
	}

	public String getName()
	{
		return mockResponse.getName();
	}

	public OutputStream getOutputStream() throws IOException
	{
		return null;
	}
}
