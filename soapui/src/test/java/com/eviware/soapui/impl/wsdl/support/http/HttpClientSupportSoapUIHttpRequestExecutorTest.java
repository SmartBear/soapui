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

package com.eviware.soapui.impl.wsdl.support.http;

import com.eviware.soapui.impl.wsdl.submit.transports.http.ExtendedHttpMethod;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.metrics.SoapUIMetrics;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.metrics.Stopwatch;
import com.eviware.soapui.impl.wsdl.support.http.HttpClientSupport.SoapUIHttpRequestExecutor;
import org.apache.http.HttpRequest;
import org.apache.http.impl.client.RequestWrapper;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpProcessor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(org.mockito.runners.MockitoJUnitRunner.class)
public class HttpClientSupportSoapUIHttpRequestExecutorTest {

    @Mock
    HttpProcessor processor;

    @Mock
    HttpContext context;

    @Mock
    RequestWrapper wrappedRequest;

    @Mock
    ExtendedHttpMethod extendedHttpMethod;

    @Mock
    SoapUIMetrics metrics;

    @Mock
    Stopwatch stopWatch;

    @Mock
    Stopwatch startWatch;

    @Test
    public void aWrappedRequestShouldDelegateToOriginalRequest() throws Exception {
        when(wrappedRequest.getOriginal()).thenReturn(extendedHttpMethod);
        when(extendedHttpMethod.getMetrics()).thenReturn(metrics);
        when(metrics.getConnectTimer()).thenReturn(stopWatch);
        when(metrics.getTimeToFirstByteTimer()).thenReturn(startWatch);

        SoapUIHttpRequestExecutor executor = new SoapUIHttpRequestExecutor();
        executor.preProcess(wrappedRequest, processor, context);

        verify(wrappedRequest, times(1)).getOriginal();
        verify(extendedHttpMethod, times(1)).getMetrics();
        verify(startWatch, times(1)).start();
        verify(stopWatch, times(1)).stop();
    }

    @Test
    public void aNonWrappedRequestShouldNotDelegateToOriginalRequest() throws Exception {
        HttpRequest req = mock(ExtendedHttpMethod.class);

        when(((ExtendedHttpMethod) req).getMetrics()).thenReturn(metrics);
        when(metrics.getConnectTimer()).thenReturn(stopWatch);
        when(metrics.getTimeToFirstByteTimer()).thenReturn(startWatch);

        SoapUIHttpRequestExecutor executor = new SoapUIHttpRequestExecutor();
        executor.preProcess(req, processor, context);

        verify(((ExtendedHttpMethod) req), times(1)).getMetrics();
        verify(startWatch, times(1)).start();
        verify(stopWatch, times(1)).stop();
    }
}
