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

import com.eviware.soapui.impl.settings.XmlBeansSettingsImpl;
import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.impl.support.http.HttpRequest;
import com.eviware.soapui.impl.wsdl.submit.transports.http.BaseHttpRequestTransport;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.settings.HttpSettings;
import org.apache.http.client.methods.HttpRequestBase;
import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.net.URI;
import java.net.URISyntaxException;

public class EndpointRequestFilterTest {
    EndpointRequestFilter endpointRequestFilter;

    @Before
    public void setUp() {
        endpointRequestFilter = new EndpointRequestFilter();
    }

    @Test
    public void doesNotDoubleEncodeAlreadyEncodedUri() throws URISyntaxException {
        String encodedUri = "http://user:password@google.se/search?q=%3F";

        HttpRequestBase httpMethod = Mockito.mock(HttpRequestBase.class);

        SubmitContext context = mockContext(httpMethod);
        AbstractHttpRequest<?> request = mockRequest(encodedUri, mockSettings());

        endpointRequestFilter.filterAbstractHttpRequest(context, request);

        ArgumentCaptor<URI> httpMethodUri = ArgumentCaptor.forClass(URI.class);
        Mockito.verify(httpMethod).setURI(httpMethodUri.capture());
        Assert.assertThat(httpMethodUri.getValue(), Is.is(new URI(encodedUri)));
    }

    private SubmitContext mockContext(HttpRequestBase httpMethod) {
        SubmitContext context = Mockito.mock(SubmitContext.class);
        Mockito.when(context.getProperty(BaseHttpRequestTransport.HTTP_METHOD)).thenReturn(httpMethod);
        return context;
    }

    private AbstractHttpRequest<?> mockRequest(String encodedUri, XmlBeansSettingsImpl settings) {
        AbstractHttpRequest<?> request = Mockito.mock(HttpRequest.class);
        Mockito.when(request.getEndpoint()).thenReturn(encodedUri);
        Mockito.when(request.getSettings()).thenReturn(settings);
        return request;
    }

    private XmlBeansSettingsImpl mockSettings() {
        XmlBeansSettingsImpl settings = Mockito.mock(XmlBeansSettingsImpl.class);
        Mockito.when(settings.getBoolean(HttpSettings.ENCODED_URLS)).thenReturn(true);
        return settings;
    }
}
