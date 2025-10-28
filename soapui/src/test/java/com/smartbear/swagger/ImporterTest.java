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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public class ImporterTest {

    private final File swaggerFile;

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
    public void testImportFile() throws IOException, SoapUIException, XmlException {
        WsdlProject project = new WsdlProject();
        RestService[] services;

        if (SwaggerUtils.isOpenApi(swaggerFile.getAbsolutePath())) {
            OpenAPI3Importer importer = new OpenAPI3Importer(project);
            services = importer.importSwagger(swaggerFile.getAbsolutePath());
        } else {
            Swagger2Importer importer = new Swagger2Importer(project);
            services = importer.importSwagger(swaggerFile.getAbsolutePath());
        }

        assertNotNull("Import failed for " + swaggerFile.getName(), services);
        assertTrue("No services imported for " + swaggerFile.getName(), services.length > 0);
    }
}
