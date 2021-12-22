package com.smartbear.integrations.swaggerhub;

import com.eviware.soapui.impl.rest.RestService;

public interface SwaggerImporter {

    RestService[] importSwagger(String url);

    RestService[] importSwagger(String url, String apiKey);

    default RestService[] importSwagger(String url, String apiKey, boolean disableLogger) {
        return importSwagger(url, apiKey);
    }
}
