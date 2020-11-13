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
import com.eviware.soapui.SoapUISystemProperties;
import com.eviware.soapui.impl.wsdl.submit.transports.http.ExtendedHttpMethod;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.metrics.SoapUIMetrics;
import com.eviware.soapui.impl.wsdl.support.CompressionSupport;
import com.eviware.soapui.model.propertyexpansion.PropertyExpander;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.model.settings.SettingsListener;
import com.eviware.soapui.settings.HttpSettings;
import com.eviware.soapui.settings.SSLSettings;
import org.apache.commons.ssl.KeyMaterial;
import org.apache.http.Header;
import org.apache.http.HttpClientConnection;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.RequestWrapper;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpRequestExecutor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.ProxySelector;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

/**
 * HttpClient related tools
 *
 * @author Ole.Matzura
 */

public class HttpClientSupport {
    private final static Helper helper = new Helper();

    static {
        if (PropertyExpander.getDefaultExpander() == null) {
            SoapUI.log.warn("Default property expander was null - will set global proxy later");
        } else {
            ProxyUtils.setGlobalProxy(SoapUI.getSettings());
        }
    }

    /**
     * Internal helper to ensure synchronized access..
     */

    public static class SoapUIHttpClient extends DefaultHttpClient {
        public SoapUIHttpClient(final ClientConnectionManager conman) {
            super(conman, null);
        }

        @Override
        protected HttpRequestExecutor createRequestExecutor() {
            return new SoapUIHttpRequestExecutor();
        }

    }

    public static class SoapUIHttpRequestExecutor extends HttpRequestExecutor {

        @Override
        public void preProcess(final HttpRequest request, final HttpProcessor processor, final HttpContext context)
                throws HttpException, IOException {
            HttpRequest original = request;

            if (original instanceof RequestWrapper) {
                RequestWrapper w = (RequestWrapper) request;
                original = w.getOriginal();
            }

            if (original instanceof ExtendedHttpMethod) {
                SoapUIMetrics metrics = ((ExtendedHttpMethod) original).getMetrics();
                metrics.getConnectTimer().stop();
                metrics.getTimeToFirstByteTimer().start();
            }
            super.preProcess(request, processor, context);
        }

        @Override
        protected HttpResponse doSendRequest(HttpRequest request, HttpClientConnection conn, HttpContext context)
                throws IOException, HttpException {
            HttpResponse response = super.doSendRequest(request, conn, context);
            return response;
        }

        @Override
        protected HttpResponse doReceiveResponse(final HttpRequest request, final HttpClientConnection conn,
                                                 final HttpContext context) throws HttpException, IOException {
            if (request == null) {
                throw new IllegalArgumentException("HTTP request may not be null");
            }
            if (conn == null) {
                throw new IllegalArgumentException("HTTP connection may not be null");
            }
            if (context == null) {
                throw new IllegalArgumentException("HTTP context may not be null");
            }

            HttpResponse response = null;
            int statuscode = 0;

            HttpRequest original = request;

            if (original instanceof RequestWrapper) {
                RequestWrapper w = (RequestWrapper) request;
                original = w.getOriginal();
            }

            while (response == null || statuscode < HttpStatus.SC_OK) {
                response = conn.receiveResponseHeader();

                SoapUIMetrics metrics = null;
                if (original instanceof ExtendedHttpMethod) {
                    metrics = ((ExtendedHttpMethod) original).getMetrics();
                    metrics.getTimeToFirstByteTimer().stop();
                    metrics.getReadTimer().start();
                }

                if (canResponseHaveBody(request, response)) {
                    conn.receiveResponseEntity(response);
                    //	if( metrics != null ) {
                    //	metrics.getReadTimer().stop();
                    // }
                }

                statuscode = response.getStatusLine().getStatusCode();

                if (conn.getMetrics() instanceof SoapUIMetrics) {
                    SoapUIMetrics connectionMetrics = (SoapUIMetrics) conn.getMetrics();

                    if (metrics != null && connectionMetrics != null && !connectionMetrics.isDone()) {
                        metrics.getDNSTimer().set(connectionMetrics.getDNSTimer().getStart(),
                                connectionMetrics.getDNSTimer().getStop());
                        // reset connection-level metrics
                        connectionMetrics.reset();
                    }
                }

            } // while intermediate response

            if (original instanceof ExtendedHttpMethod) {
                ExtendedHttpMethod extendedHttpMethod = (ExtendedHttpMethod) original;
                extendedHttpMethod.afterReadResponse(((SoapUIMultiThreadedHttpConnectionManager.SoapUIBasicPooledConnAdapter) conn).getSSLSession());
            }

            return response;
        }
    }

