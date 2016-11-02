package com.smartbear.ready.recipe.teststeps.datasource;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.smartbear.ready.recipe.teststeps.DataGeneratorTypeStruct;
import io.swagger.annotations.ApiModel;

@ApiModel(value = "RealAddressDataGenerator", description = "Real number Data Generator definition")
public class RealNumberDataGeneratorTypeStruct extends DataGeneratorTypeStruct {

    public Double minimumValue;
    public Double maximumValue;
    public String generationMode;
    public Integer decimalPlaces;
    public Double incrementBy;

    @JsonCreator
    public RealNumberDataGeneratorTypeStruct(@JsonProperty("type") String type,
                                             @JsonProperty("propertyName") String propertyName,
                                             @JsonProperty("duplicationFactor") int duplicationFactor,
                                             @JsonProperty("minimumValue") Double minimumValue,
                                             @JsonProperty("maximumValue") Double maximumValue,
                                             @JsonProperty("generationMode") String generationMode,
                                             @JsonProperty("decimalPlaces") Integer decimalPlaces,
                                             @JsonProperty("incrementBy") Double incrementBy) {
        super(type, propertyName, duplicationFactor);
        this.minimumValue = minimumValue;
        this.maximumValue = maximumValue;
        this.generationMode = generationMode;
        this.decimalPlaces = decimalPlaces;
        this.incrementBy = incrementBy;
    }
}
