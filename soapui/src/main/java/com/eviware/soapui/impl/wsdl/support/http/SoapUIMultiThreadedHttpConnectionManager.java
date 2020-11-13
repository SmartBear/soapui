/*
 * SoapUI, Copyright (C) 2004-2019 SmartBear Software
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

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.metrics.SoapUIMetrics;
import org.apache.commons.beanutils.PropertyUtils;
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
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.scheme.SchemeSocketFactory;
import org.apache.http.impl.HttpConnectionMetricsImpl;
import org.apache.http.impl.conn.AbstractPoolEntry;
import org.apache.http.impl.conn.DefaultClientConnection;
import org.apache.http.impl.conn.DefaultClientConnectionOperator;
import org.apache.http.impl.conn.tsccm.BasicPoolEntry;
import org.apache.http.impl.conn.tsccm.BasicPooledConnAdapter;
import org.apache.http.impl.conn.tsccm.PoolEntryRequest;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.io.HttpTransportMetrics;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

/**
 * Manages a set of HttpConnections for various HostConfigurations. Modified to
 * keep different pools for different keystores.
 */
public class SoapUIMultiThreadedHttpConnectionManager extends ThreadSafeClientConnManager {

    /**
     * Log object for this class.
     */
    private static final Logger log = LogManager.getLogger(SoapUIMultiThreadedHttpConnectionManager.class);

    /**
     * Connection eviction policy
     */
    IdleConnectionMonitorThread idleConnectionHandler = new IdleConnectionMonitorThread(this);

    public SoapUIMultiThreadedHttpConnectionManager(SchemeRegistry registry) {
        super(registry);
        idleConnectionHandler.start();
    }

    /**
     * Hook for creating the connection operator. It is called by the
     * constructor. Derived classes can override this method to change the
     * instantiation of the operator. The default implementation here
     * instantiates {@link DefaultClientConnectionOperator
     * DefaultClientConnectionOperator}.
     *
     * @param schreg the scheme registry.
     * @return the connection operator to use
     */
    @Override
    protected ClientConnectionOperator createConnectionOperator(SchemeRegistry schreg) {

        return new SoapUIClientConnectionOperator(schreg);// @ThreadSafe
    }

    public static class IdleConnectionMonitorThread extends Thread {
        private final ClientConnectionManager connMgr;
        private volatile boolean shutdown;

