package com.smartbear.ready.recipe.assertions;

import com.eviware.soapui.impl.wsdl.teststeps.assertions.json.JsonPathCountAssertion;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;

import static com.smartbear.ready.recipe.NullChecker.checkNotNull;

/**
 * Captures a JsonPath count assertion in JSON format.
 */
@ApiModel(value = "JsonPathCountAssertion", description = "JsonPath count assertion definition")
public class JsonPathCountAssertionStruct extends AssertionStruct<JsonPathCountAssertion> {

    public final String jsonPath;
    public final String expectedCount;
    public final boolean allowWildcards;

    @JsonCreator
    public JsonPathCountAssertionStruct(@JsonProperty("name") String name, @JsonProperty("jsonPath") String jsonPath, @JsonProperty("expectedCount") String expectedCount, @JsonProperty("allowWildcards") boolean allowWildcards) {
        super(JsonPathCountAssertion.LABEL, name);
        checkNotNull(jsonPath, "jsonPath");
        checkNotNull(expectedCount, "expectedCount");

        this.jsonPath = jsonPath;
        this.expectedCount = expectedCount;
        this.allowWildcards = allowWildcards;
    }

    @Override
    void configureAssertion(JsonPathCountAssertion assertion) {
        assertion.setPath(jsonPath);
        assertion.setExpectedContent(expectedCount);
        assertion.setAllowWildcards(allowWildcards);
    }
}
