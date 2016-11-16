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

package com.eviware.soapui.impl.wsdl.submit.transports.http;

import com.eviware.soapui.impl.settings.XmlBeansSettingsImpl;
import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.impl.wsdl.submit.RequestFilter;
import com.eviware.soapui.impl.wsdl.support.http.HttpClientSupport;
import com.eviware.soapui.model.iface.Request;
import com.eviware.soapui.model.iface.Response;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.support.AbstractSubmitContext;
import com.eviware.soapui.support.types.StringToStringsMap;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.protocol.HttpContext;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the HttpClientRequestTransport class.
 */
public class HttpClientRequestTransportTest {

    private HttpClientRequestTransport httpTransport;
    private ExtendedHttpMethod methodExecuted;
    private HttpContext contextUsed;

    @Before
    public void setUp() {
        httpTransport = new TestableHttpClientRequestTransport();
        httpTransport.addRequestFilter(new StubbedRequestSetupFilter());
        methodExecuted = null;
        contextUsed = null;
    }

    @Test
    public void processesRequestCorrectly() throws Exception {
        StringToStringsMap emptyHeaders = new StringToStringsMap();
        AbstractHttpRequest request = prepareRequestWithHeaders(emptyHeaders);
        SubmitContext submitContext = new StubbedSubmitContext(request);

        httpTransport.sendRequest(submitContext, request);
        assertThat(methodExecuted, is(notNullValue()));
        assertThat(contextUsed, is(notNullValue()));
    }

    @Test
    public void expandsPropertiesInHeaderName() throws Exception {
        StringToStringsMap headers = new StringToStringsMap();
        String headerValue = "The value";
        headers.add("Header-for-${request}", headerValue);
        AbstractHttpRequest request = prepareRequestWithHeaders(headers);
        SubmitContext submitContext = new StubbedSubmitContext(request);
        String requestName = "Fin-fin request";
        submitContext.setProperty("request", requestName);

        httpTransport.sendRequest(submitContext, request);
        String expectedHeaderName = "Header-for-" + requestName;
        Header[] modifiedHeaders = methodExecuted.getHeaders(expectedHeaderName);
        assertThat(modifiedHeaders.length, is(1));
        assertThat(modifiedHeaders[0].getName(), is(expectedHeaderName));
        assertThat(modifiedHeaders[0].getValue(), is(headerValue));
    }

    private AbstractHttpRequest prepareRequestWithHeaders(StringToStringsMap headers) {
        AbstractHttpRequest request = mock(AbstractHttpRequest.class);
        when(request.getRequestHeaders()).thenReturn(headers);
        XmlBeansSettingsImpl emptySettings = mock(XmlBeansSettingsImpl.class);
        when(request.getSettings()).thenReturn(emptySettings);
        return request;
    }

    private class TestableHttpClientRequestTransport extends HttpClientRequestTransport {
        @Override
        protected HttpClientSupport.SoapUIHttpClient getSoapUIHttpClient() {
            return mock(HttpClientSupport.SoapUIHttpClient.class);
        }

        @Override
        protected int getDefaultHttpPort(ExtendedHttpMethod httpMethod, HttpClient httpClient) {
            return 80;
        }

        @Override
        protected HttpResponse submitRequest(ExtendedHttpMethod httpMethod, HttpContext httpContext) throws IOException {
            methodExecuted = httpMethod;
            contextUsed = httpContext;
            return makeSuccessfulResponse();
        }

        private HttpResponse makeSuccessfulResponse() {
            HttpResponse mockResponse = mock(HttpResponse.class);
            StatusLine mockedStatusLine = mock(StatusLine.class);
            when(mockResponse.getStatusLine()).thenReturn(mockedStatusLine);
            when(mockedStatusLine.getStatusCode()).thenReturn(200);
            return mockResponse;
        }
    }

    private class StubbedRequestSetupFilter implements RequestFilter {
        @Override
        public void filterRequest(SubmitContext context, Request request) {
            ExtendedHttpMethod httpMethod = (ExtendedHttpMethod) context.getProperty(BaseHttpRequestTransport.HTTP_METHOD);
            try {
                httpMethod.setURI(new URI("/index.html"));
            } catch (URISyntaxException e) {
                throw new Error(e);
            }
        }

        @Override
        public void afterRequest(SubmitContext context, Request request) {
        }

        @Override
        public void afterRequest(SubmitContext context, Response response) {
        }
    }


    private class StubbedSubmitContext extends AbstractSubmitContext<Request> {

        public StubbedSubmitContext(Request modelItem) {
            super(modelItem);
        }

        @Override
        public Object getProperty(String name) {
            return getProperty(name, null, null);
        }

    }
}
