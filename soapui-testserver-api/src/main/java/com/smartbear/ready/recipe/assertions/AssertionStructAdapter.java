package com.smartbear.ready.recipe.assertions;

import com.eviware.soapui.impl.wsdl.teststeps.assertions.basic.GroovyScriptAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.basic.ResponseSLAAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.basic.SchemaComplianceAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.basic.SimpleContainsAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.basic.SimpleNotContainsAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.basic.XPathContainsAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.basic.XQueryContainsAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.jdbc.JdbcStatusAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.jdbc.JdbcTimeoutAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.json.JsonPathContentAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.json.JsonPathCountAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.json.JsonPathExistenceAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.soap.NotSoapFaultAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.soap.SoapFaultAssertion;
import com.eviware.soapui.security.assertion.InvalidHttpStatusCodesAssertion;
import com.eviware.soapui.security.assertion.ValidHttpStatusCodesAssertion;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

/**
 * Type adapter to enable polymorphism (subclasses of an abstract class) in objects deserialized by GSON.
 */
public class AssertionStructAdapter implements JsonDeserializer<AssertionStruct> {

    @Override
    public AssertionStruct deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject assertionObject = jsonElement.getAsJsonObject();
        String assertionType = assertionObject.get("type").getAsString();
        JsonElement nameElement = assertionObject.get("name");
        String assertionName = nameElement != null ? nameElement.getAsString() : null;
        switch (assertionType) {
            case ValidHttpStatusCodesAssertion.LABEL:
                JsonArray codesArray = assertionObject.get("validStatusCodes").getAsJsonArray();
                String[] codes = makeStringArrayFromJsonArray(codesArray);
                return new ValidHttpStatusCodesAssertionStruct(assertionName, codes);
            case InvalidHttpStatusCodesAssertion.LABEL:
                JsonArray invalidCodesArray = assertionObject.get("invalidStatusCodes").getAsJsonArray();
                String[] invalidCodes = makeStringArrayFromJsonArray(invalidCodesArray);
                return new InvalidHttpStatusCodesAssertionStruct(assertionName, invalidCodes);
            case SimpleContainsAssertion.LABEL:
                return new SimpleContainsAssertionStruct(assertionName, assertionObject.get("token").getAsString(),
                        nullSafeGetBoolean(assertionObject, "ignoreCase"),
                        nullSafeGetBoolean(assertionObject, "useRegexp"));
            case SimpleNotContainsAssertion.LABEL:
                return new SimpleNotContainsAssertionStruct(assertionName, assertionObject.get("token").getAsString(),
                        nullSafeGetBoolean(assertionObject, "ignoreCase"),
                        nullSafeGetBoolean(assertionObject, "useRegexp"));
            case XPathContainsAssertion.LABEL:
                return new XPathContainsAssertionStruct(assertionName,
                        assertionObject.get("xpath").getAsString(),
                        assertionObject.get("expectedContent").getAsString(),
                        nullSafeGetBoolean(assertionObject, "allowWildcards"),
                        nullSafeGetBoolean(assertionObject, "ignoreNamespaces"),
                        nullSafeGetBoolean(assertionObject, "ignoreComments")
                );
            case XQueryContainsAssertion.LABEL:
                return new XQueryContainsAssertionStruct(assertionName,
                        assertionObject.get("xquery").getAsString(),
                        assertionObject.get("expectedContent").getAsString(),
                        nullSafeGetBoolean(assertionObject, "allowWildcards")
                );
            case JsonPathContentAssertion.LABEL:
                return new JsonPathContentAssertionStruct(assertionName,
                        assertionObject.get("jsonPath").getAsString(),
                        assertionObject.get("expectedContent").getAsString(),
                        nullSafeGetBoolean(assertionObject, "allowWildcards"));
            case JsonPathCountAssertion.LABEL:
                return new JsonPathCountAssertionStruct(assertionName,
                        assertionObject.get("jsonPath").getAsString(),
                        assertionObject.get("expectedCount").getAsString(),
                        nullSafeGetBoolean(assertionObject, "allowWildcards"));
            case JsonPathExistenceAssertion.LABEL:
                JsonElement expectedContentElement = assertionObject.get("expectedContent");
                return new JsonPathExistenceAssertionStruct(assertionName,
                        assertionObject.get("jsonPath").getAsString(),
                        expectedContentElement == null ? "true" : expectedContentElement.getAsString());
            case GroovyScriptAssertion.LABEL:
                return new GroovyScriptAssertionStruct(assertionName,
                        assertionObject.get("script").getAsString()
                );
            case ResponseSLAAssertion.LABEL:
                return new ResponseSLAAssertionStruct(assertionName,
                        assertionObject.get("maxResponseTime").getAsString()
                );
            case JdbcStatusAssertion.LABEL:
                return new JdbcStatusAssertionStruct(assertionName);
            case JdbcTimeoutAssertion.LABEL:
                return new JdbcTimeoutAssertionStruct(assertionName, assertionObject.get("timeout").getAsString());
            case SchemaComplianceAssertion.LABEL:
                return new SchemaComplianceAssertionStruct(assertionName);
            case SoapFaultAssertion.LABEL:
                return new SoapFaultAssertionStruct(assertionName);
            case NotSoapFaultAssertion.LABEL:
                return new NotSoapFaultAssertionStruct(assertionName);
            default:
                return handleUnknownAssertion(assertionType);

        }
    }

    private AssertionStruct handleUnknownAssertion(String assertionType) {
        throw new IllegalArgumentException("Unknown assertion type: " + assertionType);
    }

    private String[] makeStringArrayFromJsonArray(JsonArray codesArray) {
        String[] codes = new String[codesArray.size()];
        for (int i = 0; i < codes.length; i++) {
            codes[i] = codesArray.get(i).getAsString();
        }
        return codes;
    }

    private boolean nullSafeGetBoolean(JsonObject assertionObject, String propertyName) {
        JsonElement value = assertionObject.get(propertyName);
        return value != null && value.getAsBoolean();
    }
}
