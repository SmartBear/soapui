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
package com.eviware.soapui.impl.rest.mock;

import com.eviware.soapui.impl.wsdl.mock.WsdlMockRunContext;
import com.eviware.soapui.model.mock.MockResult;
import com.eviware.soapui.support.SoapUIException;
import com.eviware.soapui.utils.ModelItemFactory;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class RestMockResponseWriterTest {

    private RestMockResponse mockResponse;
    private MockResult result;
    private String originalResponseContent = "<content>awful lot of content</content>";;
    private HttpServletRequest servletRequest;
    private HttpServletResponse servletResponse;

    @Before
    public void setUp() throws IOException, SoapUIException {
        mockHttpServletRequest();
        mockHttpServletResponse();
        mockResponse = ModelItemFactory.makeRestResponse();
        result = createMockResult();
    }

    @Test
    public void writesResponse() throws Exception {
        String writtenResponseContent = mockResponse.writeResponse(result, originalResponseContent);

        assertThat(writtenResponseContent, is( originalResponseContent ));
    }

    @Test
    public void shouldWriteContentLength() throws Exception {
        mockResponse.writeResponse(result, originalResponseContent);
        verify(servletResponse).addHeader("Content-Length", "" + originalResponseContent.getBytes().length);
    }

    @Test
    public void shouldNotWriteContentLengthIfTransferEncodingIsPresent() throws Exception {
        result.addHeader("Transfer-Encoding", "chunked");
        mockResponse.writeResponse(result, originalResponseContent);
        verify(servletResponse, never()).addHeader(eq("Content-Length"), anyString());
    }

    public MockResult createMockResult() throws IOException {
        WsdlMockRunContext runContext = mock(WsdlMockRunContext.class);
        return new RestMockResult(createMockRequest(runContext));
    }

    public RestMockRequest createMockRequest(WsdlMockRunContext runContext) throws IOException {
        return new RestMockRequest(servletRequest, servletResponse, runContext);
    }

    private void mockHttpServletRequest() {
        servletRequest = mock(HttpServletRequest.class);
        when(servletRequest.getHeaderNames()).thenReturn(headerNames());
    }

    private void mockHttpServletResponse() throws IOException {
        servletResponse = mock(HttpServletResponse.class);
        ServletOutputStream os = mock(ServletOutputStream.class);
        when(servletResponse.getOutputStream()).thenReturn(os);
    }

    private Enumeration headerNames() {
        return new Enumeration() {
            @Override
            public boolean hasMoreElements() {
                return false;
            }

            @Override
            public Object nextElement() {
                return null;
            }
        };
    }
}
