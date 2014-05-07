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

package com.eviware.soapui.impl.wsdl.submit;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.submit.filters.*;
import com.eviware.soapui.impl.wsdl.submit.transports.http.HttpClientRequestTransport;
import com.eviware.soapui.impl.wsdl.submit.transports.jms.HermesJmsRequestTransport;
import com.eviware.soapui.model.iface.SubmitContext;

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

        for (RequestFilter filter : SoapUI.getListenerRegistry().getListeners(RequestFilter.class)) {
            httpTransport.addRequestFilter(filter);
        }

        for (RequestFilterFactory factory : filterFactories) {
            if (factory.getProtocol().equals(HTTP) || factory.getProtocol().equals(HTTPS)) {
                httpTransport.addRequestFilter(factory.createRequestFilter());
            }
        }

        httpTransport.addRequestFilter(new WsdlPackagingRequestFilter());
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

        for (RequestFilter filter : SoapUI.getListenerRegistry().getListeners(RequestFilter.class)) {
            jmsTransport.addRequestFilter(filter);
        }

        for (RequestFilterFactory factory : filterFactories) {
            if (factory.getProtocol().equals(JMS)) {
                jmsTransport.addRequestFilter(factory.createRequestFilter());
            }
        }

        transports.put(JMS, jmsTransport);

        for (RequestTransportFactory factory : SoapUI.getFactoryRegistry().getFactories(RequestTransportFactory.class)) {
            RequestTransport transport = factory.newRequestTransport();
            String protocol = factory.getProtocol();

            for (RequestFilterFactory filterFactory : filterFactories) {
                if (filterFactory.getProtocol().equals(protocol)) {
                    transport.addRequestFilter(filterFactory.createRequestFilter());
                }
            }

            transports.put(protocol, transport);
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
