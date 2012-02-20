/*
 *  soapUI, copyright (C) 2004-2012 smartbear.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.support;

import com.eviware.soapui.support.StringUtils;

public class HttpUtils
{
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
}
