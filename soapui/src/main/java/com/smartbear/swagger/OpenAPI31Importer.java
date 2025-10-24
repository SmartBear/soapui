package com.smartbear.swagger;

import com.eviware.soapui.impl.rest.RestMethod;
import com.eviware.soapui.impl.rest.RestRepresentation;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.RestRequestInterface;
import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.rest.RestServiceFactory;
import com.eviware.soapui.impl.rest.support.RestParameter;
import com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder;
import com.eviware.soapui.impl.support.MediaTypeUtils;
import com.eviware.soapui.impl.wsdl.MutableTestPropertyHolder;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.support.PathUtils;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.xml.XmlUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.swagger.inflector.examples.ExampleBuilder;
import io.swagger.inflector.examples.XmlExampleSerializer;
import io.swagger.inflector.examples.models.Example;
import io.swagger.inflector.examples.models.ObjectExample;
import io.swagger.inflector.processors.JsonNodeExampleSerializer;
import io.swagger.util.Json;
import io.swagger.util.Yaml;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.parser.OpenAPIV3Parser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;


public class OpenAPI31Importer implements SwaggerImporter {
    private static final String SAMPLE_GENERATION_FAILED_MESSAGE = "Failed to create the sample. The '%s' media type is incorrect.";

    private static Logger logger = LogManager.getLogger(OpenAPI31Importer.class);

    private static ObjectMapper yamlMapper;
    private static ObjectMapper jsonMapper;
    private final WsdlProject project;
    private final String defaultMediaType;
    private OpenAPI openApi;

    static {
        yamlMapper = Yaml.mapper();
        jsonMapper = Json.mapper();
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(new JsonNodeExampleSerializer());

        yamlMapper.registerModule(simpleModule);
        jsonMapper.registerModule(simpleModule);
    }

    public OpenAPI31Importer(String defaultMediaType) {
        this(null, defaultMediaType);
    }

    public OpenAPI31Importer(WsdlProject project, String defaultMediaType) {
        this.project = project;
        this.defaultMediaType = defaultMediaType;
    }

    public OpenAPI31Importer(WsdlProject project) {
        this(project, "application/json");
    }

    @Override
    public RestService[] importSwagger(String url) {
        return importSwagger(url, null);
    }

