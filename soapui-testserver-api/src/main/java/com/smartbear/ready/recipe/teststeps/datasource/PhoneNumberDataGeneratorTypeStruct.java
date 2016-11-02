package com.smartbear.ready.recipe.teststeps.datasource;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.smartbear.ready.recipe.teststeps.DataGeneratorTypeStruct;
import io.swagger.annotations.ApiModel;

@ApiModel(value = "PhoneNumberDataGenerator", description = "Phone Number Data Generator definition")
public class PhoneNumberDataGeneratorTypeStruct extends DataGeneratorTypeStruct {

    public String numberFormat;

    @JsonCreator
    public PhoneNumberDataGeneratorTypeStruct(@JsonProperty("type") String type,
                                              @JsonProperty("propertyName") String propertyName,
                                              @JsonProperty("duplicationFactor") int duplicationFactor,
                                              @JsonProperty("format") String numberFormat) {
        super(type, propertyName, duplicationFactor);
        this.numberFormat = numberFormat;
    }
}
