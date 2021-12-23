package com.smartbear.integrations.swaggerhub.utils;

import com.eviware.soapui.impl.wsdl.support.wsdl.UrlWsdlLoader;
import io.swagger.v3.parser.core.models.AuthorizationValue;

public class OpenAPI3UrlClientLoader extends UrlWsdlLoader {
    public static final String OPENAPI3_API_KEY = "OpenAPI3ApiKey";

    public OpenAPI3UrlClientLoader(String url, AuthorizationValue apiKey) {
        super(url);
        state.setAttribute(OPENAPI3_API_KEY, apiKey);
    }
}
