package com.smartbear.ready.recipe;

import com.eviware.soapui.config.AbstractRequestConfig;
import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.impl.wsdl.support.FileAttachment;
import com.eviware.soapui.impl.wsdl.teststeps.TestRequest;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlMessageAssertion;
import com.eviware.soapui.model.testsuite.TestAssertion;
import com.eviware.soapui.support.types.StringToStringsMap;
import com.google.api.client.repackaged.org.apache.commons.codec.binary.Base64;
import com.smartbear.ready.recipe.assertions.AssertionStruct;
import com.smartbear.ready.recipe.teststeps.HttpTestRequestStepStruct;
import com.smartbear.ready.recipe.teststeps.RequestAttachmentStruct;

import java.util.List;
import java.util.Map;

public abstract class HttpRequestTestStepParser implements TestStepJsonParser {
    @SuppressWarnings("unchecked")
    protected void addAssertions(HttpTestRequestStepStruct testStepStruct, TestRequest testRequest) {
        if (testStepStruct.assertions != null) {
            for (AssertionStruct assertion : testStepStruct.assertions) {
                if( assertion != null ) {
                    TestAssertion messageAssertion = testRequest.addAssertion(assertion.type);
                    assertion.setNameAndConfigureAssertion((WsdlMessageAssertion) messageAssertion);
                }
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

    void addAttachments(HttpTestRequestStepStruct testStepStruct, AbstractHttpRequest testRequest) {
        RequestAttachmentStruct[] attachmentArray = testStepStruct.attachments;
        if (attachmentArray != null) {
            for (RequestAttachmentStruct attachment : attachmentArray) {
                FileAttachment fileAtt = (FileAttachment) testRequest.attachBinaryData(Base64.decodeBase64(attachment.content), attachment.contentType);
                fileAtt.setName(attachment.name);
                fileAtt.setContentID(attachment.contentId);
            }
        }
    }

    public void addProperties(HttpTestRequestStepStruct testStepStruct, AbstractHttpRequest<? extends AbstractRequestConfig> testRequest) {
        testRequest.setEncoding(testStepStruct.encoding);
        testRequest.setTimeout(testStepStruct.timeout);
        testRequest.setEntitizeProperties(testStepStruct.entitizeParameters);
        testRequest.setFollowRedirects(testStepStruct.followRedirects);
    }
}
