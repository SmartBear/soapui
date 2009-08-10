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

package com.eviware.soapui.impl.wsdl.monitor;

import java.util.Vector;

import com.eviware.soapui.SoapUI;

public class TcpMonMonitorEngine implements SoapMonitorEngine
{
	private SocketWaiter sw;
	private final Vector connections = new Vector();
	private int localPort;

	public TcpMonMonitorEngine()
	{
	}

	public void start( SoapMonitor monitor, int localPort )
	{
		this.localPort = localPort;
		sw = new SocketWaiter( "Monitor on port " + localPort, monitor, localPort );
	}

	public void stop()
	{
		if( sw.isAlive() )
		{
			try
			{
				for( int i = 0; i < connections.size(); i++ )
				{
					Connection conn = ( Connection )connections.get( i );
					conn.halt();
				}
				sw.halt();
			}
			catch( Throwable e )
			{
				SoapUI.log.info( "Error stopping monitor: " + e.toString() );
			}

			SoapUI.log.info( "Stopped SOAP Monitor on local port " + getLocalPort() );
		}
	}

	public int getLocalPort()
	{
		return localPort;
	}

	public boolean isRunning()
	{
		return sw != null && sw.isAlive();
	}

	public boolean isProxy()
	{
		return true;
	}
}
