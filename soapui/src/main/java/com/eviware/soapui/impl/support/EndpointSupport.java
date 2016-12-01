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

package com.eviware.soapui.impl.support;

import com.eviware.soapui.config.AbstractRequestConfig;
import com.eviware.soapui.model.iface.Request;

public class EndpointSupport {
    public String getEndpoint(AbstractHttpRequest<AbstractRequestConfig> request) {
        return request.getConfig().getEndpoint();
    }

    public void setEndpoint(AbstractHttpRequest<AbstractRequestConfig> request, String endpoint) {
        String old = request.getEndpoint();
        if (old != null && old.equals(endpoint)) {
            return;
        }

        request.getConfig().setEndpoint(endpoint);
        request.notifyPropertyChanged(Request.ENDPOINT_PROPERTY, old, endpoint);
    }

}
