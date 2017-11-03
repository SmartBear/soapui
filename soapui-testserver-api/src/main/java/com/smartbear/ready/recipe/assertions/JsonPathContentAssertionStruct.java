package com.smartbear.ready.recipe.assertions;

import com.eviware.soapui.impl.wsdl.teststeps.assertions.json.JsonPathContentAssertion;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;

import static com.smartbear.ready.recipe.NullChecker.checkNotNull;

/**
 * Captures a JsonPath Contains assertion in JSON format.
 */
@ApiModel(value = "JsonPathContentAssertion", description = "JsonPath content assertion definition")
public class JsonPathContentAssertionStruct extends AssertionStruct<JsonPathContentAssertion> {

    public final String jsonPath;
    public final String expectedContent;
    public final boolean allowWildcards;

    @JsonCreator
    public JsonPathContentAssertionStruct(@JsonProperty("name") String name, @JsonProperty("jsonPath") String jsonPath, @JsonProperty("expectedContent") String expectedContent, @JsonProperty("allowWildcards") boolean allowWildcards) {
        super(JsonPathContentAssertion.LABEL, name);

        checkNotNull(jsonPath, "jsonPath");
        checkNotNull(expectedContent, "expectedContent");

        this.jsonPath = jsonPath;
        this.expectedContent = expectedContent;
        this.allowWildcards = allowWildcards;
    }

    @Override
    void configureAssertion(JsonPathContentAssertion assertion) {
        assertion.setPath(jsonPath);
        assertion.setExpectedContent(expectedContent);
        assertion.setAllowWildcards(allowWildcards);
    }
}
