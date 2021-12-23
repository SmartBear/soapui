/**
 * Copyright 2013-2017 SmartBear Software, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.smartbear.integrations.swagger;

import com.eviware.soapui.config.TestAssertionConfig;
import com.eviware.soapui.impl.multiconfiguration.rest.RestMultiConfigurationTestStepInterface;
import com.eviware.soapui.impl.rest.RestRequestInterface;
import com.eviware.soapui.impl.rest.mock.RestMockAction;
import com.eviware.soapui.impl.rest.panels.mock.RestMockResultMessageExchange;
import com.eviware.soapui.impl.wsdl.panels.assertions.AssertionCategoryMapping;
import com.eviware.soapui.impl.wsdl.panels.assertions.AssertionListEntry;
import com.eviware.soapui.impl.wsdl.submit.HttpMessageExchange;
import com.eviware.soapui.impl.wsdl.submit.transports.http.HttpResponse;
import com.eviware.soapui.impl.wsdl.support.PathUtils;
import com.eviware.soapui.impl.wsdl.teststeps.HttpTestRequest;
import com.eviware.soapui.impl.wsdl.teststeps.HttpTestRequestInterface;
import com.eviware.soapui.impl.wsdl.teststeps.RestRequestStepResult;
import com.eviware.soapui.impl.wsdl.teststeps.RestTestRequest;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlMessageAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.AbstractTestAssertionFactory;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.TestPropertyHolder;
import com.eviware.soapui.model.iface.MessageExchange;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.mock.MockRequest;
import com.eviware.soapui.model.propertyexpansion.PropertyExpander;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContext;
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.model.testsuite.AssertionError;
import com.eviware.soapui.model.testsuite.AssertionException;
import com.eviware.soapui.model.testsuite.RequestAssertion;
import com.eviware.soapui.model.testsuite.ResponseAssertion;
import com.eviware.soapui.support.JsonUtil;
import com.eviware.soapui.support.MessageSupport;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.types.StringList;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.soapui.support.types.StringToStringsMap;
import com.eviware.soapui.support.xml.XmlObjectConfigurationBuilder;
import com.eviware.soapui.support.xml.XmlObjectConfigurationReader;
import com.eviware.x.form.XForm;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.XFormDialogBuilder;
import com.eviware.x.form.XFormFactory;
import com.eviware.x.impl.swing.FileFormField;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.load.configuration.LoadingConfiguration;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.smartbear.ready.core.Logging;
import io.swagger.inflector.utils.ResolverUtil;
import io.swagger.models.Model;
import io.swagger.models.Operation;
import io.swagger.models.RefModel;
import io.swagger.models.Response;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.AbstractSerializableParameter;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.parser.OpenAPIParser;
import io.swagger.util.Json;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.CookieParameter;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import io.swagger.v3.oas.models.parameters.PathParameter;
import io.swagger.v3.oas.models.parameters.QueryParameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import io.swagger.v3.parser.util.ClasspathHelper;
import org.apache.commons.io.FileUtils;
import org.apache.xmlbeans.XmlObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.eviware.soapui.support.StringUtils.quote;
import static com.eviware.soapui.support.StringUtils.sameString;

public class SwaggerComplianceAssertion extends WsdlMessageAssertion implements ResponseAssertion, RequestAssertion {
    private static final MessageSupport messages = MessageSupport.getMessages(SwaggerComplianceAssertion.class);

    private static final String SWAGGER_URL = "swaggerUrl";
    private static final String STRICT_MODE = "strictMode";
    private static final String SWAGGER_URL_FIELD = "Swagger URL";
    private static final String STRICT_MODE_FIELD = "Strict Mode";
    private static final String ID = "SwaggerComplianceAssertion";
    public static final String LABEL = "Swagger Compliance Assertion";
    private static final String DESCRIPTION = "Asserts that the request and response messages are compliant with a Swagger definition";
    private boolean strictMode;
    private String swaggerUrl;
    private Swagger swagger;
    private JsonSchema swaggerSchema;
    private XFormDialog dialog;
    private OpenAPI openAPI;
    private Boolean isOpenAPI;

    /**
     * Assertions need to have a constructor that takes a TestAssertionConfig and the ModelItem to be asserted
     */

    public SwaggerComplianceAssertion(TestAssertionConfig assertionConfig, Assertable modelItem) {
        super(assertionConfig, modelItem, true, false, false, true);
        readValuesFromConfig();
    }

    @Override
    public void setConfiguration(XmlObject configuration) {
        super.setConfiguration(configuration);
        readValuesFromConfig();
    }

    private void readValuesFromConfig() {
        XmlObjectConfigurationReader reader = new XmlObjectConfigurationReader(getConfiguration());
        swaggerUrl = reader.readString(SWAGGER_URL, null);
        strictMode = reader.readBoolean(STRICT_MODE, true);
    }

    public String getSwaggerUrl() {
        return swaggerUrl;
    }

    public boolean getStrictMode() {
        return strictMode;
    }

    private String getPreparedSwaggerUrl() {
        return getPreparedSwaggerUrl(null);
    }

    private String getPreparedSwaggerUrl(SubmitContext submitContext) {
        String expandedUrl;
        if (submitContext == null) {
            expandedUrl = PropertyExpander.expandProperties(swaggerUrl);
        } else {
            expandedUrl = PropertyExpander.expandProperties(submitContext, swaggerUrl);
        }
        return PathUtils.isFilePath(expandedUrl) ? PathUtils.ensureFilePathIsUrl(expandedUrl) : expandedUrl;
    }

    @Override
    public boolean isConfigurable() {
        return true;
    }

    public boolean configure() {
        if (dialog == null) {
            buildDialog();
        }

        StringToStringMap values = new StringToStringMap();
        values.put(SWAGGER_URL_FIELD, swaggerUrl);
        values.put(STRICT_MODE_FIELD, strictMode);

        values = dialog.show(values);
        if (dialog.getReturnValue() == XFormDialog.OK_OPTION) {
            setSwaggerUrl(values.get(SWAGGER_URL_FIELD));
            strictMode = values.getBoolean(STRICT_MODE_FIELD);

            setConfiguration(createConfiguration());
            return true;
        }
        return false;
    }

    private void buildDialog() {
        XFormDialogBuilder builder = XFormFactory.createDialogBuilder(messages.get("SwaggerComplianceAssertion.Dialog.Name"));
        XForm mainForm = builder.createForm("Basic");

        FileFormField fileFormField = (FileFormField) mainForm.addTextField(SWAGGER_URL_FIELD,
                messages.get("SwaggerComplianceAssertion.Swagger.Url.Field.Description"), XForm.FieldType.FILE);
        fileFormField.setWidth(40);
        Map<String, StringList> filters = new HashMap<>();
        filters.put(messages.get("SwaggerComplianceAssertion.Swagger.Url.Field.Json.Extension"), new StringList("json"));
        String[] yamlExtensions = {"yml", "yaml"};
        filters.put(messages.get("SwaggerComplianceAssertion.Swagger.Url.Field.Yaml.Extension"), new StringList(yamlExtensions));
        fileFormField.addChoosableFileFilters(filters);
        mainForm.addCheckBox(STRICT_MODE_FIELD, messages.get("SwaggerComplianceAssertion.Strict.Mode.Field.Description"));

        dialog = builder.buildDialog(builder.buildOkCancelActions(),
                messages.get("SwaggerComplianceAssertion.Dialog.Description"), UISupport.OPTIONS_ICON);
    }

    public void setSwaggerUrl(String endpoint) {
        setSwaggerUrl(endpoint, false);
    }

    public void setSwaggerUrl(String endpoint, boolean saveToConfig) {
        swaggerUrl = endpoint;
        swagger = null;
        openAPI = null;
        swaggerSchema = null;
        isOpenAPI = null;
        if (saveToConfig) {
            setConfiguration(createConfiguration());
        }
    }

    public void setStrictMode(boolean strictMode, boolean saveToConfig) {
        if (saveToConfig) {
            this.strictMode = strictMode;
            setConfiguration(createConfiguration());
        }
    }

    protected XmlObject createConfiguration() {
        XmlObjectConfigurationBuilder builder = new XmlObjectConfigurationBuilder();
        return builder.add(SWAGGER_URL, swaggerUrl).add(STRICT_MODE, strictMode).finish();
    }

    @Override
    protected String internalAssertResponse(MessageExchange messageExchange, SubmitContext submitContext) throws AssertionException {

        try {
            if (swaggerUrl != null && messageExchange instanceof HttpMessageExchange) {
                if (swaggerUrl.isEmpty()) {
                    throw new AssertionException(new AssertionError(messages.get("SwaggerComplianceAssertion.Load.Failed", swaggerUrl)));
                }
                if (!messageExchange.hasResponse() || ((HttpMessageExchange) messageExchange).getResponseStatusCode() == 0) {
                    throw new AssertionException(new AssertionError("Missing response to validate"));
                }

                if (validateMessage((HttpMessageExchange) messageExchange, submitContext)) {
                    return "Response is compliant with Swagger Definition";
                }
            }
        } catch (AssertionException e) {
            throw e;
        } catch (Exception e) {
            throw new AssertionException(new AssertionError(messages.get("SwaggerComplianceAssertion.Check.Failed", e.getMessage())));
        }
        return "Response is compliant with Swagger definition";
    }

    private boolean validateMessage(HttpMessageExchange messageExchange, SubmitContext submitContext) throws MalformedURLException, AssertionException {
        if (isOpenAPI == null) {
            String expandedSwaggerUrl = PropertyExpander.expandProperties(submitContext, swaggerUrl);
            isOpenAPI = SwaggerUtils.isOpenApi(expandedSwaggerUrl);
        }
        if (isOpenAPI) {
            return validateOpenAPIOperation(messageExchange, submitContext);
        } else {
            return validateSwaggerResponse(messageExchange, submitContext);
        }
    }

    /**
     * @param modelItem
     * @return rest method
     */
    private RestRequestInterface.HttpMethod exportMethod(ModelItem modelItem) {
        RestRequestInterface.HttpMethod method;
        if (modelItem instanceof RestMultiConfigurationTestStepInterface) {
            method = ((RestMultiConfigurationTestStepInterface) modelItem).getRestMethod();
        } else {
            HttpTestRequestInterface<?> testRequest = ((HttpTestRequestInterface) modelItem);
            method = testRequest.getMethod();
        }
        return method;
    }

    private boolean validateSwaggerResponse(HttpMessageExchange messageExchange, SubmitContext submitContext) throws AssertionException, MalformedURLException {
        Swagger swagger = getSwagger(submitContext);

        RestRequestInterface.HttpMethod method = exportMethod(messageExchange.getModelItem());

        URL endpoint = new URL(messageExchange.getEndpoint());
        String path = endpoint.getPath();
        if (path != null) {
            String basePath = extractBasePath(swagger, path);

            for (String swaggerPath : swagger.getPaths().keySet()) {

                if (SwaggerUtils.matchesPath(basePath, swaggerPath)) {

                    Operation operation = findOperation(swagger.getPath(swaggerPath), method);
                    if (operation != null) {
                        validateOperation(swagger, operation, String.valueOf(messageExchange.getResponseStatusCode()),
                                messageExchange.getResponseContent(), messageExchange.getResponseContentType()
                        );

                        return true;
                    } else {
                        throw new AssertionException(new AssertionError(messages.get(
                                "SwaggerComplianceAssertion.Resource.Validation", method, basePath)));
                    }
                }
            }

            throw new AssertionException(new AssertionError("Failed to find resource for [" + path + "] in Swagger definition"));
        }

        return false;
    }

    private boolean validateOpenAPIOperation(HttpMessageExchange messageExchange, SubmitContext submitContext) throws AssertionException, MalformedURLException {
        getOpenAPI(submitContext);

        RestRequestInterface.HttpMethod method = exportMethod(messageExchange.getModelItem());

        String endpoint = null;
        if (messageExchange instanceof RestRequestStepResult) {
            HttpResponse response = (HttpResponse) messageExchange.getResponse();
            if (response != null) {
                endpoint = response.getURL().toString();
            }
        } else {
            endpoint = messageExchange.getEndpoint();
        }

        if (endpoint != null) {
            io.swagger.v3.oas.models.Operation operation = getOpenApiOperation(endpoint, method);
            validateOpenAPIOperation(operation, String.valueOf(messageExchange.getResponseStatusCode()), messageExchange.getResponseContent(), messageExchange.getResponseContentType(), submitContext);
            return true;
        }

        return false;
    }

    private Operation findOperation(io.swagger.models.Path path, RestRequestInterface.HttpMethod method) {
        switch (method) {
            case GET:
                return path.getGet();
            case POST:
                return path.getPost();
            case DELETE:
                return path.getDelete();
            case PUT:
                return path.getPut();
            case PATCH:
                return path.getPatch();
            case OPTIONS:
                return path.getOptions();
        }

        return null;
    }

    void validateOperation(Swagger swagger, Operation operation, String responseCode, String contentAsString, String contentType) throws AssertionException {

        Response responseSchema = operation.getResponses().get(responseCode);
        if (responseSchema == null) {
            responseSchema = operation.getResponses().get("default");
        }

        if (responseSchema != null) {
            validateResponse(contentAsString, contentType, swagger, responseSchema);
        } else if (strictMode) {
            throw new AssertionException(new AssertionError(
                    "Missing response definition for " + responseCode + " response in operation " + operation.getOperationId()));
        }
    }

    void validateOpenAPIOperation(io.swagger.v3.oas.models.Operation operation, String responseCode, String contentAsString, String contentType, SubmitContext submitContext) throws AssertionException {
        if (operation.getResponses() != null) {
            ApiResponses apiResponses = operation.getResponses();
            ApiResponse apiResponse = apiResponses.get(responseCode);
            if (apiResponse == null) {
                apiResponse = apiResponses.get("default");
            }

            if (apiResponse != null) {
                validateOpenAPIResponse(contentAsString, contentType, apiResponse, submitContext);
            } else if (strictMode) {
                throw new AssertionException(new AssertionError("Missing response definition for " + responseCode + " response in operation " + operation.getOperationId()));
            }
        }

    }

    void validateResponse(String contentAsString, String contentType, Swagger swagger, Response responseSchema) throws AssertionException {
        contentType = OpenAPIUtils.extractMediaType(contentType);

        if (StringUtils.isNullOrEmpty(contentType)) {
            return;
        }

        if (responseSchema.getResponseSchema() != null) {
            Model schema = responseSchema.getResponseSchema();
            if (schema instanceof RefModel) {
                Model model = swagger.getDefinitions().get(((RefModel) schema).getSimpleRef());
                if (model != null) {
                    validatePayload(contentAsString, null, contentType);
                }
            } else {
                validatePayload(contentAsString, Json.pretty(schema), contentType);
            }
        }
    }

    void validateOpenAPIResponse(String contentAsString, String contentType, ApiResponse apiResponse, SubmitContext submitContext) throws AssertionException {
        if (apiResponse.getContent() != null) {
            contentType = OpenAPIUtils.extractMediaType(contentType);

            if (!apiResponse.getContent().isEmpty() && contentType == null) {
                throw new AssertionException(new AssertionError(messages
                        .get("SwaggerComplianceAssertion.Compliance.Test.Invalid.Type.Failed", contentType)));
            }

            MediaType mediaType = apiResponse.getContent().get(contentType);
            if (mediaType != null) {
                if (mediaType.getSchema() != null) {
                    validateOpenApiPayload(contentAsString, Json.pretty(mediaType.getSchema()), contentType, submitContext);
                }
            }
        }
    }

    Swagger getSwagger(SubmitContext submitContext) throws AssertionException {
        if (swagger == null && swaggerUrl != null) {
            if (getPreparedSwaggerUrl(submitContext).startsWith("file:/")) {
                swagger = parseFileContent(submitContext);
            } else {
                swagger = SwaggerUtils.getSwagger(submitContext.expand(swaggerUrl), null, true);
            }
            if (swagger == null) {
                throw new AssertionException(new AssertionError(messages.get("SwaggerComplianceAssertion.Load.Failed", swaggerUrl)));
            }
            swaggerSchema = null;
        }
        if (swagger.getDefinitions() == null) {
            swagger.setDefinitions(new HashMap());
        }
        ResolverUtil swaggerResolver = new ResolverUtil();
        swaggerResolver.resolveFully(swagger);
        return swagger;
    }

    private Swagger parseFileContent(SubmitContext submitContext) throws AssertionException {
        try {
            Path path = Paths.get(URI.create(getPreparedSwaggerUrl(submitContext)));
            String swaggerContent;
            if (Files.exists(path)) {
                swaggerContent = FileUtils.readFileToString(path.toFile(), StandardCharsets.UTF_8);
            } else {
                swaggerContent = ClasspathHelper.loadFileFromClasspath(getPreparedSwaggerUrl(submitContext));
            }
            return SwaggerUtils.getSwagger(swaggerContent);
        } catch (IOException e) {
            throw new AssertionException(new AssertionError(messages.get("SwaggerComplianceAssertion.Load.Failed", swaggerUrl)));
        }
    }

    private boolean isNullableOneOf(JsonNode jsonNode) {
        for (JsonNode node : jsonNode.get("oneOf")) {
            if (node.get("type").asText().contains("null")) {
                return true;
            }
        }
        return false;
    }

    private boolean isNullableAnyOf(JsonNode jsonNode) {
        for (JsonNode node : jsonNode.get("anyOf")) {
            if (node.get("type").asText().contains("null")) {
                return true;
            }
        }
        return false;
    }

    //This method should be removed as soon as alternative way will be found!
    private void validateOpenApiNullableParameters(JsonNode schemaObject) {
        List<JsonNode> nullableNodes = schemaObject.findParents("nullable");
        for (JsonNode nullableNode : nullableNodes) {
            if (nullableNode.get("nullable").asBoolean()) {
                List<JsonNode> oneOfNodes = nullableNode.findParents("oneOf");
                List<JsonNode> anyOfNodes = nullableNode.findParents("anyOf");
                if (oneOfNodes.size() > 0) {
                    for (JsonNode oneOfNode : oneOfNodes) {
                        if (!(isNullableOneOf(oneOfNode))) {
                            ObjectNode newTypeNode = Json.mapper().createObjectNode();
                            newTypeNode.put("type", "null");
                            ((ObjectNode) oneOfNode).withArray("oneOf").add(newTypeNode);
                        }
                    }
                }
                if (anyOfNodes.size() > 0) {
                    for (JsonNode anyOfNode : anyOfNodes) {
                        if (!isNullableAnyOf(anyOfNode)) {
                            ObjectNode newTypeNode = Json.mapper().createObjectNode();
                            newTypeNode.put("type", "null");
                            ((ObjectNode) anyOfNode).withArray("anyOf").add(newTypeNode);
                        }
                    }
                }
                if (!nullableNode.asText().contains("null")) {
                    JsonNode node = nullableNode.findPath("type");
                    ((ObjectNode) nullableNode).putArray("type").add("null").add(node);
                }
            }
        }
    }

    private void validateOpenApiPayload(String payload, String schema, String contentType, SubmitContext submitContext) throws AssertionException {
        try {
            JsonSchema jsonSchema;
            if (schema != null) {
                // make local refs absolute to match existing schema
                schema = schema.replaceAll("\"#/components/schemas/", "\""
                        + getPreparedSwaggerUrl(submitContext) + "#/components/schemas/");
                JsonNode schemaObject = Json.mapper().readTree(schema);

                validateOpenApiNullableParameters(schemaObject);

                // build custom schema factory that preloads existing schema
                JsonSchemaFactory factory = JsonSchemaFactory.newBuilder()
                        .setLoadingConfiguration(LoadingConfiguration.newBuilder().preloadSchema(getPreparedSwaggerUrl(submitContext),
                                Json.mapper().readTree(Json.pretty(openAPI))).freeze()
                        ).freeze();
                jsonSchema = factory.getJsonSchema(schemaObject);
            } else {
                jsonSchema = getOpenApiSchema();
            }

            JsonNode contentObject;
            if (payload == null) {
                payload = "null";
            }
            if (contentType.equalsIgnoreCase("application/json")) {
                contentObject = Json.mapper().readTree(payload);
            } else if (contentType.equalsIgnoreCase("application/xml")) {
                contentObject = JsonUtil.getValidJsonFromXml(payload);
            } else {
                throw new AssertionException(new AssertionError(messages
                        .get("SwaggerComplianceAssertion.Compliance.Test.Invalid.Type.Failed", contentType)));
            }

            ValidationSupport.validateMessage(jsonSchema, contentObject);
        } catch (AssertionException e) {
            throw e;
        } catch (Exception e) {
            throw new AssertionException(new AssertionError(messages
                    .get("SwaggerComplianceAssertion.Compliance.Test.Failed", e.toString())));
        }
    }


    public void validatePayload(String payload, String schema, String contentType) throws AssertionException {
        try {
            JsonSchema jsonSchema;

            if (schema != null) {
                // make local refs absolute to match existing schema
                schema = schema.replaceAll("\"#/definitions/", "\""
                        + getPreparedSwaggerUrl() + "#/definitions/");

                JsonNode schemaObject = Json.mapper().readTree(schema);

                // build custom schema factory that preloads existing schema
                JsonSchemaFactory factory = JsonSchemaFactory.newBuilder()
                        .setLoadingConfiguration(LoadingConfiguration.newBuilder()
                                .preloadSchema(getPreparedSwaggerUrl(),
                                        Json.mapper().readTree(Json.pretty(swagger))).freeze()).freeze();
                jsonSchema = factory.getJsonSchema(schemaObject);
            } else {
                jsonSchema = getSwaggerSchema();
            }

            JsonNode contentObject;

            if (contentType.equalsIgnoreCase("application/json")) {
                contentObject = Json.mapper().readTree(payload);
            } else if (contentType.equalsIgnoreCase("application/xml")) {
                contentObject = JsonUtil.getValidJsonFromXml(payload);
            } else {
                throw new AssertionException(new AssertionError(messages
                        .get("SwaggerComplianceAssertion.Compliance.Test.Invalid.Type.Failed", contentType)));
            }

            ValidationSupport.validateMessage(jsonSchema, contentObject);
        } catch (AssertionException e) {
            throw e;
        } catch (Exception e) {
            throw new AssertionException(new AssertionError(messages
                    .get("SwaggerComplianceAssertion.Compliance.Test.Failed", e.toString())));
        }
    }

    private void validateSwaggerParameter(String value, AbstractSerializableParameter parameter) throws AssertionException {
        try {
            JsonSchema jsonSchema;
            String type = null;

            if (parameter.getType() != null) {
                if (parameter.getType().equals("array")) {
                    type = Json.pretty(parameter.getItems());
                } else {
                    type = "{\"type\":\"" + parameter.getType() + "\"}";
                }
            }
            String currentSchemaAsString = type;

            if (type != null) {
                // make local refs absolute to match existing schema
                currentSchemaAsString = currentSchemaAsString.replaceAll("\"#/definitions/", "\""
                        + getPreparedSwaggerUrl() + "#/definitions/");
                JsonNode schemaObject = Json.mapper().readTree(currentSchemaAsString);

                // build custom schema factory that preloads existing schema
                JsonSchemaFactory factory = JsonSchemaFactory.newBuilder()
                        .setLoadingConfiguration(LoadingConfiguration.newBuilder()
                                .preloadSchema(getPreparedSwaggerUrl(),
                                        Json.mapper().readTree(Json.pretty(swagger))).freeze()).freeze();
                jsonSchema = factory.getJsonSchema(schemaObject);
            } else {
                jsonSchema = getSwaggerSchema();
            }
            JsonNode contentObject = normalizeContent(value, parameter.getType(), parameter.getFormat());
            ValidationSupport.validateMessage(jsonSchema, contentObject);
        } catch (AssertionException e) {
            throw e;
        } catch (Exception e) {
            throw new AssertionException(new AssertionError
                    (messages.get("SwaggerComplianceAssertion.Compliance.Test.Failed", e.toString())));
        }
    }

    @Override
    protected String internalAssertRequest(MessageExchange messageExchange, PropertyExpansionContext context) throws AssertionException {
        MockRequest request = null;
        try {
            if (swaggerUrl != null && messageExchange instanceof RestMockResultMessageExchange) {
                request = ((RestMockResultMessageExchange) messageExchange).getMockRequest();
                validateMessageRequest((SubmitContext) context, request);
            }
        } catch (MalformedURLException e) {
            Logging.log(messages.get("SwaggerComplianceAssertion.Endpoint.LogMessage.Error",
                    request.getHttpRequest().getRequestURL().toString()));
        }
        return messages.get("SwaggerComplianceAssertion.Request.Validation.Success");
    }

    @Override
    protected boolean appliesToRequest(MessageExchange messageExchange) {
        return true;
    }

    private void validateMessageRequest(SubmitContext context, MockRequest request) throws AssertionException, MalformedURLException {
        if (isOpenAPI == null) {
            String expandedSwaggerUrl = PropertyExpander.expandProperties(context, swaggerUrl);
            isOpenAPI = SwaggerUtils.isOpenApi(expandedSwaggerUrl);
        }
        if (isOpenAPI) {
            validateOpenAPIOperationRequest(context, request);
        } else {
            validateSwaggerRequest(context, request);
        }
    }

    private void validateSwaggerRequest(SubmitContext context, MockRequest request) throws AssertionException, MalformedURLException {
        Swagger swagger = getSwagger(context);

        RestRequestInterface.HttpMethod method = request.getMethod();

        URL endpoint = new URL(request.getHttpRequest().getRequestURL().toString());
        String path = endpoint.getPath();
        if (path != null) {
            io.swagger.models.Path pathObj = null;
            String basePath = extractBasePath(swagger, path);
            for (String swaggerPath : swagger.getPaths().keySet()) {
                if (SwaggerUtils.matchesPath(basePath, swaggerPath)) {
                    pathObj = swagger.getPath(swaggerPath);
                }
            }

            Operation operation = findOperation(pathObj, method);
            if (operation != null) {
                validateSwaggerOperationRequest(operation, request);
            } else {
                throw new AssertionException(new AssertionError(messages
                        .get("SwaggerComplianceAssertion.Resource.Validation.Failed", method, path)));
            }
        }
    }

    private void validateOpenAPIOperationRequest(SubmitContext context, MockRequest request) throws AssertionException, MalformedURLException {
        getOpenAPI(context);

        RestRequestInterface.HttpMethod method = request.getMethod();
        String endpoint = request.getHttpRequest().getRequestURL().toString();
        io.swagger.v3.oas.models.Operation operation = getOpenApiOperation(endpoint, method);
        validateOpenAPIRequestOperation(operation, request);
    }

    private void validateSwaggerOperationRequest(Operation operation, MockRequest request) throws AssertionException {
        List<Parameter> parameters = operation.getParameters();

        List<io.swagger.models.parameters.QueryParameter> queryParameters = parameters.stream()
                .filter(parameter -> parameter instanceof io.swagger.models.parameters.QueryParameter)
                .map(parameter -> (io.swagger.models.parameters.QueryParameter) parameter).collect(Collectors.toList());
        List<io.swagger.models.parameters.HeaderParameter> headerParameters = parameters.stream()
                .filter(parameter -> parameter instanceof io.swagger.models.parameters.HeaderParameter)
                .map(parameter -> (io.swagger.models.parameters.HeaderParameter) parameter).collect(Collectors.toList());
        List<io.swagger.models.parameters.PathParameter> pathParameters = parameters.stream()
                .filter(parameter -> parameter instanceof io.swagger.models.parameters.PathParameter)
                .map(parameter -> (io.swagger.models.parameters.PathParameter) parameter).collect(Collectors.toList());

        if (!queryParameters.isEmpty() && !request.getHttpRequest().getParameterMap().isEmpty()) {
            checkSwaggerQueryParameter(queryParameters, request);
        }

        if (!(headerParameters.isEmpty())) {
            checkSwaggerHeaderParameter(headerParameters, request);
        }
        if (!pathParameters.isEmpty() && !request.getRequestContext().getProperties().isEmpty()) {
            checkSwaggerPathParameters(pathParameters, request);
        }

        BodyParameter bodyParameter = null;
        for (Parameter parameter : parameters) {
            if (parameter instanceof BodyParameter) {
                bodyParameter = (BodyParameter) parameter;
            }
        }
        if (bodyParameter != null) {
            Model requestSchema = bodyParameter.getSchema();
            if (requestSchema != null) {
                validatePayload(request.getRequestContent(), Json.pretty(requestSchema), request.getHttpRequest().getContentType());
            } else if (strictMode) {
                throw new AssertionException(new AssertionError(messages.get("SwaggerComplianceAssertion.SchemaException.Text",
                        operation.getOperationId())));
            }
        }
    }

    private void checkSwaggerPathParameters(List<io.swagger.models.parameters.PathParameter> parameters, MockRequest request)
            throws AssertionException {
        if (parameters.size() < request.getRequestContext().getProperties().size() && strictMode) {
            throw new AssertionException(new AssertionError(messages.get("SwaggerComplianceAssertion.Path.Parameter.Definition.Error")));
        }
        for (AbstractSerializableParameter parameter : parameters) {
            if (request.getRequestContext().getProperties().get(parameter.getName()) != null) {
                validateSwaggerParameterValue((String) request.getRequestContext().getProperties().get(parameter.getName()), parameter, request);
            } else {
                throw new AssertionException(new AssertionError(messages.get("SwaggerComplianceAssertion.Compliance.More.Parameters", parameter.getName())));
            }
        }
    }

    private void checkSwaggerQueryParameter(List<io.swagger.models.parameters.QueryParameter> parameters, MockRequest request)
            throws AssertionException {
        if (parameters.size() < request.getHttpRequest().getParameterMap().size() && strictMode) {
            throw new AssertionException(new AssertionError(messages.get("SwaggerComplianceAssertion.Query.Parameter.Definition.Error")));
        }
        for (AbstractSerializableParameter parameter : parameters) {
            if (request.getHttpRequest().getParameter(parameter.getName()) != null) {
                validateSwaggerParameterValue(request.getHttpRequest().getParameter(parameter.getName()), parameter, request);
            } else {
                throw new AssertionException(new AssertionError(messages.get("SwaggerComplianceAssertion.Compliance.More.Parameters", parameter.getName())));
            }
        }
    }

    private void checkSwaggerHeaderParameter(List<io.swagger.models.parameters.HeaderParameter> headerParameters,
                                             MockRequest request) throws AssertionException {
        StringToStringsMap headersAsLists = request.getRequestHeaders();
        StringToStringMap headersAsStrings = headersAsLists.toStringToStringMap();
        for (io.swagger.models.parameters.HeaderParameter headerParameter : headerParameters) {
            if (headersAsStrings.get(headerParameter.getName()) != null) {
                validateSwaggerParameterValue(headersAsStrings.get(headerParameter.getName()), headerParameter, request);
            } else {
                throw new AssertionException(new AssertionError(messages.get("SwaggerComplianceAssertion.Compliance.More.Parameters", headerParameter.getName())));
            }
        }
    }

    private void validateSwaggerParameterValue(String values, AbstractSerializableParameter parameter, MockRequest request)
            throws AssertionException {
        if (parameter.getAllowEmptyValue() != null) {
            if ((values == null || values.equals("")) && (!parameter.getAllowEmptyValue())) {
                throw new AssertionException(new AssertionError(messages
                        .get("SwaggerComplianceAssertion.Query.Parameter.Empty.Error", parameter.getName())));
            }
        }
        if (values == null && parameter.getRequired()) {
            throw new AssertionException(new AssertionError(messages
                    .get("SwaggerComplianceAssertion.Query.Parameter.Required.Error", parameter.getName())));
        }
        if (parameter.getType().equals("array")) {
            validateSwaggerArrayIn(values, parameter, request.getHttpRequest().getContentType());
        } else {
            validateSwaggerParameter(values, parameter);
        }
    }

    private void validateSwaggerArrayIn(String values, AbstractSerializableParameter parameter, String contentType)
            throws AssertionException {
        if (values == null) {
            validatePayload(null, Json.pretty(parameter.getItems()), contentType);
        }
        String[] delimiterList = values.split(determineSwaggerDelimiter(parameter));
        if (parameter.getMinItems() != null && parameter.getMinItems() > delimiterList.length) {
            throw new AssertionException(new AssertionError(messages
                    .get("SwaggerComplianceAssertion.Array.Minimum.Items.Error", parameter.getName())));
        }
        if (parameter.getMaxItems() != null && parameter.getMaxItems() < delimiterList.length) {
            throw new AssertionException(new AssertionError(messages
                    .get("SwaggerComplianceAssertion.Array.Maximum.Items.Error", parameter.getName())));
        }
        if (parameter.isUniqueItems() != null && parameter.isUniqueItems()) {
            if (Arrays.stream(delimiterList).distinct().count() != delimiterList.length) {
                throw new AssertionException(new AssertionError(messages
                        .get("SwaggerComplianceAssertion.Array.Duplicate.Items.Unique.Option.Error", parameter.getName())));
            }
        }
        for (String v : delimiterList) {
            validatePayload(v, Json.pretty(parameter.getItems()), contentType);
        }
    }

    private String determineSwaggerDelimiter(AbstractSerializableParameter parameter) {
        if (parameter.getCollectionFormat() == null) {
            return ",";
        }
        switch (parameter.getCollectionFormat()) {
            case "csv":
                return ",";
            case "ssv":
                return " ";
            case "tsv":
                return "\\";
            case "pipes":
                return "|";
            default:
                return ",";
        }
    }

    private void validateOpenAPIRequestOperation(io.swagger.v3.oas.models.Operation operation, MockRequest request)
            throws AssertionException {
        if (operation.getParameters() != null) {
            List<io.swagger.v3.oas.models.parameters.Parameter> parameters = operation.getParameters();

            List<QueryParameter> queryParameters = parameters.stream()
                    .filter(parameter -> parameter instanceof QueryParameter)
                    .map(parameter -> (QueryParameter) parameter).collect(Collectors.toList());
            List<HeaderParameter> headerParameters = parameters.stream()
                    .filter(parameter -> parameter instanceof HeaderParameter)
                    .map(parameter -> (HeaderParameter) parameter).collect(Collectors.toList());
            List<CookieParameter> cookieParameters = parameters.stream()
                    .filter(parameter -> parameter instanceof CookieParameter)
                    .map(parameter -> (CookieParameter) parameter).collect(Collectors.toList());
            List<PathParameter> pathParameters = parameters.stream()
                    .filter(parameter -> parameter instanceof PathParameter)
                    .map(parameter -> (PathParameter) parameter).collect(Collectors.toList());

            if (!queryParameters.isEmpty() && !request.getHttpRequest().getParameterMap().isEmpty()) {
                validateOpenApiQueryParameters(queryParameters, request);
            }
            if (!headerParameters.isEmpty()) {
                validateOpenApiHeaderParameters(headerParameters, request);
            }
            if (!cookieParameters.isEmpty()) {
                validateOpenApiCookieParameters(cookieParameters, request);
            }
            if (!pathParameters.isEmpty() && !request.getRequestContext().getProperties().isEmpty()) {
                validateOpenApiPathParameters(pathParameters, request);
            }
        }

        if (operation.getRequestBody() != null) {
            RequestBody requestBodyFromDefinition = operation.getRequestBody();
            String contentType = request.getHttpRequest().getContentType();
            if (request.getRequestContent() != null) {
                MediaType mediaType = requestBodyFromDefinition.getContent().get(contentType);
                if (mediaType.getSchema() != null) {
                    validatePayload(request.getRequestContent(), Json.pretty(mediaType.getSchema()), contentType);
                } else if (strictMode) {
                    throw new AssertionException(new AssertionError(messages
                            .get("SwaggerComplianceAssertion.SchemaException.Text", operation.getOperationId())));
                }
            }
        }
    }

    private void validateOpenApiPathParameters(List<PathParameter> parameters, MockRequest request) throws AssertionException {
        if (parameters.size() < request.getRequestContext().getProperties().size() && strictMode) {
            throw new AssertionException(new AssertionError(messages.get("SwaggerComplianceAssertion.Path.Parameter.Definition.Error")));
        }
        for (io.swagger.v3.oas.models.parameters.Parameter parameter : parameters) {
            if (request.getRequestContext().getProperties().get(parameter.getName()) != null) {
                validateOpenAPIParameterValue((String) request.getRequestContext().getProperties().get(parameter.getName()), parameter);
            } else {
                throw new AssertionException(new AssertionError(messages.get("SwaggerComplianceAssertion.Compliance.More.Parameters", parameter.getName())));
            }
        }
    }

    private JsonNode normalizeContent(String value, String type, String format) throws AssertionException {
        try {
            String normalisedValue = value;

            if ("null".equalsIgnoreCase(value)) {
                return Json.mapper().readTree("null");
            }
            if ("date-time".equalsIgnoreCase(format)) {
                try {
                    DateTimeFormatter DATE_TIME_FORMATTER;
                    DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder()
                            .appendPattern("yyyy-MM-dd")
                            .appendLiteral('T')
                            .appendPattern("HH:mm:ss")
                            .optionalStart()
                            .appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true)
                            .parseDefaulting(ChronoField.NANO_OF_SECOND, 0)
                            .optionalEnd()
                            .appendOffset("+HH:mm", "Z");
                    DATE_TIME_FORMATTER = builder.toFormatter();
                    LocalDateTime dateTime = LocalDateTime.parse(value, DATE_TIME_FORMATTER);
                    normalisedValue = quote(dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")));
                } catch (DateTimeParseException e) {
                    throw new AssertionException(new AssertionError(messages
                            .get("SwaggerComplianceAssertion.Parameter.Date.Format.Parsing", value)));
                }

            } else if ("number".equalsIgnoreCase(type) || "integer".equalsIgnoreCase(type)) {
                try {
                    Double.parseDouble(value);
                } catch (NumberFormatException e) {
                    normalisedValue = quote(value);
                }
            } else if ("string".equalsIgnoreCase(type)) {
                normalisedValue = quote(value);
            }
            return Json.mapper().readTree(normalisedValue);
        } catch (IOException e) {
            throw new AssertionException(new AssertionError(messages
                    .get("SwaggerComplianceAssertion.Compliance.Test.Failed", e.toString())));
        }
    }

    private void validateOpenApiQueryParameters(List<QueryParameter> parameters, MockRequest request) throws AssertionException {
        if (parameters.size() < request.getHttpRequest().getParameterMap().size() && strictMode) {
            throw new AssertionException(new AssertionError(messages.get("SwaggerComplianceAssertion.Query.Parameter.Definition.Error")));
        }
        for (QueryParameter parameter : parameters) {
            if (request.getHttpRequest().getParameter(parameter.getName()) != null) {
                validateOpenAPIParameterValue(request.getHttpRequest().getParameter(parameter.getName()), parameter);
            } else {
                throw new AssertionException(new AssertionError(messages.get("SwaggerComplianceAssertion.Compliance.More.Parameters", parameter.getName())));
            }
        }
    }

    private void validateOpenApiHeaderParameters(List<HeaderParameter> headerParameters, MockRequest request) throws AssertionException {
        StringToStringsMap headersAsLists = request.getRequestHeaders();
        StringToStringMap headersAsStrings = headersAsLists.toStringToStringMap();
        for (HeaderParameter headerParameter : headerParameters) {
            if (headersAsStrings.get(headerParameter.getName()) != null) {
                validateOpenAPIParameterValue(headersAsStrings.get(headerParameter.getName()), headerParameter);
            } else {
                throw new AssertionException(new AssertionError(messages
                        .get("SwaggerComplianceAssertion.Compliance.More.Parameters", headerParameter.getName())));
            }
        }
    }

    private void validateOpenAPIParameterValue(String values, io.swagger.v3.oas.models.parameters.Parameter parameter) throws AssertionException {
        if (parameter.getAllowEmptyValue() != null) {
            if ((values == null || values.equals("")) && (!parameter.getAllowEmptyValue())) {
                throw new AssertionException(new AssertionError(messages
                        .get("SwaggerComplianceAssertion.Query.Parameter.Empty.Error", parameter.getName())));
            }
        }
        if (values == null && parameter.getRequired()) {
            throw new AssertionException(new AssertionError(messages
                    .get("SwaggerComplianceAssertion.Query.Parameter.Required.Error", parameter.getName())));
        }
        if (parameter.getSchema() instanceof ArraySchema) {
            validateOpenAPIArraySchema(values, parameter);
        } else {
            validateParameter(values, parameter);
        }
    }

    private void validateOpenAPIArraySchema(String values, io.swagger.v3.oas.models.parameters.Parameter parameter) throws AssertionException {
        if (values == null) {
            validateParameter(null, parameter);
        }
        String[] delimiterList = values.split(determineDelimiter(parameter));
        if (parameter.getSchema().getMinItems() != null && parameter.getSchema().getMinItems() > delimiterList.length) {
            throw new AssertionException(new AssertionError(messages
                    .get("SwaggerComplianceAssertion.Array.Minimum.Items.Error", parameter.getName())));
        }
        if (parameter.getSchema().getMaxItems() != null && parameter.getSchema().getMaxItems() < delimiterList.length) {
            throw new AssertionException(new AssertionError(messages
                    .get("SwaggerComplianceAssertion.Array.Maximum.Items.Error", parameter.getName())));
        }
        if (parameter.getSchema().getUniqueItems() != null && parameter.getSchema().getUniqueItems()) {
            if (Arrays.stream(delimiterList).distinct().count() != delimiterList.length) {
                throw new AssertionException(new AssertionError(messages
                        .get("SwaggerComplianceAssertion.Array.Duplicate.Items.Unique.Option.Error", parameter.getName())));
            }
        }
        for (String v : delimiterList) {
            validateParameter(v, parameter);
        }
    }

    private String determineDelimiter(io.swagger.v3.oas.models.parameters.Parameter parameter) {
        switch (parameter.getStyle()) {
            case FORM:
                return ",";
            case LABEL:
                return ".";
            case SIMPLE:
                return ",";
            case SPACEDELIMITED:
                return " ";
            case PIPEDELIMITED:
                return "|";
            case MATRIX:
                return ",";
            default:
                return ",";
        }
    }

    private void validateOpenApiCookieParameters(List<CookieParameter> cookieParameters, MockRequest request) throws AssertionException {
        StringToStringsMap requestCookiesAsLists = request.getRequestHeaders();
        StringToStringMap requestCookiesAsStrings = requestCookiesAsLists.toStringToStringMap();
        validateOpenAPIParameterValue(requestCookiesAsStrings.get("Cookies"), cookieParameters.get(0));
    }

    private void validateParameter(String value, io.swagger.v3.oas.models.parameters.Parameter parameter) throws AssertionException {
        try {
            JsonSchema jsonSchema;
            Schema schema;

            if (parameter.getSchema() instanceof ArraySchema) {
                ArraySchema arraySchema = (ArraySchema) parameter.getSchema();
                schema = arraySchema.getItems();
            } else {
                schema = parameter.getSchema();
            }

            String currentSchemaAsString = Json.pretty(schema);
            // make local refs absolute to match existing schema
            if (isOpenAPI) {
                currentSchemaAsString = currentSchemaAsString.replaceAll("\"#/components/schemas/", "\""
                        + getPreparedSwaggerUrl() + "#/components/schemas/");
            } else {
                currentSchemaAsString = currentSchemaAsString.replaceAll("\"#/definitions/", "\""
                        + getPreparedSwaggerUrl() + "#/definitions/");
            }

            if (schema != null) {
                // make local refs absolute to match existing schema
                currentSchemaAsString = currentSchemaAsString
                        .replaceAll("\"#/definitions/", "\"" + getPreparedSwaggerUrl() + "#/definitions/");
                JsonNode schemaObject = Json.mapper().readTree(currentSchemaAsString);

                // build custom schema factory that preloads existing schema
                JsonSchemaFactory factory = JsonSchemaFactory.newBuilder()
                        .setLoadingConfiguration(LoadingConfiguration.newBuilder().preloadSchema(getPreparedSwaggerUrl(),
                                Json.mapper().readTree(Json.pretty(swagger))).freeze()).freeze();
                jsonSchema = factory.getJsonSchema(schemaObject);
            } else {
                jsonSchema = getSwaggerSchema();
            }
            JsonNode contentObject = normalizeContent(value, schema.getType(), schema.getFormat());
            ValidationSupport.validateMessage(jsonSchema, contentObject);
        } catch (AssertionException e) {
            throw e;
        } catch (Exception e) {
            throw new AssertionException(new AssertionError(messages
                    .get("SwaggerComplianceAssertion.Compliance.Test.Failed", e.toString())));
        }
    }

    private JsonSchema getSwaggerSchema() throws IOException, ProcessingException {
        if (swaggerSchema == null) {
            JsonNode schemaObject = Json.mapper().readTree(Json.pretty(swagger));
            JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
            swaggerSchema = factory.getJsonSchema(schemaObject);
        }

        return swaggerSchema;
    }

    private JsonSchema getOpenApiSchema() throws IOException, ProcessingException {
        if (swaggerSchema == null) {
            JsonNode schemaObject = Json.mapper().readTree(Json.pretty(openAPI));
            JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
            swaggerSchema = factory.getJsonSchema(schemaObject);
        }

        return swaggerSchema;
    }

    @Override
    protected String internalAssertProperty(TestPropertyHolder source, String propertyName, MessageExchange messageExchange,
                                            SubmitContext context) throws AssertionException {
        return null;
    }

    public void configureAssertion(Map<String, Object> configMap) {
        swaggerUrl = (String) configMap.get(SWAGGER_URL);
        strictMode = Boolean.valueOf((String) configMap.get(STRICT_MODE));
    }

    private OpenAPI getOpenAPI(SubmitContext submitContext) throws AssertionException {
        if (openAPI == null) {
            OpenAPIParser parser = new OpenAPIParser();
            ParseOptions options = new ParseOptions();
            boolean resolveFully = Boolean.parseBoolean(System.getProperty("soapui.swagger.resolvefully", "true"));
            options.setResolveFully(resolveFully);
            options.setResolve(true);
            SwaggerParseResult result = parser.readLocation(submitContext.expand(swaggerUrl), null, options);
            if (result.getOpenAPI() == null) {
                throw new AssertionException(new AssertionError("Failed to load OpenAPI definition from [" + swaggerUrl + "]"));
            } else {
                openAPI = result.getOpenAPI();
            }
        }
        return openAPI;
    }

    private io.swagger.v3.oas.models.Operation getOpenApiOperation(String endpoint, RestRequestInterface.HttpMethod method)
            throws MalformedURLException, AssertionException {
        String resourcePath = new URL(endpoint).getPath();
        if (resourcePath.contains("?")) {
            resourcePath = resourcePath.substring(0, resourcePath.indexOf("?"));
        }
        PathItem path = findPathInDefinition(resourcePath);
        if (path == null) {
            throw new AssertionException(new AssertionError(messages.get("SwaggerComplianceAssertion.Resource.OpenAPI.Validation.Failed")));
        }
        io.swagger.v3.oas.models.Operation operation = OpenAPIUtils.extractOperation(path, method);

        if (operation == null) {
            throw new AssertionException(new AssertionError(messages
                    .get("SwaggerComplianceAssertion.Method.Validation.Failed", method, path)));
        }
        return operation;
    }

    private String extractBasePath(Swagger swagger, String path) {
        String basePath = swagger.getBasePath();
        if (basePath != null && !sameString(basePath, "/")) {
            basePath = StringUtils.trimRight(basePath, "/");
            if (path.toLowerCase().startsWith(basePath.toLowerCase())) {
                path = path.substring(basePath.length());
            }
        }
        return path;
    }

    private PathItem findPathInDefinition(String resourcePath) {
        if (openAPI.getPaths().keySet().contains(resourcePath)) {
            return openAPI.getPaths().get(resourcePath);
        } else {
            String targetPath = null;
            int targetPathLength = -1;
            int currentPathLength;
            for (String path : openAPI.getPaths().keySet()) {
                if (OpenAPIUtils.compareResourceWithPath(resourcePath, path)) {
                    currentPathLength = OpenAPIUtils.getPathLength(path);
                    if (targetPathLength < currentPathLength) {
                        targetPath = path;
                        targetPathLength = currentPathLength;
                    }
                }
            }

            if (StringUtils.hasContent(targetPath)) {
                return openAPI.getPaths().get(targetPath);
            }
        }

        return null;
    }

    public static class Factory extends AbstractTestAssertionFactory {
        public Factory() {
            super(ID, LABEL, SwaggerComplianceAssertion.class);
        }

        @Override
        public String getCategory() {
            return AssertionCategoryMapping.STATUS_CATEGORY;
        }

        @Override
        public Class<? extends WsdlMessageAssertion> getAssertionClassType() {
            return SwaggerComplianceAssertion.class;
        }

        @Override
        public AssertionListEntry getAssertionListEntry() {
            return new AssertionListEntry(ID, LABEL, DESCRIPTION, 70);
        }

        @Override
        public boolean canAssert(Assertable assertable) {
            return super.canAssert(assertable) && (assertable instanceof RestTestRequest || assertable instanceof HttpTestRequest
                    || assertable instanceof RestMockAction || assertable instanceof RestMultiConfigurationTestStepInterface);
        }
    }
}

