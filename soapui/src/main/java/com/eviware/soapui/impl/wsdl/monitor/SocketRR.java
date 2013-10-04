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

package com.eviware.soapui.impl.wsdl.monitor;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import com.eviware.soapui.SoapUI;

/**
 * this class handles the pumping of data from the incoming socket to the
 * outgoing socket
 */
class SocketRR extends Thread
{

	Socket inSocket = null;
	Socket outSocket = null;

	InputStream in = null;
	OutputStream out = null;

	volatile boolean done = false;
	volatile long elapsed = 0;

	Connection myConnection = null;
	SlowLinkSimulator slowLink;

	public SocketRR( String name, Connection c, Socket inputSocket, InputStream inputStream, Socket outputSocket,
			OutputStream outputStream, SlowLinkSimulator slowLink )
	{
		super( name );

		inSocket = inputSocket;
		in = inputStream;
		outSocket = outputSocket;
		out = outputStream;
		myConnection = c;
		this.slowLink = slowLink;
		start();
	}

	/**
	 * Method isDone
	 * 
	 * @return boolean
	 */
	public boolean isDone()
	{
		return done;
	}

	public long getElapsed()
	{
		return elapsed;
	}

	/**
	 * Method run
	 */
	public void run()
	{
		try
		{
			byte[] buffer = new byte[4096];
			int saved = 0;
			int len;
			long start = System.currentTimeMillis();
			a : for( ;; )
			{

				elapsed = System.currentTimeMillis() - start;

				if( done )
				{
					break;
				}

				// try{
				// len = in.available();
				// }catch(Exception e){len=0;}
				len = buffer.length;

				// Used to be 1, but if we block it doesn't matter
				// however 1 will break with some servers, including apache
				if( len == 0 )
				{
					len = buffer.length;
				}
				if( saved + len > buffer.length )
				{
					len = buffer.length - saved;
				}
				int len1 = 0;
				while( len1 == 0 )
				{
					try
					{
						len1 = in.read( buffer, saved, len );
					}
					catch( Exception ex )
					{
						if( done && ( saved == 0 ) )
						{
							break a;
						}
						len1 = -1;
						break;
					}
				}
				len = len1;
				if( ( len == -1 ) && ( saved == 0 ) )
				{
					break;
				}
				if( len == -1 )
				{
					done = true;
				}

				if( ( out != null ) && ( len > 0 ) )
				{
					slowLink.pump( len );
					out.write( buffer, saved, len );
				}
			}

		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
		finally
		{
			done = true;
			try
			{
				if( out != null )
				{
					out.flush();
					if( null != outSocket )
					{
						outSocket.shutdownOutput();
					}
					else
					{
						out.close();
					}
					out = null;
				}
			}
			catch( Exception e )
			{
			}
			try
			{
				if( in != null )
				{
					if( inSocket != null )
					{
						inSocket.shutdownInput();
					}
					else
					{
						in.close();
					}
					in = null;
				}
			}
			catch( Exception e )
			{
			}
			myConnection.wakeUp();
		}
	}

	/**
	 * Method halt
	 */
	public void halt()
	{
		try
		{
			if( inSocket != null )
			{
				inSocket.close();
			}
			if( outSocket != null )
			{
				outSocket.close();
			}
			inSocket = null;
			outSocket = null;
			if( in != null )
			{
				in.close();
			}
			if( out != null )
			{
				out.close();
			}
			in = null;
			out = null;
			done = true;
		}
		catch( Exception e )
		{
			SoapUI.log.info( "Error halting socket: " + e.toString() );
		}
	}
}
