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

package com.eviware.soapui.impl.wsdl.panels.teststeps.amf;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;

import com.eviware.soapui.impl.wsdl.submit.transports.http.support.methods.ExtendedPostMethod;
import com.eviware.soapui.impl.wsdl.support.http.HttpClientSupport;

import flex.messaging.io.ClassAliasRegistry;
import flex.messaging.io.MessageDeserializer;
import flex.messaging.io.MessageIOConstants;
import flex.messaging.io.SerializationContext;
import flex.messaging.io.amf.ActionContext;
import flex.messaging.io.amf.ActionMessage;
import flex.messaging.io.amf.AmfMessageDeserializer;
import flex.messaging.io.amf.AmfMessageSerializer;
import flex.messaging.io.amf.MessageBody;
import flex.messaging.io.amf.MessageHeader;
import flex.messaging.io.amf.client.AMFHeaderProcessor;
import flex.messaging.io.amf.client.exceptions.ClientStatusException;
import flex.messaging.io.amf.client.exceptions.ServerStatusException;
import flex.messaging.io.amf.client.exceptions.ServerStatusException.HttpResponseInfo;

/**
 * A Java alternative to the native flash.net.NetConnection class for sending
 * AMF formatted requests over HTTP or HTTPS based on Peter Farland's.
 * AMFConnection in Actionscript. AMF connection automatically handles cookies
 * by looking for cookie headers and setting the cookies in subsequent request.
 * 
 * AMF connection class is not thread safe.
 */
public class SoapUIAMFConnection
{
	// --------------------------------------------------------------------------
	//
	// Public Static Variables
	//
	// --------------------------------------------------------------------------

	public static final String COOKIE = "Cookie";
	public static final String COOKIE2 = "Cookie2";
	public static final String COOKIE_SEPERATOR = ";";
	public static final String COOKIE_NAMEVALUE_SEPERATOR = "=";
	public static final String SET_COOKIE = "Set-Cookie";
	public static final String SET_COOKIE2 = "Set-Cookie2";

	// --------------------------------------------------------------------------
	//
	// Private Static Variables
	//
	// --------------------------------------------------------------------------

	private static int DEFAULT_OBJECT_ENCODING = MessageIOConstants.AMF3;

	// --------------------------------------------------------------------------
	//
	// Public Static Methods
	//
	// --------------------------------------------------------------------------

	/**
	 * Registers a custom alias for a class name bidirectionally.
	 * 
	 * @param alias
	 *           The alias for the class name.
	 * @param className
	 *           The concrete class name.
	 */
	public static void registerAlias( String alias, String className )
	{
		ClassAliasRegistry registry = ClassAliasRegistry.getRegistry();
		registry.registerAlias( alias, className );
		registry.registerAlias( className, alias );
	}

	// --------------------------------------------------------------------------
	//
	// Constructor
	//
	// --------------------------------------------------------------------------

	/**
	 * Creates a default AMF connection instance.
	 */
	public SoapUIAMFConnection()
	{
	}

	// --------------------------------------------------------------------------
	//
	// Private Variables
	//
	// --------------------------------------------------------------------------

	private ActionContext actionContext;
	private boolean connected;
	private boolean instantiateTypes = true;
	private int objectEncoding;
	private boolean objectEncodingSet = false;
	private SerializationContext serializationContext;
	private String url;

	// --------------------------------------------------------------------------
	//
	// Protected Variables
	//
	// --------------------------------------------------------------------------

	/**
	 * List of AMF message headers.
	 */
	protected List<MessageHeader> amfHeaders;

	/**
	 * An AMF connection may have an AMF header processor where AMF headers can
	 * be passed to as they are encountered in AMF response messages.
	 */
	protected AMFHeaderProcessor amfHeaderProcessor;

	/**
	 * A map of cookie names and values that are used to keep track of cookies.
	 */
	// protected Map<String, String> cookies;

	/**
	 * Map of Http request header names and values.
	 */
	protected Map<String, String> httpRequestHeaders;

	/**
	 * Sequentially incremented counter used to generate a unique responseURI to
	 * match response messages to responders.
	 */
	protected int responseCounter;

	/**
	 * The URL connection used to make AMF formatted HTTP and HTTPS requests for
	 * this connection.
	 */
	protected ExtendedPostMethod postMethod;
	private HttpState httpState = new HttpState();

	// --------------------------------------------------------------------------
	//
	// Properties
	//
	// --------------------------------------------------------------------------

	// ----------------------------------
	// amfHeaderProcessor
	// ----------------------------------

