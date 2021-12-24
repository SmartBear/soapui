package com.smartbear.swagger;

import com.eviware.soapui.impl.rest.AbstractRestService;

public interface SwaggerExporter {

    String exportToFileSystem(String path, String apiVersion, String format, AbstractRestService[] services, String basePath);

    String getOasVersion();
}
