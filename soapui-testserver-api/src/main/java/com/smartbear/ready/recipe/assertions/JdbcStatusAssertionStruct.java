package com.smartbear.ready.recipe.assertions;

import com.eviware.soapui.impl.wsdl.teststeps.assertions.jdbc.JdbcStatusAssertion;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;

@ApiModel(value = "JdbcStatusAssertion", description = JdbcStatusAssertion.DESCRIPTION)
public class JdbcStatusAssertionStruct extends AssertionStruct<JdbcStatusAssertion> {

    @JsonCreator
    public JdbcStatusAssertionStruct(@JsonProperty("name") String name) {
        super(JdbcStatusAssertion.LABEL, name);
    }

    @Override
    void configureAssertion(JdbcStatusAssertion assertion) {
        //Does not need configuration
    }
}
