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

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Basic RestURIParser using java's .net.URI class
 *
 * @author Shadid Chowdhury
 * @since 4.5.6
 */
public class RestURIParserImpl implements RestURIParser
{
	URI uri;
	private static final String SCHEME_SEPARATOR = "://";
	private static final String DEFAULT_SCHEME = "http";

	public RestURIParserImpl( String uri ) throws URISyntaxException
	{
		this.uri = new URI( uri );
	}

	@Override
	public String getEndpoint()
	{
		String scheme = uri.getScheme();
		String authority = uri.getAuthority();
		String endpoint;

		if( authority == null )
		{
			endpoint = "";
		}
		else if( scheme == null )
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
		String path = uri.getPath();
		if( path == null )
			return "";

		String[] splitResourcePath = path.split( "/" );
		String resourceName = splitResourcePath[splitResourcePath.length - 1];
		String capitalizedResourceName = resourceName.substring( 0, 1 ).toUpperCase() + resourceName.substring( 1 );

		return capitalizedResourceName;
	}

	@Override
	public String getScheme()
	{
		String scheme = uri.getScheme();
		if( scheme == null )
			return "";

		return scheme;
	}

	@Override
	public String getAuthority()
	{
		String authority = uri.getAuthority();
		if( authority == null )
			return "";

		return authority;
	}

	@Override
	public String getPath()
	{
		String path = uri.getPath();
		if( path == null )
			return "";

		return path;
	}

	@Override
	public String getQuery()
	{
		String query = uri.getQuery();
		if( query == null )
			return "";

		return query;
	}

	@Override
	public String getFragment()
	{
		String fragment = uri.getFragment();
		if( fragment == null )
			return "";

		return fragment;
	}

	private boolean isValidScheme( URI uri )
	{
		String scheme = uri.getScheme();

		if (scheme.equals( "http" ) || scheme.equals ("https") )
		{
			return true;
		}
		else
		{
			return false;
		}
	}
}
