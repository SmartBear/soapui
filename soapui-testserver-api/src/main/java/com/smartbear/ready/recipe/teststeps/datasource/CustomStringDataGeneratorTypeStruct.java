package com.smartbear.ready.recipe.teststeps.datasource;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.smartbear.ready.recipe.teststeps.DataGeneratorTypeStruct;
import io.swagger.annotations.ApiModel;

import static com.smartbear.ready.recipe.NullChecker.checkNotNull;

@ApiModel(value = "ComputerAddressDataGenerator", description = "Computer Address Data Generator definition")
public class CustomStringDataGeneratorTypeStruct extends DataGeneratorTypeStruct {
    public String value;

    @JsonCreator
    public CustomStringDataGeneratorTypeStruct(@JsonProperty("type") String type,
                                               @JsonProperty("propertyName") String propertyName,
                                               @JsonProperty("duplicationFactor") int duplicationFactor,
                                               @JsonProperty("value") String value) {
        super(type, propertyName, duplicationFactor);
        checkNotNull(value, "value");
        this.value = value;
    }
}
