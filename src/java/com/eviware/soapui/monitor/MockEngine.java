/*
 *  soapUI, copyright (C) 2004-2008 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.monitor;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.AbstractHandler;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.security.SslSocketConnector;
import org.mortbay.thread.BoundedThreadPool;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.mock.DispatchException;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockResult;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockRunner;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockService;
import com.eviware.soapui.impl.wsdl.support.soap.SoapMessageBuilder;
import com.eviware.soapui.impl.wsdl.support.soap.SoapVersion;
import com.eviware.soapui.model.mock.MockRunner;
import com.eviware.soapui.model.mock.MockService;
import com.eviware.soapui.settings.HttpSettings;
import com.eviware.soapui.settings.SSLSettings;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.log.JettyLogger;

/**
 * Core Mock-Engine hosting a Jetty web server
 * 
 * @author ole.matzura
 */

public class MockEngine
{
	public final static Logger log = Logger.getLogger( MockEngine.class );
	
	private Server server;
	private Map<Integer, Map<String,List<MockRunner>>> runners = new HashMap<Integer, Map<String,List<MockRunner>>>();
	private Map<Integer, SoapUIConnector> connectors = new HashMap<Integer,SoapUIConnector>();
	private List<MockRunner> mockRunners = new ArrayList<MockRunner>();

	private SslSocketConnector sslConnector;
	
	public MockEngine()
	{
		System.setProperty( "org.mortbay.log.class", JettyLogger.class.getName() );
	}
	
	public boolean hasRunningMock( MockService mockService )
	{
		for( MockRunner runner : mockRunners )
			if( runner.getMockService() == mockService )
				return true;
		
		return false;
	}

	public synchronized void startMockService( MockRunner runner ) throws Exception
	{
		if( server == null )
			initServer();
		
		WsdlMockService mockService = ( WsdlMockService ) runner.getMockService();
		int port = mockService.getPort();
		
		if( !runners.containsKey( port ))
		{
			SoapUIConnector connector = new SoapUIConnector();
			
			connector.setPort( port );
			if( sslConnector != null )
				connector.setConfidentialPort( sslConnector.getPort() );
			
			if( mockService.getBindToHostOnly() )
			{
				String host = mockService.getHost();
				if( StringUtils.hasContent( host ))
				{
					connector.setHost( host );
				}
			}
			
			boolean wasRunning = server.isRunning();
			
			if( wasRunning )
			{
				server.stop();
			}
			
			server.addConnector( connector );
			try
			{
				server.start();
			}
			catch( RuntimeException e )
			{
				UISupport.showErrorMessage( e );
				
				server.removeConnector( connector );
				if( wasRunning ) 
				{
					server.start();
					return;
				}
			}
			
			connectors.put( new Integer( port), connector );
			runners.put( new Integer( port), new HashMap<String,List<MockRunner>>() );
		}
		
		Map<String, List<MockRunner>> map = runners.get( port );
		String path = mockService.getPath();
		if( !map.containsKey(path))
		{
			map.put( path, new ArrayList<MockRunner>() );
		}
		map.get(path).add( runner );
		mockRunners.add( runner );
		
		log.info(  "Started mockService [" + mockService.getName() + "] on port [" + port + "] at path [" + path + "]" );
	}

	private void initServer() throws Exception
	{
		server = new Server();
		BoundedThreadPool threadPool = new BoundedThreadPool();
		threadPool.setMaxThreads( 100 );
		server.setThreadPool( threadPool );
		server.setHandler( new ServerHandler() );
		
		if( SoapUI.getSettings().getBoolean( SSLSettings.ENABLE_MOCK_SSL ))
		{
			sslConnector = new SslSocketConnector();
			sslConnector.setKeystore( SoapUI.getSettings().getString( SSLSettings.MOCK_KEYSTORE, null ));
			sslConnector.setPassword( SoapUI.getSettings().getString( SSLSettings.MOCK_PASSWORD, null ));
			sslConnector.setKeyPassword( SoapUI.getSettings().getString( SSLSettings.MOCK_KEYSTORE_PASSWORD, null ));
			sslConnector.setTruststore( SoapUI.getSettings().getString( SSLSettings.MOCK_TRUSTSTORE, null ));
			sslConnector.setTrustPassword( SoapUI.getSettings().getString( SSLSettings.MOCK_TRUSTSTORE_PASSWORD, null ));
			sslConnector.setMaxIdleTime( 30000 );
			sslConnector.setPort( ( int ) SoapUI.getSettings().getLong( SSLSettings.MOCK_PORT, 443 ) );
			sslConnector.setNeedClientAuth( SoapUI.getSettings().getBoolean(SSLSettings.CLIENT_AUTHENTICATION));
			
			server.addConnector( sslConnector );
		}
	}

