package com.smartbear.ready.recipe.teststeps;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.smartbear.ready.recipe.PropertyTransferSource;
import com.smartbear.ready.recipe.PropertyTransferTarget;
import io.swagger.annotations.ApiModel;

import static com.smartbear.ready.recipe.NullChecker.checkNotNull;

@ApiModel(value = "PropertyTransfer",
        description = "Property transfer definition")
public class PropertyTransferStruct {
    public String transferName;
    public PropertyTransferSource source;
    public PropertyTransferTarget target;

    public boolean failTransferOnError;
    public boolean setNullOnMissingSource;
    public boolean transferTextContent;
    public boolean ignoreEmptyValue;
    public boolean transferToAll;
    public boolean transferChildNodes;
    public boolean entitizeTransferredValues;

    @JsonCreator
    public PropertyTransferStruct(@JsonProperty("transferName")String transferName,
                                  @JsonProperty("source")PropertyTransferSource source,
                                  @JsonProperty("target")PropertyTransferTarget target,
                                  @JsonProperty("failTransferOnError")boolean failTransferOnError,
                                  @JsonProperty("setNullOnMissingSource")boolean setNullOnMissingSource,
                                  @JsonProperty("transferTextContent")boolean transferTextContent,
                                  @JsonProperty("ignoreEmptyValue")boolean ignoreEmptyValue,
                                  @JsonProperty("transferToAll")boolean transferToAll,
                                  @JsonProperty("transferChildNodes")boolean transferChildNodes,
                                  @JsonProperty("entitizeTransferredValues")boolean entitizeTransferredValues) {
        checkNotNull(source, "source");
        checkNotNull(target, "target");

        this.transferName = transferName;
        this.source = source;
        this.target = target;
        this.failTransferOnError = failTransferOnError;
        this.setNullOnMissingSource = setNullOnMissingSource;
        this.transferTextContent = transferTextContent;
        this.ignoreEmptyValue = ignoreEmptyValue;
        this.transferToAll = transferToAll;
        this.transferChildNodes = transferChildNodes;
        this.entitizeTransferredValues = entitizeTransferredValues;
    }
}
