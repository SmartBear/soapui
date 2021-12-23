package com.smartbear.integrations.swaggerhub.utils;

import com.eviware.soapui.impl.rest.RestMethod;
import com.eviware.soapui.impl.rest.RestRepresentation;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.RestRequestInterface;
import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.impl.rest.mock.RestMockAction;
import com.eviware.soapui.impl.rest.mock.RestMockResponse;
import com.eviware.soapui.impl.rest.support.RestParamProperty;
import com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder;
import com.eviware.soapui.model.mock.MockOperation;
import com.eviware.soapui.support.JsonUtil;
import com.eviware.soapui.support.ModelItemNamer;
import com.eviware.soapui.support.xml.XmlUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.util.Json;
import io.swagger.util.Yaml;
import io.swagger.v3.oas.integration.IntegrationObjectMapperFactory;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import javax.annotation.Nullable;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.xmlbeans.SchemaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.eviware.soapui.impl.rest.RestRequestInterface.HttpMethod.DELETE;
import static com.eviware.soapui.impl.rest.RestRequestInterface.HttpMethod.HEAD;
import static com.eviware.soapui.impl.rest.RestRequestInterface.HttpMethod.OPTIONS;
import static com.eviware.soapui.impl.rest.RestRequestInterface.HttpMethod.PATCH;
import static com.eviware.soapui.impl.rest.RestRequestInterface.HttpMethod.POST;
import static com.eviware.soapui.impl.rest.RestRequestInterface.HttpMethod.PUT;
import static com.eviware.soapui.impl.rest.RestRequestInterface.HttpMethod.TRACE;
import static com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder.ParameterStyle.HEADER;
import static com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder.ParameterStyle.QUERY;
import static com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder.ParameterStyle.TEMPLATE;
import static com.eviware.soapui.impl.support.HttpUtils.canHavePayload;

public class OpenAPIUtils {

    private static final Logger LOG = LoggerFactory.getLogger(OpenAPI3Importer.class);

    public static void copyParametersToPath(PathItem pathItem, RestParamsPropertyHolder propertyHolder) {
        for (String propertyName : propertyHolder.getPropertyNames()) {
            RestParamProperty paramProperty = propertyHolder.getProperty(propertyName);
            Parameter parameter = new Parameter();
            parameter.setName(propertyName);
            parameter.setIn(OpenAPIUtils.convertReadyApiParameterToOpenApiParameterType(paramProperty));
            pathItem.addParametersItem(parameter);
        }
    }

    private static String replaceUnescaped(String url) {
        return url.replaceAll("([\\/\\\\\\.\\[\\{\\(\\*\\+\\?\\^\\$\\|??])", "\\\\$1");
    }

