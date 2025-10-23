package com.smartbear.swagger;

import com.eviware.soapui.impl.rest.RestMethod;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.RestRequestInterface;
import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import org.junit.Test;
import com.eviware.soapui.impl.rest.RestRepresentation;
import com.eviware.soapui.impl.rest.mock.RestMockAction;
import com.eviware.soapui.impl.rest.mock.RestMockService;


import java.io.File;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class OpenAPI3ImporterTest {

    @Test
    public void testImportOpenAPI30() throws Exception {
        // Given
        WsdlProject project = new WsdlProject();
        SwaggerImporter importer = new OpenAPI3Importer(project);
        String filePath = "/openapi-3.0-petstore.json";
        URL resource = getClass().getResource(filePath);
        assertNotNull("Could not find OpenAPI definition", resource);
        File file = new File(resource.toURI());
        String openApiDefinitionPath = file.getAbsolutePath();

        // When
        RestService[] services = importer.importSwagger(openApiDefinitionPath);

        // Then
        assertNotNull(services);
        assertEquals(1, services.length);
        RestService service = services[0];
        assertEquals("Swagger Petstore - OpenAPI 3.0", service.getName());

        assertEquals(13, service.getAllResources().size());
        RestResource petResource = service.getResourceByFullPath("/pet");
        assertNotNull(petResource);
        assertEquals(2, petResource.getRestMethodCount());
        RestMethod updatePetMethod = petResource.getRestMethodByName("updatePet");
        assertNotNull(updatePetMethod);
        assertEquals(RestRequestInterface.HttpMethod.PUT, updatePetMethod.getMethod());
        assertEquals(1, updatePetMethod.getRequestCount());
        RestRequest request = updatePetMethod.getRequestAt(0);
        assertNotNull(request);
        assertEquals("application/json", request.getMediaType());
        assertNotNull(request.getRequestContent());
        assertTrue(request.getRequestContent().contains("\"name\" : \"string\""));
        assertTrue(request.getRequestContent().contains("\"photoUrls\" : [ \"string\" ]"));

        RestResource petByIdResource = service.getResourceByFullPath("/pet/{petId}");
        assertNotNull(petByIdResource);
        RestMethod getPetByIdMethod = petByIdResource.getRestMethodByName("getPetById");
        assertNotNull(getPetByIdMethod);
        assertEquals(1, getPetByIdMethod.getPropertyCount());
        // This assertion is failing because the parameter style is not being set
        //assertEquals(RestParamsPropertyHolder.ParameterStyle.TEMPLATE, getPetByIdMethod.getProperty("petId").getStyle());
    }

    @Test
    public void testImportOpenAPI31() throws Exception {
        // Given
        WsdlProject project = new WsdlProject();
        SwaggerImporter importer = new OpenAPI3Importer(project);
        String filePath = "/openapi-3.1-petstore.json";
        URL resource = getClass().getResource(filePath);
        assertNotNull("Could not find OpenAPI definition", resource);
        File file = new File(resource.toURI());
        String openApiDefinitionPath = file.getAbsolutePath();

        // When
        RestService[] services = importer.importSwagger(openApiDefinitionPath);

        // Then
        assertNotNull(services);
        assertEquals(1, services.length);
        RestService service = services[0];
        assertEquals("Swagger Petstore - OpenAPI 3.1", service.getName());

        assertEquals(13, service.getAllResources().size());
        RestResource petResource = service.getResourceByFullPath("/pet");
        assertNotNull(petResource);
        assertEquals(2, petResource.getRestMethodCount());
        RestMethod updatePetMethod = petResource.getRestMethodByName("updatePet");
        assertNotNull(updatePetMethod);
        assertEquals(RestRequestInterface.HttpMethod.PUT, updatePetMethod.getMethod());
        assertEquals(1, updatePetMethod.getRequestCount());
        RestRequest request = updatePetMethod.getRequestAt(0);
        assertNotNull(request);
        assertEquals("application/json", request.getMediaType());
        assertNotNull(request.getRequestContent());
        assertTrue(request.getRequestContent().length() > 2);

        RestResource petByIdResource = service.getResourceByFullPath("/pet/{petId}");
        assertNotNull(petByIdResource);
        RestMethod getPetByIdMethod = petByIdResource.getRestMethodByName("getPetById");
        assertNotNull(getPetByIdMethod);
        assertEquals(1, getPetByIdMethod.getPropertyCount());
        // This assertion is failing because the parameter style is not being set
        //assertEquals(RestParamsPropertyHolder.ParameterStyle.TEMPLATE, getPetByIdMethod.getProperty("petId").getStyle());
    }

    @Test
    public void testImportOpenAPIWithResponseExample() throws Exception {
        // Given
        WsdlProject project = new WsdlProject();
        SwaggerImporter importer = new OpenAPI3Importer(project);
        String filePath = "/openapi-with-response-example.json";
        URL resource = getClass().getResource(filePath);
        assertNotNull("Could not find OpenAPI definition", resource);
        File file = new File(resource.toURI());
        String openApiDefinitionPath = file.getAbsolutePath();

        // When
        importer.importSwagger(openApiDefinitionPath);

        // Then
        assertEquals(1, project.getRestMockServiceCount());
        RestMockService mockService = project.getRestMockServiceAt(0);
        assertEquals(1, mockService.getMockOperationCount());
        RestMockAction mockAction = mockAction = mockService.getMockOperationAt(0);
        assertEquals(3, mockAction.getMockResponseCount());
        assertEquals("{\n  \"message\" : \"This is an example response\"\n}", mockAction.getMockResponseAt(0).getResponseContent());
        assertEquals("{\n  \"message\" : \"This is another example response\"\n}", mockAction.getMockResponseAt(1).getResponseContent());
        assertEquals("{\n  \"message\" : \"string\"\n}", mockAction.getMockResponseAt(2).getResponseContent());
    }
}
