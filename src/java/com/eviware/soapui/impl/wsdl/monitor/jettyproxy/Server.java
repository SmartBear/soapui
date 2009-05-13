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
package com.eviware.soapui.impl.wsdl.monitor.jettyproxy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SocketChannel;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.Response;
import org.mortbay.util.IO;

import com.eviware.soapui.SoapUI;

public class Server extends org.mortbay.jetty.Server
{

	private Logger log = Logger.getLogger( Server.class );

	public Server()
	{
		super();
		if( SoapUI.getLogMonitor() == null || SoapUI.getLogMonitor().getLogArea( "jetty log" ) == null )
			return;
		SoapUI.getLogMonitor().getLogArea( "jetty log" ).addLogger( log.getName(), true );
	}

	@Override
	public void handle( final org.mortbay.jetty.HttpConnection connection ) throws IOException, ServletException
	{
		final Request request = connection.getRequest();

		if( !request.getMethod().equals( "CONNECT" ) )
		{
			super.handle( connection );
			return;
		}

		final String uri = request.getUri().toString();

		final int c = uri.indexOf( ':' );
		final String port = uri.substring( c + 1 );
		final String host = uri.substring( 0, c );

		final InetSocketAddress inetAddress = new InetSocketAddress( host, Integer.parseInt( port ) );

		final Socket clientSocket = connection.getEndPoint().getTransport() instanceof Socket ? ( Socket )connection
				.getEndPoint().getTransport() : ( ( SocketChannel )connection.getEndPoint().getTransport() ).socket();
		final InputStream in = clientSocket.getInputStream();
		final OutputStream out = clientSocket.getOutputStream();

		final SSLSocket socket = ( SSLSocket )SSLSocketFactory.getDefault().createSocket( inetAddress.getAddress(),
				inetAddress.getPort() );

		final Response response = connection.getResponse();
		response.setStatus( 200 );
		// response.setHeader("Connection", "close");
		response.flushBuffer();

		IO.copyThread( socket.getInputStream(), out );

		IO.copyThread( in, socket.getOutputStream() );

	}

}
