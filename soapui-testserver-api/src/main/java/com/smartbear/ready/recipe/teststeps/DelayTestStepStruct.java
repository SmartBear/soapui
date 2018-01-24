package com.smartbear.ready.recipe.teststeps;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;

@ApiModel(value = "DelayTestStep", description = "Delay Test step definition")
public class DelayTestStepStruct extends TestStepStruct {
    public int delay;

    @JsonCreator
    public DelayTestStepStruct(@JsonProperty("type") String type,
                               @JsonProperty("name") String name,
                               @JsonProperty("delay") int delay) {
        super(type, name);
        this.delay = delay;
    }
}