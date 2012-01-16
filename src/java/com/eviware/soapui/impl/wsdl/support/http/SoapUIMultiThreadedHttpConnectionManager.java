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
import java.util.concurrent.TimeUnit;

import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.ClientConnectionOperator;
import org.apache.http.conn.ClientConnectionRequest;
import org.apache.http.conn.ConnectionPoolTimeoutException;
import org.apache.http.conn.ManagedClientConnection;
import org.apache.http.conn.OperatedClientConnection;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.conn.AbstractPoolEntry;
import org.apache.http.impl.conn.DefaultClientConnection;
import org.apache.http.impl.conn.DefaultClientConnectionOperator;
import org.apache.http.impl.conn.tsccm.BasicPoolEntry;
import org.apache.http.impl.conn.tsccm.BasicPooledConnAdapter;
import org.apache.http.impl.conn.tsccm.PoolEntryRequest;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.log4j.Logger;

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
	}

	private class SoapUIDefaultClientConnection extends DefaultClientConnection
	{

		public SoapUIDefaultClientConnection()
		{
			super();
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
