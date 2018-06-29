package com.smartbear.ready.recipe;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;

import static com.smartbear.ready.recipe.NullChecker.checkNotNull;

@ApiModel(value = "PropertyTransferTarget", description = "Property transfer target")
public class PropertyTransferTarget {
    public String targetName;
    public String property;
    public String pathLanguage;
    public String path;

    @JsonCreator
    public PropertyTransferTarget(@JsonProperty("targetName") String targetName,
                                  @JsonProperty("property") String property,
                                  @JsonProperty("pathLanguage") String pathLanguage,
                                  @JsonProperty("path") String path) {
        checkNotNull(targetName, "targetName");
        checkNotNull(property, "property");

        this.targetName = targetName;
        this.property = property;
        this.pathLanguage = pathLanguage;
        this.path = path;
    }
}
