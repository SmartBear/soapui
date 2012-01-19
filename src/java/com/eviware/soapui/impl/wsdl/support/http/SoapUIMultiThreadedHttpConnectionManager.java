/*
 *  soapUI, copyright (C) 2004-2011 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.wsdl.support.http;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.ClientConnectionOperator;
import org.apache.http.conn.ClientConnectionRequest;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.ConnectionPoolTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.conn.ManagedClientConnection;
import org.apache.http.conn.OperatedClientConnection;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.conn.AbstractPoolEntry;
import org.apache.http.impl.conn.DefaultClientConnection;
import org.apache.http.impl.conn.DefaultClientConnectionOperator;
import org.apache.http.impl.conn.LoggingSessionInputBuffer;
import org.apache.http.impl.conn.LoggingSessionOutputBuffer;
import org.apache.http.impl.conn.Wire;
import org.apache.http.impl.conn.tsccm.BasicPoolEntry;
import org.apache.http.impl.conn.tsccm.BasicPooledConnAdapter;
import org.apache.http.impl.conn.tsccm.PoolEntryRequest;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.impl.io.SocketInputBuffer;
import org.apache.http.impl.io.SocketOutputBuffer;
import org.apache.http.io.SessionInputBuffer;
import org.apache.http.io.SessionOutputBuffer;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HttpContext;
import org.apache.log4j.Logger;

import com.eviware.soapui.impl.wsdl.submit.transports.http.support.metrics.HttpMetrics;

/**
 * Manages a set of HttpConnections for various HostConfigurations. Modified to
 * keep different pools for different keystores.
 * 
 */
public class SoapUIMultiThreadedHttpConnectionManager extends ThreadSafeClientConnManager
{

	/**
	 * Log object for this class.
	 */
	private static final Logger log = Logger.getLogger( SoapUIMultiThreadedHttpConnectionManager.class );

	/**
	 * Connection eviction policy
	 */
	IdleConnectionMonitorThread idleConnectionHandler = new IdleConnectionMonitorThread( this );

	public SoapUIMultiThreadedHttpConnectionManager( SchemeRegistry registry )
	{
		super( registry );
		idleConnectionHandler.start();
	}

	/**
	 * Hook for creating the connection operator. It is called by the
	 * constructor. Derived classes can override this method to change the
	 * instantiation of the operator. The default implementation here
	 * instantiates {@link DefaultClientConnectionOperator
	 * DefaultClientConnectionOperator}.
	 * 
	 * @param schreg
	 *           the scheme registry.
	 * 
	 * @return the connection operator to use
	 */
	@Override
	protected ClientConnectionOperator createConnectionOperator( SchemeRegistry schreg )
	{

		return new SoapUIClientConnectionOperator( schreg );// @ThreadSafe
	}

	public static class IdleConnectionMonitorThread extends Thread
	{
		private final ClientConnectionManager connMgr;
		private volatile boolean shutdown;

		public IdleConnectionMonitorThread( ClientConnectionManager connMgr )
		{
			super();
			this.connMgr = connMgr;
		}

		@Override
		public void run()
		{
			try
			{
				while( !shutdown )
				{
					synchronized( this )
					{
						wait( 5000 );
						// Close expired connections
						connMgr.closeExpiredConnections();
						// Optionally, close connections
						// that have been idle longer than 30 sec
						connMgr.closeIdleConnections( 30, TimeUnit.SECONDS );
					}
				}
			}
			catch( InterruptedException ex )
			{
				// terminate
			}
		}

		public void shutdown()
		{
			shutdown = true;
			synchronized( this )
			{
				notifyAll();
			}
		}
	}

	public ClientConnectionRequest requestConnection( final HttpRoute route, final Object state )
	{

		final PoolEntryRequest poolRequest = pool.requestPoolEntry( route, state );

		return new ClientConnectionRequest()
		{

			public void abortRequest()
			{
				poolRequest.abortRequest();
			}

			public ManagedClientConnection getConnection( long timeout, TimeUnit tunit ) throws InterruptedException,
					ConnectionPoolTimeoutException
			{
				if( route == null )
				{
					throw new IllegalArgumentException( "Route may not be null." );
				}

				if( log.isDebugEnabled() )
				{
					log.debug( "Get connection: " + route + ", timeout = " + timeout );
				}

				BasicPoolEntry entry = poolRequest.getPoolEntry( timeout, tunit );
				SoapUIBasicPooledConnAdapter connAdapter = new SoapUIBasicPooledConnAdapter(
						SoapUIMultiThreadedHttpConnectionManager.this, entry );
				return connAdapter;
			}
		};

	}

