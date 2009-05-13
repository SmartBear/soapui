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

package com.eviware.soapui.actions;

import java.util.TreeMap;

import com.eviware.soapui.impl.wsdl.support.CompressionSupport;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.settings.HttpSettings;
import com.eviware.soapui.support.components.SimpleForm;
import com.eviware.soapui.support.types.StringToStringMap;

/**
 * Preferences class for HttpSettings
 * 
 * @author ole.matzura
 */

public class HttpPrefs implements Prefs
{
	public static final String AUTHENTICATE_PREEMPTIVELY = "Authenticate Preemptively";
	public static final String INCLUDE_REQUEST_IN_TIME_TAKEN = "Include request in time taken";
	public static final String INCLUDE_RESPONSE_IN_TIME_TAKEN = "Include response in time taken";
	public static final String REQUEST_COMPRESSION = "Request compression";
	public static final String RESPONSE_COMPRESSION = "Response compression";
	public static final String CLOSE_CONNECTIONS_AFTER_REQUEST = "Close connections after request";
	public static final String USER_AGENT_HEADER = "User-Agent Header";
	public static final String SOCKET_TIMEOUT = "Socket Timeout";
	public static final String MAX_RESPONSE_SIZE = "Max response size";
	public static final String ENCODED_URLS = "Pre-encoded Endpoints";
	public static final String MAX_CONNECTIONS_PER_HOST = "Max Connections Per Host";
	public static final String MAX_TOTAL_CONNECTIONS = "Max Total Connections";
	public static final String BIND_ADDRESS = "Bind Address";
	public static final String LEAVE_MOCKENGINE = "Leave MockEngine";
	// public static final String DISABLE_CHUNKING = "Disable Chunking";
	public static final String CHUNKING_THRESHOLD = "Chunking Threshold";
	public static final String HTTP_VERSION = "HTTP Version";
	public static final String ENABLE_MOCK_WIRE_LOG = "Enable Mock HTTP Log";
	public static final String DISABLE_RESPONSE_DECOMPRESSION = "Disable Response Decompression";

	private static TreeMap<String, String> compressionAlgs = new TreeMap<String, String>();

	static
	{
		compressionAlgs.put( "None", "None" );
		compressionAlgs.put( CompressionSupport.ALG_GZIP, "GZIP" );
		compressionAlgs.put( CompressionSupport.ALG_DEFLATE, "DEFLATE" );
	}

	private SimpleForm httpForm;
	private final String title;

	public HttpPrefs( String title )
	{
		this.title = title;
	}

