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

package com.eviware.soapui.impl.wsdl.support;

import com.eviware.soapui.impl.wsdl.panels.teststeps.JdbcResponse;
import com.eviware.soapui.impl.wsdl.teststeps.JdbcRequestTestStep;
import com.eviware.soapui.model.iface.Response;

/**
 * @author ole.matzura
 */

public class JdbcMessageExchange extends AbstractNonHttpMessageExchange<JdbcRequestTestStep> {
    private final JdbcResponse response;

    public JdbcMessageExchange(JdbcRequestTestStep modelItem, JdbcResponse response) {
        super(modelItem);
        this.response = response;
    }

    @Override
    public Response getResponse() {
        return response;
    }

    public String getRequestContent() {
        return response.getRequestContent();
    }

    public String getResponseContent() {
        return response == null? null : response.getContentAsString();
    }

    public long getTimeTaken() {
        return response.getTimeTaken();
    }

    public long getTimestamp() {
        return response.getTimestamp();
    }

    public boolean hasRequest(boolean ignoreEmpty) {
        return true;
    }

    public boolean hasResponse() {
        return getResponseContent() != null;
    }

    public boolean isDiscarded() {
        return false;
    }

    public String getEndpoint() {
        // TODO Auto-generated method stub
        return null;
    }
}
