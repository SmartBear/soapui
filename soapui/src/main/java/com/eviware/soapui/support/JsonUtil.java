/*
 * SoapUI, Copyright (C) 2004-2022 SmartBear Software
 *
 * Licensed under the EUPL, Version 1.1 or - as soon as they will be approved by the European Commission - subsequent 
 * versions of the EUPL (the "Licence"); 
 * You may not use this work except in compliance with the Licence. 
 * You may obtain a copy of the Licence at: 
 * 
 * http://ec.europa.eu/idabc/eupl 
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is 
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either 
 * express or implied. See the Licence for the specific language governing permissions and limitations 
 * under the Licence. 
 */

package com.eviware.soapui.support;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import net.sf.json.JSON;
import net.sf.json.JSONNull;
import net.sf.json.JSONSerializer;
import net.sf.json.groovy.JsonSlurper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Date;

/**
 * @author joel.jonsson
 */
public class JsonUtil {

    public static boolean REMOVE_D_ELEMENT = true;
    private static final String WHILE_1 = "while(1);";
    private static final String CLOSING_BRACKETS_WITH_COMMA = ")]}',";
    private static final String CLOSING_BRACKETS = ")]}'";
    private static final String EMPTY_FOR = "for(;;);";
    private static final String D_PREFIXED = "{\"d\":";
    private static final String[] VULNERABILITY_TOKENS = {WHILE_1, CLOSING_BRACKETS_WITH_COMMA, CLOSING_BRACKETS, EMPTY_FOR};

    private static final String DEFAULT_INDENT = "   ";
    private final static Logger log = LogManager.getLogger(JsonUtil.class);

    private static ObjectMapper mapper;
    private static JacksonJsonNodeJsonProvider defaultNodeProvider;
    private static Configuration configuration;
    private static DefaultPrettyPrinter printer;

    static {
        initStaticVariables();
    }

    public static ObjectMapper getMapper() {
        return mapper;
    }

    public static Configuration getDefaultConfiguration() {
        return configuration;
    }

    private static void initStaticVariables() {
        mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT)
                .enable(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY);
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        mapper.enable(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN);
        mapper.enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
        mapper.enable(JsonParser.Feature.ALLOW_COMMENTS);
        mapper.setNodeFactory(JsonNodeFactory.withExactBigDecimals(true));
        SimpleModule module = new SimpleModule();
        module.addSerializer(Date.class, new DateObjectSerializer());
        mapper.registerModule(module);
        defaultNodeProvider = new JacksonJsonProvider(mapper);
        configuration = Configuration.builder()
                .jsonProvider(defaultNodeProvider)
                .mappingProvider(new JacksonMappingProvider(mapper))
                .build();
        DefaultPrettyPrinter.Indenter indenter =
                new DefaultIndenter(DEFAULT_INDENT, DefaultIndenter.SYS_LF);
        printer = new DefaultPrettyPrinter();
        printer.indentObjectsWith(indenter);
        printer.indentArraysWith(indenter);
    }

    public static boolean isValidJson(String value) {
        try {
            JSON json = new JsonSlurper().parseText(value);
            return json != null && !(json instanceof JSONNull);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * This method and its name are somewhat awkward, but both stem from the fact that there are so many commonly used
     * content types for JSON.
     *
     * @param contentType the MIME type to examine
     * @return <code>true</code> if content type is non-null and contains either "json" or "javascript"
     */
    public static boolean seemsToBeJsonContentType(String contentType) {
        return contentType != null && (contentType.contains("javascript") || contentType.contains("json"));
    }

    public static boolean seemsToBeJson(String content) {
        if (!StringUtils.hasContent(content)) {
            return false;
        }
        try {
            new JsonSlurper().parseText(content);
            return true;
        } catch (Exception e) {
            return false;
        }
    }


    public JSON parseTrimmedText(String text) {
        if (text == null) {
            return null;
        }
        String trimmedText = text.trim();
        if (trimmedText.startsWith(WHILE_1)) {
            trimmedText = trimmedText.substring(WHILE_1.length()).trim();
        }
        return JSONSerializer.toJSON(trimmedText);
    }

    public static String format(Object json) {
        if (json instanceof JsonNode) {
            try {
                return mapper.writer(printer).writeValueAsString(json);
            } catch (JsonProcessingException e) {
                log.error(e.getMessage(), e);
            }
        }
        return json.toString();
    }

    public static JsonNode parseTrimmedTextToJsonNode(String text) throws IOException {
        if (text == null) {
            return null;
        }
        String trimmedText = removeVulnerabilityTokens(text).trim();
        return getJson(trimmedText);
    }

    public static String removeVulnerabilityTokens(String inputJsonString) {
        if (inputJsonString == null) {
            return null;
        }
        String outputString = inputJsonString.trim();
        for (String vulnerabilityToken : VULNERABILITY_TOKENS) {
            if (outputString.startsWith(vulnerabilityToken)) {
                outputString = outputString.substring(vulnerabilityToken.length()).trim();
            }
        }

        if (REMOVE_D_ELEMENT && outputString.startsWith(D_PREFIXED) && outputString.endsWith("}")) {
            outputString = outputString.substring(D_PREFIXED.length(), outputString.length() - 1).trim();
        }
        return outputString;
    }

    public static JsonNode getJson(String value) throws IOException {
        return getJson(value, mapper);
    }

    private static JsonNode getJson(String value, ObjectMapper mapper) throws IOException {
        JsonNode json = mapper.readTree(value);
        return (json instanceof NullNode) || (json instanceof MissingNode) ? null : json;
    }

    private static class JacksonJsonProvider extends JacksonJsonNodeJsonProvider {

        public JacksonJsonProvider(ObjectMapper objectMapper) {
            super(objectMapper);
        }

        @Override
        public void setArrayIndex(Object array, int index, Object newValue) {
            if (!isArray(array)) {
                throw new UnsupportedOperationException();
            } else {
                ArrayNode arrayNode = (ArrayNode) array;
                removeDefaultNullNode(arrayNode);
                if (index == arrayNode.size()) {
                    arrayNode.add(createJsonElement(newValue));
                } else {
                    arrayNode.set(index, createJsonElement(newValue));
                }
            }
        }

        private void removeDefaultNullNode(ArrayNode node) {
            if (node.size() == 1 && node.get(0).getNodeType() == null) {
                node.remove(0);
            }
        }

        @Override
        public Object createArray() {
            ArrayNode node = JsonNodeFactory.instance.arrayNode();
            node.add(new NullPathNode());
            return node;
        }

        @Override
        public Object getArrayIndex(Object obj, int idx) {
            Object arrayElement = super.getArrayIndex(obj, idx);
            return arrayElement != null ? arrayElement : new NullPathNode();
        }

        private JsonNode createJsonElement(Object o) {
            if (o != null) {
                return o instanceof JsonNode ? (JsonNode) o : this.objectMapper.valueToTree(o);
            } else {
                return null;
            }
        }

        @Override
        public Object unwrap(Object o) {
            if (o == null || o instanceof NullPathNode) {
                return null;
            } else {
                return super.unwrap(o);
            }
        }
    }

}
