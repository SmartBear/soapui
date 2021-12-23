package com.smartbear.integrations.swaggerhub.importers;

import com.eviware.soapui.support.MessageSupport;
import com.smartbear.swagger.SwaggerImporter;

public abstract class AbstractSwaggerImporter implements SwaggerImporter {
    private static final MessageSupport messages = MessageSupport.getMessages(AbstractSwaggerImporter.class);

    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String TYPE_HEADER = "header";
}
