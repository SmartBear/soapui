package com.smartbear.integrations.swagger;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.rest.RestMethod;
import com.eviware.soapui.impl.rest.RestRepresentation;
import com.eviware.soapui.impl.rest.RestRequestInterface;
import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.rest.RestServiceFactory;
import com.eviware.soapui.impl.rest.support.RestParameter;
import com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder.ParameterStyle;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.smartbear.integrations.swaggerhub.importers.AbstractSwaggerImporter;
import com.smartbear.swagger4j.*;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class AbstractSwagger1XImporter extends AbstractSwaggerImporter {

    protected final WsdlProject project;
    protected final String defaultMediaType;

    public AbstractSwagger1XImporter(WsdlProject project, String defaultMediaType) {
        this.project = project;
        this.defaultMediaType = defaultMediaType;
    }

    public AbstractSwagger1XImporter(WsdlProject project) {
        this(project, "application/json");
    }

    public void ensureEndpoint(RestService restService, String url) {
        if (restService.getEndpoints().length == 0) {

            int ix = url.indexOf("://");
            if (ix > 0) {
                ix = url.indexOf("/", ix + 3);

                url = ix == -1 ? url : url.substring(0, ix);
                restService.addEndpoint(url);
            }
        }
    }

    /**
     * Imports all swagger api declarations in the specified JSON document into a RestService
     * @url the url of the JSON document defining swagger APIs to import
     * @return the created RestService
     */

    public RestService importApiDeclaration(ApiDeclaration apiDeclaration, String name) {
        // create the RestService
        RestService restService = createRestService(apiDeclaration.getBasePath(), name);

        // loop apis in document
        for (Api api : apiDeclaration.getApis()) {
            // add a resource for this api
            RestResource resource = restService.addNewResource(api.getPath(), api.getPath());
            resource.setDescription(api.getDescription());

            // check for format template parameter - add at resource level so all methods will inherit
            if (api.getPath().contains("{format}")) {
                RestParameter p = resource.getParams().addProperty("format");
                p.setStyle(ParameterStyle.TEMPLATE);
                p.setRequired(true);
                p.setOptions( new String[]{ "json" });
                p.setDefaultValue("json");
            }

            // loop all operations - import as methods
            for (Operation operation : api.getOperations()) {

                String methodName = operation.getNickName();
                int cnt = 0;
                while (resource.getRestMethodByName(methodName) != null) {
                    methodName = operation.getNickName() + " " + (++cnt);
                }

                RestMethod method = resource.addNewMethod(methodName);
                method.setMethod(RestRequestInterface.HttpMethod.valueOf(operation.getMethod().name().toUpperCase()));
                method.setDescription(operation.getSummary());

                // loop parameters and add accordingly
                for (Parameter parameter : operation.getParameters()) {

                    // ignore body parameters
                    if (parameter.getParamType() != Parameter.ParamType.body) {

                        String paramType = parameter.getParamType().name();
                        if (paramType.equals("path"))
                            paramType = "template";
                        else if (paramType.equals("form"))
                            paramType = "query";

                        // path parameters are added at resource level
                        RestParameter p = paramType.equals("template") ? method.getResource().getParams().addProperty(parameter.getName())
                                : method.getParams().addProperty(parameter.getName());

                        try {
                            p.setStyle(ParameterStyle.valueOf(paramType.toUpperCase()));
                        }
                        catch (IllegalArgumentException e) {
                            SoapUI.logError(e);
                        }

                        p.setDescription(parameter.getDescription());
                        p.setRequired(parameter.isRequired());
                    }
                }

                List<ResponseMessage> responseMessages = operation.getResponseMessages();
                if(responseMessages!= null) {
                    for (ResponseMessage response : responseMessages) {
                        Collection<String> produces = operation.getProduces();
                        if (produces == null || produces.isEmpty()) {
                            RestRepresentation representation = method.addNewRepresentation(RestRepresentation.Type.RESPONSE);
                            ArrayList<Object> codes = new ArrayList<>();
                            codes.add(response.getCode());
                            representation.setStatus(codes);
                            representation.setMediaType(defaultMediaType);
                        } else {
                            for (String produce : produces) {
                                RestRepresentation representation = method.addNewRepresentation(RestRepresentation.Type.RESPONSE);
                                representation.setMediaType(produce);
                                List codes = new ArrayList();
                                codes.add(response.getCode());
                                representation.setStatus(codes);
                            }
                        }
                    }
                }

                // add a default request for the generated method
                method.addNewRequest("Request 1");
            }
        }
        return restService;
    }

    private RestService createRestService(String path, String name) {
        RestService restService = (RestService) project.addNewInterface(name, RestServiceFactory.REST_TYPE);

        if (path != null) {
            try {
                if (path.startsWith("/")) {
                    restService.setBasePath(path);
                } else {
                    URL url = new URL(path);
                    int pathPos = path.length() - url.getPath().length();

                    restService.setBasePath(path.substring(pathPos));
                    restService.addEndpoint(path.substring(0, pathPos));
                }
            }
            catch (Exception e) {
                SoapUI.logError(e);
            }
        }

        return restService;
    }
}
