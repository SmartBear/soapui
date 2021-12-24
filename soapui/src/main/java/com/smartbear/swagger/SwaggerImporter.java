package com.smartbear.swagger;

import com.eviware.soapui.impl.rest.RestService;

public interface SwaggerImporter {

    String AUTHORIZATION_HEADER = "Authorization";
    String TYPE_HEADER = "header";

    RestService[] importSwagger(String url);

    RestService[] importSwagger(String url, String apiKey);

    default RestService[] importSwagger(String url, String apiKey, boolean disableLogger) {
        return importSwagger(url, apiKey);
    }
}
