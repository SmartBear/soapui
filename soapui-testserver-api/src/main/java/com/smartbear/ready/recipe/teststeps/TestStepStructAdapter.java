package com.smartbear.ready.recipe.teststeps;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.smartbear.ready.recipe.TestStepNames;

import java.lang.reflect.Type;

/**
 * Type adapter to enable polymorphism (subclasses of an abstract class) in test step objects deserialized by GSON.
 */
public class TestStepStructAdapter implements JsonDeserializer<TestStepStruct> {

    @Override
    public TestStepStruct deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context)
            throws JsonParseException {
        JsonObject testStepObject = jsonElement.getAsJsonObject();
        String assertionType = testStepObject.get("type").getAsString();
        switch (assertionType) {
            case TestStepNames.REST_REQUEST_TYPE:
                return context.deserialize(testStepObject, RestTestRequestStepStruct.class);
            case TestStepNames.PROPERTY_TRANSFER_TYPE:
                return context.deserialize(testStepObject, PropertyTransferTestStepStruct.class);
            case TestStepNames.GROOVY_SCRIPT_TYPE:
                return context.deserialize(testStepObject, GroovyScriptTestStepStruct.class);
            case TestStepNames.DELAY_TYPE:
                return context.deserialize(testStepObject, DelayTestStepStruct.class);
            case TestStepNames.PROPERTIES_TYPE:
                return context.deserialize(testStepObject, PropertiesTestStepStruct.class);
            case TestStepNames.SOAP_MOCK_RESPONSE_TYPE:
                return context.deserialize(testStepObject, WsdlMockResponseStepStruct.class);
            case TestStepNames.JDBC_REQUEST_TYPE:
                return context.deserialize(testStepObject, JdbcRequestTestStepStruct.class);
            case TestStepNames.SOAP_REQUEST_TYPE:
                return context.deserialize(testStepObject, SoapTestRequestStepStruct.class);
            default:
                return context.deserialize(testStepObject, PluginTestStepStruct.class);
        }
    }

}
