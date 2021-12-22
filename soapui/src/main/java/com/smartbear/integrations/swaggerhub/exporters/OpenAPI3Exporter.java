package com.smartbear.integrations.swaggerhub.exporters;

import com.eviware.soapui.config.AuthEntryTypeConfig;
import com.eviware.soapui.config.OAuth2FlowConfig;
import com.eviware.soapui.impl.AuthRepository.AuthEntries.BaseAuthEntry;
import com.eviware.soapui.impl.AuthRepository.AuthRepository;
import com.eviware.soapui.impl.rest.AbstractRestService;
import com.eviware.soapui.impl.rest.OAuth2Profile;
import com.eviware.soapui.impl.rest.RestMethod;
import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.support.StringUtils;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.type.SimpleType;
import com.smartbear.swagger.utils.ApiResponsesSerializer;
import com.smartbear.swagger.utils.OpenAPIUtils;
import com.smartbear.swagger.utils.ResponseCodeSerializer;
import com.smartbear.swagger.utils.YamlFactoryExtended;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.Scopes;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.parser.ObjectMapperFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class OpenAPI3Exporter implements SwaggerExporter {
    private static final String DEFAULT_VERSION = "1.0.0";
    private static final String YAML_EXTENSION = "yaml";

    private final WsdlProject project;

    private OpenAPI openAPI = null;

    public OpenAPI3Exporter(WsdlProject project) {
        this.project = project;
    }

    @Override
    public String exportToFileSystem(String fileName, String apiVersion, String format, AbstractRestService[] services, String basePath) {
        if (!ExportSwaggerAction.shouldOverwriteFileIfExists(fileName, null)) {
            return null;
        }

        openAPI = createOpenAPI(project, services);

        ObjectMapper mapper = format.equals(YAML_EXTENSION) ? createYamlMapper() : ObjectMapperFactory.createJson();

        try {
            mapper.writeValue(new FileWriter(fileName), openAPI);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return fileName;
    }

    /**
     * @param baseInfoModelItem model item with description and name which will be used in openApi info
     * @param services          rest services from where will be filled resources, methods and etc. to OpenApi info
     * @return filled OpenApi object
     */
    public OpenAPI createOpenAPI(ModelItem baseInfoModelItem, AbstractRestService[] services) {
        OpenAPI openAPI = new OpenAPI();
        Info info = new Info();
        info.setVersion(DEFAULT_VERSION);
        info.setDescription(baseInfoModelItem.getDescription() == null
                ? StringUtils.EMPTY
                : baseInfoModelItem.getDescription());
        info.setTitle(baseInfoModelItem.getName());
        openAPI.setInfo(info);

        Paths paths = new Paths();
        for (AbstractRestService restService : services) {
            copyEndpoints(openAPI, restService);
            for (RestResource restResource : restService.getResourceList()) {
                PathItem pathItem = new PathItem();
                for (RestMethod restMethod : restResource.getRestMethodList()) {
                    OpenAPIUtils.addMethodToPath(pathItem, restMethod);
                }
                OpenAPIUtils.copyParametersToPath(pathItem, restResource.getParams());
                pathItem.setDescription(restResource.getDescription() == null
                        ? StringUtils.EMPTY
                        : restResource.getDescription());
                paths.addPathItem(restResource.getFullPath(false), pathItem);
            }
        }
        openAPI.setPaths(paths);
        createSecurityComponent(openAPI);

        return openAPI;
    }

    @Override
    public String getOasVersion() {
        return openAPI == null ? null : openAPI.getOpenapi();
    }

    private ObjectMapper createYamlMapper() {
        ObjectMapper mapper = new ObjectMapper(new YamlFactoryExtended());
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(ApiResponses.class, new ApiResponsesSerializer(null, SimpleType.constructUnsafe(String.class),
                SimpleType.constructUnsafe(ApiResponse.class), false, null, new ResponseCodeSerializer(), null));
        mapper.registerModule(simpleModule);
        mapper.configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }

    private void copyEndpoints(OpenAPI openAPI, AbstractRestService restService) {
        List<Server> serverList = new ArrayList<>();
        for (String endpoint : restService.getEndpoints()) {
            Server server = new Server();
            server.setUrl(endpoint + restService.getBasePath());
            serverList.add(server);
        }
        openAPI.setServers(serverList);
    }

    private void createSecurityComponent(OpenAPI openAPI) {
        AuthRepository authRepository = project.getAuthRepository();
        HashMap<String, SecurityScheme> securitySchemes = new HashMap<>();

        if (authRepository != null && !authRepository.getEntryList().isEmpty()) {
            for (BaseAuthEntry authEntry : project.getAuthRepository().getEntryList()) {
                if (authEntry.getType().equals(AuthEntryTypeConfig.O_AUTH_2_0)) {
                    copyOauthProfile(authEntry, securitySchemes);
                } else if (authEntry.getType().equals(AuthEntryTypeConfig.BASIC)) {
                    createBasicSecurity(authEntry, securitySchemes);
                }
            }
        }

        if (!securitySchemes.isEmpty()) {
            openAPI.setComponents(new Components());
            openAPI.getComponents().setSecuritySchemes(securitySchemes);
        }
    }

    private Scopes extractScopes(OAuth2Profile auth2Profile) {
        Scopes scopes = new Scopes();
        if (StringUtils.hasContent(auth2Profile.getScope())) {
            String[] extractedScopes = auth2Profile.getScope().split(" ");
            for (String scope : extractedScopes) {
                scopes.put(scope, "");
            }
        }
        return scopes;
    }

    private void copyOauthProfile(BaseAuthEntry authEntry, HashMap<String, SecurityScheme> securitySchemes) {
        SecurityScheme securityScheme = new SecurityScheme();
        securityScheme.setType(SecurityScheme.Type.OAUTH2);
        OAuthFlows oAuthFlows = new OAuthFlows();
        OAuth2Profile oAuth2Profile = (OAuth2Profile) authEntry;
        OAuthFlow oAuthFlow = new OAuthFlow();
        OAuth2FlowConfig.Enum entry = oAuth2Profile.getOAuth2Flow();

        if (entry.equals(OAuth2FlowConfig.IMPLICIT_GRANT)) {
            oAuthFlow.setAuthorizationUrl(oAuth2Profile.getAuthorizationURI() == null ? "" : oAuth2Profile.getAuthorizationURI());
            oAuthFlow.setScopes(extractScopes(oAuth2Profile));
            oAuthFlows.setImplicit(oAuthFlow);
        } else if (entry.equals(OAuth2FlowConfig.AUTHORIZATION_CODE_GRANT)) {
            oAuthFlow.setAuthorizationUrl(oAuth2Profile.getAuthorizationURI() == null ? "" : oAuth2Profile.getAuthorizationURI());
            oAuthFlow.setTokenUrl(oAuth2Profile.getAccessTokenURI() == null ? "" : oAuth2Profile.getAccessTokenURI());
            oAuthFlow.setScopes(extractScopes(oAuth2Profile));
            oAuthFlows.setAuthorizationCode(oAuthFlow);
        } else if (entry.equals(OAuth2FlowConfig.CLIENT_CREDENTIALS_GRANT)) {
            oAuthFlow.setTokenUrl(oAuth2Profile.getAccessTokenURI() == null ? "" : oAuth2Profile.getAccessTokenURI());
            oAuthFlow.setScopes(extractScopes(oAuth2Profile));
            oAuthFlows.setClientCredentials(oAuthFlow);
        } else if (entry.equals(OAuth2FlowConfig.RESOURCE_OWNER_PASSWORD_CREDENTIALS)) {
            oAuthFlow.setTokenUrl(oAuth2Profile.getAccessTokenURI() == null ? "" : oAuth2Profile.getAccessTokenURI());
            oAuthFlow.setScopes(extractScopes(oAuth2Profile));
            oAuthFlows.setPassword(oAuthFlow);
        }
        securityScheme.setFlows(oAuthFlows);
        securitySchemes.put(authEntry.getName(), securityScheme);
    }

    private void createBasicSecurity(BaseAuthEntry authEntry, HashMap<String, SecurityScheme> securitySchemes) {
        SecurityScheme securityScheme = new SecurityScheme();
        securityScheme.setType(SecurityScheme.Type.HTTP);
        securityScheme.setScheme("basic");
        securitySchemes.put(authEntry.getName(), securityScheme);
    }
}