	/**
	 * Returns the AMF header processor associated with the AMF connection. AMF
	 * header processor is same as NetConnection's client property. See
	 * flash.net.NetConnection#client.
	 * 
	 * @return The AMF header processor associated with the AMF connection.
	 */
	public AMFHeaderProcessor getAMFHeaderProcessor()
	{
		return amfHeaderProcessor;
	}

	/**
	 * Sets the AMF header processor associated with the AMF connection.
	 * 
	 * @param amfHeaderProcessor
	 *           The AMF header processor to set.
	 */
	public void setAMFHeaderProcessor( AMFHeaderProcessor amfHeaderProcessor )
	{
		this.amfHeaderProcessor = amfHeaderProcessor;
	}

	// ----------------------------------
	// defaultObjectEncoding
	// ----------------------------------

	/**
	 * The default object encoding for all AMFConnection instances. This controls
	 * which version of AMF is used during serialization. The default is AMF 3.
	 * See flash.net.ObjectEncoding#DEFAULT
	 * 
	 * @return The default object encoding of the AMF connection.
	 */
	public static int getDefaultObjectEncoding()
	{
		return DEFAULT_OBJECT_ENCODING;
	}

	/**
	 * Sets the default object encoding of the AMF connection.
	 * 
	 * @param value
	 *           The value to set the default object encoding to.
	 */
	public static void setDefaultObjectEncoding( int value )
	{
		DEFAULT_OBJECT_ENCODING = value;
	}

	// ----------------------------------
	// instantiateTypes
	// ----------------------------------

	/**
	 * Returns instantiateTypes property. InstantiateTypes property determines
	 * whether type information will be used to instantiate a new instance. If
	 * set to false, types will be deserialized as flex.messaging.io.ASObject
	 * instances with type information retained but not used to create an
	 * instance. Note that types in the flex.* package (and any subpackage) will
	 * always be instantiated. The default is true.
	 * 
	 * @return The instantitateTypes property.
	 */
	public boolean isInstantiateTypes()
	{
		return instantiateTypes;
	}

	/**
	 * Sets the instantiateTypes property.
	 * 
	 * @param instantiateTypes
	 *           The value to set the instantiateTypes property to.
	 */
	public void setInstantiateTypes( boolean instantiateTypes )
	{
		this.instantiateTypes = instantiateTypes;
	}

	// ----------------------------------
	// objectEncoding
	// ----------------------------------

	/**
	 * The object encoding for this AMFConnection sets which AMF version to use
	 * during serialization. If set, this version overrides the
	 * defaultObjectEncoding.
	 * 
	 * @return The object encoding for the AMF connection.
	 */
	public int getObjectEncoding()
	{
		if( !objectEncodingSet )
			return getDefaultObjectEncoding();
		return objectEncoding;
	}

	/**
	 * Sets the object encoding for the AMF connection.
	 * 
	 * @param objectEncoding
	 *           The value to set the object encoding to.
	 */
	public void setObjectEncoding( int objectEncoding )
	{
		this.objectEncoding = objectEncoding;
		objectEncodingSet = true;
	}

	// ----------------------------------
	// url
	// ----------------------------------

	/**
	 * Returns the HTTP or HTTPS url for the AMF connection.
	 * 
	 * @return The HTTP or HTTPs url for the AMF connection.
	 */
	public String getUrl()
	{
		return url;
	}

	// --------------------------------------------------------------------------
	//
	// Public Methods
	//
	// --------------------------------------------------------------------------

	/**
	 * Adds an AMF packet-level header which is sent with every request for the
	 * life of this AMF connection.
	 * 
	 * @param name
	 *           The name of the header.
	 * @param mustUnderstand
	 *           Whether the header must be processed or not.
	 * @param data
	 *           The value of the header.
	 */
	public void addAmfHeader( String name, boolean mustUnderstand, Object data )
	{
		if( amfHeaders == null )
			amfHeaders = new ArrayList<MessageHeader>();

		MessageHeader header = new MessageHeader( name, mustUnderstand, data );
		amfHeaders.add( header );
	}

	/**
	 * Add an AMF packet-level header with mustUnderstand=false, which is sent
	 * with every request for the life of this AMF connection.
	 * 
	 * @param name
	 *           The name of the header.
	 * @param data
	 *           The value of the header.
	 */
	public void addAmfHeader( String name, Object data )
	{
		addAmfHeader( name, false, data );
	}

	/**
	 * Removes any AMF headers found with the name given.
	 * 
	 * @param name
	 *           The name of the header(s) to remove.
	 * 
	 * @return true if a header existed with the given name.
	 */
	public boolean removeAmfHeader( String name )
	{
		boolean exists = false;
		if( amfHeaders != null )
		{
			for( Iterator<MessageHeader> iterator = amfHeaders.iterator(); iterator.hasNext(); )
			{
				MessageHeader header = iterator.next();
				if( name.equals( header.getName() ) )
				{
					iterator.remove();
					exists = true;
				}
			}
		}
		return exists;
	}

