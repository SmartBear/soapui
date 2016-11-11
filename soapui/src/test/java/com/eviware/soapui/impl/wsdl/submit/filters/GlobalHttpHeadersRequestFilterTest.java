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

package com.eviware.soapui.impl.wsdl.submit.filters;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.impl.support.http.HttpRequest;
import com.eviware.soapui.impl.wsdl.support.AbstractTestCaseRunner;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestRunContext;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.support.types.StringToStringsMap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.internal.verification.Times;

/**
 * Created by ole on 27/05/14.
 */

public class GlobalHttpHeadersRequestFilterTest {

    GlobalHttpHeadersRequestFilter globalHttpHeadersRequestFilter;

    @Before
    public void setUp() {
        globalHttpHeadersRequestFilter = new GlobalHttpHeadersRequestFilter();
        SoapUI.initDefaultCore();
    }

    @Test
    public void addsNothingByDefault() {
        StringToStringsMap requestHeaders = new StringToStringsMap();

        SubmitContext context = mockContext();
        AbstractHttpRequest<?> request = mockRequest(requestHeaders);

        globalHttpHeadersRequestFilter.filterAbstractHttpRequest(context, request);

        Mockito.verifyZeroInteractions(request);
    }

    @Test
    public void addsSpecifiedHeadersWithAndWithoutCaching() {
        System.setProperty(GlobalHttpHeadersRequestFilter.HEADER_SYSTEM_PROPERTY_PREFIX + "User-Agent", "Testing");
        StringToStringsMap value = invokeFilterWithHeaders(new StringToStringsMap());
        Assert.assertEquals("Testing", value.get("User-Agent").get(0));

        System.setProperty(GlobalHttpHeadersRequestFilter.HEADER_SYSTEM_PROPERTY_PREFIX + "User.Agent", "Testing");
        value = invokeFilterWithHeaders(new StringToStringsMap());

        // should be the same as the first since headers are cached
        Assert.assertEquals("Testing", value.get("User-Agent").get(0));

        // disable caching and run again
        System.setProperty(GlobalHttpHeadersRequestFilter.CACHE_HEADERS_SYSTEM_PROPERTY, "false");
        System.setProperty(GlobalHttpHeadersRequestFilter.HEADER_SYSTEM_PROPERTY_PREFIX + "User.Agent", "Testing");
        value = invokeFilterWithHeaders(new StringToStringsMap());
        Assert.assertEquals("Testing", value.get("User.Agent").get(0));
        Assert.assertEquals("Testing", value.get("User-Agent").get(0));

        System.clearProperty(GlobalHttpHeadersRequestFilter.HEADER_SYSTEM_PROPERTY_PREFIX + "User-Agent");
        System.clearProperty(GlobalHttpHeadersRequestFilter.HEADER_SYSTEM_PROPERTY_PREFIX + "User.Agent");
    }

    @Test
    public void addMultipleHeadersWithSameName() {
        GlobalHttpHeadersRequestFilter.addGlobalHeader("User-Agent", "Testing");
        GlobalHttpHeadersRequestFilter.addGlobalHeader("User-Agent", "Testing2");
        StringToStringsMap value = invokeFilterWithHeaders(new StringToStringsMap());
        Assert.assertEquals("Testing", value.get("User-Agent").get(0));
        Assert.assertEquals("Testing2", value.get("User-Agent").get(1));
        GlobalHttpHeadersRequestFilter.removeGlobalHeader( "User-Agent", null );
    }

    @Test
    public void replaceOrAddSpecifiedHeaders() {
        System.setProperty(GlobalHttpHeadersRequestFilter.REPLACE_HEADERS_SYSTEM_PROPERTY, "true");
        System.setProperty(GlobalHttpHeadersRequestFilter.HEADER_SYSTEM_PROPERTY_PREFIX + "User-Agent", "Testing Replace");
        StringToStringsMap requestHeaders = new StringToStringsMap();
        requestHeaders.put("User-Agent", "Testing");
        StringToStringsMap value = invokeFilterWithHeaders(requestHeaders);
        Assert.assertEquals("Testing Replace", value.get("User-Agent").get(0));
        Assert.assertEquals(1, value.size());

        System.setProperty(GlobalHttpHeadersRequestFilter.REPLACE_HEADERS_SYSTEM_PROPERTY, "false");
        requestHeaders = new StringToStringsMap();
        requestHeaders.put("User-Agent", "Testing");
        value = invokeFilterWithHeaders(requestHeaders);
        Assert.assertEquals("Testing", value.get("User-Agent").get(0));
        Assert.assertEquals("Testing Replace", value.get("User-Agent").get(1));
        Assert.assertEquals(2, value.get("User-Agent").size());

        System.clearProperty(GlobalHttpHeadersRequestFilter.HEADER_SYSTEM_PROPERTY_PREFIX + "User-Agent");
    }

    @Test
    @Ignore
    public void addHeaderWithPropertyExpansion() {
        System.setProperty(GlobalHttpHeadersRequestFilter.HEADER_SYSTEM_PROPERTY_PREFIX + "User-Agent", "Testing Replace.${currentStepIndex}.${currentStepRunIndex}");

        AbstractTestCaseRunner runner = Mockito.mock(AbstractTestCaseRunner.class);
        Mockito.when(runner.getResultCount()).thenReturn(4);

        WsdlTestRunContext context = mockContext();
        Mockito.when(context.getTestRunner()).thenReturn(runner);
        Mockito.when(context.getCurrentStepIndex()).thenReturn(3);

        AbstractHttpRequest<?> request = mockRequest(new StringToStringsMap());

        globalHttpHeadersRequestFilter.filterAbstractHttpRequest(context, request);

        ArgumentCaptor<StringToStringsMap> headers = ArgumentCaptor.forClass(StringToStringsMap.class);
        Mockito.verify(request).setRequestHeaders(headers.capture());

        Assert.assertEquals("Testing Replace.3.4", headers.getAllValues().get(0).get("User-Agent").get(0));

        System.clearProperty(GlobalHttpHeadersRequestFilter.HEADER_SYSTEM_PROPERTY_PREFIX + "User-Agent");
    }

