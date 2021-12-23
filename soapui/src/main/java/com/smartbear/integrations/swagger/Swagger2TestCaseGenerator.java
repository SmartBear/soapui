package com.smartbear.integrations.swagger

import com.eviware.soapui.impl.rest.RestRequestInterface.HttpMethod
import com.eviware.soapui.impl.rest.RestResource
import com.eviware.soapui.impl.wsdl.WsdlProject
import com.eviware.soapui.impl.wsdl.WsdlTestSuite
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase
import com.eviware.soapui.impl.wsdl.teststeps.RestTestRequestStep
import com.eviware.soapui.impl.wsdl.teststeps.registry.RestRequestStepFactory
import com.eviware.soapui.model.testsuite.TestProperty
import com.eviware.soapui.support.StringUtils
import com.smartbear.swagger.assertion.Assertions
import io.swagger.models.Operation
import io.swagger.models.Path
import io.swagger.models.parameters.AbstractSerializableParameter

class Swagger2TestCaseGenerator {
    static void generateTestCases(WsdlProject project, RestResource resource, Path path, Map<String, Object> context) {
        WsdlTestSuite testSuite = createTestSuiteIfNotPresent(project)
        WsdlTestCase testCase = testSuite.addNewTestCase("$resource.name-TestCase")
        resource.restMethodList.each {
            method ->
                method.requestList.each {
                    request ->
                        RestTestRequestStep testStep = (RestTestRequestStep) testCase.addTestStep(RestRequestStepFactory.createConfig(request, request.name))
                        testStep.testRequest.params.each {
                            setParameterValue(it.value, request.method, path, resource.path)
                        }
                        addAssertions(testStep, request.method, path, resource.path, context)

                        testStep.name = testStep.name + ": " + request.method + " " + resource.path
                }
        }
    }

    private static void addAssertions(RestTestRequestStep restTestRequestStep, HttpMethod httpMethod, Path swaggerPath,
                                      String path, Map<String, Object> context) {
        Operation operation = getSwaggerOperation(httpMethod, swaggerPath, path)
        if (operation) {
            Assertions.addAssertions(restTestRequestStep, operation.responses, context)
        }
    }

    private static void setParameterValue(TestProperty parameter, HttpMethod httpMethod, Path swaggerPath,
                                          String path) {
        Operation operation = getSwaggerOperation(httpMethod, swaggerPath, path)
        AbstractSerializableParameter swaggerParam = (AbstractSerializableParameter) operation.parameters.find {
            it.name == parameter.name
        }

        if (swaggerParam.example) {
            parameter.value = String.valueOf(swaggerParam.example)
        }

        if (StringUtils.isNullOrEmpty(parameter.value) && swaggerParam.required) {
            parameter.value = buildExampleValue(swaggerParam)
        }
    }

    static String buildExampleValue(AbstractSerializableParameter parameter) {

        switch (parameter.type) {
            case "string": return createStringExample(parameter.format)
            case "number": return "double" == parameter.format ? String.valueOf((double) Math.random() * 1000) : String.valueOf((float) Math.random() * 1000)
            case "integer": return String.valueOf((int) (Math.random() * 1000))
            case "boolean": return Math.random() > 0.5 ? "true" : "false"
        }

        return "exampleValue"
    }

    static String createStringExample(String format) {
        switch (format) {
            case "byte": return Base64.encoder.encodeToString("bytes".bytes)
            case "binary ": return "01234567"
            case "date": return "2015-07-20"
            case "date-time": return "2015-07-20T15:49:04-07:00"
            case "password": return "asdfghjk"
        }

        return "stringValue"
    }

    private static Operation getSwaggerOperation(HttpMethod httpMethod, Path swaggerPath, String path) {
        switch (httpMethod) {
            case HttpMethod.GET: return swaggerPath.get
            case HttpMethod.POST: return swaggerPath.post
            case HttpMethod.PUT: return swaggerPath.put
            case HttpMethod.DELETE: return swaggerPath.delete
            case HttpMethod.HEAD: return swaggerPath.head
            case HttpMethod.OPTIONS: return swaggerPath.options
            case HttpMethod.PATCH: return swaggerPath.patch
            default: throw new IllegalStateException("No operation found with HTTP method $httpMethod for path: $path")
        }
    }

    private static WsdlTestSuite createTestSuiteIfNotPresent(WsdlProject project) {
        return project.getTestSuiteCount() > 0 ? project.getTestSuiteAt(0) : project.addNewTestSuite("TestSuite1")
    }
}
