package com.smartbear.swagger;

import com.eviware.soapui.impl.rest.RestService;

public interface SwaggerExporter {

    String exportToFileSystem(String path, String apiVersion, String format, RestService[] services, String basePath);

    String getOasVersion();
}
