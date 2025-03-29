/*
 * SoapUI, Copyright (C) 2004-2022 SmartBear Software
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

package com.eviware.soapui.impl.actions;

import com.eviware.soapui.config.RestParametersConfig;
import com.eviware.soapui.impl.rest.RestMethod;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.RestRequestInterface;
import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.rest.RestServiceFactory;
import com.eviware.soapui.impl.rest.RestURIParser;
import com.eviware.soapui.impl.rest.actions.explorer.RequestInspectionData;
import com.eviware.soapui.impl.rest.support.RestParamProperty;
import com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder;
import com.eviware.soapui.impl.rest.support.RestURIParserImpl;
import com.eviware.soapui.impl.rest.support.RestUtils;
import com.eviware.soapui.impl.rest.support.XmlBeansRestParamsTestPropertyHolder;
import com.eviware.soapui.impl.support.AbstractInterface;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.support.ModelItemNamer;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import org.apache.commons.lang.ArrayUtils;

import java.net.MalformedURLException;
import java.util.Map;

import static com.eviware.soapui.impl.actions.RestServiceBuilder.ModelCreationStrategy.CREATE_NEW_MODEL;
import static com.eviware.soapui.impl.actions.RestServiceBuilder.ModelCreationStrategy.REUSE_MODEL;

public class RestServiceBuilder {

    public enum ModelCreationStrategy {
        CREATE_NEW_MODEL, REUSE_MODEL
    }

    public static class RequestInfo {
        private final String uri;
        private final RestRequestInterface.HttpMethod requestMethod;

        public RequestInfo(String uri, RestRequestInterface.HttpMethod requestMethod) {
            this.uri = uri;
            this.requestMethod = requestMethod;
        }

        public String getUri() {
            return uri;
        }

        public RestRequestInterface.HttpMethod getRequestMethod() {
            return requestMethod;
        }
    }

    public void createRestService(WsdlProject project, String uri) throws MalformedURLException {
        if (StringUtils.isNullOrEmpty(uri)) {
            return;
        }

        RestResource restResource = createResource(ModelCreationStrategy.CREATE_NEW_MODEL, project, uri);
        RestRequest restRequest = addNewRequest(addNewMethod(ModelCreationStrategy.CREATE_NEW_MODEL,
                restResource, RestRequestInterface.HttpMethod.GET));
        copyParameters(extractParams(uri), restResource.getParams());
        UISupport.select(restRequest);
        UISupport.showDesktopPanel(restRequest);
    }

    public RestRequest createRestServiceHeadlessFromUri(WsdlProject project, RequestInfo requestInfo,
                                                        ModelCreationStrategy methodReuseStrategy) throws MalformedURLException {
        RestResource restResource = createResource(REUSE_MODEL, project, requestInfo.getUri());
        RestMethod restMethod = addNewMethod(methodReuseStrategy, restResource, requestInfo.getRequestMethod());
        RestRequest restRequest = addNewRequest(restMethod);
        copyParametersWithDefaultsOnResource(extractParams(requestInfo.getUri()), restMethod.getParams(), restRequest.getParams());
        return restRequest;
    }

    public RestRequest createRestServiceWithMethod(WsdlProject project, String uri,
                                                   RestRequestInterface.HttpMethod method,
                                                   boolean showDesktopPanel,
                                                   String requestName) throws MalformedURLException {
        if (StringUtils.isNullOrEmpty(uri)) {
            throw new MalformedURLException("The URL is null or empty");
        }
        RestResource restResource = createResource(REUSE_MODEL, project, uri);
        ModelCreationStrategy methodReuseStrategy = CREATE_NEW_MODEL;
        for (RestMethod restMethod : restResource.getRestMethodList()) {
            if (restMethod.getMethod() == method) {
                methodReuseStrategy = REUSE_MODEL;
            }
        }
        RestMethod restMethod = addNewMethod(methodReuseStrategy, restResource, method);
        RestRequest restRequest;
        if (requestName != null) {
            restRequest = restMethod.addNewRequest(requestName);
        } else {
            restRequest = addNewRequest(restMethod);
        }
        copyParameters(extractParams(uri), restMethod.getParams());
        if (showDesktopPanel) {
            UISupport.select(restRequest);
            UISupport.showDesktopPanel(restRequest);
        }
        return restRequest;
    }

    public RestRequest createRestServiceFromInspectionData(WsdlProject project, String uri,
                                                           RestRequestInterface.HttpMethod method,
                                                           RequestInspectionData inspectionData,
                                                           boolean showDesktopPanel,
                                                           String requestName) throws MalformedURLException {
        RestRequest restRequest = createRestServiceWithMethod(project, uri, method, showDesktopPanel, requestName);
        if (inspectionData.getHeaders() != null) {
            applyHeaders(restRequest, inspectionData.getHeaders());
        }
        if (StringUtils.hasContent(inspectionData.getRequestBody())) {
            restRequest.setRequestContent(inspectionData.getRequestBody());
        }
        return restRequest;
    }

    protected RestParamsPropertyHolder extractParams(String uri) {
        RestParamsPropertyHolder params = new XmlBeansRestParamsTestPropertyHolder(null,
                RestParametersConfig.Factory.newInstance());
        extractAndFillParameters(uri, params);
        return params;
    }

    protected RestResource createResource(ModelCreationStrategy creationStrategy, WsdlProject project, String uri) throws MalformedURLException {
        RestURIParser restURIParser = new RestURIParserImpl(uri);
        String resourcePath = restURIParser.getResourcePath();
        String host = restURIParser.getEndpoint();

        RestService restService = null;
        if (creationStrategy == ModelCreationStrategy.REUSE_MODEL) {
            AbstractInterface<?> existingInterface = project.getInterfaceByName(host);
            if (existingInterface instanceof RestService service && ArrayUtils.contains(existingInterface.getEndpoints(), host)) {
                restService = service;
            }
        }
        if (restService == null) {
            restService = (RestService) project.addNewInterface(host, RestServiceFactory.REST_TYPE);
            restService.addEndpoint(restURIParser.getEndpoint());
        }
        if (creationStrategy == ModelCreationStrategy.REUSE_MODEL) {
            RestResource existingResource = restService.getResourceByFullPath(RestResource.removeMatrixParams(resourcePath));
            if (existingResource != null) {
                return existingResource;
            }
        }
        return restService.addNewResource(restURIParser.getResourceName(), resourcePath);
    }

    protected void extractAndFillParameters(String uri, RestParamsPropertyHolder params) {
        // This does lot of magic including extracting and filling up parameters on the params
        RestUtils.extractParams(uri, params, false, RestUtils.TemplateExtractionOption.EXTRACT_TEMPLATE_PARAMETERS);
    }

    //TODO: In advanced version we have to apply filtering like which type of parameter goes to which location
    protected void copyParameters(RestParamsPropertyHolder srcParams, RestParamsPropertyHolder destinationParams) {
        for (int i = 0; i < srcParams.size(); i++) {
            RestParamProperty prop = srcParams.getPropertyAt(i);

            destinationParams.addParameter(prop);

        }
    }

    //TODO: In advanced version we have to apply filtering like which type of parameter goes to which location
    protected void copyParametersWithDefaultsOnResource(RestParamsPropertyHolder srcParams, RestParamsPropertyHolder resourceParams, RestParamsPropertyHolder requestParams) {
        for (int i = 0; i < srcParams.size(); i++) {
            RestParamProperty prop = srcParams.getPropertyAt(i);
            String value = prop.getValue();
            prop.setValue("");
            prop.setDefaultValue("");
            resourceParams.addParameter(prop);

            requestParams.getProperty(prop.getName()).setValue(value);
        }
    }


    protected RestMethod addNewMethod(ModelCreationStrategy creationStrategy, RestResource restResource, RestRequestInterface.HttpMethod requestMethod) {
        if (creationStrategy == ModelCreationStrategy.REUSE_MODEL) {
            for (RestMethod restMethod : restResource.getRestMethodList()) {
                if (restMethod.getMethod() == requestMethod) {
                    return restMethod;
                }
            }
        }
        String methodName = ModelItemNamer.createName(restResource.getName(), restResource.getRestMethodList());
        RestMethod restMethod = restResource.addNewMethod(methodName);
        restMethod.setMethod(requestMethod);
        return restMethod;
    }

    protected RestRequest addNewRequest(RestMethod restMethod) {
        return restMethod.addNewRequest("Request " + (restMethod.getRequestCount() + 1));
    }

    private void applyHeaders(RestRequest restRequest, Map<String, String> headers) {
        RestParamsPropertyHolder requestPropertyHolder = restRequest.getRestMethod().getParams();
        for (Map.Entry<String, String> header : headers.entrySet()) {
            RestParamProperty property = requestPropertyHolder.addProperty(header.getKey());
            property.setStyle(RestParamsPropertyHolder.ParameterStyle.HEADER);
            property.setValue(header.getValue());
        }
    }

}
