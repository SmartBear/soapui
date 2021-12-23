package com.smartbear.integrations.swagger;

import com.eviware.soapui.impl.support.MediaTypeUtils;
import com.eviware.soapui.impl.swagger.OpenAPIv3DefinitionContext;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.xml.XmlUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.swagger.oas.inflector.examples.ExampleBuilder;
import io.swagger.oas.inflector.examples.XmlExampleSerializer;
import io.swagger.oas.inflector.examples.models.ObjectExample;
import io.swagger.oas.inflector.processors.JsonNodeExampleSerializer;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class OpenAPI3SampleUtil {
    /*OT*/ private static final String SAMPLE_GENERATION_FAILED_MESSAGE = "Failed to create the sample. The '%s' media type is incorrect.";

    private final static Logger logger = LogManager.getLogger(OpenAPI3SampleUtil.class);

    OpenAPI openAPI;

    public OpenAPI3SampleUtil(OpenAPIv3DefinitionContext definitionContext) {
        try {
            this.openAPI = definitionContext.getInterfaceDefinition().getOpenAPIDefinition();
        } catch (Exception ignore) {
        }
    }

    public String generateSample(String path, String httpMethod, String mediaType, String statusCode) {
        if (openAPI == null) {
            return "";
        }

        PathItem pathItem = openAPI.getPaths().get(path);
        if (pathItem == null) {
            return "";
        }

        Operation operation = getOperation(pathItem, httpMethod);
        if (operation == null) {
            return "";
        }

        if (StringUtils.hasContent(statusCode)) {
            return generateResponseSample(operation, mediaType, statusCode);
        } else {
            return generateRequestSample(operation, mediaType);
        }
    }

    private Operation getOperation(PathItem pathItem, String httpMethod) {
        PathItem.HttpMethod targetMethod;
        try {
            targetMethod = Enum.valueOf(PathItem.HttpMethod.class, httpMethod.toUpperCase());
        } catch (IllegalArgumentException ignore) {
            return null;
        }

        return pathItem.readOperationsMap().get(targetMethod);
    }

    private String generateRequestSample(Operation operation, String mediaType) {
        if (operation.getRequestBody() == null || operation.getRequestBody().getContent() == null) {
            return "";
        }

        Content content = operation.getRequestBody().getContent();
        if (content.isEmpty()) {
            return "";
        }

        MediaType targetMediaType = content.get(mediaType);
        if (targetMediaType == null) {
            return "";
        }

        Schema mediaTypeSchema = targetMediaType.getSchema();
        if (mediaTypeSchema != null) {
            return generateSample(mediaTypeSchema, mediaType, ExampleBuilder.RequestType.WRITE);
        }

        return "";
    }

    private String generateResponseSample(Operation operation, String mediaType, String statusCode) {
        if (operation.getResponses() == null || operation.getResponses().isEmpty()) {
            return "";
        }

        ApiResponse apiResponse = operation.getResponses().get(statusCode);
        if (apiResponse == null) {
            return "";
        }

        if (apiResponse.getContent() == null || apiResponse.getContent().get(mediaType) == null) {
            return "";
        }

        MediaType targetMediaType = apiResponse.getContent().get(mediaType);
        Schema mediaTypeSchema = targetMediaType.getSchema();
        if (mediaTypeSchema != null) {
            return generateSample(mediaTypeSchema, mediaType, ExampleBuilder.RequestType.READ);
        }

        return "";
    }

    private String generateSample(Schema mediaTypeSchema, String mediaType, ExampleBuilder.RequestType type) {
        Map<String, Schema> schemas = getOpenApiSchemas();
        io.swagger.oas.inflector.examples.models.Example example = ExampleBuilder.fromSchema(mediaTypeSchema, schemas, type);
        if (example != null) {
            return serializeExample(mediaType, example);
        }

        return "";
    }

    private Map<String, Schema> getOpenApiSchemas() {
        if (openAPI.getComponents() != null) {
            Map<String, Schema> schemas = openAPI.getComponents().getSchemas();
            if (schemas != null) {
                return schemas;
            }
        }
        return new HashMap<>();
    }

    private String serializeExample(String mediaType, io.swagger.oas.inflector.examples.models.Example output) {
        String sampleValue = "";
        ObjectMapper mapper = null;

        ObjectMapper yamlMapper = Yaml.mapper();
        ObjectMapper jsonMapper = Json.mapper();
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(new JsonNodeExampleSerializer());
        yamlMapper.registerModule(simpleModule);
        jsonMapper.registerModule(simpleModule);

        String subtype = "";
        try {
            subtype = MediaTypeUtils.getSubtype(mediaType);
            String suffix = MediaTypeUtils.getSuffix(mediaType);
            if (StringUtils.hasContent(suffix)) {
                subtype = suffix;
            }
        } catch (IllegalArgumentException e) {
            logger.warn(String.format(SAMPLE_GENERATION_FAILED_MESSAGE, mediaType));
        }


        switch (subtype.toLowerCase()) {
            case "xml":
                sampleValue = XmlUtils.prettyPrintXml(new XmlExampleSerializer().serialize(output));
                break;
            case "yaml":
                mapper = yamlMapper;
                break;
            case "json":
                mapper = jsonMapper;
                break;
            case "plain":
                if (!(output instanceof ObjectExample)) {
                    sampleValue = output.asString();
                }
                break;
        }

        if (mapper != null) {
            try {
                sampleValue = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(output);
            } catch (JsonProcessingException ignore) {
            }
        }
        return sampleValue;
    }
}
