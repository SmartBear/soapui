package com.smartbear.ready.recipe.assertions;

import com.eviware.soapui.security.assertion.InvalidHttpStatusCodesAssertion;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.StringUtils;

import static com.smartbear.ready.recipe.NullChecker.checkNotEmpty;

/**
 * Captures the JSON configuration of an Invalid HTTP Status codes assertion.
 */
@ApiModel(value = "InvalidHttpStatusCodesAssertion", description = "Invalid Http status codes assertion definition")
public class InvalidHttpStatusCodesAssertionStruct extends AssertionStruct<InvalidHttpStatusCodesAssertion> {

    @ApiModelProperty(value = "invalidStatusCodes", allowableValues = "range[100, 509]")
    public String[] invalidStatusCodes;

    @JsonCreator
    public InvalidHttpStatusCodesAssertionStruct(@JsonProperty("name") String name, @JsonProperty("invalidStatusCodes") String[] codes) {
        super(InvalidHttpStatusCodesAssertion.LABEL, name);

        checkNotEmpty(codes, "invalidStatusCodes");

        invalidStatusCodes = codes;
    }

    @Override
    void configureAssertion(InvalidHttpStatusCodesAssertion assertion) {
        assertion.setCodes(StringUtils.join(invalidStatusCodes, ','));
    }
}
