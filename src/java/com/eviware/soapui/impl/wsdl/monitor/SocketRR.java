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
