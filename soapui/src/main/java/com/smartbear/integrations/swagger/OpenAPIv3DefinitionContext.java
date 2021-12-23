package com.smartbear.integrations.swagger;

import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.support.definition.DefinitionCache;
import com.eviware.soapui.impl.support.definition.DefinitionLoader;
import com.eviware.soapui.impl.support.definition.support.AbstractDefinitionContext;
import com.eviware.soapui.impl.support.definition.support.OpenAPIv3DefinitionCache;
import com.eviware.soapui.impl.support.definition.support.ProjectCacheDefinitionLoader;
import com.eviware.soapui.impl.swagger.support.GeneratedOpenAPIv3DefinitionLoader;
import com.eviware.soapui.impl.swagger.support.OpenAPIv3InterfaceDefinition;
import com.eviware.soapui.impl.wsdl.support.wsdl.UrlWsdlLoader;
import com.eviware.soapui.support.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class OpenAPIv3DefinitionContext extends AbstractDefinitionContext<RestServiceEx, DefinitionLoader, OpenAPIv3InterfaceDefinition> {
    private final static Logger log = LogManager.getLogger(OpenAPIv3DefinitionContext.class);

    public OpenAPIv3DefinitionContext(String url, RestServiceEx iface) {
        super(url, iface);
    }

    public OpenAPIv3DefinitionContext(String url) {
        super(url);
    }

    @Override
    public String export(String path) throws Exception {
        return null;
    }

    @Override
    public void regenerate() {
        try {
            DefinitionCache definitionCache = getDefinitionCache();
            OpenAPIv3DefinitionCache openAPIv3DefinitionCache = (OpenAPIv3DefinitionCache) definitionCache;
            openAPIv3DefinitionCache.synchronizedWithService();
            cacheDefinition(definitionCache);
        } catch (Exception exception) {
            log.error(exception.getMessage());
        }
    }

    @Override
    protected DefinitionLoader createDefinitionLoader(DefinitionCache definitionCache) {
        RestServiceEx restServiceEx = getInterface();
        String definitionUrl = restServiceEx == null ? StringUtils.EMPTY : restServiceEx.getDefinitionUrl();
        if (shouldUseGeneratedDefinitionLoader(restServiceEx, definitionUrl)) {
            return new GeneratedOpenAPIv3DefinitionLoader(restServiceEx);
        } else {
            return new ProjectCacheDefinitionLoader(definitionCache);
        }
    }

    @Override
    protected DefinitionLoader createDefinitionLoader(String url) {
        RestServiceEx restServiceEx = getInterface();
        if (shouldUseGeneratedDefinitionLoader(restServiceEx, url)) {
            return new GeneratedOpenAPIv3DefinitionLoader(restServiceEx);
        } else {
            return new UrlWsdlLoader(url, restServiceEx);
        }
    }

    private boolean shouldUseGeneratedDefinitionLoader(RestServiceEx restServiceEx, String url) {
        return restServiceEx != null && (restServiceEx.isGenerated() || StringUtils.isNullOrEmpty(url) || restServiceEx.exportChanges());
    }

    @Override
    protected OpenAPIv3InterfaceDefinition loadDefinition(DefinitionLoader loader) throws Exception {
        return new OpenAPIv3InterfaceDefinition(getInterface()).load(loader);
    }

    @Override
    protected DefinitionCache createDefinitionCache() {
        return new OpenAPIv3DefinitionCache<>(getInterface());
    }

    @Override
    protected void cacheDefinition(DefinitionCache cache) throws Exception {
        super.cacheDefinition(cache);
        if (definition != null) {
            definition.load(currentLoader);
        }
    }
}
