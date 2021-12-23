package com.smartbear.swagger;

import com.eviware.soapui.impl.support.MediaTypeUtils;
import com.eviware.soapui.impl.swagger.SwaggerDefinitionContext;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.xml.XmlUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.swagger.inflector.examples.ExampleBuilder;
import io.swagger.inflector.examples.XmlExampleSerializer;
import io.swagger.inflector.examples.models.Example;
import io.swagger.inflector.examples.models.ObjectExample;
import io.swagger.inflector.processors.JsonNodeExampleSerializer;
import io.swagger.models.ComposedModel;
import io.swagger.models.HttpMethod;
import io.swagger.models.Model;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.RefModel;
import io.swagger.models.Response;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.properties.ObjectProperty;
import io.swagger.util.Json;
import io.swagger.util.Yaml;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.Optional;

public class SwaggerSampleUtil {
    /*OT*/ private static final String SAMPLE_GENERATION_FAILED_MESSAGE = "Failed to create the sample. The '%s' media type is incorrect.";

    private final static Logger logger = LogManager.getLogger(SwaggerSampleUtil.class);

    private Swagger swagger;

    public SwaggerSampleUtil(SwaggerDefinitionContext definitionContext) {
        try {
            swagger = definitionContext.getInterfaceDefinition().getSwaggerDefinition();
        } catch (Exception ignore) {
        }
    }

    public String generateSample(String path, String httpMethod, String mediaType, String statusCode) {
        if (swagger == null) {
            return "";
        }

        HttpMethod targetHttpMethod;
        try {
            targetHttpMethod = getHttpMethod(httpMethod);
        } catch (IllegalArgumentException ignore) {
            return "";
        }

        String basePath = swagger.getBasePath();
        String processedPath = path;
        if (basePath != null && !StringUtils.sameString(basePath, "/")) {
            basePath = StringUtils.trimRight(basePath, "/");
            if (processedPath.toLowerCase().startsWith(basePath.toLowerCase())) {
                processedPath = path.substring(basePath.length());
            }
        }

        Path targetPath = swagger.getPath(processedPath);
        if (targetPath == null) {
            return "";
        }

        Operation operation = targetPath.getOperationMap().get(targetHttpMethod);
        if (operation == null) {
            return "";
        }

        if (StringUtils.hasContent(statusCode)) {
            return generateResponseSample(operation, mediaType, statusCode);
        } else {
            return generateRequestSample(operation, mediaType);
        }
    }

    private HttpMethod getHttpMethod(String httpMethod) {
        return Enum.valueOf(HttpMethod.class, httpMethod.toUpperCase());
    }

    private String generateRequestSample(Operation operation, String mediaType) {
        Optional<Parameter> bodyParameter = operation.getParameters().stream().filter(parameter -> parameter.getIn().equals("body")).findFirst();

        if (!bodyParameter.isPresent()) {
            return "";
        }

        Model bodyParameterModel = ((BodyParameter) bodyParameter.get()).getSchema();
        if (bodyParameterModel != null) {
            ObjectProperty objectProperty = new ObjectProperty(bodyParameterModel.getProperties());
            if (bodyParameterModel instanceof RefModel) {
                RefModel refModel = (RefModel) bodyParameterModel;
                Model modelDefinition = swagger.getDefinitions().get(refModel.getSimpleRef());
                if (modelDefinition instanceof ComposedModel) {
                    objectProperty = null;
                } else if (modelDefinition != null) {
                    objectProperty = new ObjectProperty(modelDefinition.getProperties());
                    objectProperty.name(refModel.getSimpleRef());
                }
            }
            Example output = objectProperty != null ? ExampleBuilder.fromProperty(objectProperty, swagger.getDefinitions()) :
                    ExampleBuilder.fromModel(null, bodyParameterModel, swagger.getDefinitions(), new HashSet<>());
            if (output != null) {
                return serializeExample(mediaType, output);
            }
        }

        return "";
    }

    private String generateResponseSample(Operation operation, String mediaType, String statusCode) {
        Response response = operation.getResponses().get(statusCode);
        if (response == null) {
            return "";
        }

        Example output = ExampleBuilder.fromProperty(response.getSchema(), swagger.getDefinitions());
        if (output != null) {
            return serializeExample(mediaType, output);
        }

        return "";
    }

    private String serializeExample(String mediaType, Example output) {
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
                if(!XmlUtils.seemsToBeXml(sampleValue)) {
                    return "";
                }
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
