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

package com.eviware.soapui.utils;

import com.eviware.soapui.config.MockOperationConfig;
import com.eviware.soapui.config.MockResponseConfig;
import com.eviware.soapui.config.MockServiceConfig;
import com.eviware.soapui.config.OAuth2ProfileConfig;
import com.eviware.soapui.config.OAuth2ProfileContainerConfig;
import com.eviware.soapui.config.OperationConfig;
import com.eviware.soapui.config.RESTMockResponseConfig;
import com.eviware.soapui.config.RESTMockServiceConfig;
import com.eviware.soapui.config.RestMethodConfig;
import com.eviware.soapui.config.RestRequestConfig;
import com.eviware.soapui.config.RestResourceConfig;
import com.eviware.soapui.config.RestServiceConfig;
import com.eviware.soapui.config.TestCaseConfig;
import com.eviware.soapui.config.TestStepConfig;
import com.eviware.soapui.config.TestSuiteConfig;
import com.eviware.soapui.config.WsdlInterfaceConfig;
import com.eviware.soapui.impl.WorkspaceImpl;
import com.eviware.soapui.impl.rest.DefaultOAuth2ProfileContainer;
import com.eviware.soapui.impl.rest.OAuth2Profile;
import com.eviware.soapui.impl.rest.OAuth2ProfileContainer;
import com.eviware.soapui.impl.rest.RestMethod;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.RestRequestInterface;
import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.rest.mock.RestMockAction;
import com.eviware.soapui.impl.rest.mock.RestMockResponse;
import com.eviware.soapui.impl.rest.mock.RestMockService;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockOperation;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockResponse;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockService;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.RestTestRequestStep;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestRequestStep;
import com.eviware.soapui.model.workspace.WorkspaceFactory;
import com.eviware.soapui.support.SoapUIException;
import com.eviware.soapui.support.types.StringToStringMap;

/**
 * Class containing factory methods for commonly used model items, for use in automatic tests.
 */
public class ModelItemFactory {
    public static RestRequest makeRestRequest() throws SoapUIException {
        return new RestRequest(makeRestMethod(), RestRequestConfig.Factory.newInstance(), false);
    }

    public static RestRequest makeRestRequest(RestResource restResource) throws SoapUIException {
        return new RestRequest(makeRestMethod(restResource), RestRequestConfig.Factory.newInstance(), false);
    }

    private static RestMethod makeRestMethod(RestResource restResource) {
        return new RestMethod(restResource, RestMethodConfig.Factory.newInstance());
    }

    public static RestMethod makeRestMethod() throws SoapUIException {

        RestMethod restMethod = new RestMethod(makeRestResource(), RestMethodConfig.Factory.newInstance());
        restMethod.setMethod(RestRequestInterface.HttpMethod.GET);
        return restMethod;
    }

    public static RestResource makeRestResource() throws SoapUIException {
        return new RestResource(makeRestService(), RestResourceConfig.Factory.newInstance());
    }

    public static RestService makeRestService() throws SoapUIException {
        return new RestService(makeWsdlProject(), RestServiceConfig.Factory.newInstance());
    }

    public static RestMockAction makeRestMockAction() throws SoapUIException {
        RestMockService mockService = makeRestMockService();
        RestMockAction restMockAction = new RestMockAction(mockService, mockService.getConfig().addNewRestMockAction());
        mockService.addMockOperation(restMockAction);
        return restMockAction;
    }

    public static WsdlProject makeWsdlProject() throws SoapUIException {
        return new WsdlProject((WorkspaceImpl) WorkspaceFactory.getInstance().openWorkspace("testWorkSpace", new StringToStringMap()));
    }

    public static WsdlTestCase makeTestCase() throws SoapUIException {
        return new WsdlTestCase(new WsdlTestSuite(makeWsdlProject(), TestSuiteConfig.Factory.newInstance()), TestCaseConfig.Factory.newInstance(), false);
    }

    public static WsdlTestRequestStep makeTestRequestStep() throws SoapUIException {
        return new WsdlTestRequestStep(makeTestCase(), TestStepConfig.Factory.newInstance(), false);
    }

    public static RestTestRequestStep makeRestTestRequestStep() throws Exception {
        RestTestRequestStep restTestRequestStep = new RestTestRequestStep(makeTestCase(),
                TestStepConfig.Factory.newInstance(), false);
        restTestRequestStep.getConfig().setConfig(ModelItemFactory.makeRestRequest().getConfig());
        return restTestRequestStep;
    }

    public static WsdlOperation makeWsdlOperation() throws SoapUIException {
        return new WsdlOperation(makeWsdlInterface(), OperationConfig.Factory.newInstance());
    }

    private static WsdlInterface makeWsdlInterface() throws SoapUIException {
        return new WsdlInterface(makeWsdlProject(), WsdlInterfaceConfig.Factory.newInstance());
    }

    public static OAuth2ProfileContainer makeOAuth2ProfileContainer() throws SoapUIException {
        return new DefaultOAuth2ProfileContainer(makeWsdlProject(),
                OAuth2ProfileContainerConfig.Factory.newInstance());
    }

    public static OAuth2Profile makeOAuth2Profile() throws SoapUIException {
        OAuth2ProfileConfig configuration = OAuth2ProfileConfig.Factory.newInstance();
        return new OAuth2Profile(makeOAuth2ProfileContainer(), configuration);
    }


    public static RestMockService makeRestMockService() throws SoapUIException {
        WsdlProject project = makeWsdlProject();
        return makeRestMockService(project);
    }

    public static RestMockService makeRestMockService(WsdlProject project) {
        RESTMockServiceConfig restMockServiceConfig = project.getConfig().addNewRestMockService();
        restMockServiceConfig.setName("mockServiceConfig");
        RestMockService restMockService = new RestMockService(project, restMockServiceConfig);
        project.addRestMockService(restMockService);
        return restMockService;
    }

    public static WsdlMockOperation makeWsdlMockOperation() throws SoapUIException {
        return new WsdlMockOperation(makeWsdlMockService(), MockOperationConfig.Factory.newInstance(), makeWsdlOperation());
    }

    private static WsdlMockService makeWsdlMockService() throws SoapUIException {
        return new WsdlMockService(makeWsdlProject(), MockServiceConfig.Factory.newInstance());
    }

    public static WsdlMockResponse makeWsdlMockResponse() throws SoapUIException {
        return new WsdlMockResponse(makeWsdlMockOperation(), MockResponseConfig.Factory.newInstance());
    }

    public static RestMockResponse makeRestResponse() throws SoapUIException {
        return new RestMockResponse(makeRestMockAction(), RESTMockResponseConfig.Factory.newInstance());
    }
}
