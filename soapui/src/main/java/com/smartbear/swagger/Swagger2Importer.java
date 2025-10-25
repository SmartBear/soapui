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
import io.swagger.models.ModelImpl;
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
import io.swagger.models.properties.RefProperty;
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
        swagger.getPaths().forEach((key, value) -> importPath(restService, key, value, context));

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

    private RestResource importPath(RestService restService, String path, Path resource, Map<String, Object> context) {
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

        project.getRestMockServiceList().forEach(mockService -> mockService.addNewMockAction(method));

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
            responses.forEach((responseCode, response) -> addResponse(responseCode, response, operation, method));
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
                method.addNewRepresentation(RestRepresentation.Type.REQUEST).setMediaType(mediaType);

                Model bodyParameterModel = bodyParameter.getSchema();
                if (bodyParameterModel != null) {
                    // From example property
                    if (bodyParameterModel.getExample() != null) {
                        ObjectExample example = new ObjectExample();
                        example.setExample(bodyParameterModel.getExample());
                        String content = serializeExample(mediaType, example);
                        if (StringUtils.hasContent(content)) {
                            RestRequest request = method.addNewRequest("Request " + (method.getRequestList().size() + 1));
                            request.setMediaType(mediaType);
                            request.setRequestContent(content);
                        }
                    } else {
                        // From schema
                        String content = mediaType.toLowerCase().contains("json") ? "{}" : "<root/>";
                        RestRequest request = method.addNewRequest("Request " + (method.getRequestList().size() + 1));
                        request.setMediaType(mediaType);
                        request.setRequestContent(content);
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

        if (method.getRequestList().isEmpty()) {
            if (consumes != null && !consumes.isEmpty()) {
                for (String mediaType : consumes) {
                    RestRequest request = method.addNewRequest("Request " + (method.getRequestList().size() + 1));
                    request.setMediaType(mediaType);
                    if (mediaType.toLowerCase().contains("json")) {
                        request.setRequestContent("{}");
                    } else if (mediaType.toLowerCase().contains("xml")) {
                        request.setRequestContent("<root/>");
                    }
                }
            } else if (method.getRequestList().size() == 0) {
                method.addNewRequest("Request " + (method.getRequestList().size() + 1));
            }
        }
    }

    private void addResponse(String responseCode, Response response, Operation operation, RestMethod method) {
        Map<String, Object> responseExamples = response.getExamples();

        if (responseExamples != null && !responseExamples.isEmpty()) {
            responseExamples.forEach((mediaType, example) -> {
                ObjectExample objectExample = new ObjectExample();
                objectExample.setExample(example);
                attachResponse(responseCode, method, mediaType, objectExample, response);
            });
        } else {
            List<String> produces = operation.getProduces();
            if (produces == null || produces.isEmpty()) {
                produces = swagger.getProduces();
            }

            if (produces == null || produces.isEmpty()) {
                attachResponse(responseCode, method, defaultMediaType, null, response);
            } else {
                produces.forEach(mediaType -> attachResponse(responseCode, method, mediaType, null, response));
            }
        }
    }

    private void attachResponse(String responseCode, RestMethod method, String mediaType, ObjectExample example, Response response) {
        RestRepresentation representation = method.addNewRepresentation(RestRepresentation.Type.RESPONSE);
        representation.setMediaType(mediaType);
        List<String> statusList = new ArrayList<>();
        if (!responseCode.equals("default")) {
            statusList.add(responseCode);
        }
        representation.setStatus(statusList);

        String content = "";

        if (example != null) {
            content = serializeExample(mediaType, example);
        } else if (response.getSchema() != null) {
            if (mediaType.toLowerCase().contains("xml")) {
                content = createSampleXmlRequestFromProperty(response.getSchema());
            } else {
                content = createSampleRequestFromProperty(response.getSchema());
            }
        }

        if (StringUtils.hasContent(content)) {
            final String finalContent = content;
            project.getRestMockServiceList().forEach(mockService -> {
                RestMockAction mockAction = (RestMockAction) mockService.getMockOperationByName(method.getName());
                if (mockAction != null) {
                    RestMockResponse mockResponse = mockAction.addNewMockResponse("Response from example");
                    mockResponse.setResponseContent(finalContent);
                    mockResponse.setMediaType(mediaType);
                }
            });
        }
    }

    private String createSampleRequest(Model schema) {
        if (schema instanceof RefModel) {
            schema = swagger.getDefinitions().get(((RefModel) schema).getSimpleRef());
        }

        if (schema instanceof ComposedModel) {
            ComposedModel composedModel = (ComposedModel) schema;
            List<Model> models = composedModel.getAllOf();
            if (models != null && !models.isEmpty()) {
                schema = models.get(0);
            }
        }

        if (schema.getProperties() != null) {
            StringBuilder sb = new StringBuilder("{\n");
            schema.getProperties().forEach((key, value) -> {
                sb.append("  \"").append(key).append("\": ");
                sb.append(createSampleRequestFromProperty(value));
                sb.append(",\n");
            });
            if (sb.length() > 2) {
                sb.delete(sb.length() - 2, sb.length());
            }
            sb.append("\n}");
            return sb.toString();
        }

        return "{}";
    }

    private String createSampleRequestFromProperty(io.swagger.models.properties.Property property) {
        if (property instanceof RefProperty) {
            return createSampleRequest(swagger.getDefinitions().get(((RefProperty) property).getSimpleRef()));
        } else if (property instanceof ObjectProperty) {
            return createSampleRequest(swagger.getDefinitions().get(((ObjectProperty) property).getType()));
        } else if (property != null) {
            return "\"" + property.getType() + "\"";
        } else {
            return "\"{}\"";
        }
    }

    private String createSampleXmlRequestFromProperty(io.swagger.models.properties.Property property) {
        if (property instanceof RefProperty) {
            return createSampleXmlRequest(swagger.getDefinitions().get(((RefProperty) property).getSimpleRef()));
        } else if (property instanceof ObjectProperty) {
            return createSampleXmlRequest(swagger.getDefinitions().get(((ObjectProperty) property).getType()));
        } else if (property != null){
            return "<" + property.getType() + "/>";
        } else {
            return "<root/>";
        }
    }

    private String createSampleXmlRequest(Model schema) {
        if (schema instanceof RefModel) {
            schema = swagger.getDefinitions().get(((RefModel) schema).getSimpleRef());
        }

        if (schema instanceof ComposedModel) {
            ComposedModel composedModel = (ComposedModel) schema;
            List<Model> models = composedModel.getAllOf();
            if (models != null && !models.isEmpty()) {
                schema = models.get(0);
            }
        }

        if (schema.getProperties() != null) {
            StringBuilder sb = new StringBuilder("<" + schema.getTitle() + ">\n");
            schema.getProperties().forEach((key, value) -> {
                sb.append("  <").append(key).append(">");
                if (value instanceof ObjectProperty) {
                    sb.append("\n").append(createSampleXmlRequest(swagger.getDefinitions().get(((ObjectProperty) value).getType()))).append("  ");
                } else {
                    sb.append(value.getType());
                }
                sb.append("</").append(key).append(">\n");
            });
            sb.append("</").append(schema.getTitle()).append(">");
            return sb.toString();
        }

        return "<root/>";
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
                Object valueToSerialize = output;
                if (output instanceof ObjectExample) {
                    valueToSerialize = ((ObjectExample) output).getExample();
                }
                sampleValue = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(valueToSerialize);
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
