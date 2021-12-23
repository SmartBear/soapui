package com.smartbear.swagger

import com.eviware.soapui.analytics.Analytics
import com.eviware.soapui.impl.rest.RestService
import com.eviware.soapui.impl.wsdl.WsdlProject
import com.smartbear.swagger.AbstractSwagger1XImporter
import com.smartbear.swagger4j.Swagger

public class Swagger1XApiDeclarationImporter extends AbstractSwagger1XImporter {

    public Swagger1XApiDeclarationImporter(WsdlProject project, String defaultMediaType) {
        super(project, defaultMediaType)
    }

    public Swagger1XApiDeclarationImporter(WsdlProject project) {
        super(project)
    }

    @Override
    public RestService[] importSwagger(String url) {
        return importSwagger(url, null)
    }

    @Override
    RestService[] importSwagger(String url, String apiKey) {
        def declaration = Swagger.createReader().readApiDeclaration(URI.create(url))
        def name = declaration.basePath == null ? url : declaration.basePath

        def restService = importApiDeclaration(declaration, name);

        ensureEndpoint(restService, url)

        Analytics.trackAction(ReadyApiActions.SWAGGER_OAS_IMPORT_VERSION, "SwaggerOAS_version", declaration.swaggerVersion.getIdentifier())
        return restService
    }
}
