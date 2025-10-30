package com.smartbear.swagger;

import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.support.SoapUIException;
import org.apache.xmlbeans.XmlException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public class ImporterTest {

    protected final File swaggerFile;

    public ImporterTest(File swaggerFile) {
        this.swaggerFile = swaggerFile;
    }

    @Parameters(name = "{0}")
    public static Collection<Object[]> data() throws URISyntaxException {
        List<Object[]> files = new ArrayList<>();
        URL resource = ImporterTest.class.getResource("/swagger-test-files");
        File directory = new File(resource.toURI());
        File[] testFiles = directory.listFiles();
        if (testFiles != null) {
            for (File file : testFiles) {
                if (file.isFile() && (file.getName().endsWith(".json") || file.getName().endsWith(".yaml") || file.getName().endsWith(".yml"))) {
                    files.add(new Object[]{file});
                }
            }
        }
        return files;
    }

    @Test
    public void testImportFile() throws Exception {
        WsdlProject project = new WsdlProject();
        SwaggerImporter importer = SwaggerUtils.importSwaggerFromUrl(project, swaggerFile.getAbsolutePath(), "application/json");

        assertNotNull("Import failed for " + swaggerFile.getName(), importer.importSwagger(swaggerFile.getAbsolutePath()));
    }

    @Test
    public void testEndpointCount() throws Exception {
        // a. Programmatically import an existing OpenAPI specification
        WsdlProject project = new WsdlProject();
        SwaggerImporter importer = SwaggerUtils.importSwaggerFromUrl(project, swaggerFile.getAbsolutePath(), "application/json");
        RestService[] initialServices = importer.importSwagger(swaggerFile.getAbsolutePath());
        assertNotNull("Import failed, returned null", initialServices);
        RestService initialService = initialServices[0];

        // b. Count the number of RestMethod objects
        List<String> initialEndpoints = getEndpointNames(initialService);
        int initialCount = initialEndpoints.size();

        // c. Export the imported service to a temporary OpenAPI file
        File tempFile = File.createTempFile("exported-swagger", ".json");
        tempFile.deleteOnExit();
        Swagger2Exporter exporter = new Swagger2Exporter(project);
        exporter.exportToFileSystem(tempFile.getAbsolutePath(), "1.0", "json", new RestService[]{initialService}, "/");

        // d. Import the temporary file back into a new project
        WsdlProject reimportedProject = new WsdlProject();
        SwaggerImporter reimporter = SwaggerUtils.importSwaggerFromUrl(reimportedProject, tempFile.getAbsolutePath(), "application/json");
        RestService[] reimportedServices = reimporter.importSwagger(tempFile.getAbsolutePath());
        assertNotNull("Re-import failed, returned null", reimportedServices);
        RestService reimportedService = reimportedServices[0];


        // e. Count the number of RestMethod objects in the re-imported service
        List<String> reimportedEndpoints = getEndpointNames(reimportedService);
        int reimportedCount = reimportedEndpoints.size();

        // f. Assert that the initial and re-imported endpoint counts are identical
        String errorMessage = "Endpoint counts do not match. Missing endpoints: " +
                initialEndpoints.stream()
                        .filter(endpoint -> !reimportedEndpoints.contains(endpoint))
                        .collect(java.util.stream.Collectors.joining(", "));
        assertEquals(errorMessage, initialCount, reimportedCount);
    }

    private List<String> getEndpointNames(RestService service) {
        List<String> endpointNames = new ArrayList<>();
        if (service != null) {
            for (com.eviware.soapui.impl.rest.RestResource resource : service.getAllResources()) {
                for (com.eviware.soapui.impl.rest.RestMethod method : resource.getRestMethodList()) {
                    endpointNames.add(method.getMethod().name() + " " + resource.getFullPath());
                }
            }
        }
        return endpointNames;
    }
}
