package com.smartbear.ready.recipe.assertions;

import com.eviware.soapui.impl.wsdl.teststeps.assertions.basic.SimpleContainsAssertion;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;

import static com.smartbear.ready.recipe.NullChecker.checkNotNull;

/**
 * Captures the JSON configuration of Simple contains assertion.
 */
@ApiModel(value = "SimpleContainsAssertion", description = "Simple contains assertion definition")
public class SimpleContainsAssertionStruct extends AssertionStruct<SimpleContainsAssertion> {
    public final String token;
    public final boolean ignoreCase;
    public final boolean useRegexp;

    @JsonCreator
    public SimpleContainsAssertionStruct(@JsonProperty("name") String name, @JsonProperty("token") String token, @JsonProperty("ignoreCase") boolean ignoreCase, @JsonProperty("useRegexp") boolean useRegexp) {
        super(SimpleContainsAssertion.LABEL, name);

        checkNotNull(token, "token");

        this.token = token;
        this.ignoreCase = ignoreCase;
        this.useRegexp = useRegexp;
    }


    @Override
    void configureAssertion(SimpleContainsAssertion simpleContainsAssertion) {
        simpleContainsAssertion.setToken(token);
        simpleContainsAssertion.setIgnoreCase(ignoreCase);
        simpleContainsAssertion.setUseRegEx(useRegexp);
    }
}
