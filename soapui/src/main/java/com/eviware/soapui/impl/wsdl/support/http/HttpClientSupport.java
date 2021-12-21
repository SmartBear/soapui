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
import com.eviware.soapui.impl.wsdl.submit.transports.http.ExtendedHttpMethod;
import com.eviware.soapui.impl.wsdl.submit.transports.http.HttpClientRequestTransport;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.metrics.SoapUIMetrics;
import com.eviware.soapui.impl.wsdl.support.CompressionSupport;
import com.eviware.soapui.impl.wsdl.support.wsdl.UrlWsdlLoader;
import com.eviware.soapui.model.propertyexpansion.PropertyExpander;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.model.settings.SettingsListener;
import com.eviware.soapui.settings.HttpSettings;
import com.eviware.soapui.settings.SSLSettings;
import com.eviware.soapui.support.MessageSupport;
import com.eviware.soapui.support.StringUtils;
import org.apache.commons.lang.NullArgumentException;
import org.apache.http.Header;
import org.apache.http.HttpClientConnection;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.HttpStatus;
import org.apache.http.ProtocolVersion;
import org.apache.http.auth.AuthScheme;
import org.apache.http.auth.AuthSchemeProvider;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.AuthState;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestWrapper;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.protocol.RequestAcceptEncoding;
import org.apache.http.client.protocol.RequestAddCookies;
import org.apache.http.client.protocol.RequestAuthCache;
import org.apache.http.client.protocol.RequestClientConnControl;
import org.apache.http.client.protocol.RequestDefaultHeaders;
import org.apache.http.client.protocol.RequestExpectContinue;
import org.apache.http.client.protocol.ResponseContentEncoding;
import org.apache.http.client.protocol.ResponseProcessCookies;
import org.apache.http.config.Lookup;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.ManagedHttpClientConnection;
import org.apache.http.conn.SchemePortResolver;
import org.apache.http.conn.UnsupportedSchemeException;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.util.PublicSuffixMatcher;
import org.apache.http.conn.util.PublicSuffixMatcherLoader;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.impl.auth.BasicSchemeFactory;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.CookieSpecRegistries;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.RequestWrapper;
import org.apache.http.impl.conn.DefaultRoutePlanner;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.cookie.DefaultCookieSpecProvider;
import org.apache.http.impl.cookie.RFC6265CookieSpecProvider;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpRequestExecutor;
import org.apache.http.protocol.ImmutableHttpProcessor;
import org.apache.http.protocol.RequestTargetHost;
import org.apache.http.protocol.RequestUserAgent;
import org.apache.http.util.VersionInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ProxySelector;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * HttpClient related tools
 *
 * @author Ole.Matzura
 */

public class HttpClientSupport {
    private static final Logger log = LogManager.getLogger(HttpClientSupport.class);
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

    public static class SoapUIHttpClient extends CloseableHttpClient {
        private CloseableHttpClient realClient;
        private PoolingHttpClientConnectionManager connectionManager;
        private SoapUISchemePortResolver schemePortResolver;
        private HttpRoutePlanner routePlanner;
        Lookup<AuthSchemeProvider> authProviders;
        private CredentialsProvider credential;
        private HttpClientBuilder builder;

        private static final MessageSupport messages = MessageSupport.getMessages(SoapUIHttpClient.class);

        private final int MAX_TOTAL_CONNECTIONS_DEFAULT = 2000;
        private final int MAX_CONNECTIONS_PER_HOST_DEFAULT = 500;
        /*OT*/
        private final String CURRENT_VALUE_MESSAGE = "The current value is {0}";
        /*OT*/
        private final String DEFAULT_VALUE_MESSAGE = "The value has been set to default: {0}";

        public static SSLConnectionSocketFactory initSSLSocketFactory()
                throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException,
                CertificateException, IOException {
            KeyStore keyStore = SSLUtils.getReadyApiKeystore(log);
            String password = SSLUtils.getKeyStorePassword();

            return SoapUISSLSocketFactory.create(keyStore, password);
        }

