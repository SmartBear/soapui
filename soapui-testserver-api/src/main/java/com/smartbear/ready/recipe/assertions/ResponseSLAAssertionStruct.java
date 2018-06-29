package com.smartbear.ready.recipe.assertions;

import com.eviware.soapui.impl.wsdl.teststeps.assertions.basic.ResponseSLAAssertion;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;

import static com.smartbear.ready.recipe.NullChecker.checkNotNull;

/**
 * Captures the JSON configuration of a Response SLA assertion.
 */
@ApiModel(value = "ResponseSLAAssertion", description = "Response SLA assertion definition")
public class ResponseSLAAssertionStruct extends AssertionStruct<ResponseSLAAssertion> {

    public final String maxResponseTime;

    @JsonCreator
    public ResponseSLAAssertionStruct(@JsonProperty("name") String name, @JsonProperty("maxResponseTime") String maxResponseTime) {
        super(ResponseSLAAssertion.LABEL, name);

        checkNotNull(maxResponseTime, "maxResponseTime");

        this.maxResponseTime = maxResponseTime;
    }

    @Override
    void configureAssertion(ResponseSLAAssertion assertion) {
        assertion.setSLA(maxResponseTime);
    }
}
