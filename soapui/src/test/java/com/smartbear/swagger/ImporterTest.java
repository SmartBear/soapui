package com.smartbear.swagger;

import com.eviware.soapui.impl.rest.RestMethod;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.rest.mock.RestMockAction;
import com.eviware.soapui.impl.rest.mock.RestMockResponse;
import com.eviware.soapui.impl.rest.mock.RestMockService;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.teststeps.PropertyTransfersTestStep;
import com.smartbear.swagger.utils.PropertyTransferDiscovery;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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

    @Test
    public void testImportLargeSwaggerDefinition() throws Exception {
        // Given
        WsdlProject project = new WsdlProject();
        Swagger2Importer importer = new Swagger2Importer(project);
        String filePath = "/petstore-swagger-2.0.large.json";
        URL resource = getClass().getResource(filePath);
        assertNotNull("Could not find swagger definition", resource);
        File file = new File(resource.toURI());
        String swaggerDefinitionPath = file.getAbsolutePath();

        // When
        RestService[] services = importer.importSwagger(swaggerDefinitionPath);

        // Then
        assertNotNull(services);
        assertNotNull(services[0]);
    }

    @Test
    public void testCreatesMocksFromExamples() throws Exception {
        WsdlProject project = new WsdlProject();
        Swagger2Importer importer = new Swagger2Importer(project);
        String filePath = "/swagger-with-examples.json";
        URL resource = getClass().getResource(filePath);
        assertNotNull("Could not find swagger definition", resource);
        File file = new File(resource.toURI());
        String swaggerDefinitionPath = file.getAbsolutePath();

        RestMockService mockService = project.addNewRestMockService("mock");

        RestService[] services = importer.importSwagger(swaggerDefinitionPath);

        assertEquals("Should have 1 service", 1, services.length);
        assertEquals("Should have 1 mock action", 1, mockService.getMockOperationCount());
        RestMockAction mockAction = mockService.getMockOperationAt(0);
        assertEquals("Should have 4 mock responses", 4, mockAction.getMockResponseCount());
        RestMockResponse response = mockAction.getMockResponseAt(0);
        assertEquals("application/json", response.getMediaType());
        assertTrue("Should contain the example", response.getResponseContent().contains("John Doe"));
    }

    @Test
    public void testIntelligentTestCaseGeneration() throws Exception {
        // Given
        WsdlProject project = new WsdlProject();
        OpenAPI3Importer importer = new OpenAPI3Importer(project);
        String filePath = "/simple-api-for-testing.json";
        URL resource = getClass().getResource(filePath);
        assertNotNull("Could not find swagger definition", resource);
        File file = new File(resource.toURI());
        String swaggerDefinitionPath = file.getAbsolutePath();
        RestService[] services = importer.importSwagger(swaggerDefinitionPath);

        // When
        PropertyTransferDiscovery discovery = new PropertyTransferDiscovery();
        List<PropertyTransferDiscovery.PropertyTransfer> transfers = discovery.discoverPropertyTransfers(importer.getOpenApi());

        // Then
        assertEquals(1, transfers.size());
        assertEquals("createAccount", transfers.get(0).sourceOperationId);
        assertEquals("getAccount", transfers.get(0).targetOperationId);
        assertEquals("accountId", transfers.get(0).propertyName);

        // When
        List<String> selectedOperations = new ArrayList<>();
        selectedOperations.add("createAccount");
        selectedOperations.add("getAccount");

        importer.createTestCase(Arrays.asList(services), selectedOperations, transfers);

        // Then
        assertEquals(1, project.getTestSuiteCount());
        assertEquals(1, project.getTestSuiteByName("Generated Test Suite").getTestCaseCount());
        assertEquals(3, project.getTestSuiteByName("Generated Test Suite").getTestCaseByName("Generated Test Case").getTestStepCount());
        PropertyTransfersTestStep transferStep = (PropertyTransfersTestStep) project.getTestSuiteByName("Generated Test Suite").getTestCaseByName("Generated Test Case").getTestStepByName("Property Transfer");
        assertNotNull(transferStep);
        assertEquals(1, transferStep.getTransferCount());
        assertEquals("createAccount", transferStep.getTransferAt(0).getSourceStepName());
        assertEquals("getAccount", transferStep.getTransferAt(0).getTargetStepName());
        assertEquals("Response", transferStep.getTransferAt(0).getSourcePath());
        assertEquals("accountId", transferStep.getTransferAt(0).getTargetPath());
    }
}
