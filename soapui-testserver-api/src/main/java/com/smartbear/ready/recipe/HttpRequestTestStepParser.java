package com.smartbear.ready.recipe;

import com.eviware.soapui.config.AbstractRequestConfig;
import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.impl.wsdl.teststeps.TestRequest;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlMessageAssertion;
import com.eviware.soapui.model.testsuite.TestAssertion;
import com.eviware.soapui.support.types.StringToStringsMap;
import com.smartbear.ready.recipe.assertions.AssertionStruct;
import com.smartbear.ready.recipe.teststeps.HttpTestRequestStepStruct;

import java.util.List;
import java.util.Map;

public abstract class HttpRequestTestStepParser implements TestStepJsonParser {
    @SuppressWarnings("unchecked")
    protected void addAssertions(HttpTestRequestStepStruct testStepStruct, TestRequest testRequest) {
        if (testStepStruct.assertions != null) {
            for (AssertionStruct assertion : testStepStruct.assertions) {
                TestAssertion messageAssertion = testRequest.addAssertion(assertion.type);
                assertion.setNameAndConfigureAssertion((WsdlMessageAssertion) messageAssertion);
            }
        }
    }

    protected void addHeaders(HttpTestRequestStepStruct testStepStruct, AbstractHttpRequest<? extends AbstractRequestConfig> testRequest) {
        if (testStepStruct.headers != null) {
            StringToStringsMap requestHeaders = new StringToStringsMap();
            for (Map.Entry<String, Object> headerEntry : testStepStruct.headers.entrySet()) {
                String headerName = headerEntry.getKey();
                if (headerEntry.getValue() instanceof String) {
                    requestHeaders.add(headerName, (String) headerEntry.getValue());
                } else {
                    requestHeaders.put(headerName, (List<String>) headerEntry.getValue());
                }
            }
            testRequest.setRequestHeaders(requestHeaders);
        }
    }

    public void addProperties(HttpTestRequestStepStruct testStepStruct, AbstractHttpRequest<? extends AbstractRequestConfig> testRequest) {
        testRequest.setEncoding(testStepStruct.encoding);
        testRequest.setTimeout(testStepStruct.timeout);
        testRequest.setEntitizeProperties(testStepStruct.entitizeParameters);
        testRequest.setFollowRedirects(testStepStruct.followRedirects);
    }
}
