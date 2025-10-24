package com.smartbear.swagger;

import com.eviware.soapui.impl.rest.RestMethod;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import org.junit.Test;

import java.io.File;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ImporterTest {

    @Test
    public void testImportSwaggerWithDefaultPayload() throws Exception {
        // Given
        WsdlProject project = new WsdlProject();
        Swagger2Importer importer = new Swagger2Importer(project);
        String filePath = "/swagger-definition-without-body-parameter.json";
        URL resource = getClass().getResource(filePath);
        assertNotNull("Could not find swagger definition", resource);
        File file = new File(resource.toURI());
        String swaggerDefinitionPath = file.getAbsolutePath();

        // When
        RestService[] services = importer.importSwagger(swaggerDefinitionPath);
        RestService service = services[0];
        RestResource restResource = (RestResource) service.getOperationList().get(0);
        RestMethod method = restResource.getRestMethodAt(0);
        RestRequest request = method.getRequestList().get(0);

        // Then
        assertNotNull(request);
        assertEquals("{}", request.getRequestContent());
        assertEquals("application/json", request.getMediaType());
    }

    @Test
    public void testImportSwaggerWithMultipleConsumes() throws Exception {
        // Given
        WsdlProject project = new WsdlProject();
        Swagger2Importer importer = new Swagger2Importer(project);
        String filePath = "/swagger-definition-with-multiple-consumes.json";
        URL resource = getClass().getResource(filePath);
        assertNotNull("Could not find swagger definition", resource);
        File file = new File(resource.toURI());
        String swaggerDefinitionPath = file.getAbsolutePath();

        // When
        RestService[] services = importer.importSwagger(swaggerDefinitionPath);
        RestService service = services[0];
        RestResource restResource = (RestResource) service.getOperationList().get(0);
        RestMethod method = restResource.getRestMethodAt(0);

        // Then
        assertEquals(2, method.getRequestList().size());
        assertEquals("Request 1", method.getRequestList().get(0).getName());
        assertEquals("application/json", method.getRequestList().get(0).getMediaType());
        assertEquals("Request 2", method.getRequestList().get(1).getName());
        assertEquals("application/xml", method.getRequestList().get(1).getMediaType());
    }

    @Test
    public void testImportOpenApi3() throws Exception {
        // Given
        WsdlProject project = new WsdlProject();
        OpenAPI3Importer importer = new OpenAPI3Importer(project);
        String filePath = "/petstore-openapi-3.0.json";
        URL resource = getClass().getResource(filePath);
        assertNotNull("Could not find swagger definition", resource);
        File file = new File(resource.toURI());
        String swaggerDefinitionPath = file.getAbsolutePath();

        // When
        RestService[] services = importer.importSwagger(swaggerDefinitionPath);
        RestService service = services[0];
        assertEquals("Swagger Petstore", service.getName());
        assertEquals(2, service.getOperationCount());
    }
}
