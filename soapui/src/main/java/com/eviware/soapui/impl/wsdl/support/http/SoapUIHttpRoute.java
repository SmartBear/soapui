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
package com.eviware.soapui.impl.wsdl.support.http;

import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.util.LangUtils;

public class SoapUIHttpRoute
{

	public static final String SOAPUI_SSL_CONFIG = "soapui.sslConfig";
	private HttpRoute httpRoute;
	private String param;

	public SoapUIHttpRoute( HttpRoute httpRoute )
	{
		this.httpRoute = httpRoute;
	}

	public final boolean equals( Object o )
	{
		if( o instanceof SoapUIHttpRoute )
		{
			SoapUIHttpRoute obj = ( SoapUIHttpRoute )o;
			HttpRoute that = obj.getHttpRoute();

			boolean result = httpRoute.equals( that );
			if( result )
			{
				return param.equals( obj.getParam() );
			}
		}
		return false;
	}

	public String getParam()
	{
		return this.param;
	}

	public HttpRoute getHttpRoute()
	{
		return this.httpRoute;
	}

	public synchronized int hashCode()
	{
		int hash = httpRoute.hashCode();
		hash = LangUtils.hashCode( hash, param );
		return hash;
	}

}
