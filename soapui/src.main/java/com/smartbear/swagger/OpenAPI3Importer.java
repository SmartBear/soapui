package com.smartbear.swagger;

import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;

public class OpenAPI3Importer implements SwaggerImporter {
    private final WsdlProject project;

    public OpenAPI3Importer(WsdlProject project) {
        this.project = project;
    }

    @Override
    public RestService[] importSwagger(String url) {
        return importSwagger(url, null);
    }

    @Override
    public RestService[] importSwagger(String url, String apiKey) {
        OpenAPI openAPI = new OpenAPIV3Parser().read(url);
        if (openAPI == null) {
            return new RestService[0];
        }

        RestService service = project.addNewRestService(openAPI.getInfo().getTitle());
        return new RestService[]{service};
    }
}
