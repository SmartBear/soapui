package com.smartbear.ready.recipe.teststeps;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.type.TypeFactory;

import static com.smartbear.ready.recipe.TestStepNames.DELAY_TYPE;
import static com.smartbear.ready.recipe.TestStepNames.GROOVY_SCRIPT_TYPE;
import static com.smartbear.ready.recipe.TestStepNames.JDBC_REQUEST_TYPE;
import static com.smartbear.ready.recipe.TestStepNames.PROPERTIES_TYPE;
import static com.smartbear.ready.recipe.TestStepNames.PROPERTY_TRANSFER_TYPE;
import static com.smartbear.ready.recipe.TestStepNames.REST_REQUEST_TYPE;
import static com.smartbear.ready.recipe.TestStepNames.SOAP_REQUEST_TYPE;
import static com.smartbear.ready.recipe.TestStepNames.SOAP_MOCK_RESPONSE_TYPE;

class TestStepTypeResolver implements TypeIdResolver {
    private JavaType baseType;

    @Override
    public void init(JavaType javaType) {
        baseType = javaType;
    }

    @Override
    public String idFromValue(Object object) {
        return idFromValueAndType(object, object.getClass());
    }

    @Override
    public String idFromValueAndType(Object object, Class<?> clazz) {
        try {
            return (String) clazz.getField("type").get(object);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public String idFromBaseType() {
        return idFromValueAndType(null, baseType.getRawClass());
    }

    @Override
    public JavaType typeFromId(String type) {
        switch (type) {
            case REST_REQUEST_TYPE:
                return TypeFactory.defaultInstance().constructSpecializedType(baseType, RestTestRequestStepStruct.class);
            case SOAP_REQUEST_TYPE:
                return TypeFactory.defaultInstance().constructSpecializedType(baseType, SoapTestRequestStepStruct.class);
            case PROPERTY_TRANSFER_TYPE:
                return TypeFactory.defaultInstance().constructSpecializedType(baseType, PropertyTransferTestStepStruct.class);
            case GROOVY_SCRIPT_TYPE:
                return TypeFactory.defaultInstance().constructSpecializedType(baseType, GroovyScriptTestStepStruct.class);
            case DELAY_TYPE:
                return TypeFactory.defaultInstance().constructSpecializedType(baseType, DelayTestStepStruct.class);
            case PROPERTIES_TYPE:
                return TypeFactory.defaultInstance().constructSpecializedType(baseType, PropertiesTestStepStruct.class);
            case SOAP_MOCK_RESPONSE_TYPE:
                return TypeFactory.defaultInstance().constructSpecializedType(baseType, WsdlMockResponseStepStruct.class);
            case JDBC_REQUEST_TYPE:
                return TypeFactory.defaultInstance().constructSpecializedType(baseType, JdbcRequestTestStepStruct.class);
            default:
                return TypeFactory.defaultInstance().constructSpecializedType(baseType, PluginTestStepStruct.class);
        }
    }

    @Override
    public JavaType typeFromId(DatabindContext databindContext, String type) {
        return typeFromId(type);
    }

    @Override
    public JsonTypeInfo.Id getMechanism() {
        return JsonTypeInfo.Id.CUSTOM;
    }
}
