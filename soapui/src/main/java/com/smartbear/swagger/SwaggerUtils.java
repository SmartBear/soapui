package com.smartbear.swagger;

import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.support.wsdl.UrlWsdlLoader;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.x.dialogs.Worker;
import com.eviware.x.dialogs.XProgressDialog;
import com.eviware.x.dialogs.XProgressMonitor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import groovy.json.JsonSlurper;
import io.swagger.models.Swagger;
import io.swagger.models.auth.AuthorizationValue;
import io.swagger.parser.SwaggerParser;
import io.swagger.parser.util.ClasspathHelper;
import io.swagger.parser.util.DeserializationUtils;
import io.swagger.parser.util.SwaggerDeserializationResult;
import io.swagger.util.Json;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

class SwaggerUtils {
    private static final Logger logger = LogManager.getLogger(SwaggerUtils.class);

    public static final String DEFAULT_MEDIA_TYPE = "application/json";
    public static final boolean DEFAULT_FOR_CREATE_TEST_CASE = false;

    /**
     * Selects the appropriate SwaggerImporter for the specified URL. For .yaml urls the Swagger2Importer
     * is returned. For .xml urls the Swagger1Importer is returned. For other urls the Swagger2Importer will be
     * returned if the file is json and contains a root attribute named "swagger" or "swaggerVersion" with the
     * value of 2.0.
     *
     * @param url
     * @param project
     * @param defaultMediaType
     * @return the corresponding SwaggerImporter based on the described "algorithm"
     */

    static SwaggerImporter createSwaggerImporter(String url, WsdlProject project, String defaultMediaType,
                                                 boolean generateTestCase) throws Exception {
        if (url.endsWith(".yaml") || url.endsWith(".yml")) {
            return new Swagger2Importer(project, defaultMediaType);
        }

        UrlWsdlLoader loader = new UrlWsdlLoader(url);
        Object json = new JsonSlurper().parse(loader.load());

        if (json instanceof Map){
            Map mapJson  = (Map)json;
            Object swagger = mapJson.get("swagger");
            Object swaggerVersion = mapJson.get("swaggerVersion");
            if("2.0".equals(swaggerVersion) || "2.0".equals(swagger)) {
                return new Swagger2Importer(project, defaultMediaType);
            }
        }
        return null;
    }

    static SwaggerImporter createSwaggerImporter(String url, WsdlProject project, String defaultMediaType) throws Exception {
        return createSwaggerImporter(url, project, defaultMediaType, DEFAULT_FOR_CREATE_TEST_CASE);
    }

    static SwaggerImporter createSwaggerImporter(String url, WsdlProject project) throws Exception {
        return createSwaggerImporter(url, project, DEFAULT_MEDIA_TYPE,
                DEFAULT_FOR_CREATE_TEST_CASE);
    }

    @Deprecated
    static SwaggerImporter createSwaggerImporter(String url, WsdlProject project, boolean removedParameterForRefactoring) throws Exception {
        return createSwaggerImporter(url, project);
    }

    static SwaggerImporter importSwaggerFromUrl(
            final WsdlProject project, final String finalExpUrl) throws Exception {
        return importSwaggerFromUrl(project, finalExpUrl, DEFAULT_MEDIA_TYPE);
    }

    static boolean isOpenApi(String location) {
        String data;
        try {
            location = location.replaceAll("\\\\", "/");
            if (location.toLowerCase().startsWith("http")) {
                UrlWsdlLoader loader = new UrlWsdlLoader(location);
                data = IOUtils.toString(loader.load());
            } else {
                final String fileScheme = "file:";
                Path path;
                if (location.toLowerCase().startsWith(fileScheme)) {
                    path = Paths.get(URI.create(location));
                } else {
                    path = Paths.get(location);
                }
                if (Files.exists(path)) {
                    data = FileUtils.readFileToString(path.toFile(), "UTF-8");
                } else {
                    data = ClasspathHelper.loadFileFromClasspath(location);
                }
            }
            JsonNode rootNode;
            if (data.trim().startsWith("{")) {
                ObjectMapper mapper = Json.mapper();
                rootNode = mapper.readTree(data);
            } else {
                SwaggerDeserializationResult result  = new SwaggerDeserializationResult();
                rootNode = DeserializationUtils.readYamlTree(data, result);
                logErrors(result);
            }
            JsonNode openapiNode = rootNode.get("openapi");
            return openapiNode != null;
        }
        catch (Exception e) {
            return false;
        }
    }