        public IdleConnectionMonitorThread(ClientConnectionManager connMgr) {
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

    public ClientConnectionRequest requestConnection(final HttpRoute route, final Object state) {

        final PoolEntryRequest poolRequest = pool.requestPoolEntry(route, state);

        return new ClientConnectionRequest() {

            public void abortRequest() {
                poolRequest.abortRequest();
            }

            public ManagedClientConnection getConnection(long timeout, TimeUnit tunit) throws InterruptedException,
                    ConnectionPoolTimeoutException {
                if (route == null) {
                    throw new IllegalArgumentException("Route may not be null.");
                }

                if (log.isDebugEnabled()) {
                    log.debug("Get connection: " + route + ", timeout = " + timeout);
                }

                BasicPoolEntry entry = poolRequest.getPoolEntry(timeout, tunit);
                SoapUIBasicPooledConnAdapter connAdapter = new SoapUIBasicPooledConnAdapter(
                        SoapUIMultiThreadedHttpConnectionManager.this, entry);
                return connAdapter;
            }
        };

    }

    public void releaseConnection(ManagedClientConnection conn, long validDuration, TimeUnit timeUnit) {

        if (!(conn instanceof SoapUIBasicPooledConnAdapter)) {
            throw new IllegalArgumentException("Connection class mismatch, "
                    + "connection not obtained from this manager.");
        }
        SoapUIBasicPooledConnAdapter hca = (SoapUIBasicPooledConnAdapter) conn;
        if ((hca.getPoolEntry() != null) && (hca.getManager() != this)) {
            throw new IllegalArgumentException("Connection not obtained from this manager.");
        }
        synchronized (hca) {
            BasicPoolEntry entry = (BasicPoolEntry) hca.getPoolEntry();
            if (entry == null) {
                return;
            }
            try {
                // make sure that the response has been read completely
                if (hca.isOpen() && !hca.isMarkedReusable()) {
                    // In MTHCM, there would be a call to
                    // SimpleHttpConnectionManager.finishLastResponse(conn);
                    // Consuming the response is handled outside in 4.0.

                    // make sure this connection will not be re-used
                    // Shut down rather than close, we might have gotten here
                    // because of a shutdown trigger.
                    // Shutdown of the adapter also clears the tracked route.
                    hca.shutdown();
                }
            } catch (IOException iox) {
                if (log.isDebugEnabled()) {
                    log.debug("Exception shutting down released connection.", iox);
                }
            } finally {
                boolean reusable = hca.isMarkedReusable();
                if (log.isDebugEnabled()) {
                    if (reusable) {
                        log.debug("Released connection is reusable.");
                    } else {
                        log.debug("Released connection is not reusable.");
                    }
                }
                hca.detach();
                pool.freeEntry(entry, reusable, validDuration, timeUnit);
            }
        }
    }

    @Override
    public void shutdown() {
        super.shutdown(); //To change body of generated methods, choose Tools | Templates.
        idleConnectionHandler.shutdown();
    }


    private class SoapUIClientConnectionOperator extends DefaultClientConnectionOperator {
        public SoapUIClientConnectionOperator(SchemeRegistry schemes) {
            super(schemes);
        }

        @Override
        public OperatedClientConnection createConnection() {
            SoapUIDefaultClientConnection connection = new SoapUIDefaultClientConnection();
            return connection;
        }

        @Override
        public void openConnection(final OperatedClientConnection conn, final HttpHost target, final InetAddress local,
                                   final HttpContext context, final HttpParams params) throws IOException {
            if (conn == null) {
                throw new IllegalArgumentException("Connection may not be null");
            }
            if (target == null) {
                throw new IllegalArgumentException("Target host may not be null");
            }
            if (params == null) {
                throw new IllegalArgumentException("Parameters may not be null");
            }
            if (conn.isOpen()) {
                throw new IllegalStateException("Connection must not be open");
            }

            Scheme schm = schemeRegistry.getScheme(target.getSchemeName());
            SchemeSocketFactory sf = schm.getSchemeSocketFactory();

            //long start = System.nanoTime();
            long start = System.currentTimeMillis();
            InetAddress[] addresses = resolveHostname(target.getHostName());
            // long dnsEnd = System.nanoTime();
            long dnsEnd = System.currentTimeMillis();

            int port = schm.resolvePort(target.getPort());
            for (int i = 0; i < addresses.length; i++) {
                InetAddress address = addresses[i];
                boolean last = i == addresses.length - 1;

                Socket sock = sf.createSocket(params);
                try {
                    // hostname is required by web server with virtual hosts and one IP (TLS-SNI)
                    if (sock instanceof SSLSocket) {
                        PropertyUtils.setProperty(sock, "host", target.getHostName());
                    }
                } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    SoapUI.logError(e);
                }
                conn.opening(sock, target);

                InetSocketAddress remoteAddress = new InetSocketAddress(address, port);
                InetSocketAddress localAddress = null;
                if (local != null) {
                    localAddress = new InetSocketAddress(local, 0);
                }
                if (log.isDebugEnabled()) {
                    log.debug("Connecting to " + remoteAddress);
                }
                try {
                    Socket connsock = sf.connectSocket(sock, remoteAddress, localAddress, params);
                    if (sock != connsock) {
                        sock = connsock;
                        conn.opening(sock, target);
                    }
                    prepareSocket(sock, context, params);
                    conn.openCompleted(sf.isSecure(sock), params);

                    SoapUIMetrics metrics = (SoapUIMetrics) conn.getMetrics();

                    if (metrics != null) {
                        metrics.getDNSTimer().set(start, dnsEnd);
                    }

                    return;
                } catch (ConnectException ex) {
                    if (last) {
                        throw new HttpHostConnectException(target, ex);
                    }
                } catch (ConnectTimeoutException ex) {
                    if (last) {
                        throw ex;
                    }
                }
                if (log.isDebugEnabled()) {
                    log.debug("Connect to " + remoteAddress + " timed out. "
                            + "Connection will be retried using another IP address");
                }
            }
        }
    }

    private class SoapUIDefaultClientConnection extends DefaultClientConnection {

        public SoapUIDefaultClientConnection() {
            super();
        }

        @Override
        /**
         * @since 4.1
         */
        protected HttpConnectionMetricsImpl createConnectionMetrics(final HttpTransportMetrics inTransportMetric,
                                                                    final HttpTransportMetrics outTransportMetric) {
            return new SoapUIMetrics(inTransportMetric, outTransportMetric);
        }
    }

    static class SoapUIBasicPooledConnAdapter extends BasicPooledConnAdapter {

        protected SoapUIBasicPooledConnAdapter(ThreadSafeClientConnManager tsccm, AbstractPoolEntry entry) {
            super(tsccm, entry);
        }

        @Override
        protected ClientConnectionManager getManager() {
            // override needed only to make method visible in this package
            return super.getManager();
        }

        @Override
        protected AbstractPoolEntry getPoolEntry() {
            // override needed only to make method visible in this package
            return super.getPoolEntry();
        }

        @Override
        protected void detach() {
            // override needed only to make method visible in this package
            super.detach();
        }
    }

}
