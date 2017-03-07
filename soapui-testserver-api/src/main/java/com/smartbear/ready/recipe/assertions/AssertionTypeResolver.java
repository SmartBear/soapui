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
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class AssertionTypeResolver implements TypeIdResolver {
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
        TypeFactory typeFactory = TypeFactory.defaultInstance();
        switch (type) {
            case ValidHttpStatusCodesAssertion.LABEL:
                return typeFactory.constructSpecializedType(baseType, ValidHttpStatusCodesAssertionStruct.class);
            case InvalidHttpStatusCodesAssertion.LABEL:
                return typeFactory.constructSpecializedType(baseType, InvalidHttpStatusCodesAssertionStruct.class);
            case SimpleContainsAssertion.LABEL:
                return typeFactory.constructSpecializedType(baseType, SimpleContainsAssertionStruct.class);
            case SimpleNotContainsAssertion.LABEL:
                return typeFactory.constructSpecializedType(baseType, SimpleNotContainsAssertionStruct.class);
            case XPathContainsAssertion.LABEL:
                return typeFactory.constructSpecializedType(baseType, XPathContainsAssertionStruct.class);
            case XQueryContainsAssertion.LABEL:
                return typeFactory.constructSpecializedType(baseType, XQueryContainsAssertionStruct.class);
            case JsonPathContentAssertion.LABEL:
                return typeFactory.constructSpecializedType(baseType, JsonPathContentAssertionStruct.class);
            case JsonPathExistenceAssertion.LABEL:
                return typeFactory.constructSpecializedType(baseType, JsonPathExistenceAssertionStruct.class);
            case JsonPathCountAssertion.LABEL:
                return typeFactory.constructSpecializedType(baseType, JsonPathCountAssertionStruct.class);
            case GroovyScriptAssertion.LABEL:
                return typeFactory.constructSpecializedType(baseType, GroovyScriptAssertionStruct.class);
            case ResponseSLAAssertion.LABEL:
                return typeFactory.constructSpecializedType(baseType, ResponseSLAAssertionStruct.class);
            case JdbcStatusAssertion.LABEL:
                return typeFactory.constructSpecializedType(baseType, JdbcStatusAssertionStruct.class);
            case JdbcTimeoutAssertion.LABEL:
                return typeFactory.constructSpecializedType(baseType, JdbcTimeoutAssertionStruct.class);
            case SchemaComplianceAssertion.LABEL:
                return typeFactory.constructSpecializedType(baseType, SchemaComplianceAssertionStruct.class);
            case SoapFaultAssertion.LABEL:
                return typeFactory.constructSpecializedType(baseType, SoapFaultAssertionStruct.class);
            case NotSoapFaultAssertion.LABEL:
                return typeFactory.constructSpecializedType(baseType, NotSoapFaultAssertionStruct.class);
            default:
                return handleUnknownAssertionType(type);
        }
    }

    private JavaType handleUnknownAssertionType(String type) {
        throw new IllegalArgumentException("Unknown assertion type: " + type);
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
