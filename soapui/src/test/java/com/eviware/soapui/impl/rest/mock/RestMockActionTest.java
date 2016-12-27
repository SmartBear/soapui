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

package com.eviware.soapui.impl.rest.mock;

import com.eviware.soapui.impl.rest.RestRequestInterface;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockRunContext;
import com.eviware.soapui.impl.wsdl.mock.dispatch.ScriptMockOperationDispatcher;
import com.eviware.soapui.support.types.StringToStringsMap;
import com.eviware.soapui.utils.ModelItemFactory;
import org.apache.commons.httpclient.HttpStatus;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.eviware.soapui.utils.MockedServlet.mockHttpServletRequest;
import static com.eviware.soapui.utils.MockedServlet.mockHttpServletResponse;
import static java.lang.Boolean.TRUE;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class RestMockActionTest {
    RestMockRequest restMockRequest;
    RestMockAction mockAction;

    RestMockResponse mockResponse;


    @Before
    public void setUp() throws Exception {
        restMockRequest = makeRestMockRequest();
        mockAction = ModelItemFactory.makeRestMockAction();
        mockResponse = mockAction.addNewMockResponse("response 1");
    }

    @Test
    public void testDispatchRequestReturnsHttpStatus() throws Exception {
        mockResponse.setResponseHttpStatus(HttpStatus.SC_BAD_REQUEST);

        RestMockResult mockResult = mockAction.dispatchRequest(restMockRequest);

        // HttpResponse is the response transferred over the wire.
        // So here we making sure the http status is actually set on the HttpResponse.
        verify(mockResult.getMockRequest().getHttpResponse()).setStatus(HttpStatus.SC_BAD_REQUEST);

        assertThat(mockResult.getMockResponse().getResponseHttpStatus(), is(HttpStatus.SC_BAD_REQUEST));
    }

    @Test
    public void testDispatchRequestReturnsResponseContent() throws Exception {
        String responseContent = "response content";
        mockResponse.setResponseContent(responseContent);

        RestMockResult mockResult = mockAction.dispatchRequest(restMockRequest);

        assertThat(mockResult.getMockResponse().getResponseContent(), is(responseContent));
    }

    @Test
    public void testDispatchRequestReturnsHttpHeader() throws Exception {
        StringToStringsMap responseHeaders = mockResponse.getResponseHeaders();
        String headerKey = "awesomekey";
        String headerValue = "awesomevalue";
        responseHeaders.add(headerKey, headerValue);
        mockResponse.setResponseHeaders(responseHeaders);

        RestMockResult mockResult = mockAction.dispatchRequest(restMockRequest);

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

        RestMockResult mockResult = mockAction.dispatchRequest(restMockRequest);

        // HttpResponse is the response transferred over the wire.
        // So here we making sure the header is actually set on the HttpResponse.
        verify(mockResult.getMockRequest().getHttpResponse()).addHeader(headerKey, expandedValue);

        assertThat(mockResult.getResponseHeaders().get(headerKey, ""), is(expandedValue));
        assertThat(mockResult.getMockResponse().getResponseHeaders().get(headerKey, ""), is(headerValue));

    }

    @Test
    public void testScriptIsExecuted() throws Exception {
        String mockServiceName = "RenamedFromScript";

        mockResponse.setName("MockResponse");
        mockResponse.setScript("mockResponse.setName('" + mockServiceName + "')");

        RestMockResult mockResult = mockAction.dispatchRequest(restMockRequest);

        assertThat(mockResult.getMockResponse().getName(), is(mockServiceName));
    }

    @Test
    public void shouldSetPath() {
        String updatedPath = "an/updatedpath";
        assertNotSame(updatedPath, mockAction.getResourcePath());

        mockAction.setResourcePath(updatedPath);

        assertThat(mockAction.getResourcePath(), is(updatedPath));
    }

    @Test
    public void shouldSetMethod() {
        mockAction.setMethod(RestRequestInterface.HttpMethod.TRACE);

        assertThat(mockAction.getMethod(), is(RestRequestInterface.HttpMethod.TRACE));
    }

    @Test
    public void testResponsesAreDispatchedSequentially() throws Exception {
        RestMockResult mockResult;
        mockAction.addNewMockResponse("response 2");

        mockResult = mockAction.dispatchRequest(restMockRequest);
        assertThat(mockResult.getMockResponse().getName(), is("response 1"));

        mockResult = mockAction.dispatchRequest(restMockRequest);
        assertThat(mockResult.getMockResponse().getName(), is("response 2"));

        mockResult = mockAction.dispatchRequest(restMockRequest);
        assertThat(mockResult.getMockResponse().getName(), is("response 1"));

        mockResult = mockAction.dispatchRequest(restMockRequest);
        assertThat(mockResult.getMockResponse().getName(), is("response 2"));
    }

    @Test
    public void testResponsesAreDispatchedSequentiallyForSingleResponse() throws Exception {
        RestMockResult mockResult;

        mockResult = mockAction.dispatchRequest(restMockRequest);
        assertThat(mockResult.getMockResponse().getName(), is("response 1"));

        mockResult = mockAction.dispatchRequest(restMockRequest);
        assertThat(mockResult.getMockResponse().getName(), is("response 1"));
    }

    @Test
    public void testResponsesAreDispatchedWithScript() throws Exception {
        mockAction.addNewMockResponse("response 2");

        mockAction.setDispatcher(new ScriptMockOperationDispatcher(mockAction));
        mockAction.setScript("return 'response 2'");

        RestMockResult mockResult = mockAction.dispatchRequest(restMockRequest);
        assertThat(mockResult.getMockResponse().getName(), is("response 2"));

        mockResult = mockAction.dispatchRequest(restMockRequest);
        assertThat(mockResult.getMockResponse().getName(), is("response 2"));
    }

    @Test
    public void testResponsesScriptDispatchingHasDefaultResponse() throws Exception {
        mockAction.addNewMockResponse("response 2");

        mockAction.setDispatcher(new ScriptMockOperationDispatcher(mockAction));
        mockAction.setDefaultResponse("response 2");
        mockAction.setScript("return 'absent response'");

        RestMockResult mockResult = mockAction.dispatchRequest(restMockRequest);
        assertThat(mockResult.getMockResponse().getName(), is("response 2"));
    }

    @Test
    public void testSetsDefaultDispatchScriptUponLoad() {
        String actualScript = mockAction.getScript().trim();
        assertThat(actualScript.startsWith("/*"), is(TRUE));
        assertThat(actualScript.endsWith("*/"), is(TRUE));
    }


    private RestMockRequest makeRestMockRequest() throws Exception {
        HttpServletRequest request = mockHttpServletRequest();
        HttpServletResponse response = mockHttpServletResponse();

        WsdlMockRunContext context = mock(WsdlMockRunContext.class);

        return new RestMockRequest(request, response, context);
    }
}
