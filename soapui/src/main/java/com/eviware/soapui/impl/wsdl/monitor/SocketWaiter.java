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

package com.eviware.soapui.impl.wsdl.monitor;

import java.net.ServerSocket;
import java.net.Socket;

import com.eviware.soapui.SoapUI;

/**
 * wait for incoming connections, spawn a connection thread when stuff comes in.
 */

class SocketWaiter extends Thread
{
	ServerSocket sSocket = null;
	SoapMonitor listener;
	int port;
	boolean pleaseStop = false;
	private SlowLinkSimulator slowLink;

	public SocketWaiter( String name, SoapMonitor l, int p )
	{
		super( name );
		listener = l;
		port = p;
		this.slowLink = new SlowLinkSimulator( 0, 0 );
		start();
	}

	/**
	 * Method run
	 */
	public void run()
	{
		try
		{
			sSocket = new ServerSocket( port );
			for( ;; )
			{
				Socket inSocket = sSocket.accept();
				if( pleaseStop )
				{
					break;
				}
				new Connection( getName() + " connection from " + inSocket.getRemoteSocketAddress(), listener, inSocket,
						slowLink );
				inSocket = null;
			}
		}
		catch( Exception exp )
		{
			if( !"socket closed".equals( exp.getMessage() ) )
			{
				listener.stop();
			}

			SoapUI.log.info( "Error stopping SocketWaiter: " + exp.toString() );
		}
	}

	/**
	 * force a halt by connecting to self and then closing the server socket
	 */
	public void halt()
	{
		try
		{
			pleaseStop = true;
			new Socket( "127.0.0.1", port );
			if( sSocket != null )
			{
				sSocket.close();
			}
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}
}
