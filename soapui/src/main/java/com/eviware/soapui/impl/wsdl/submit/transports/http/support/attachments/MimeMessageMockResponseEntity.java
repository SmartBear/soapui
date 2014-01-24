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

import com.eviware.soapui.model.mock.MockResponse;
import org.apache.http.Header;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.message.BasicHeader;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockResponse;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.attachments.WsdlRequestMimeMessageRequestEntity.DummyOutputStream;
import com.eviware.soapui.impl.wsdl.support.soap.SoapVersion;

/**
 * MimeMessage response for a WsdlMockResponse
 * 
 * @author ole.matzura
 */

public class MimeMessageMockResponseEntity extends AbstractHttpEntity
{
	private final MimeMessage message;
	private final boolean isXOP;
	private final MockResponse mockResponse;

	public MimeMessageMockResponseEntity( MimeMessage message, boolean isXOP, MockResponse response )
	{
		this.message = message;
		this.isXOP = isXOP;
		this.mockResponse = response;
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
			if(mockResponse instanceof  WsdlMockResponse)
			{
				SoapVersion soapVersion = (( WsdlMockResponse )mockResponse).getSoapVersion();

				if( isXOP )
				{
					String header = message.getHeader( "Content-Type" )[0];
					return new BasicHeader( "Content-Type", AttachmentUtils.buildMTOMContentType( header, null, soapVersion ) );
				}
				else
				{
					String header = message.getHeader( "Content-Type" )[0];
					int ix = header.indexOf( "boundary" );
					return new BasicHeader( "Content-Type", "multipart/related; type=\"" + soapVersion.getContentType()
							+ "\"; start=\"" + AttachmentUtils.ROOTPART_SOAPUI_ORG + "\"; " + header.substring( ix ) );
				}
			}
			else
			{
				return new BasicHeader( "Content-Type", "application/xml" );
			}
		}
		catch( MessagingException e )
		{
			SoapUI.logError( e );
		}

		return null;
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
