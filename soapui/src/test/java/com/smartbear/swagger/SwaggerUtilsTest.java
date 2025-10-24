package com.smartbear.swagger;

import com.eviware.soapui.impl.wsdl.WsdlProject;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class SwaggerUtilsTest {

    @Test
    public void testCreateSwagger2Importer() throws Exception {
        WsdlProject project = new WsdlProject();
        String filePath = getClass().getResource("/petstore-swagger-2.0.json").getFile();
        SwaggerImporter importer = SwaggerUtils.createSwaggerImporter(filePath, project);
        assertTrue(importer instanceof Swagger2Importer);
    }

    @Test
    public void testCreateOpenAPI3Importer() throws Exception {
        WsdlProject project = new WsdlProject();
        String filePath = getClass().getResource("/petstore-openapi-3.0.json").getFile();
        SwaggerImporter importer = SwaggerUtils.createSwaggerImporter(filePath, project);
        assertTrue(importer instanceof OpenAPI3Importer);
    }
}
