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

package com.eviware.soapui.impl.support;

import java.net.InetAddress;
import java.net.URISyntaxException;

import com.eviware.soapui.support.StringUtils;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.http.client.utils.URIUtils;

public class HttpUtils
{
	private static String pingErrorMessage;

	public static boolean isErrorStatus( int statusCode )
	{
		return statusCode >= 400;
	}

	public static String extractHttpHeaderParameter( String headerString, String parameterName )
	{
		if( !StringUtils.hasContent( headerString ) || !StringUtils.hasContent( parameterName ) )
			return null;

		int ix = headerString.indexOf( parameterName + "=\"" );
		if( ix > 0 )
		{
			int ix2 = headerString.indexOf( '"', ix + parameterName.length() + 2 );
			if( ix2 > ix )
				return headerString.substring( ix + parameterName.length() + 2, ix2 );
		}

		return null;
	}

	public static String ensureEndpointStartsWithProtocol( String endpoint )
	{
		if( StringUtils.isNullOrEmpty( endpoint ) )
			return endpoint;

		String ep = endpoint.toLowerCase().trim();
		if( !ep.startsWith( "http://" ) && !ep.startsWith( "https://" ) && !ep.startsWith( "$" ) )
			return "http://" + endpoint;

		return endpoint;
	}

	public static boolean ping( String host, int timeout )
	{
		boolean result = false;
		pingErrorMessage = "No Error";
		try
		{
			InetAddress address = InetAddress.getByName( host );
			result = address.isReachable( timeout );
		}
		catch( Exception e )
		{
			result = false;
			pingErrorMessage = e.getMessage();
		}
		finally
		{
			return result;
		}
	}
	
	public static String getPingErrorMessage() {
		return pingErrorMessage;
	}

	public static java.net.URI createUri( URI uri ) throws URISyntaxException, URIException
	{
		return createUri( uri.getScheme(), uri.getUserinfo(), uri.getHost(), uri.getPort(), uri.getEscapedPath(),
				uri.getEscapedQuery(), uri.getEscapedFragment() );
	}

	public static java.net.URI createUri( String scheme, String userinfo, String host, int port, String escapedPath,
													  String escapedQuery, String escapedFragment ) throws URISyntaxException
	{
		return URIUtils.createURI( scheme, ( userinfo == null ? "" : ( userinfo + "@" ) ) + host, port, escapedPath,
				escapedQuery, escapedFragment );
	}
}
