/*
 * soapUI, copyright (C) 2004-2013 smartbear.com
 *
 * soapUI is free software; you can redistribute it and/or modify it under the
 * terms of version 2.1 of the GNU Lesser General Public License as published by
 * the Free Software Foundation.
 *
 * soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.rest.support;

import com.eviware.soapui.impl.rest.RestURIParser;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Basic RestURIParser using java's .net.URI and .net.URL class
 *
 * @author Shadid Chowdhury
 * @since 4.5.6
 */
public class RestURIParserImpl implements RestURIParser
{
	private static final String SCHEME_SEPARATOR = "://";
	private static final String DEFAULT_SCHEME = "http";

	private String resourcePath = "";
	private String query = "";
	private String scheme = "";
	private String authority = "";

	public RestURIParserImpl( String uriString ) throws MalformedURLException
	{
		if( uriString == null || uriString.isEmpty() )
			throw new MalformedURLException( "Empty URI" );

		parseURI( uriString );
	}

	private void parseURI( String uriString ) throws MalformedURLException
	{
		try
		{
			parseWithURI( uriString );
		}
		catch( URISyntaxException e )
		{
			parseWithURL( uriString );

		}
	}

	@Override
	public String getEndpoint()
	{
		String endpoint;

		if( authority.isEmpty() )
		{
			endpoint = "";
		}
		else if( scheme.isEmpty() )
		{
			endpoint = DEFAULT_SCHEME + SCHEME_SEPARATOR + authority;
		}
		else
		{
			endpoint = scheme + SCHEME_SEPARATOR + authority;
		}

		return endpoint;
	}

	@Override
	public String getResourceName()
	{
		String path = getResourcePath();

		if( path.isEmpty() )
			return path;

		String[] splitResourcePath = path.split( "/" );
		String resourceName = splitResourcePath[splitResourcePath.length - 1];
		resourceName = resourceName.replaceAll( "^\\{|\\}$", "" );
		String capitalizedResourceName = resourceName.substring( 0, 1 ).toUpperCase() + resourceName.substring( 1 );

		return capitalizedResourceName;
	}

	@Override
	public String getScheme()
	{
		return scheme;
	}

	@Override
	public String getResourcePath()
	{
		String path = resourcePath;

		path = removeTrailingSlash( path );
		path = addPrefixSlash( path );

		return path;
	}

	@Override
	public String getQuery()
	{
		return query;
	}

	private String removeTrailingSlash( String path )
	{
		return path.replaceFirst( "/$", "" );
	}

	private String addPrefixSlash( String path )
	{
		if( !path.startsWith( "/" ) && !path.isEmpty() )
			path = "/" + path;

		return path;
	}

	private void parseWithURI( String uriString ) throws URISyntaxException
	{
		if( isOnlyAuthority( uriString ) )
		{
			authority = uriString;
			return;
		}

		URI uri = new URI( uriString );
		resourcePath = ( uri.getPath() == null ? "" : uri.getPath() );
		query = ( uri.getQuery() == null ? "" : uri.getQuery() );
		scheme = ( uri.getScheme() == null ? "" : uri.getScheme() );
		authority = ( uri.getAuthority() == null ? "" : uri.getAuthority() );
	}

	private boolean isOnlyAuthority( String uriString )
	{
		// To match spotify.com and 127.0.0.1
		// TODO: Add port support like spotify.com:8081
		return uriString.matches( "([a-zA-Z0-9]+\\.[a-z]+)|([0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+)" );
	}

	private void parseWithURL( String uriString ) throws MalformedURLException
	{
		URL url = null;
		try
		{
			url = new URL( uriString );
			resourcePath = ( url.getPath() == null ? "" : url.getPath() );
			query = ( url.getQuery() == null ? "" : url.getQuery() );
			scheme = ( url.getProtocol() == null ? "" : url.getProtocol() );
			authority = ( url.getAuthority() == null ? "" : url.getAuthority() );
		}
		catch( MalformedURLException e )
		{
			parseManually( uriString );
		}

	}

	private void parseManually( String uriString )
	{
		resourcePath = uriString;
		query = "";
		scheme = "";
		authority = "";

		int startIndexOfQuery = uriString.indexOf( '?' );
		if( startIndexOfQuery >= 0 )
		{
			query = uriString.substring( startIndexOfQuery + 1 );
			resourcePath = uriString.substring( 0, startIndexOfQuery );
		}

		int startIndexOfResource = resourcePath.indexOf( '/' );
		if( startIndexOfResource >= 0 )
		{
			resourcePath = resourcePath.substring( startIndexOfResource + 1 );
			authority = resourcePath.substring( 0, startIndexOfResource );
		}
	}

}
