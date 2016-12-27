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
