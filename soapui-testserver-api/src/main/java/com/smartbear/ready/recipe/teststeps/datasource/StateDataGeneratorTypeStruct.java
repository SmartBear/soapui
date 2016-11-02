package com.smartbear.ready.recipe.teststeps.datasource;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.smartbear.ready.recipe.teststeps.DataGeneratorTypeStruct;
import io.swagger.annotations.ApiModel;

@ApiModel(value = "StateDataGenerator", description = "State Data Generator definition")
public class StateDataGeneratorTypeStruct extends DataGeneratorTypeStruct {

    public String nameFormat;

    @JsonCreator
    public StateDataGeneratorTypeStruct(@JsonProperty("type") String type,
                                        @JsonProperty("propertyName") String propertyName,
                                        @JsonProperty("duplicationFactor") int duplicationFactor,
                                        @JsonProperty("nameFormat") String nameFormat) {
        super(type, propertyName, duplicationFactor);
        this.nameFormat = nameFormat;
    }
}
