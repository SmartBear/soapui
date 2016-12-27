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

package com.eviware.soapui.impl.rest;

import com.eviware.soapui.config.InterfaceConfig;
import com.eviware.soapui.config.RestServiceConfig;
import com.eviware.soapui.impl.InterfaceFactory;
import com.eviware.soapui.impl.wsdl.WsdlProject;

public class RestServiceFactory implements InterfaceFactory<RestService> {
    public final static String REST_TYPE = "rest";

    public RestService build(WsdlProject project, InterfaceConfig config) {
        return new RestService(project, (RestServiceConfig) config.changeType(RestServiceConfig.type));
    }

    public RestService createNew(WsdlProject project, String name) {
        RestServiceConfig config = (RestServiceConfig) project.getConfig().addNewInterface()
                .changeType(RestServiceConfig.type);
        RestService iface = new RestService(project, config);
        iface.setName(name);

        return iface;
    }
}
