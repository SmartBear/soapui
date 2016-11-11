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

package com.eviware.soapui.impl.support.wsa;

import com.eviware.soapui.config.HttpRequestConfig;
import com.eviware.soapui.impl.rest.RestRequestInterface;
import com.eviware.soapui.impl.support.http.HttpRequest;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.support.wsa.WsaConfig;
import com.eviware.soapui.impl.wsdl.support.wsa.WsaContainer;
import com.eviware.soapui.impl.wsdl.support.wsrm.WsrmConfig;
import com.eviware.soapui.impl.wsdl.support.wsrm.WsrmContainer;

public class WsaRequest extends HttpRequest implements WsaContainer, WsrmContainer {

    private WsaConfig wsaConfig;
    private WsrmConfig wsrmConfig;
    private WsdlOperation operation;
    private boolean wsrmEnabled;

    public WsaRequest(HttpRequestConfig httpRequestConfig, WsaConfig wsaConfig, WsrmConfig wsrmConfig,
                      boolean forLoadTest) {
        super(httpRequestConfig, forLoadTest);
        this.setWsaConfig(wsaConfig);
        this.setWsrmConfig(wsrmConfig);
    }

    public void setWsaConfig(WsaConfig wsaConfig) {
        this.wsaConfig = wsaConfig;
    }

    public WsaConfig getWsaConfig() {
        return wsaConfig;
    }

    public boolean isWsaEnabled() {
        return wsaConfig.isWsaEnabled();
    }

    public void setWsaEnabled(boolean arg0) {
        wsaConfig.setWsaEnabled(arg0);

    }

    public WsdlOperation getOperation() {
        return operation;
    }

    public RestRequestInterface.HttpMethod getMethod() {
        return RestRequestInterface.HttpMethod.POST;
    }

    public void setOperation(WsdlOperation operation) {
        this.operation = operation;

    }

    public WsrmConfig getWsrmConfig() {
        return wsrmConfig;
    }

    public boolean isWsrmEnabled() {
        return wsrmEnabled;
    }

    public void setWsrmEnabled(boolean arg0) {
        wsrmEnabled = arg0;

    }

    public void setWsrmConfig(WsrmConfig wsrmConfig) {
        this.wsrmConfig = wsrmConfig;
    }
}
