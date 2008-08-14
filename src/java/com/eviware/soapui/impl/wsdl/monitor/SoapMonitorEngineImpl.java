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

package com.eviware.soapui.impl.wsdl.monitor;

import org.mortbay.jetty.bio.SocketConnector;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;

import com.eviware.soapui.impl.wsdl.monitor.jettyproxy.ProxyServlet;
import com.eviware.soapui.impl.wsdl.monitor.jettyproxy.Server;
import com.eviware.soapui.support.UISupport;

public class SoapMonitorEngineImpl implements SoapMonitorEngine {

	Server server = new Server();
	SocketConnector connector = new SocketConnector();

	public boolean isRunning() {
		return server.isRunning();
	}

	public void start(SoapMonitor soapMonitor, int localPort) {
		
		connector.setPort(localPort);
		server.addConnector(connector);
		Context context = new Context(server, "/", 0);
		context.addServlet(new ServletHolder(new ProxyServlet(soapMonitor)), "/");
		
			try
			{
				server.start();
			}
			catch (Exception e)
			{
				UISupport.showErrorMessage("Error starting monitor: " + e.getMessage());
			}

	}

	public void stop() {
		
		try {
			if( server != null ) {
				server.stop();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if ( server != null ) { 
				server.destroy();
			}
		}

	}

}
