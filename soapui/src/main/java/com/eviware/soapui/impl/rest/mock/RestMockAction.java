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

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.RESTMockActionConfig;
import com.eviware.soapui.config.RESTMockResponseConfig;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.RestRequestInterface;
import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.impl.support.AbstractMockOperation;
import com.eviware.soapui.impl.support.HasHelpUrl;
import com.eviware.soapui.impl.wsdl.mock.DispatchException;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.Operation;
import com.eviware.soapui.model.mock.MockResponse;
import com.eviware.soapui.support.UISupport;
import com.google.common.io.CharStreams;

import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public class RestMockAction extends AbstractMockOperation<RESTMockActionConfig, RestMockResponse> implements HasHelpUrl {
    private RestResource resource = null;

    public RestMockAction(RestMockService mockService, RESTMockActionConfig config) {
        super(config, mockService, RestMockAction.getIconName(config));

        mockService.getMockOperationByName(config.getName());

        List<RESTMockResponseConfig> responseConfigs = config.getResponseList();
        for (RESTMockResponseConfig responseConfig : responseConfigs) {
            RestMockResponse restMockResponse = new RestMockResponse(this, responseConfig);
            restMockResponse.addPropertyChangeListener(this);
            addMockResponse(restMockResponse);
        }

        super.setupConfig(config);
    }

    public RestMockAction(RestMockService mockService, RESTMockActionConfig config, RestRequest request) {
        this(mockService, config);
        resource = request.getResource();
    }

    public static String getIconName(RESTMockActionConfig methodConfig) {
        if (methodConfig.isSetMethod()) {
            return getIconName(methodConfig.getMethod());
        }
        return getDefaultIcon();
    }

    private static String getIconName(String method) {
        return "/mock_" + method.toLowerCase() + "_method.gif";
    }

    public static String getDefaultIcon() {
        return getIconName(RestRequestInterface.HttpMethod.GET.name());
    }

    @Override
    public RestMockService getMockService() {
        return (RestMockService) getParent();
    }

    @Override
    public void removeResponseFromConfig(int index) {
        getConfig().removeResponse(index);
    }

    @Override
    public Operation getOperation() {
        return resource;
    }

    @Override
    public String getScriptHelpUrl() {
        return HelpUrls.REST_MOCK_SCRIPTDISPATCH;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {

    }

    @Override
    public List<? extends ModelItem> getChildren() {
        return getMockResponses();
    }

    public RestMockResponse addNewMockResponse(String name) {

        RESTMockResponseConfig restMockResponseConfig = getConfig().addNewResponse();
        restMockResponseConfig.setName(name);

        RestMockResponse mockResponse = new RestMockResponse(this, restMockResponseConfig);
        addMockResponse(mockResponse);


        if (getMockResponseCount() == 1 && restMockResponseConfig.getResponseContent() != null) {
            setDefaultResponse(restMockResponseConfig.getResponseContent().toString());
        }

        (getMockService()).fireMockResponseAdded(mockResponse);
        notifyPropertyChanged("mockResponses", null, mockResponse);

        return mockResponse;
    }

    public RestMockResult dispatchRequest(RestMockRequest request) throws DispatchException {
        if (getMockResponseCount() == 0) {
            throw new DispatchException("Missing MockResponse(s) in MockOperation [" + getName() + "]");
        }

        try {
            RestMockResult result = new RestMockResult(request);

            MockResponse mockResponse = getDispatcher().selectMockResponse(request, result);

            result.setMockResponse(mockResponse);

            result.setMockOperation(this);

            if (mockResponse == null) {
                mockResponse = getMockResponseByName(this.getDefaultResponse());
            }

            if (mockResponse == null) {
                throw new DispatchException("Failed to find MockResponse");
            }

            result.setMockResponse(mockResponse);
            mockResponse.execute(request, result);

            return result;
        } catch (Exception e) {
            throw new DispatchException(e);
        }
    }

    public String getResourcePath() {
        return getConfig().getResourcePath();
    }

    public void setMethod(RestRequestInterface.HttpMethod method) {
        getConfig().setMethod(method.name());
        setIcon(UISupport.createImageIcon(getIconName(method.name())));

        notifyPropertyChanged("httpMethod", null, this);
    }

    public RestRequestInterface.HttpMethod getMethod() {
        return RestRequestInterface.HttpMethod.valueOf(getConfig().getMethod());
    }

    public void setResourcePath(String path) {
        getConfig().setResourcePath(path);
        notifyPropertyChanged("resourcePath", null, this);
    }

    public void setResource(RestResource resource) {
        this.resource = resource;

    }


    @Override
    public void setExampleScript() {
        if (getScript() == null) {
            try {
                String groovyScriptName = "com/eviware/soapui/impl/rest/mock/dispatching-script-sample.groovy";
                InputStream groovyStream = getClass().getClassLoader().getResourceAsStream(groovyScriptName);
                InputStreamReader groovyReader = new InputStreamReader(groovyStream);
                String groovyScript = CharStreams.toString(groovyReader);

                setScript(groovyScript);
            } catch (IOException e) {
                SoapUI.logError(e);
            }
        }
    }

    public String getHelpUrl() {
        return HelpUrls.REST_MOCKSERVICE_ACTION;
    }
}
