package com.smartbear.ready.recipe.teststeps.datasource;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.smartbear.ready.recipe.teststeps.DataGeneratorTypeStruct;
import io.swagger.annotations.ApiModel;

import static com.smartbear.ready.recipe.NullChecker.checkNotEmpty;

@ApiModel(value = "ValuesFromSetDataGenerator", description = "Values from set Data Generator definition")
public class ValuesFromSetDataGeneratorTypeStruct extends DataGeneratorTypeStruct {

    public String generationMode;
    public String[] values;

    @JsonCreator
    public ValuesFromSetDataGeneratorTypeStruct(@JsonProperty("type") String type,
                                                @JsonProperty("propertyName") String propertyName,
                                                @JsonProperty("duplicationFactor") int duplicationFactor,
                                                @JsonProperty("generationMode") String generationMode,
                                                @JsonProperty("values") String[] values) {
        super(type, propertyName, duplicationFactor);
        this.generationMode = generationMode;
        checkNotEmpty(values, "values");
        this.values = values;
    }
}
