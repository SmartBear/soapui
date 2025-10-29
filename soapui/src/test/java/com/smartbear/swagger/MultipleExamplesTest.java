package com.smartbear.swagger;

import com.eviware.soapui.impl.rest.RestMethod;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class MultipleExamplesTest {

    @Test
    public void testMultipleExamples() throws Exception {
        WsdlProject project = new WsdlProject();
        URL resource = ImporterTest.class.getResource("/swagger-test-files/multiple-examples.json");
        File swaggerFile = new File(resource.toURI());

        OpenAPI3Importer importer = new OpenAPI3Importer(project);
        RestService[] services = importer.importSwagger(swaggerFile.getAbsolutePath());

        assertNotNull(services);
        assertEquals(1, services.length);

        RestService service = services[0];
        assertEquals(2, service.getAllResources().size());

        Optional<RestResource> multipleExamplesResourceOptional = service.getAllResources().stream()
                .filter(r -> r.getName().equals("/multiple-examples")).findFirst();
        assertTrue(multipleExamplesResourceOptional.isPresent());
        RestResource multipleExamplesResource = multipleExamplesResourceOptional.get();
        assertEquals(1, multipleExamplesResource.getRestMethodCount());
        RestMethod multipleExamplesMethod = multipleExamplesResource.getRestMethodAt(0);
        assertEquals(2, multipleExamplesMethod.getRequestCount());

        RestRequest example1Request = multipleExamplesMethod.getRequestByName("example1");
        assertNotNull(example1Request);
        assertEquals("{\"id\":1,\"name\":\"Example 1\"}", example1Request.getRequestContent());

        RestRequest example2Request = multipleExamplesMethod.getRequestByName("example2");
        assertNotNull(example2Request);
        assertEquals("{\"id\":2,\"name\":\"Example 2\"}", example2Request.getRequestContent());

        Optional<RestResource> noExamplesResourceOptional = service.getAllResources().stream()
                .filter(r -> r.getName().equals("/no-examples")).findFirst();
        assertTrue(noExamplesResourceOptional.isPresent());
        RestResource noExamplesResource = noExamplesResourceOptional.get();
        assertEquals(1, noExamplesResource.getRestMethodCount());
        RestMethod noExamplesMethod = noExamplesResource.getRestMethodAt(0);
        assertEquals(1, noExamplesMethod.getRequestCount());

        RestRequest noExampleRequest = noExamplesMethod.getRequestAt(0);
        assertNotNull(noExampleRequest);
        assertEquals("{\"name\":\"string\",\"id\":0}", noExampleRequest.getRequestContent());
    }
}
