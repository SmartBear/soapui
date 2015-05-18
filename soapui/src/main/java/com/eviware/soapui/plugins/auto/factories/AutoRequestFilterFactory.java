package com.eviware.soapui.plugins.auto.factories;

import com.eviware.soapui.impl.wsdl.submit.RequestFilter;
import com.eviware.soapui.impl.wsdl.submit.RequestFilterFactory;
import com.eviware.soapui.plugins.auto.PluginRequestFilter;

/**
 * Created by ole on 15/06/14.
 */
public class AutoRequestFilterFactory extends SimpleSoapUIFactory<RequestFilter> implements RequestFilterFactory {
    private final String protocol;

    public AutoRequestFilterFactory(PluginRequestFilter annotation, Class<RequestFilter> requestFilterClass) {
        super(RequestFilterFactory.class, requestFilterClass);
        protocol = annotation.protocol();
    }

    @Override
    public String getProtocol() {
        return protocol;
    }

    @Override
    public RequestFilter createRequestFilter() {
        return create();
    }
}