        private SoapUIMultiThreadedHttpConnectionManager buildConnectionManager() {
            RegistryBuilder<ConnectionSocketFactory> registryBuilder = RegistryBuilder.create();
            registryBuilder.register("http", new SoapUIPlainSocketFactory());
            try {
                SSLConnectionSocketFactory socketFactory = initSSLSocketFactory();
                registryBuilder.register("https", socketFactory);
            } catch (Throwable e) {
                // TODO:
                //Logging.logError(e);
            }
            return new SoapUIMultiThreadedHttpConnectionManager(registryBuilder.build());
        }

        /**
         * Creates {@link HttpProcessor} with custom set and order
         * of request and response interceptors.
         *
         * @param settings
         * @return HttpProcessor with custom set and order of interceptors
         */
        protected HttpProcessor createHttpProcessor(Settings settings) {

            /*Set and order agree with set and order which generated by
             HttpClientBuilder using HttpProcessorBuilder.
              */

            //request interceptors
            List<HttpRequestInterceptor> requestInterceptors = new ArrayList<>();
            requestInterceptors.add(new RequestDefaultHeaders());
            requestInterceptors.add(new HeaderChecker());
            requestInterceptors.add(new RequestContentWrapper(true));
            requestInterceptors.add(new RequestTargetHost());
            requestInterceptors.add(new RequestClientConnControl());
            requestInterceptors.add(
                    new RequestUserAgent(
                            VersionInfo.getUserAgent(
                                    "Apache-HttpClient",
                                    "org.apache.http.client",
                                    HttpClientBuilder.class
                            )
                    )
            );
            requestInterceptors.add(new RequestExpectContinue());
            requestInterceptors.add(new RequestAddCookies());
            if (settings.getBoolean(HttpSettings.RESPONSE_COMPRESSION)) {
                requestInterceptors.add(new RequestAcceptEncoding());
            }
            requestInterceptors.add(new RequestAuthCache());
            // this interceptor needs to be last one added and executed.
            requestInterceptors.add(new HeaderRequestInterceptor());

            //response interceptors
            List<HttpResponseInterceptor> responseInterceptors = new ArrayList<>();
            responseInterceptors.add(new ResponseProcessCookies());
            if (!settings.getBoolean(HttpSettings.DISABLE_RESPONSE_DECOMPRESSION)) {
                responseInterceptors.add(new ResponseContentEncoding());
            }
            return new ImmutableHttpProcessor(requestInterceptors, responseInterceptors);
        }

