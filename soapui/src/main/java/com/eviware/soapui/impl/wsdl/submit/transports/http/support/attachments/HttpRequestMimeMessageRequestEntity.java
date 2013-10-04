/*
 *  SoapUI, copyright (C) 2004-2011 smartbear.com
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

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.http.Header;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.message.BasicHeader;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.support.http.HttpRequestInterface;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.attachments.WsdlRequestMimeMessageRequestEntity.DummyOutputStream;

/**
 * MimeMessage request class
 * 
 * @author ole.matzura
 */

public class HttpRequestMimeMessageRequestEntity extends AbstractHttpEntity
{
	private final MimeMessage message;
	private final HttpRequestInterface<?> restRequest;

	public HttpRequestMimeMessageRequestEntity( MimeMessage message, HttpRequestInterface<?> restRequest )
	{
		this.message = message;
		this.restRequest = restRequest;
	}

	public long getContentLength()
	{
		try
		{
			DummyOutputStream out = new DummyOutputStream();
			writeTo( out );
			return out.getSize();
		}
		catch( Exception e )
		{
			SoapUI.logError( e );
			return -1;
		}
	}

	public Header getContentType()
	{
		try
		{
			String header = message.getHeader( "Content-Type" )[0];
			int ix = header.indexOf( "boundary" );

			return new BasicHeader( "Content-Type", restRequest.getMediaType() + "; " + header.substring( ix ) );
		}
		catch( MessagingException e )
		{
			SoapUI.logError( e );
		}

		return new BasicHeader( "Content-Type", restRequest.getMediaType() );
	}

	public boolean isRepeatable()
	{
		return true;
	}

	public void writeTo( OutputStream arg0 ) throws IOException
	{
		try
		{
			arg0.write( "\r\n".getBytes() );
			( ( MimeMultipart )message.getContent() ).writeTo( arg0 );
		}
		catch( Exception e )
		{
			SoapUI.logError( e );
		}
	}

	@Override
	public InputStream getContent() throws IOException, IllegalStateException
	{
		try
		{
			return message.getInputStream();
		}
		catch( MessagingException e )
		{
			throw new IOException( e );
		}
	}

	@Override
	public boolean isStreaming()
	{
		return false;
	}
}
