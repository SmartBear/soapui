package com.smartbear.ready.recipe.teststeps.datasource;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.smartbear.ready.recipe.teststeps.DataGeneratorTypeStruct;
import io.swagger.annotations.ApiModel;

@ApiModel(value = "DateAndTimeDataGenerator", description = "Date and Time Data Generator definition")
public class DateAndTimeDataGeneratorTypeStruct extends DataGeneratorTypeStruct {

    public String dateTimeFormat;
    public String generationMode;
    public String minimumValue;
    public String maximumValue;
    public Integer incrementValueDay;
    public Integer incrementValueHour;
    public Integer incrementValueMinute;
    public Integer incrementValueSecond;

    @JsonCreator
    public DateAndTimeDataGeneratorTypeStruct(@JsonProperty("type") String type,
                                              @JsonProperty("propertyName") String propertyName,
                                              @JsonProperty("duplicationFactor") int duplicationFactor,
                                              @JsonProperty("dateTimeFormat") String dateTimeFormat,
                                              @JsonProperty("generationMode") String generationMode,
                                              @JsonProperty("minimumValue") String minimumValue,
                                              @JsonProperty("maximumValue") String maximumValue,
                                              @JsonProperty("incrementValueDay") Integer incrementValueDay,
                                              @JsonProperty("incrementValueHour") Integer incrementValueHour,
                                              @JsonProperty("incrementValueMinute") Integer incrementValueMinute,
                                              @JsonProperty("incrementValueSecond") Integer incrementValueSecond) {
        super(type, propertyName, duplicationFactor);
        this.dateTimeFormat = dateTimeFormat;
        this.generationMode = generationMode;
        this.minimumValue = minimumValue;
        this.maximumValue = maximumValue;
        this.incrementValueDay = incrementValueDay;
        this.incrementValueHour = incrementValueHour;
        this.incrementValueMinute = incrementValueMinute;
        this.incrementValueSecond = incrementValueSecond;
    }
}
