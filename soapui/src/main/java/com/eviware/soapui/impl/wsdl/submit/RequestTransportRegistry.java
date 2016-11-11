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

package com.eviware.soapui.impl.wsdl.submit;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.submit.filters.EndpointRequestFilter;
import com.eviware.soapui.impl.wsdl.submit.filters.EndpointStrategyRequestFilter;
import com.eviware.soapui.impl.wsdl.submit.filters.GlobalHttpHeadersRequestFilter;
import com.eviware.soapui.impl.wsdl.submit.filters.HttpAuthenticationRequestFilter;
import com.eviware.soapui.impl.wsdl.submit.filters.HttpCompressionRequestFilter;
import com.eviware.soapui.impl.wsdl.submit.filters.HttpPackagingResponseFilter;
import com.eviware.soapui.impl.wsdl.submit.filters.HttpSettingsRequestFilter;
import com.eviware.soapui.impl.wsdl.submit.filters.OAuth2RequestFilter;
import com.eviware.soapui.impl.wsdl.submit.filters.PostPackagingRequestFilter;
import com.eviware.soapui.impl.wsdl.submit.filters.PropertyExpansionRequestFilter;
import com.eviware.soapui.impl.wsdl.submit.filters.RemoveEmptyContentRequestFilter;
import com.eviware.soapui.impl.wsdl.submit.filters.RestRequestFilter;
import com.eviware.soapui.impl.wsdl.submit.filters.SoapHeadersRequestFilter;
import com.eviware.soapui.impl.wsdl.submit.filters.StripWhitespacesRequestFilter;
import com.eviware.soapui.impl.wsdl.submit.filters.WsaRequestFilter;
import com.eviware.soapui.impl.wsdl.submit.filters.WsdlPackagingRequestFilter;
import com.eviware.soapui.impl.wsdl.submit.filters.WsrmRequestFilter;
import com.eviware.soapui.impl.wsdl.submit.filters.WssAuthenticationRequestFilter;
import com.eviware.soapui.impl.wsdl.submit.filters.WssRequestFilter;
import com.eviware.soapui.impl.wsdl.submit.transports.http.HttpClientRequestTransport;
import com.eviware.soapui.impl.wsdl.submit.transports.jms.HermesJmsRequestTransport;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.support.factory.SoapUIFactoryRegistryListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Registry of available transports, currently hard-coded but should be
 * configurable in the future.
 *
 * @author Ole.Matzura
 */

public class RequestTransportRegistry {
    public static final String HTTP = "http";
    public static final String HTTPS = "https";
    public static final String JMS = "jms";

    private static Map<String, RequestTransport> transports = new HashMap<String, RequestTransport>();
    private static Map<String, List<RequestFilter>> addedCustomRequestFilters = new HashMap<String, List<RequestFilter>>();

    private static WsdlPackagingRequestFilter wsdlPackagingRequestFilter;

    static {
        HttpClientRequestTransport httpTransport = new HttpClientRequestTransport();
        HermesJmsRequestTransport jmsTransport = new HermesJmsRequestTransport();

        List<RequestFilterFactory> filterFactories = SoapUI.getFactoryRegistry()
                .getFactories(RequestFilterFactory.class);

        httpTransport.addRequestFilter(new EndpointRequestFilter());
        httpTransport.addRequestFilter(new HttpSettingsRequestFilter());
        httpTransport.addRequestFilter(new RestRequestFilter());
        httpTransport.addRequestFilter(new SoapHeadersRequestFilter());
        httpTransport.addRequestFilter(new HttpAuthenticationRequestFilter());
        httpTransport.addRequestFilter(new WssAuthenticationRequestFilter());
        httpTransport.addRequestFilter(new PropertyExpansionRequestFilter());
        httpTransport.addRequestFilter(new RemoveEmptyContentRequestFilter());
        httpTransport.addRequestFilter(new StripWhitespacesRequestFilter());
        httpTransport.addRequestFilter(new EndpointStrategyRequestFilter());
        httpTransport.addRequestFilter(new WsaRequestFilter());
        httpTransport.addRequestFilter(new WsrmRequestFilter());
        httpTransport.addRequestFilter(new WssRequestFilter());
        httpTransport.addRequestFilter(new OAuth2RequestFilter());
        httpTransport.addRequestFilter(new GlobalHttpHeadersRequestFilter());

        addListenerRequestFilters(httpTransport);

        for (RequestFilterFactory factory : filterFactories) {
            String protocol = factory.getProtocol();
            if (protocol.equals(HTTP) || protocol.equals(HTTPS)) {
                RequestFilter requestFilter = factory.createRequestFilter();
                httpTransport.addRequestFilter(requestFilter);

                addToCustomRequestFilters(protocol, requestFilter);
            }
        }

        wsdlPackagingRequestFilter = new WsdlPackagingRequestFilter();
        httpTransport.addRequestFilter(wsdlPackagingRequestFilter);
        httpTransport.addRequestFilter(new HttpCompressionRequestFilter());
        httpTransport.addRequestFilter(new HttpPackagingResponseFilter());
        httpTransport.addRequestFilter(new PostPackagingRequestFilter());

        transports.put(HTTP, httpTransport);
        transports.put(HTTPS, httpTransport);

        jmsTransport.addRequestFilter(new WssAuthenticationRequestFilter());
        jmsTransport.addRequestFilter(new PropertyExpansionRequestFilter());
        jmsTransport.addRequestFilter(new RemoveEmptyContentRequestFilter());
        jmsTransport.addRequestFilter(new StripWhitespacesRequestFilter());
        jmsTransport.addRequestFilter(new WsaRequestFilter());
        jmsTransport.addRequestFilter(new WssRequestFilter());

        addListenerRequestFilters(jmsTransport);

        for (RequestFilterFactory factory : filterFactories) {
            if (factory.getProtocol().equals(JMS)) {
                RequestFilter requestFilter = factory.createRequestFilter();
                jmsTransport.addRequestFilter(requestFilter);

                addToCustomRequestFilters(JMS, requestFilter);
            }
        }

        transports.put(JMS, jmsTransport);
        initCustomTransports(filterFactories);

        SoapUI.getFactoryRegistry().addFactoryRegistryListener(new SoapUIFactoryRegistryListener() {
            @Override
            public void factoryAdded(Class<?> factoryType, Object factory) {
                if (factory instanceof RequestTransportFactory) {
                    RequestTransportFactory transportFactory = (RequestTransportFactory) factory;
                    addTransport(transportFactory.getProtocol(), transportFactory.newRequestTransport());
                }
                if (factory instanceof RequestFilterFactory) {
                    RequestFilterFactory requestFilterFactory = (RequestFilterFactory) factory;

                    RequestFilter filter = requestFilterFactory.createRequestFilter();
                    String protocol = requestFilterFactory.getProtocol();

                    if (protocol.startsWith(HTTP)) {
                        RequestTransport transport = transports.get(HTTP);
                        transport.insertRequestFilter(filter, wsdlPackagingRequestFilter);
                    } else {
                        RequestTransport transport = transports.get(protocol);
                        if (transport != null) {
                            transport.addRequestFilter(filter);
                        }
                    }

                    addToCustomRequestFilters(protocol, filter);
                }
            }

            @Override
            public void factoryRemoved(Class<?> factoryType, Object factory) {
                if (factory instanceof RequestTransportFactory) {
                    removeFactory((RequestTransportFactory) factory);
                }
                if (factory instanceof RequestFilterFactory) {
                    removeRequestFilterFactory((RequestFilterFactory) factory);
                }
            }
        });
    }

