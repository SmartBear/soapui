package com.smartbear.ready.recipe.teststeps.datasource;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.smartbear.ready.recipe.teststeps.DataGeneratorTypeStruct;
import io.swagger.annotations.ApiModel;

@ApiModel(value = "USZipCodeDataGenerator", description = "United States Zip Code Data Generator definition")
public class USZipCodeDataGeneratorTypeStruct extends DataGeneratorTypeStruct {

    public String codeFormat;

    @JsonCreator
    public USZipCodeDataGeneratorTypeStruct(@JsonProperty("type") String type,
                                            @JsonProperty("propertyName") String propertyName,
                                            @JsonProperty("duplicationFactor") int duplicationFactor,
                                            @JsonProperty("codeFormat") String codeFormat) {
        super(type, propertyName, duplicationFactor);
        this.codeFormat = codeFormat;
    }
}
