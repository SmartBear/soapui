package com.smartbear.ready.recipe.assertions;

import com.eviware.soapui.impl.wsdl.teststeps.assertions.basic.XQueryContainsAssertion;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;

import static com.smartbear.ready.recipe.NullChecker.checkNotNull;

/**
 * Captures a XQuery Contains assertion in JSON format.
 */
@ApiModel(value = "XQueryContainsAssertion", description = "XQuery contains assertion definition")
public class XQueryContainsAssertionStruct extends AssertionStruct<XQueryContainsAssertion> {

    public final String xquery;
    public final String expectedContent;
    public final boolean allowWildcards;

    @JsonCreator
    public XQueryContainsAssertionStruct(@JsonProperty("name") String name, @JsonProperty("xquery") String xquery,
                                         @JsonProperty("expectedContent") String expectedContent,
                                         @JsonProperty("allowWildcards") boolean allowWildcards) {
        super(XQueryContainsAssertion.LABEL, name);

        checkNotNull(xquery, "xquery");
        checkNotNull(expectedContent, "expectedContent");

        this.xquery = xquery;
        this.expectedContent = expectedContent;
        this.allowWildcards = allowWildcards;
    }

    @Override
    void configureAssertion(XQueryContainsAssertion assertion) {
        assertion.setPath(xquery);
        assertion.setExpectedContent(expectedContent);
        assertion.setAllowWildcards(allowWildcards);
    }
}
