package com.eviware.soapui.plugins.auto.factories;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.submit.RequestTransport;
import com.eviware.soapui.impl.wsdl.submit.RequestTransportFactory;
import com.eviware.soapui.plugins.auto.PluginRequestTransport;

/**
 * Created by ole on 14/06/14.
 */
public class AutoRequestTransportFactory extends
        SimpleSoapUIFactory<RequestTransport> implements RequestTransportFactory {
    private String protocol;

    public AutoRequestTransportFactory(PluginRequestTransport annotation, Class<RequestTransport> requestTransportClass) {
        super(RequestTransportFactory.class, requestTransportClass);
        protocol = requestTransportClass.getAnnotation(PluginRequestTransport.class).protocol();
        SoapUI.log("Added RequestTransport for protocol [" + protocol + "]");
    }

    @Override
    public RequestTransport newRequestTransport() {
        return create();
    }

    @Override
    public String getProtocol() {
        return protocol;
    }
}
