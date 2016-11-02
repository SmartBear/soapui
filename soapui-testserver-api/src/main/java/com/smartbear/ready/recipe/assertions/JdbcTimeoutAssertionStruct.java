package com.smartbear.ready.recipe.assertions;

import com.eviware.soapui.impl.wsdl.teststeps.assertions.jdbc.JdbcTimeoutAssertion;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;

import static com.smartbear.ready.recipe.NullChecker.checkNotNull;

@ApiModel(value = "JdbcTimeoutAssertion", description = JdbcTimeoutAssertion.DESCRIPTION)
public class JdbcTimeoutAssertionStruct extends AssertionStruct<JdbcTimeoutAssertion> {

    public final String timeout;

    @JsonCreator
    public JdbcTimeoutAssertionStruct(@JsonProperty("name") String name, @JsonProperty("timeout") String timeout) {
        super(JdbcTimeoutAssertion.LABEL, name);

        checkNotNull(timeout, "timeout");

        this.timeout = timeout;
    }

    @Override
    void configureAssertion(JdbcTimeoutAssertion assertion) {
        assertion.setQueryTimeoutProperty(timeout);
    }
}
