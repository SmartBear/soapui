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
import javax.servlet.http.HttpServletRequest;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.support.Tools;

/**
 * DataSource for a MockRequest
 * 
 * @author ole.matzura
 */

public class MockRequestDataSource implements DataSource
{
	private byte[] data;
	private String contentType;
	private String name;

	public MockRequestDataSource( HttpServletRequest request )
	{
		try
		{
			data = Tools.readAll( request.getInputStream(), 0 ).toByteArray();
			contentType = request.getContentType();
			name = "Request for " + request.getPathInfo();
		}
		catch( Exception e )
		{
			SoapUI.logError( e );
		}
	}

	public String getContentType()
	{
		return contentType;
	}

	public InputStream getInputStream() throws IOException
	{
		return new ByteArrayInputStream( data );
	}

	public String getName()
	{
		return name;
	}

	public OutputStream getOutputStream() throws IOException
	{
		return null;
	}

	public byte[] getData()
	{
		return data;
	}
}
