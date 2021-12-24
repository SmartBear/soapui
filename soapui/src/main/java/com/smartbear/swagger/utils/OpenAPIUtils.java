package com.smartbear.swagger.utils;

import com.eviware.soapui.support.xml.XmlUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import io.swagger.util.Json;
import io.swagger.util.Yaml;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.eviware.soapui.impl.support.HttpUtils.canHavePayload;

public class OpenAPIUtils {

    private static final Logger LOG = LoggerFactory.getLogger(OpenAPIUtils.class);

    public static String extractStringFromExampleObject(String contentType, Object example) {
        if (example instanceof String) {
            return ((String) example);
        } else if (contentType.equals("application/yaml")) {
            try {
                return Yaml.pretty().writeValueAsString(example);
            } catch (JsonProcessingException e) {
                LOG.warn("Failed to serialize example to YAML", e);
            }
        } else if (contentType.equals("application/xml")) {
            try {
                return XmlUtils.prettyPrintXml(new XmlMapper().writeValueAsString(example));
            } catch (JsonProcessingException e) {
                LOG.warn("Failed to serialize example to XML", e);
            }
        }
        return Json.pretty(example);
    }
}
