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

package com.eviware.soapui.impl.wsdl;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.RestRequestInterface;
import com.eviware.soapui.impl.rest.mock.RestMockAction;
import com.eviware.soapui.impl.rest.mock.RestMockResponse;
import com.eviware.soapui.impl.rest.mock.RestMockService;
import com.eviware.soapui.model.mock.MockService;
import com.eviware.soapui.support.SoapUIException;
import com.eviware.soapui.utils.ModelItemFactory;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static com.eviware.soapui.impl.rest.RestRequestInterface.HttpMethod.GET;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public class WsdlProjectWithRestMockTest {
    public static final String TEST_PROJECT_PATH = "/soapui-projects/BasicMock-soapui-4.6.3-Project.xml";
    private final String restMockResponseContent = "Some response";
    private WsdlProject project;
    private final String restMockServiceName = "Another Great Wow";
    private final String restMockResponseName = "Teh Response";

    @Before
    public void setUp() throws Exception {
        String fileName = SoapUI.class.getResource(TEST_PROJECT_PATH).toURI().toString();
        project = new WsdlProject(fileName);
    }

    @Test
    public void shouldSaveAndReloadRestMockServices() throws Exception {
        String expectedName = "Teh Awesome Mock Service";
        project.addNewRestMockService(expectedName);

        WsdlProject reloadedProject = saveAndReloadProject(project);

        RestMockService restMockService = reloadedProject.getRestMockServiceByName(expectedName);
        assertThat(restMockService, notNullValue());
        assertThat(restMockService.getName(), is(expectedName));
    }

    @Test
    public void shouldSaveAndReloadRestMockResponses() throws Exception {
        addRestMockResponseToProject();

        WsdlProject reloadedProject = saveAndReloadProject(project);

        RestMockService reloadedMockService = reloadedProject.getRestMockServiceByName(restMockServiceName);
        RestMockResponse reloadedMockResponse = reloadedMockService.getMockOperationAt(0).getMockResponseAt(0);
        assertThat(reloadedMockResponse, notNullValue());
        assertThat(reloadedMockResponse.getName(), is(restMockResponseName));
        assertThat(reloadedMockResponse.getResponseContent(), is(restMockResponseContent));
    }

    @Test
    public void shouldRemoveMockServices() throws Exception {
        int mockServiceCountBefore = project.getMockServiceCount();
        MockService mocka = project.addNewMockService("Mocka");
        assertThat(project.getMockServiceCount(), is(mockServiceCountBefore + 1));

        project.removeMockService(mocka);
        assertThat(project.getMockServiceCount(), is(mockServiceCountBefore));
        assertThat(project.getMockServiceByName("Mocka"), nullValue());
    }

    @Test
    public void shouldRemoveRestMockServices() throws Exception {
        int restMockServiceCountBefore = project.getRestMockServiceCount();
        MockService mocka = project.addNewRestMockService("Mocka");
        assertThat(project.getRestMockServiceCount(), is(restMockServiceCountBefore + 1));

        project.removeMockService(mocka);
        assertThat(project.getRestMockServiceCount(), is(restMockServiceCountBefore));
        assertThat(project.getRestMockServiceByName("Mocka"), nullValue());
    }

    @Test
    public void shouldNotResortMockOperationsOnReload() throws Exception {
        RestMockService restMockService = project.addNewRestMockService("x");
        restMockService.addEmptyMockAction(RestRequestInterface.HttpMethod.GET, "b");
        restMockService.addEmptyMockAction(RestRequestInterface.HttpMethod.GET, "a");

        WsdlProject reloadedProject = saveAndReloadProject(project);

        RestMockService mockServiceX = reloadedProject.getRestMockServiceByName("x");

        assertThat(mockServiceX.getMockOperationAt(0).getName(), is("b"));
        assertThat(mockServiceX.getMockOperationAt(1).getName(), is("a"));
    }

    private void addRestMockResponseToProject() throws SoapUIException {
        RestMockService restMockService = project.addNewRestMockService(restMockServiceName);
        RestRequest restRequest = ModelItemFactory.makeRestRequest();
        restRequest.setMethod(GET);
        restRequest.setName("REST Mock Action");
        restRequest.setPath("Resource/path/subpath");
        RestMockAction restMockAction = restMockService.addNewMockAction(restRequest);
        RestMockResponse mockResponse = restMockAction.addNewMockResponse("Response 1");
        mockResponse.setResponseContent(restMockResponseContent);
        mockResponse.setName(restMockResponseName);
    }

    protected WsdlProject saveAndReloadProject(WsdlProject project) throws Exception {
        File tempFile = File.createTempFile("soapuitemptestfile", ".xml");
        tempFile.deleteOnExit();
        project.saveIn(tempFile);
        return new WsdlProject(tempFile.toURI().toString());
    }
}
