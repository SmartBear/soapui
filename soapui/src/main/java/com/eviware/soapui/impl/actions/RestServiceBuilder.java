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

package com.eviware.soapui.impl.actions;

import com.eviware.soapui.config.RestParametersConfig;
import com.eviware.soapui.impl.rest.RestMethod;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.RestRequestInterface;
import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.rest.RestServiceFactory;
import com.eviware.soapui.impl.rest.RestURIParser;
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

public class RestServiceBuilder {

    public void createRestService(WsdlProject project, String URI) throws MalformedURLException {
        if (StringUtils.isNullOrEmpty(URI)) {
            return;
        }

        RestResource restResource = createResource(ModelCreationStrategy.CREATE_NEW_MODEL, project, URI);
        RestRequest restRequest = addNewRequest(addNewMethod(ModelCreationStrategy.CREATE_NEW_MODEL, restResource, RestRequestInterface.HttpMethod.GET));
        copyParameters(extractParams(URI), restResource.getParams());
        UISupport.select(restRequest);
        UISupport.showDesktopPanel(restRequest);

    }

    protected RestParamsPropertyHolder extractParams(String URI) {
        RestParamsPropertyHolder params = new XmlBeansRestParamsTestPropertyHolder(null,
                RestParametersConfig.Factory.newInstance());
        extractAndFillParameters(URI, params);
        return params;
    }

    protected RestResource createResource(ModelCreationStrategy creationStrategy, WsdlProject project, String URI) throws MalformedURLException {
        RestURIParser restURIParser = new RestURIParserImpl(URI);
        String resourcePath = restURIParser.getResourcePath();
        String host = restURIParser.getEndpoint();

        RestService restService = null;
        if (creationStrategy == ModelCreationStrategy.REUSE_MODEL) {
            AbstractInterface<?> existingInterface = project.getInterfaceByName(host);
            if (existingInterface instanceof RestService && ArrayUtils.contains(existingInterface.getEndpoints(), host)) {
                restService = (RestService) existingInterface;
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

    protected void extractAndFillParameters(String URI, RestParamsPropertyHolder params) {
        // This does lot of magic including extracting and filling up parameters on the params
        RestUtils.extractParams(URI, params, false, RestUtils.TemplateExtractionOption.EXTRACT_TEMPLATE_PARAMETERS);
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

    protected static enum ModelCreationStrategy {
        CREATE_NEW_MODEL, REUSE_MODEL
    }
}
