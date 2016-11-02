package com.smartbear.ready.recipe.teststeps;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;

@ApiModel(value = "DataGeneratorDataSource", description = "Data Generator data source definition")
public class DataGeneratorDataSourceStruct {
    public String numberOfRows;
    public DataGeneratorTypeStruct[] dataGenerators;

    @JsonCreator
    public DataGeneratorDataSourceStruct(@JsonProperty("numberOfRows") String numberOfRows,
                                         @JsonProperty("dataGenerators") DataGeneratorTypeStruct[] dataGenerators) {
        this.numberOfRows = numberOfRows;
        this.dataGenerators = dataGenerators;
    }
}

