package com.eviware.soapui.impl.wsdl.monitor;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;

import com.eviware.soapui.impl.wsdl.monitor.jettyproxy.ProxyServlet;

public class SoapMonitorEngineImpl implements SoapMonitorEngine {

	Server server = new Server();
	SelectChannelConnector connector = new SelectChannelConnector();

	@Override
	public boolean isRunning() {
		return server.isRunning();
	}

	@Override
	public void start(SoapMonitor soapMonitor, int localPort) {
		
		connector.setPort(localPort);
		server.addConnector(connector);
		Context context = new Context(server, "/", 0);
		context.addServlet(new ServletHolder(new ProxyServlet(soapMonitor)), "/");
		
		try {
			server.start();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public void stop() {
		
		try {
			if( server != null ) {
				server.stop();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if ( server != null ) { 
				server.destroy();
			}
		}

	}

}
