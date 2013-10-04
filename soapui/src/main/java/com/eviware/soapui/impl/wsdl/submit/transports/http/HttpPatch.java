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

package com.eviware.soapui.impl.wsdl.submit.transports.http;

import java.net.URI;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;

public class HttpPatch extends HttpEntityEnclosingRequestBase
{

	public final static String METHOD_NAME = "PATCH";

	public HttpPatch()
	{
		super();
	}

	public HttpPatch( final URI uri )
	{
		super();
		setURI( uri );
	}

	public HttpPatch( final String uri )
	{
		super();
		setURI( URI.create( uri ) );
	}

	@Override
	public String getMethod()
	{
		return METHOD_NAME;
	}

}
