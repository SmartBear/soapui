package com.smartbear.ready.recipe.teststeps;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;

/**
 * Struct capturing values from JSON for building a Property transfer test step.
 */
@ApiModel(value="PropertyTransferTestStep", description="Property TransferTestStep source test step definition")
public class PropertyTransferTestStepStruct extends TestStepStruct {
    public PropertyTransferStruct[] transfers;

    @JsonCreator
    public PropertyTransferTestStepStruct(@JsonProperty("type") String type,
                                          @JsonProperty("name") String name,
                                          @JsonProperty("transfers") PropertyTransferStruct[] transfers) {
        super(type, name);
        this.transfers = transfers;
    }
}
