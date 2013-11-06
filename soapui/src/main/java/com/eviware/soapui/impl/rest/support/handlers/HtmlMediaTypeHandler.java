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

package com.eviware.soapui.impl.rest.support.handlers;

import java.io.ByteArrayInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.w3c.dom.Document;
import org.w3c.tidy.Tidy;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.rest.support.MediaTypeHandler;
import com.eviware.soapui.impl.wsdl.submit.transports.http.HttpResponse;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.xml.XmlUtils;

public class HtmlMediaTypeHandler implements MediaTypeHandler
{
	public boolean canHandle( String contentType )
	{
		return contentType != null && contentType.toLowerCase().contains( "text/html" );
	}

	public String createXmlRepresentation( HttpResponse response )
	{
		String content = response == null ? null : response.getContentAsString();
		if( !StringUtils.hasContent( content ) )
			return "<xml/>";

		try
		{
			// XmlObject.Factory.parse( new ByteArrayInputStream(
			// content.getBytes() ) );
			XmlUtils.createXmlObject( new ByteArrayInputStream( content.getBytes() ) );
			return content;
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
			tidy.setFixUri( false );

			Document document = tidy.parseDOM( new ByteArrayInputStream( content.getBytes() ), null );
			StringWriter writer = new StringWriter();
			XmlUtils.serializePretty( document, writer );
			return writer.toString();
		}
		catch( Throwable e )
		{
			SoapUI.logError( e );
		}
		return null;
	}
}
