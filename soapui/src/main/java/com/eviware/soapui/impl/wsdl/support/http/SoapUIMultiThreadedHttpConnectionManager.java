/*
 * SoapUI, Copyright (C) 2004-2022 SmartBear Software
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

package com.eviware.soapui.impl.wsdl.support.http;

import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.impl.wsdl.submit.transports.http.ExtendedHttpMethod;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.metrics.Stopwatch;
import org.apache.http.HttpClientConnection;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.client.methods.HttpRequestWrapper;
import org.apache.http.config.Lookup;
import org.apache.http.config.Registry;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.ConnectionPoolTimeoutException;
import org.apache.http.conn.ConnectionRequest;
import org.apache.http.conn.DnsResolver;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.HttpConnectionFactory;
import org.apache.http.conn.ManagedHttpClientConnection;
import org.apache.http.conn.SchemePortResolver;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.impl.conn.DefaultHttpClientConnectionOperator;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.net.ssl.SSLSession;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Manages a set of HttpConnections for various HostConfigurations. Modified to
 * keep different pools for different keystores.
 */
public class SoapUIMultiThreadedHttpConnectionManager extends PoolingHttpClientConnectionManager {
    private Object SSLState;
    /**
     * Log object for this class.
     */
    private static final Logger log = LogManager.getLogger(SoapUIMultiThreadedHttpConnectionManager.class);

    /**
     * Connection eviction policy
     */
    IdleConnectionMonitorThread idleConnectionHandler = new IdleConnectionMonitorThread(this);

    public SoapUIMultiThreadedHttpConnectionManager(Registry<ConnectionSocketFactory> registry) {
        this(registry, new SoapUIManagedHttpClientConnectionFactory(), null);
        idleConnectionHandler.start();
    }

    public SoapUIMultiThreadedHttpConnectionManager(Registry<ConnectionSocketFactory> socketFactoryRegistry, HttpConnectionFactory<HttpRoute, ManagedHttpClientConnection> connFactory, DnsResolver dnsResolver) {
        this(socketFactoryRegistry, connFactory, null, dnsResolver, -1, TimeUnit.MILLISECONDS);
    }

    public SoapUIMultiThreadedHttpConnectionManager(Registry<ConnectionSocketFactory> socketFactoryRegistry, HttpConnectionFactory<HttpRoute, ManagedHttpClientConnection> connFactory, SchemePortResolver schemePortResolver, DnsResolver dnsResolver, long timeToLive, TimeUnit tunit) {
        super(
                new SoapUiHttpClientConnectionOperator(socketFactoryRegistry, schemePortResolver, dnsResolver),
                connFactory,
                timeToLive, tunit
        );
    }

    public void setSSLState(Object SSLState) {
        this.SSLState = SSLState;
    }

    public static class IdleConnectionMonitorThread extends Thread {
        private final HttpClientConnectionManager connMgr;
        private volatile boolean shutdown;

        public IdleConnectionMonitorThread(HttpClientConnectionManager connMgr) {
            super();
            this.connMgr = connMgr;
        }

        @Override
        public void run() {
            try {
                while (!shutdown) {
                    synchronized (this) {
                        wait(5000);
                        // Close expired connections
                        connMgr.closeExpiredConnections();
                        // Optionally, close connections
                        // that have been idle longer than 30 sec
                        connMgr.closeIdleConnections(30, TimeUnit.SECONDS);
                    }
                }
            } catch (InterruptedException ex) {
                // terminate
            }
        }

        public void shutdown() {
            shutdown = true;
            synchronized (this) {
                notifyAll();
            }
        }
    }

    @Override
    public void shutdown() {
        super.shutdown(); //To change body of generated methods, choose Tools | Templates.
        idleConnectionHandler.shutdown();
    }

    @Override
    public ConnectionRequest requestConnection(
            final HttpRoute route,
            final Object state) {
        if (SSLState != null) {
            return super.requestConnection(route, SSLState);
        }
        return super.requestConnection(route, state);
    }

    @Override
    protected HttpClientConnection leaseConnection(
            final Future future,
            final long timeout,
            final TimeUnit tunit) throws InterruptedException, ExecutionException, ConnectionPoolTimeoutException {
        HttpClientConnection httpClientConnection = null;
        do {
            httpClientConnection = super.leaseConnection(future, timeout, tunit);
            if (!httpClientConnection.isOpen()) {
                break;
            } else if (httpClientConnection.isOpen() && AbstractHttpRequest.EMPTY_SSLSTATE.equals(SSLState)) {
                try {
                    httpClientConnection.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } while (AbstractHttpRequest.EMPTY_SSLSTATE.equals(SSLState));
        return httpClientConnection;
    }

    @Override
    public void releaseConnection(
            final HttpClientConnection managedConn,
            final Object state,
            final long keepalive, final TimeUnit tunit) {
        Object curState = state;
        SSLSession sslSession = ((ManagedHttpClientConnection) managedConn).getSSLSession();
        if (sslSession != null) {
            curState = sslSession.getLocalPrincipal();
        }
        super.releaseConnection(managedConn, curState, keepalive, tunit);
    }
}

class SoapUiHttpClientConnectionOperator extends DefaultHttpClientConnectionOperator {
    private static final String HTTP_REQUEST_ATTRIBUTE = "http.request";

    public SoapUiHttpClientConnectionOperator(Lookup<ConnectionSocketFactory> socketFactoryRegistry, SchemePortResolver schemePortResolver, DnsResolver dnsResolver) {
        super(socketFactoryRegistry, schemePortResolver, dnsResolver);
    }

    @Override
    public void connect(ManagedHttpClientConnection conn, HttpHost host, InetSocketAddress localAddress, int connectTimeout, SocketConfig socketConfig, HttpContext context) throws IOException {
        HttpRequest request = (HttpRequest) context.getAttribute(HTTP_REQUEST_ATTRIBUTE);
        if (request instanceof HttpRequestWrapper) {
            request = ((HttpRequestWrapper) request).getOriginal();
        }
        //for a request via proxy
        if (!request.getRequestLine().getUri().contains(host.toURI())) {
            Object sslConfig = null;
            sslConfig = request.getParams().getParameter(SoapUIHttpRoute.SOAPUI_SSL_CONFIG);
            if (sslConfig == null) {
                sslConfig = request.getParams().getParameter(SoapUIHttpRoute.TESTSERVER_SSL_CONFIG);
            }
            if (sslConfig != null) {
                context.setAttribute(SoapUISSLSocketFactory.SSL_CONFIG_FOR_LAYERED_SOCKET_PARAM, sslConfig.toString());
            }
        }

        Stopwatch connectTimer = null;
        if ((request instanceof ExtendedHttpMethod) && (((ExtendedHttpMethod) request).getMetrics() != null)) {
            connectTimer = ((ExtendedHttpMethod) request).getMetrics().getConnectTimer();
            connectTimer.start();
        }
        super.connect(conn, host, localAddress, connectTimeout, socketConfig, context);
        if (connectTimer != null) {
            connectTimer.stop();
        }
    }
}
