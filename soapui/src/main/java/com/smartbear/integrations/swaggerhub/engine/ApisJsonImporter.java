package com.smartbear.integrations.swaggerhub.engine;

import groovy.json.JsonSlurper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ApisJsonImporter {
    static final Pattern OWNER_PATTERN = Pattern.compile("api\\.swaggerhub\\.com\\/apis\\/(.*?)\\/");

    public List<ApiDescriptor> importApis(String json) {
        Object apisJson = new JsonSlurper().parseText(json);
        List<ApiDescriptor> result = new ArrayList<>();

        if(!(apisJson instanceof Map)) {
            return result;
        }

        Map apisJsomMap = (Map)apisJson;
        Object apis = apisJsomMap.get("apis");

        if(!(apis instanceof List)) {
            return result;
        }

        List listApis = (List) apis;

        for (Object api : listApis) {
            if(api instanceof Map) {
                Map apiMap = (Map) api;
                ApiDescriptor descriptor = new ApiDescriptor();
                descriptor.name = (String) apiMap.get("name");
                descriptor.description = (String) apiMap.get("description");

                Object properties = apiMap.get("properties");
                List listProperties = (List) properties;

                for (Object property : listProperties) {
                    Map mapProperty = (Map) property;
                    String type = (String) mapProperty.get("type");
                    String value = (String) mapProperty.get("value");

                    if ("Swagger".equals(type)) {
                        String url = (String) mapProperty.get("url");
                        descriptor.swaggerUrl = url;
                        Matcher matcher = OWNER_PATTERN.matcher(url);
                        if (matcher.find()) {
                            descriptor.owner = matcher.group(1);
                        }
                    } else if ("X-Versions".equals(type)) {
                        descriptor.versions = value.split(",");
                    } else if ("X-Private".equals(type)) {
                        descriptor.isPrivate = Boolean.parseBoolean(value);
                    } else if ("X-OASVersion".equals(type)) {
                        descriptor.oasVersion = value;
                    } else if ("X-Published".equals(type)) {
                        descriptor.isPublished = Boolean.parseBoolean(value);
                    } else if ("X-Version".equals(type)) {
                        descriptor.defaultVersion = value;
                    }
                }

                result.add(descriptor);
            }
        }

        return result;
    }
}