        public SoapUIHttpClient(Settings settings) {
            schemePortResolver = new SoapUISchemePortResolver();
            routePlanner = new DefaultRoutePlanner(schemePortResolver);
            authProviders = RegistryBuilder.<AuthSchemeProvider>create()
                    .register(AuthSchemes.BASIC, new BasicSchemeFactory())
                    .register(AuthSchemes.DIGEST, new FixedDigestSchemeFactory())
                    .register(AuthSchemes.NTLM, new org.apache.http.impl.auth.NTLMSchemeFactory())
                    .register(AuthSchemes.SPNEGO, new FixedSPNegoSchemeFactory())
                    .register(AuthSchemes.KERBEROS, new FixedKerberosSchemeFactory())
                    .build();
            connectionManager = buildConnectionManager();
            builder = HttpClientBuilder.create();
            builder.setConnectionManager(connectionManager);
            builder.setRequestExecutor(new SoapUIHttpRequestExecutor());
            builder.setSchemePortResolver(schemePortResolver);
            builder.setRoutePlanner(routePlanner);
            builder.setDefaultAuthSchemeRegistry(authProviders);

            builder.setHttpProcessor(createHttpProcessor(settings));

            int maxTotalConnections = (int) settings.getLong(HttpSettings.MAX_TOTAL_CONNECTIONS, MAX_TOTAL_CONNECTIONS_DEFAULT);
            if (maxTotalConnections < 1) {
                settings.setLong(HttpSettings.MAX_TOTAL_CONNECTIONS, MAX_TOTAL_CONNECTIONS_DEFAULT);
                log.warn(HttpSettings.MAX_TOTAL_CONNECTIONS_INVALID_VALUE_ERROR_MESSAGE +
                        messages.get(CURRENT_VALUE_MESSAGE, maxTotalConnections));
                log.warn(messages.get(DEFAULT_VALUE_MESSAGE, MAX_TOTAL_CONNECTIONS_DEFAULT));
                maxTotalConnections = MAX_TOTAL_CONNECTIONS_DEFAULT;
            }

            int maxConnectionsPerHost = (int) settings.getLong(HttpSettings.MAX_CONNECTIONS_PER_HOST, MAX_CONNECTIONS_PER_HOST_DEFAULT);
            if (maxConnectionsPerHost < 1) {
                settings.setLong(HttpSettings.MAX_CONNECTIONS_PER_HOST, MAX_CONNECTIONS_PER_HOST_DEFAULT);
                log.warn(HttpSettings.MAX_CONNECTIONS_PER_HOST_INVALID_VALUE_ERROR_MESSAGE +
                        messages.get(CURRENT_VALUE_MESSAGE, maxConnectionsPerHost));
                log.warn(messages.get(DEFAULT_VALUE_MESSAGE, MAX_CONNECTIONS_PER_HOST_DEFAULT));
                maxConnectionsPerHost = MAX_CONNECTIONS_PER_HOST_DEFAULT;
            }

            connectionManager.setMaxTotal(maxTotalConnections);
            connectionManager.setDefaultMaxPerRoute(maxConnectionsPerHost);

            PublicSuffixMatcher publicSuffixMatcher = PublicSuffixMatcherLoader.getDefault();
            DefaultCookieSpecProvider defaultCookieSpecProvider = new DefaultCookieSpecProvider(
                    DefaultCookieSpecProvider.CompatibilityLevel.IE_MEDIUM_SECURITY, publicSuffixMatcher);
            CookieSpecProvider standardCookieSpecProvider = new RFC6265CookieSpecProvider(
                    RFC6265CookieSpecProvider.CompatibilityLevel.IE_MEDIUM_SECURITY, publicSuffixMatcher);
            Lookup<CookieSpecProvider> cookieSpecProviderRegistry = CookieSpecRegistries
                    .createDefaultBuilder(publicSuffixMatcher)
                    .register(CookieSpecs.DEFAULT, defaultCookieSpecProvider)
                    .register(CookieSpecs.STANDARD, standardCookieSpecProvider).build();
            builder.setDefaultCookieSpecRegistry(cookieSpecProviderRegistry);

            realClient = builder.build();
        }

        private HttpRequest tuneRequest(HttpRequest httpRequest) {
            Object version = httpRequest.getParams().getParameter(CoreProtocolPNames.PROTOCOL_VERSION);
            if (version != null && httpRequest.getProtocolVersion() != version) {
                return RequestBuilder.copy(httpRequest).setVersion((ProtocolVersion) version).build();
            }
            return httpRequest;
        }

        private HttpClientContext tuneContext(HttpRequest httpRequest, HttpContext httpContext) {
            //httpContext must not be null to use setCredentialsProvider and setRequestConfig
            if (httpContext == null) {
                httpContext = new BasicHttpContext();
            }

            CredentialsProvider credentialsProvider = (CredentialsProvider) httpContext.getAttribute(HttpClientContext.CREDS_PROVIDER);
            HttpClientContext clientContext = HttpClientContext.adapt(httpContext);
            if (credentialsProvider != null) {
                clientContext.setCredentialsProvider(credentialsProvider);
            } else {
                if (ProxyUtils.isProxyEnabled()) {
                    clientContext.setCredentialsProvider(credential);
                }
            }

            Object socketTimeout = httpRequest.getParams().getParameter(CoreConnectionPNames.SO_TIMEOUT);
            Object redirect = httpRequest.getParams().getParameter(ClientPNames.HANDLE_REDIRECTS);
            Object expectContinue = httpRequest.getParams().getParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE);
            Object localAddress = httpRequest.getParams().getParameter(ConnRoutePNames.LOCAL_ADDRESS);

