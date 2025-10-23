package com.smartbear.swagger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.v3.oas.models.media.Schema;

import java.util.Map;

public class JsonSchemaGenerator {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static String generate(Schema<?> schema) {
        if (schema == null) {
            return "{}";
        }

        try {
            if (schema.getExample() != null) {
                return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(schema.getExample());
            }

            ObjectNode root = mapper.createObjectNode();
            generateObject(schema, root);

            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(root);
        } catch (Exception e) {
            return "{}";
        }
    }

    private static void generateObject(Schema<?> schema, ObjectNode node) {
        if (schema != null && schema.getProperties() != null) {
            for (Map.Entry<String, Schema> entry : (Iterable<Map.Entry<String, Schema>>) schema.getProperties().entrySet()) {
                String key = entry.getKey();
                Schema<?> propertySchema = entry.getValue();
                addValue(node, key, propertySchema);
            }
        }
    }

    private static void addValue(ObjectNode node, String key, Schema<?> schema) {
        if (schema == null) {
            return;
        }

        if (schema.getExample() != null) {
            node.putPOJO(key, schema.getExample());
            return;
        }

        String type = schema.getType();
        if (type == null) {
            // Default to object if type is not specified
            type = "object";
        }

        switch (type) {
            case "object":
                ObjectNode childNode = mapper.createObjectNode();
                generateObject(schema, childNode);
                node.set(key, childNode);
                break;
            case "array":
                ArrayNode arrayNode = mapper.createArrayNode();
                Schema<?> itemsSchema = schema.getItems();
                if (itemsSchema != null) {
                    addArrayValue(arrayNode, itemsSchema);
                }
                node.set(key, arrayNode);
                break;
            case "string":
                node.put(key, "string");
                break;
            case "integer":
                node.put(key, 0);
                break;
            case "number":
                node.put(key, 0.0);
                break;
            case "boolean":
                node.put(key, true);
                break;
            default:
                break;
        }
    }

    private static void addArrayValue(ArrayNode node, Schema<?> schema) {
        if (schema == null) {
            return;
        }
        String type = schema.getType();
        if (type == null) {
            type = "object";
        }

        switch (type) {
            case "object":
                ObjectNode childNode = mapper.createObjectNode();
                generateObject(schema, childNode);
                node.add(childNode);
                break;
            case "string":
                node.add("string");
                break;
            case "integer":
                node.add(0);
                break;
            case "number":
                node.add(0.0);
                break;
            case "boolean":
                node.add(true);
                break;
            default:
                break;
        }
    }
}
