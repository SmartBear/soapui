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

import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.support.StringUtils;
import org.mortbay.jetty.bio.SocketConnector;
import org.mortbay.jetty.security.SslSocketConnector;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;

import com.eviware.soapui.impl.wsdl.actions.monitor.SoapMonitorAction;
import com.eviware.soapui.impl.wsdl.monitor.jettyproxy.ProxyServlet;
import com.eviware.soapui.impl.wsdl.monitor.jettyproxy.JettyServer;
import com.eviware.soapui.impl.wsdl.monitor.jettyproxy.TunnelServlet;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.monitor.SoapUIJettyThreadPool;
import com.eviware.soapui.support.UISupport;

public class SoapMonitorEngineImpl implements SoapMonitorEngine
{

	private static final String ROOT = "/";
	private static final String HTTP = "http://";
	private static final String HTTPS = "https://";
	JettyServer server = new JettyServer();
	SocketConnector connector = new SocketConnector();
	private SslSocketConnector sslConnector;
	private final String sslEndpoint;
	private boolean proxyOrTunnel = true;

	public SoapMonitorEngineImpl(final String sslEndpoint)
	{
		this.sslEndpoint = sslEndpoint;
	}

	public boolean isRunning()
	{
		return server.isRunning();
	}

	public void start( WsdlProject project, int localPort, SoapMonitorListenerCallBack listenerCallBack )
	{
		Settings settings = project.getSettings();
		server.setThreadPool( new SoapUIJettyThreadPool() );
		Context context = new Context( server, ROOT, 0 );

		if( !StringUtils.isNullOrEmpty(  sslEndpoint ))
		{
			if( sslEndpoint.startsWith( HTTPS ) )
			{
				sslConnector = new SslSocketConnector();
				sslConnector
						.setKeystore( settings.getString( SoapMonitorAction.SecurityTabForm.SSLTUNNEL_KEYSTORE, "JKS" ) );
				sslConnector.setPassword( settings.getString( SoapMonitorAction.SecurityTabForm.SSLTUNNEL_PASSWORD, "" ) );
				sslConnector.setKeyPassword( settings.getString( SoapMonitorAction.SecurityTabForm.SSLTUNNEL_KEYPASSWORD,
						"" ) );
				sslConnector.setTruststore( settings.getString( SoapMonitorAction.SecurityTabForm.SSLTUNNEL_TRUSTSTORE,
						"JKS" ) );
				sslConnector.setTrustPassword( settings.getString(
						SoapMonitorAction.SecurityTabForm.SSLTUNNEL_TRUSTSTORE_PASSWORD, "" ) );
				sslConnector.setNeedClientAuth( false );
				sslConnector.setMaxIdleTime( 30000 );
				sslConnector.setPort( localPort );

				server.addConnector( sslConnector );
				context.addServlet( new ServletHolder( new TunnelServlet( project, sslEndpoint, listenerCallBack ) ), ROOT );
			}
			else
			{
				if( sslEndpoint.startsWith( HTTP ) )
				{
					connector.setPort( localPort );
					server.addConnector( connector );
					context.addServlet( new ServletHolder( new TunnelServlet( project, sslEndpoint, listenerCallBack ) ), ROOT );
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
			context.addServlet( new ServletHolder( new ProxyServlet( project, listenerCallBack ) ), ROOT );
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
