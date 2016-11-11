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
import com.eviware.soapui.impl.wsdl.mock.WsdlMockResponse;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockResult;
import com.eviware.soapui.impl.wsdl.submit.AbstractWsdlMessageExchange;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.Attachment;
import com.eviware.soapui.model.iface.Response;
import com.eviware.soapui.model.mock.MockResponse;
import com.eviware.soapui.model.mock.MockResult;
import com.eviware.soapui.support.types.StringToStringsMap;

import java.util.Vector;

/**
 * WsdlMessageExchange for a WsdlMockResult, required for validations
 *
 * @author ole.matzura
 */

public class WsdlMockResultMessageExchange extends AbstractWsdlMessageExchange<ModelItem> {
    private final MockResult mockResult;
    private MockResponse mockResponse;

    public WsdlMockResultMessageExchange(MockResult mockResult, MockResponse mockResponse) {
        super(mockResponse);

        this.mockResult = mockResult;
        this.mockResponse = mockResponse;
    }

    public ModelItem getModelItem() {
        return mockResponse == null ? mockResult.getMockOperation() : mockResponse;
    }

    public String getEndpoint() {
        return mockResult.getMockRequest().getHttpRequest().getRequestURI();
    }

    @Override
    public Response getResponse() {
        return null;
    }

    public Attachment[] getRequestAttachments() {
        return mockResult.getMockRequest().getRequestAttachments();
    }

    public String getRequestContent() {
        if (mockResult == null || mockResult.getMockRequest() == null) {
            return null;
        }

        return mockResult.getMockRequest().getRequestContent();
    }

    public StringToStringsMap getRequestHeaders() {
        return mockResult == null ? null : mockResult.getMockRequest().getRequestHeaders();
    }

    public Attachment[] getResponseAttachments() {
        return mockResult == null || mockResponse == null ? new Attachment[0] : mockResult.getMockResponse()
                .getAttachments();
    }

    public String getResponseContent() {
        return mockResult == null ? null : mockResult.getResponseContent();
    }

    public StringToStringsMap getResponseHeaders() {
        return mockResult == null ? new StringToStringsMap() : mockResult.getResponseHeaders();
    }

    public WsdlOperation getOperation() {
        if (mockResponse != null && mockResponse instanceof WsdlMockResponse) {
            WsdlMockResponse wsdlMockResponse = (WsdlMockResponse) mockResponse;
            if (mockResult.getMockOperation() != null) {
                return (WsdlOperation) mockResult.getMockOperation().getOperation();
            }

            return wsdlMockResponse.getMockOperation().getOperation();
        }
        return null;
    }

    public long getTimeTaken() {
        return mockResult == null ? -1 : mockResult.getTimeTaken();
    }

    public long getTimestamp() {
        return mockResult == null ? -1 : mockResult.getTimestamp();
    }

    public boolean isDiscarded() {
        return mockResponse == null;
    }

    public void discard() {
        mockResponse = null;
    }

    public Vector<?> getRequestWssResult() {
        if (mockResult != null && mockResult instanceof WsdlMockResult) {
            return ((WsdlMockResult) mockResult).getRequestWssResult();
        }
        return null;
    }

    public Vector<?> getResponseWssResult() {
        return null;
    }

    public int getResponseStatusCode() {
        return mockResponse.getResponseHttpStatus();
    }

    public String getResponseContentType() {
        return mockResult.getMockResponse().getContentType();
    }

    @Override
    public byte[] getRawRequestData() {
        return mockResult.getMockRequest().getRawRequestData();
    }

    @Override
    public byte[] getRawResponseData() {
        return mockResult.getRawResponseData();
    }

    @Override
    public boolean hasRawData() {
        return true;
    }
}
