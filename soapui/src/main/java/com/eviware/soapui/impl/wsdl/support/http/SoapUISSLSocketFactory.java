/*
 * Copyright 2004-2014 SmartBear Software
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

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.ssl.KeyMaterial;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.log4j.Logger;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.support.StringUtils;

public class SoapUISSLSocketFactory extends SSLSocketFactory {
    // a cache of factories for custom certificates/Keystores at the project level - never cleared
    private static final Map<String, SSLSocketFactory> factoryMap = new ConcurrentHashMap<String, SSLSocketFactory>();
    private final String sslContextAlgorithm = System.getProperty("soapui.sslcontext.algorithm", "TLS");
    private final SSLContext sslContext = SSLContext.getInstance(sslContextAlgorithm);
    private final static Logger log = Logger.getLogger(SoapUISSLSocketFactory.class);

    @SuppressWarnings("deprecation")
    public SoapUISSLSocketFactory(KeyStore keyStore, String keystorePassword) throws KeyManagementException,
            UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException {
        super(keyStore);

        // trust everyone!
        X509TrustManager tm = new X509TrustManager() {
            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            @Override
            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }
        };

        if (keyStore != null) {
            KeyManagerFactory kmfactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmfactory.init(keyStore, keystorePassword != null ? keystorePassword.toCharArray() : null);
            KeyManager[] keymanagers = kmfactory.getKeyManagers();
            sslContext.init(keymanagers, new TrustManager[]{tm}, null);
        } else {
            sslContext.init(null, new TrustManager[]{tm}, null);
        }

        setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

    }

    private static SSLSocket enableSocket(SSLSocket socket) {
        String invalidateSession = System.getProperty("soapui.https.session.invalidate");
        String protocols = System.getProperty("soapui.https.protocols");
        String ciphers = System.getProperty("soapui.https.ciphers");

        if (StringUtils.hasContent(invalidateSession)) {
            socket.getSession().invalidate();
        }

        if (StringUtils.hasContent(protocols)) {
            socket.setEnabledProtocols(protocols.split(","));
        }
        //		else if( socket.getSupportedProtocols() != null )
        //		{
        //			socket.setEnabledProtocols( socket.getSupportedProtocols() );
        //		}

        if (StringUtils.hasContent(ciphers)) {
            socket.setEnabledCipherSuites(ciphers.split(","));
        }
        //		else if( socket.getSupportedCipherSuites() != null )
        //		{
        //			socket.setEnabledCipherSuites(  socket.getSupportedCipherSuites()  );
        //		}

        return socket;
    }

    @Override
    public Socket createSocket(HttpParams params) throws IOException {
        String sslConfig = (String) params.getParameter(SoapUIHttpRoute.SOAPUI_SSL_CONFIG);

        if (StringUtils.isNullOrEmpty(sslConfig)) {
            return enableSocket((SSLSocket) sslContext.getSocketFactory().createSocket());
        }

        SSLSocketFactory factory = factoryMap.get(sslConfig);

        if (factory != null) {
            if (factory == this) {
                return enableSocket((SSLSocket) sslContext.getSocketFactory().createSocket());
            } else {
                return enableSocket((SSLSocket) factory.createSocket(params));
            }
        }

        try {
            // try to create new factory for specified config
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
                        SoapUI.logError(e);
                        pwd = null;
                    }
                }
            }

            factory = new SoapUISSLSocketFactory(ks, pwd);
            factoryMap.put(sslConfig, factory);

            return enableSocket((SSLSocket) factory.createSocket(params));
        } catch (Exception gse) {
            SoapUI.logError(gse);
            return enableSocket((SSLSocket) super.createSocket(params));
        }
    }

    /**
     * @since 4.1
     */
    @Override
    public Socket connectSocket(final Socket socket, final InetSocketAddress remoteAddress,
                                final InetSocketAddress localAddress, final HttpParams params) throws IOException, UnknownHostException,
            ConnectTimeoutException {
        if (remoteAddress == null) {
            throw new IllegalArgumentException("Remote address may not be null");
        }
        if (params == null) {
            throw new IllegalArgumentException("HTTP parameters may not be null");
        }
        Socket sock = socket != null ? socket : new Socket();
        if (localAddress != null) {
            sock.setReuseAddress(HttpConnectionParams.getSoReuseaddr(params));
            sock.bind(localAddress);
        }

        int connTimeout = HttpConnectionParams.getConnectionTimeout(params);
        int soTimeout = HttpConnectionParams.getSoTimeout(params);

        try {
            sock.setSoTimeout(soTimeout);
            sock.connect(remoteAddress, connTimeout);
        } catch (SocketTimeoutException ex) {
            throw new ConnectTimeoutException("Connect to " + remoteAddress.getHostName() + "/"
                    + remoteAddress.getAddress() + " timed out");
        }
        SSLSocket sslsock;
        // Setup SSL layering if necessary
        if (sock instanceof SSLSocket) {
            sslsock = (SSLSocket) sock;
        } else {
            sslsock = (SSLSocket)sslContext.getSocketFactory().createSocket(sock, remoteAddress.getHostName(),
                    remoteAddress.getPort(), true);
            sslsock = enableSocket(sslsock);
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

    /**
     * @since 4.1
     */
    @Override
    public Socket createLayeredSocket(final Socket socket, final String host, final int port, final boolean autoClose)
            throws IOException, UnknownHostException {
        SSLSocket sslSocket = (SSLSocket)sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
        sslSocket = enableSocket(sslSocket);
//		if( getHostnameVerifier() != null )
//		{
//			getHostnameVerifier().verify( host, sslSocket );
//		}
        // verifyHostName() didn't blowup - good!
        return sslSocket;
    }
}
