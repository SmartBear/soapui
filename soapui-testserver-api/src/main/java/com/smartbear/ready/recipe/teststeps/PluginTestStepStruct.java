package com.smartbear.ready.recipe.teststeps;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;

import java.util.Map;

@ApiModel(value = "PluginTestStep", description = "Plugin test step definition")
public class PluginTestStepStruct extends TestStepStruct {
    public PluginTestStepStruct(@JsonProperty("type") String type, @JsonProperty("name") String name,
                                @JsonProperty("configuration") Map<String, Object> configuration) {
        super(type, name, configuration);
    }
}
