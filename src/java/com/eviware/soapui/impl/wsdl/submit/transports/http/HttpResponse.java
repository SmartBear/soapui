/*
 *  soapUI, copyright (C) 2004-2009 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.wsdl.submit.transports.http;

import java.net.URL;

import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.model.iface.Response;

public interface HttpResponse extends Response
{
	public abstract AbstractHttpRequest<?> getRequest();

	public abstract String getRequestContent();

	public abstract void setResponseContent( String responseContent );

	public abstract SSLInfo getSSLInfo();

	public abstract URL getURL();

	public AbstractHttpRequest.RequestMethod getMethod();

	public String getHttpVersion();

	public abstract int getStatusCode();
}