    private static void addListenerRequestFilters(RequestTransport transport) {
        for (RequestFilter filter : SoapUI.getListenerRegistry().getListeners(RequestFilter.class)) {
            transport.addRequestFilter(filter);
        }
    }

    private static void initCustomTransports(List<RequestFilterFactory> filterFactories) {
        for (RequestTransportFactory factory : SoapUI.getFactoryRegistry().getFactories(RequestTransportFactory.class)) {
            RequestTransport transport = factory.newRequestTransport();
            String protocol = factory.getProtocol();

            for (RequestFilterFactory filterFactory : filterFactories) {
                if (filterFactory.getProtocol().equals(protocol)) {
                    RequestFilter requestFilter = filterFactory.createRequestFilter();
                    transport.addRequestFilter(requestFilter);

                    addToCustomRequestFilters(protocol, requestFilter);
                }
            }

            transports.put(protocol, transport);
        }
    }

    private static void addToCustomRequestFilters(String protocol, RequestFilter requestFilter) {
        if (!addedCustomRequestFilters.containsKey(protocol)) {
            addedCustomRequestFilters.put(protocol, new ArrayList<RequestFilter>());
        }

        addedCustomRequestFilters.get(protocol).add(requestFilter);
    }

    public static void removeRequestFilterFactory(RequestFilterFactory factory) {
        String protocol = factory.getProtocol();
        if (addedCustomRequestFilters.containsKey(protocol)) {
            for (RequestFilter filter : addedCustomRequestFilters.get(protocol)) {
                for (RequestTransport transport : transports.values()) {
                    transport.removeRequestFilter(filter);
                }
            }

            addedCustomRequestFilters.remove(protocol);
        }
    }

    public static synchronized RequestTransport getTransport(String endpoint, SubmitContext submitContext)
            throws MissingTransportException, CannotResolveJmsTypeException {
        int ix = endpoint.indexOf("://");
        if (ix == -1) {
            throw new MissingTransportException("Missing protocol in endpoint [" + endpoint + "]");
        }

        String protocol = endpoint.substring(0, ix).toLowerCase();

        RequestTransport transport = transports.get(protocol);

        if (transport == null) {
            throw new MissingTransportException("Missing transport for protocol [" + protocol + "]");
        }

        return transport;
    }

    public static synchronized RequestTransport getTransport(String protocol) throws MissingTransportException {
        RequestTransport transport = transports.get(protocol);

        if (transport == null) {
            throw new MissingTransportException("Missing transport for protocol [" + protocol + "]");
        }

        return transport;
    }

    public static void addTransport(String key, RequestTransport rt) {
        transports.put(key, rt);
    }

    public static void removeFactory(RequestTransportFactory factory) {
        RequestTransport transport = factory.newRequestTransport();
        for (Map.Entry<String, RequestTransport> transportEntry : transports.entrySet()) {
            if (transportEntry.getValue().getClass().equals(transport.getClass())) {
                transports.remove(transportEntry.getKey());
                break;
            }
        }
    }

    public static class MissingTransportException extends Exception {
        public MissingTransportException(String msg) {
            super(msg);
        }
    }

    public static class CannotResolveJmsTypeException extends Exception {
        public CannotResolveJmsTypeException(String msg) {
            super(msg);
        }
    }

}
