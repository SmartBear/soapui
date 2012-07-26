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
package com.eviware.soapui.impl.wsdl.support.http;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.protocol.HttpContext;

/**
 * This request interceptor checks if wrapper request have more http headers. If
 * that is true then it copies those headers in original request. This way they
 * will be visible in raw request and accessible for users in Groovy scripts.
 * 
 * @author robert.nemet
 * 
 */
public class HeaderRequestInterceptor implements HttpRequestInterceptor
{
	public static final String SOAPUI_REQUEST_HEADERS = "soapui.request.headers";

	@Override
	public void process( HttpRequest request, HttpContext context ) throws HttpException, IOException
	{
		List<Header> wHeaders = Arrays.asList( request.getAllHeaders() );
		context.setAttribute( SOAPUI_REQUEST_HEADERS, wHeaders );
	}
}
