package com.smartbear.ready.recipe.teststeps.datasource;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.smartbear.ready.recipe.teststeps.DataGeneratorTypeStruct;
import io.swagger.annotations.ApiModel;

@ApiModel(value = "StringDataGenerator", description = "String Data Generator definition")
public class StringDataGeneratorTypeStruct extends DataGeneratorTypeStruct {
    public Integer minimumCharacters;
    public Integer maximumCharacters;
    public Boolean useLetters;
    public Boolean useDigits;
    public Boolean useSpaces;
    public Boolean usePunctuationMarks;

    @JsonCreator
    public StringDataGeneratorTypeStruct(@JsonProperty("type") String type,
                                         @JsonProperty("propertyName") String propertyName,
                                         @JsonProperty("duplicationFactor") int duplicationFactor,
                                         @JsonProperty("minimumCharacters") Integer minimumCharacters,
                                         @JsonProperty("maximumCharacters") Integer maximumCharacters,
                                         @JsonProperty("useLetters") Boolean useLetters,
                                         @JsonProperty("useDigits") Boolean useDigits,
                                         @JsonProperty("useSpaces") Boolean useSpaces,
                                         @JsonProperty("usePunctuationMarks") Boolean usePunctuationMarks) {
        super(type, propertyName, duplicationFactor);
        this.minimumCharacters = minimumCharacters;
        this.maximumCharacters = maximumCharacters;
        this.useLetters = useLetters;
        this.useDigits = useDigits;
        this.useSpaces = useSpaces;
        this.usePunctuationMarks = usePunctuationMarks;
    }
}
