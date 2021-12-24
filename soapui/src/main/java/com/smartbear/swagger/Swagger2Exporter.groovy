package com.smartbear.swagger

import com.eviware.soapui.impl.rest.AbstractRestService
import com.eviware.soapui.impl.rest.RestRepresentation
import com.eviware.soapui.impl.rest.RestRequestInterface
import com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder
import com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder.ParameterStyle
import com.eviware.soapui.impl.wsdl.WsdlProject
import com.fasterxml.jackson.databind.ObjectMapper
import io.swagger.models.*
import io.swagger.models.parameters.*
import io.swagger.util.Json
import io.swagger.util.Yaml

class Swagger2Exporter implements SwaggerExporter {

    private final WsdlProject project

    private Swagger swagger = null

    public Swagger2Exporter(WsdlProject project) {
        this.project = project
    }

    String exportToFileSystem(String fileName, String apiVersion, String format, AbstractRestService[] services, String basePath) {
        if (!ExportSwaggerAction.shouldOverwriteFileIfExists(fileName, null)) {
            return null
        }

        swagger = new Swagger();
        swagger.basePath = basePath
        swagger.info = new Info()

        swagger.info.version = apiVersion
        swagger.info.title = services[0].name

        services.each {
            it.allResources.each {
                Path p = new Path()

                it.restMethodList.each {
                    Operation operation = new Operation()
                    operation.operationId = it.resource.name

                    it.representations.each {
                        if (it.type == RestRepresentation.Type.RESPONSE || it.type == RestRepresentation.Type.FAULT)
                            it.status?.each { operation.addResponse(String.valueOf(it), new Response()) }
                        else if (it.type == RestRepresentation.Type.REQUEST && it.mediaType != null)
                            operation.addConsumes(it.mediaType)
                    }

                    if (!operation.responses || operation.responses.isEmpty()) {
                        operation.addResponse("200", new Response())
                    }

                    p.set(it.method.name().toLowerCase(), operation)

                    addParametersToOperation(it.overlayParams, operation)

                    if (it.method == RestRequestInterface.HttpMethod.POST || it.method == RestRequestInterface.HttpMethod.PUT) {
                        def param = new BodyParameter()
                        operation.addParameter(param)
                        param.name = "body"
                        param.description = "Request body"
                        param.required = true
                    }
                }

                swagger.path(it.fullPath, p)
            }
        }

        ObjectMapper mapper = format.equals("yaml") ? Yaml.mapper() : Json.mapper();
        mapper.writeValue(new FileWriter(fileName), swagger);

        return fileName;
    }

    @Override
    String getOasVersion() {
        return swagger == null ? null : swagger.getSwagger()
    }

    private void addParametersToOperation(RestParamsPropertyHolder params, Operation op) {

        for (name in params.getPropertyNames()) {
            def param = params.getProperty(name)
            if (!operationHasParameter(op, name)) {
                Parameter p = null

                switch (param.style) {
                    case ParameterStyle.HEADER: p = new HeaderParameter(); break;
                    case ParameterStyle.QUERY: p = new QueryParameter(); break;
                    case ParameterStyle.TEMPLATE: p = new PathParameter(); break;
                }

                if (p != null) {
                    op.addParameter(p)
                    p.name = param.name
                    p.required = p instanceof PathParameter || param.getRequired()
                    p.description = param.description

                    // needs to be extended to support all schema types
                    switch (param.type.localPart) {
                        case "byte": p.type = "byte"; break
                        case "dateTime": p.type = "Date"; break
                        case "float": p.type = "float"; break
                        case "double": p.type = "double"; break
                        case "long": p.type = "long"; break
                        case "short":
                        case "int":
                        case "integer": p.type = "int"; break
                        case "boolean": p.type = "boolean"; break
                        default: p.type = "string"
                    }
                }
            }
        }
    }

    boolean operationHasParameter(Operation operation, String name) {
        operation?.parameters.each { if (it.name == name) return true }

        return false
    }
}
