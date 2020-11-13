/*
 * SoapUI, Copyright (C) 2004-2019 SmartBear Software
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

package com.eviware.soapui.impl.rest.support;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.AttachmentConfig;
import com.eviware.soapui.config.CompressedStringConfig;
import com.eviware.soapui.config.CredentialsConfig;
import com.eviware.soapui.config.HttpRequestConfig;
import com.eviware.soapui.config.OldRestRequestConfig;
import com.eviware.soapui.config.RestParameterConfig;
import com.eviware.soapui.config.RestParametersConfig;
import com.eviware.soapui.config.RestRequestConfig;
import com.eviware.soapui.config.RestRequestStepConfig;
import com.eviware.soapui.config.RestResourceRepresentationConfig;
import com.eviware.soapui.config.SettingsConfig;
import com.eviware.soapui.config.StringToStringMapConfig;
import com.eviware.soapui.config.StringToStringMapConfig.Entry;
import com.eviware.soapui.config.TestAssertionConfig;
import com.eviware.soapui.impl.rest.RestMethod;
import com.eviware.soapui.impl.rest.RestRepresentation;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.RestRequestInterface;
import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.teststeps.RestTestRequestStep;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.model.project.Project;
import com.eviware.soapui.model.testsuite.TestProperty;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Convert old-style REST Requests to new ones, creating REST Methods for them
 * or placing them into existing Methods.
 *
 * @author Dain Nilsson
 */
public class RestRequestConverter {

    private static Map<Project, Boolean> autoConvert = new HashMap<Project, Boolean>();
    private final static Logger log = LogManager.getLogger(RestRequestConverter.class);

    public static void convert(RestResource resource, OldRestRequestConfig oldConfig) {
        convert(resource, oldConfig, getMethod(resource, oldConfig.getMethod(), oldConfig.getName()));
    }

    public static RestMethod getMethod(RestResource resource, String methodType, String requestName) {
        WsdlProject project = resource.getService().getProject();
        if (!autoConvert.containsKey(project)) {
            autoConvert
                    .put(project,
                            UISupport
                                    .confirm(
                                            "The model for REST requests has changed slightly,\r\n"
                                                    + "introducing a new REST Method item in-between each REST Resource and Request.\r\n"
                                                    + "Any existing REST Request must now be placed under either an existing Method or a new one, "
                                                    + "either automatically or manually.\r\n\r\nWould You like SoapUI to do this automatically using the default values?",
                                            "Update REST model for project: " + project.getName()));
        }
        RestMethod method = null;
        List<String> options = new ArrayList<String>();
        for (int c = 0; c < resource.getRestMethodCount(); c++) {
            RestMethod restMethod = resource.getRestMethodAt(c);
            if (restMethod.getMethod().toString().equals(methodType)) {
                options.add(restMethod.getName());
            }
        }
        if (autoConvert.get(project)) {
            if (options.size() > 0) {
                method = resource.getRestMethodByName(options.get(0));
                log.info("Placed request '" + requestName + "' under method '" + method.getName() + "' in Resource '"
                        + resource.getName() + "'.");
            } else {
                method = resource.addNewMethod(methodType + " Method");
                method.setMethod(RestRequestInterface.HttpMethod.valueOf(methodType));
                log.info("Created new Method for Resource '" + resource.getName() + "'.");
            }
        } else {
            options.add("[Create new REST Method]");
            if (requestName == null) {
                requestName = "REST Request";
            }
            String message = "Select REST Method to place \"" + resource.getName() + " > " + requestName + "\" under.";
            Object op = UISupport.prompt(message, "Migrate REST Request", options.toArray());
            if (op != null) {
                int ix = options.indexOf(op);
                if (ix != -1 && ix != options.size() - 1) {
                    method = resource.getRestMethodByName((String) op);
                }
            } else {
                throw new RestConversionException("Cannot get RestMethod selection!");
            }
            if (method == null) {
                String name = UISupport.prompt("Name for REST " + methodType + " Method", "Create new REST Method",
                        methodType + " Method");
                if (name == null) {
                    throw new RestConversionException("Cannot get name for RestMethod!");
                }
                method = resource.addNewMethod(name);
                method.setMethod(RestRequestInterface.HttpMethod.valueOf(methodType));
            }
        }
        return method;
    }

