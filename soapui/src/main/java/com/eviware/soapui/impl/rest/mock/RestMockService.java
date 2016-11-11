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

import com.eviware.soapui.config.RESTMockActionConfig;
import com.eviware.soapui.config.RESTMockServiceConfig;
import com.eviware.soapui.impl.rest.RestMethod;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.impl.rest.support.RestUtils;
import com.eviware.soapui.impl.support.AbstractMockService;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockRunContext;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.Operation;
import com.eviware.soapui.model.mock.MockDispatcher;
import com.eviware.soapui.model.mock.MockOperation;
import com.eviware.soapui.model.project.Project;

import java.util.ArrayList;
import java.util.List;

import static com.eviware.soapui.impl.rest.RestRequestInterface.HttpMethod;

public class RestMockService extends AbstractMockService<RestMockAction, RESTMockServiceConfig> {

    public final static String ICON_NAME = "/rest_virt.png";
    public static final String STRING_ID = "REST";

    public String getStringID() {
        return STRING_ID;
    }

    public RestMockService(Project project, RESTMockServiceConfig config) {
        super(config, project, ICON_NAME);

        List<RESTMockActionConfig> restActionConfigList = config.getRestMockActionList();
        for (RESTMockActionConfig restActionConfig : restActionConfigList) {
            RestMockAction restMockAction = new RestMockAction(this, restActionConfig);
            addMockOperation(restMockAction);
        }

        if (!getConfig().isSetProperties()) {
            getConfig().addNewProperties();
        }

        setPropertiesConfig(config.getProperties());

    }

    @Override
    public String getIconName() {
        return ICON_NAME;
    }

    @Override
    public MockDispatcher createDispatcher(WsdlMockRunContext mockContext) {
        return new RestMockDispatcher(this, mockContext);
    }

    @Override
    public List<? extends ModelItem> getChildren() {
        return getMockOperationList();
    }

    public RestMockAction addNewMockAction(RestRequest restRequest) {
        RestMockAction mockAction = addEmptyMockAction(restRequest.getMethod(),
                RestUtils.getExpandedPath(restRequest.getPath(), restRequest.getParams(), restRequest));
        mockAction.setResource(restRequest.getResource());

        return mockAction;
    }


    public RestMockAction addEmptyMockAction(HttpMethod method, String path) {
        RESTMockActionConfig config = getConfig().addNewRestMockAction();

        String slashifiedPath = slashify(path);
        String name = path;

        config.setName(name);
        config.setMethod(method.name());
        config.setResourcePath(slashifiedPath);
        RestMockAction restMockAction = new RestMockAction(this, config);

        addMockOperation(restMockAction);
        fireMockOperationAdded(restMockAction);

        return restMockAction;
    }


    public MockOperation findOrCreateNewOperation(RestRequest restRequest) {
        String expandedPath = RestUtils.getExpandedPath(restRequest.getPath(), restRequest.getParams(), restRequest);

        MockOperation matchedOperation = findMatchingOperationWithExactPath(expandedPath, restRequest.getMethod());

        if (matchedOperation == null) {
            matchedOperation = addNewMockAction(restRequest);
        }
        return matchedOperation;
    }

    protected MockOperation findBestMatchedOperation(String pathToFind, HttpMethod verbToFind) {
        boolean includePartialMatch = true;
        return findMatchedOperation(pathToFind, verbToFind, includePartialMatch);
    }

    protected MockOperation findMatchingOperationWithExactPath(String pathToFind, HttpMethod verbToFind) {
        boolean dontIncludePartialMatch = false;
        return findMatchedOperation(pathToFind, verbToFind, dontIncludePartialMatch);
    }

    private MockOperation findMatchedOperation(String pathToFind, HttpMethod verbToFind, boolean includePartialMatch) {
        MockOperation bestMatchedOperation = null;

        for (MockOperation operation : getMockOperationList()) {
            String operationPath = ((RestMockAction) operation).getResourcePath();
            HttpMethod operationVerb = ((RestMockAction) operation).getMethod();

            boolean matchesPath = operationPath.equals(pathToFind);
            boolean matchesVerb = verbToFind == operationVerb;
            boolean matchesPathPartially = pathToFind.startsWith(operationPath);

            if (matchesPath && matchesVerb) {
                return operation;
            } else if (includePartialMatch && matchesPathPartially && matchesVerb) {
                bestMatchedOperation = getBestMatchedOperation(bestMatchedOperation, operation, operationPath);
            }
        }

        return bestMatchedOperation;
    }

    private MockOperation getBestMatchedOperation(MockOperation currentBestMatchedOperation, MockOperation operation, String operationPath) {
        MockOperation bestMatchedOperation = currentBestMatchedOperation;

        if (bestMatchedOperation == null || foundBetterMatch((RestMockAction) bestMatchedOperation, operationPath)) {
            bestMatchedOperation = operation;
        }
        return bestMatchedOperation;
    }

    private boolean foundBetterMatch(RestMockAction bestMatchedOperation, String operationPath) {
        return bestMatchedOperation.getResourcePath().length() < operationPath.length();
    }

    public boolean canIAddAMockOperation(RestMockAction mockOperation) {
        return this.getConfig().getRestMockActionList().contains(mockOperation.getConfig());
    }

    @Override
    public MockOperation addNewMockOperation(Operation operation) {
        return addNewMockOperationsFromResource((RestResource) operation).get(0);
    }

    public List<MockOperation> addNewMockOperationsFromResource(RestResource restResource) {
        List<MockOperation> actions = new ArrayList<MockOperation>();
        String path = RestUtils.getExpandedPath(restResource.getFullPath(), restResource.getParams(), restResource);

        if (restResource.getRestMethodCount() < 1) {
            actions.add(addEmptyMockAction(HttpMethod.GET, path));
        }

        for (RestMethod restMethod : restResource.getRestMethodList()) {
            actions.add(addEmptyMockAction(restMethod.getMethod(), path));
        }

        return actions;
    }

    private String slashify(String path) {
        if (!path.startsWith("/") && !path.isEmpty()) {
            return "/" + path;
        }

        return path;
    }

    @Override
    public String getHelpUrl() {
        return HelpUrls.REST_MOCKSERVICE_HELP_URL;
    }

}
