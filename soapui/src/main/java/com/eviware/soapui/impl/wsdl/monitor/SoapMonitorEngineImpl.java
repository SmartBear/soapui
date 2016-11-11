/*
 * SoapUI, Copyright (C) 2004-2016 SmartBear Software 
 *
 * Licensed under the EUPL, Version 1.1 or - as soon as they will be approved by the European Commission - subsequent 
 * versions of the EUPL (the "Licence"); 
 * You may not use this work except in compliance with the Licence. 
 * You may obtain a copy of the Licence at: 
 * 
 * http://ec.europa.eu/idabc/eupl 
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is 
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either 
 * express or implied. See the Licence for the specific language governing permissions and limitations 
 * under the Licence. 
 */

package com.eviware.soapui.impl.wsdl.monitor;

import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.actions.monitor.SoapMonitorAction;
import com.eviware.soapui.impl.wsdl.monitor.jettyproxy.JettyServer;
import com.eviware.soapui.impl.wsdl.monitor.jettyproxy.ProxyServlet;
import com.eviware.soapui.impl.wsdl.monitor.jettyproxy.TunnelServlet;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.monitor.SoapUIJettyThreadPool;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import org.mortbay.jetty.bio.SocketConnector;
import org.mortbay.jetty.security.SslSocketConnector;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;

import java.net.BindException;

public class SoapMonitorEngineImpl implements SoapMonitorEngine {

    private static final String ROOT = "/";
    private static final String HTTP = "http://";
    private static final String HTTPS = "https://";
    JettyServer server = new JettyServer();
    SocketConnector connector = new SocketConnector();
    private SslSocketConnector sslConnector;
    private final String sslEndpoint;
    private boolean proxyOrTunnel = true;
    private ContentTypes includedContentTypes = SoapMonitorAction.defaultContentTypes();

    public SoapMonitorEngineImpl(final String sslEndpoint) {
        this.sslEndpoint = sslEndpoint;
    }

    public boolean isRunning() {
        return server.isRunning();
    }

    public void start(WsdlProject project, int localPort, SoapMonitorListenerCallBack listenerCallBack) {
        Settings settings = project.getSettings();
        server.setThreadPool(new SoapUIJettyThreadPool());
        Context context = new Context(server, ROOT, 0);

        if (!StringUtils.isNullOrEmpty(sslEndpoint)) {
            if (sslEndpoint.startsWith(HTTPS)) {
                sslConnector = new SslSocketConnector();
                sslConnector
                        .setKeystore(settings.getString(SoapMonitorAction.SecurityTabForm.SSLTUNNEL_KEYSTORE, "JKS"));
                sslConnector.setPassword(settings.getString(SoapMonitorAction.SecurityTabForm.SSLTUNNEL_PASSWORD, ""));
                sslConnector.setKeyPassword(settings.getString(SoapMonitorAction.SecurityTabForm.SSLTUNNEL_KEYPASSWORD,
                        ""));
                sslConnector.setTruststore(settings.getString(SoapMonitorAction.SecurityTabForm.SSLTUNNEL_TRUSTSTORE,
                        "JKS"));
                sslConnector.setTrustPassword(settings.getString(
                        SoapMonitorAction.SecurityTabForm.SSLTUNNEL_TRUSTSTORE_PASSWORD, ""));
                sslConnector.setNeedClientAuth(false);
                sslConnector.setMaxIdleTime(30000);
                sslConnector.setPort(localPort);

                server.addConnector(sslConnector);
                TunnelServlet tunnelServlet = new TunnelServlet(project, sslEndpoint, listenerCallBack);
                tunnelServlet.setIncludedContentTypes(includedContentTypes);
                context.addServlet(new ServletHolder(tunnelServlet), ROOT);
            } else {
                if (sslEndpoint.startsWith(HTTP)) {
                    connector.setPort(localPort);
                    server.addConnector(connector);
                    TunnelServlet tunnelServlet = new TunnelServlet(project, sslEndpoint, listenerCallBack);
                    tunnelServlet.setIncludedContentTypes(includedContentTypes);
                    context.addServlet(new ServletHolder(tunnelServlet), ROOT);
                } else {
                    UISupport.showErrorMessage("Unsupported/unknown protocol tunnel will not start");
                    return;
                }
            }
            proxyOrTunnel = false;
        } else {
            proxyOrTunnel = true;
            connector.setPort(localPort);
            server.addConnector(connector);
            ProxyServlet proxyServlet = new ProxyServlet(project, listenerCallBack);
            proxyServlet.setIncludedContentTypes(includedContentTypes);
            context.addServlet(new ServletHolder(proxyServlet), ROOT);
        }
        try {
            server.start();
        } catch (BindException e) {
            UISupport.showErrorMessage("Error starting " + getProxyOrTunnelString() + ": Could not open port " + localPort + ".\nTry a different port number.");
        } catch (Exception e) {
            UISupport.showErrorMessage("Error starting " + getProxyOrTunnelString() + ": " + e.getMessage());
        }

    }

    private String getProxyOrTunnelString() {
        return proxyOrTunnel ? "proxy" : "tunnel";
    }

    public void stop() {

        try {
            if (server != null) {
                server.stop();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (server != null) {
                server.destroy();
            }
        }

    }

    @Override
    public void setIncludedContentTypes(ContentTypes includedContentTypes) {
        this.includedContentTypes = includedContentTypes;
    }

    /*
         * @return true if proxy, false if ssl tunnel (non-Javadoc)
         *
         * @see com.eviware.soapui.impl.wsdl.monitor.SoapMonitorEngine#isProxy()
         */
    public boolean isProxy() {
        return proxyOrTunnel;
    }

}