    public static RestResource resolveResource(RestTestRequestStep requestStep) {
        Map<String, RestResource> options = new LinkedHashMap<String, RestResource>();

        WsdlProject project = requestStep.getTestCase().getTestSuite().getProject();
        String serviceName = requestStep.getRequestStepConfig().getService();
        RestService service = (RestService) project.getInterfaceByName(serviceName);
        if (service != null) {
            addResources(service, options);
        } else {
            for (Interface iface : project.getInterfaceList()) {
                if (iface instanceof RestService) {
                    addResources((RestService) iface, options);
                }
            }
        }
        options.put("<Delete TestRequest>", null);

        String message = "Select a new REST Resource to place TestRequest \"" + requestStep.getName() + "\" under.";
        Object op = UISupport.prompt(message, "Missing REST Resource for TestRequest", options.keySet().toArray());
        RestResource resource = options.get(op);

        return resource;
    }

    private static void addResources(RestService service, Map<String, RestResource> list) {
        for (RestResource resource : service.getResources().values()) {
            list.put(service.getName() + " > " + resource.getName(), resource);
        }
    }

    @SuppressWarnings("deprecation")
    private static void convert(RestResource resource, OldRestRequestConfig oldConfig, RestMethod method) {

        RestRequest request = method.addNewRequest(oldConfig.getName());

        XmlBeansRestParamsTestPropertyHolder params = new XmlBeansRestParamsTestPropertyHolder(null,
                oldConfig.getParameters());
        RestParamsPropertyHolder parentParams = method.getOverlayParams();

        for (TestProperty prop : params.values()) {
            if (!parentParams.containsKey(prop.getName())) {
                method.getParams().addParameter((RestParamProperty) prop);
            }
            request.setPropertyValue(prop.getName(), prop.getValue());
        }
        params.release();

        boolean exists;
        for (RestResourceRepresentationConfig rep : oldConfig.getRepresentationList()) {
            exists = false;
            for (RestRepresentation existing : method.getRepresentations(
                    RestRepresentation.Type.valueOf(rep.getType().toString()), rep.getMediaType())) {
                if (existing.getElement() == null && rep.getElement() == null
                        || existing.getElement().equals(rep.getElement())) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                RestRepresentation repr = method.addNewRepresentation(RestRepresentation.Type.valueOf(rep.getType()
                        .toString()));
                repr.setConfig((RestResourceRepresentationConfig) rep.copy());
            }
        }

        RestRequestConfig newConfig = request.getConfig();

        newConfig.setRequest(oldConfig.getRequest());

        for (AttachmentConfig ac : oldConfig.getAttachmentList()) {
            try {
                if (ac.isSetData()) {
                    File temp = File.createTempFile("pattern", ".suffix");
                    temp.deleteOnExit();
                    FileOutputStream out = new FileOutputStream(temp);
                    out.write(ac.getData());
                    request.attachFile(temp, true);
                } else {
                    request.attachFile(new File(ac.getUrl()), false);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        newConfig.setAttachmentArray(oldConfig.getAttachmentArray());

        if (oldConfig.isSetFullPath()) {
            newConfig.setFullPath(oldConfig.getFullPath());
        }
        if (oldConfig.isSetMediaType()) {
            newConfig.setMediaType(oldConfig.getMediaType());
        }
        if (oldConfig.isSetPostQueryString()) {
            newConfig.setPostQueryString(oldConfig.getPostQueryString());
        }
        if (oldConfig.isSetAccept()) {
            newConfig.setAccept(oldConfig.getAccept());
        }
        if (oldConfig.isSetDescription()) {
            newConfig.setDescription(oldConfig.getDescription());
        }
        if (oldConfig.isSetId()) {
            newConfig.setId(oldConfig.getId());
        }
        if (oldConfig.isSetSettings()) {
            newConfig.setSettings((SettingsConfig) oldConfig.getSettings().copy());
        }
        if (oldConfig.isSetSslKeystore()) {
            newConfig.setSslKeystore(oldConfig.getSslKeystore());
        }
        if (oldConfig.isSetTimestamp()) {
            newConfig.setTimestamp(oldConfig.getTimestamp());
        }
        if (oldConfig.isSetWadlId()) {
            newConfig.setWadlId(oldConfig.getWadlId());
        }

        request.updateConfig(newConfig);

    }

    public static HttpRequestConfig convert(OldRestRequestConfig old) {
        HttpRequestConfig config = HttpRequestConfig.Factory.newInstance();
        config.setAssertionArray(old.getAssertionList().toArray(new TestAssertionConfig[old.sizeOfAssertionArray()]));
        config.setAttachmentArray(old.getAttachmentList().toArray(new AttachmentConfig[old.sizeOfAttachmentArray()]));
        XmlObject obj = old.getCredentials();
        if (obj != null) {
            config.setCredentials((CredentialsConfig) obj.copy());
        }
        obj = old.getParameters();
        if (obj != null) {
            config.setParameters((RestParametersConfig) obj.copy());
        }

        obj = old.getRequest();
        if (obj != null) {
            config.setRequest((CompressedStringConfig) obj.copy());
        }
        obj = old.getSettings();
        if (obj != null) {
            config.setSettings((SettingsConfig) obj.copy());
        }
        if (old.isSetDescription()) {
            config.setDescription(old.getDescription());
        }
        config.setEncoding(old.getEncoding());
        config.setEndpoint(old.getEndpoint());
        config.setSslKeystore(old.getSslKeystore());
        if (old.isSetMediaType()) {
            config.setMediaType(old.getMediaType());
        }
        if (old.isSetMethod()) {
            config.setMethod(old.getMethod());
        }
        if (old.isSetName()) {
            config.setName(old.getName());
        }
        if (old.isSetPostQueryString()) {
            config.setPostQueryString(old.getPostQueryString());
        }
        return config;
    }

    public static HttpRequestConfig updateIfNeeded(XmlObject config) {
        try {
            if (config instanceof RestRequestStepConfig) {
                return convert(OldRestRequestConfig.Factory.parse(config.selectChildren(
                        "http://eviware.com/soapui/config", "restRequest")[0].toString()));
            } else {
                return (HttpRequestConfig) config.changeType(HttpRequestConfig.type);
            }
        } catch (XmlException e) {
            return HttpRequestConfig.Factory.newInstance();
        }
    }

    public static class RestConversionException extends RuntimeException {
        public RestConversionException(String message) {
            super(message);
        }
    }

    public static void updateRestTestRequest(RestTestRequestStep restTestRequestStep) {
        try {
            RestRequestStepConfig restRequestStepConfig = (RestRequestStepConfig) restTestRequestStep.getConfig()
                    .getConfig();
            RestRequestConfig restRequestConfig = restRequestStepConfig.getRestRequest();
            OldRestRequestConfig oldConfig = OldRestRequestConfig.Factory.parse(restRequestConfig.toString());

            RestParametersConfig oldParams = oldConfig.getParameters();
            if (oldParams != null) {
                StringToStringMapConfig newParams = restRequestConfig.addNewParameters();

                for (RestParameterConfig oldParam : oldParams.getParameterList()) {
                    if (StringUtils.hasContent(oldParam.getValue())) {
                        Entry entry = newParams.addNewEntry();
                        entry.setKey(oldParam.getName());
                        entry.setValue(oldParam.getValue());
                    }
                }

                restRequestConfig.getParameters().getDomNode().getParentNode()
                        .removeChild(restRequestConfig.getParameters().getDomNode());
            }
        } catch (XmlException e) {
            SoapUI.logError(e);
        }
    }
}
