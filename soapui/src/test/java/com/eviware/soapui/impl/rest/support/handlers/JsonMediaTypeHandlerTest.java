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

package com.eviware.soapui.impl.rest.support.handlers;

import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.support.RestParamProperty;
import com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder;
import com.eviware.soapui.impl.wsdl.WsdlSubmitContext;
import com.eviware.soapui.impl.wsdl.submit.transports.http.BaseHttpRequestTransport;
import com.eviware.soapui.impl.wsdl.submit.transports.http.ExtendedHttpMethod;
import com.eviware.soapui.impl.wsdl.submit.transports.http.HttpResponse;
import com.eviware.soapui.impl.wsdl.submit.transports.http.SinglePartHttpResponse;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.metrics.SoapUIMetrics;
import com.eviware.soapui.model.iface.Request;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContext;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.http.Header;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.io.HttpTransportMetrics;
import org.junit.Before;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

import static com.eviware.soapui.utils.ModelItemFactory.makeRestRequest;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the JsonMediaTypeHandler class.
 */
public class JsonMediaTypeHandlerTest {

    public static final String ENDPOINT = "http://somehost.com";
    private RestRequest restRequest;
    private JsonMediaTypeHandler mediaTypeHandler;

    @Before
    public void setUp() throws Exception {
        restRequest = makeRestRequest();
        restRequest.setEndpoint(ENDPOINT);

        mediaTypeHandler = new JsonMediaTypeHandler();
    }

    @Test
    public void retainsUriInFirstSubmitAsNamespaceUri() throws Exception {
        HttpResponse response = submitRequestAndReceiveResponse(restRequest, "/original/path");

        String originalXml = mediaTypeHandler.createXmlRepresentation(response);
        HttpResponse responseWithNewPath = submitRequestAndReceiveResponse(restRequest, "/another/path");
        assertThat(mediaTypeHandler.createXmlRepresentation(responseWithNewPath), is(equalTo(originalXml)));
    }

    @Test
    public void usesActualUriWhenPathContainsTemplateParameters() throws Exception {
        RestParamProperty userParameter = restRequest.getParams().addProperty("user");
        userParameter.setStyle(RestParamsPropertyHolder.ParameterStyle.TEMPLATE);
        userParameter.setValue("billy");

        String originalPath = "/original/{user}";
        restRequest.setPath(originalPath);

        SubmitContext submitContext = submitRequest(restRequest, originalPath);
        HttpResponse response = makeResponseFor(restRequest, "/original/billy");
        restRequest.setResponse(response, submitContext);

        assertThat(mediaTypeHandler.createXmlRepresentation(response), containsString("/original/billy"));
    }

    private HttpResponse submitRequestAndReceiveResponse(RestRequest restRequest, String originalPath) throws Exception {
        restRequest.setPath(originalPath);

        SubmitContext submitContext = submitRequest(restRequest, originalPath);
        HttpResponse response = makeResponseFor(restRequest, originalPath);
        // this simulates that we receive a response
        restRequest.setResponse(response, submitContext);
        return response;
    }

    private SubmitContext submitRequest(RestRequest restRequest, String originalPath) throws URISyntaxException, URIException, Request.SubmitException {
        SubmitContext submitContext = new WsdlSubmitContext(restRequest);
        HttpRequestBase httpMethod = mock(HttpRequestBase.class);
        submitContext.setProperty(BaseHttpRequestTransport.HTTP_METHOD, httpMethod);
        submitContext.setProperty(BaseHttpRequestTransport.REQUEST_URI, new URI(ENDPOINT + originalPath));
        restRequest.submit(submitContext, false);
        return submitContext;
    }

    private SinglePartHttpResponse makeResponseFor(RestRequest restRequest, String path) throws Exception {
        ExtendedHttpMethod httpMethod = prepareHttpMethodWith(path);
        SinglePartHttpResponse response =
                new SinglePartHttpResponse(restRequest, httpMethod, null, mock(PropertyExpansionContext.class));
        response.setResponseContent("{ firstName: 'Kalle', secondName: 'Ek' }");
        return response;
    }

    private ExtendedHttpMethod prepareHttpMethodWith(String path) throws URISyntaxException, MalformedURLException {
        ExtendedHttpMethod httpMethod = mock(ExtendedHttpMethod.class);
        when(httpMethod.getResponseContentType()).thenReturn("text/json");
        when(httpMethod.getMethod()).thenReturn("GET");
        when(httpMethod.getProtocolVersion()).thenReturn(new ProtocolVersion("http", 1, 1));
        SoapUIMetrics soapUIMetrics = new SoapUIMetrics(mock(HttpTransportMetrics.class),
                mock(HttpTransportMetrics.class));
        when(httpMethod.getMetrics()).thenReturn(soapUIMetrics);
        when(httpMethod.getAllHeaders()).thenReturn(new Header[0]);
        when(httpMethod.getResponseReadTime()).thenReturn(10L);
        when(httpMethod.getURI()).thenReturn(new java.net.URI(ENDPOINT + path));
        when(httpMethod.getURL()).thenReturn(new java.net.URL(ENDPOINT + path));
        return httpMethod;
    }
}
