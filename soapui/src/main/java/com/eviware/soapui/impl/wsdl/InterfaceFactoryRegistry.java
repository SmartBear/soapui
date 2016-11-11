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

package com.eviware.soapui.impl.wsdl;

import com.eviware.soapui.config.InterfaceConfig;
import com.eviware.soapui.impl.InterfaceFactory;
import com.eviware.soapui.impl.WsdlInterfaceFactory;
import com.eviware.soapui.impl.rest.RestServiceFactory;
import com.eviware.soapui.impl.support.AbstractInterface;
import com.eviware.soapui.support.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class InterfaceFactoryRegistry {
    private static Map<String, InterfaceFactory<?>> factories = new HashMap<String, InterfaceFactory<?>>();

    static {
        factories.put(WsdlInterfaceFactory.WSDL_TYPE, new WsdlInterfaceFactory());
        factories.put(RestServiceFactory.REST_TYPE, new RestServiceFactory());
    }

    public static AbstractInterface<?> createNew(WsdlProject project, String type, String name) {
        if (!factories.containsKey(type)) {
            throw new RuntimeException("Unknown interface type [" + type + "]");
        }

        return factories.get(type).createNew(project, name);
    }

    public static AbstractInterface<?> build(WsdlProject project, InterfaceConfig config) {
        String type = config.getType();
        if (StringUtils.isNullOrEmpty(type)) {
            type = WsdlInterfaceFactory.WSDL_TYPE;
        }

        return factories.get(type).build(project, config);
    }
}
