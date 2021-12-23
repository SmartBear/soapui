package com.eviware.soapui.impl.support.definition.support;

import com.eviware.soapui.config.DefinitionCacheConfig;
import com.eviware.soapui.config.DefintionPartConfig;
import com.eviware.soapui.config.InterfaceConfig;
import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.impl.rest.RestServiceEx;
import com.eviware.soapui.impl.support.AbstractInterface;
import com.eviware.soapui.impl.support.definition.DefinitionLoader;
import com.eviware.soapui.impl.support.definition.InterfaceDefinitionPart;
import com.eviware.soapui.impl.swagger.support.GeneratedOpenAPIv3DefinitionLoader;
import com.eviware.soapui.impl.swagger.support.ReadyApiOpenAPIParser;
import com.eviware.soapui.impl.swagger.support.ReadyApiSwaggerParseResult;
import com.eviware.soapui.impl.wsdl.support.Constants;
import com.eviware.soapui.model.iface.Operation;
import com.eviware.soapui.support.StringUtils;
import com.smartbear.swagger.OpenAPI3Importer;
import com.smartbear.swagger.utils.OpenAPIUtils;
import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.core.models.AuthorizationValue;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import org.w3c.dom.Node;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class OpenAPIv3DefinitionCache<T extends AbstractInterface<?, ? extends Operation>> extends CommonDefinitionCache<T> {
    public OpenAPIv3DefinitionCache(T iface) {
        super(iface.getConfig().getDefinitionCache(), iface);
        setDefinitionType(Constants.OPENAPI_V3);
    }

    /**
     * update existing cache according to existing service
     */
    public void synchronizedWithService() {
        InterfaceDefinitionPart rootDefinitionPart = getRootPart();
        if (rootDefinitionPart == null) {
            return;
        }
        String partUrl = rootDefinitionPart.getUrl();
        String partType = rootDefinitionPart.getType();
        String partContent = rootDefinitionPart.getContent();

        OpenAPIParser parser = new ReadyApiOpenAPIParser();
        ParseOptions options = new ParseOptions();
        SwaggerParseResult parseResult = parser.readContents(partContent, null, options);
        OpenAPI openAPI = parseResult.getOpenAPI();
        if (!(container instanceof RestServiceEx)) {
            return;
        }
        RestServiceEx restServiceEx = (RestServiceEx) container;
        List<RestResource> resources = restServiceEx.getAllResources();
        OpenAPIUtils.synchronizeResources(resources, openAPI);
        String resultJson = OpenAPIUtils.getOpenApiJson(openAPI);

        DefinitionCacheConfig definitionCacheConfig = getConfig();
        // save into config
        int count = definitionCacheConfig.sizeOfPartArray();
        DefintionPartConfig partConfig = null;
        for (int index = 0; index < count; index++) {
            partConfig = definitionCacheConfig.getPartArray(index);
            if (StringUtils.sameString(partConfig.getUrl(), partUrl)) {
                definitionCacheConfig.removePart(index);

                DefintionPartConfig updatedDefinitionPart = definitionCacheConfig.addNewPart();
                updatedDefinitionPart.setUrl(partUrl);
                Node newDomNode = updatedDefinitionPart.addNewContent().getDomNode();
                newDomNode.appendChild(newDomNode.getOwnerDocument().createTextNode(resultJson));
                updatedDefinitionPart.setType(partType);
                break;
            }
        }

        reinitParts(definitionCacheConfig);
    }

    @Override
    protected Map<String, String> getDefinitionParts(DefinitionLoader loader) {
        Map<String, String> result = new LinkedHashMap<>();
        String location = loader.getBaseURI();
        if (location == null) {
            return null;
        }
        location = location.replaceAll("\\\\", "/");

        try {
            OpenAPIParser parser = new ReadyApiOpenAPIParser();
            ParseOptions options = new ParseOptions();
            boolean resolveFully = Boolean.parseBoolean(System.getProperty("soapui.swagger.resolvefully", "true"));
            options.setResolveFully(resolveFully);
            options.setResolve(true);
            List<AuthorizationValue> auths = OpenAPI3Importer.authorizationValue != null ?
                    Arrays.asList(OpenAPI3Importer.authorizationValue) : null;
            ReadyApiSwaggerParseResult parseResult;
            if (loader instanceof GeneratedOpenAPIv3DefinitionLoader) {
                parseResult = ReadyApiSwaggerParseResult.create(
                        parser.readContents(loader.loadDefinition(location), auths, options));
            } else {
                parseResult = (ReadyApiSwaggerParseResult) parser.readLocation(location, auths, options);
            }
            if ((parseResult != null) && (parseResult.getOpenAPI() != null)) {
                result.put(loader.getBaseURI(), loader.loadDefinition(location));
                Map<String, String> externalFileCache = parseResult.getExternalFiles();
                externalFileCache.entrySet().stream().forEach(entry -> result.put(entry.getKey(), entry.getValue()));
            }
        } catch (Exception ignored) {
        }

        return result;
    }

    @Override
    protected DefinitionCacheConfig reinit(T iface) {
        InterfaceConfig config = iface.getConfig();
        if (config.isSetDefinitionCache()) {
            config.unsetDefinitionCache();
        }

        return config.addNewDefinitionCache();
    }
}
