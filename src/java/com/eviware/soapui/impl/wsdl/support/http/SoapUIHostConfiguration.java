/*
 *  soapUI, copyright (C) 2004-2011 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.wsdl.support.http;

import org.apache.http.HttpHost;

public class SoapUIHostConfiguration
{
	private HttpHost httpHost;

	public SoapUIHostConfiguration( HttpHost httpHost )
	{
		this.httpHost = httpHost;
	}

	public SoapUIHostConfiguration( String httpHost )
	{
		this.httpHost = new HttpHost( httpHost );
	}

	public SoapUIHostConfiguration( String host, int port )
	{
		this.httpHost = new HttpHost( host, port );
	}

	public void setHttpHost( HttpHost httpHost )
	{
		this.httpHost = httpHost;
	}

	public HttpHost getHttpHost()
	{
		return httpHost;
	}

}
