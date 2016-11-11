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
