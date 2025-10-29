package com.smartbear.swagger;

import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.support.SoapUIException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.swagger.models.Swagger;
import io.swagger.parser.SwaggerParser;
import org.apache.xmlbeans.XmlException;

import java.io.File;
import java.io.IOException;

public class SwaggerUtils {
    public static boolean isOpenApi(String filePath) {
        return getOpenApiVersion(filePath) != null;
    }

    public static String getOpenApiVersion(String filePath) {
        try {
            ObjectMapper mapper;
            if (filePath.toLowerCase().endsWith(".yaml") || filePath.toLowerCase().endsWith(".yml")) {
                mapper = new ObjectMapper(new YAMLFactory());
            } else {
                mapper = new ObjectMapper();
            }
            JsonNode rootNode = mapper.readTree(new File(filePath));
            if (rootNode.has("openapi")) {
                return rootNode.get("openapi").asText();
            } else {
                return null;
            }
        } catch (IOException e) {
            return null;
        }
    }


    public static SwaggerImporter importSwaggerFromUrl(WsdlProject project, String url, String defaultMediaType) throws Exception {
        SwaggerImporter importer;
        String openApiVersion = getOpenApiVersion(url);
        if (openApiVersion != null) {
            if (openApiVersion.startsWith("3.1")) {
                importer = new OpenAPI31Importer(project);
            } else {
                importer = new OpenAPI3Importer(project);
            }
        } else {
            importer = new Swagger2Importer(project);
        }
        importer.importSwagger(url);
        return importer;
    }

    public static Swagger getSwagger(String definition, String auth, boolean openApi, boolean b) {
        return new SwaggerParser().read(definition);
    }
}
