/*
 *  SoapUI, copyright (C) 2004-2014 smartbear.com 
 *
 *  SoapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  SoapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */
package com.eviware.soapui.impl.wsdl.support.http;

import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.conn.params.ConnRouteParams;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.conn.ProxySelectorRoutePlanner;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;

import java.net.ProxySelector;

/**
 * @author Joel
 */
public class OverridableProxySelectorRoutePlanner extends ProxySelectorRoutePlanner
{
	private static final String FORCE_DIRECT_CONNECTION = "FORCE_DIRECT_CONNECTION";

	static void setForceDirectConnection( HttpParams params )
	{
		params.setBooleanParameter( FORCE_DIRECT_CONNECTION, true );
	}

	public OverridableProxySelectorRoutePlanner( SchemeRegistry registry, ProxySelector proxySelector )
	{
		super( registry, proxySelector );
	}

	@Override
	protected HttpHost determineProxy( HttpHost target, HttpRequest request, HttpContext context ) throws HttpException
	{
		if( request.getParams().getBooleanParameter( FORCE_DIRECT_CONNECTION, false ) )
		{
			return null;
		}
		final HttpHost proxy = ConnRouteParams.getDefaultProxy( request.getParams() );
		// Proxy should be able to be set for a request with ConnRoutePNames.DEFAULT_PROXY
		if( proxy != null )
		{
			return proxy;
		}

		return super.determineProxy( target, request, context );
	}
}
