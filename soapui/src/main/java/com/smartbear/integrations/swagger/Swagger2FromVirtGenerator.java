/**
 *  Copyright 2013-2017 SmartBear Software, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.smartbear.swagger

import com.eviware.soapui.impl.rest.RestRequestInterface
import com.eviware.soapui.impl.rest.RestResource
import com.eviware.soapui.impl.rest.mock.RestMockRequest
import com.eviware.soapui.impl.rest.mock.RestMockResult
import com.eviware.soapui.impl.rest.mock.RestMockService
import com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder
import com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder.ParameterStyle
import com.eviware.soapui.model.mock.MockResult
import com.eviware.soapui.support.StringUtils
import com.fasterxml.jackson.databind.ObjectMapper
import io.swagger.models.Info
import io.swagger.models.Operation
import io.swagger.models.Path
import io.swagger.models.Response
import io.swagger.models.Swagger
import io.swagger.models.parameters.BodyParameter
import io.swagger.models.parameters.HeaderParameter
import io.swagger.models.parameters.PathParameter
import io.swagger.models.parameters.QueryParameter
import io.swagger.models.properties.StringProperty
import io.swagger.util.Json
import io.swagger.v3.oas.models.parameters.Parameter

public class Swagger2FromVirtGenerator {

    private final RestMockRequest mockRequest

    Swagger2FromVirtGenerator(RestMockRequest mockRequest) {

        this.mockRequest = mockRequest
    }

    String createSwagger(RestMockService mockService) {

        Swagger swagger = new Swagger();
        swagger.basePath = "/"
        swagger.info = new Info()

        swagger.info.title = mockService.name
        swagger.info.version = mockService.getPropertyValue("swagger.info.version")
        if (StringUtils.isNullOrEmpty(swagger.info.version)) {
            swagger.info.version = "1.0"
        }

        swagger.info.description = mockService.getPropertyValue("swagger.info.description")

        mockService.mockOperationList.each {

            Path p = new Path()

            Operation operation = new Operation()
            operation.operationId = it.name

            it.mockResponses.each {

                def mockResponse = it
                def response = new Response()

                response.description = it.name
                response.examples = new HashMap<>()
                response.examples.put(it.contentType, it.responseContent)

                mockResponse.responseHeaders.keySet().each {
                    if (response.headers == null) {
                        response.headers = new HashMap<>()
                    }

                    def property = new StringProperty()
                    property.example = mockResponse.responseHeaders.get(it)
                    response.headers.put(it, property)
                }

                operation.addResponse(mockResponse.responseHttpStatus.toString(), response)

                if (!operation.produces?.contains(mockResponse.contentType)) {
                    operation.addProduces(mockResponse.contentType)
                }
            }

            p.set(it.method.name().toLowerCase(), operation)

            if (it.operation instanceof RestResource) {
                addParametersToOperation((it.operation as RestResource).params, operation)
            }

            if (it.method == RestRequestInterface.HttpMethod.POST || it.method == RestRequestInterface.HttpMethod.PUT) {
                def param = new BodyParameter()
                operation.addParameter(param)
                param.name = "body"
                param.description = "Request body"
                param.required = true
            }

            swagger.path(it.resourcePath, p)
        }

        ObjectMapper mapper = Json.mapper();
        StringWriter writer = new StringWriter()
        mapper.writeValue(writer, swagger);
        return writer.toString();
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

    MockResult generate() {
        RestMockResult result = new RestMockResult(mockRequest)
        result.contentType = "application/json"
        result.responseContent = createSwagger(mockRequest.context.mockService)
        result.statusCode = 200

        def httpResponse = mockRequest.httpResponse
        httpResponse.contentType = "application/json"
        httpResponse.status = 200

        // add so swagger-ui can be used to access
        httpResponse.addHeader("Access-Control-Allow-Headers", "*")
        httpResponse.addHeader("Access-Control-Allow-Methods", "POST, GET, DELETE, OPTIONS, PATCH, PUT")
        httpResponse.addHeader("Access-Control-Allow-Origin", "*")
        httpResponse.addHeader("Access-Control-Request-Headers", "*")

        httpResponse.writer.write(result.responseContent)
        httpResponse.flushBuffer()

        return result
    }
}
