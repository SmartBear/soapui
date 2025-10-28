package com.smartbear.swagger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.models.media.Schema;

import java.util.Map;
import java.util.stream.Collectors;

public class ExampleGenerator {

    public static String serializeExample(Map<String, Object> example, String mediaType) {
        if (mediaType.toLowerCase().contains("xml")) {
            return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<root/>";
        } else {
            try {
                return new ObjectMapper().writeValueAsString(example);
            } catch (JsonProcessingException e) {
                return "{}";
            }
        }
    }

    public static String generateExample(Schema schema, String mediaType) {
        if (mediaType.toLowerCase().contains("xml")) {
            return generateXmlExample(schema);
        } else {
            return generateJsonExample(schema);
        }
    }

    private static String generateJsonExample(Schema schema) {
        if ("array".equals(schema.getType())) {
            Schema itemSchema = schema.getItems();
            if (itemSchema != null) {
                return "[" + generateJsonExample(itemSchema) + "]";
            } else {
                return "[]";
            }
        }

        if (schema.getProperties() != null) {
            String properties = (String) schema.getProperties().entrySet().stream()
                    .map(entry -> {
                        Map.Entry<String, Schema> me = (Map.Entry<String, Schema>) entry;
                        String propertyName = me.getKey();
                        Schema propertySchema = me.getValue();
                        return "\"" + propertyName + "\": " + generateJsonExample(propertySchema);
                    })
                    .collect(Collectors.joining(",\n"));
            return "{\n" + properties + "\n}";
        }

        if (schema.getType() != null) {
            switch (schema.getType()) {
                case "string":
                    return "\"string\"";
                case "integer":
                    return "0";
                case "number":
                    return "0.0";
                case "boolean":
                    return "true";
            }
        }
        return "{}";
    }

    private static String generateXmlExample(Schema schema) {
        if ("array".equals(schema.getType())) {
            Schema itemSchema = schema.getItems();
            if (itemSchema != null) {
                return generateXmlExample(itemSchema);
            } else {
                return "";
            }
        }
        if (schema.getProperties() != null) {
            String properties = (String) schema.getProperties().entrySet().stream()
                    .map(entry -> {
                        Map.Entry<String, Schema> me = (Map.Entry<String, Schema>) entry;
                        String propertyName = me.getKey();
                        Schema propertySchema = me.getValue();
                        return "<" + propertyName + ">" + generateXmlExample(propertySchema) + "</" + propertyName + ">";
                    })
                    .collect(Collectors.joining("\n"));
            return properties;
        }

        if (schema.getType() != null) {
            switch (schema.getType()) {
                case "string":
                    return "string";
                case "integer":
                    return "0";
                case "number":
                    return "0.0";
                case "boolean":
                    return "true";
            }
        }
        return "";
    }
}
