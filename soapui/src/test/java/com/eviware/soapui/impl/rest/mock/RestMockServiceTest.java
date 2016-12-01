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

import com.eviware.soapui.config.CompressedStringConfig;
import com.eviware.soapui.config.RESTMockActionConfig;
import com.eviware.soapui.config.RESTMockResponseConfig;
import com.eviware.soapui.config.RESTMockServiceConfig;
import com.eviware.soapui.impl.rest.RestMethod;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.RestRequestInterface;
import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.model.project.Project;
import com.eviware.soapui.support.SoapUIException;
import com.eviware.soapui.utils.ModelItemFactory;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.eviware.soapui.impl.rest.RestRequestInterface.HttpMethod.GET;
import static com.eviware.soapui.impl.rest.RestRequestInterface.HttpMethod.TRACE;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RestMockServiceTest {
    public static final String PATH = "/aNicePath";
    private RestMockService restMockService;
    private RestRequest restRequest;

    @Before
    public void setup() throws SoapUIException {
        restMockService = ModelItemFactory.makeRestMockService();
        restRequest = ModelItemFactory.makeRestRequest();
        restRequest.setPath(PATH);
        restRequest.setMethod(GET);
    }

    @Test
    public void shouldAddNewMockAction() throws SoapUIException {
        restMockService.addNewMockAction(restRequest);

        RestMockAction restMockAction = restMockService.getMockOperationAt(0);
        assertEquals(PATH, restMockAction.getResourcePath());
        assertEquals(GET, restMockAction.getMethod());
    }

    @Test
    public void shouldAddNewMockActionWithEmptyPath() throws SoapUIException {
        restRequest.setPath("/");

        restMockService.addNewMockAction(restRequest);

        RestMockAction restMockAction = restMockService.getMockOperationAt(0);
        assertEquals("/", restMockAction.getResourcePath());
        assertEquals(GET, restMockAction.getMethod());
    }

    @Test
    public void isConstructedWithActionsAndResponses() throws SoapUIException {
        Project project = ModelItemFactory.makeWsdlProject();
        RESTMockServiceConfig config = createRestMockServiceConfig();

        RestMockService mockService = new RestMockService(project, config);

        assertEquals(config.getName(), mockService.getName());
        RestMockAction mockOperation = mockService.getMockOperationAt(0);
        RestMockResponse mockResponse = mockOperation.getMockResponseAt(0);
        assertEquals("Some content", mockResponse.getResponseContent());
    }


    @Test
    public void shouldFindMatchingOperation() throws SoapUIException {
        restRequest.setMethod(TRACE);
        restMockService.addNewMockAction(restRequest);
        RestMockAction restMockAction = restMockService.getMockOperationAt(0);
        RestMockAction matchingAction = (RestMockAction) restMockService.findMatchingOperationWithExactPath(PATH, TRACE);

        assertThat(matchingAction, is(restMockAction));
        assertEquals(PATH, matchingAction.getResourcePath());
    }

    @Test
    public void shouldFindPartiallyMatchingOperation() throws SoapUIException {
        restRequest.setMethod(TRACE);
        restMockService.addNewMockAction(restRequest);
        RestMockAction restMockAction = restMockService.getMockOperationAt(0);

        String requestPath = PATH + "/123";
        RestMockAction matchingAction = (RestMockAction) restMockService.findBestMatchedOperation(requestPath, TRACE);

        assertThat(matchingAction, is(restMockAction));
        assertEquals(PATH, matchingAction.getResourcePath());
    }


    @Test
    public void shouldFindBestPartiallyMatchingOperation() throws SoapUIException {

        restRequest.setMethod(TRACE);
        restMockService.addNewMockAction(restRequest);

        String updatedPath = PATH + "/123";
        restRequest.setPath(updatedPath);
        restMockService.addNewMockAction(restRequest);

        restRequest.setPath(PATH);
        restMockService.addNewMockAction(restRequest);

        RestMockAction restMockAction = restMockService.getMockOperationAt(1);

        String requestPath = updatedPath + "/123/231";
        RestMockAction matchingAction = (RestMockAction) restMockService.findBestMatchedOperation(requestPath, TRACE);

        assertThat(matchingAction, is(restMockAction));
        assertEquals(updatedPath, matchingAction.getResourcePath());
    }

    @Test
    public void shouldNotFindPartiallyMatchingOperation() throws SoapUIException {
        restRequest.setMethod(TRACE);
        restMockService.addNewMockAction(restRequest);

        String requestPath = PATH + "/123";
        RestMockAction matchingAction = (RestMockAction) restMockService.findMatchingOperationWithExactPath(requestPath, TRACE);

        assertThat(matchingAction, is(nullValue()));
    }

    @Test
    public void partialPathMatchingShouldBeDoneOnPrefix() throws SoapUIException {
        restMockService.addNewMockAction(restRequest);

        String requestPath = "/123" + PATH;
        RestMockAction matchingAction = (RestMockAction) restMockService.findBestMatchedOperation(requestPath, GET);

        assertThat(matchingAction, is(nullValue()));
    }

    @Test
    public void shouldNotFindMatchingOperationForDifferentMethod() throws SoapUIException {
        restRequest.setMethod(TRACE);
        restMockService.addNewMockAction(restRequest);
        RestMockAction matchingAction = (RestMockAction) restMockService.findBestMatchedOperation(PATH, GET);

        assertThat(matchingAction, is(nullValue()));
    }

    @Test
    public void shouldNotFindMatchingOperationForDifferentPath() throws SoapUIException {
        restRequest.setMethod(TRACE);
        restMockService.addNewMockAction(restRequest);
        RestMockAction matchingAction = (RestMockAction) restMockService.findBestMatchedOperation("/123", GET);

        assertThat(matchingAction, is(nullValue()));
    }

    @Test
    public void shouldSetPort() {
        restMockService.setPort(1234);
        assertThat(restMockService.getPort(), is(1234));
    }

    @Test
    public void shouldSetPath() {
        restMockService.setPath("myPath");
        assertThat(restMockService.getPath(), is("myPath"));
    }

    @Test
    public void shouldAddOperationToMockServiceAction() throws SoapUIException {
        RestMethod restMethod = mock(RestMethod.class);
        when(restMethod.getRequestAt(0)).thenReturn(restRequest);
        when(restMethod.getMethod()).thenReturn(RestRequestInterface.HttpMethod.GET);

        RestResource restResource = mock(RestResource.class);
        when(restResource.getRestMethodCount()).thenReturn(1);
        when(restResource.getFullPath()).thenReturn("/full/path");
        List<RestMethod> restMethodList = new ArrayList<RestMethod>();
        restMethodList.add(restMethod);
        when(restResource.getRestMethodList()).thenReturn(restMethodList);


        restMockService.addNewMockOperation(restResource);

        RestMockAction mockOperation = restMockService.getMockOperationAt(0);
        assertThat(mockOperation.getMethod(), is(restRequest.getMethod()));
        assertTrue(mockOperation.getResourcePath().contains("/full/path"));
    }

    private RESTMockServiceConfig createRestMockServiceConfig() {
        RESTMockServiceConfig config = RESTMockServiceConfig.Factory.newInstance();
        config.setName("Da service");
        RESTMockActionConfig mockActionConfig = config.addNewRestMockAction();
        mockActionConfig.setName("Da action");
        RESTMockResponseConfig mockResponseConfig = mockActionConfig.addNewResponse();
        mockResponseConfig.setName("Da response");
        CompressedStringConfig responseContent = CompressedStringConfig.Factory.newInstance();
        responseContent.setStringValue("Some content");
        mockResponseConfig.setResponseContent(responseContent);
        return config;
    }
}
