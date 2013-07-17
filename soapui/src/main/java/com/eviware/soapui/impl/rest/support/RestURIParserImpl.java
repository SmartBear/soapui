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
 * Author: Shadid Chowdhury
 */
public class RestURIParserImpl implements RestURIParser
{
	URI uri;
	private static final String HTTP_SCHEME_SEPARATOR = "://";

	public RestURIParserImpl( String uri ) throws URISyntaxException
	{
		this.uri = new URI( uri );
	}

	@Override
	public String getEndpoint()
	{
		String endpoint = uri.getScheme() + HTTP_SCHEME_SEPARATOR + uri.getAuthority();
		return endpoint;
	}

	@Override
	public String getScheme()
	{
		return uri.getScheme();
	}

	@Override
	public String getAuthority()
	{
		return uri.getAuthority();
	}

	@Override
	public String getPath()
	{
		return uri.getPath();
	}

	@Override
	public String getQuery()
	{
		return uri.getQuery();
	}

	@Override
	public String getFragment()
	{
		return uri.getFragment();
	}

	@Override
	public String getParams()
	{
		return uri.getQuery();
	}
}
