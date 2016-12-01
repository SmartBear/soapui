/*
 * SoapUI, Copyright (C) 2004-2016 SmartBear Software 
 *
 * Licensed under the EUPL, Version 1.1 or - as soon as they will be approved by the European Commission - subsequent 
 * versions of the EUPL (the "Licence"); 
 * You may not use this work except in compliance with the Licence. 
 * You may obtain a copy of the Licence at: 
 * 
 * http://ec.europa.eu/idabc/eupl 
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is 
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either 
 * express or implied. See the Licence for the specific language governing permissions and limitations 
 * under the Licence. 
 */

package com.eviware.soapui.support;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.internal.PathToken;
import com.jayway.jsonpath.internal.PathTokenizer;
import com.jayway.jsonpath.internal.filter.PathTokenFilter;
import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.groovy.JsonSlurper;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.eviware.soapui.support.JsonUtil.isValidJson;

public class JsonPathFacade {

    private String currentJson;
    private Object jsonObject;

    public JsonPathFacade(String targetJson) {
        if (!isValidJson(targetJson)) {
            throw new IllegalArgumentException("Invalid JSON: " + targetJson);
        }
        this.currentJson = targetJson;
        jsonObject = new JsonSlurper().parseText(targetJson);
    }

    public String readStringValue(String jsonPathExpression) {
        return String.valueOf(readObjectValue(jsonPathExpression));
    }

    public void writeValue(String jsonPathExpression, Object value) {
        PlainJavaJsonProvider provider = new PlainJavaJsonProvider();
        Configuration configuration = Configuration.builder().jsonProvider(provider).build();
        jsonObject = provider.parse(currentJson);
        JsonPath path = JsonPath.compile(jsonPathExpression);
        LinkedList<PathToken> pathTokens = getPathTokensFrom(path);
        PathToken endToken = pathTokens.removeLast();
        int index = pathTokens.size();
        JsonWriteDecorator writeDecorator = new JsonWriteDecorator(provider, index, endToken, value);
        pathTokens.addLast(writeDecorator);
        path.read(jsonObject, configuration);
        jsonObject = MutableValue.FROM_MUTABLE_VALUE.apply(jsonObject);
        currentJson = buildJsonStringFrom(jsonObject);
    }

    private String buildJsonStringFrom(Object sourceObject) {
        Object json = makeJSONObject(sourceObject);
        return json instanceof JSON ? ((JSON) json).toString(3) : json.toString();
    }

    private Object makeJSONObject(Object sourceObject) {
        if (sourceObject instanceof Map) {
            JSONObject jsonObject = new JSONObject();
            Map sourceMap = (Map) sourceObject;
            for (Object key : sourceMap.keySet()) {
                jsonObject.put(key, makeJSONObject(sourceMap.get(key)));
            }
            return jsonObject;
        } else if (sourceObject instanceof List) {
            List sourceList = (List) sourceObject;
            JSONArray array = new JSONArray();
            for (Object element : sourceList) {
                array.add(makeJSONObject(element));
            }
            return array;
        } else {
            return sourceObject;
        }
    }

    private void removeMutableWrappersFrom(JSON jsonObject) {
        if (jsonObject.isArray()) {
            JSONArray array = (JSONArray) jsonObject;
            for (int i = 0; i < array.size(); i++) {
                array.set(i, removeMutableWrapperFrom(array.get(i)));
            }
        } else if (jsonObject instanceof JSONObject) {
            JSONObject object = (JSONObject) jsonObject;
            for (Object key : object.keySet()) {
                object.put(key, removeMutableWrapperFrom(object.get(key)));
            }
        }
    }

    private Object removeMutableWrapperFrom(Object o) {
        Object value = MutableValue.extractValueFromMutable(o);
        if (value instanceof JSON) {
            removeMutableWrappersFrom((JSON) value);
        }
        return value;
    }

    private LinkedList<PathToken> getPathTokensFrom(JsonPath jsonPathObject) {
        try {
            Field tokenizerField = JsonPath.class.getDeclaredField("tokenizer");
            tokenizerField.setAccessible(true);
            PathTokenizer tokenizer = (PathTokenizer) tokenizerField.get(jsonPathObject);
            Field pathTokensField = PathTokenizer.class.getDeclaredField("pathTokens");
            pathTokensField.setAccessible(true);
            return (LinkedList<PathToken>) pathTokensField.get(tokenizer);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public Object getJSON() {
        return jsonObject;
    }

    public String getCurrentJson() {
        return currentJson;
    }

    public <T> T readObjectValue(String jsonPathExpression) {
        PlainJavaJsonProvider provider = new PlainJavaJsonProvider();
        Configuration configuration = Configuration.builder().jsonProvider(provider).build();
        JsonPath jsonPath = JsonPath.compile(jsonPathExpression);
        return jsonPath.read(jsonObject, configuration);
    }

    private class JsonWriteDecorator extends PathToken {
        private final PlainJavaJsonProvider provider;
        private final Object value;

        public JsonWriteDecorator(PlainJavaJsonProvider provider, int index, PathToken endToken, Object value) {
            super(endToken.getFragment(), index, true);
            this.provider = provider;
            this.value = value;
        }

        @Override
        public PathTokenFilter getFilter() {
            // WORKAROUND: ideally we would use a decorator PathTokenFilter instead, but the PathTokenFilter constructor
            // is package protected. Unfortunately this entails that we can't reuse the provider after this step.
            provider.setValueToWrite(value);
            return super.getFilter();
        }

    }
}
