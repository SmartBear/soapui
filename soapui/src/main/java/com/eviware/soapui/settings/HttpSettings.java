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

package com.eviware.soapui.settings;

import com.eviware.soapui.impl.wsdl.support.CompressionSupport;
import com.eviware.soapui.settings.Setting.SettingType;

/**
 * HTTP-related settings constants
 * 
 * @author Ole.Matzura
 */

public interface HttpSettings
{
	public final static long DEFAULT_SOCKET_TIMEOUT = 60000L;

	public static final String HTTP_VERSION_0_9 = "0.9";
	public static final String HTTP_VERSION_1_0 = "1.0";
	public static final String HTTP_VERSION_1_1 = "1.1";

	@Setting( name = "HTTP Version", description = "Select HTTP Version to use", type = SettingType.ENUMERATION, values = {
			HTTP_VERSION_1_0, HTTP_VERSION_1_1 } )
	public static final String HTTP_VERSION = HttpSettings.class.getSimpleName() + "@" + "http_version";

	@Setting( name = "User-Agent Header", description = "User-Agent HTTP header to send, blank will send default" )
	public final static String USER_AGENT = HttpSettings.class.getSimpleName() + "@" + "user-agent";

	@Setting( name = "Request compression", description = "", type = SettingType.ENUMERATION, values = { "None",
			CompressionSupport.ALG_GZIP, CompressionSupport.ALG_DEFLATE } )
	public final static String REQUEST_COMPRESSION = HttpSettings.class.getSimpleName() + "@" + "request-compression";

	@Setting( name = "Response compression", description = "Accept compressed responses from hosts", type = SettingType.BOOLEAN )
	public final static String RESPONSE_COMPRESSION = HttpSettings.class.getSimpleName() + "@" + "response-compression";

	@Setting( name = "Disable Response Decompression", description = "Disable decompression of compressed responses", type = SettingType.BOOLEAN )
	public static final String DISABLE_RESPONSE_DECOMPRESSION = HttpSettings.class.getSimpleName()
			+ "@disable_response_decompression";

	@Setting( name = "Close connections after request", description = "Closes the HTTP connection after each SOAP request", type = SettingType.BOOLEAN )
	public final static String CLOSE_CONNECTIONS = HttpSettings.class.getSimpleName() + "@" + "close-connections";

	// @Setting( name = "Disable Chunking", description =
	// "Disables content-chunking", type = SettingType.BOOLEAN )
	// public static final String DISABLE_CHUNKING =
	// HttpSettings.class.getSimpleName() + "@" + "disable_chunking";

	@Setting( name = "Chunking Threshold", description = "Uses content-chunking for requests larger than threshold, blank to disable", type = SettingType.INT )
	public static final String CHUNKING_THRESHOLD = HttpSettings.class.getSimpleName() + "@" + "chunking_threshold";

	@Setting( name = "Authenticate Preemptively", description = "Adds authentication information to outgoing request", type = SettingType.BOOLEAN )
	public final static String AUTHENTICATE_PREEMPTIVELY = HttpSettings.class.getSimpleName() + "@"
			+ "authenticate-preemptively";

	@Setting( name = "Expect-Continue", description = "Adds Expect-Continue header to outgoing request", type = SettingType.BOOLEAN )
	public final static String EXPECT_CONTINUE = HttpSettings.class.getSimpleName() + "@expect-continue";

	@Setting( name = "Pre-encoded Endpoints", description = "Do not URL-escape service endpoints", type = SettingType.BOOLEAN )
	public static final String ENCODED_URLS = HttpSettings.class.getSimpleName() + "@" + "encoded_urls";

	@Setting( name = "Normalize Forward Slashes", description = "Replaces duplicate forward slashes in HTTP request endpoints with a single slash", type = SettingType.BOOLEAN )
	public static final String FORWARD_SLASHES = HttpSettings.class.getSimpleName() + "@" + "forward_slashes";

	@Setting( name = "Bind Address", description = "Default local address to bind to when sending requests" )
	public static final String BIND_ADDRESS = HttpSettings.class.getSimpleName() + "@" + "bind_address";

	@Setting( name = "Include request in time taken", description = "Includes the time it took to write the request in time-taken", type = SettingType.BOOLEAN )
	public final static String INCLUDE_REQUEST_IN_TIME_TAKEN = HttpSettings.class.getSimpleName() + "@"
			+ "include_request_in_time_taken";

	@Setting( name = "Include response in time taken", description = "Includes the time it took to read the entire response in time-taken", type = SettingType.BOOLEAN )
	public final static String INCLUDE_RESPONSE_IN_TIME_TAKEN = HttpSettings.class.getSimpleName() + "@"
			+ "include_response_in_time_taken";

	@Setting( name = "Socket Timeout (ms)", description = "Socket timeout in milliseconds" )
	public final static String SOCKET_TIMEOUT = HttpSettings.class.getSimpleName() + "@" + "socket_timeout";

	@Setting( name = "Max response size", description = "Maximum size to read from response (0 = no limit)" )
	public final static String MAX_RESPONSE_SIZE = HttpSettings.class.getSimpleName() + "@" + "max_response_size";

	@Setting( name = "Max Connections Per Host", description = "Maximum number of Connections Per Host" )
	public static final String MAX_CONNECTIONS_PER_HOST = HttpSettings.class.getSimpleName() + "@"
			+ "max_connections_per_host";

	@Setting( name = "Max Total Connections", description = "Maximum number of Total Connections" )
	public static final String MAX_TOTAL_CONNECTIONS = HttpSettings.class.getSimpleName() + "@"
			+ "max_total_connections";

	@Setting( name = "Leave MockEngine", description = "Leave MockEngine running when stopping MockServices", type = SettingType.BOOLEAN )
	public static final String LEAVE_MOCKENGINE = HttpSettings.class.getSimpleName() + "@" + "leave_mockengine";

	@Setting( name = "Enable Mock HTTP Log", description = "Logs wire content of all mock requests", type = SettingType.BOOLEAN )
	public static final String ENABLE_MOCK_WIRE_LOG = HttpSettings.class.getSimpleName() + "@" + "enable_mock_wire_log";

}
