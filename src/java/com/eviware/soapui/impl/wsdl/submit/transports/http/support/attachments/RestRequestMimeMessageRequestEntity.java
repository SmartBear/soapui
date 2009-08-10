/*
 * soapUI, copyright (C) 2004-2009 eviware.com
 *
 * soapUI is free software; you can redistribute it and/or modify it under the
 * terms of version 2.1 of the GNU Lesser General Public License as published by
 * the Free Software Foundation.
 *
 * soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.wsdl.submit.transports.http.support.attachments;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.httpclient.methods.RequestEntity;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.support.http.HttpRequestInterface;

/**
 * MimeMessage request class
 * 
 * @author ole.matzura
 */

public class RestRequestMimeMessageRequestEntity implements RequestEntity
{
	private final MimeMessage message;
	private byte[] buffer;
	private final HttpRequestInterface<?> restRequest;

	public RestRequestMimeMessageRequestEntity( MimeMessage message, HttpRequestInterface<?> restRequest )
	{
		this.message = message;
		this.restRequest = restRequest;
	}

	public long getContentLength()
	{
		try
		{
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			writeRequest( out );
			buffer = out.toByteArray();
			return buffer.length;
		}
		catch( Exception e )
		{
			SoapUI.logError( e );
			return -1;
		}
	}

	public String getContentType()
	{
		try
		{
			String header = message.getHeader( "Content-Type" )[0];
			int ix = header.indexOf( "boundary" );

			return restRequest.getMediaType() + "; " + header.substring( ix );
		}
		catch( MessagingException e )
		{
			SoapUI.logError( e );
		}

		return restRequest.getMediaType();
	}

	public boolean isRepeatable()
	{
		return true;
	}

	public void writeRequest( OutputStream arg0 ) throws IOException
	{
		try
		{
			if( buffer == null )
			{
				arg0.write( "\r\n".getBytes() );
				( ( MimeMultipart )message.getContent() ).writeTo( arg0 );
			}
			else
				arg0.write( buffer );
		}
		catch( Exception e )
		{
			SoapUI.logError( e );
		}
	}
}