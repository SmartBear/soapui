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

import org.mortbay.jetty.bio.SocketConnector;
import org.mortbay.jetty.security.SslSocketConnector;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.thread.QueuedThreadPool;

import com.eviware.soapui.impl.wsdl.actions.monitor.SoapMonitorAction;
import com.eviware.soapui.impl.wsdl.monitor.jettyproxy.ProxyServlet;
import com.eviware.soapui.impl.wsdl.monitor.jettyproxy.Server;
import com.eviware.soapui.impl.wsdl.monitor.jettyproxy.TunnelServlet;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.support.UISupport;

public class SoapMonitorEngineImpl implements SoapMonitorEngine
{

	private static final String ROOT = "/";
	private static final String HTTP = "http://";
	private static final String HTTPS = "https://";
	Server server = new Server();
	SocketConnector connector = new SocketConnector();
	private SslSocketConnector sslConnector;
	private String sslEndpoint = null;
	private boolean proxyOrTunnel = true;

	public boolean isRunning()
	{
		return server.isRunning();
	}

	public void start( SoapMonitor soapMonitor, int localPort )
	{

		Settings settings = soapMonitor.getProject().getSettings();
		QueuedThreadPool threadPool = new QueuedThreadPool();
		threadPool.setMaxThreads( 100 );
		server.setThreadPool( threadPool );
		Context context = new Context( server, ROOT, 0 );

		if( sslEndpoint != null )
		{
			if( sslEndpoint.startsWith( HTTPS ) )
			{
				sslConnector = new SslSocketConnector();
				sslConnector.setKeystore( settings.getString( SoapMonitorAction.LaunchForm.SSLTUNNEL_KEYSTORE, "JKS" ) );
				sslConnector.setPassword( settings.getString( SoapMonitorAction.LaunchForm.SSLTUNNEL_PASSWORD, "" ) );
				sslConnector.setKeyPassword( settings.getString( SoapMonitorAction.LaunchForm.SSLTUNNEL_KEYPASSWORD, "" ) );
				sslConnector.setTruststore( settings.getString( SoapMonitorAction.LaunchForm.SSLTUNNEL_TRUSTSTORE, "JKS" ) );
				sslConnector.setTrustPassword( settings.getString(
						SoapMonitorAction.LaunchForm.SSLTUNNEL_TRUSTSTORE_PASSWORD, "" ) );
				sslConnector.setNeedClientAuth( false );
				sslConnector.setMaxIdleTime( 30000 );
				sslConnector.setPort( localPort );

				server.addConnector( sslConnector );
				context.addServlet( new ServletHolder( new TunnelServlet( soapMonitor, sslEndpoint ) ), ROOT );
			}
			else
			{
				if( sslEndpoint.startsWith( HTTP ) )
				{
					connector.setPort( localPort );
					server.addConnector( connector );
					context.addServlet( new ServletHolder( new TunnelServlet( soapMonitor, sslEndpoint ) ), ROOT );
				}
				else
				{
					UISupport.showErrorMessage( "Unsupported/unknown protocol tunnel will not start" );
					return;
				}
			}
			proxyOrTunnel = false;
		}
		else
		{
			proxyOrTunnel = true;
			connector.setPort( localPort );
			server.addConnector( connector );
			context.addServlet( new ServletHolder( new ProxyServlet( soapMonitor ) ), ROOT );
		}
		try
		{
			server.start();
		}
		catch( Exception e )
		{
			UISupport.showErrorMessage( "Error starting monitor: " + e.getMessage() );
		}

	}

	public void stop()
	{

		try
		{
			if( server != null )
			{
				server.stop();
			}
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
		finally
		{
			if( server != null )
			{
				server.destroy();
			}
		}

	}

	protected void setSslEndpoint( String sslEndpoint )
	{
		this.sslEndpoint = sslEndpoint;
	}

	/*
	 * @return true if proxy, false if ssl tunnel (non-Javadoc)
	 * 
	 * @see com.eviware.soapui.impl.wsdl.monitor.SoapMonitorEngine#isProxy()
	 */
	public boolean isProxy()
	{
		return proxyOrTunnel;
	}

}
