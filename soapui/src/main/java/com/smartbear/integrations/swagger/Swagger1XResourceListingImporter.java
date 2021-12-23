package com.smartbear.integrations.swagger;

import com.eviware.soapui.analytics.Analytics;
import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.smartbear.swagger4j.ResourceListing;
import com.smartbear.swagger4j.Swagger;

import java.util.ArrayList;
import java.util.List;

public class Swagger1XResourceListingImporter extends AbstractSwagger1XImporter {

    public Swagger1XResourceListingImporter(WsdlProject project, String defaultMediaType) {
        super(project, defaultMediaType);
    }

    public Swagger1XResourceListingImporter(WsdlProject project) {
        super(project);
    }

    @Override
    public RestService[] importSwagger(String url) {
        return importSwagger(url, null);
    }

    @Override
    RestService[] importSwagger(String url, String apiKey) {
        List<RestService> result = new ArrayList<>();

        ResourceListing resourceListing = Swagger.readSwagger(URI.create(url))
        resourceListing.apis.each {

            String name = it.path
            if (name.startsWith("/api-docs")) {
                def ix = name.indexOf("/", 1)
                if (ix > 0)
                    name = name.substring(ix)
            }

            Console.println("Importing API declaration with path $it.path")

            def restService = importApiDeclaration(it.declaration, name)
            ensureEndpoint(restService, url)
            result.add(restService)
        }

        Analytics.trackAction(ReadyApiActions.SWAGGER_OAS_IMPORT_VERSION, "SwaggerOAS_version", resourceListing.swaggerVersion.getIdentifier())
        return result.toArray()
    }
}