	public synchronized void stopMockService( WsdlMockRunner runner )
	{
		MockService mockService = runner.getMockService();
		final Integer port = new Integer( mockService.getPort());
		Map<String, List<MockRunner>> map = runners.get( port );
		
		map.get(mockService.getPath() ).remove(runner);
		mockRunners.remove( runner );
		
		log.info( "Stopped MockService [" + mockService.getName() + "] on port [" + port + "]" );
		
		if( map.isEmpty() && !SoapUI.getSettings().getBoolean( HttpSettings.LEAVE_MOCKENGINE ) )
		{
			SoapUIConnector connector = ( SoapUIConnector ) connectors.get( port );
			if( connector == null )
			{
				log.warn( "Missing connectors on port [" + port + "]" );
				return;
			}
			
			try
			{
				log.info( "Stopping connector on port " + port );
				if( !connector.waitUntilIdle( 5000 ))
				{
					log.warn(  "Failed to wait for idle.. stopping connector anyway.." );
				}
				connector.stop();
			}
			catch( Exception e )
			{
				SoapUI.logError( e );
			}
			server.removeConnector( connector );
			runners.remove( port );
			if( runners.isEmpty() )
			{
				try
				{
					log.info( "No more connectors.. stopping server" );
					server.stop();
					if( sslConnector != null )
					{
						server.removeConnector(sslConnector);
						sslConnector.stop();
						sslConnector = null;
					}
				}
				catch( Exception e )
				{
					SoapUI.logError( e );
				}
			}
		}
	}
	
	private class SoapUIConnector extends SelectChannelConnector
	{
		private Set<HttpConnection> connections = new HashSet<HttpConnection>();
		
		@Override
		protected void connectionClosed( HttpConnection arg0 )
		{
			super.connectionClosed( arg0 );
			connections.remove( arg0 );
		}

		@Override
		protected void connectionOpened( HttpConnection arg0 )
		{
			super.connectionOpened( arg0 );
			connections.add( arg0 );
		}
		
		public boolean waitUntilIdle( long maxwait ) throws Exception
		{
			while( maxwait > 0 && hasActiveConnections() )
			{
				System.out.println( "Waiting for active connections to finish.." );
				Thread.sleep( 500 );
				maxwait -= 500;
			}
			
			return !hasActiveConnections();
		}

		private boolean hasActiveConnections()
		{
			for( HttpConnection connection : connections )
			{
				if( !connection.isIdle() )
					return true;
			}
			
			return false;
		}
	}
	
	private class ServerHandler extends AbstractHandler
	{
		public void handle( String target, HttpServletRequest request,
					HttpServletResponse response, int dispatch ) throws IOException, ServletException
		{
			// find mockService
			Map<String, List<MockRunner>> map = runners.get( request.getLocalPort() );
			
			// ssl?
			if( map == null && sslConnector != null && request.getLocalPort() == sslConnector.getPort())
			{
				for( Map<String, List<MockRunner>> runnerMap : runners.values() )
				{
					if( runnerMap.containsKey( request.getPathInfo() ))
					{
						map = runnerMap;
						break;
					}
				}
			}
			
			if( map != null )
			{
				List<MockRunner> wsdlMockRunners = map.get( request.getPathInfo() );
				if( wsdlMockRunners == null && request.getMethod().equals( "GET" ))
				{
					for( String root : map.keySet())
					{
						if( request.getPathInfo().startsWith(root))
						{
							wsdlMockRunners = map.get( root );
						}
					}
				}
				
				if( wsdlMockRunners != null )
				{
					synchronized( wsdlMockRunners )
					{
						try
						{
							for( MockRunner wsdlMockRunner : wsdlMockRunners)
							{
								try
								{
									if( request.getMethod().equals( "GET" ))
									{
										wsdlMockRunner.dispatchGetRequest( request, response );
									}
									else if( request.getMethod().equals( "POST" ))
									{
										WsdlMockResult result = ( WsdlMockResult ) wsdlMockRunner.dispatchMockRequest( request, response );
										result.finish();
									}
									
									// if we get here, we got dispatched..
									break;
								}
								catch( DispatchException e )
								{
									log.debug(wsdlMockRunner.getMockService().getName() + " was unable to dispatch mock request ", e);
								}
							}
						}
						catch( Exception e )
						{
							SoapUI.logError( e );
							
							response.setStatus( HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
							response.setContentType( "text/html" );
							response.getWriter().print( SoapMessageBuilder.buildFault( "Server", e.getMessage(), 
										SoapVersion.Utils.getSoapVersionForContentType( request.getContentType(), SoapVersion.Soap11 )));
							response.flushBuffer();
							
							//throw new ServletException( e );
						}
					}
				}
				else
				{
					printMockServiceList( response );
				}
			}
			else
			{
				printMockServiceList( response );
			}
		}

		private void printMockServiceList( HttpServletResponse response ) throws IOException
		{
			response.setStatus( HttpServletResponse.SC_OK );
			response.setContentType( "text/html" );

			MockRunner[] mockRunners = getMockRunners();
			PrintWriter out = response.getWriter();
			out.print( "<html><body><p>There are currently " + mockRunners.length + " running soapUI MockServices</p><ul>" );
			
			for( MockRunner mockRunner : mockRunners )
			{
				out.print( "<li><a href=\"" );
				out.print( ((WsdlMockService)mockRunner.getMockService()).getPath() + "?WSDL" );
				out.print( "\">" + mockRunner.getMockService().getName() + "</a></li>" );
			}

			out.print( "</ul></p></body></html>" );
			response.flushBuffer();
		}
	}

	public MockRunner [] getMockRunners()
	{
		return mockRunners.toArray( new MockRunner[mockRunners.size()] );
	}
}
