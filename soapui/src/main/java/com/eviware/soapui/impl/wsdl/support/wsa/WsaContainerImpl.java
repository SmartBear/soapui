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

package com.eviware.soapui.impl.wsdl.support.wsa;

import com.eviware.soapui.impl.wsdl.WsdlOperation;

public class WsaContainerImpl implements WsaContainer {

    WsdlOperation operation;
    WsaConfig wsaConfig;
    boolean enabled = false;

    public WsdlOperation getOperation() {
        return operation;
    }

    public WsaConfig getWsaConfig() {
        return wsaConfig;
    }

    public boolean isWsaEnabled() {
        return enabled;
    }

    public void setWsaEnabled(boolean arg0) {
        enabled = arg0;

    }

    public void setOperation(WsdlOperation operation) {
        this.operation = operation;

    }

}
