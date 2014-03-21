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

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.AbstractRequestConfig;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.support.MediaTypeHandler;
import com.eviware.soapui.impl.support.AbstractHttpRequestInterface;
import com.eviware.soapui.impl.support.HttpUtils;
import com.eviware.soapui.impl.wsdl.submit.transports.http.HttpResponse;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.xml.XmlUtils;
import net.sf.json.JSON;
import net.sf.json.JSONException;
import net.sf.json.JSONSerializer;

import java.net.URL;

public class JsonMediaTypeHandler implements MediaTypeHandler
{
	public boolean canHandle( String contentType )
	{
		return seemsToBeJsonContentType( contentType );
	}

	/**
	 * This method and its name are somewhat awkward, but both stem from the fact that there are so many commonly used
	 * content types for JSON.
	 * @param contentType the MIME type to examine
	 * @return <code>true</code> if content type is non-null and contains either "json" or "javascript"
	 */
	public static boolean seemsToBeJsonContentType( String contentType )
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
			// remove nulls - workaround for bug in xmlserializer!?
			content = content.replaceAll( "\\\\u0000", "" );
			JSON json = JSONSerializer.toJSON( content );
			JsonXmlSerializer serializer = new JsonXmlSerializer();
			serializer.setTypeHintsEnabled( false );
			serializer.setRootName( HttpUtils.isErrorStatus( response.getStatusCode() ) ? "Fault" : "Response" );
			URL url = response.getURL();
			String originalUri = readOriginalUriFrom( response.getRequest() );
			String namespaceUri = originalUri != null ? originalUri : makeNamespaceUriFrom( url );
			serializer.setNamespace( "",  namespaceUri);
			content = serializer.write( json );
			content = XmlUtils.prettyPrintXml( content );

			return content;
		}
		catch (JSONException ignore)
		{
			// if the content is not valid JSON, empty XML will be returned
		}
		catch( Exception e )
		{
			SoapUI.logError(e);
		}
		return "<xml/>";
	}

	private String readOriginalUriFrom( AbstractHttpRequestInterface<?> request )
	{
		if (request instanceof RestRequest )
		{
			AbstractRequestConfig config = ( ( RestRequest )request ).getConfig();
			String originalUri = config.getOriginalUri();
			// if URI contains unexpanded template parameters
			if (originalUri != null && originalUri.contains("{"))
			{
				return null;
			}
			return originalUri;
		}
		else
		{
			return null;
		}
	}

	public static String makeNamespaceUriFrom( URL url )
	{
		return url.getProtocol() + "://" + url.getHost() + url.getPath();
	}
}
