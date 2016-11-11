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

package com.eviware.soapui.impl.rest;

import com.eviware.soapui.impl.rest.panels.request.inspectors.schema.InferredSchemaManager;
import com.eviware.soapui.impl.rest.support.RestParamProperty;
import com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder;
import com.eviware.soapui.impl.wsdl.support.Constants;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.soapui.support.xml.XmlUtils;
import net.java.dev.wadl.x2009.x02.ApplicationDocument;
import net.java.dev.wadl.x2009.x02.ApplicationDocument.Application;
import net.java.dev.wadl.x2009.x02.DocDocument.Doc;
import net.java.dev.wadl.x2009.x02.GrammarsDocument.Grammars;
import net.java.dev.wadl.x2009.x02.MethodDocument.Method;
import net.java.dev.wadl.x2009.x02.ParamDocument.Param;
import net.java.dev.wadl.x2009.x02.ParamStyle;
import net.java.dev.wadl.x2009.x02.RepresentationDocument.Representation;
import net.java.dev.wadl.x2009.x02.RequestDocument.Request;
import net.java.dev.wadl.x2009.x02.ResourceDocument.Resource;
import net.java.dev.wadl.x2009.x02.ResourcesDocument.Resources;
import net.java.dev.wadl.x2009.x02.ResponseDocument.Response;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WadlGenerator {
    private RestService restService;

    private boolean isWADL11 = true;

    public WadlGenerator(RestService restService) {
        this.restService = restService;
    }

    public XmlObject generateWadl() {
        ApplicationDocument applicationDocument = ApplicationDocument.Factory.newInstance();
        Application application = applicationDocument.addNewApplication();

        createDoc(application.addNewDoc(), restService);

        Resources resources = application.addNewResources();

        // use first endpoint for now -> this should be configurable
        String basePath = restService.getBasePath();
        String[] endpoints = restService.getEndpoints();
        if (endpoints.length > 0) {
            basePath = endpoints[0] + basePath;
        }

        resources.setBase(basePath);

        for (int c = 0; c < restService.getOperationCount(); c++) {
            resources.addNewResource().set(generateWadlResource(restService.getOperationAt(c)));
        }

        String[] namespaces = InferredSchemaManager.getInferredSchema(restService).getNamespaces();
        if (namespaces.length > 0) {
            Grammars grammars = application.addNewGrammars();
            for (String namespace : namespaces) {
                grammars.addNewInclude().setHref(InferredSchemaManager.filenameForNamespace(namespace));
            }
        }

        if (!isWADL11) {
            XmlOptions options = new XmlOptions();
            StringToStringMap subst = new StringToStringMap();
            subst.put(Constants.WADL11_NS, Constants.WADL10_NS);
            options.setLoadSubstituteNamespaces(subst);
            try {
                // return XmlObject.Factory.parse( applicationDocument.xmlText(),
                // options );
                return XmlUtils.createXmlObject(applicationDocument.xmlText(), options);
            } catch (XmlException e) {
                e.printStackTrace();
            }
        }

        return applicationDocument;
    }

    private XmlObject generateWadlResource(RestResource resource) {
        Resource resourceConfig = Resource.Factory.newInstance();
        createDoc(resourceConfig.addNewDoc(), resource);
        String path = resource.getPath();
        if (path.startsWith("/")) {
            path = path.length() > 1 ? path.substring(1) : "";
        }

        resourceConfig.setPath(path);
        resourceConfig.setId(resource.getName());

        RestParamsPropertyHolder params = resource.getParams();
        for (int c = 0; c < params.size(); c++) {
            generateParam(resourceConfig.addNewParam(), params.getPropertyAt(c));
        }

        for (int c = 0; c < resource.getChildResourceCount(); c++) {
            resourceConfig.addNewResource().set(generateWadlResource(resource.getChildResourceAt(c)));
        }

        for (int c = 0; c < resource.getRestMethodCount(); c++) {
            RestMethod restMethod = resource.getRestMethodAt(c);
            generateWadlMethod(resourceConfig, restMethod);
        }

        return resourceConfig;
    }

    private void generateParam(Param paramConfig, RestParamProperty param) {
        paramConfig.setName(param.getName());

        if (StringUtils.hasContent(param.getDefaultValue())) {
            paramConfig.setDefault(param.getDefaultValue());
        }

        paramConfig.setType(param.getType());
        paramConfig.setRequired(param.getRequired());
        paramConfig.setDefault(param.getDefaultValue());

        if (StringUtils.hasContent(param.getDescription())) {
            createDoc(paramConfig.addNewDoc(), param.getName() + " Parameter", param.getDescription());
        }

        String[] options = param.getOptions();
        for (String option : options) {
            paramConfig.addNewOption().setValue(option);
        }

        ParamStyle.Enum style = ParamStyle.QUERY;
        switch (param.getStyle()) {
            case HEADER:
                style = ParamStyle.HEADER;
                break;
            case MATRIX:
                style = ParamStyle.MATRIX;
                break;
            case PLAIN:
                style = ParamStyle.PLAIN;
                break;
            case TEMPLATE:
                style = ParamStyle.TEMPLATE;
                break;
        }

        paramConfig.setStyle(style);
    }

    private void createDoc(Doc docConfig, ModelItem modelItem) {
        createDoc(docConfig, modelItem.getName(), modelItem.getDescription());
    }

    private void createDoc(Doc docConfig, String name, String description) {
        docConfig.setLang("en");
        docConfig.setTitle(name);
        docConfig.getDomNode().appendChild(docConfig.getDomNode().getOwnerDocument().createTextNode(description));
    }

    private void generateWadlMethod(Resource resourceConfig, RestMethod restMethod) {
        Method methodConfig = resourceConfig.addNewMethod();
        createDoc(methodConfig.addNewDoc(), restMethod);
        methodConfig.setName(restMethod.getMethod().toString());
        methodConfig.setId(restMethod.getName());
        Request requestConfig = methodConfig.addNewRequest();

        Map<String, RestParamProperty> defaultParams = new HashMap<String, RestParamProperty>();
        for (RestParamProperty defaultParam : restMethod.getResource().getDefaultParams()) {
            defaultParams.put(defaultParam.getName(), defaultParam);
        }

        RestParamsPropertyHolder params = restMethod.getParams();
        for (int c = 0; c < params.size(); c++) {
            RestParamProperty param = params.getPropertyAt(c);
            if (!defaultParams.containsKey(param.getName()) || !param.equals(defaultParams.get(param.getName()))) {
                generateParam(requestConfig.addNewParam(), param);
            }
        }

        if (restMethod.hasRequestBody()) {
            for (RestRepresentation representation : restMethod.getRepresentations(RestRepresentation.Type.REQUEST, null)) {
                generateRepresentation(requestConfig.addNewRepresentation(), representation);
            }
        }

        Map<String, Response> responses = new HashMap<String, Response>();
        if (!isWADL11) {
            responses.put(null, methodConfig.addNewResponse());
        }
        for (RestRepresentation representation : restMethod.getRepresentations()) {
            Response response;
            if (isWADL11) {
                List<Comparable> status = new ArrayList<Comparable>((List<Comparable>) representation.getStatus());
                Collections.sort(status);
                StringBuilder statusStrBuilder = new StringBuilder();
                for (Object o : status) {
                    statusStrBuilder.append(o).append(" ");
                }
                String statusStr = statusStrBuilder.toString();

                if (!responses.containsKey(statusStr)) {
                    response = methodConfig.addNewResponse();
                    response.setStatus(status);
                    responses.put(statusStr, response);
                } else {
                    response = responses.get(statusStr);
                }
            } else {
                response = responses.get(null);
            }

            Representation representationConfig = response.addNewRepresentation();
            generateRepresentation(representationConfig, representation);

            if (!isWADL11 && representation.getType() == RestRepresentation.Type.FAULT) {
                Element resp = (Element) response.getDomNode();
                Element rep = (Element) representationConfig.getDomNode();
                Element fault = resp.getOwnerDocument().createElementNS(Constants.WADL11_NS, "fault");

                NamedNodeMap attributes = rep.getAttributes();
                for (int i = 0; i < attributes.getLength(); i++) {
                    fault.setAttribute(attributes.item(i).getNodeName(), attributes.item(i).getNodeValue());
                }
                NodeList children = rep.getChildNodes();
                for (int i = 0; i < children.getLength(); i++) {
                    fault.appendChild(children.item(i));
                }

                resp.appendChild(fault);
                rep.getParentNode().removeChild(rep);
            }
        }
    }

    private void generateRepresentation(Representation representationConfig, RestRepresentation representation) {
        representationConfig.setMediaType(representation.getMediaType());

        if (StringUtils.hasContent(representation.getId())) {
            representationConfig.setId(representation.getId());
        }

        if (!isWADL11) {
            List<?> status = representation.getStatus();
            if (status != null && status.size() > 0) {
                StringBuilder statusStr = new StringBuilder();
                for (Object s : status) {
                    statusStr.append(s).append(" ");
                }
                ((Element) representationConfig.getDomNode()).setAttribute("status", statusStr.toString().trim());
            }
        }

        if (representation.getElement() != null) {
            representationConfig.setElement(representation.getElement());
        }
    }

}
