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
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataSource;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlBase64Binary;
import org.apache.xmlbeans.XmlHexBinary;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.support.xsd.SchemaUtils;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.editor.inspectors.attachments.ContentTypeHandler;

/**
 * DataSource for XOP/MTOM attachments
 * 
 * @author ole.matzura
 */

public final class XOPPartDataSource implements DataSource
{
	private String content;
	private final String contentType;
	private final SchemaType schemaType;
	private File source;

	public XOPPartDataSource( String content, String contentType, SchemaType schemaType )
	{
		this.content = content;
		this.contentType = contentType;
		this.schemaType = schemaType;
	}

	public XOPPartDataSource( File source, String contentType, SchemaType schemaType )
	{
		this.source = source;
		this.contentType = contentType;
		this.schemaType = schemaType;
	}

	public String getContentType()
	{
		return StringUtils.isNullOrEmpty( contentType ) ? ContentTypeHandler.DEFAULT_CONTENTTYPE : contentType;
	}

	public InputStream getInputStream() throws IOException
	{
		try
		{
			if( source != null )
			{
				return new FileInputStream( source );
			}
			if( SchemaUtils.isInstanceOf( schemaType, XmlHexBinary.type ) )
			{
				return new ByteArrayInputStream( Hex.decodeHex( content.toCharArray() ) );
			}
			else if( SchemaUtils.isInstanceOf( schemaType, XmlBase64Binary.type ) )
			{
				return new ByteArrayInputStream( Base64.decodeBase64( content.getBytes() ) );
			}
			else
				throw new IOException( "Invalid type for XOPPartDataSource; " + schemaType.getName() );
		}
		catch( Exception e )
		{
			SoapUI.logError( e );
			throw new IOException( e.toString() );
		}
	}

	public String getName()
	{
		return schemaType.getName().toString();
	}

	public OutputStream getOutputStream() throws IOException
	{
		return null;
	}
}