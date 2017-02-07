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

package com.eviware.soapui.monitor;

import com.eviware.soapui.impl.wsdl.mock.DispatchException;
import com.eviware.soapui.model.mock.MockResult;
import com.eviware.soapui.model.mock.MockRunContext;
import com.eviware.soapui.model.mock.MockRunner;
import com.eviware.soapui.model.mock.MockService;
import com.google.common.io.ByteStreams;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 *
 * @author ninckblokje
 */
public class JettyMockEngineTest {
    
    private static final String baseContext = "/" + JettyMockEngineTest.class.getName();
    private static final int port = 18080;
    
    protected void callMockService(int index, int statusCode) throws IOException {
        HttpClient client = new DefaultHttpClient();
        
        HttpPost request = new HttpPost("http://localhost:" + port + baseContext + "/" + index);
        request.setEntity(new StringEntity(JettyMockEngineTest.class.getName()));
        request.setHeader("Content-type", "text/xml; charset=UTF-8");
        request.setHeader("SOAPAction", "");
        
        HttpResponse response = client.execute(request);
        assertNotNull(response);
        assertEquals(statusCode, response.getStatusLine().getStatusCode());
    }
    
    protected MockRunContext getMockedMockRunContext(MockService service) {
        MockRunContext context = mock(MockRunContext.class);
        
        when(context.getMockService())
                .thenReturn(service);
        
        return context;
    }
    
    protected MockRunner getMockedMockRunner(MockRunContext context) {
        MockRunner runner = mock(MockRunner.class);
        
        when(runner.getMockContext())
                .thenReturn(context);
        
        return runner;
    }
    
    protected MockService getMockedMockService(int port, String path) {
        MockService service = mock(MockService.class);
        
        when(service.getPath())
                .thenReturn(path);
        when(service.getPort())
                .thenReturn(18080);
        
        return service;
    }
    
    protected MockResult getMockedMockResult() {
        MockResult result = mock(MockResult.class);
        
        return result;
    }
    
    protected MockRunner setUpMockRunner(JettyMockEngine mockEngine, int index, MockResult result) throws DispatchException {
        MockService service = getMockedMockService(port, baseContext + "/" + index);
        MockRunContext context = getMockedMockRunContext(service);
        MockRunner runner = getMockedMockRunner(context);
        
        when(runner.isRunning())
                .thenReturn(Boolean.TRUE);
        
        if (result == null) {
            when(runner.dispatchRequest(any(), any()))
                    .thenThrow(new DispatchException("/" + this.getClass().getName() + "/" + index));
        } else {
            when(runner.dispatchRequest(any(), any()))
                    .thenReturn(result);
        }
        
        return runner;
    }
    
    protected void verifyMockedMockRunner(MockRunner runner, int noTimes) throws DispatchException {
        verify(runner, times(noTimes)).isRunning();
        verify(runner, times(noTimes))
                .dispatchRequest(argThat(new HttpServletRequestArgumentMatcher(JettyMockEngineTest.class.getName())), any());
    }
    
    @Test
    public void testServerHandlerWithTwoUniquePathsAndThreeRunners() throws Exception {
        JettyMockEngine mockEngine = new JettyMockEngine();
        
        MockResult result2 = getMockedMockResult();
        MockResult result3 = getMockedMockResult();
        
        MockRunner runner1 = setUpMockRunner(mockEngine, 1, null);
        MockRunner runner2 = setUpMockRunner(mockEngine, 1, result2);
        MockRunner runner3 = setUpMockRunner(mockEngine, 3, result3);
        
        try {
            mockEngine.startMockService(runner1);
            mockEngine.startMockService(runner2);
            mockEngine.startMockService(runner3);
            
            assertNotNull(mockEngine.getMockRunners());
            assertEquals(3, mockEngine.getMockRunners().length);
            
            callMockService(1, 200);
            
            verifyMockedMockRunner(runner1, 1);
            verifyMockedMockRunner(runner2, 1);
            verifyMockedMockRunner(runner3, 0);
            
            verify(result2, times(1)).finish();
        } finally {
            mockEngine.stopMockService(runner3);
            mockEngine.stopMockService(runner2);
            mockEngine.stopMockService(runner1);
        }
    }
    
    private static class HttpServletRequestArgumentMatcher extends ArgumentMatcher<HttpServletRequest> {
        
        private String expectedContent;
        
        HttpServletRequestArgumentMatcher(String expectedContent) {
            this.expectedContent = expectedContent;
        }

        @Override
        public boolean matches(Object argument) {
            if (!(argument instanceof JettyMockEngine.SoapUIHttpServletRequestWrapper))
                return false;
            
            HttpServletRequest request = (HttpServletRequest) argument;
            
            try {
                byte[] content = ByteStreams.toByteArray(request.getInputStream());
                return expectedContent.equals(new String(content, "UTF-8"));
            } catch (IOException ex) {
                ex.printStackTrace();
                return false;
            }
        }
        
    }
}
