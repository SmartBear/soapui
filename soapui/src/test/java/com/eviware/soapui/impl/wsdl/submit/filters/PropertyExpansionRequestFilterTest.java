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

import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.settings.XmlBeansSettingsImpl;
import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.impl.support.http.HttpRequest;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.WsdlSubmitContext;
import com.eviware.soapui.impl.wsdl.submit.transports.http.BaseHttpRequestTransport;
import com.eviware.soapui.impl.wsdl.teststeps.HttpTestRequest;
import com.eviware.soapui.impl.wsdl.teststeps.RestTestRequest;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestRequest;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestRequestStep;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.settings.CommonSettings;
import com.eviware.soapui.support.SoapUIException;
import com.eviware.soapui.utils.ModelItemFactory;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 *
 */
public class PropertyExpansionRequestFilterTest {
    private static final String REQUEST_CONTENT = "<type>${#TestCase#StoreType}</type>";

    private static final String EXPECTED_REQUEST_CONTENT_AFTER_PROPERTY_EXPANSION = "<type>shoe_Store</type>";

    private SubmitContext submitContext;
    private PropertyExpansionRequestFilter requestFilter;


    @Before
    public void setUp() throws SoapUIException {
        requestFilter = new PropertyExpansionRequestFilter();
        submitContext = createSubmitContext();
    }

    @Test
    public void performsPropertyExpansionOnWsdlTestRequestContent() throws SoapUIException {
        AbstractHttpRequest request = mockRequest(WsdlTestRequest.class);

        requestFilter.filterAbstractHttpRequest(submitContext, request);

        String contentAfterPropertyExpansion = (String) submitContext.getProperty(BaseHttpRequestTransport.REQUEST_CONTENT);
        assertThat(contentAfterPropertyExpansion, is(EXPECTED_REQUEST_CONTENT_AFTER_PROPERTY_EXPANSION));
    }

    @Test
    public void performsPropertyExpansionOnRestTestRequestContent() throws SoapUIException {
        AbstractHttpRequest request = mockRequest(RestTestRequest.class);
        requestFilter.filterAbstractHttpRequest(submitContext, request);

        String contentAfterPropertyExpansion = (String) submitContext.getProperty(BaseHttpRequestTransport.REQUEST_CONTENT);
        assertThat(contentAfterPropertyExpansion, is(EXPECTED_REQUEST_CONTENT_AFTER_PROPERTY_EXPANSION));
    }

    @Test
    public void performsPropertyExpansionOnHttpTestRequestContent() throws SoapUIException {
        AbstractHttpRequest request = mockRequest(HttpTestRequest.class);
        requestFilter.filterAbstractHttpRequest(submitContext, request);

        String contentAfterPropertyExpansion = (String) submitContext.getProperty(BaseHttpRequestTransport.REQUEST_CONTENT);
        assertThat(contentAfterPropertyExpansion, is(EXPECTED_REQUEST_CONTENT_AFTER_PROPERTY_EXPANSION));
    }

    @Test
    public void performsPropertyExpansionOnWsdlRequestContent() throws SoapUIException {
        AbstractHttpRequest request = mockRequest(WsdlRequest.class);
        requestFilter.filterAbstractHttpRequest(submitContext, request);

        String contentAfterPropertyExpansion = (String) submitContext.getProperty(BaseHttpRequestTransport.REQUEST_CONTENT);
        assertThat(contentAfterPropertyExpansion, is(EXPECTED_REQUEST_CONTENT_AFTER_PROPERTY_EXPANSION));
    }

    @Test
    public void performsPropertyExpansionOnRestRequestContent() throws SoapUIException {
        AbstractHttpRequest request = mockRequest(RestRequest.class);
        requestFilter.filterAbstractHttpRequest(submitContext, request);

        String contentAfterPropertyExpansion = (String) submitContext.getProperty(BaseHttpRequestTransport.REQUEST_CONTENT);
        assertThat(contentAfterPropertyExpansion, is(EXPECTED_REQUEST_CONTENT_AFTER_PROPERTY_EXPANSION));
    }

    @Test
    public void performsPropertyExpansionOnHttpRequestContent() throws SoapUIException {
        AbstractHttpRequest request = mockRequest(HttpRequest.class);
        requestFilter.filterAbstractHttpRequest(submitContext, request);

        String contentAfterPropertyExpansion = (String) submitContext.getProperty(BaseHttpRequestTransport.REQUEST_CONTENT);
        assertThat(contentAfterPropertyExpansion, is(EXPECTED_REQUEST_CONTENT_AFTER_PROPERTY_EXPANSION));
    }


    private SubmitContext createSubmitContext() throws SoapUIException {
        WsdlTestRequestStep requestStep = ModelItemFactory.makeTestRequestStep();
        requestStep.getTestCase().setPropertyValue("StoreType", "shoe_Store");
        SubmitContext context = new WsdlSubmitContext(requestStep);

        context.setProperty(BaseHttpRequestTransport.REQUEST_CONTENT, REQUEST_CONTENT);
        return context;
    }

    private <T extends AbstractHttpRequest> AbstractHttpRequest mockRequest(Class<T> type) {
        AbstractHttpRequest request = Mockito.mock(type);
        XmlBeansSettingsImpl settings = Mockito.mock(XmlBeansSettingsImpl.class);
        Mockito.when(settings.getBoolean(CommonSettings.ENTITIZE_PROPERTIES)).thenReturn(false);
        Mockito.when(request.getSettings()).thenReturn(settings);
        return request;
    }
}
