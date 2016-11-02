package com.smartbear.ready.recipe.assertions;

import com.eviware.soapui.security.assertion.ValidHttpStatusCodesAssertion;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.StringUtils;

import static com.smartbear.ready.recipe.NullChecker.checkNotEmpty;

/**
 * Captures the JSON configuration of a Valid HTTP Status codes assertion.
 */
@ApiModel(value = "ValidHttpStatusCodesAssertion", description = "Valid HTTP test step assertion definition")
public class ValidHttpStatusCodesAssertionStruct extends AssertionStruct<ValidHttpStatusCodesAssertion> {

    @ApiModelProperty(value = "validStatusCodes", allowableValues = "range[100, 509]")
    public String[] validStatusCodes;

    @JsonCreator
    public ValidHttpStatusCodesAssertionStruct(@JsonProperty("name") String name, @JsonProperty("validStatusCodes") String[] codes) {
        super(ValidHttpStatusCodesAssertion.LABEL, name);

        checkNotEmpty(codes, "validStatusCodes");

        validStatusCodes = codes;
    }

    @Override
    void configureAssertion(ValidHttpStatusCodesAssertion assertion) {
        assertion.setCodes(StringUtils.join(validStatusCodes, ','));
    }
}
