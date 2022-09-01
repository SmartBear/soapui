/*
 * SoapUI, Copyright (C) 2004-2022 SmartBear Software
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
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.groovy.JsonSlurper;

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
        Object data = readObjectValue(jsonPathExpression);
        return String.valueOf(data);
    }

    public void writeValue(String jsonPathExpression, Object value) {
        PlainJavaJsonProvider provider = new PlainJavaJsonProvider();
        Configuration configuration = Configuration.builder().jsonProvider(provider).build();
        DocumentContext documentContext = JsonPath.parse(currentJson);
        JsonPath path = JsonPath.compile(jsonPathExpression);
        documentContext.set(path,value);
        currentJson = documentContext.jsonString();
        jsonObject = new JsonSlurper().parseText(currentJson);
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

}
