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

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.support.SSLUtils;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.support.StringUtils;
import org.apache.commons.ssl.KeyMaterial;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.protocol.HttpContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509ExtendedTrustManager;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SoapUISSLSocketFactory extends SSLConnectionSocketFactory {
    public static final String SSL_CONFIG_FOR_LAYERED_SOCKET_PARAM = "soapui.layered.socket.ssl.config";

    private final static Logger log = LogManager.getLogger(SoapUISSLSocketFactory.class);
    // A cache of factories for custom certificates/Keystores at the project level - never cleared
    private static final Map<String, SSLConnectionSocketFactory> factoryMap = new ConcurrentHashMap<String, SSLConnectionSocketFactory>();

    private final SSLContext sslContext;

    public static SoapUISSLSocketFactory create(KeyStore keyStore, String keystorePassword) throws KeyManagementException,
            UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException {
        final String sslContextAlgorithm = System.getProperty("soapui.sslcontext.algorithm", "TLS");
        final SSLContext sslContext = SSLContext.getInstance(sslContextAlgorithm);

        // trust everyone!
        X509ExtendedTrustManager tm = SSLUtils.getTrustAllManager();

        if (keyStore != null) {
            KeyManagerFactory kmfactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmfactory.init(keyStore, keystorePassword != null ? keystorePassword.toCharArray() : null);
            KeyManager[] keymanagers = kmfactory.getKeyManagers();
            sslContext.init(keymanagers, new TrustManager[]{tm}, null);
        } else {
            sslContext.init(null, new TrustManager[]{tm}, null);
        }

        return new SoapUISSLSocketFactory(sslContext);
    }

    private SoapUISSLSocketFactory(SSLContext sslContext) throws KeyManagementException,
            UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException {
        super(sslContext, SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        this.sslContext = sslContext;
    }

    private static Socket enableSocket(Socket socket) {
        if (socket instanceof SSLSocket) {
            SSLSocket sslSocket = (SSLSocket) socket;

            String invalidateSession = System.getProperty("soapui.https.session.invalidate");
            String protocols = System.getProperty("soapui.https.protocols");
            String ciphers = System.getProperty("soapui.https.ciphers");

            if (StringUtils.hasContent(invalidateSession)) {
                sslSocket.getSession().invalidate();
            }

            if (StringUtils.hasContent(protocols)) {
                sslSocket.setEnabledProtocols(protocols.split(","));
            }
            //		else if( socket.getSupportedProtocols() != null )
            //		{
            //			socket.setEnabledProtocols( socket.getSupportedProtocols() );
            //		}

            if (StringUtils.hasContent(ciphers)) {
                sslSocket.setEnabledCipherSuites(ciphers.split(","));
            }
            //		else if( socket.getSupportedCipherSuites() != null )
            //		{
            //			socket.setEnabledCipherSuites(  socket.getSupportedCipherSuites()  );
            //		}
        }

        return socket;
    }

    private String getSSLConfig(HttpContext context) {
        //TODO: still used deprecated getParams
        HttpClientContext clientContext = HttpClientContext.adapt(context);
        org.apache.http.HttpRequest request = clientContext.getRequest();
        if (request == null) {
            return "";
        } else {
            Object result = request.getParams().getParameter(SoapUIHttpRoute.SOAPUI_SSL_CONFIG);
            if (result == null) {
                result = request.getParams().getParameter(SoapUIHttpRoute.TESTSERVER_SSL_CONFIG);
            }
            if (result != null) {
                return (String) result;
            }
            return null;
        }
    }

    @Override
    public Socket createSocket(HttpContext context) throws IOException {
        String sslConfig = getSSLConfig(context);

        if (StringUtils.isNullOrEmpty(sslConfig)) {
            return enableSocket(createSocketWithProxy(context));
        }

        SSLConnectionSocketFactory factory = factoryMap.get(sslConfig);

        if (factory != null) {
            if (factory == this) {
                return enableSocket(createSslSocketWithProxy(context));
            } else {
                return enableSocket(factory.createSocket(context));
            }
        }

        try {
            factory = SoapUISSLSocketFactory.create(getKeyAndPassword(sslConfig).getKey(), getKeyAndPassword(sslConfig).getPassword());
            factoryMap.put(sslConfig, factory);

            return enableSocket(factory.createSocket(context));
        } catch (Exception gse) {
            //Logging.logError(gse);
            return enableSocket(createSocketWithProxy(context));
        }
    }

    private Socket createSocketWithProxy(HttpContext context) throws IOException {
        Settings settings = SoapUI.getSettings();
        if (ProxyUtils.isProxyEnabled() && !ProxyUtils.isAutoProxy() && ProxyUtils.getProxyType(settings) == Proxy.Type.SOCKS) {
            Proxy proxy = ProxyUtils.getProxy(settings);
            if (proxy != null) {
                return new Socket(proxy);
            }
        }

        return super.createSocket(context);
    }

    private Socket createSslSocketWithProxy(HttpContext context) throws IOException {
        Settings settings = SoapUI.getSettings();
        if (ProxyUtils.isProxyEnabled() && !ProxyUtils.isAutoProxy() && ProxyUtils.getProxyType(settings) == Proxy.Type.SOCKS) {
            Proxy proxy = ProxyUtils.getProxy(settings);
            if (proxy != null) {
                Socket socket = new Socket(proxy);

                HttpHost targetHost = (HttpHost) context.getAttribute("http.target_host");

                HttpClientContext clientContext = HttpClientContext.adapt(context);
                RequestConfig requestConfig = clientContext.getRequestConfig();

                int connectionTimeout = requestConfig.getConnectTimeout();
                int soTimeout = requestConfig.getSocketTimeout();
                connectionTimeout = connectionTimeout < 0 ? 0 : connectionTimeout;
                soTimeout = soTimeout < 0 ? 0 : soTimeout;

                socket.setSoTimeout(soTimeout);
                socket.connect(new InetSocketAddress(targetHost.getHostName(), targetHost.getPort()), connectionTimeout);

                InetSocketAddress proxyAddress = (InetSocketAddress) proxy.address();
                return sslContext.getSocketFactory().createSocket(socket, proxyAddress.getHostName(), proxyAddress.getPort(), true);
            }
        }

        return sslContext.getSocketFactory().createSocket();
    }

    /**
     * @since 4.1
     */
    @Override
    public Socket connectSocket(int connectTimeout, Socket socket, HttpHost host, InetSocketAddress remoteAddress, InetSocketAddress localAddress, HttpContext context) throws IOException,
            UnknownHostException, ConnectTimeoutException {
        if (remoteAddress == null) {
            throw new IllegalArgumentException("Remote address may not be null");
        }
        if (context == null) {
            throw new IllegalArgumentException("HTTP context  may not be null");
        }
        Socket sock = socket != null ? socket : new Socket();
        if (localAddress != null) {
            //TODO: not implemented, SoReuseAddress flag can be extracted form the SocketConfig (available through PoolingHttpClientConnectionManager.getSocketConfig)
            //sock.setReuseAddress(HttpConnectionParams.getSoReuseaddr(params));
            sock.bind(localAddress);
        }

        HttpClientContext clientContext = HttpClientContext.adapt(context);
        RequestConfig cfg = clientContext.getRequestConfig();

        int connTimeout = cfg.getConnectTimeout();
        int soTimeout = cfg.getSocketTimeout();
        connTimeout = connTimeout < 0 ? 0 : connTimeout;
        soTimeout = soTimeout < 0 ? 0 : soTimeout;

        try {
            sock.setSoTimeout(soTimeout);
            if (!sock.isConnected()) {
                sock.connect(remoteAddress, connTimeout);
            }
        } catch (SocketTimeoutException ex) {
            throw new ConnectTimeoutException("Connect to " + remoteAddress.getHostName() + "/"
                    + remoteAddress.getAddress() + " timed out");
        }
        SSLSocket sslsock;
        // Setup SSL layering if necessary
        if (sock instanceof SSLSocket) {
            sslsock = (SSLSocket) sock;
        } else {
            sslsock = (SSLSocket) sslContext.getSocketFactory().createSocket(sock, remoteAddress.getHostName(),
                    remoteAddress.getPort(), true);
            sslsock = (SSLSocket) enableSocket(sslsock);
        }
        // do we need it? trust all hosts
//		if( getHostnameVerifier() != null )
//		{
//			try
//			{
//				getHostnameVerifier().verify( remoteAddress.getHostName(), sslsock );
//				// verifyHostName() didn't blowup - good!
//			}
//			catch( IOException iox )
//			{
//				// close the socket before re-throwing the exception
//				try
//				{
//					sslsock.close();
//				}
//				catch( Exception x )
//				{ /* ignore */
//				}
//				throw iox;
//			}
//		}
        return sslsock;
    }

    @Override
    public Socket createLayeredSocket(Socket socket, String host, int port, HttpContext context)
            throws IOException {
        Socket sslSocket;
        String sslConfigForLayeredSocket = (String) context.getAttribute(SSL_CONFIG_FOR_LAYERED_SOCKET_PARAM);
        if (StringUtils.isNullOrEmpty(sslConfigForLayeredSocket)) {
            sslSocket = sslContext.getSocketFactory().createSocket(socket, host, port, true);
            sslSocket = enableSocket(sslSocket);
        } else {
            try {
                SSLConnectionSocketFactory factory = SoapUISSLSocketFactory.create(getKeyAndPassword(sslConfigForLayeredSocket).getKey(),
                        getKeyAndPassword(sslConfigForLayeredSocket).getPassword());
                SSLContext sslContext = ((SoapUISSLSocketFactory) factory).getSSLContext();
                sslSocket = enableSocket(sslContext.getSocketFactory().createSocket(socket, host, port, true));
            } catch (Exception gse) {
                //Logging.logError(gse);
                sslSocket = enableSocket(sslContext.getSocketFactory().createSocket(socket, host, port, true));
            }
        }
        return sslSocket;
    }

    private SSLContext getSSLContext() {
        return this.sslContext;
    }

    private KeyAndPassword getKeyAndPassword(String sslConfig) throws KeyStoreException {
        int ix = sslConfig.lastIndexOf(' ');
        String keyStore = sslConfig.substring(0, ix);
        String pwd = sslConfig.substring(ix + 1);
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        if (keyStore.trim().length() > 0) {
            File f = new File(keyStore);
            if (f.exists()) {
                log.info("Initializing Keystore from [" + keyStore + "]");
                try {
                    KeyMaterial km = new KeyMaterial(f, pwd.toCharArray());
                    ks = km.getKeyStore();
                } catch (Exception e) {
                    //Logging.logError(e);
                    pwd = null;
                }
            }
        }
        return new KeyAndPassword(ks, pwd);
    }

    private final class KeyAndPassword {
        private final KeyStore key;
        private final String password;

        protected KeyAndPassword(KeyStore key, String password) {
            this.key = key;
            this.password = password;
        }

        protected KeyStore getKey() {
            return this.key;
        }

        protected String getPassword() {
            return this.password;
        }
    }
}