	/**
	 * Removes all AMF headers.
	 */
	public void removeAllAmfHeaders()
	{
		if( amfHeaders != null )
			amfHeaders = null;
	}

	/**
	 * Adds a Http request header to the underlying connection.
	 * 
	 * @param name
	 *           The name of the Http header.
	 * @param value
	 *           The value of the Http header.
	 */
	public void addHttpRequestHeader( String name, String value )
	{
		if( httpRequestHeaders == null )
			httpRequestHeaders = new HashMap<String, String>();

		httpRequestHeaders.put( name, value );
	}

	/**
	 * Removes the Http header found with the name given.
	 * 
	 * @param name
	 *           The name of the Http header.
	 * 
	 * @return true if a header existed with the given name.
	 */
	public boolean removeHttpRequestHeader( String name )
	{
		boolean exists = false;
		if( httpRequestHeaders != null )
		{
			Object previousValue = httpRequestHeaders.remove( name );
			exists = ( previousValue != null );
		}
		return exists;
	}

	/**
	 * Removes all Http request headers.
	 */
	public void removeAllHttpRequestHeaders()
	{
		if( httpRequestHeaders != null )
			httpRequestHeaders = null;
	}

	/**
	 * Makes an AMF request to the server. A connection must have been made prior
	 * to making a call.
	 * 
	 * @param command
	 *           The method to call on the server.
	 * @param arguments
	 *           Arguments for the method.
	 * 
	 * @return The result of the call.
	 * 
	 * @throws ClientStatusException
	 *            If there is a client side exception.
	 * @throws ServerStatusException
	 *            If there is a server side exception.
	 */
	public Object call( String command, Object... arguments ) throws ClientStatusException, ServerStatusException
	{
		if( !connected )
		{
			String message = "AMF connection is not connected";
			ClientStatusException cse = new ClientStatusException( message, ClientStatusException.AMF_CALL_FAILED_CODE );
			throw cse;
		}

		String responseURI = getResponseURI();

		ActionMessage requestMessage = new ActionMessage( getObjectEncoding() );

		if( amfHeaders != null )
		{
			for( MessageHeader header : amfHeaders )
				requestMessage.addHeader( header );
		}

		MessageBody amfMessage = new MessageBody( command, responseURI, arguments );
		requestMessage.addBody( amfMessage );

		// Setup for AMF message serializer
		actionContext.setRequestMessage( requestMessage );
		ByteArrayOutputStream outBuffer = new ByteArrayOutputStream();
		AmfMessageSerializer amfMessageSerializer = new AmfMessageSerializer();
		amfMessageSerializer.initialize( serializationContext, outBuffer, null/* debugTrace */);

		try
		{
			amfMessageSerializer.writeMessage( requestMessage );
			Object result = send( outBuffer );
			return result;
		}
		catch( Exception e )
		{
			if( e instanceof ClientStatusException )
				throw ( ClientStatusException )e;
			else if( e instanceof ServerStatusException )
				throw ( ServerStatusException )e;
			// Otherwise, wrap into a ClientStatusException.
			ClientStatusException exception = new ClientStatusException( e, ClientStatusException.AMF_CALL_FAILED_CODE );
			throw exception;
		}
		finally
		{
			try
			{
				outBuffer.close();
			}
			catch( IOException ignore )
			{
			}
		}
	}

	/**
	 * Closes the underlying URL connection, sets the url to null, and clears the
	 * cookies.
	 */
	public void close()
	{
		// Clear the URL connection and URL.
		if( postMethod != null )
		{
			postMethod.releaseConnection();
			postMethod = null;
		}
		url = null;

		serializationContext = null;
		connected = false;
	}

	/**
	 * Connects to the URL provided. Any previous connections are closed.
	 * 
	 * @param url
	 *           The url to connect to.
	 * 
	 * @throws ClientStatusException
	 *            If there is a client side exception.
	 */
	public void connect( String url ) throws ClientStatusException
	{
		if( connected )
			close();

		this.url = url;
		try
		{
			serializationContext = new SerializationContext();
			serializationContext.createASObjectForMissingType = true;
			internalConnect();
		}
		catch( IOException e )
		{
			ClientStatusException exception = new ClientStatusException( e, ClientStatusException.AMF_CONNECT_FAILED_CODE );
			throw exception;
		}
	}

	// --------------------------------------------------------------------------
	//
	// Protected Methods
	//
	// --------------------------------------------------------------------------

