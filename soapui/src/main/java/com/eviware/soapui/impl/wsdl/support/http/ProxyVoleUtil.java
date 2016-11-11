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

import com.btr.proxy.search.ProxySearch;
import com.btr.proxy.util.PlatformUtil;

/**
 * @author Joel
 */
public class ProxyVoleUtil {
    public ProxySearch createAutoProxySearch() {
        ProxySearch proxySearch = new ProxySearch();
        proxySearch.addStrategy(ProxySearch.Strategy.JAVA);
        proxySearch.addStrategy(ProxySearch.Strategy.ENV_VAR);
        if (PlatformUtil.getCurrentPlattform() != PlatformUtil.Platform.WIN) {
            proxySearch.addStrategy(ProxySearch.Strategy.BROWSER);
            // For Windows both BROWSER and OS_DEFAULT will end up with an IEProxySearchStrategy.
            // The call in createPacSelector to winHttpDetectAutoProxyConfigUrl is quite slow and we don't want to do it twice.
        }
        proxySearch.addStrategy(ProxySearch.Strategy.OS_DEFAULT);
        return proxySearch;
    }
}
