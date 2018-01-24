package com.smartbear.ready.recipe;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;

import static com.smartbear.ready.recipe.NullChecker.checkNotNull;

@ApiModel(value = "PropertyTransferSource", description = "Property transfer source")
public class PropertyTransferSource {
    public String sourceName;
    public String property;
    public String pathLanguage;
    public String path;

    @JsonCreator
    public PropertyTransferSource(
            @JsonProperty("sourceName") String sourceName,
            @JsonProperty("property") String property,
            @JsonProperty("pathLanguage") String pathLanguage,
            @JsonProperty("path") String path) {
        checkNotNull(sourceName, "sourceName");
        checkNotNull(property, "property");

        this.sourceName = sourceName;
        this.property = property;
        this.pathLanguage = pathLanguage;
        this.path = path;
    }
}