	/**
	 * Generates the HTTP response info for the server status exception.
	 * 
	 * @return The HTTP response info for the server status exception.
	 */
	protected HttpResponseInfo generateHttpResponseInfo()
	{
		HttpResponseInfo httpResponseInfo = null;
		try
		{
			int responseCode = postMethod.getStatusCode();
			String responseMessage = postMethod.getResponseBodyAsString();
			httpResponseInfo = new HttpResponseInfo( responseCode, responseMessage );
		}
		catch( IOException ignore )
		{
		}
		return httpResponseInfo;
	}

	/**
	 * Generates and returns the response URI.
	 * 
	 * @return The response URI.
	 */
	protected String getResponseURI()
	{
		String responseURI = "/" + responseCounter;
		responseCounter++ ;
		return responseURI;
	}

	/**
	 * An internal method that sets up the underlying URL connection.
	 * 
	 * @throws IOException
	 *            If an exception is encountered during URL connection setup.
	 */
	protected void internalConnect() throws IOException
	{
		serializationContext.instantiateTypes = instantiateTypes;
		postMethod = new ExtendedPostMethod( url );
		setHttpRequestHeaders();
		actionContext = new ActionContext();
		connected = true;
	}

	/**
	 * Processes the HTTP response headers and body.
	 */
	protected Object processHttpResponse( InputStream inputStream ) throws ClassNotFoundException, IOException,
			ClientStatusException, ServerStatusException
	{
		return processHttpResponseBody( inputStream );
	}

	/**
	 * Processes the HTTP response body.
	 */
	protected Object processHttpResponseBody( InputStream inputStream ) throws ClassNotFoundException, IOException,
			ClientStatusException, ServerStatusException
	{
		DataInputStream din = new DataInputStream( inputStream );
		ActionMessage message = new ActionMessage();
		actionContext.setRequestMessage( message );
		MessageDeserializer deserializer = new AmfMessageDeserializer();
		deserializer.initialize( serializationContext, din, null/* trace */);
		deserializer.readMessage( message, actionContext );
		din.close();
		return processAmfPacket( message );
	}

	/**
	 * Processes the AMF packet.
	 */
	@SuppressWarnings( "unchecked" )
	protected Object processAmfPacket( ActionMessage packet ) throws ClientStatusException, ServerStatusException
	{
		processAmfHeaders( packet.getHeaders() );
		return processAmfBody( packet.getBodies() );
	}

	/**
	 * Processes the AMF headers by dispatching them to an AMF header processor,
	 * if one exists.
	 */
	protected void processAmfHeaders( ArrayList<MessageHeader> headers ) throws ClientStatusException
	{
		// No need to process headers if there's no AMF header processor.
		if( amfHeaderProcessor == null )
			return;

		for( MessageHeader header : headers )
			amfHeaderProcessor.processHeader( header );
	}

	/**
	 * Processes the AMF body. Note that this method won't work if batching of
	 * AMF messages is supported at some point but for now we are guaranteed to
	 * have a single message.
	 */
	protected Object processAmfBody( ArrayList<MessageBody> messages ) throws ServerStatusException
	{
		for( MessageBody message : messages )
		{
			String targetURI = message.getTargetURI();

			if( targetURI.endsWith( MessageIOConstants.RESULT_METHOD ) )
			{
				return message.getData();
			}
			else if( targetURI.endsWith( MessageIOConstants.STATUS_METHOD ) )
			{
				String exMessage = "Server error";
				HttpResponseInfo responseInfo = generateHttpResponseInfo();
				ServerStatusException exception = new ServerStatusException( exMessage, message.getData(), responseInfo );
				throw exception;
			}
		}
		return null; // Should not happen.
	}

	/**
	 * Writes the output buffer and processes the HTTP response.
	 */
	protected Object send( ByteArrayOutputStream outBuffer ) throws ClassNotFoundException, IOException,
			ClientStatusException, ServerStatusException
	{
		// internalConnect.
		internalConnect();

		postMethod.setRequestEntity( new ByteArrayRequestEntity( outBuffer.toByteArray() ) );
		HttpClientSupport.getHttpClient().executeMethod( new HostConfiguration(), postMethod, httpState );

		// Process the response
		return processHttpResponse( postMethod.getResponseBodyAsStream() );
	}

	/**
	 * Sets the Http request headers, including the cookie headers.
	 */
	protected void setHttpRequestHeaders()
	{
		if( httpRequestHeaders != null )
		{
			for( Map.Entry<String, String> element : httpRequestHeaders.entrySet() )
			{
				String key = element.getKey();
				String value = element.getValue();
				postMethod.setRequestHeader( key, value );
			}
		}
	}
}
