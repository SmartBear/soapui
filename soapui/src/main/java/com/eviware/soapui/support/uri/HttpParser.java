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

package com.eviware.soapui.support.uri;

import java.io.IOException;
import java.io.InputStream;
import java.net.ProtocolException;
import java.util.ArrayList;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.message.BasicHeader;

public class HttpParser
{

	public static String readLine( InputStream inputStream, String charset ) throws IOException
	{
		return IOUtils.toString( inputStream, charset );
	}

	public static Header[] parseHeaders( InputStream is, String charset ) throws IOException, HttpException
	{
		ArrayList<Header> headers = new ArrayList<Header>();
		String name = null;
		StringBuffer value = null;
		for( ;; )
		{
			String line = HttpParser.readLine( is, charset );
			if( ( line == null ) || ( line.trim().length() < 1 ) )
			{
				break;
			}

			if( ( line.charAt( 0 ) == ' ' ) || ( line.charAt( 0 ) == '\t' ) )
			{
				if( value != null )
				{
					value.append( ' ' );
					value.append( line.trim() );
				}
			}
			else
			{
				if( name != null )
				{
					headers.add( new BasicHeader( name, value.toString() ) );
				}

				int colon = line.indexOf( ":" );
				if( colon < 0 )
				{
					throw new ProtocolException( "Unable to parse header: " + line );
				}
				name = line.substring( 0, colon ).trim();
				value = new StringBuffer( line.substring( colon + 1 ).trim() );
			}

		}

		if( name != null )
		{
			headers.add( new BasicHeader( name, value.toString() ) );
		}

		return ( Header[] )headers.toArray( new Header[headers.size()] );
	}

}