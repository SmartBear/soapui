package com.smartbear.ready.recipe.teststeps;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;

import java.util.Map;

@ApiModel(value = "PropertiesTestStep", description = "Properties test step definition")
public class PropertiesTestStepStruct extends TestStepStruct {
    public Map<String, String> properties;

    @JsonCreator
    public PropertiesTestStepStruct(@JsonProperty("type") String type, @JsonProperty("name") String name,
                                    @JsonProperty("properties") Map<String, String> properties) {
        super(type, name);
        this.properties = properties;
    }
}