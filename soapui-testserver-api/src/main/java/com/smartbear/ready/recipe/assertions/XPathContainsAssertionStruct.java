package com.smartbear.ready.recipe.assertions;

import com.eviware.soapui.impl.wsdl.teststeps.assertions.basic.XPathContainsAssertion;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;

import static com.smartbear.ready.recipe.NullChecker.checkNotNull;

/**
 * Captures the JSON configuration of an XPath contains assertion.
 */
@ApiModel(value = "XPathContainsAssertion", description = "XPath contains assertion definition")
public class XPathContainsAssertionStruct extends AssertionStruct<XPathContainsAssertion> {

    public final String xpath;
    public final String expectedContent;
    public final boolean allowWildcards;
    public final boolean ignoreNamespaces;
    public final boolean ignoreComments;

    @JsonCreator
    public XPathContainsAssertionStruct(@JsonProperty("name") String name, @JsonProperty("xpath") String xpath, @JsonProperty("expectedContent") String expectedContent,
                                        @JsonProperty("allowWildcards") boolean allowWildcards, @JsonProperty("ignoreNamespaces") boolean ignoreNamespaces,
                                        @JsonProperty("ignoreComments") boolean ignoreComments) {
        super(XPathContainsAssertion.LABEL, name);

        checkNotNull(xpath, "xpath");
        checkNotNull(expectedContent, "expectedContent");

        this.xpath = xpath;
        this.expectedContent = expectedContent;
        this.allowWildcards = allowWildcards;
        this.ignoreNamespaces = ignoreNamespaces;
        this.ignoreComments = ignoreComments;
    }

    @Override
    void configureAssertion(XPathContainsAssertion assertion) {
        assertion.setPath(xpath);
        assertion.setExpectedContent(expectedContent);
        assertion.setAllowWildcards(allowWildcards);
        assertion.setIgnoreNamespaceDifferences(ignoreNamespaces);
        assertion.setIgnoreComments(ignoreComments);
    }
}
