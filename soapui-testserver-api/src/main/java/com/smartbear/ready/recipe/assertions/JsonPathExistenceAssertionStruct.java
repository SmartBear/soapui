package com.smartbear.ready.recipe.assertions;

import com.eviware.soapui.impl.wsdl.teststeps.assertions.json.JsonPathExistenceAssertion;
import com.fasterxml.jackson.annotation.JsonProperty;

public class JsonPathExistenceAssertionStruct extends AssertionStruct<JsonPathExistenceAssertion> {
    private final String jsonPath;
    private final String expectedContent;

    public JsonPathExistenceAssertionStruct(@JsonProperty("name") String name, @JsonProperty("jsonPath") String jsonPath,
                                            @JsonProperty("expectedContent") String expectedContent) {
        super(JsonPathExistenceAssertion.LABEL, name);
        this.jsonPath = jsonPath;
        this.expectedContent = expectedContent;
    }

    @Override
    void configureAssertion(JsonPathExistenceAssertion assertion) {
        assertion.setPath(jsonPath);
        assertion.setExpectedContent(expectedContent);
    }
}
