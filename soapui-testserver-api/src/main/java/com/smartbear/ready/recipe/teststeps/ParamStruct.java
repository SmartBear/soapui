package com.smartbear.ready.recipe.teststeps;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;

import static com.smartbear.ready.recipe.NullChecker.checkNotNull;

/**
 * Used to deserialize JSON objects representing REST request parameters to Java.
 */
@ApiModel(value="Parameter", description="Parameter definition")
public class ParamStruct {

    @JsonCreator
    public ParamStruct(@JsonProperty("type") String type, @JsonProperty("name")  String name, @JsonProperty("value")  String value) {
        checkNotNull(name, "name");

        this.type = type;
        this.name = name;
        this.value = value;
    }

    public String type;
    public String name;
    public String value;
}
