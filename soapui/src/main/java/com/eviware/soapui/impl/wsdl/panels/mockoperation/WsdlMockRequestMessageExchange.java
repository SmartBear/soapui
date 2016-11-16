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

package com.eviware.soapui.impl.wsdl.panels.mockoperation;

import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockOperation;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockRequest;
import com.eviware.soapui.impl.wsdl.submit.AbstractWsdlMessageExchange;
import com.eviware.soapui.model.iface.Attachment;
import com.eviware.soapui.model.iface.Response;
import com.eviware.soapui.support.types.StringToStringsMap;

import java.util.Vector;

public class WsdlMockRequestMessageExchange extends AbstractWsdlMessageExchange<WsdlMockOperation> {
    private final WsdlMockRequest request;

    public WsdlMockRequestMessageExchange(WsdlMockRequest request, WsdlMockOperation mockOperation) {
        super(mockOperation);
        this.request = request;
    }

    public String getEndpoint() {
        return request.getHttpRequest().getRequestURI();
    }

    @Override
    public Response getResponse() {
        return null;
    }

    @Override
    public WsdlOperation getOperation() {
        return getModelItem().getOperation();
    }

    public Vector<?> getRequestWssResult() {
        return null;
    }

    public Vector<?> getResponseWssResult() {
        return null;
    }

    public Attachment[] getRequestAttachments() {
        return request.getRequestAttachments();
    }

    public String getRequestContent() {
        return request.getRequestContent();
    }

    public StringToStringsMap getRequestHeaders() {
        return request.getRequestHeaders();
    }

    public Attachment[] getResponseAttachments() {
        return null;
    }

    public String getResponseContent() {
        return null;
    }

    public StringToStringsMap getResponseHeaders() {
        return new StringToStringsMap();
    }

    public long getTimeTaken() {
        return 0;
    }

    public long getTimestamp() {
        return 0;
    }

    public boolean isDiscarded() {
        return false;
    }

    public int getResponseStatusCode() {
        return 0;
    }

    public String getResponseContentType() {
        return null;
    }
}