    public static String getOpenApiJson(OpenAPI openAPI) {
        StringWriter stringWriter = new StringWriter();
        try {
            IntegrationObjectMapperFactory.createJson().writeValue(stringWriter, openAPI);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return stringWriter.toString();
    }

    private static void updateResource(@Nonnull RestResource restResource, PathItem pathItem) {
        Set<RestRequestInterface.HttpMethod> resourceExistingMethodsSet = extractMethodsSet(pathItem);

        for (RestMethod restMethod : restResource.getRestMethodList()) {
            RestRequestInterface.HttpMethod httpMethod = restMethod.getMethod();
            if (!resourceExistingMethodsSet.contains(httpMethod)) {
                addMethodToPath(pathItem, restMethod);
            }
            resourceExistingMethodsSet.remove(httpMethod);
        }
        // remove absent methods
        for (RestRequestInterface.HttpMethod httpMethod : resourceExistingMethodsSet) {
            removeMethodFromPath(pathItem, httpMethod);
        }
    }

    private static void addResourceToOpenApi(@Nonnull RestResource restResource, @Nonnull OpenAPI openApi) {
        String pathItemName = restResource.getFullPath(false);
        PathItem pathItem = new PathItem();
        for (RestMethod restMethod : restResource.getRestMethodList()) {
            OpenAPIUtils.addMethodToPath(pathItem, restMethod);
        }
        OpenAPIUtils.copyParametersToPath(pathItem, restResource.getParams());
        pathItem.setDescription(restResource.getDescription());
        openApi.path(pathItemName, pathItem);
    }

    public static void synchronizeResources(@Nonnull List<RestResource> restResources, @Nonnull OpenAPI openApi) {
        Paths existingPaths = openApi.getPaths();
        Set<String> removedResourcesSet = new HashSet<>();
        for (String item : existingPaths.keySet()) {
            removedResourcesSet.add(item);
        }

        for (RestResource restResource : restResources) {
            String pathItemName = restResource.getFullPath(false);
            removedResourcesSet.remove(pathItemName);
            if (existingPaths.containsKey(pathItemName)) {
                updateResource(restResource, existingPaths.get(pathItemName));
            } else {
                addResourceToOpenApi(restResource, openApi);
            }
        }
        for (String path : removedResourcesSet) {
            existingPaths.remove(path);
        }
        openApi.setPaths(existingPaths);
    }

    public static Operation extractOperation(PathItem pathItem, RestRequestInterface.HttpMethod method) {
        if (method.equals(RestRequestInterface.HttpMethod.GET) && pathItem.getGet() != null) {
            return pathItem.getGet();
        } else if (method.equals(POST) && pathItem.getPost() != null) {
            return pathItem.getPost();
        } else if (method.equals(DELETE) && pathItem.getDelete() != null) {
            return pathItem.getDelete();
        } else if (method.equals(HEAD) && pathItem.getHead() != null) {
            return pathItem.getHead();
        } else if (method.equals(TRACE) && pathItem.getTrace() != null) {
            return pathItem.getTrace();
        } else if (method.equals(PUT) && pathItem.getPut() != null) {
            return pathItem.getPut();
        } else if (method.equals(OPTIONS) && pathItem.getOptions() != null) {
            return pathItem.getOptions();
        } else if (method.equals(PATCH) && pathItem.getPatch() != null) {
            return pathItem.getPatch();
        }
        return null;
    }

    private static List<Operation> extractOperationList(PathItem pathItem) {
        List<Operation> operationList = new ArrayList();
        if (pathItem.getGet() != null) {
            operationList.add(pathItem.getGet());
        }
        if (pathItem.getPost() != null) {
            operationList.add(pathItem.getPost());
        }
        if (pathItem.getDelete() != null) {
            pathItem.getDelete();
        }
        if (pathItem.getHead() != null) {
            operationList.add(pathItem.getHead());
        }
        if (pathItem.getTrace() != null) {
            operationList.add(pathItem.getTrace());
        }
        if (pathItem.getPut() != null) {
            operationList.add(pathItem.getPut());
        }
        if (pathItem.getOptions() != null) {
            operationList.add(pathItem.getOptions());
        }
        if (pathItem.getPatch() != null) {
            operationList.add(pathItem.getPatch());
        }
        return operationList;
    }

    /**
     * @param pathItem
     * @return set of pathItem methods
     */
    private static Set<RestRequestInterface.HttpMethod> extractMethodsSet(@Nonnull PathItem pathItem) {
        Set<RestRequestInterface.HttpMethod> existingMethodsSet = new HashSet<>();
        for (RestRequestInterface.HttpMethod httpMethod : RestRequestInterface.HttpMethod.values()) {
            if (extractOperation(pathItem, httpMethod) != null) {
                existingMethodsSet.add(httpMethod);
            }
        }
        return existingMethodsSet;
    }

    public static void addMethodToPath(PathItem pathItem, RestMethod restMethod) {
        if (pathItem != null && restMethod != null) {
            if (restMethod.getMethod().equals(RestRequestInterface.HttpMethod.GET)) {
                pathItem.setGet(createOperation(restMethod));
            } else if (restMethod.getMethod().equals(POST)) {
                pathItem.setPost(createOperation(restMethod));
            } else if (restMethod.getMethod().equals(DELETE)) {
                pathItem.setDelete(createOperation(restMethod));
            } else if (restMethod.getMethod().equals(HEAD)) {
                pathItem.setHead(createOperation(restMethod));
            } else if (restMethod.getMethod().equals(TRACE)) {
                pathItem.setTrace(createOperation(restMethod));
            } else if (restMethod.getMethod().equals(PUT)) {
                pathItem.setPut(createOperation(restMethod));
            } else if (restMethod.getMethod().equals(OPTIONS)) {
                pathItem.setOptions(createOperation(restMethod));
            } else if (restMethod.getMethod().equals(PATCH)) {
                pathItem.setPatch(createOperation(restMethod));
            }
        }
    }

    private static void removeMethodFromPath(PathItem pathItem, RestRequestInterface.HttpMethod httpMethod) {
        if (pathItem != null && httpMethod != null) {
            switch (httpMethod) {
                case GET:
                    pathItem.setGet(null);
                    break;
                case POST:
                    pathItem.setPost(null);
                    break;
                case DELETE:
                    pathItem.setDelete(null);
                    break;
                case HEAD:
                    pathItem.setHead(null);
                    break;
                case TRACE:
                    pathItem.setTrace(null);
                    break;
                case PUT:
                    pathItem.setPut(null);
                    break;
                case OPTIONS:
                    pathItem.setOptions(null);
                    break;
                case PATCH:
                    pathItem.setPatch(null);
                    break;
            }
        }
    }

    private static Operation createOperation(RestMethod restMethod) {
        Operation operation = new Operation();
        operation.setDescription(restMethod.getDescription() == null ? "" : restMethod.getDescription());
        copyParametersToOperation(operation, restMethod.getParams());
        createResponses(operation, restMethod);
        createRequestBodies(operation, restMethod);

        return operation;
    }

    private static void copyParametersToOperation(Operation operation, RestParamsPropertyHolder propertyHolder) {
        for (String propertyName : propertyHolder.getPropertyNames()) {
            RestParamProperty paramProperty = propertyHolder.getProperty(propertyName);
            Parameter parameter = new Parameter();
            parameter.setName(propertyName);
            parameter.setIn(convertReadyApiParameterToOpenApiParameterType(paramProperty));
            parameter.setSchema(xmlSchemaTypesToJsonTypes(paramProperty.getSchemaType()));
            parameter.setRequired(paramProperty.getRequired());
            operation.addParametersItem(parameter);
        }
    }

    public static String convertReadyApiParameterToOpenApiParameterType(RestParamProperty restParamProperty) {
        if (restParamProperty.getStyle().equals(HEADER)) {
            return "header";
        } else if (restParamProperty.getStyle().equals(QUERY)) {
            return "query";
        } else if (restParamProperty.getStyle().equals(TEMPLATE)) {
            return "path";
        } else {
            return "cookie";
        }
    }

    private static void createResponses(Operation operation, RestMethod restMethod) {
        ApiResponses apiResponses = new ApiResponses();
        for (RestRepresentation representation : restMethod.getRepresentations()) {
            ApiResponse response;
            if (apiResponses.containsKey(representation.getStatus())) {
                response = apiResponses.get(representation.getStatus());
            } else {
                response = new ApiResponse();
                response.setDescription(representation.getDescription() == null ? "" : representation.getDescription());
                response.setContent(new Content());
                if (org.apache.commons.lang3.StringUtils.isNumeric(representation.getStatus().toString())) {
                    apiResponses.put(representation.getStatus().toString(), response);
                }
            }
            if (!response.getContent().containsKey(representation.getMediaType()) && com.eviware.soapui.support.StringUtils.hasContent(representation.getMediaType())) {
                MediaType mediaType = new MediaType();
                mediaType.setExample(com.eviware.soapui.support.StringUtils.hasContent(representation.getDefaultContent()) ? representation.getDefaultContent() : "");
                response.getContent().put(representation.getMediaType(), mediaType);
            }
        }
        if (apiResponses.isEmpty()) {
            ApiResponse defaultResponse = new ApiResponse();
            defaultResponse.setDescription("Default response");
            apiResponses.setDefault(defaultResponse);
        }
        operation.setResponses(apiResponses);
    }

    private static void createRequestBodies(Operation operation, RestMethod restMethod) {
        if (!canHavePayload(restMethod.getMethod())) {
            return;
        }
        RequestBody requestBody = new RequestBody();
        Content content = new Content();
        requestBody.setContent(content);
        for (RestRequest restRequest : restMethod.getRequestList()) {
            if (!content.containsKey(restRequest.getMediaType())) {
                MediaType mediaType = new MediaType();
                if (com.eviware.soapui.support.StringUtils.hasContent(restRequest.getRequestContent())) {
                    mediaType.setExample(restRequest.getRequestContent());
                }
                content.put(restRequest.getMediaType(), mediaType);
            }
        }
        operation.setRequestBody(requestBody);
    }

    public static boolean compareResourceWithPath(String resourcePath, String path) {
        String regex = generateRegexForPath(path);
        return resourcePath.matches(regex);
    }

    public static int getPathLength(String path) {
        Pattern pattern = Pattern.compile(".+?/");
        Matcher matcher = pattern.matcher(path.replaceAll("/$", ""));
        int length = 0;
        while (matcher.find()) {
            length++;
        }

        return length;
    }

    private static String generateRegexForPath(String path) {
        String processedPath = replaceUnescaped(path);
        return ".*" + processedPath.replaceAll("\\\\\\{.*?}", ".*?") + "$";
    }

    @Nullable
    public static String extractMediaType(String mediaType) {
        if (com.eviware.soapui.support.StringUtils.isNullOrEmpty(mediaType)) {
            return null;
        }

        Pattern pattern = Pattern.compile("\\w+/[-+.\\w]+");
        Matcher m = pattern.matcher(mediaType);
        if (m.find()) {
            return m.group();
        }
        return mediaType;
    }

    public static void createMockResponses(MockOperation mockOperation, OpenAPI openAPI) {
        if (!(mockOperation instanceof RestMockAction) || openAPI == null) {
            return;
        }
        RestMockAction restMockAction = (RestMockAction) mockOperation;
        PathItem pathItem = openAPI.getPaths().get(((RestMockAction) mockOperation).getResourcePath());
        if (pathItem != null) {
            for (Operation operation : extractOperationList(pathItem)) {
                ApiResponses apiResponses = operation.getResponses();
                if (apiResponses != null) {
                    for (String responseCode : apiResponses.keySet()) {
                        ApiResponse apiResponse = apiResponses.get(responseCode);
                        if (apiResponse.getContent() != null) {
                            for (String contentType : apiResponse.getContent().keySet()) {
                                if (isOperationHasSameResponse(restMockAction, contentType, responseCode)) {
                                    continue;
                                }
                                MediaType mediaType = apiResponse.getContent().get(contentType);
                                RestMockResponse restMockResponse = restMockAction.addNewMockResponse(ModelItemNamer.getUniqueName("Response ", restMockAction));
                                restMockResponse.setContentType(contentType);
                                if (StringUtils.isNumeric(responseCode)) {
                                    restMockResponse.setResponseHttpStatus(NumberUtils.toInt(responseCode));
                                }
                                if (mediaType.getExample() != null) {
                                    restMockResponse.setResponseContent(extractStringFromExampleObject(contentType, mediaType.getExample()));
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static boolean isOperationHasSameResponse(RestMockAction restMockAction, String contentType, String responseCode) {
        for (RestMockResponse mockResponse : restMockAction.getMockResponses()) {
            if (mockResponse.getResponseHttpStatus() == NumberUtils.toInt(responseCode) && mockResponse.getContentType().equals(contentType)) {
                return true;
            }
        }
        return false;
    }

    public static Schema xmlSchemaTypesToJsonTypes(SchemaType schemaType) {
        if (schemaType == null) {
            return null;
        }
        Schema schema = new Schema();
        String schemaString = schemaType.toString();
        String[] splitted = schemaString.split("@");
        if (splitted.length > 1) {
            String[] primitiveType = splitted[0].split("=");
            if (primitiveType.length > 1) {
                String type = primitiveType[0];
                switch (type) {
                    case "double":
                    case "decimal":
                    case "float":
                    case "long":
                    case "short":
                    case "int":
                        schema.setType("number");
                        break;
                    case "boolean":
                        schema.setType("boolean");
                        break;
                    case "positiveInteger":
                        schema.setType("number");
                        schema.setMinimum(new BigDecimal(1));
                        schema.setExclusiveMinimum(false);
                        break;
                    case "negativeInteger":
                        schema.setType("number");
                        schema.setMaximum(new BigDecimal(-1));
                        schema.setExclusiveMaximum(false);
                        break;
                    case "nonPositiveInteger":
                        schema.setType("number");
                        schema.setMaximum(new BigDecimal(0));
                        schema.setExclusiveMaximum(false);
                        break;
                    case "nonNegativeInteger":
                        schema.setType("number");
                        schema.setMinimum(new BigDecimal(0));
                        schema.setExclusiveMinimum(false);
                        break;
                    default:
                        schema.setType("string");
                        break;
                }
                return schema;
            } else {
                schema.setType("string");
            }
        } else {
            schema.setType("string");
        }
        return schema;
    }

    public static String extractStringFromExampleObject(String contentType, Object example) {
        if (example instanceof String) {
            return ((String) example);
        } else if (contentType.equals("application/yaml")) {
            try {
                return Yaml.pretty().writeValueAsString(example);
            } catch (JsonProcessingException e) {
                LOG.warn("Failed to serialize example to YAML", e);
            }
        } else if (contentType.equals("application/xml")) {
            try {
                return XmlUtils.prettyPrintXml(JsonUtil.getXmlMapper().writeValueAsString(example));
            } catch (JsonProcessingException e) {
                LOG.warn("Failed to serialize example to XML", e);
            }
        }
        return Json.pretty(example);
    }
}