            if (socketTimeout != null || redirect != null || localAddress != null || expectContinue != null) {
                RequestConfig.Builder cfgBuilder = RequestConfig.copy(clientContext.getRequestConfig());
                if (socketTimeout != null) {
                    cfgBuilder.setSocketTimeout((Integer) socketTimeout);
                }
                if (redirect != null) {
                    cfgBuilder.setRedirectsEnabled((Boolean) redirect);
                }
                if (expectContinue != null) {
                    cfgBuilder.setExpectContinueEnabled((Boolean) expectContinue);
                }
                if (localAddress != null) {
                    cfgBuilder.setLocalAddress((InetAddress) localAddress);
                }
                clientContext.setRequestConfig(cfgBuilder.build());
            }

            httpRequest.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookieSpecs.STANDARD);

            Object reguestSSLState = clientContext.getAttribute(HttpClientRequestTransport.USER_TOKEN_FOR_SSL);
            ((SoapUIMultiThreadedHttpConnectionManager) connectionManager).setSSLState(reguestSSLState);

            clientContext.setAttribute(HttpClientContext.TARGET_AUTH_STATE, new AuthState());

            return clientContext;
        }

        @Override
        protected CloseableHttpResponse doExecute(HttpHost httpHost, HttpRequest httpRequest, HttpContext httpContext) throws IOException {
            HttpClientContext clientContext = tuneContext(httpRequest, httpContext);
            HttpRequest request = tuneRequest(httpRequest);
            return realClient.execute(httpHost, request, clientContext);
        }

        @Override
        public void close() throws IOException {
            realClient.close();
        }

        @Override
        public HttpParams getParams() {
            return realClient.getParams();
        }

        @Override
        public ClientConnectionManager getConnectionManager() {
            return realClient.getConnectionManager();
        }

        public int getDefaultPort(String schemeName) {
            try {
                return schemePortResolver.resolve(schemeName);
            } catch (UnsupportedSchemeException ex) {
                //TODO: may be rethrow unchecked exception?
                return 0;
            }
        }

        public void setMaxTotal(int value) {
            connectionManager.setMaxTotal(value);
            //TODO: may be realClient is needed to rebuild?
        }

        public void setDefaultMaxPerRoute(int value) {
            connectionManager.setDefaultMaxPerRoute(value);
            //TODO: may be realClient is needed to rebuild?
        }

        public void updateSSLSocketFactory() {
            PoolingHttpClientConnectionManager old = connectionManager;

            connectionManager = buildConnectionManager();
            connectionManager.setMaxTotal(old.getMaxTotal());
            connectionManager.setDefaultMaxPerRoute(old.getDefaultMaxPerRoute());
            builder.setConnectionManager(connectionManager);
            realClient = builder.build();
        }

        public void setProxy(ProxySelector proxySelector, CredentialsProvider credential) {
            this.credential = credential;
            if (credential != null && ProxyUtils.isProxyEnabled()) {
                UrlWsdlLoader.setProxyCredentials(credential.getCredentials(AuthScope.ANY));
            } else {
                UrlWsdlLoader.setProxyCredentials(null);
            }
            routePlanner = new OverridableProxySelectorRoutePlanner(schemePortResolver, proxySelector);
            builder.setRoutePlanner(routePlanner);
            realClient = builder.build();
        }

        public HttpRoutePlanner getRoutePlanner() {
            return routePlanner;
        }

        public AuthScheme getAuthScheme(String schemeName) {
            AuthSchemeProvider provider = authProviders.lookup(schemeName);
            if (provider != null) {
                return provider.create(null);
            } else {
                throw new IllegalStateException("Unsupported authentication scheme: " + schemeName);
            }
        }
    }

    private static class SoapUISchemePortResolver implements SchemePortResolver {
        @Override
        public int resolve(HttpHost httpHost) throws UnsupportedSchemeException {
            int port = httpHost.getPort();
            return port > 0 ? port : resolve(httpHost.getSchemeName());
        }

        public int resolve(String schemeName) throws UnsupportedSchemeException {
            if (StringUtils.isNullOrEmpty(schemeName)) {
                throw new NullArgumentException("schemeName");
            } else if (schemeName.equalsIgnoreCase("http")) {
                return 80;
            } else if (schemeName.equalsIgnoreCase("https")) {
                return 443;
            } else {
                throw new UnsupportedSchemeException(schemeName + " protocol is not supported");
            }
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

            if (original instanceof HttpRequestWrapper) {
                HttpRequestWrapper w = (HttpRequestWrapper) request;
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
                extendedHttpMethod.afterReadResponse(((ManagedHttpClientConnection) conn).getSSLSession());
            }

            return response;
        }
    }

    private static class Helper {
        private final SoapUIHttpClient httpClient;
        private final static Logger log = LogManager.getLogger(HttpClientSupport.Helper.class);

        public Helper() {
            Settings settings = SoapUI.getSettings();
            httpClient = new SoapUIHttpClient(settings);
            settings.addSettingsListener(new SSLSettingsListener());
        }

        public SoapUIHttpClient getHttpClient() {
            return httpClient;
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

            private void updateSSLSocketFactory() {
                try {
                    log.info("Updating keyStore...");
                    httpClient.updateSSLSocketFactory();
                } catch (Throwable e) {
                    // TODO:
                    //Logging.logError(e);
                }
            }

            @Override
            public void settingChanged(String name, String newValue, String oldValue) {
                if (name.equals(SSLSettings.KEYSTORE) || name.equals(SSLSettings.KEYSTORE_PASSWORD)) {
                    updateSSLSocketFactory();
                } else if (name.equals(HttpSettings.MAX_CONNECTIONS_PER_HOST)) {
                    log.info("Updating max connections per host to " + newValue);
                    httpClient.setDefaultMaxPerRoute(Integer.parseInt(newValue));
                } else if (name.equals(HttpSettings.MAX_TOTAL_CONNECTIONS)) {
                    log.info("Updating max total connections host to " + newValue);
                    httpClient.setMaxTotal(Integer.parseInt(newValue));
                }
            }

            @Override
            public void settingsReloaded() {
                    updateSSLSocketFactory();
            }
        }
    }

    public static SoapUIHttpClient getHttpClient() {
        return helper.getHttpClient();
    }

    public static AuthScheme getAuthScheme(String schemeName) {
        return getHttpClient().getAuthScheme(schemeName);
    }

    public static int getDefaultPort(ExtendedHttpMethod httpMethod, HttpClient httpClient) {
        return getHttpClient().getDefaultPort(httpMethod.getURI().getScheme());
    }

    public static void setProxy(ProxySelector proxySelector, CredentialsProvider credential) {
        getHttpClient().setProxy(proxySelector, credential);
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

    /**
     * This class posts a warning to ReadyAPI log that informs
     * that the headers will be ignored and overwritten by the @RequestContent interceptor.
     * Condition for notice: any of headers
     *
     * @HTTP.TRANSFER_ENCODING
     * @HTTP.CONTENT_LEN must be in request
     */
    private static class HeaderChecker implements HttpRequestInterceptor {

        private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(HeaderChecker.class);
        private static final MessageSupport message = MessageSupport.getMessages(HeaderChecker.class);

        @Override
        public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
            List overriddenHeaders = Arrays.asList(HTTP.TRANSFER_ENCODING, HTTP.CONTENT_LEN);
            for (Header header : request.getAllHeaders()) {
                String name = header.getName();
                if (overriddenHeaders.contains(name)) {
                    log.warn(String.format(message.get("HeaderChecker.log.warn.header.ignored"), name));
                } else if (StringUtils.isNullOrEmpty(name)) {
                    log.warn(String.format(message.get("HeaderChecker.log.warn.header.ignored"), name));
                    request.removeHeader(header);
                }
            }
        }
    }
}
