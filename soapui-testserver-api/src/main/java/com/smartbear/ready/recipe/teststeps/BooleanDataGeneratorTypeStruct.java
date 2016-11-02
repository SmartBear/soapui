package com.smartbear.ready.recipe.teststeps;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;

@ApiModel(value = "BooleanDataGenerator", description = "Boolean Data Generator definition")
public class BooleanDataGeneratorTypeStruct extends DataGeneratorTypeStruct {

    public String format;

    @JsonCreator
    public BooleanDataGeneratorTypeStruct(@JsonProperty("type") String type,
                                          @JsonProperty("propertyName") String propertyName,
                                          @JsonProperty("duplicationFactor") int duplicationFactor,
                                          @JsonProperty("format") String format) {
        super(type, propertyName, duplicationFactor);
        this.format = format;
    }
}
