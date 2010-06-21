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

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.Socket;
import java.net.URL;

import com.eviware.soapui.SoapUI;

/**
 * a connection listens to a single current connection
 */
class Connection extends Thread
{

	private SoapMonitor monitor;
	private boolean active;

	private Socket inSocket = null;
	private Socket outSocket = null;

	private SocketRR rr1 = null;
	private SocketRR rr2 = null;

	private InputStream inputStream = null;

	private String httpProxyHost = null;
	private int httpProxyPort = 80;
	private SlowLinkSimulator slowLink;

	/**
	 * Constructor Connection
	 * 
	 * @param l
	 */
	public Connection( String name, SoapMonitor l, SlowLinkSimulator slowLink )
	{
		super( name );
		monitor = l;
		httpProxyHost = l.getHttpProxyHost();
		httpProxyPort = l.getHttpProxyPort();
		this.slowLink = slowLink;
	}

	/**
	 * Constructor Connection
	 * 
	 * @param l
	 * @param s
	 */
	public Connection( String name, SoapMonitor l, Socket s, SlowLinkSimulator slowLink )
	{
		this( name, l, slowLink );
		inSocket = s;
		start();
	}

	/**
	 * Constructor Connection
	 * 
	 * @param l
	 * @param in
	 */
	public Connection( String name, SoapMonitor l, InputStream in, SlowLinkSimulator slowLink )
	{
		this( name, l, slowLink );
		inputStream = in;
		start();
	}

