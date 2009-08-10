/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
