package com.smartbear.ready.recipe.teststeps;

import com.smartbear.ready.recipe.assertions.AssertionStruct;

import java.util.Map;

public class HttpTestRequestStepStruct extends TestStepStruct {
    public String URI;
    public String requestBody;
    public AuthenticationStruct authentication;
    public AssertionStruct[] assertions;
    public Map<String, Object> headers;
    public String encoding;
    public String timeout;
    public boolean followRedirects;
    public boolean entitizeParameters;
    public String clientCertificateFileName;
    public String clientCertificatePassword;
    public RequestAttachmentStruct[] attachments;

    public HttpTestRequestStepStruct(String type, String name, String URI,
                                     AssertionStruct[] assertions,
                                     String encoding,
                                     Map<String, Object> headers,
                                     String timeout,
                                     boolean followRedirects,
                                     boolean entitizeParameters,
                                     String requestBody,
                                     AuthenticationStruct authentication,
                                     String clientCertificateFileName,
                                     String clientCertificatePassword,
                                     RequestAttachmentStruct[] attachments) {
        super(type, name);
        this.URI = URI;
        this.assertions = assertions;
        this.encoding = encoding;
        this.headers = headers;
        this.timeout = timeout;
        this.followRedirects = followRedirects;
        this.entitizeParameters = entitizeParameters;
        this.requestBody = requestBody;
        this.authentication = authentication;
        this.clientCertificateFileName = clientCertificateFileName;
        this.clientCertificatePassword = clientCertificatePassword;
        this.attachments = attachments;
    }
}
