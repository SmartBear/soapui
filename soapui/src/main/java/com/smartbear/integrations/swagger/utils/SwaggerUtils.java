/**
 *  Copyright 2013-2017 SmartBear Software, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.smartbear.integrations.swagger.utils;

import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.rest.support.RestParameter;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.x.dialogs.Worker;
import com.eviware.x.dialogs.XProgressDialog;
import com.eviware.x.dialogs.XProgressMonitor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartbear.integrations.swagger.Swagger1XResourceListingImporter;
import com.smartbear.integrations.swaggerhub.SwaggerImporter;
import com.smartbear.integrations.swaggerhub.importers.OpenAPI3Importer;
import com.smartbear.integrations.swaggerhub.importers.Swagger2Importer;
import groovy.json.JsonSlurper;
import io.swagger.models.Swagger;
import io.swagger.models.auth.AuthorizationValue;
import io.swagger.parser.SwaggerParser;
import io.swagger.parser.util.SwaggerDeserializationResult;
import io.swagger.util.Json;
import io.swagger.v3.parser.util.ClasspathHelper;
import io.swagger.v3.parser.util.DeserializationUtils;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SwaggerUtils {
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
                                                 boolean generateTestCase) {
        if (url.endsWith(".yaml") || url.endsWith(".yml")) {
            if (isOpenApi(url)) {
                return new OpenAPI3Importer(project, defaultMediaType, generateTestCase);
            }
            return new Swagger2Importer(project, defaultMediaType, generateTestCase);
        }

        if (url.endsWith(".xml"))
            return new Swagger1XResourceListingImporter(project, defaultMediaType);

        UrlRestLoader loader = new UrlRestLoader(url);
        def json = new JsonSlurper().parseText(loader.load().text);

        if (isOAS3Definition(String.valueOf(json?.openapi))) {
            return new OpenAPI3Importer(project, defaultMediaType)
        } else if (String.valueOf(json?.swagger) == "2.0" || String.valueOf(json?.swaggerVersion) == "2.0")
            return new Swagger2Importer(project, defaultMediaType)
        else {
            def version = json?.swaggerVersion
            // in 1.2 only api-declarations have a basePath, see
            // https://github.com/OAI/OpenAPI-Specification/blob/master/versions/1.2.md#52-api-declaration
            if (version == "1.1") {
                if (json?.models != null || json?.resourcePath != null) {
                    return new Swagger1XApiDeclarationImporter(project, defaultMediaType)
                } else {
                    return new Swagger1XResourceListingImporter(project, defaultMediaType)
                }
            } else {
                if (json?.basePath != null) {
                    return new Swagger1XApiDeclarationImporter(project, defaultMediaType)
                } else {
                    return new Swagger1XResourceListingImporter(project, defaultMediaType)
                }
            }
        }
    }


    static SwaggerImporter createSwaggerImporter(String url, WsdlProject project, String defaultMediaType) {
        return createSwaggerImporter(url, project, defaultMediaType, DEFAULT_FOR_CREATE_TEST_CASE)
    }

    static SwaggerImporter createSwaggerImporter(String url, WsdlProject project) {
        return createSwaggerImporter(url, project, DEFAULT_MEDIA_TYPE,
                DEFAULT_FOR_CREATE_TEST_CASE);
    }

    @Deprecated
    static SwaggerImporter createSwaggerImporter(String url, WsdlProject project, boolean removedParameterForRefactoring) {
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
                UrlRestLoader loader = new UrlRestLoader(location);
                data = loader.load().text;
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
                rootNode = DeserializationUtils.readYamlTree(data);
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

    static void setParameterType(RestParameter parameter, String type) {
        if (JsonServiceUtils.JSON_SIMPLE_TYPES.contains(type)) {
            parameter.setDataType(type);
        } else {
            parameter.setDataType(JsonType.STRING.toString());
        }
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
        SwaggerDeserializationResult swaggerDeserializationResult = new SwaggerParser().readWithInfo(swaggerAsString, resolve)
        logErrors(swaggerDeserializationResult);
        return swaggerDeserializationResult.getSwagger();
    }

    public static Swagger getSwagger(String url, List<AuthorizationValue> auths, boolean resolve, boolean disableLogger) {
        SwaggerParser swaggerParser = new SwaggerParser();
        SwaggerDeserializationResult swaggerDeserializationResult = swaggerParser.readWithInfo(url, auths, resolve)
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
            String errorMessage = StringUtils.join(messages.toArray(new String[messages.size()]), StringUtils.NEWLINE)
            logger.error(errorMessage);
        }
    }
}