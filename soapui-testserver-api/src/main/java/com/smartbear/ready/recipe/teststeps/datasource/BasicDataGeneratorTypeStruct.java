package com.smartbear.ready.recipe.teststeps.datasource;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.smartbear.ready.recipe.teststeps.DataGeneratorTypeStruct;

public class BasicDataGeneratorTypeStruct extends DataGeneratorTypeStruct {

    @JsonCreator
    public BasicDataGeneratorTypeStruct(@JsonProperty("type") String type,
                                        @JsonProperty("propertyName") String propertyName,
                                        @JsonProperty("duplicationFactor") int duplicationFactor) {
        super(type, propertyName, duplicationFactor);
    }
}
