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

package com.eviware.soapui.impl.wsdl.support.http;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

/**
 * @author Joel
 */
public class ManualProxySelector extends ProxySelector {
    private static final List<Proxy> NO_PROXY_LIST = Arrays.asList(Proxy.NO_PROXY);
    private final List<Proxy> proxyList;
    private final String[] excludes;

    public ManualProxySelector(Proxy proxy, String[] excludes) {
        this.excludes = excludes;
        this.proxyList = Arrays.asList(proxy);
    }

    public ManualProxySelector(String proxyHost, int proxyPort, String[] excludes) {
        this(new Proxy(Proxy.Type.HTTP,
                InetSocketAddress.createUnresolved(proxyHost, proxyPort)), excludes);
    }

    @Override
    public List<Proxy> select(URI uri) {
        if (!ProxyUtils.excludes(excludes, uri.getHost(), uri.getPort())) {
            return proxyList;
        } else {
            return NO_PROXY_LIST;
        }
    }

    @Override
    public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
        // Not used
    }
}
