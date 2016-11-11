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

package com.eviware.soapui.impl.wsdl.monitor;

import com.eviware.soapui.impl.wsdl.submit.AbstractWsdlMessageExchange;
import com.eviware.soapui.impl.wsdl.support.wss.IncomingWss;
import com.eviware.soapui.model.iface.Operation;

import java.net.URL;
import java.util.Map;

public abstract class WsdlMonitorMessageExchange extends AbstractWsdlMessageExchange<Operation> {
    public WsdlMonitorMessageExchange(Operation modelItem) {
        super(modelItem);
    }

    public abstract URL getTargetUrl();

    public abstract void discard();

    public abstract String getRequestHost();

    public abstract long getRequestContentLength();

    public abstract long getResponseContentLength();

    public abstract void prepare(IncomingWss incomingRequestWss, IncomingWss incomingResponseWss);

    public abstract String getRequestMethod();

    public abstract Map<String, String> getHttpRequestParameters();

    public abstract String getQueryParameters();
}
