package com.smartbear.ready.recipe.teststeps;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.smartbear.ready.recipe.teststeps.datasource.BasicDataGeneratorTypeStruct;
import com.smartbear.ready.recipe.teststeps.datasource.ComputerAddressDataGeneratorTypeStruct;
import com.smartbear.ready.recipe.teststeps.datasource.CustomStringDataGeneratorTypeStruct;
import com.smartbear.ready.recipe.teststeps.datasource.DateAndTimeDataGeneratorTypeStruct;
import com.smartbear.ready.recipe.teststeps.datasource.IntegerDataGeneratorTypeStruct;
import com.smartbear.ready.recipe.teststeps.datasource.NameDataGeneratorTypeStruct;
import com.smartbear.ready.recipe.teststeps.datasource.PhoneNumberDataGeneratorTypeStruct;
import com.smartbear.ready.recipe.teststeps.datasource.RealNumberDataGeneratorTypeStruct;
import com.smartbear.ready.recipe.teststeps.datasource.StateDataGeneratorTypeStruct;
import com.smartbear.ready.recipe.teststeps.datasource.StringDataGeneratorTypeStruct;
import com.smartbear.ready.recipe.teststeps.datasource.UKPostCodeDataGeneratorTypeStruct;
import com.smartbear.ready.recipe.teststeps.datasource.USZipCodeDataGeneratorTypeStruct;
import com.smartbear.ready.recipe.teststeps.datasource.ValuesFromSetDataGeneratorTypeStruct;
import io.swagger.annotations.ApiModel;

import static com.smartbear.ready.recipe.NullChecker.checkNotNull;
import static com.smartbear.ready.recipe.teststeps.DataGeneratorTypeStruct.BOOLEAN;
import static com.smartbear.ready.recipe.teststeps.DataGeneratorTypeStruct.CITY;
import static com.smartbear.ready.recipe.teststeps.DataGeneratorTypeStruct.COMPUTER_ADDRESS;
import static com.smartbear.ready.recipe.teststeps.DataGeneratorTypeStruct.COUNTRY;
import static com.smartbear.ready.recipe.teststeps.DataGeneratorTypeStruct.CUSTOM_STRING;
import static com.smartbear.ready.recipe.teststeps.DataGeneratorTypeStruct.DATE_AND_TIME;
import static com.smartbear.ready.recipe.teststeps.DataGeneratorTypeStruct.EMAIL;
import static com.smartbear.ready.recipe.teststeps.DataGeneratorTypeStruct.GUID;
import static com.smartbear.ready.recipe.teststeps.DataGeneratorTypeStruct.INTEGER;
import static com.smartbear.ready.recipe.teststeps.DataGeneratorTypeStruct.NAME;
import static com.smartbear.ready.recipe.teststeps.DataGeneratorTypeStruct.PHONE_NUMBER;
import static com.smartbear.ready.recipe.teststeps.DataGeneratorTypeStruct.REAL_NUMBER;
import static com.smartbear.ready.recipe.teststeps.DataGeneratorTypeStruct.SOCIAL_SECURITY_NUMBER;
import static com.smartbear.ready.recipe.teststeps.DataGeneratorTypeStruct.STATE;
import static com.smartbear.ready.recipe.teststeps.DataGeneratorTypeStruct.STREET_ADDRESS;
import static com.smartbear.ready.recipe.teststeps.DataGeneratorTypeStruct.STRING;
import static com.smartbear.ready.recipe.teststeps.DataGeneratorTypeStruct.UK_POST_CODE;
import static com.smartbear.ready.recipe.teststeps.DataGeneratorTypeStruct.US_ZIP_CODE;
import static com.smartbear.ready.recipe.teststeps.DataGeneratorTypeStruct.VALUES_FROM_SET;

@ApiModel(value = "DataGeneratorType", description = "Data Generator type definition")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = BooleanDataGeneratorTypeStruct.class, name = BOOLEAN),
        @JsonSubTypes.Type(value = ComputerAddressDataGeneratorTypeStruct.class, name = COMPUTER_ADDRESS),
        @JsonSubTypes.Type(value = NameDataGeneratorTypeStruct.class, name = NAME),
        @JsonSubTypes.Type(value = CustomStringDataGeneratorTypeStruct.class, name = CUSTOM_STRING),
        @JsonSubTypes.Type(value = StringDataGeneratorTypeStruct.class, name = STRING),
        @JsonSubTypes.Type(value = PhoneNumberDataGeneratorTypeStruct.class, name = PHONE_NUMBER),
        @JsonSubTypes.Type(value = StateDataGeneratorTypeStruct.class, name = STATE),
        @JsonSubTypes.Type(value = UKPostCodeDataGeneratorTypeStruct.class, name = UK_POST_CODE),
        @JsonSubTypes.Type(value = USZipCodeDataGeneratorTypeStruct.class, name = US_ZIP_CODE),
        @JsonSubTypes.Type(value = IntegerDataGeneratorTypeStruct.class, name = INTEGER),
        @JsonSubTypes.Type(value = RealNumberDataGeneratorTypeStruct.class, name = REAL_NUMBER),
        @JsonSubTypes.Type(value = ValuesFromSetDataGeneratorTypeStruct.class, name = VALUES_FROM_SET),
        @JsonSubTypes.Type(value = DateAndTimeDataGeneratorTypeStruct.class, name = DATE_AND_TIME),
        @JsonSubTypes.Type(value = BasicDataGeneratorTypeStruct.class, name = CITY),
        @JsonSubTypes.Type(value = BasicDataGeneratorTypeStruct.class, name = COUNTRY),
        @JsonSubTypes.Type(value = BasicDataGeneratorTypeStruct.class, name = STREET_ADDRESS),
        @JsonSubTypes.Type(value = BasicDataGeneratorTypeStruct.class, name = EMAIL),
        @JsonSubTypes.Type(value = BasicDataGeneratorTypeStruct.class, name = GUID),
        @JsonSubTypes.Type(value = BasicDataGeneratorTypeStruct.class, name = SOCIAL_SECURITY_NUMBER)
})
public class DataGeneratorTypeStruct {
    public static final String BOOLEAN = "Boolean";
    public static final String CITY = "City";
    public static final String COUNTRY = "Country";
    public static final String STREET_ADDRESS = "Street Address";
    public static final String EMAIL = "E-Mail";
    public static final String GUID = "Guid";
    public static final String SOCIAL_SECURITY_NUMBER = "Social Security Number";
    public static final String COMPUTER_ADDRESS = "Computer Address";
    public static final String NAME = "Name";
    public static final String CUSTOM_STRING = "Custom String";
    public static final String STRING = "String";
    public static final String PHONE_NUMBER = "Phone Number";
    public static final String STATE = "State";
    public static final String UK_POST_CODE = "United Kingdom Postcode";
    public static final String US_ZIP_CODE = "United States ZIP Code";
    public static final String INTEGER = "Integer";
    public static final String REAL_NUMBER = "Real";
    public static final String VALUES_FROM_SET = "Value from Set";
    public static final String DATE_AND_TIME = "Date and Time";

    public String type;
    public String propertyName;
    public int duplicationFactor;

    @JsonCreator
    public DataGeneratorTypeStruct(@JsonProperty("type") String type, @JsonProperty("propertyName") String propertyName,
                                   @JsonProperty("duplicationFactor") int duplicationFactor) {
        checkNotNull(type, "type");
        checkNotNull(propertyName, "propertyName");
        this.type = type;
        this.duplicationFactor = duplicationFactor;
        this.propertyName = propertyName;
    }
}
