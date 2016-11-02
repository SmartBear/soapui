package com.smartbear.ready.recipe.teststeps.datasource;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.smartbear.ready.recipe.teststeps.DataGeneratorTypeStruct;
import io.swagger.annotations.ApiModel;

@ApiModel(value = "NameDataGenerator", description = "Name Data Generator definition")
public class NameDataGeneratorTypeStruct extends DataGeneratorTypeStruct {

    public String gender;
    public String nameType;

    @JsonCreator
    public NameDataGeneratorTypeStruct(@JsonProperty("type") String type,
                                       @JsonProperty("propertyName") String propertyName,
                                       @JsonProperty("duplicationFactor") int duplicationFactor,
                                       @JsonProperty("gender") String gender,
                                       @JsonProperty("nameType") String nameType) {
        super(type, propertyName, duplicationFactor);
        this.gender = gender;
        this.nameType = nameType;
    }
}
