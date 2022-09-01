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
import com.eviware.soapui.settings.ProxySettings;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.SchemePortResolver;
import org.apache.http.conn.params.ConnRouteParams;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.conn.DefaultRoutePlanner;
import org.apache.http.impl.conn.ProxySelectorRoutePlanner;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

/**
 * @author Joel
 */
public class OverridableProxySelectorRoutePlanner extends DefaultRoutePlanner {
    private static final String FORCE_DIRECT_CONNECTION = "FORCE_DIRECT_CONNECTION";

    private final ProxySelector proxySelector;

    @Deprecated
    static void setForceDirectConnection(HttpParams params) {
        params.setBooleanParameter(FORCE_DIRECT_CONNECTION, true);
    }

    public OverridableProxySelectorRoutePlanner(SchemePortResolver schemePortResolver, ProxySelector proxySelector) {
        super(schemePortResolver);
        this.proxySelector = proxySelector;
    }

    @Override
    protected HttpHost determineProxy(HttpHost target, HttpRequest request, HttpContext context) throws HttpException {
        if (request.getParams().getBooleanParameter(FORCE_DIRECT_CONNECTION, false)) {
            return null;
        }

        //ATTENTION: keep old implementation, see ConnRoutePNames.DEFAULT_PROXY usage
        HttpHost proxy = ConnRouteParams.getDefaultProxy(request.getParams());

        if (proxySelector != null) {
            proxy = determineProxyThroughProxySelector(target);
        }

        //TODO: replace deprecated ClientContext with new HttpClientContext (string representation of the CREDS_PROVIDER remains the same)
        if ((proxy != null) && (context != null)) {
            CredentialsProvider credentialsProvider = (CredentialsProvider) context.getAttribute(ClientContext.CREDS_PROVIDER);
            if ((credentialsProvider != null) && (credentialsProvider instanceof HttpCredentialsProvider)) {
                boolean autoProxy = SoapUI.getSettings().getBoolean(ProxySettings.AUTO_PROXY);
                if (autoProxy) {
                    HttpCredentialsProvider httpCredentialsProvider = (HttpCredentialsProvider) credentialsProvider;
                    httpCredentialsProvider.setProxy(proxy.getHostName(), String.valueOf(proxy.getPort()));
                }

            }
        }

        return proxy;
    }

    //borrowed from the ProxySelectorRoutePlanner
    protected HttpHost determineProxyThroughProxySelector(HttpHost target) throws HttpException {
        if(proxySelector == null) {
            return null;
        }

        URI targetURI = null;
        try {
            targetURI = new URI(target.toURI());
        } catch (URISyntaxException var10) {
            throw new HttpException("Cannot convert host to URI: " + target, var10);
        }

        List proxies = proxySelector.select(targetURI);
        Proxy proxy = this.chooseProxy(proxies);

        HttpHost result = null;
        if (proxy != null && proxy.type() == Proxy.Type.HTTP) {
            if (!(proxy.address() instanceof InetSocketAddress)) {
                throw new HttpException("Unable to handle non-Inet proxy address: " + proxy.address());
            }

            InetSocketAddress isa = (InetSocketAddress)proxy.address();
            String host = isa.isUnresolved() ? isa.getHostName() : isa.getAddress().getHostAddress();
            result = new HttpHost(host, isa.getPort());
        }

        return result;
    }

    protected Proxy chooseProxy(List<Proxy> proxies) {
        for (Proxy p: proxies) {
            if (p.type() == Proxy.Type.HTTP) {
                return p;
            }
        }
        return null;
    }
}
