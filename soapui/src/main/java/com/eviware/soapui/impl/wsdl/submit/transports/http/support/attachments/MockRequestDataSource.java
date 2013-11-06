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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataSource;
import javax.servlet.http.HttpServletRequest;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.monitor.CaptureInputStream;
import com.eviware.soapui.settings.UISettings;

/**
 * DataSource for a MockRequest
 * 
 * @author ole.matzura
 */

public class MockRequestDataSource implements DataSource
{
	private String contentType;
	private String name;
	private final HttpServletRequest request;
	private CaptureInputStream capture = null;

	public MockRequestDataSource( HttpServletRequest request )
	{
		this.request = request;
		try
		{
			contentType = request.getContentType();
			name = "Request for " + request.getPathInfo();
			capture = new CaptureInputStream( request.getInputStream(), SoapUI.getSettings().getLong(
					UISettings.RAW_REQUEST_MESSAGE_SIZE, 0 ) );
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
		return request.getInputStream();
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
		return capture.getCapturedData();
	}
}