    private static class Helper {
        private final SoapUIHttpClient httpClient;
        private final static Logger log = LogManager.getLogger(HttpClientSupport.Helper.class);
        private final SoapUIMultiThreadedHttpConnectionManager connectionManager;
        private final SchemeRegistry registry;

        public Helper() {
            Settings settings = SoapUI.getSettings();
            registry = new SchemeRegistry();

            registry.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));

            try {
                SoapUISSLSocketFactory socketFactory = initSocketFactory();
                registry.register(new Scheme("https", 443, socketFactory));
            } catch (Throwable e) {
                SoapUI.logError(e);
            }

            connectionManager = new SoapUIMultiThreadedHttpConnectionManager(registry);
            connectionManager.setMaxTotal((int) settings.getLong(HttpSettings.MAX_TOTAL_CONNECTIONS, 2000));
            connectionManager
                    .setDefaultMaxPerRoute((int) settings.getLong(HttpSettings.MAX_CONNECTIONS_PER_HOST, 500));

            httpClient = new SoapUIHttpClient(connectionManager);

            // this interceptor needs to be last one added and executed.
            httpClient.addRequestInterceptor(new HeaderRequestInterceptor(), httpClient.getRequestInterceptorCount());

            settings.addSettingsListener(new SSLSettingsListener());
        }

        public SoapUIHttpClient getHttpClient() {
            return httpClient;
        }

        private SchemeRegistry getRegistry() {
            return registry;
        }

        public HttpResponse execute(ExtendedHttpMethod method, HttpContext httpContext) throws ClientProtocolException,
                IOException {
            method.afterWriteRequest();
            if (method.getMetrics() != null) {
                method.getMetrics().getConnectTimer().start();
            }
            HttpResponse httpResponse = httpClient.execute(method, httpContext);
            method.setHttpResponse(httpResponse);

            return httpResponse;
        }

        public HttpResponse execute(ExtendedHttpMethod method) throws ClientProtocolException, IOException {
            method.afterWriteRequest();
            if (method.getMetrics() != null) {
                method.getMetrics().getConnectTimer().start();
            }
            HttpResponse httpResponse = httpClient.execute(method);
            method.setHttpResponse(httpResponse);
            return httpResponse;
        }

        public final class SSLSettingsListener implements SettingsListener {
            @Override
            public void settingChanged(String name, String newValue, String oldValue) {

                if (name.equals(SSLSettings.KEYSTORE) || name.equals(SSLSettings.KEYSTORE_PASSWORD)) {
                    try {
                        log.info("Updating keyStore..");
                        registry.register(new Scheme("https", 443, initSocketFactory()));
                    } catch (Throwable e) {
                        SoapUI.logError(e);
                    }
                } else if (name.equals(HttpSettings.MAX_CONNECTIONS_PER_HOST)) {
                    log.info("Updating max connections per host to " + newValue);
                    connectionManager.setDefaultMaxPerRoute(Integer.parseInt(newValue));
                } else if (name.equals(HttpSettings.MAX_TOTAL_CONNECTIONS)) {
                    log.info("Updating max total connections host to " + newValue);
                    connectionManager.setMaxTotal(Integer.parseInt(newValue));
                }
            }

            @Override
            public void settingsReloaded() {
                try {
                    log.info("Updating keyStore..");
                    registry.register(new Scheme("https", 443, initSocketFactory()));
                } catch (Throwable e) {
                    SoapUI.logError(e);
                }
            }
        }

        public SoapUISSLSocketFactory initSocketFactory() throws KeyStoreException, NoSuchAlgorithmException,
                CertificateException, IOException, UnrecoverableKeyException, KeyManagementException {
            KeyStore keyStore = null;
            Settings settings = SoapUI.getSettings();

            String keyStoreUrl = System.getProperty(SoapUISystemProperties.SOAPUI_SSL_KEYSTORE_LOCATION,
                    settings.getString(SSLSettings.KEYSTORE, null));

            keyStoreUrl = keyStoreUrl != null ? keyStoreUrl.trim() : "";

            String pass = System.getProperty(SoapUISystemProperties.SOAPUI_SSL_KEYSTORE_PASSWORD,
                    settings.getString(SSLSettings.KEYSTORE_PASSWORD, ""));

            char[] pwd = pass.toCharArray();

            if (keyStoreUrl.trim().length() > 0) {
                File f = new File(keyStoreUrl);
                if (f.exists()) {
                    log.info("Initializing KeyStore");

                    try {
                        KeyMaterial km = new KeyMaterial(f, pwd);
                        keyStore = km.getKeyStore();
                    } catch (Exception e) {
                        SoapUI.logError(e);
                    }
                }
            }

            return new SoapUISSLSocketFactory(keyStore, pass);
        }
    }

    public static SoapUIHttpClient getHttpClient() {
        return helper.getHttpClient();
    }

    public static void setProxySelector(ProxySelector proxySelector) {
        getHttpClient().setRoutePlanner(new OverridableProxySelectorRoutePlanner(helper.getRegistry(), proxySelector));
    }

    public static HttpResponse execute(ExtendedHttpMethod method, HttpContext httpContext)
            throws ClientProtocolException, IOException {
        return helper.execute(method, httpContext);
    }

    public static HttpResponse execute(ExtendedHttpMethod method) throws ClientProtocolException, IOException {
        return helper.execute(method);
    }

    public static void applyHttpSettings(HttpRequest httpMethod, Settings settings) {
        // user agent?
        String userAgent = settings.getString(HttpSettings.USER_AGENT, null);
        if (userAgent != null && userAgent.length() > 0) {
            httpMethod.setHeader("User-Agent", userAgent);
        }

        // timeout?
        long timeout = settings.getLong(HttpSettings.SOCKET_TIMEOUT, HttpSettings.DEFAULT_SOCKET_TIMEOUT);
        httpMethod.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, (int) timeout);
    }

    public static String getResponseCompressionType(HttpResponse httpResponse) {
        Header contentType = null;
        if (httpResponse.getEntity() != null) {
            contentType = httpResponse.getEntity().getContentType();
        }

        Header contentEncoding = null;
        if (httpResponse.getEntity() != null) {
            contentEncoding = httpResponse.getEntity().getContentEncoding();
        }

        return getCompressionType(contentType == null ? null : contentType.getValue(), contentEncoding == null ? null
                : contentEncoding.getValue());
    }

    public static String getCompressionType(String contentType, String contentEncoding) {
        String compressionAlg = contentType == null ? null : CompressionSupport.getAvailableAlgorithm(contentType);
        if (compressionAlg != null) {
            return compressionAlg;
        }

        if (contentEncoding == null) {
            return null;
        } else {
            return CompressionSupport.getAvailableAlgorithm(contentEncoding);
        }
    }

    public static void addSSLListener(Settings settings) {
        settings.addSettingsListener(helper.new SSLSettingsListener());
    }

    public static BasicHttpContext createEmptyContext() {
        BasicHttpContext httpContext = new BasicHttpContext();

        // always use local cookie store so we don't share cookies with other threads/executions/requests
        CookieStore cookieStore = new BasicCookieStore();
        httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);

        return httpContext;
    }

}
