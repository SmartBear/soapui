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

package com.eviware.soapui.impl.wsdl.mock;

import com.eviware.soapui.support.types.StringToStringsMap;
import com.eviware.soapui.utils.ModelItemFactory;
import org.apache.commons.httpclient.HttpStatus;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Enumeration;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class WsdlMockOperationTest {
    WsdlMockRequest restMockRequest;
    WsdlMockResponse mockResponse;
    WsdlMockOperation mockOperation;

    @Before
    public void setUp() throws Exception {
        restMockRequest = makeWsdlMockRequest();
        mockResponse = ModelItemFactory.makeWsdlMockResponse();
        mockOperation = mockResponse.getMockOperation();
        mockOperation.addMockResponse(mockResponse);

    }

    @Test
    public void testDispatchRequestReturnsHttpStatus() throws Exception {
        mockResponse.setResponseHttpStatus(HttpStatus.SC_BAD_REQUEST);

        WsdlMockResult mockResult = mockOperation.dispatchRequest(restMockRequest);

        // HttpResponse is the response transferred over the wire.
        // So here we making sure the http status is actually set on the HttpResponse.
        verify(mockResult.getMockRequest().getHttpResponse()).setStatus(HttpStatus.SC_BAD_REQUEST);

        assertThat(mockResult.getMockResponse().getResponseHttpStatus(), is(HttpStatus.SC_BAD_REQUEST));
    }

    @Test
    public void testDispatchRequestReturnsResponseContent() throws Exception {
        String responseContent = "mock response content";
        mockResponse.setResponseContent(responseContent);

        WsdlMockResult mockResult = mockOperation.dispatchRequest(restMockRequest);

        assertThat(mockResult.getMockResponse().getResponseContent(), is(responseContent));
    }

    @Test
    public void testDispatchRequestReturnsHttpHeader() throws Exception {
        StringToStringsMap responseHeaders = mockResponse.getResponseHeaders();
        String headerKey = "awesomekey";
        String headerValue = "awesomevalue";
        responseHeaders.add(headerKey, headerValue);
        mockResponse.setResponseHeaders(responseHeaders);

        WsdlMockResult mockResult = mockOperation.dispatchRequest(restMockRequest);

        // HttpResponse is the response transferred over the wire.
        // So here we making sure the header is actually set on the HttpResponse.
        verify(mockResult.getMockRequest().getHttpResponse()).addHeader(headerKey, headerValue);

        assertThat(mockResult.getResponseHeaders().get(headerKey, ""), is(headerValue));
        assertThat(mockResult.getMockResponse().getResponseHeaders().get(headerKey, ""), is(headerValue));
    }

    @Test
    public void testDispatchRequestReturnsExpandedHttpHeader() throws Exception {
        String expandedValue = "application/json; charset=iso-8859-1";
        mockResponse.getMockOperation().getMockService().setPropertyValue("ContentType", expandedValue);

        StringToStringsMap responseHeaders = mockResponse.getResponseHeaders();
        String headerKey = "ContentType";
        String headerValue = "${#MockService#ContentType}";
        responseHeaders.add(headerKey, headerValue);
        mockResponse.setResponseHeaders(responseHeaders);

        WsdlMockResult mockResult = mockOperation.dispatchRequest(restMockRequest);

        // HttpResponse is the response transferred over the wire.
        // So here we making sure the header is actually set on the HttpResponse.
        verify(mockResult.getMockRequest().getHttpResponse()).addHeader(headerKey, expandedValue);

        assertThat(mockResult.getResponseHeaders().get(headerKey, ""), is(expandedValue));
        assertThat(mockResult.getMockResponse().getResponseHeaders().get(headerKey, ""), is(headerValue));

    }

    private WsdlMockRequest makeWsdlMockRequest() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        Enumeration enumeration = mock(Enumeration.class);
        when(request.getHeaderNames()).thenReturn(enumeration);

        HttpServletResponse response = mock(HttpServletResponse.class);
        ServletOutputStream os = mock(ServletOutputStream.class);
        when(response.getOutputStream()).thenReturn(os);

        WsdlMockRunContext context = mock(WsdlMockRunContext.class);

        return new WsdlMockRequest(request, response, context);
    }
}
