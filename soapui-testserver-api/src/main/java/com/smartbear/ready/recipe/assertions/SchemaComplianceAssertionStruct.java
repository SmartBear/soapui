package com.smartbear.ready.recipe.assertions;

import com.eviware.soapui.impl.wsdl.teststeps.assertions.basic.SchemaComplianceAssertion;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;

/**
 * Captures the JSON configuration of a Schema Compliance assertion.
 */
@ApiModel(value = "SchemaComplianceAssertion", description = "Schema compliance assertion definition")
public class SchemaComplianceAssertionStruct extends AssertionStruct<SchemaComplianceAssertion> {

    @JsonCreator
    public SchemaComplianceAssertionStruct(@JsonProperty("name") String name) {
        super(SchemaComplianceAssertion.LABEL, name);
    }

    @Override
    void configureAssertion(SchemaComplianceAssertion assertion) {
    }
}