    static SwaggerImporter importSwaggerFromUrl(final WsdlProject project,
                                                final String finalExpUrl,
                                                final String defaultMediaType) throws Exception {

        final SwaggerImporter importer = SwaggerUtils.createSwaggerImporter(finalExpUrl, project, defaultMediaType);

        XProgressDialog dlg = UISupport.getDialogs().createProgressDialog("Importing Swagger", 0, "", false);
        dlg.run(new Worker.WorkerAdapter() {
            @Override
            public Object construct(XProgressMonitor xProgressMonitor) {
                // create the importer and import!
                List<RestService> result = new ArrayList<RestService>();
                try {
                    result.addAll(Arrays.asList(importer.importSwagger(finalExpUrl)));

                    // select the first imported REST Service (since a swagger definition can
                    // define multiple APIs
                    if (!result.isEmpty()) {
                        UISupport.selectAndShow(result.get(0));
                    }
                }
                catch (Throwable t) {
                    UISupport.showErrorMessage(t);
                }
                return null;
            }
        });

        return importer;
    }

    public static boolean matchesPath(String path, String swaggerPath) {

        String[] pathSegments = path.split("\\/");
        String[] swaggerPathSegments = swaggerPath.split("\\/");

        if (pathSegments.length != swaggerPathSegments.length) {
            return false;
        }

        for (int c = 0; c < pathSegments.length; c++) {
            String pathSegment = pathSegments[c];
            String swaggerPathSegment = swaggerPathSegments[c];

            if (swaggerPathSegment.startsWith("{") && swaggerPathSegment.endsWith("}")) {
                continue;
            } else if (!swaggerPathSegment.equalsIgnoreCase(pathSegment)) {
                return false;
            }
        }

        return true;
    }

    static boolean isOAS3Definition(String oasVersion) {
        return oasVersion.startsWith("3.");
    }

    public static Swagger getSwagger(String swaggerAsString) {
        return getSwagger(swaggerAsString, true);
    }

    public static Swagger getSwagger(String swaggerAsString, boolean resolve) {
        SwaggerDeserializationResult swaggerDeserializationResult = new SwaggerParser().readWithInfo(swaggerAsString, resolve);
        logErrors(swaggerDeserializationResult);
        return swaggerDeserializationResult.getSwagger();
    }

    public static Swagger getSwagger(String url, List<AuthorizationValue> auths, boolean resolve, boolean disableLogger) {
        SwaggerParser swaggerParser = new SwaggerParser();
        SwaggerDeserializationResult swaggerDeserializationResult = swaggerParser.readWithInfo(url, auths, resolve);
        if (!disableLogger) {
            logErrors(swaggerDeserializationResult);
        }
        return swaggerDeserializationResult.getSwagger();
    }

    public static Swagger getSwagger(String url, List<AuthorizationValue> auths, boolean resolve) {
        return getSwagger(url, auths, resolve, false);
    }

    private static void logErrors(SwaggerDeserializationResult swaggerDeserializationResult) {
        List<String> messages = swaggerDeserializationResult.getMessages();
        if ((messages != null) && !messages.isEmpty()) {
            String errorMessage = StringUtils.join(messages.toArray(new String[messages.size()]), StringUtils.NEWLINE);
            logger.error(errorMessage);
        }
    }
}