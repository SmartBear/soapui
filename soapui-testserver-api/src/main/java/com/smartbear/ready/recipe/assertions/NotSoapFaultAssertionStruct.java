package com.smartbear.ready.recipe.assertions;

import com.eviware.soapui.impl.wsdl.teststeps.assertions.soap.NotSoapFaultAssertion;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;

/**
 * Captures the JSON configuration of a Schema Compliance assertion.
 */
@ApiModel(value = "NotSoapFaultAssertion", description = "Not SOAP Fault assertion definition")
public class NotSoapFaultAssertionStruct extends AssertionStruct<NotSoapFaultAssertion> {

    @JsonCreator
    public NotSoapFaultAssertionStruct(@JsonProperty("name") String name) {
        super(NotSoapFaultAssertion.LABEL, name);

    }

    @Override
    void configureAssertion(NotSoapFaultAssertion assertion) {
    }
}
