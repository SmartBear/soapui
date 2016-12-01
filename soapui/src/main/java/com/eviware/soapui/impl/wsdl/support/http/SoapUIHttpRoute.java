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

import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.util.LangUtils;

public class SoapUIHttpRoute {

    public static final String SOAPUI_SSL_CONFIG = "soapui.sslConfig";
    private HttpRoute httpRoute;
    private String param;

    public SoapUIHttpRoute(HttpRoute httpRoute) {
        this.httpRoute = httpRoute;
    }

    public final boolean equals(Object o) {
        if (o instanceof SoapUIHttpRoute) {
            SoapUIHttpRoute obj = (SoapUIHttpRoute) o;
            HttpRoute that = obj.getHttpRoute();

            boolean result = httpRoute.equals(that);
            if (result) {
                return param.equals(obj.getParam());
            }
        }
        return false;
    }

    public String getParam() {
        return this.param;
    }

    public HttpRoute getHttpRoute() {
        return this.httpRoute;
    }

    public synchronized int hashCode() {
        int hash = httpRoute.hashCode();
        hash = LangUtils.hashCode(hash, param);
        return hash;
    }

}
