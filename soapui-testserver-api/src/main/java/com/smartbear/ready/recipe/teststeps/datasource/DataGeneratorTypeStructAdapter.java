package com.smartbear.ready.recipe.teststeps.datasource;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.smartbear.ready.recipe.teststeps.BooleanDataGeneratorTypeStruct;
import com.smartbear.ready.recipe.teststeps.DataGeneratorTypeStruct;

import java.lang.reflect.Type;

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

public class DataGeneratorTypeStructAdapter implements JsonDeserializer<DataGeneratorTypeStruct> {
    @Override
    public DataGeneratorTypeStruct deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject testStepObject = jsonElement.getAsJsonObject();
        String dataGeneratorType = testStepObject.get("type").getAsString();
        switch (dataGeneratorType) {
            case CITY:
            case COUNTRY:
            case EMAIL:
            case SOCIAL_SECURITY_NUMBER:
            case STREET_ADDRESS:
            case GUID:
                return context.deserialize(testStepObject, BasicDataGeneratorTypeStruct.class);
            case BOOLEAN:
                return context.deserialize(testStepObject, BooleanDataGeneratorTypeStruct.class);
            case COMPUTER_ADDRESS:
                return context.deserialize(testStepObject, ComputerAddressDataGeneratorTypeStruct.class);
            case NAME:
                return context.deserialize(testStepObject, NameDataGeneratorTypeStruct.class);
            case CUSTOM_STRING:
                return context.deserialize(testStepObject, CustomStringDataGeneratorTypeStruct.class);
            case STRING:
                return context.deserialize(testStepObject, StringDataGeneratorTypeStruct.class);
            case PHONE_NUMBER:
                return context.deserialize(testStepObject, PhoneNumberDataGeneratorTypeStruct.class);
            case STATE:
                return context.deserialize(testStepObject, StateDataGeneratorTypeStruct.class);
            case UK_POST_CODE:
                return context.deserialize(testStepObject, UKPostCodeDataGeneratorTypeStruct.class);
            case US_ZIP_CODE:
                return context.deserialize(testStepObject, USZipCodeDataGeneratorTypeStruct.class);
            case INTEGER:
                return context.deserialize(testStepObject, IntegerDataGeneratorTypeStruct.class);
            case REAL_NUMBER:
                return context.deserialize(testStepObject, RealNumberDataGeneratorTypeStruct.class);
            case VALUES_FROM_SET:
                return context.deserialize(testStepObject, ValuesFromSetDataGeneratorTypeStruct.class);
            case DATE_AND_TIME:
                return context.deserialize(testStepObject, DateAndTimeDataGeneratorTypeStruct.class);
            default:
                throw new IllegalArgumentException("Invalid data generator type: " + dataGeneratorType);
        }
    }
}
