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

import com.eviware.soapui.impl.rest.mock.RestMockAction;
import com.eviware.soapui.impl.rest.mock.RestMockService;

public class Swagger2ImporterTest {

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
    public void testImportSwaggerWithResponseExample() throws Exception {
        // Given
        WsdlProject project = new WsdlProject();
        Swagger2Importer importer = new Swagger2Importer(project);
        String filePath = "/swagger-with-response-example.json";
        URL resource = getClass().getResource(filePath);
        assertNotNull("Could not find swagger definition", resource);
        File file = new File(resource.toURI());
        String swaggerDefinitionPath = file.getAbsolutePath();

        // When
        importer.importSwagger(swaggerDefinitionPath);

        // Then
        assertEquals(1, project.getRestMockServiceCount());
        RestMockService mockService = project.getRestMockServiceAt(0);
        assertEquals(1, mockService.getMockOperationCount());
        RestMockAction mockAction = mockService.getMockOperationAt(0);
        assertEquals(3, mockAction.getMockResponseCount());
        assertEquals("{\n  \"message\" : \"This is an example response\"\n}", mockAction.getMockResponseAt(0).getResponseContent());
        assertEquals("{\n  \"message\" : \"This is another example response\"\n}", mockAction.getMockResponseAt(1).getResponseContent());
        assertEquals("{\n  \"message\" : \"string\"\n}", mockAction.getMockResponseAt(2).getResponseContent());
    }
}
