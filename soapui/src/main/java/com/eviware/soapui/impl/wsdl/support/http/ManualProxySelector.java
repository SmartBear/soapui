/*
 *  SoapUI, copyright (C) 2004-2013 smartbear.com 
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

import java.io.IOException;
import java.net.*;
import java.util.Arrays;
import java.util.List;

/**
 * @author Joel
 */
public class ManualProxySelector extends ProxySelector
{
	private static final List<Proxy> NO_PROXY_LIST = Arrays.asList( Proxy.NO_PROXY );
	private final List<Proxy> proxyList;
	private final String[] excludes;

	public ManualProxySelector( Proxy proxy, String[] excludes )
	{
		super();
		this.excludes = excludes;
		this.proxyList = Arrays.asList( proxy );
	}

	public ManualProxySelector( String proxyHost, int proxyPort, String[] excludes )
	{
		this( new Proxy( Proxy.Type.HTTP,
				InetSocketAddress.createUnresolved( proxyHost, proxyPort ) ), excludes );
	}

	@Override
	public List<Proxy> select( URI uri )
	{
		if( !ProxyUtils.excludes( excludes, uri.getHost(), uri.getPort() ) )
		{
			return proxyList;
		}
		else
		{
			return NO_PROXY_LIST;
		}
	}

	@Override
	public void connectFailed( URI uri, SocketAddress sa, IOException ioe )
	{
		// Not used
	}
}
