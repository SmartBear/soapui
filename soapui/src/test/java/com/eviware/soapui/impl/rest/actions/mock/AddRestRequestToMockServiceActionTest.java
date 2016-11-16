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

package com.eviware.soapui.impl.rest.actions.mock;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.rest.RestMethod;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.RestRequestInterface;
import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.rest.RestServiceFactory;
import com.eviware.soapui.impl.rest.mock.RestMockAction;
import com.eviware.soapui.impl.rest.mock.RestMockService;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.submit.transports.http.HttpResponse;
import com.eviware.soapui.model.support.ProjectListenerAdapter;
import com.eviware.soapui.settings.HttpSettings;
import com.eviware.soapui.support.SoapUIException;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.types.StringToStringsMap;
import com.eviware.soapui.utils.ModelItemFactory;
import com.eviware.soapui.utils.StubbedDialogs;
import com.eviware.x.dialogs.XDialogs;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.eviware.soapui.impl.rest.RestRequestInterface.HttpMethod.GET;
import static java.lang.Boolean.FALSE;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.matchers.NotNull.NOT_NULL;
import static org.mockito.internal.matchers.Null.NULL;

public class AddRestRequestToMockServiceActionTest {
    private static final String ONE_HEADER = "oneHeader";
    private static final String ANOTHER_HEADER = "anotherHeader";
    private static final String HEADER_STATUS = "#status#";
    private static final String HEADER_CONTENT_LENGTH = "Content-Length";
    private static final String HEADER_CONTENT_TYPE = "Content-Type";
    private final String requestPath = "/some/path";
    AddRestRequestToMockServiceAction action = new AddRestRequestToMockServiceAction();
    RestRequest restRequest;
    Object notUsed = null;
    String mockServiceName = "Mock Service1 1";
    private XDialogs originalDialogs;
    private WsdlProject project;

    @Before
    public void setUp() throws Exception {
        restRequest = ModelItemFactory.makeRestRequest();
        restRequest.setMethod(GET);
        restRequest.setPath(requestPath);
        mockPromptDialog();
        project = restRequest.getRestMethod().getInterface().getProject();

        setUpResponse();

        SoapUI.getSettings().setBoolean(HttpSettings.START_MOCK_SERVICE, FALSE);
        SoapUI.getSettings().setBoolean(HttpSettings.LEAVE_MOCKENGINE, FALSE);
    }

    public void setUpResponse() {
        HttpResponse response = mock(HttpResponse.class);

        StringToStringsMap headers = new StringToStringsMap();
        headers.add(ONE_HEADER, "oneValue");
        headers.add(ANOTHER_HEADER, "anotherValue");
        headers.add(HEADER_STATUS, "HTTP/1.1 200 OK");
        headers.add(HEADER_CONTENT_LENGTH, "456");
        headers.add(HEADER_CONTENT_TYPE, "application/xml");

        when(response.getResponseHeaders()).thenReturn(headers);
        when(response.getContentType()).thenReturn("application/xml");

        restRequest.setResponse(response, null);
    }

    @After
    public void tearDown() {
        UISupport.setDialogs(originalDialogs);
        RestMockService restMockService = project.getRestMockServiceByName(mockServiceName);

        if (restMockService != null && restMockService.getMockRunner() != null) {
            restMockService.getMockRunner().stop();
        }

    }

    @Test
    public void shouldSaveRestMockWithSetNameToProject() {
        action.perform(restRequest, notUsed);
        List<RestMockService> serviceList = project.getRestMockServiceList();
        assertThat(serviceList.size(), is(1));

        RestMockService service = project.getRestMockServiceByName(mockServiceName);
        assertThat(service.getName(), is(mockServiceName));
    }

    @Test
    public void shouldSetAGoodNameOnTheRestMockAction() {
        action.perform(restRequest, notUsed);

        RestMockService service = project.getRestMockServiceByName(mockServiceName);
        assertThat(service.getMockOperationByName(requestPath), is(NOT_NULL));
    }

    @Test
    public void shouldFireProjectChangedEvent() {
        ProjectListenerAdapter listener = mock(ProjectListenerAdapter.class);
        project.addProjectListener(listener);
        action.perform(restRequest, notUsed);
        verify(listener, times(1)).mockServiceAdded(any(RestMockService.class));
    }

    @Test
    public void shouldAddASecondResponseToAnOperationForTheSamePath() throws SoapUIException {
        action.perform(restRequest, notUsed);
        action.perform(restRequest, notUsed);

        int mockResponseCount = getFirstMockOperation().getMockResponseCount();

        assertThat(mockResponseCount, is(2));
    }

