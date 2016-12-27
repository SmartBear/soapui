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

import com.btr.proxy.util.UriFilter;

import java.net.URI;

/**
 * An UriFilter that accepts all schemes except the supplied ones.
 *
 * @author Joel
 */
class SchemeProxyFilter implements UriFilter {
    private String[] unacceptedSchemes;

    public SchemeProxyFilter(String... unacceptedSchemes) {
        this.unacceptedSchemes = unacceptedSchemes;
    }

    @Override
    public boolean accept(URI uri) {
        for (String unacceptedScheme : unacceptedSchemes) {
            if (unacceptedScheme.equalsIgnoreCase(uri.getScheme())) {
                return false;
            }
        }
        return true;
    }
}
