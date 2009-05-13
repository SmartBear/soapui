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

package com.eviware.soapui.impl.rest.support.handlers;

import java.io.ByteArrayInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.xmlbeans.XmlObject;
import org.w3c.dom.Document;
import org.w3c.tidy.Tidy;

import com.eviware.soapui.impl.rest.support.MediaTypeHandler;
import com.eviware.soapui.impl.wsdl.submit.transports.http.HttpResponse;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.xml.XmlUtils;

public class HtmlMediaTypeHandler implements MediaTypeHandler
{
	public boolean canHandle( String contentType )
	{
		return contentType != null && contentType.equals( "text/html" );
	}

	public String createXmlRepresentation( HttpResponse response )
	{
		String content = response == null ? null : response.getContentAsString();
		if( !StringUtils.hasContent( content ) )
			return "<xml/>";

		try
		{
			XmlObject xmlObject = XmlObject.Factory.parse( new ByteArrayInputStream( content.getBytes() ) );
			return xmlObject.xmlText();
		}
		catch( Exception e )
		{
			// fall through, this wasn't xml
		}

		try
		{

			Tidy tidy = new Tidy();
			tidy.setXmlOut( true );
			tidy.setShowWarnings( false );
			tidy.setErrout( new PrintWriter( new StringWriter() ) );
			// tidy.setQuiet(true);
			tidy.setNumEntities( true );
			tidy.setQuoteNbsp( true );

			Document document = tidy.parseDOM( new ByteArrayInputStream( content.getBytes() ), null );
			StringWriter writer = new StringWriter();
			XmlUtils.serializePretty( document, writer );
			return writer.toString();
		}
		catch( Throwable e )
		{
			e.printStackTrace();
		}
		return null;
	}
}
