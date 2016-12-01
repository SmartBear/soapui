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

package com.eviware.soapui.impl.wsdl.monitor.jettyproxy;

import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.monitor.ContentTypes;
import com.eviware.soapui.impl.wsdl.monitor.SoapMonitorListenerCallBack;
import com.eviware.soapui.impl.wsdl.submit.transports.http.ExtendedHttpMethod;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.message.BasicHeader;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * @author joel.jonsson
 */
public class ProxyServletContentTypeTest {
    private ProxyServlet proxyServlet;

    @Mock
    private SoapMonitorListenerCallBack listenerCallBack;
    @Mock
    private WsdlProject project;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        proxyServlet = new ProxyServlet(project, listenerCallBack);
    }

    @Test
    public void noContentTypeMatchesRequestWithNoContentType() {
        proxyServlet.setIncludedContentTypes(ContentTypes.of(""));
        ExtendedHttpMethod request = createRequestWithContentTypes();
        assertThat(proxyServlet.contentTypeMatches(request), is(true));
    }

    private ExtendedHttpMethod createRequestWithContentTypes(String... contentTypes) {
        ExtendedHttpMethod method = mock(ExtendedHttpMethod.class);
        HttpResponse httpResponse = mock(HttpResponse.class);
        when(method.hasHttpResponse()).thenReturn(true);
        when(method.getHttpResponse()).thenReturn(httpResponse);
        Header[] headers = new Header[contentTypes.length];
        for (int i = 0; i < headers.length; i++) {
            headers[i] = new BasicHeader("Content-Type", contentTypes[i]);
        }
        when(httpResponse.getHeaders(eq("Content-Type"))).thenReturn(headers);
        return method;
    }
}
