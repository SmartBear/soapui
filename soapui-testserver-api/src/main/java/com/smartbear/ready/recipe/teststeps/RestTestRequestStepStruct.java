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
@ApiModel(value = "RestTestRequestStep", description = "REST Test step definition")
public class RestTestRequestStepStruct extends HttpTestRequestStepStruct {

    public String method;
    public ParamStruct[] parameters;
    public String mediaType;
    public boolean postQueryString;

    @JsonCreator
    public RestTestRequestStepStruct(
            @JsonProperty("type") String type,
            @JsonProperty("name") String name,
            @JsonProperty("method") String method,
            @JsonProperty("URI") String URI,
            @JsonProperty("requestBody") String requestBody,
            @JsonProperty("authentication") AuthenticationStruct authentication,
            @JsonProperty("parameters") ParamStruct[] parameters,
            @JsonProperty("assertions") AssertionStruct[] assertions,
            @JsonProperty("headers") Map<String, Object> headers,
            @JsonProperty("encoding") String encoding,
            @JsonProperty("timeout") String timeout,
            @JsonProperty("mediaType") String mediaType,
            @JsonProperty("followRedirects") boolean followRedirects,
            @JsonProperty("entitizeParameters") boolean entitizeParameters,
            @JsonProperty("postQueryString") boolean postQueryString,
            @JsonProperty("clientCertificateFileName") String clientCertificateFileName,
            @JsonProperty("clientCertificatePassword") String clientCertificatePassword,
            @JsonProperty("attachments") RequestAttachmentStruct[] attachments) {
        super(type, name, URI, assertions, encoding, headers, timeout, followRedirects, entitizeParameters,
                requestBody, authentication, clientCertificateFileName, clientCertificatePassword, attachments);

        checkNotNull(method, "method");
        checkNotNull(URI, "URI");

        this.method = method;
        this.parameters = parameters;
        this.mediaType = mediaType;
        this.postQueryString = postQueryString;
    }
}