	public void releaseConnection( ManagedClientConnection conn, long validDuration, TimeUnit timeUnit )
	{

		if( !( conn instanceof SoapUIBasicPooledConnAdapter ) )
		{
			throw new IllegalArgumentException( "Connection class mismatch, "
					+ "connection not obtained from this manager." );
		}
		SoapUIBasicPooledConnAdapter hca = ( SoapUIBasicPooledConnAdapter )conn;
		if( ( hca.getPoolEntry() != null ) && ( hca.getManager() != this ) )
		{
			throw new IllegalArgumentException( "Connection not obtained from this manager." );
		}
		synchronized( hca )
		{
			BasicPoolEntry entry = ( BasicPoolEntry )hca.getPoolEntry();
			if( entry == null )
			{
				return;
			}
			try
			{
				// make sure that the response has been read completely
				if( hca.isOpen() && !hca.isMarkedReusable() )
				{
					// In MTHCM, there would be a call to
					// SimpleHttpConnectionManager.finishLastResponse(conn);
					// Consuming the response is handled outside in 4.0.

					// make sure this connection will not be re-used
					// Shut down rather than close, we might have gotten here
					// because of a shutdown trigger.
					// Shutdown of the adapter also clears the tracked route.
					hca.shutdown();
				}
			}
			catch( IOException iox )
			{
				if( log.isDebugEnabled() )
					log.debug( "Exception shutting down released connection.", iox );
			}
			finally
			{
				boolean reusable = hca.isMarkedReusable();
				if( log.isDebugEnabled() )
				{
					if( reusable )
					{
						log.debug( "Released connection is reusable." );
					}
					else
					{
						log.debug( "Released connection is not reusable." );
					}
				}
				hca.detach();
				pool.freeEntry( entry, reusable, validDuration, timeUnit );
			}
		}
	}

	private class SoapUIClientConnectionOperator extends DefaultClientConnectionOperator
	{

		public SoapUIClientConnectionOperator( SchemeRegistry schemes )
		{
			super( schemes );
		}

		@Override
		public OperatedClientConnection createConnection()
		{
			SoapUIDefaultClientConnection connection = new SoapUIDefaultClientConnection();
			return connection;
		}

		@Override
		protected InetAddress[] resolveHostname( final String host ) throws UnknownHostException
		{
			HttpMetrics.getDNSTimer().start();
			InetAddress[] inetAddress = InetAddress.getAllByName( host );
			HttpMetrics.getDNSTimer().stop();
			return inetAddress;
		}

		@Override
		public void openConnection( final OperatedClientConnection conn, final HttpHost target, final InetAddress local,
				final HttpContext context, final HttpParams params ) throws IOException
		{
			// probably can't be static
			HttpMetrics.getConnectTimer().start();
			try
			{
				super.openConnection( conn, target, local, context, params );
			}
			catch( HttpHostConnectException e )
			{
				HttpMetrics.getConnectTimer().reset();
				throw e;
			}
			catch( ConnectTimeoutException e )
			{
				HttpMetrics.getConnectTimer().reset();
				throw e;
			}
		}
	}

	private class SoapUIDefaultClientConnection extends DefaultClientConnection
	{

		private final Log wireLog = LogFactory.getLog( "org.apache.http.wire" );

		public SoapUIDefaultClientConnection()
		{
			super();
		}

		@Override
		public void openCompleted( boolean secure, HttpParams params ) throws IOException
		{
			super.openCompleted( secure, params );
			HttpMetrics.getConnectTimer().stop();
		}

		@Override
		protected SessionInputBuffer createSessionInputBuffer( final Socket socket, int buffersize,
				final HttpParams params ) throws IOException
		{
			if( buffersize == -1 )
			{
				buffersize = 8192;
			}
			SessionInputBuffer inbuffer = new SoapUISocketInputBuffer( socket, buffersize, params );
			if( wireLog.isDebugEnabled() )
			{
				inbuffer = new LoggingSessionInputBuffer( inbuffer, new Wire( wireLog ),
						HttpProtocolParams.getHttpElementCharset( params ) );
			}
			return inbuffer;
		}

		@Override
		protected SessionOutputBuffer createSessionOutputBuffer( final Socket socket, int buffersize,
				final HttpParams params ) throws IOException
		{
			if( buffersize == -1 )
			{
				buffersize = 8192;
			}
			SessionOutputBuffer outbuffer = new SocketOutputBuffer( socket, buffersize, params );
			if( wireLog.isDebugEnabled() )
			{
				outbuffer = new LoggingSessionOutputBuffer( outbuffer, new Wire( wireLog ),
						HttpProtocolParams.getHttpElementCharset( params ) );
			}
			return outbuffer;
		}

	}

	private class SoapUISocketInputBuffer extends SocketInputBuffer
	{

		public SoapUISocketInputBuffer( Socket socket, int buffersize, HttpParams params ) throws IOException
		{
			super( socket, buffersize, params );
		}

		@Override
		protected int fillBuffer() throws IOException
		{
			int l = super.fillBuffer();
			HttpMetrics.getTimeToFirstByteTimer().stop();
			return l;
		}

	}

	private class SoapUIBasicPooledConnAdapter extends BasicPooledConnAdapter
	{

		protected SoapUIBasicPooledConnAdapter( ThreadSafeClientConnManager tsccm, AbstractPoolEntry entry )
		{
			super( tsccm, entry );
		}

		@Override
		protected ClientConnectionManager getManager()
		{
			// override needed only to make method visible in this package
			return super.getManager();
		}

		@Override
		protected AbstractPoolEntry getPoolEntry()
		{
			// override needed only to make method visible in this package
			return super.getPoolEntry();
		}

		@Override
		protected void detach()
		{
			// override needed only to make method visible in this package
			super.detach();
		}
	}

}
