/*
 *  soapUI, copyright (C) 2004-2008 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.settings;

/**
 * HTTP-related settings constants
 * 
 * @author Ole.Matzura
 */

public interface HttpSettings
{
	public final static long DEFAULT_SOCKET_TIMEOUT = 60000L;
	
	public final static String REQUEST_COMPRESSION = HttpSettings.class.getSimpleName() + "@" + "request-compression";
	public final static String RESPONSE_COMPRESSION = HttpSettings.class.getSimpleName() + "@" + "response-compression";
	public final static String CLOSE_CONNECTIONS = HttpSettings.class.getSimpleName() + "@" + "close-connections";
	public final static String USER_AGENT = HttpSettings.class.getSimpleName() + "@" + "user-agent";
	public final static String AUTHENTICATE_PREEMPTIVELY = HttpSettings.class.getSimpleName() + "@" + "authenticate-preemptively";
	public final static String SOCKET_TIMEOUT = HttpSettings.class.getSimpleName() + "@" + "socket_timeout";
	public final static String INCLUDE_REQUEST_IN_TIME_TAKEN = HttpSettings.class.getSimpleName() + "@" + "include_request_in_time_taken";
	public final static String INCLUDE_RESPONSE_IN_TIME_TAKEN = HttpSettings.class.getSimpleName() + "@" + "include_response_in_time_taken";
	public final static String MAX_RESPONSE_SIZE = HttpSettings.class.getSimpleName() + "@" + "max_response_size";
	public static final String ENCODED_URLS = HttpSettings.class.getSimpleName() + "@" + "encoded_urls";
	public static final String BIND_ADDRESS = HttpSettings.class.getSimpleName() + "@" + "bind_address";
	public static final String DISABLE_CHUNKING = HttpSettings.class.getSimpleName() + "@" + "disable_chunking";
	public static final String HTTP_VERSION = HttpSettings.class.getSimpleName() + "@" + "http_version";
	
	public static final String MAX_CONNECTIONS_PER_HOST = HttpSettings.class.getSimpleName() + "@" + "max_connections_per_host";
	public static final String MAX_TOTAL_CONNECTIONS = HttpSettings.class.getSimpleName() + "@" + "max_total_connections";

	public static final String LEAVE_MOCKENGINE =  HttpSettings.class.getSimpleName() + "@" + "leave_mockengine";

	public static final String HTTP_VERSION_0_9 = "0.9";
	public static final String HTTP_VERSION_1_0 = "1.0";
	public static final String HTTP_VERSION_1_1 = "1.1";
}
