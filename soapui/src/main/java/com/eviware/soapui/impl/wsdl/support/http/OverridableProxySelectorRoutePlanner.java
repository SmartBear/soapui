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

import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.conn.params.ConnRouteParams;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.conn.ProxySelectorRoutePlanner;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;

import java.net.ProxySelector;

/**
 * @author Joel
 */
public class OverridableProxySelectorRoutePlanner extends ProxySelectorRoutePlanner {
    private static final String FORCE_DIRECT_CONNECTION = "FORCE_DIRECT_CONNECTION";

    static void setForceDirectConnection(HttpParams params) {
        params.setBooleanParameter(FORCE_DIRECT_CONNECTION, true);
    }

    public OverridableProxySelectorRoutePlanner(SchemeRegistry registry, ProxySelector proxySelector) {
        super(registry, proxySelector);
    }

    @Override
    protected HttpHost determineProxy(HttpHost target, HttpRequest request, HttpContext context) throws HttpException {
        if (request.getParams().getBooleanParameter(FORCE_DIRECT_CONNECTION, false)) {
            return null;
        }
        final HttpHost proxy = ConnRouteParams.getDefaultProxy(request.getParams());
        // Proxy should be able to be set for a request with ConnRoutePNames.DEFAULT_PROXY
        if (proxy != null) {
            return proxy;
        }

        return super.determineProxy(target, request, context);
    }
}
