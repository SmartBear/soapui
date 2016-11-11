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
import com.eviware.soapui.model.mock.MockRequest;
import org.apache.commons.httpclient.HttpStatus;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.eviware.soapui.impl.rest.RestRequestInterface.HttpMethod;
import static com.eviware.soapui.utils.MockedServlet.mockHttpServletRequest;
import static com.eviware.soapui.utils.MockedServlet.mockHttpServletResponse;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class RestMockDispatcherTest {

    private HttpServletRequest request;
    private HttpServletResponse response;
    private RestMockDispatcher restMockDispatcher;
    private WsdlMockRunContext context;
    private RestMockService restMockService;

    @Before
    public void setUp() throws IOException {
        createRestMockDispatcher();
    }

    @Test
    public void afterRequestScriptIsCalled() throws Exception {
        when(restMockService.getPath()).thenReturn("/");
        when(request.getPathInfo()).thenReturn("/");
        RestMockResult mockResult = (RestMockResult) restMockDispatcher.dispatchRequest(request, response);

        verify(restMockService).runAfterRequestScript(context, mockResult);
    }

    @Test
    public void onRequestScriptIsCalled() throws Exception {
        restMockDispatcher.dispatchRequest(request, response);

        verify(restMockService).runOnRequestScript(any(WsdlMockRunContext.class), any(MockRequest.class));
    }

    @Test
    public void onRequestScriptOverridesRegularDispatching() throws Exception {
        /*
			When onRequestScript returns a MockResult instance then regular dispatching is ignored.
			This tests verify when script returns MokResult instance we bypass regular dispatching.

		 */

        RestMockResult restMockResult = mock(RestMockResult.class);
        when(restMockService.runOnRequestScript(any(WsdlMockRunContext.class), any(MockRequest.class))).thenReturn(restMockResult);

        restMockDispatcher.dispatchRequest(request, response);

        verify(restMockService, never()).findBestMatchedOperation(anyString(), any(HttpMethod.class));
    }

    @Test
    public void shouldReturnNoResponseFoundWhenThereIsNoMatchingAction() throws Exception {
        when(restMockService.findBestMatchedOperation(anyString(), any(HttpMethod.class))).thenReturn(null);
        when(restMockService.getPath()).thenReturn("/");
        when(request.getPathInfo()).thenReturn("/");

        restMockDispatcher.dispatchRequest(request, response);

        verify(response).setStatus(HttpStatus.SC_NOT_FOUND);
    }


    @Test
    public void shouldResponseWhenServicePathMatches() throws Exception {
        RestMockAction action = mock(RestMockAction.class);
        when(restMockService.findBestMatchedOperation("/api", HttpMethod.DELETE)).thenReturn(action);
        when(restMockService.getPath()).thenReturn("/sweden");
        when(request.getPathInfo()).thenReturn("/sweden/api");

        restMockDispatcher.dispatchRequest(request, response);

        verify(action).dispatchRequest(any(RestMockRequest.class));
    }

    @Test
    public void shouldResponseWhenPathMatches() throws Exception {
        RestMockAction action = mock(RestMockAction.class);
        when(restMockService.findBestMatchedOperation("/api", HttpMethod.DELETE)).thenReturn(action);
        when(restMockService.getPath()).thenReturn("/");
        when(request.getPathInfo()).thenReturn("/api");

        restMockDispatcher.dispatchRequest(request, response);

        verify(action).dispatchRequest(any(RestMockRequest.class));
    }

    @Test
    public void returnsErrorOnrequestScriptException() throws Exception {
        Exception runTimeException = new IllegalStateException("wrong state");
        when(restMockService.runOnRequestScript(any(WsdlMockRunContext.class), any(MockRequest.class))).thenThrow(runTimeException);

        restMockDispatcher.dispatchRequest(request, response);

        verify(response).setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
    }


    private void createRestMockDispatcher() throws IOException {
        request = mockHttpServletRequest();
        when(request.getMethod()).thenReturn(HttpMethod.DELETE.name());

        response = mockHttpServletResponse();
        restMockService = mock(RestMockService.class);
        context = mock(WsdlMockRunContext.class);

        restMockDispatcher = new RestMockDispatcher(restMockService, context);
    }
}
