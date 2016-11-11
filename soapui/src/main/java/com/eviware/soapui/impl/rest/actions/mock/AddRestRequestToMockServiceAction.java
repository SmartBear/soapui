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
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.mock.RestMockAction;
import com.eviware.soapui.impl.rest.mock.RestMockResponse;
import com.eviware.soapui.impl.rest.mock.RestMockService;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.submit.transports.http.HttpResponse;
import com.eviware.soapui.model.mock.MockOperation;
import com.eviware.soapui.model.mock.MockService;
import com.eviware.soapui.model.support.ModelSupport;
import com.eviware.soapui.support.MessageSupport;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;
import com.eviware.soapui.support.types.StringToStringsMap;

import java.util.ArrayList;
import java.util.List;

public class AddRestRequestToMockServiceAction extends AbstractSoapUIAction<RestRequest> {

    private static final String SELECT_MOCKSERVICE_OPTION = "Create new..";
    public static final String SOAPUI_ACTION_ID = "AddRestRequestToMockServiceAction";
    private static final MessageSupport messages = MessageSupport.getMessages(AddRestRequestToMockServiceAction.class);
    private static List<String> HEADERS_TO_IGNORE = new ArrayList<String>();

    static {
        HEADERS_TO_IGNORE.add("#status#");
        HEADERS_TO_IGNORE.add("Content-Type");
        HEADERS_TO_IGNORE.add("Content-Length");
    }


    public AddRestRequestToMockServiceAction() {
        super(messages.get("Title"), messages.get("Description"));
    }

    @Override
    public void perform(RestRequest restRequest, Object param) {
        String title = getName();

        RestMockService mockService = null;
        WsdlProject project = restRequest.getOperation().getInterface().getProject();

        while (mockService == null) {

            if (project.getRestMockServiceCount() > 0) {
                String option = promptForMockServiceSelection(title, project);
                boolean userCancelled = option == null;
                if (userCancelled) {
                    return;
                }

                mockService = project.getRestMockServiceByName(option);
            }

            if (mockService == null) {
                mockService = createNewMockService(title, project);
                UISupport.showDesktopPanel(mockService);
                maybeStart(mockService);
            }
        }

        addRequestToMockService(restRequest, mockService);
        restRequest.getOperation().getService().addEndpoint(mockService.getLocalEndpoint());
    }


    private void maybeStart(MockService mockService) {
        try {
            mockService.startIfConfigured();
        } catch (Exception e) {
            SoapUI.logError(e);
            UISupport.showErrorMessage(e.getMessage());
        }
    }


    private String promptForMockServiceSelection(String title, WsdlProject project) {
        String[] mockServices = ModelSupport.getNames(project.getRestMockServiceList(),
                new String[]{SELECT_MOCKSERVICE_OPTION});

        // prompt
        return UISupport.prompt("Select RESTMockService for adding REST request", title, mockServices);
    }

    private RestMockService createNewMockService(String title, WsdlProject project) {
        String mockServiceName = promptForServiceName(title, project);
        return project.addNewRestMockService(mockServiceName);
    }

    private String promptForServiceName(String title, WsdlProject project) {
        String defaultName = "REST MockService " + (project.getRestMockServiceCount() + 1);
        return UISupport.prompt("Enter name of new MockService", title, defaultName);
    }

    private void addRequestToMockService(RestRequest restRequest, RestMockService mockService) {
        MockOperation matchedOperation = mockService.findOrCreateNewOperation(restRequest);

        int responseCount = matchedOperation.getMockResponseCount() + 1;
        String responseName = "Response " + responseCount;

        RestMockResponse mockResponse = ((RestMockAction) matchedOperation).addNewMockResponse(responseName);
        // add expected response if available
        if (restRequest != null && restRequest.getResponse() != null) {
            copyResponseContent(restRequest, mockResponse);
            copyHeaders(restRequest, mockResponse);
        }
    }

    private void copyHeaders(RestRequest restRequest, RestMockResponse mockResponse) {
        StringToStringsMap requestHeaders = restRequest.getResponse().getResponseHeaders();
        for (String header : HEADERS_TO_IGNORE) {
            requestHeaders.remove(header);
        }
        mockResponse.setResponseHeaders(requestHeaders);
    }

    private void copyResponseContent(RestRequest restRequest, RestMockResponse mockResponse) {
        HttpResponse response = restRequest.getResponse();

        if (response.getContentAsString() != null) {
            mockResponse.setResponseContent(response.getContentAsString());
            mockResponse.setContentType(response.getContentType());
        }

    }

}
