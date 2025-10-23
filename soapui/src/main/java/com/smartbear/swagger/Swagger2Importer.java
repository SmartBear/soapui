package com.smartbear.swagger;

import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.rest.RestMethod;
import com.eviware.soapui.impl.rest.RestRepresentation;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.RestRequestInterface;
import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.impl.rest.RestServiceFactory;
import com.eviware.soapui.impl.rest.mock.RestMockAction;
import com.eviware.soapui.impl.rest.mock.RestMockResponse;
import com.eviware.soapui.impl.rest.mock.RestMockService;
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
import io.swagger.models.ComposedModel;
import io.swagger.models.Info;
import io.swagger.models.Model;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.RefModel;
import io.swagger.models.Response;
import io.swagger.models.Scheme;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.AbstractSerializableParameter;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.PathParameter;
import io.swagger.models.parameters.RefParameter;
import io.swagger.models.properties.ObjectProperty;
import io.swagger.util.Json;
import io.swagger.util.Yaml;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Swagger2Importer implements SwaggerImporter {
    /*OT*/ private static final String SAMPLE_GENERATION_FAILED_MESSAGE = "Failed to create the sample. The '%s' media type is incorrect.";

    private static Logger logger = LogManager.getLogger(Swagger2Importer.class);

    private static ObjectMapper yamlMapper;
    private static ObjectMapper jsonMapper;
    private final WsdlProject project;
    private final String defaultMediaType;
    private Swagger swagger;

    static {
        yamlMapper = Yaml.mapper();
        jsonMapper = Json.mapper();
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(new JsonNodeExampleSerializer());

        yamlMapper.registerModule(simpleModule);
        jsonMapper.registerModule(simpleModule);
    }

    public Swagger2Importer(String defaultMediaType) {
        this(null, defaultMediaType);
    }

    public Swagger2Importer(WsdlProject project, String defaultMediaType) {
        this.project = project;
        this.defaultMediaType = defaultMediaType;
    }

    public Swagger2Importer(WsdlProject project) {
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

        swagger = SwaggerUtils.getSwagger(url, null, true, disableLogger);

        if (swagger == null) {
            return new RestService[]{null};
        }

        if (swagger.getPaths() == null) {
            return new RestService[]{null};
        }
        RestService restService = createRestService(swagger, url);
        RestMockService mockService = project.addNewRestMockService(restService.getName() + " Mock");
        swagger.getPaths().forEach((key, value) -> importPath(restService, mockService, key, value, context));

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

    private RestResource importPath(RestService restService, RestMockService mockService, String path, Path resource, Map<String, Object> context) {
        if (restService == null) {
            return null;
        }
        RestResource restResource = restService.addNewResource(path, path);

        List<Parameter> parameters = resource.getParameters();
        if (parameters != null) {
            parameters.forEach(parameter -> {
                if (parameter instanceof BodyParameter) {
                    //move body parameters to operation level
                    resource.getOperations().forEach(operation -> {
                        boolean matched = false;
                        for (Parameter existingParameter : operation.getParameters()) {
                            if (parameter.getIn() != null && parameter.getIn().equals(existingParameter.getIn()) &&
                                    parameter.getName().equals(existingParameter.getName())) {
                                matched = true;
                                break;
                            }
                        }
                        if (!matched) {
                            operation.getParameters().add(parameter);
                        }
                    });
                } else {
                    addParameter(parameter, restResource);
                }

            });
        }

        if (resource.getGet() != null) {
            addOperation(mockService, restResource, resource.getGet(), RestRequestInterface.HttpMethod.GET);
        }

        if (resource.getPost() != null) {
            addOperation(mockService, restResource, resource.getPost(), RestRequestInterface.HttpMethod.POST);
        }

        if (resource.getPut() != null) {
            addOperation(mockService, restResource, resource.getPut(), RestRequestInterface.HttpMethod.PUT);
        }

        if (resource.getDelete() != null) {
            addOperation(mockService, restResource, resource.getDelete(), RestRequestInterface.HttpMethod.DELETE);
        }

        if (resource.getPatch() != null) {
            addOperation(mockService, restResource, resource.getPatch(), RestRequestInterface.HttpMethod.PATCH);
        }

        if (resource.getOptions() != null) {
            addOperation(mockService, restResource, resource.getOptions(), RestRequestInterface.HttpMethod.OPTIONS);
        }

        return restResource;
    }

    private void addOperation(RestMockService mockService, RestResource resource, Operation operation, RestRequestInterface.HttpMethod httpMethod) {
        String operationName = operation.getOperationId();

        if (StringUtils.isNullOrEmpty(operationName)) {
            operationName = httpMethod.toString();
        }

        RestMethod method = resource.addNewMethod(operationName);
        method.setMethod(httpMethod);
        RestMockAction mockAction = mockService.addEmptyMockAction(httpMethod, resource.getPath());

        String description = StringUtils.emptyIfNull(operation.getDescription()) +
                System.getProperty("line.separator") + StringUtils.emptyIfNull(operation.getSummary());
        method.setDescription(description);

        List<Parameter> parameters = operation.getParameters();
        if (parameters != null) {
            parameters.forEach(parameter -> {
                if (parameter instanceof BodyParameter) {
                    addBodyParameter((BodyParameter) parameter, operation, method);
                } else if (parameter instanceof PathParameter) {
                    //add path parameters on resource level
                    addParameter(parameter, method.getResource());
                } else {
                    addParameter(parameter, method);
                }

            });
        }

        if (method.getRequestList().isEmpty()) {
            attachDefaultPayload(operation, method);
        }

        Map<String, Response> responses = operation.getResponses();
        if (responses != null) {
            responses.forEach((responseCode, response) -> addResponse(responseCode, response, operation, mockAction));
        }

        if (method.getRepresentations(RestRepresentation.Type.RESPONSE, null) != null
                && method.getRepresentations(RestRepresentation.Type.RESPONSE, null).length == 0) {
            List<String> produces = operation.getProduces();
            if (produces != null) {
                produces.forEach(mediaType ->
                        method.addNewRepresentation(RestRepresentation.Type.RESPONSE).setMediaType(mediaType));
            }
        }

        List<String> consumes = operation.getConsumes();
        if (consumes != null) {
            consumes.forEach(mediaType ->
                    method.addNewRepresentation(RestRepresentation.Type.REQUEST).setMediaType(mediaType));
        }
    }

    private void addParameter(Parameter parameter, MutableTestPropertyHolder propertyHolder) {
        String parameterName = parameter.getName();
        if (StringUtils.isNullOrEmpty(parameterName) && parameter instanceof RefParameter) {
            parameterName = ((RefParameter) parameter).get$ref();
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

            if ((parameter instanceof AbstractSerializableParameter) &&
                    ((AbstractSerializableParameter) parameter).getDefaultValue() != null) {
                String defaultValue = ((AbstractSerializableParameter) parameter).getDefaultValue().toString();
                restParameter.setDefaultValue(defaultValue);
                restParameter.setValue(defaultValue);
            }
        }
    }

    private void addBodyParameter(BodyParameter bodyParameter, Operation operation, RestMethod method) {

        List<String> consumes = operation.getConsumes();
        if (consumes == null || consumes.isEmpty()) {
            consumes = swagger.getConsumes();
        }

        if (consumes != null) {
            consumes.forEach(mediaType -> {
                RestRepresentation representation = method.addNewRepresentation(RestRepresentation.Type.REQUEST);
                representation.setMediaType(mediaType);

                RestRequest request = method.addNewRequest("Request " + (method.getRequestList().size() + 1));
                request.setMediaType(mediaType);
                Model bodyParameterModel = bodyParameter.getSchema();
                if (bodyParameterModel != null) {
                    ObjectProperty objectProperty = new ObjectProperty(bodyParameterModel.getProperties());
                    if (bodyParameterModel instanceof RefModel) {
                        RefModel refModel = (RefModel) bodyParameterModel;
                        Model modelDefinition = swagger.getDefinitions().get(refModel.getSimpleRef());
                        if (modelDefinition instanceof ComposedModel) {
                            objectProperty = null;
                        } else if (modelDefinition != null) {
                            objectProperty = new ObjectProperty(modelDefinition.getProperties());
                            objectProperty.name(refModel.getSimpleRef());
                        }
                    }
                    Example output = objectProperty != null ? ExampleBuilder.fromProperty(objectProperty, swagger.getDefinitions()) :
                            ExampleBuilder.fromModel(null, bodyParameterModel, swagger.getDefinitions(), new HashSet<String>());
                    if (output != null) {
                        request.setRequestContent(serializeExample(mediaType, output));
                    }
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

    private void attachDefaultPayload(Operation operation, RestMethod method) {
        List<String> consumes = operation.getConsumes();
        if (consumes == null) {
            consumes = swagger.getConsumes();
        }

        if (consumes != null && !consumes.isEmpty()) {
            for (String mediaType : consumes) {
                RestRequest request = method.addNewRequest("Request 1");
                request.setMediaType(mediaType);
                if (mediaType.toLowerCase().contains("json")) {
                    request.setRequestContent("{}");
                } else if (mediaType.toLowerCase().contains("xml")) {
                    request.setRequestContent("<root/>");
                }
            }
        } else {
            method.addNewRequest("Request 1");
        }
    }

    private void addResponse(String responseCode, Response response, Operation operation, RestMockAction mockAction) {
        List<String> produces = operation.getProduces();
        if (produces == null || produces.isEmpty()) {
            operation.setProduces(swagger.getProduces());
            produces = operation.getProduces();
        }

        if (produces == null || produces.isEmpty()) {
            addMockResponse(responseCode, response, mockAction, defaultMediaType);
        } else {
            produces.forEach(mediaType -> addMockResponse(responseCode, response, mockAction, mediaType));
        }
    }

    private void addMockResponse(String responseCode, Response response, RestMockAction mockAction, String mediaType) {
        // Add mock responses for each example
        Map<String, Object> responseExamples = response.getExamples();
        if (responseExamples != null && !responseExamples.isEmpty()) {
            responseExamples.forEach((exampleMediaType, exampleObject) -> {
                RestMockResponse mockResponse = mockAction.addNewMockResponse(responseCode);
                try {
                    String content = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(exampleObject);
                    mockResponse.setResponseContent(content);
                } catch (JsonProcessingException e) {
                    logger.error("Failed to serialize example", e);
                }
            });
        }

        // Add mock response from schema
        if (response.getSchema() != null) {
            RestMockResponse mockResponse = mockAction.addNewMockResponse(responseCode);
            Example example = ExampleBuilder.fromProperty(response.getSchema(), swagger.getDefinitions());
            if (example != null) {
                String content = serializeExample(mediaType, example);
                mockResponse.setResponseContent(content);
            }
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

    private RestService createRestService(Swagger swagger, String url) {
        Info swaggerInfo = swagger.getInfo();
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

        if (!StringUtils.isNullOrEmpty(swagger.getHost())) {
            List<Scheme> schemes = swagger.getSchemes();
            if (schemes != null) {
                schemes.forEach(currentSchema -> {
                    String scheme = currentSchema.toValue().toLowerCase();
                    if (scheme.startsWith("http")) {
                        restService.addEndpoint(scheme + "://" + swagger.getHost());
                    }
                });
            }

            if (restService.getEndpoints().length == 0) {
                if (url.toLowerCase().startsWith("http") && url.indexOf(':') > 0) {
                    restService.addEndpoint(url.substring(0, url.indexOf(':')).toLowerCase() + "://" + swagger.getHost());
                } else {
                    restService.addEndpoint("http://" + swagger.getHost());
                }
            }
        }

        if (swagger.getBasePath() != null) {
            restService.setBasePath(swagger.getBasePath());
            if (restService.getBasePath().endsWith("/")) {
                restService.setBasePath(restService.getBasePath().substring(0, restService.getBasePath().length() - 1));
            }
        }

        return restService;
    }

    public WsdlProject getProject() {
        return project;
    }

    public Swagger getSwagger() {
        return swagger;
    }
}
