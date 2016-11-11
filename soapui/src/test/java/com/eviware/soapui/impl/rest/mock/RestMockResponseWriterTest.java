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

import com.eviware.soapui.impl.wsdl.mock.WsdlMockRunContext;
import com.eviware.soapui.model.mock.MockResult;
import com.eviware.soapui.utils.ModelItemFactory;
import org.apache.commons.httpclient.HttpStatus;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.eviware.soapui.utils.MockedServlet.mockHttpServletRequest;
import static com.eviware.soapui.utils.MockedServlet.mockHttpServletResponse;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class RestMockResponseWriterTest {

    private RestMockResponse mockResponse;
    private MockResult result;
    private String originalResponseContent = "<content>awful lot of content</content>";;
    private HttpServletRequest servletRequest;
    private HttpServletResponse servletResponse;

    @Before
    public void setUp() throws Exception {
        servletRequest = mockHttpServletRequest();
        servletResponse = mockHttpServletResponse();
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

    @Test
    public void shouldSetStatusCode() throws Exception {
        mockResponse.setResponseHttpStatus(HttpStatus.SC_CONFLICT);
        mockResponse.writeResponse(result, originalResponseContent);
        verify(servletResponse).setStatus(HttpStatus.SC_CONFLICT);
    }

    public MockResult createMockResult() throws Exception {
        WsdlMockRunContext runContext = mock(WsdlMockRunContext.class);
        return new RestMockResult(createMockRequest(runContext));
    }

    public RestMockRequest createMockRequest(WsdlMockRunContext runContext) throws Exception {
        return new RestMockRequest(servletRequest, servletResponse, runContext);
    }

}
