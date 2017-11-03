package com.smartbear.ready.recipe.assertions;

import com.eviware.soapui.impl.wsdl.teststeps.WsdlMessageAssertion;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import io.swagger.annotations.ApiModel;
import org.apache.commons.lang3.StringUtils;

/**
 * Base class for the simple value classes used to deserialize JSON objects to Java.
 */
@ApiModel(value = "Assertion", description = "Test step assertion definition")
@JsonTypeIdResolver(AssertionTypeResolver.class)
@JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type", visible = true)
public abstract class AssertionStruct<T extends WsdlMessageAssertion> {
    public String type;
    public String name;

    @JsonCreator
    public AssertionStruct(@JsonProperty("type") String type, @JsonProperty("name") String name) {
        this.type = type;
        this.name = name;
    }

    public void setNameAndConfigureAssertion(T assertion) {
        if (StringUtils.isNotEmpty(name)) {
            assertion.setName(name);
        }
        configureAssertion(assertion);
    }

    abstract void configureAssertion(T assertion);
}