    @Override
    public RestService[] importSwagger(String url, String apiKey, boolean disableLogger) {
        List<RestService> result = new ArrayList<>();
        Map<String, Object> context = new HashMap<>();
        context.put("swaggerUrl", url);

        if (url.startsWith("file:")) {
            try {
                url = new File(new URL(url).toURI()).getAbsolutePath();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        logger.info(String.format("Importing swagger %s", url));

        openApi = new OpenAPIV3Parser().read(url);

        if (openApi == null) {
            return new RestService[]{null};
        }

        if (openApi.getPaths() == null) {
            return new RestService[]{null};
        }
        RestService restService = createRestService(openApi, url);
        openApi.getPaths().forEach((key, value) -> importPath(restService, key, value, context));

        result.add(restService);
        ensureEndpoint(restService, url);

        return result.toArray(new RestService[result.size()]);
    }

    @Override
    public RestService[] importSwagger(String url, String apiKey) {
        return importSwagger(url, apiKey, false);
    }

    private void ensureEndpoint(RestService restService, String url) {
        if (restService != null && restService.getEndpoints().length == 0) {
            int ix = url.indexOf("://");
            if (ix > 0) {
                ix = url.indexOf("/", ix + 3);

                url = ix == -1 ? url : url.substring(0, ix);
                restService.addEndpoint(url);
            }
        }
    }

    private RestResource importPath(RestService restService, String path, PathItem resource, Map<String, Object> context) {
        if (restService == null) {
            return null;
        }
        RestResource restResource = restService.addNewResource(path, path);

        List<Parameter> parameters = resource.getParameters();
        if (parameters != null) {
            parameters.forEach(parameter -> {
                resource.getGet().getParameters().add(parameter);
                resource.getPost().getParameters().add(parameter);
                resource.getPut().getParameters().add(parameter);
                resource.getDelete().getParameters().add(parameter);
                resource.getPatch().getParameters().add(parameter);
                resource.getOptions().getParameters().add(parameter);
            });
        }

        if (resource.getGet() != null) {
            addOperation(restResource, resource.getGet(), RestRequestInterface.HttpMethod.GET);
        }

        if (resource.getPost() != null) {
            addOperation(restResource, resource.getPost(), RestRequestInterface.HttpMethod.POST);
        }

        if (resource.getPut() != null) {
            addOperation(restResource, resource.getPut(), RestRequestInterface.HttpMethod.PUT);
        }

        if (resource.getDelete() != null) {
            addOperation(restResource, resource.getDelete(), RestRequestInterface.HttpMethod.DELETE);
        }

        if (resource.getPatch() != null) {
            addOperation(restResource, resource.getPatch(), RestRequestInterface.HttpMethod.PATCH);
        }

        if (resource.getOptions() != null) {
            addOperation(restResource, resource.getOptions(), RestRequestInterface.HttpMethod.OPTIONS);
        }

        return restResource;
    }

    private void addOperation(RestResource resource, Operation operation, RestRequestInterface.HttpMethod httpMethod) {
        String operationName = operation.getOperationId();

        if (StringUtils.isNullOrEmpty(operationName)) {
            operationName = httpMethod.toString();
        }

        RestMethod method = resource.addNewMethod(operationName);
        method.setMethod(httpMethod);
        String description = StringUtils.emptyIfNull(operation.getDescription()) +
                System.getProperty("line.separator") + StringUtils.emptyIfNull(operation.getSummary());
        method.setDescription(description);

        List<Parameter> parameters = operation.getParameters();
        if (parameters != null) {
            parameters.forEach(parameter -> {
                addParameter(parameter, method);
            });
        }

        if (operation.getRequestBody() != null) {
            addBodyParameter(operation.getRequestBody(), operation, method);
        }

        if (method.getRequestList().isEmpty()) {
            attachDefaultPayload(method);
        }

        if (operation.getResponses() != null) {
            operation.getResponses().forEach((responseCode, response) -> addResponse(responseCode, response, operation, method));
        }
    }

    private void addParameter(Parameter parameter, MutableTestPropertyHolder propertyHolder) {
        String parameterName = parameter.getName();
        if (StringUtils.isNullOrEmpty(parameterName) && StringUtils.hasContent(parameter.get$ref())) {
            parameterName = parameter.get$ref();
        }

        if (StringUtils.isNullOrEmpty(parameterName)) {
            logger.warn("Can not import property without name or ref [" + parameter.toString() + "]");
        } else {
            RestParameter restParameter = (RestParameter) propertyHolder.addProperty(parameterName);

            try {
                restParameter.setStyle(getParameterStyle(parameter));
            } catch (IllegalArgumentException e) {
                logger.error(e.getMessage(), e);
            }

            restParameter.setDescription(parameter.getDescription());
            restParameter.setRequired(parameter.getRequired());

            if (parameter.getSchema() != null && parameter.getSchema().getDefault() != null) {
                String defaultValue = parameter.getSchema().getDefault().toString();
                restParameter.setDefaultValue(defaultValue);
                restParameter.setValue(defaultValue);
            }
        }
    }

    private void addBodyParameter(RequestBody requestBody, Operation operation, RestMethod method) {
        if (requestBody.getContent() != null) {
            requestBody.getContent().forEach((mediaType, mediaTypeObject) -> {
                RestRepresentation representation = method.addNewRepresentation(RestRepresentation.Type.REQUEST);
                representation.setMediaType(mediaType);
                RestRequest request = method.addNewRequest("Request " + (method.getRequestList().size() + 1));
                request.setMediaType(mediaType);
                if (mediaType.toLowerCase().contains("json")) {
                    request.setRequestContent("{}");
                } else if (mediaType.toLowerCase().contains("xml")) {
                    request.setRequestContent("<root/>");
                }
            });
        }
    }


    private RestParamsPropertyHolder.ParameterStyle getParameterStyle(Parameter parameter) {
        String parameterLocation = parameter.getIn() == null ? "query" : parameter.getIn();
        if (parameter.getIn().equals("body")) {
            return null;
        }

        if (parameterLocation.equals("path")) {
            parameterLocation = "template";
        } else if (parameterLocation.equals("formData")) {
            parameterLocation = "query";
        }

        return RestParamsPropertyHolder.ParameterStyle.valueOf(parameterLocation.toUpperCase());
    }

    private void attachDefaultPayload(RestMethod method) {
        RestRequest request = method.addNewRequest("Request " + (method.getRequestList().size() + 1));
        request.setMediaType(defaultMediaType);
        if (defaultMediaType.toLowerCase().contains("json")) {
            request.setRequestContent("{}");
        } else if (defaultMediaType.toLowerCase().contains("xml")) {
            request.setRequestContent("<root/>");
        }
    }


    private void addResponse(String responseCode, ApiResponse response, Operation operation, RestMethod method) {
        if (response.getContent() != null) {
            response.getContent().forEach((mediaType, mediaTypeObject) -> {
                RestRepresentation representation = method.addNewRepresentation(RestRepresentation.Type.RESPONSE);
                representation.setMediaType(mediaType);

                List<String> statusList = new ArrayList<>();
                if (!responseCode.equals("default")) {
                    statusList.add(responseCode);
                }
                representation.setStatus(statusList);
            });
        } else {
            RestRepresentation representation = method.addNewRepresentation(RestRepresentation.Type.RESPONSE);

            List<String> statusList = new ArrayList<>();
            if (!responseCode.equals("default")) {
                statusList.add(responseCode);
            }
            representation.setStatus(statusList);
            representation.setMediaType(defaultMediaType);
        }
    }

    private String serializeExample(String mediaType, Example output) {
        String sampleValue = null;
        ObjectMapper mapper = null;

        String subtype = "";
        try {
            subtype = MediaTypeUtils.getSubtype(mediaType);
            String suffix = MediaTypeUtils.getSuffix(mediaType);
            if (StringUtils.hasContent(suffix)) {
                subtype = suffix;
            }
        } catch (IllegalArgumentException e) {
            logger.warn(String.format(SAMPLE_GENERATION_FAILED_MESSAGE, mediaType));
        }

        switch (subtype.toLowerCase()) {
            case "xml":
                sampleValue = XmlUtils.prettyPrintXml(new XmlExampleSerializer().serialize(output));
                if (!XmlUtils.seemsToBeXml(sampleValue)) {
                    return "";
                }
                break;
            case "yaml":
                mapper = yamlMapper;
                break;
            case "json":
                mapper = jsonMapper;
                break;
            case "plain":
                if (!(output instanceof ObjectExample)) {
                    sampleValue = output.asString();
                }
                break;
        }

        if (mapper != null) {
            try {
                sampleValue = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(output);
            } catch (JsonProcessingException e) {
                logger.error(e.getMessage(), e);
            }
        }
        return sampleValue;
    }

    private RestService createRestService(OpenAPI openApi, String url) {
        Info swaggerInfo = openApi.getInfo();
        String name = swaggerInfo != null && swaggerInfo.getTitle() != null ? swaggerInfo.getTitle() : null;
        if (name == null) {
            if (url.toLowerCase().startsWith("http://") || url.toLowerCase().startsWith("https://")) {
                try {
                    name = new URL(url).getHost();
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                }

            } else {
                int ix = url.lastIndexOf('/');
                name = ix == -1 || ix == url.length() - 1 ? url : url.substring(ix + 1);
            }
        }

        RestService restService = (RestService) project.addNewInterface(name, RestServiceFactory.REST_TYPE);

        String expandedUrl = PathUtils.expandPath(url, project);
        if (new File(expandedUrl).exists()) {
            try {
                expandedUrl = new File(expandedUrl).toURI().toURL().toString();
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }

        if (swaggerInfo != null) {
            restService.setDescription(swaggerInfo.getDescription());
        }

        if (openApi.getServers() != null) {
            for (Server server : openApi.getServers()) {
                restService.addEndpoint(server.getUrl());
            }
        }

        return restService;
    }

    public WsdlProject getProject() {
        return project;
    }

    public OpenAPI getOpenApi() {
        return openApi;
    }
}
