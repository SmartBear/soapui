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

package com.eviware.soapui.impl.wsdl.monitor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataSource;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.support.Tools;

/**
 * DataSource for a MockRequest
 * 
 * @author ole.matzura
 */

public class MonitorMessageExchangeDataSource implements DataSource
{
	private byte[] data;
	private String contentType;
	private String name;

	public MonitorMessageExchangeDataSource( String name, InputStream in, String contentType )
	{
		try
		{
			data = Tools.readAll( in, 0 ).toByteArray();
			this.contentType = contentType;
			this.name = name;
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
}
