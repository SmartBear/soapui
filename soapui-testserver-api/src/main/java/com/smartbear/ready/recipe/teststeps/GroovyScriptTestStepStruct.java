package com.smartbear.ready.recipe.teststeps;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;

import static com.smartbear.ready.recipe.NullChecker.checkNotNull;

/**
 * Struct capturing values from JSON for building a Groovy test step
 */
@ApiModel(value = "GroovyScriptTestStep", description = "Groovy Script Test step definition")
public class GroovyScriptTestStepStruct extends TestStepStruct {
    public String script;

    @JsonCreator
    public GroovyScriptTestStepStruct(@JsonProperty("type") String type,
                                      @JsonProperty("name") String name,
                                      @JsonProperty("script") String script) {
        super(type, name);

        checkNotNull(script, "script");

        this.script = script;
    }
}