    @Test
    public void shouldCreateNewOperationForDifferentPath() throws SoapUIException {
        action.perform(restRequest, notUsed);
        restRequest.setPath("someotherpath");
        action.perform(restRequest, notUsed);

        int mockResponseCount = getFirstMockOperation().getMockResponseCount();

        assertThat(mockResponseCount, is(1));
        assertThat(getFirstRestMockService().getMockOperationCount(), is(2));
    }

    public RestMockAction getFirstMockOperation() {
        return getFirstRestMockService().getMockOperationAt(0);
    }

    public RestMockService getFirstRestMockService() {
        return project.getRestMockServiceAt(0);
    }

    @Test
    public void shouldCreateNewOperationForDifferentVerb() {
        action.perform(restRequest, notUsed);
        int mockOperationCount = getFirstRestMockService().getMockOperationCount();
        assertThat(mockOperationCount, is(1));

        restRequest.setMethod(RestRequestInterface.HttpMethod.TRACE);
        action.perform(restRequest, notUsed);
        mockOperationCount = getFirstRestMockService().getMockOperationCount();
        assertThat(mockOperationCount, is(2));
    }

    @Test
    public void shouldSaveHeadersOnMockResponse() {
        action.perform(restRequest, notUsed);

        StringToStringsMap responseHeaders = getActualResponseHeaders();
        assertThat(responseHeaders.get(ONE_HEADER).get(0), is("oneValue"));
        assertThat(responseHeaders.get(ANOTHER_HEADER).get(0), is("anotherValue"));
    }

    public StringToStringsMap getActualResponseHeaders() {
        return getFirstMockOperation().getMockResponseAt(0).getResponseHeaders();
    }

    @Test
    public void shouldNotSaveSomeHeaders() {
        String[] headersNotToSave = new String[]{HEADER_STATUS, HEADER_CONTENT_TYPE, HEADER_CONTENT_LENGTH};

        action.perform(restRequest, notUsed);

        StringToStringsMap responseHeaders = getActualResponseHeaders();

        for (String header : headersNotToSave) {
            assertThat(responseHeaders.get(header), is(NULL));
        }
    }

    @Test
    public void shouldAddEmptyResponses() {
        restRequest.setResponse(null, null);
        action.perform(restRequest, notUsed);

        assertThat(getFirstMockOperation().getMockResponseCount(), is(1));
    }

    @Test
    public void shouldExpandPathParameters() throws SoapUIException {
        RestService restService = (RestService) project.addNewInterface("a rest resource", RestServiceFactory.REST_TYPE);

        RestResource restResource = restService.addNewResource("resource", "http://some.path.example.com");

        RestMethod restMethod = restResource.addNewMethod("get");
        RestRequest anotherRestRequest = createRestRequest(restMethod, "/template/{id}/path");
        anotherRestRequest.setPropertyValue("id", "42");

        action.perform(anotherRestRequest, notUsed);

        assertThat(getFirstMockOperation().getResourcePath(), is("/template/42/path"));
    }

    @Test
    public void shouldExpandMultiplePathParameters() throws SoapUIException {
        RestService restService = (RestService) project.addNewInterface("a rest resource", RestServiceFactory.REST_TYPE);

        RestResource restResource = restService.addNewResource("resource", "http://some.path.example.com");

        RestMethod restMethod = restResource.addNewMethod("get");
        RestRequest anotherRestRequest = createRestRequest(restMethod, "/template/{id}/path/{version}");
        anotherRestRequest.setPropertyValue("id", "42");
        anotherRestRequest.setPropertyValue("version", "3.1");

        action.perform(anotherRestRequest, notUsed);

        assertThat(getFirstMockOperation().getResourcePath(), is("/template/42/path/3.1"));
        assertThat(getFirstMockOperation().getName(), is("/template/42/path/3.1"));
    }

    @Test
    public void shouldAddEndPointToRestService() throws SoapUIException {

        int endPointCount = restRequest.getOperation().getService().getEndpoints().length;
        int expectedEndPointCount = endPointCount + 1;

        action.perform(restRequest, notUsed);

        assertThat(restRequest.getOperation().getService().getEndpoints().length, is(expectedEndPointCount));
    }

    private RestRequest createRestRequest(RestMethod restMethod, String path) {
        RestRequest anotherRestRequest = restMethod.addNewRequest("another");
        anotherRestRequest.setPath(path);
        anotherRestRequest.setMethod(RestRequestInterface.HttpMethod.GET);
        return anotherRestRequest;
    }

    private void mockPromptDialog() {
        originalDialogs = UISupport.getDialogs();
        StubbedDialogs dialogs = new StubbedDialogs();
        UISupport.setDialogs(dialogs);
        dialogs.mockPromptWithReturnValue(mockServiceName);
    }
}
