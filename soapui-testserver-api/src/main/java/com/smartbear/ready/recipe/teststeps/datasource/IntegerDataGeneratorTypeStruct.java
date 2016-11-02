package com.smartbear.ready.recipe.teststeps.datasource;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.smartbear.ready.recipe.teststeps.DataGeneratorTypeStruct;
import io.swagger.annotations.ApiModel;

@ApiModel(value = "IntegerDataGenerator", description = "Integer Data Generator definition")
public class IntegerDataGeneratorTypeStruct extends DataGeneratorTypeStruct {
    public Integer minimumValue;
    public Integer maximumValue;
    public String generationMode;
    public Integer incrementBy;

    @JsonCreator
    public IntegerDataGeneratorTypeStruct(@JsonProperty("type") String type,
                                          @JsonProperty("propertyName") String propertyName,
                                          @JsonProperty("duplicationFactor") int duplicationFactor,
                                          @JsonProperty("minimumValue") Integer minimumValue,
                                          @JsonProperty("maximumValue") Integer maximumValue,
                                          @JsonProperty("generationMode") String generationMode,
                                          @JsonProperty("incrementBy") Integer incrementBy) {
        super(type, propertyName, duplicationFactor);
        this.minimumValue = minimumValue;
        this.maximumValue = maximumValue;
        this.generationMode = generationMode;
        this.incrementBy = incrementBy;
    }
}
