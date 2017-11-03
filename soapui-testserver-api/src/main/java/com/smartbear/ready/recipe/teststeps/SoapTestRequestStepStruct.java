package com.smartbear.ready.recipe.teststeps;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.smartbear.ready.recipe.assertions.AssertionStruct;
import io.swagger.annotations.ApiModel;

import java.util.Map;

import static com.smartbear.ready.recipe.NullChecker.checkNotNull;

/**
 * Struct capturing values from JSON for building a REST request test step.
 */
@ApiModel(value = "SoapTestRequestStep", description = "SOAP Test step definition")
public class SoapTestRequestStepStruct extends HttpTestRequestStepStruct {

    public String wsdl;
    public String binding;
    public String operation;
    public SoapParamStruct[] parameters;

    @JsonCreator
    public SoapTestRequestStepStruct(
            @JsonProperty("type") String type,
            @JsonProperty("name") String name,
            @JsonProperty("wsdl") String wsdl,
            @JsonProperty("binding") String binding,
            @JsonProperty("operation") String operation,
            @JsonProperty("URI") String uri,
            @JsonProperty("requestBody") String requestBody,
            @JsonProperty("authentication") AuthenticationStruct authentication,
            @JsonProperty("parameters") SoapParamStruct[] parameters,
            @JsonProperty("assertions") AssertionStruct[] assertions,
            @JsonProperty("headers") Map<String, Object> headers,
            @JsonProperty("encoding") String encoding,
            @JsonProperty("timeout") String timeout,
            @JsonProperty("followRedirects") boolean followRedirects,
            @JsonProperty("entitizeParameters") boolean entitizeParameters,
            @JsonProperty("clientCertificateFileName") String clientCertificateFileName,
            @JsonProperty("clientCertificatePassword") String clientCertificatePassword,
            @JsonProperty("attachments") RequestAttachmentStruct[] attachments) {
        super(type, name, uri, assertions, encoding, headers, timeout, followRedirects, entitizeParameters,
                requestBody, authentication, clientCertificateFileName, clientCertificatePassword, attachments);

        checkNotNull(wsdl, "wsdl");
        checkNotNull(operation, "operation");

        this.wsdl = wsdl;
        this.binding = binding;
        this.operation = operation;
        this.parameters = parameters;
    }
}
