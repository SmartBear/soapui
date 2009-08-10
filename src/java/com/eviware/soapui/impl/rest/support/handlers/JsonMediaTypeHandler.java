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

import java.net.URL;

import net.sf.json.JSON;
import net.sf.json.JSONException;
import net.sf.json.JSONSerializer;
import net.sf.json.xml.XMLSerializer;

import com.eviware.soapui.impl.rest.support.MediaTypeHandler;
import com.eviware.soapui.impl.support.HttpUtils;
import com.eviware.soapui.impl.wsdl.submit.transports.http.HttpResponse;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.xml.XmlUtils;

public class JsonMediaTypeHandler implements MediaTypeHandler
{
	public boolean canHandle( String contentType )
	{
		return couldBeJsonContent( contentType );
	}

	public static boolean couldBeJsonContent( String contentType )
	{
		return contentType != null && ( contentType.contains( "javascript" ) || contentType.contains( "json" ) );
	}

	public String createXmlRepresentation( HttpResponse response )
	{
		try
		{
			String content = response.getContentAsString().trim();
			if( !StringUtils.hasContent( content ) )
				return null;

			JSON json = JSONSerializer.toJSON( content );
			XMLSerializer serializer = new XMLSerializer();
			serializer.setTypeHintsEnabled( false );
			serializer.setRootName( HttpUtils.isErrorStatus( response.getStatusCode() ) ? "Fault" : "Response" );
			URL url = response.getURL();
			serializer.setNamespace( "", url.getProtocol() + "://" + url.getHost() + url.getPath() );
			content = serializer.write( json );
			content = XmlUtils.prettyPrintXml( content );

			return content;
		}
		catch( Throwable e )
		{
			if( !( e instanceof JSONException ) )
				e.printStackTrace();
		}
		return "<xml/>";
	}
}