    @Test
    public void addAndRemoveHeader() {
        System.setProperty(GlobalHttpHeadersRequestFilter.HEADER_SYSTEM_PROPERTY_PREFIX + "User-Agent", "Testing");

        WsdlTestRunContext context = mockContext();
        AbstractHttpRequest<?> request = mockRequest(new StringToStringsMap());

        globalHttpHeadersRequestFilter.filterAbstractHttpRequest(context, request);
        globalHttpHeadersRequestFilter.afterAbstractHttpResponse(context, request);

        ArgumentCaptor<StringToStringsMap> headers = ArgumentCaptor.forClass(StringToStringsMap.class);
        Mockito.verify(request, new Times(2)).setRequestHeaders(headers.capture());

        // first headers should contain the added header
        Assert.assertEquals("Testing", headers.getAllValues().get(0).get("User-Agent").get(0));

        // and the second should contain nothing
        Assert.assertEquals(0, headers.getAllValues().get(1).size());

        System.clearProperty(GlobalHttpHeadersRequestFilter.HEADER_SYSTEM_PROPERTY_PREFIX + "User-Agent");
    }

    @Test
    public void complementAndRemoveHeader() {
        System.setProperty(GlobalHttpHeadersRequestFilter.HEADER_SYSTEM_PROPERTY_PREFIX + "User-Agent", "Testing");

        WsdlTestRunContext context = mockContext();
        StringToStringsMap requestHeaders = new StringToStringsMap();
        requestHeaders.add("User-Agent", "Existing header");
        AbstractHttpRequest<?> request = mockRequest(requestHeaders);

        globalHttpHeadersRequestFilter.filterAbstractHttpRequest(context, request);
        globalHttpHeadersRequestFilter.afterAbstractHttpResponse(context, request);

        ArgumentCaptor<StringToStringsMap> headers = ArgumentCaptor.forClass(StringToStringsMap.class);
        Mockito.verify(request, new Times(2)).setRequestHeaders(headers.capture());

        // check that the headers has been added
        Assert.assertEquals("Existing header", headers.getAllValues().get(0).get("User-Agent").get(0));
        Assert.assertEquals("Testing", headers.getAllValues().get(0).get("User-Agent").get(1));

        // and removed
        Assert.assertEquals(1, headers.getAllValues().get(1).size());
        Assert.assertEquals("Existing header", headers.getAllValues().get(1).get("User-Agent").get(0));

        System.clearProperty(GlobalHttpHeadersRequestFilter.HEADER_SYSTEM_PROPERTY_PREFIX + "User-Agent");
    }

    @Test
    public void replaceAndRemoveHeader() {
        System.setProperty(GlobalHttpHeadersRequestFilter.HEADER_SYSTEM_PROPERTY_PREFIX + "User-Agent", "Testing");
        System.setProperty(GlobalHttpHeadersRequestFilter.REPLACE_HEADERS_SYSTEM_PROPERTY, "true");

        WsdlTestRunContext context = mockContext();
        StringToStringsMap requestHeaders = new StringToStringsMap();
        requestHeaders.add("User-Agent", "Existing header");
        AbstractHttpRequest<?> request = mockRequest(requestHeaders);

        globalHttpHeadersRequestFilter.filterAbstractHttpRequest(context, request);
        globalHttpHeadersRequestFilter.afterAbstractHttpResponse(context, request);

        ArgumentCaptor<StringToStringsMap> headers = ArgumentCaptor.forClass(StringToStringsMap.class);
        Mockito.verify(request, new Times(2)).setRequestHeaders(headers.capture());

        // check that the header has been added
        Assert.assertEquals("Testing", headers.getAllValues().get(0).get("User-Agent").get(0));
        Assert.assertEquals(1, headers.getAllValues().get(0).size());

        // and removed
        Assert.assertEquals(1, headers.getAllValues().get(1).size());
        Assert.assertEquals("Existing header", headers.getAllValues().get(1).get("User-Agent").get(0));

        System.clearProperty(GlobalHttpHeadersRequestFilter.HEADER_SYSTEM_PROPERTY_PREFIX + "User-Agent");
    }

    private StringToStringsMap invokeFilterWithHeaders(StringToStringsMap requestHeaders) {
        SubmitContext context = mockContext();
        AbstractHttpRequest<?> request = mockRequest(requestHeaders);

        globalHttpHeadersRequestFilter.filterAbstractHttpRequest(context, request);

        ArgumentCaptor<StringToStringsMap> headers = ArgumentCaptor.forClass(StringToStringsMap.class);
        Mockito.verify(request).setRequestHeaders(headers.capture());
        return headers.getValue();
    }

    private WsdlTestRunContext mockContext() {
        return Mockito.mock(WsdlTestRunContext.class);
    }

    private AbstractHttpRequest<?> mockRequest(StringToStringsMap requestHeaders) {
        AbstractHttpRequest<?> request = Mockito.mock(HttpRequest.class);
        Mockito.when(request.getRequestHeaders()).thenReturn(requestHeaders);
        return request;
    }


}
