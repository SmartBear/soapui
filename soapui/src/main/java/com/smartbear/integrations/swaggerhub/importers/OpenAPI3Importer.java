package com.smartbear.integrations.swaggerhub.importers;

import com.eviware.soapui.config.OAuth2FlowConfig;
import com.eviware.soapui.impl.rest.*;
import com.eviware.soapui.impl.rest.RestRequestInterface.HttpMethod;
import com.eviware.soapui.impl.rest.support.RestParamProperty;
import com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder;
import com.eviware.soapui.impl.support.HttpUtils;
import com.eviware.soapui.impl.support.MediaTypeUtils;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.support.PathUtils;
import com.eviware.soapui.support.MessageSupport;
import com.eviware.soapui.support.ModelItemNamer;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.xml.XmlUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.common.collect.Lists;
import com.smartbear.integrations.swaggerhub.utils.OpenAPI3UrlClientLoader;
import com.smartbear.integrations.swaggerhub.utils.OpenAPIUtils;
import com.smartbear.swagger.SwaggerUtils;
import io.swagger.oas.inflector.examples.ExampleBuilder;
import io.swagger.oas.inflector.examples.XmlExampleSerializer;
import io.swagger.oas.inflector.examples.models.ObjectExample;
import io.swagger.oas.inflector.processors.JsonNodeExampleSerializer;
import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.Scopes;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.servers.ServerVariables;
import io.swagger.v3.parser.core.models.AuthorizationValue;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.http.client.utils.URIUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OpenAPI3Importer extends AbstractSwaggerImporter {
    /*OT*/ private static final String SAMPLE_GENERATION_FAILED_MESSAGE = "Failed to create the sample. The '%s' media type is incorrect.";

    public static final String COOKIE_PARAMETER_NAME = "Cookie";
    private static final Logger logger = LogManager.getLogger(OpenAPI3Importer.class);
    private static final MessageSupport messages = MessageSupport.getMessages(OpenAPI3Importer.class);
    private OpenAPI openAPI;

    public static AuthorizationValue authorizationValue;
    private final WsdlProject project;
    private final String defaultMediaType;
    private final HashMap context = new HashMap();
    private final boolean generateTestCase;
    private String definitionHostLocation;

    private static ObjectMapper yamlMapper;
    private static ObjectMapper jsonMapper;

    public OpenAPI3Importer(WsdlProject project) {
        this(project, SwaggerUtils.DEFAULT_MEDIA_TYPE);
    }

    public OpenAPI3Importer(WsdlProject project, String defaultMediaType) {
        this(project, defaultMediaType, false);
    }

    public OpenAPI3Importer(WsdlProject project, String defaultMediaType, boolean generateTestCase) {
        this.project = project;
        this.defaultMediaType = defaultMediaType;
        this.generateTestCase = generateTestCase;
    }

    static {
        yamlMapper = Yaml.mapper();
        jsonMapper = Json.mapper();
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(new JsonNodeExampleSerializer());

        yamlMapper.registerModule(simpleModule);
        jsonMapper.registerModule(simpleModule);
    }

    @Override
    public RestService[] importSwagger(String swaggerUrl) {
        return importSwagger(swaggerUrl, null);
    }

    @Override
    public RestService importApiDeclaration(String s) {
        return importSwagger(s)[0];
    }

    public RestService[] importSwagger(String swaggerUrl, String apiKey) {
        RestService restService = null;
        OpenAPIParser parser = new OpenAPIParser();
        ParseOptions options = new ParseOptions();
        boolean resolveFully = Boolean.parseBoolean(System.getProperty("soapui.swagger.resolvefully", "true"));
        options.setResolveFully(resolveFully);
        options.setResolve(true);
        SwaggerParseResult result;
        if (StringUtils.hasContent(apiKey)) {
            authorizationValue = new AuthorizationValue(AUTHORIZATION_HEADER, apiKey, TYPE_HEADER);
            result = parser.readLocation(swaggerUrl, Arrays.asList(authorizationValue), options);
        } else {
            definitionHostLocation = extractHostLocation(swaggerUrl);
            result = parser.readLocation(swaggerUrl, null, options);
        }

        List<String> messages = result.getMessages();
        if ((messages != null) && !messages.isEmpty()) {
            String errorMessage = StringUtils.join(messages.toArray(new String[messages.size()]), StringUtils.NEWLINE);
            errorMessage = StringUtils.toHtml(errorMessage);
            UISupport.showErrorMessage(errorMessage);
        } else {
            restService = createRestService(result, swaggerUrl);
        }

        context.put("swaggerUrl", swaggerUrl);
        return new RestService[]{restService};
    }

    private String extractHostLocation(String swaggerUrl) {
        if (startsWithHttp(swaggerUrl)) {
            try {
                return URIUtils.extractHost(new URI(swaggerUrl)).toURI();
            } catch (URISyntaxException ignore) {
            }
        }

        return null;
    }

    private boolean startsWithHttp(String url) {
        return url.toLowerCase().startsWith(HttpUtils.HTTP_PROTOCOL) || url.toLowerCase().startsWith(HttpUtils.HTTPS_PROTOCOL);
    }

    private RestService createRestService(SwaggerParseResult parseResult, String url) {
        if (parseResult == null || parseResult.getOpenAPI() == null) {
            return null;
        }
        openAPI = parseResult.getOpenAPI();
        String name = null;
        String description = "";
        Info info = openAPI.getInfo();

        if (info != null) {
            String title = info.getTitle();
            if (title != null) {
                name = title;
            }
            description = info.getDescription();
        }

        if (name == null) {
            if (startsWithHttp(url)) {
                try {
                    name = new URL(url).getHost();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            } else {
                int ix = url.lastIndexOf('/');
                name = ix == -1 || ix == url.length() - 1 ? url : url.substring(ix + 1);
            }
        }

        RestService restService = (RestService) project.addNewInterface(name, RestServiceFactory.REST_TYPE);
        restService.setDescription(description);

        String expandedUrl = PathUtils.expandPath(url, project);
        if (new File(expandedUrl).exists()) {
            try {
                expandedUrl = new File(expandedUrl).toURI().toURL().toString();
            } catch (MalformedURLException e) {
                //do nothing
            }
        }
        try {
            restService.getDefinitionContext().load(new OpenAPI3UrlClientLoader(expandedUrl, authorizationValue));
        } catch (Exception e) {
            //do nothing
        }

        List<Server> servers = openAPI.getServers();
        if (servers != null) {
            addEndpoints(servers, restService);
        }
        createResources(restService);
        createAuthProfiles();

        return restService;
    }

    private void addEndpoints(List<Server> servers, RestService restService) {
        final String DEFAULT_ENDPOINT = "/";
        if (servers != null) {
            for (Server server : servers) {
                String url = server.getUrl();
                if (server.getVariables() != null) {
                    ServerVariables serverVariables = server.getVariables();
                    for (String variable : serverVariables.keySet()) {
                        String defaultValue = serverVariables.get(variable).getDefault();
                        if (defaultValue == null) {
                            continue;
                        }
                        String variableTemplate = "{" + variable + "}";
                        if (url.contains(variableTemplate)) {
                            url = url.replace(variableTemplate, defaultValue);
                        }
                    }
                }

                if (isRelativeUrl(url) && StringUtils.hasContent(definitionHostLocation)) {
                    url = definitionHostLocation + url;
                }

                if (!Arrays.asList(restService.getEndpoints()).contains(url)) {
                    restService.addEndpoint(url);
                    if (isRelativeUrl(url)) {
                        logger.warn(String.format(
                                messages.get("OpenAPI3Importer.Relative.Endpoint.Warning.Message"), url, restService.getName()));
                    }
                }
            }
        }
        if (Arrays.asList(restService.getEndpoints()).contains(DEFAULT_ENDPOINT) && restService.getEndpoints().length > 1) {
            restService.removeEndpoint(DEFAULT_ENDPOINT);
        } else if (restService.getEndpoints().length == 0) {
            restService.addEndpoint(DEFAULT_ENDPOINT);
        }
    }

    private boolean isRelativeUrl(String url) {
        return url.startsWith("/");
    }

    private void createResources(RestService restService) {
        if (openAPI == null || openAPI.getPaths() == null) {
            return;
        }
        Paths paths = openAPI.getPaths();
        for (String pathKey : paths.keySet()) {
            PathItem path = paths.get(pathKey);
            String resourceName = path.getSummary() != null ? path.getSummary() : pathKey;
            RestResource restResource = restService.addNewResource(resourceName, pathKey);
            addEndpoints(path.getServers(), restService);
            addResourceLevelParameters(restResource, path.getParameters());
            createResourceMethods(restResource, path);
        }
    }

    private void addResourceLevelParameters(RestResource restResource, List<Parameter> parameters) {
        if (restResource == null || parameters == null) {
            return;
        }
        for (Parameter parameter : parameters) {
            if (parameter == null || BooleanUtils.isTrue(parameter.getDeprecated())) {
                continue;
            }
            createParameter(restResource.getParams(), parameter);
        }
    }

    /**
     * Sets the default parameter value from the schema if the value is specified
     *
     * @param paramProperty project service property object
     * @param parameter external definition property object
     */
    private void setDefaultParamValue(RestParamProperty paramProperty, Parameter parameter) {
        if (paramProperty == null || parameter == null) {
            return;
        }
        Schema schema = parameter.getSchema();
        if (schema == null) {
            return;
        }
        Object defaultObject = schema.getDefault();
        if (defaultObject == null) {
            return;
        }
        String result = String.valueOf(defaultObject);
        paramProperty.setDefaultValue(result);
        paramProperty.setValue(result);
    }

    private void createParameter(RestParamsPropertyHolder propertyHolder, Parameter parameter) {
        RestParamProperty paramProperty = propertyHolder.addProperty(getParameterName(parameter));
        String parameterIn = parameter.getIn();
        if (parameterIn.equalsIgnoreCase("query")) {
            paramProperty.setStyle(RestParamsPropertyHolder.ParameterStyle.QUERY);
            setDefaultParamValue(paramProperty, parameter);
        } else if (parameterIn.equalsIgnoreCase("header")) {
            paramProperty.setStyle(RestParamsPropertyHolder.ParameterStyle.HEADER);
        } else if (parameterIn.equalsIgnoreCase("path")) {
            paramProperty.setStyle(RestParamsPropertyHolder.ParameterStyle.TEMPLATE);
            setDefaultParamValue(paramProperty, parameter);
        } else if (parameterIn.equalsIgnoreCase("cookie")) {
            paramProperty.setStyle(RestParamsPropertyHolder.ParameterStyle.HEADER);
            setCookieValue(paramProperty, parameter.getName());
        }
        if (parameter.getRequired() != null) {
            paramProperty.setRequired(parameter.getRequired());
        }
        if (parameter.getDescription() != null) {
            paramProperty.setDescription(parameter.getDescription());
        }
    }

    private String getParameterName(Parameter parameter) {
        return parameter.getIn().equalsIgnoreCase("cookie") ? COOKIE_PARAMETER_NAME : parameter.getName();
    }

    private void setCookieValue(RestParamProperty paramProperty, String cookieName) {
        String cookieValue = paramProperty.getDefaultValue();
        if (StringUtils.hasContent(cookieValue)) {
            paramProperty.setValue(cookieValue + "; " + cookieName + "=");
        } else {
            paramProperty.setValue(cookieName + "=");
        }
    }

    private void createResourceMethods(RestResource restResource, PathItem pathItem) {
        if (restResource == null || pathItem == null) {
            return;
        }
        if (pathItem.getGet() != null) {
            createMethod(restResource, HttpMethod.GET, pathItem.getGet());
        }
        if (pathItem.getPost() != null) {
            createMethod(restResource, HttpMethod.POST, pathItem.getPost());
        }
        if (pathItem.getDelete() != null) {
            createMethod(restResource, HttpMethod.DELETE, pathItem.getDelete());
        }
        if (pathItem.getPut() != null) {
            createMethod(restResource, HttpMethod.PUT, pathItem.getPut());
        }
        if (pathItem.getOptions() != null) {
            createMethod(restResource, HttpMethod.OPTIONS, pathItem.getOptions());
        }
        if (pathItem.getTrace() != null) {
            createMethod(restResource, HttpMethod.TRACE, pathItem.getTrace());
        }
        if (pathItem.getHead() != null) {
            createMethod(restResource, HttpMethod.HEAD, pathItem.getHead());
        }
        if (pathItem.getPatch() != null) {
            createMethod(restResource, HttpMethod.PATCH, pathItem.getPatch());
        }
    }

    private void createMethod(RestResource restResource, HttpMethod method, Operation operation) {
        if (operation == null || BooleanUtils.isTrue(operation.getDeprecated())) {
            return;
        }
        String name;
        if (StringUtils.hasContent(operation.getOperationId())) {
            name = operation.getOperationId();
        } else {
            name = method.toString();
        }
        RestMethod restMethod = restResource.addNewMethod(name);
        restMethod.setMethod(method);
        if (operation.getDescription() != null) {
            restMethod.setDescription(operation.getDescription());
        } else if (operation.getSummary() != null) {
            restMethod.setDescription(operation.getSummary());
        }
        addMethodLevelParameters(restMethod, operation.getParameters());
        createRepresentations(restMethod, operation);
        addEndpoints(operation.getServers(), restResource.getService());
        createRequest(restMethod, operation);
    }

    private void addMethodLevelParameters(RestMethod restMethod, List<Parameter> parameters) {
        if (restMethod == null || parameters == null) {
            return;
        }
        for (Parameter parameter : parameters) {
            if (BooleanUtils.isTrue(parameter.getDeprecated())) {
                continue;
            }
            createParameter(restMethod.getParams(), parameter);
        }
    }

    private void createRequest(RestMethod restMethod, Operation operation) {
        if (operation.getRequestBody() != null && operation.getRequestBody().getContent() != null) {
            Content content = operation.getRequestBody().getContent();
            if (!content.isEmpty()) {
                boolean addRequestContent = Boolean.parseBoolean(System.getProperty("soapui.swagger.addRequestContent", "true"));
                for (Map.Entry<String, MediaType> contentItem : content.entrySet()) {
                    RestRepresentation representation = restMethod.addNewRepresentation(RestRepresentation.Type.REQUEST);
                    String mediaTypeName = getMediaTypeName(contentItem.getKey());
                    representation.setMediaType(mediaTypeName);
                    MediaType mediaType = contentItem.getValue();
                    if (mediaType.getExamples() != null) {
                        for (String exampleName : mediaType.getExamples().keySet()) {
                            RestRequest request = restMethod.addNewRequest(exampleName);
                            request.setMediaType(mediaTypeName);
                            if (addRequestContent) {
                                Example example = mediaType.getExamples().get(exampleName);
                                if (example.getExternalValue() != null) {
                                    try {
                                        String externalContent = IOUtils.toString(new URL(example.getExternalValue()), StandardCharsets.UTF_8);
                                        request.setRequestContent(externalContent);
                                    } catch (Exception e) {
                                    }
                                } else if (example.getValue() != null) {
                                    request.setRequestContent(example.getValue().toString());
                                }
                            }
                        }
                    } else {
                        RestRequest request = restMethod.addNewRequest(ModelItemNamer.getUniqueName("Request", restMethod));
                        request.setMediaType(mediaTypeName);
                        if (addRequestContent) {
                            if (mediaType.getExample() != null) {
                                request.setRequestContent(OpenAPIUtils.extractStringFromExampleObject(mediaTypeName, mediaType.getExample()));
                            } else if (mediaType.getSchema() != null) {
                                Map<String, Schema> schemas = getOpenApiSchemas();
                                io.swagger.oas.inflector.examples.models.Example example = ExampleBuilder.fromSchema(mediaType.getSchema(), schemas, ExampleBuilder.RequestType.WRITE);
                                if (example != null) {
                                    request.setRequestContent(serializeExample(mediaTypeName, example));
                                }
                            }
                        }
                    }
                }

                return;
            }
        }

        RestRequest requestWithoutBody = restMethod.addNewRequest(ModelItemNamer.getUniqueName("Request", restMethod));
        requestWithoutBody.setMediaType(defaultMediaType);
    }

    private String serializeExample(String mediaType, io.swagger.oas.inflector.examples.models.Example output) {
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

    private void createRepresentations(RestMethod restMethod, Operation operation) {
        if (operation != null && operation.getResponses() != null) {
            for (String httpStatusCode : operation.getResponses().keySet()) {
                if (httpStatusCode.equals("default") || isRangeOfHttpCodes(httpStatusCode)) {
                    continue;
                }
                ApiResponse response = operation.getResponses().get(httpStatusCode);
                if (response.getContent() != null) {
                    for (Map.Entry<String, MediaType> contentItem : response.getContent().entrySet()) {
                        RestRepresentation representation = restMethod.addNewRepresentation(RestRepresentation.Type.RESPONSE);
                        String mediaTypeName = getMediaTypeName(contentItem.getKey());
                        representation.setMediaType(mediaTypeName);
                        representation.setStatus(Lists.newArrayList(httpStatusCode));
                        representation.setDescription(response.getDescription() != null ? response.getDescription() : "");
                    }
                }
            }
        }
    }

    private boolean isRangeOfHttpCodes(String httpStatusCode) {
        return httpStatusCode.matches("(?i)[1-5]xx");
    }

    private Map<String, Schema> getOpenApiSchemas() {
        if (openAPI.getComponents() != null) {
            Map<String, Schema> schemas = openAPI.getComponents().getSchemas();
            if (schemas != null) {
                return schemas;
            }
        }
        return new HashMap<>();
    }

    private String getMediaTypeName(String mediaTypeFromContent) {
        return StringUtils.isNullOrEmpty(mediaTypeFromContent) ? defaultMediaType : mediaTypeFromContent;
    }

    private void createAuthProfiles() {
        if (openAPI.getComponents() != null && openAPI.getComponents().getSecuritySchemes() != null) {
            Map<String, SecurityScheme> securitySchemeMap = openAPI.getComponents().getSecuritySchemes();
            for (String securityName : securitySchemeMap.keySet()) {
                SecurityScheme securityScheme = securitySchemeMap.get(securityName);
                if (securityScheme != null && securityScheme.getType() != null) {
                    SecurityScheme.Type securitySchemeType = securityScheme.getType();
                    switch (securitySchemeType.toString()) {
                        case "oauth2":
                            //createOauth2Profile(securityName, securityScheme);
                            break;
                        case "apikey":
                            createApiKey(securityScheme);
                            break;
                        case "http":
                            //createHttpAuth(securityName, securityScheme);
                            break;
                    }
                }
            }
        }
    }

    /*private void createOauth2Profile(String securitySchemeName, SecurityScheme securityScheme) {
        AuthRepository authRepository = project.getAuthRepository();
        if (authRepository != null) {
            OAuthFlows oAuthFlows = securityScheme.getFlows();
            if (oAuthFlows != null) {
                if (oAuthFlows.getImplicit() != null) {
                    OAuth2Profile oAuth2Profile = (OAuth2Profile) authRepository.createEntry(AuthEntryTypeConfig.O_AUTH_2_0, securitySchemeName + " IMPLICIT");
                    oAuth2Profile.setOAuth2Flow(OAuth2FlowConfig.IMPLICIT_GRANT);
                    oAuth2Profile.setAuthorizationURI(oAuthFlows.getImplicit().getAuthorizationUrl() == null ? "" : (oAuthFlows.getImplicit().getAuthorizationUrl()));
                    addScopeToProfile(oAuth2Profile, oAuthFlows.getImplicit().getScopes());
                }

                if (oAuthFlows.getAuthorizationCode() != null) {
                    OAuth2Profile oAuth2Profile = (OAuth2Profile) authRepository.createEntry(AuthEntryTypeConfig.O_AUTH_2_0, securitySchemeName + " AUTHORIZATION");
                    oAuth2Profile.setOAuth2Flow(OAuth2FlowConfig.AUTHORIZATION_CODE_GRANT);
                    oAuth2Profile.setAuthorizationURI(oAuthFlows.getAuthorizationCode().getAuthorizationUrl() == null ? "" : (oAuthFlows.getAuthorizationCode().getAuthorizationUrl()));
                    oAuth2Profile.setAccessTokenURI(oAuthFlows.getAuthorizationCode().getTokenUrl() == null ? "" : oAuthFlows.getAuthorizationCode().getTokenUrl());
                    addScopeToProfile(oAuth2Profile, oAuthFlows.getAuthorizationCode().getScopes());
                }

                if (oAuthFlows.getClientCredentials() != null) {
                    OAuth2Profile oAuth2Profile = (OAuth2Profile) authRepository.createEntry(AuthEntryTypeConfig.O_AUTH_2_0, securitySchemeName + " CLIENT CRED");
                    oAuth2Profile.setOAuth2Flow(OAuth2FlowConfig.CLIENT_CREDENTIALS_GRANT);
                    oAuth2Profile.setAccessTokenURI(oAuthFlows.getClientCredentials().getTokenUrl() == null ? "" : oAuthFlows.getClientCredentials().getTokenUrl());
                    addScopeToProfile(oAuth2Profile, oAuthFlows.getClientCredentials().getScopes());
                }

                if (oAuthFlows.getPassword() != null) {
                    OAuth2Profile oAuth2Profile = (OAuth2Profile) authRepository.createEntry(AuthEntryTypeConfig.O_AUTH_2_0, securitySchemeName + " CLIENT PASS");
                    oAuth2Profile.setOAuth2Flow(OAuth2FlowConfig.RESOURCE_OWNER_PASSWORD_CREDENTIALS);
                    oAuth2Profile.setAccessTokenURI(oAuthFlows.getPassword().getTokenUrl() == null ? "" : oAuthFlows.getPassword().getTokenUrl());
                    addScopeToProfile(oAuth2Profile, oAuthFlows.getPassword().getScopes());
                }
            }
        }
    }*/

    private void addScopeToProfile(OAuth2Profile oAuth2Profile, Scopes scopes) {
        if (scopes != null && oAuth2Profile != null) {
            String scopesSummaryString = "";
            for (String scope : scopes.keySet()) {
                scopesSummaryString = scopesSummaryString + scope + " ";
            }
            oAuth2Profile.setScope(scopesSummaryString.trim());
        }
    }

    private void createApiKey(SecurityScheme securityScheme) {
        if (securityScheme != null && securityScheme.getIn() != null) {
            String type = securityScheme.getIn().toString();
            //TODO ADD TO REQUESTS PARAMETERS
            if (type.equalsIgnoreCase("query")) {

            } else if (type.equalsIgnoreCase("header")) {

            }
        }
    }

    /*private void createHttpAuth(String securitySchemeName, SecurityScheme securityScheme) {
        if (securityScheme != null) {
            String scheme = securityScheme.getScheme();
            AuthRepository authRepository = project.getAuthRepository();
            if (scheme != null) {
                if (scheme.equalsIgnoreCase("basic")) {
                    authRepository.createEntry(AuthEntryTypeConfig.BASIC, securitySchemeName + " BASIC");
                }
            }
        }
    }*/
}