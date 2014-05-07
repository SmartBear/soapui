/*
 * Copyright 2004-2014 SmartBear Software
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

package com.eviware.soapui.model.mock;

import com.eviware.soapui.impl.wsdl.mock.WsdlMockRunContext;

public interface MockServer {

    public String getPath();

    public void setPath(String path);

    public int getPort();

    public void setPort(int i);

    public MockRunner getMockRunner();

    public MockRunner start() throws Exception;

    /**
     * Start this mock service if HttpSetting.START_MOCK_SERVICE is true.
     *
     * @throws Exception if the start fails for some reason. One case may be that the port is occupied already.
     */
    public void startIfConfigured() throws Exception;

    public boolean getBindToHostOnly();

    public String getLocalEndpoint();

    public MockDispatcher createDispatcher(WsdlMockRunContext mockContext);

    public String getHost();

    public void addMockRunListener(MockRunListener listener);

    public void removeMockRunListener(MockRunListener listener);

    public MockRunListener[] getMockRunListeners();

}