	public SimpleForm getForm()
	{
		if( httpForm == null )
		{
			httpForm = new SimpleForm();
			httpForm.addSpace( 5 );
			httpForm.appendComboBox( HttpPrefs.HTTP_VERSION, new String[] { HttpSettings.HTTP_VERSION_1_1,
					HttpSettings.HTTP_VERSION_1_0 }, "Select HTTP Version to use" );
			httpForm.appendTextField( HttpPrefs.USER_AGENT_HEADER,
					"User-Agent HTTP header to send, blank will send default" );
			httpForm.appendComboBox( HttpPrefs.REQUEST_COMPRESSION, compressionAlgs );
			httpForm.appendCheckBox( HttpPrefs.RESPONSE_COMPRESSION, "Accept compressed responses from hosts", true );
			httpForm.appendCheckBox( HttpPrefs.DISABLE_RESPONSE_DECOMPRESSION,
					"Disable decompression of compressed responses", true );
			httpForm.appendCheckBox( HttpPrefs.CLOSE_CONNECTIONS_AFTER_REQUEST,
					"Closes the HTTP connection after each HTTP request", true );
			// httpForm.appendCheckBox( HttpPrefs.DISABLE_CHUNKING,
			// "Disables content-chunking", true );
			httpForm.appendTextField( HttpPrefs.CHUNKING_THRESHOLD,
					"Uses content-chunking for requests larger than threshold, blank to disable" );
			httpForm.appendCheckBox( HttpPrefs.AUTHENTICATE_PREEMPTIVELY,
					"Adds authentication information to outgoing request", true );
			httpForm.appendCheckBox( HttpPrefs.ENCODED_URLS, "Do not URL-escape service endpoints", true );
			httpForm.appendTextField( HttpPrefs.BIND_ADDRESS, "Default local address to bind to when sending requests" );
			httpForm.appendSeparator();
			httpForm.appendCheckBox( HttpPrefs.INCLUDE_REQUEST_IN_TIME_TAKEN,
					"Includes the time it took to write the request in time-taken", true );
			httpForm.appendCheckBox( HttpPrefs.INCLUDE_RESPONSE_IN_TIME_TAKEN,
					"Includes the time it took to read the entire response in time-taken", true );
			httpForm.appendTextField( HttpPrefs.SOCKET_TIMEOUT, "Socket timeout in milliseconds" );
			httpForm.appendTextField( HttpPrefs.MAX_RESPONSE_SIZE, "Maximum size to read from response (0 = no limit)" );
			httpForm.appendTextField( HttpPrefs.MAX_CONNECTIONS_PER_HOST, "Maximum number of Connections Per Host" );
			httpForm.appendTextField( HttpPrefs.MAX_TOTAL_CONNECTIONS, "Maximum number of Total Connections" );
			httpForm.appendSeparator();
			httpForm.appendCheckBox( HttpPrefs.LEAVE_MOCKENGINE, "Leave MockEngine running when stopping MockServices",
					false );
			httpForm.appendCheckBox( HttpPrefs.ENABLE_MOCK_WIRE_LOG, "Logs wire content of all mock requests", false );
			httpForm.addSpace( 5 );
		}

		return httpForm;
	}

	public void getFormValues( Settings settings )
	{
		StringToStringMap httpValues = new StringToStringMap();
		httpForm.getValues( httpValues );
		storeValues( httpValues, settings );
	}

	public void storeValues( StringToStringMap httpValues, Settings settings )
	{
		settings.setString( HttpSettings.HTTP_VERSION, httpValues.get( HTTP_VERSION ) );
		// settings.setString( HttpSettings.DISABLE_CHUNKING, httpValues.get(
		// DISABLE_CHUNKING ) );
		settings.setString( HttpSettings.CHUNKING_THRESHOLD, httpValues.get( CHUNKING_THRESHOLD ) );
		settings.setString( HttpSettings.USER_AGENT, httpValues.get( USER_AGENT_HEADER ) );
		settings.setString( HttpSettings.REQUEST_COMPRESSION, httpValues.get( REQUEST_COMPRESSION ) );
		settings.setString( HttpSettings.RESPONSE_COMPRESSION, httpValues.get( RESPONSE_COMPRESSION ) );
		settings
				.setString( HttpSettings.DISABLE_RESPONSE_DECOMPRESSION, httpValues.get( DISABLE_RESPONSE_DECOMPRESSION ) );
		settings.setString( HttpSettings.CLOSE_CONNECTIONS, httpValues.get( CLOSE_CONNECTIONS_AFTER_REQUEST ) );
		settings.setString( HttpSettings.AUTHENTICATE_PREEMPTIVELY, httpValues.get( AUTHENTICATE_PREEMPTIVELY ) );
		settings.setString( HttpSettings.SOCKET_TIMEOUT, httpValues.get( SOCKET_TIMEOUT ) );
		settings.setString( HttpSettings.ENCODED_URLS, httpValues.get( ENCODED_URLS ) );
		settings.setString( HttpSettings.MAX_RESPONSE_SIZE, httpValues.get( MAX_RESPONSE_SIZE ) );
		settings.setString( HttpSettings.INCLUDE_REQUEST_IN_TIME_TAKEN, httpValues.get( INCLUDE_REQUEST_IN_TIME_TAKEN ) );
		settings
				.setString( HttpSettings.INCLUDE_RESPONSE_IN_TIME_TAKEN, httpValues.get( INCLUDE_RESPONSE_IN_TIME_TAKEN ) );
		settings.setString( HttpSettings.MAX_CONNECTIONS_PER_HOST, httpValues.get( MAX_CONNECTIONS_PER_HOST ) );
		settings.setString( HttpSettings.MAX_TOTAL_CONNECTIONS, httpValues.get( MAX_TOTAL_CONNECTIONS ) );
		settings.setString( HttpSettings.BIND_ADDRESS, httpValues.get( BIND_ADDRESS ) );
		settings.setString( HttpSettings.LEAVE_MOCKENGINE, httpValues.get( LEAVE_MOCKENGINE ) );
		settings.setString( HttpSettings.ENABLE_MOCK_WIRE_LOG, httpValues.get( ENABLE_MOCK_WIRE_LOG ) );
	}

