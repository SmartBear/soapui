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
import com.eviware.soapui.impl.wsdl.mock.WsdlMockRequest;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockResponse;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockResult;
import com.eviware.soapui.impl.wsdl.submit.AbstractWsdlMessageExchange;
import com.eviware.soapui.model.iface.Attachment;
import com.eviware.soapui.model.iface.Response;
import com.eviware.soapui.support.types.StringToStringsMap;

import java.util.Vector;

/**
 * WsdlMessageExchange for a WsdlMockResponse, required for validations
 *
 * @author ole.matzura
 */

public class WsdlMockResponseMessageExchange extends AbstractWsdlMessageExchange<WsdlMockResponse> {
    public WsdlMockResponseMessageExchange(WsdlMockResponse mockResponse) {
        super(mockResponse);
    }

    public Attachment[] getRequestAttachments() {
        return null;
    }

    @Override
    public Response getResponse() {
        return null;
    }

    public String getEndpoint() {
        return getWsdlMockResult().getMockRequest().getHttpRequest().getRequestURI();
    }

    public String getRequestContent() {
        WsdlMockResult mockResult = getWsdlMockResult();
        WsdlMockRequest mockRequest = mockResult.getMockRequest();
        return mockRequest.getRequestContent();
    }

    public StringToStringsMap getRequestHeaders() {
        return null;
    }

    public Attachment[] getResponseAttachments() {
        return getModelItem().getAttachments();
    }

    public String getResponseContent() {
        return getModelItem().getResponseContent();
    }

    public StringToStringsMap getResponseHeaders() {
        return getModelItem().getResponseHeaders();
    }

    @Override
    public WsdlOperation getOperation() {
        return getModelItem().getMockOperation().getOperation();
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

    public Vector<?> getRequestWssResult() {
        return getWsdlMockResult().getRequestWssResult();
    }

    public Vector<?> getResponseWssResult() {
        return null;
    }

    public int getResponseStatusCode() {
        return getModelItem().getResponseHttpStatus();
    }

    public String getResponseContentType() {
        return getWsdlMockResult().getResponseContentType();
    }

    @Override
    public boolean hasRawData() {
        return true;
    }

    @Override
    public byte[] getRawResponseData() {
        return getWsdlMockResult().getRawResponseData();
    }

    public byte[] getRawRequestData() {
        return getModelItem().getMockResult().getMockRequest().getRawRequestData();
    }

    public WsdlMockResult getWsdlMockResult() {
        return (WsdlMockResult) getModelItem().getMockResult();
    }

}
