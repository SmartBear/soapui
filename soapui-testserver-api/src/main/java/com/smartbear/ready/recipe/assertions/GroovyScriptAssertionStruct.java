package com.smartbear.ready.recipe.assertions;

import com.eviware.soapui.impl.wsdl.teststeps.assertions.basic.GroovyScriptAssertion;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;

import static com.smartbear.ready.recipe.NullChecker.checkNotNull;

/**
 * Captures the JSON configuration of a Groovy script tests step.
 */
@ApiModel(value = "GroovyScriptAssertion", description = "Groovy script assertion definition")
public class GroovyScriptAssertionStruct extends AssertionStruct<GroovyScriptAssertion> {

    public final String script;

    @JsonCreator
    public GroovyScriptAssertionStruct(@JsonProperty("name") String name, @JsonProperty("script") String script) {
        super(GroovyScriptAssertion.LABEL, name);

        checkNotNull(script, "script");

        this.script = script;
    }

    @Override
    void configureAssertion(GroovyScriptAssertion assertion) {
        assertion.setScriptText(script);
    }
}
