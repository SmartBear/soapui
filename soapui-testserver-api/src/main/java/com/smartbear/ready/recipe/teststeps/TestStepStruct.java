package com.smartbear.ready.recipe.teststeps;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import io.swagger.annotations.ApiModel;

import java.util.Map;

/**
 * Base class for struct classes used to deserialize test step JSON objects to Java.
 */
@ApiModel(value = "TestStep", description = "Test step definition")
@JsonTypeIdResolver(TestStepTypeResolver.class)
@JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type", visible = true)
public class TestStepStruct {
    @JsonIgnore
    public int index;
    public String type;
    public String name;
    public Map<String, Object> configuration;//Used only for Plugin provided test steps

    /**
     * Used by sub classes
     */
    public TestStepStruct(String type, String name) {
        this.type = type;
        this.name = name;
    }

    /**
     * Used by Jackson library and <code>PluginTestStepStruct</code>
     */
    @JsonCreator
    public TestStepStruct(@JsonProperty("type") String type, @JsonProperty("name") String name, @JsonProperty("configuration") Map<String, Object> configuration) {
        this.type = type;
        this.name = name;
        this.configuration = configuration;
    }
}
