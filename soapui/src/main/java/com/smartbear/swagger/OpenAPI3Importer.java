package com.smartbear.swagger;

import com.eviware.soapui.impl.rest.RestMethod;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.RestRequestInterface;
import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.rest.RestServiceFactory;
import com.eviware.soapui.impl.rest.mock.RestMockAction;
import com.eviware.soapui.impl.rest.mock.RestMockResponse;
import com.eviware.soapui.impl.rest.mock.RestMockService;
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
        RestMockService mockService = project.addNewRestMockService(serviceName + " Mock");

        for (Map.Entry<String, PathItem> entry : openAPI.getPaths().entrySet()) {
            String path = entry.getKey();
            PathItem pathItem = entry.getValue();
            RestResource resource = service.addNewResource(path, path);
            addOperations(mockService, resource, pathItem);
        }

        return service;
    }

    private void addOperations(RestMockService mockService, RestResource resource, PathItem pathItem) {
        if (pathItem.getGet() != null) {
            addMethod(mockService, resource, "GET", pathItem.getGet());
        }
        if (pathItem.getPost() != null) {
            addMethod(mockService, resource, "POST", pathItem.getPost());
        }
        if (pathItem.getPut() != null) {
            addMethod(mockService, resource, "PUT", pathItem.getPut());
        }
        if (pathItem.getDelete() != null) {
            addMethod(mockService, resource, "DELETE", pathItem.getDelete());
        }
        if (pathItem.getOptions() != null) {
            addMethod(mockService, resource, "OPTIONS", pathItem.getOptions());
        }
        if (pathItem.getHead() != null) {
            addMethod(mockService, resource, "HEAD", pathItem.getHead());
        }
        if (pathItem.getPatch() != null) {
            addMethod(mockService, resource, "PATCH", pathItem.getPatch());
        }
    }

    private void addMethod(RestMockService mockService, RestResource resource, String httpMethod, Operation operation) {
        String operationName = operation.getOperationId();
        if (operationName == null) {
            operationName = httpMethod;
        }
        RestMethod method = resource.addNewMethod(operationName);
        method.setMethod(RestRequestInterface.HttpMethod.valueOf(httpMethod));
        RestMockAction mockAction = mockService.addEmptyMockAction(RestRequestInterface.HttpMethod.valueOf(httpMethod), resource.getPath());

        if (operation.getParameters() != null) {
            for (Parameter parameter : operation.getParameters()) {
                addParameter(method, parameter);
            }
        }

        if (operation.getRequestBody() != null) {
            addRequestBody(method, operation.getRequestBody());
        }

        if (operation.getResponses() != null) {
            addResponses(mockAction, operation.getResponses());
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

    private void addResponses(RestMockAction mockAction, ApiResponses responses) {
        for (Map.Entry<String, ApiResponse> entry : responses.entrySet()) {
            String statusCode = entry.getKey();
            ApiResponse response = entry.getValue();

            if (response.getContent() != null && !response.getContent().isEmpty()) {
                response.getContent().forEach((mediaType, mediaTypeObject) -> {
                    // Add mock responses for each example
                    if (mediaTypeObject.getExamples() != null && !mediaTypeObject.getExamples().isEmpty()) {
                        mediaTypeObject.getExamples().forEach((exampleName, example) -> {
                            RestMockResponse mockResponse = mockAction.addNewMockResponse(statusCode);
                            if (example.getValue() != null) {
                                try {
                                    String content = new com.fasterxml.jackson.databind.ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(example.getValue());
                                    mockResponse.setResponseContent(content);
                                } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                                    // ignore
                                }
                            }
                        });
                    }

                    // Add mock response from schema
                    if (mediaTypeObject.getSchema() != null) {
                        RestMockResponse mockResponse = mockAction.addNewMockResponse(statusCode);
                        String example = JsonSchemaGenerator.generate(mediaTypeObject.getSchema());
                        mockResponse.setResponseContent(example);
                    }
                });
            } else {
                mockAction.addNewMockResponse(statusCode);
            }
        }
    }
}
