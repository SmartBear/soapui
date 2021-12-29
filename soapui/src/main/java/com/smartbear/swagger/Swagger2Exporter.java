package com.smartbear.swagger;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.rest.*;
import com.eviware.soapui.impl.rest.support.RestParamProperty;
import com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.models.*;
import io.swagger.models.parameters.*;
import io.swagger.util.Json;
import io.swagger.util.Yaml;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class Swagger2Exporter implements SwaggerExporter {

    private final WsdlProject project;

    private Swagger swagger = null;

    public Swagger2Exporter(WsdlProject project) {
        this.project = project;
    }

    @Override
    public String exportToFileSystem(String fileName, String apiVersion, String format, RestService[] services, String basePath) {
        if (!ExportSwaggerAction.shouldOverwriteFileIfExists(fileName, null)) {
            return null;
        }

        swagger = new Swagger();
        swagger.setBasePath(basePath);
        Info info = new Info();
        swagger.setInfo(info);

        info.setVersion(apiVersion);
        info.setTitle(services[0].getName());

        for (RestService service : services) {
            for (RestResource resource : service.getAllResources()) {
                Path p = new Path();

                for (RestMethod restMethod : resource.getRestMethodList()) {
                    Operation operation = new Operation();
                    operation.setOperationId(restMethod.getResource().getName());

                    for (RestRepresentation representation : restMethod.getRepresentations()) {
                        RestRepresentation.Type type = representation.getType();
                        if (type == RestRepresentation.Type.RESPONSE || type == RestRepresentation.Type.FAULT) {
                            List<?> statuses = representation.getStatus();
                            if(statuses != null){
                                for (Object status : statuses) {
                                    operation.addResponse(String.valueOf(status), new Response());
                                }
                            }
                        } else if (type == RestRepresentation.Type.REQUEST && representation.getMediaType() != null) {
                            operation.addConsumes(representation.getMediaType());
                        }
                    }

                    Map<String, Response> responses = operation.getResponses();

                    if (responses != null && responses.isEmpty()) {
                        operation.addResponse("200", new Response());
                    }

                    RestRequestInterface.HttpMethod method = restMethod.getMethod();

                    p.set(method.name().toLowerCase(), operation);

                    addParametersToOperation(restMethod.getOverlayParams(), operation);

                    if (method == RestRequestInterface.HttpMethod.POST || method == RestRequestInterface.HttpMethod.PUT) {
                        BodyParameter param = new BodyParameter();
                        operation.addParameter(param);
                        param.setName("body");
                        param.setDescription("Request body");
                        param.setRequired(true);
                    }
                }
                swagger.path(resource.getFullPath(), p);
            }
        }

        ObjectMapper mapper = format.equals("yaml") ? Yaml.mapper() : Json.mapper();
        try {
            mapper.writeValue(new FileWriter(fileName), swagger);
        } catch (IOException e) {
            SoapUI.logError(e);
        }

        return fileName;
    }

    @Override
    public String getOasVersion() {
        return swagger == null ? null : swagger.getSwagger();
    }

    private void addParametersToOperation(RestParamsPropertyHolder params, Operation op) {

        for (String name: params.getPropertyNames()) {
            RestParamProperty param = params.getProperty(name);
            if (!operationHasParameter(op, name)) {
                AbstractSerializableParameter<?> p = null;

                switch (param.getStyle()) {
                    case HEADER: p = new HeaderParameter(); break;
                    case QUERY: p = new QueryParameter(); break;
                    case TEMPLATE: p = new PathParameter(); break;
                }

                if (p != null) {
                    op.addParameter(p);
                    p.setName(param.getName());
                    p.setRequired(p instanceof PathParameter || param.getRequired());
                    p.setDescription(param.getDescription());

                    // needs to be extended to support all schema types
                    switch (param.getType().getLocalPart()) {
                        case "byte":
                            p.setType("byte");
                            break;
                        case "dateTime":
                            p.setType("Date");
                            break;
                        case "float":
                            p.setType("float");
                            break;
                        case "double":
                            p.setType("double");
                            break;
                        case "long":
                            p.setType("long");
                            break;
                        case "short":
                        case "int":
                        case "integer":
                            p.setType("int");
                            break;
                        case "boolean":
                            p.setType("boolean");
                            break;
                        default:
                            p.setType("string");
                    }
                }
            }
        }
    }

    boolean operationHasParameter(Operation operation, String name) {
        if(operation!= null) {
            for (Parameter parameter : operation.getParameters()) {
                if(parameter.getName().equals(name)) {
                    return true;
                }
            }
        }
        return false;
    }
}
