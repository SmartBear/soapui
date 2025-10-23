package com.smartbear.swagger;

import com.eviware.soapui.impl.rest.RestMethod;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.RestRequestInterface;
import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.rest.RestServiceFactory;
import com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.ParseOptions;
import com.eviware.soapui.impl.rest.RestRepresentation;


import java.util.Collections;
import java.util.Map;

public class OpenAPI3Importer implements SwaggerImporter {

    private final WsdlProject project;

    public OpenAPI3Importer(WsdlProject project) {
        this.project = project;
    }

    @Override
    public RestService[] importSwagger(String url) {
        return new RestService[]{importOpenApi(url)};
    }

    @Override
    public RestService[] importSwagger(String url, String apiKey) {
        return importSwagger(url);
    }

    @Override
    public RestService[] importSwagger(String url, String apiKey, boolean disableLogger) {
        return importSwagger(url);
    }

    public RestService importOpenApi(String url) {
        ParseOptions options = new ParseOptions();
        options.setResolveFully(true);
        OpenAPI openAPI = new OpenAPIV3Parser().read(url, null, options);

        if (openAPI == null) {
            return null;
        }

        String serviceName = openAPI.getInfo().getTitle();
        RestService service = (RestService) project.addNewInterface(serviceName, RestServiceFactory.REST_TYPE);

        for (Map.Entry<String, PathItem> entry : openAPI.getPaths().entrySet()) {
            String path = entry.getKey();
            PathItem pathItem = entry.getValue();
            RestResource resource = service.addNewResource(path, path);
            addOperations(resource, pathItem);
        }

        return service;
    }

    private void addOperations(RestResource resource, PathItem pathItem) {
        if (pathItem.getGet() != null) {
            addMethod(resource, "GET", pathItem.getGet());
        }
        if (pathItem.getPost() != null) {
            addMethod(resource, "POST", pathItem.getPost());
        }
        if (pathItem.getPut() != null) {
            addMethod(resource, "PUT", pathItem.getPut());
        }
        if (pathItem.getDelete() != null) {
            addMethod(resource, "DELETE", pathItem.getDelete());
        }
        if (pathItem.getOptions() != null) {
            addMethod(resource, "OPTIONS", pathItem.getOptions());
        }
        if (pathItem.getHead() != null) {
            addMethod(resource, "HEAD", pathItem.getHead());
        }
        if (pathItem.getPatch() != null) {
            addMethod(resource, "PATCH", pathItem.getPatch());
        }
    }

    private void addMethod(RestResource resource, String httpMethod, Operation operation) {
        RestMethod method = resource.addNewMethod(operation.getOperationId());
        method.setMethod(RestRequestInterface.HttpMethod.valueOf(httpMethod));

        if (operation.getParameters() != null) {
            for (Parameter parameter : operation.getParameters()) {
                addParameter(method, parameter);
            }
        }

        if (operation.getRequestBody() != null) {
            addRequestBody(method, operation.getRequestBody());
        }

        if (operation.getResponses() != null) {
            addResponses(method, operation.getResponses());
        }
    }

    private void addParameter(RestMethod method, Parameter parameter) {
        String name = parameter.getName();
        method.addProperty(name);
    }

    private void addRequestBody(RestMethod method, RequestBody requestBody) {
        if (requestBody.getContent() != null && !requestBody.getContent().isEmpty()) {
            Map.Entry<String, io.swagger.v3.oas.models.media.MediaType> entry = requestBody.getContent().entrySet().iterator().next();
            String mediaType = entry.getKey();
            io.swagger.v3.oas.models.media.MediaType mediaTypeObject = entry.getValue();
            String example = JsonSchemaGenerator.generate(mediaTypeObject.getSchema());
            RestRequest request = method.addNewRequest("Request 1");
            request.setMediaType(mediaType);
            request.setRequestContent(example);
        }
    }

    private void addResponses(RestMethod method, ApiResponses responses) {
        for (Map.Entry<String, ApiResponse> entry : responses.entrySet()) {
            String statusCode = entry.getKey();
            ApiResponse response = entry.getValue();

            if (response.getContent() != null && !response.getContent().isEmpty()) {
                response.getContent().forEach((mediaType, mediaTypeObject) -> {
                    RestRepresentation representation = method.addNewRepresentation(RestRepresentation.Type.RESPONSE);
                    representation.setMediaType(mediaType);
                    if (!"default".equals(statusCode)) {
                        representation.setStatus(Collections.singletonList(statusCode));
                    }
                });
            } else {
                RestRepresentation representation = method.addNewRepresentation(RestRepresentation.Type.RESPONSE);
                if (!"default".equals(statusCode)) {
                    representation.setStatus(Collections.singletonList(statusCode));
                }
            }
        }
    }
}
