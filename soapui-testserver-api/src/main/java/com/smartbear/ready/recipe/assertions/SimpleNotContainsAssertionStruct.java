package com.smartbear.ready.recipe.assertions;

import com.eviware.soapui.impl.wsdl.teststeps.assertions.basic.SimpleNotContainsAssertion;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;

import static com.smartbear.ready.recipe.NullChecker.checkNotNull;

/**
 * Captures the JSON configuration of Simple not contains assertion.
 */
@ApiModel(value = "SimpleNotContainsAssertion", description = "Simple not contain assertion definition")
public class SimpleNotContainsAssertionStruct extends AssertionStruct<SimpleNotContainsAssertion> {
    public final String token;
    public final boolean ignoreCase;
    public final boolean useRegexp;

    @JsonCreator
    public SimpleNotContainsAssertionStruct(@JsonProperty("name") String name, @JsonProperty("token") String token, @JsonProperty("ignoreCase") boolean ignoreCase, @JsonProperty("useRegexp") boolean useRegexp) {
        super(SimpleNotContainsAssertion.LABEL, name);

        checkNotNull(token, "token");

        this.token = token;
        this.ignoreCase = ignoreCase;
        this.useRegexp = useRegexp;
    }


    @Override
    void configureAssertion(SimpleNotContainsAssertion simpleNotContainsAssertion) {
        simpleNotContainsAssertion.setToken(token);
        simpleNotContainsAssertion.setIgnoreCase(ignoreCase);
        simpleNotContainsAssertion.setUseRegEx(useRegexp);
    }
}
