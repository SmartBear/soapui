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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataSource;
import javax.mail.internet.MimeMultipart;

import com.eviware.soapui.SoapUI;

public class MultipartAttachmentFileDataSource implements DataSource
{
	private final MimeMultipart multipart;

	public MultipartAttachmentFileDataSource( MimeMultipart multipart )
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
			File file = File.createTempFile( "largeAttachment", ".tmp" );
			file.deleteOnExit();

			BufferedOutputStream out = new BufferedOutputStream( new FileOutputStream( file ) );
			multipart.writeTo( out );

			out.flush();
			out.close();

			return new BufferedInputStream( new FileInputStream( file ) );
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
