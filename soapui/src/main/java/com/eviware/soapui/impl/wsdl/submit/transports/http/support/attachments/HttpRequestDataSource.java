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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataSource;

import com.eviware.soapui.impl.support.http.HttpRequestInterface;

/**
 * DataSource for an existing WsdlRequest
 * 
 * @author ole.matzura
 */

public class HttpRequestDataSource implements DataSource
{
	private final HttpRequestInterface<?> restRequest;
	private final String requestContent;

	public HttpRequestDataSource( HttpRequestInterface<?> restRequest, String requestContent )
	{
		this.restRequest = restRequest;
		this.requestContent = requestContent;
	}

	public String getContentType()
	{
		return restRequest.getMediaType();
	}

	public InputStream getInputStream() throws IOException
	{
		byte[] bytes = requestContent.getBytes( "UTF-8" );
		return new ByteArrayInputStream( bytes );
	}

	public String getName()
	{
		return restRequest.getName();
	}

	public OutputStream getOutputStream() throws IOException
	{
		return null;
	}
}
