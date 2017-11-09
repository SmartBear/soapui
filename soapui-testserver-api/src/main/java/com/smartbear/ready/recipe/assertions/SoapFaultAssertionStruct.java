package com.smartbear.ready.recipe.assertions;

import com.eviware.soapui.impl.wsdl.teststeps.assertions.soap.SoapFaultAssertion;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;

/**
 * Captures the JSON configuration of a Schema Compliance assertion.
 */
@ApiModel(value = "SoapFaultAssertion", description = "SOAP Fault assertion definition")
public class SoapFaultAssertionStruct extends AssertionStruct<SoapFaultAssertion> {

    @JsonCreator
    public SoapFaultAssertionStruct(@JsonProperty("name") String name) {
        super(SoapFaultAssertion.LABEL, name);

    }

    @Override
    void configureAssertion(SoapFaultAssertion assertion) {
    }
}
