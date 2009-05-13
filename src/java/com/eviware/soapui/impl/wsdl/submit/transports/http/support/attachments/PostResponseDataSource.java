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

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.submit.transports.http.ExtendedHttpMethod;
import com.eviware.soapui.impl.wsdl.support.CompressionSupport;
import com.eviware.soapui.impl.wsdl.support.http.HttpClientSupport;
import com.eviware.soapui.settings.HttpSettings;

/**
 * DataSource for a standard POST response
 * 
 * @author ole.matzura
 */

public class PostResponseDataSource implements DataSource
{
	private final ExtendedHttpMethod postMethod;
	private byte[] data;

	public PostResponseDataSource( ExtendedHttpMethod postMethod )
	{
		this.postMethod = postMethod;

		try
		{
			data = postMethod.getResponseBody(); // Tools.readAll(
																// postMethod.getResponseBodyAsStream(),
																// 0 ).toByteArray();

			if( !SoapUI.getSettings().getBoolean( HttpSettings.DISABLE_RESPONSE_DECOMPRESSION ) )
			{
				String compressionAlg = HttpClientSupport.getResponseCompressionType( postMethod );
				if( compressionAlg != null )
					data = CompressionSupport.decompress( compressionAlg, data );
			}
		}
		catch( Exception e )
		{
			SoapUI.logError( e );
		}
	}

	public long getDataSize()
	{
		return data == null ? -1 : data.length;
	}

	public String getContentType()
	{
		return postMethod.getResponseHeader( "Content-Type" ).getValue();
	}

	public InputStream getInputStream() throws IOException
	{
		return new ByteArrayInputStream( data );
	}

	public String getName()
	{
		return postMethod.getName() + " response for " + postMethod.getPath().toString();
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