	public void setFormValues( Settings settings )
	{
		getForm().setValues( getValues( settings ) );
	}

	public StringToStringMap getValues( Settings settings )
	{
		StringToStringMap httpValues = new StringToStringMap();
		httpValues.put( HTTP_VERSION, settings.getString( HttpSettings.HTTP_VERSION, HttpSettings.HTTP_VERSION_1_1 ) );
		// httpValues.put( DISABLE_CHUNKING, settings.getString(
		// HttpSettings.DISABLE_CHUNKING, null ) );
		httpValues.put( CHUNKING_THRESHOLD, settings.getString( HttpSettings.CHUNKING_THRESHOLD, null ) );
		httpValues.put( USER_AGENT_HEADER, settings.getString( HttpSettings.USER_AGENT, null ) );
		httpValues.put( REQUEST_COMPRESSION, compressionAlgs.get( settings.getString( HttpSettings.REQUEST_COMPRESSION,
				"None" ) ) );
		httpValues.put( RESPONSE_COMPRESSION, settings.getString( HttpSettings.RESPONSE_COMPRESSION, null ) );
		httpValues.put( DISABLE_RESPONSE_DECOMPRESSION, settings.getString( HttpSettings.DISABLE_RESPONSE_DECOMPRESSION,
				null ) );
		httpValues.put( CLOSE_CONNECTIONS_AFTER_REQUEST, settings.getString( HttpSettings.CLOSE_CONNECTIONS, null ) );
		httpValues.put( AUTHENTICATE_PREEMPTIVELY, settings.getString( HttpSettings.AUTHENTICATE_PREEMPTIVELY, null ) );
		httpValues.put( INCLUDE_REQUEST_IN_TIME_TAKEN, settings.getString( HttpSettings.INCLUDE_REQUEST_IN_TIME_TAKEN,
				null ) );
		httpValues.put( INCLUDE_RESPONSE_IN_TIME_TAKEN, settings.getString( HttpSettings.INCLUDE_RESPONSE_IN_TIME_TAKEN,
				null ) );
		httpValues.put( SOCKET_TIMEOUT, settings.getString( HttpSettings.SOCKET_TIMEOUT, null ) );
		httpValues.put( ENCODED_URLS, settings.getString( HttpSettings.ENCODED_URLS, null ) );
		httpValues.put( MAX_RESPONSE_SIZE, settings.getString( HttpSettings.MAX_RESPONSE_SIZE, "0" ) );
		httpValues.put( MAX_CONNECTIONS_PER_HOST, settings.getString( HttpSettings.MAX_CONNECTIONS_PER_HOST, "500" ) );
		httpValues.put( MAX_TOTAL_CONNECTIONS, settings.getString( HttpSettings.MAX_TOTAL_CONNECTIONS, "2000" ) );
		httpValues.put( BIND_ADDRESS, settings.getString( HttpSettings.BIND_ADDRESS, "" ) );
		httpValues.put( LEAVE_MOCKENGINE, settings.getString( HttpSettings.LEAVE_MOCKENGINE, null ) );
		httpValues.put( ENABLE_MOCK_WIRE_LOG, settings.getString( HttpSettings.ENABLE_MOCK_WIRE_LOG, null ) );
		return httpValues;
	}

	public String getTitle()
	{
		return title;
	}
}