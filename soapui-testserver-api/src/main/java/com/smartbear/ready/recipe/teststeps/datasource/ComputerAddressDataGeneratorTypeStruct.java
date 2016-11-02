package com.smartbear.ready.recipe.teststeps.datasource;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.smartbear.ready.recipe.teststeps.DataGeneratorTypeStruct;
import io.swagger.annotations.ApiModel;

@ApiModel(value = "ComputerAddressDataGenerator", description = "Computer Address Data Generator definition")
public class ComputerAddressDataGeneratorTypeStruct extends DataGeneratorTypeStruct {
    public String addressType;

    @JsonCreator
    public ComputerAddressDataGeneratorTypeStruct(@JsonProperty("type") String type,
                                                  @JsonProperty("propertyName") String propertyName,
                                                  @JsonProperty("duplicationFactor") int duplicationFactor,
                                                  @JsonProperty("addressType") String addressType) {
        super(type, propertyName, duplicationFactor);
        this.addressType = addressType;
    }
}
