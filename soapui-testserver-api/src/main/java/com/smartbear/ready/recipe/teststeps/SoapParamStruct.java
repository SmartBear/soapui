package com.smartbear.ready.recipe.teststeps;

import com.eviware.soapui.support.StringUtils;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Used to deserialize JSON objects representing REST request parameters to Java.
 */
@ApiModel(value = "SoapParameter", description = "Parameter definition")
public class SoapParamStruct {

    @JsonCreator
    public SoapParamStruct(@JsonProperty("path") String path, @JsonProperty("name") String name, @JsonProperty("value") String value) {
        checkArgument(StringUtils.hasContent(path) || StringUtils.hasContent(name), "Either name or path must be specified");

        this.path = path;
        this.name = name;
        this.value = value;
    }

    public String path;
    public String name;
    public String value;
}
