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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataSource;
import javax.mail.internet.MimeMultipart;

import com.eviware.soapui.SoapUI;

/**
 * DataSource for multipart attachments
 * 
 * @author ole.matzura
 */

public class MultipartAttachmentDataSource implements DataSource
{
	private final MimeMultipart multipart;

	public MultipartAttachmentDataSource( MimeMultipart multipart )
	{
		this.multipart = multipart;
	}

	public String getContentType()
	{
		return multipart.getContentType();
	}

	public InputStream getInputStream() throws IOException
	{
		try
		{
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			multipart.writeTo( out );
			return new ByteArrayInputStream( out.toByteArray() );
		}
		catch( Exception e )
		{
			SoapUI.logError( e );
			return null;
		}
	}

	public String getName()
	{
		return multipart.toString();
	}

	public OutputStream getOutputStream() throws IOException
	{
		return null;
	}
}