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
import javax.mail.BodyPart;
import javax.mail.MessagingException;

import com.eviware.soapui.SoapUI;

/**
 * DataSource for a BodyPart
 * 
 * @author ole.matzura
 */

public class BodyPartDataSource implements DataSource
{
	private final BodyPart bodyPart;

	public BodyPartDataSource( BodyPart bodyPart )
	{
		this.bodyPart = bodyPart;
	}

	public String getContentType()
	{
		try
		{
			return bodyPart.getContentType();
		}
		catch( MessagingException e )
		{
			SoapUI.logError( e );
			return null;
		}
	}

	public InputStream getInputStream() throws IOException
	{
		try
		{
			return bodyPart.getInputStream();
		}
		catch( MessagingException e )
		{
			SoapUI.logError( e );
			return null;
		}
	}

	public String getName()
	{
		try
		{
			return bodyPart.getHeader( "Content-ID" )[0];
		}
		catch( MessagingException e )
		{
			SoapUI.logError( e );
			return null;
		}
	}

	public OutputStream getOutputStream() throws IOException
	{
		return null;
	}

}