	/**
	 * Method run
	 */
	public void run()
	{
		try
		{
			active = true;
			httpProxyHost = System.getProperty( "http.proxyHost" );
			if( ( httpProxyHost != null ) && httpProxyHost.equals( "" ) )
			{
				httpProxyHost = null;
			}
			if( httpProxyHost != null )
			{
				String tmp = System.getProperty( "http.proxyPort" );
				if( ( tmp != null ) && tmp.equals( "" ) )
				{
					tmp = null;
				}
				if( tmp == null )
				{
					httpProxyPort = 80;
				}
				else
				{
					httpProxyPort = Integer.parseInt( tmp );
				}
			}

			String fromHost = "";
			if( inSocket != null )
			{
				fromHost = ( inSocket.getInetAddress() ).getHostName();
			}

			String targetHost = monitor.getTargetHost();
			int targetPort = monitor.getTargetPort();
			int listenPort = monitor.getLocalPort();
			InputStream tmpIn1 = inputStream;
			OutputStream tmpOut1 = null;
			InputStream tmpIn2 = null;
			OutputStream tmpOut2 = null;
			if( tmpIn1 == null )
			{
				tmpIn1 = inSocket.getInputStream();
			}
			if( inSocket != null )
			{
				tmpOut1 = inSocket.getOutputStream();
			}

			CaptureInputStream requestCapture = new CaptureInputStream( tmpIn1 );
			tmpIn1 = requestCapture;

			String bufferedData = null;
			StringBuffer buf = null;

			TcpMonWsdlMonitorMessageExchange exchange = new TcpMonWsdlMonitorMessageExchange( monitor.getProject() );
			exchange.setRequestHost( fromHost );

			// this is just temp, this class will be removed soon.
			// boolean isProxy = monitor.isProxy();
			boolean isProxy = true;
			URL targetUrl = isProxy ? null : new URL( monitor.getTargetEndpoint() );

			if( isProxy || ( httpProxyHost != null ) )
			{
				// Check if we're a proxy
				byte[] b = new byte[1];
				buf = new StringBuffer();
				String s;
				for( ;; )
				{
					int len;
					len = tmpIn1.read( b, 0, 1 );
					if( len == -1 )
					{
						break;
					}
					s = new String( b );
					buf.append( s );
					if( b[0] != '\n' )
					{
						continue;
					}
					break;
				}
				bufferedData = buf.toString();
				if( bufferedData.startsWith( "GET " ) || bufferedData.startsWith( "POST " )
						|| bufferedData.startsWith( "PUT " ) || bufferedData.startsWith( "DELETE " ) )
				{
					int start, end;

					start = bufferedData.indexOf( ' ' ) + 1;
					while( bufferedData.charAt( start ) == ' ' )
					{
						start++ ;
					}
					end = bufferedData.indexOf( ' ', start );
					String urlString = bufferedData.substring( start, end );
					if( urlString.charAt( 0 ) == '/' )
					{
						urlString = urlString.substring( 1 );
					}
					if( isProxy )
					{
						targetUrl = new URL( urlString );
						targetHost = targetUrl.getHost();
						targetPort = targetUrl.getPort();
						if( targetPort == -1 )
						{
							targetPort = 80;
						}

						bufferedData = bufferedData.substring( 0, start ) + targetUrl.getFile()
								+ bufferedData.substring( end );
						bufferedData += "Connection: close\r\n";
					}
					else
					{
						targetUrl = new URL( "http://" + targetHost + ":" + targetPort + "/" + urlString );
						bufferedData = bufferedData.substring( 0, start ) + targetUrl.toExternalForm()
								+ bufferedData.substring( end );
						targetHost = httpProxyHost;
						targetPort = httpProxyPort;
					}
				}
			}
			else
			{
				// 
				// Change Host: header to point to correct host
				// 
				byte[] b1 = new byte[1];
				buf = new StringBuffer();
				String s1;
				String lastLine = null;
				for( ;; )
				{
					int len;
					len = tmpIn1.read( b1, 0, 1 );
					if( len == -1 )
					{
						break;
					}
					s1 = new String( b1 );
					buf.append( s1 );
					if( b1[0] != '\n' )
					{
						continue;
					}

					// we have a complete line
					String line = buf.toString();
					buf.setLength( 0 );

					// check to see if we have found Host: header
					if( line.startsWith( "Host: " ) )
					{
						// we need to update the hostname to target host
						String newHost = "Host: " + targetHost + ":" + listenPort + "\r\n";
						bufferedData = bufferedData.concat( newHost );
						bufferedData += "Connection: close\r\n";
						break;
					}

					// failsafe
					if( line.equals( "\r\n" ) || ( "\n".equals( lastLine ) && line.equals( "\n" ) ) )
					{
						bufferedData += "Connection: close" + line;
						break;
					}

					// add it to our headers so far
					if( bufferedData == null )
					{
						bufferedData = line;
					}
					else
					{
						bufferedData = bufferedData.concat( line );
					}

					lastLine = line;
				}

				// if( bufferedData != null )
				// {
				// int idx = ( bufferedData.length() < 50 ) ? bufferedData.length()
				// : 50;
				// s1 = bufferedData.substring( 0, idx );
				// int i = s1.indexOf( '\n' );
				// if( i > 0 )
				// {
				// s1 = s1.substring( 0, i - 1 );
				// }
				// s1 = s1 + "                           " +
				// "                       ";
				// s1 = s1.substring( 0, 51 );
				// }
			}
			if( targetPort == -1 )
			{
				targetPort = 80;
			}

			exchange.setTargetUrl( targetUrl );

			outSocket = new Socket( targetHost, targetPort );
			tmpIn2 = outSocket.getInputStream();

			CaptureInputStream responseCapture = new CaptureInputStream( tmpIn2 );
			tmpIn2 = responseCapture;

			tmpOut2 = outSocket.getOutputStream();
			if( bufferedData != null )
			{
				byte[] b = bufferedData.getBytes();
				tmpOut2.write( b );
				slowLink.pump( b.length );
			}

			// this is the channel to the endpoint
			rr1 = new SocketRR( getName() + " to endpoint", this, inSocket, tmpIn1, outSocket, tmpOut2, slowLink );

			// create the response slow link from the inbound slow link
			SlowLinkSimulator responseLink = new SlowLinkSimulator( slowLink );

			// this is the channel from the endpoint
			rr2 = new SocketRR( getName() + " from endpoint", this, outSocket, tmpIn2, inSocket, tmpOut1, responseLink );

			while( ( rr1 != null ) || ( rr2 != null ) )
			{
				if( rr2 != null )
				{
					exchange.setTimeTaken( rr2.getElapsed() );
				}

				// Only loop as long as the connection to the target
				// machine is available - once that's gone we can stop.
				// The old way, loop until both are closed, left us
				// looping forever since no one closed the 1st one.

				if( ( null != rr1 ) && rr1.isDone() )
				{
					rr1 = null;
				}

				if( ( null != rr2 ) && rr2.isDone() )
				{
					rr2 = null;
				}

				synchronized( this )
				{
					this.wait( 10 ); // Safety just incase we're not told to wake
					// up.
				}
			}

			active = false;
			exchange.finish( requestCapture.getCapturedData(), responseCapture.getCapturedData() );
			monitor.addMessageExchange( exchange );
		}
		catch( Exception e )
		{
			StringWriter st = new StringWriter();
			PrintWriter wr = new PrintWriter( st );
			e.printStackTrace( wr );
			wr.close();
			halt();
		}
	}

	protected boolean isActive()
	{
		return active;
	}

	/**
	 * Method wakeUp
	 */
	synchronized void wakeUp()
	{
		this.notifyAll();
	}

	/**
	 * Method halt
	 */
	public void halt()
	{
		try
		{
			if( rr1 != null )
			{
				rr1.halt();
			}
			if( rr2 != null )
			{
				rr2.halt();
			}
			if( inSocket != null )
			{
				inSocket.close();
			}
			inSocket = null;
			if( outSocket != null )
			{
				outSocket.close();
			}
			outSocket = null;
		}
		catch( Exception e )
		{
			SoapUI.log.info( "Error halting connection: " + e.toString() );
		}
	}

	/**
	 * Method remove
	 */
	public void remove()
	{
		try
		{
			halt();
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}
}
