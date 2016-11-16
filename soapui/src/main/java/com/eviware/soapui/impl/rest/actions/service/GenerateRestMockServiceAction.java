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

package com.eviware.soapui.impl.rest.actions.service;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.rest.mock.RestMockService;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.model.mock.MockOperation;
import com.eviware.soapui.model.mock.MockService;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AForm;

import java.util.List;

public class GenerateRestMockServiceAction extends AbstractSoapUIAction<RestService> {
    XFormDialog dialog = null;

    public GenerateRestMockServiceAction() {
        super("Generate REST Mock Service", "Generates a REST mock service containing all resources of this REST service");
    }

    @Override
    public void perform(RestService restService, Object param) {
        createDialog(restService);

        if (dialog.show()) {
            String mockServiceName = dialog.getValue(Form.MOCKSERVICE_NAME);
            RestMockService mockService = getMockService(mockServiceName, restService.getProject());

            if (mockService != null) {
                populateMockService(restService, mockService);
                restService.addEndpoint(mockService.getLocalEndpoint());

                UISupport.showDesktopPanel(mockService);
                maybeStart(mockService);
            }
        }
    }

    private void createDialog(RestService restService) {
        if (dialog == null) {
            dialog = ADialogBuilder.buildDialog(Form.class);
        }
        String nextMockServiceName = nextMockServiceName(restService);
        dialog.setValue(Form.MOCKSERVICE_NAME, nextMockServiceName);
    }

    private void maybeStart(MockService mockService) {
        try {
            mockService.startIfConfigured();
        } catch (Exception e) {
            SoapUI.logError(e);
            UISupport.showErrorMessage(e.getMessage());
        }
    }

    private String nextMockServiceName(RestService restService) {
        int nextMockServiceCount = restService.getProject().getRestMockServiceCount() + 1;
        return "REST MockService " + nextMockServiceCount;
    }

    private void populateMockService(RestService restService, RestMockService mockService) {
        mockService.setPath("/");
        mockService.setPort(8089);
        addMockOperations(restService, mockService);
    }

    private RestMockService getMockService(String mockServiceName, WsdlProject project) {
        if (StringUtils.isNullOrEmpty(mockServiceName)) {
            UISupport.showInfoMessage("The mock service name can not be empty");
            return null;
        }

        if (project.getRestMockServiceByName(mockServiceName) == null) {
            return project.addNewRestMockService(mockServiceName);
        } else {
            UISupport.showInfoMessage("The mock service name need to be unique. '" + mockServiceName + "' already exists.");
            return null;
        }
    }

    private void addMockOperations(RestService restService, RestMockService mockService) {
        for (RestResource oneResource : restService.getAllResources()) {
            List<MockOperation> listOfOperations = mockService.addNewMockOperationsFromResource(oneResource);

            for (MockOperation mockOperation : listOfOperations) {
                if (mockOperation != null) {
                    mockOperation.addNewMockResponse("Response 1");
                }
            }
        }
    }

    /*
     * only for injecting the dialog when testing
     */
    protected void setFormDialog(XFormDialog dialog) {
        this.dialog = dialog;
    }

    @AForm(name = "Generate REST Mock Service", description = "Set name for the new REST Mock Service", helpUrl = HelpUrls.GENERATE_REST_MOCKSERVICE)
    protected interface Form {
        @AField(name = "MockService Name", description = "The Mock Service name", type = AField.AFieldType.STRING)
        public final static String MOCKSERVICE_NAME = "MockService Name";
    }
}
