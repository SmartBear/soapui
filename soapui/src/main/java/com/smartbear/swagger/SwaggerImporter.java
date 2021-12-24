package com.smartbear.swagger;

import com.eviware.soapui.impl.rest.AbstractRestService;
import com.eviware.soapui.impl.rest.refactoring.definition.model.project.RestRefactoringService;

public interface SwaggerImporter {

    AbstractRestService[] importSwagger(String url);

    AbstractRestService[] importSwagger(String url, String apiKey);

    default AbstractRestService[] importSwagger(String url, String apiKey, boolean disableLogger) {
        return importSwagger(url, apiKey);
    }

    RestRefactoringService loadRestRefactorServer(AbstractRestService service, String url) throws Exception;
}
