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

import com.eviware.soapui.impl.wsdl.submit.RequestTransport;

/**
 * Base for HTTP transports
 * 
 * @author Ole.Matzura
 */

public interface BaseHttpRequestTransport extends RequestTransport
{
	public static final String HTTP_METHOD = "httpMethod";
	public static final String POST_METHOD = "postMethod";
	public static final String HTTP_CLIENT = "httpClient";
	public static final String HOST_CONFIGURATION = "hostConfiguration";
	public static final String REQUEST_CONTENT = "requestContent";
	public static final String RESPONSE = "httpResponse";
	public static final String RESPONSE_PROPERTIES = "httpResponseProperties";
}
