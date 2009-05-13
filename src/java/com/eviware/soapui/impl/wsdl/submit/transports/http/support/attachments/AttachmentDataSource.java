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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataSource;

import com.eviware.soapui.model.iface.Attachment;

/**
 * Standard DataSource for existing attachments in soapUI
 * 
 * @author ole.matzura
 */

public class AttachmentDataSource implements DataSource
{
	private final Attachment attachment;

	public AttachmentDataSource( Attachment attachment )
	{
		this.attachment = attachment;
	}

	public String getContentType()
	{
		return attachment.getContentType();
	}

	public InputStream getInputStream() throws IOException
	{
		try
		{
			return attachment.getInputStream();
		}
		catch( Exception e )
		{
			throw new IOException( e.toString() );
		}
	}

	public String getName()
	{
		return attachment.getName();
	}

	public OutputStream getOutputStream() throws IOException
	{
		return null;
	}
}