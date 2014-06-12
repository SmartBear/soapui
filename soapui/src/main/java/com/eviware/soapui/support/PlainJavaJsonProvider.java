/*
 *  SoapUI, copyright (C) 2004-2014 smartbear.com
 *
 *  SoapUI is free software; you can redistribute it and/or modify it under the
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  SoapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */
package com.eviware.soapui.support;

import com.jayway.jsonpath.InvalidJsonException;
import com.jayway.jsonpath.spi.Mode;
import com.jayway.jsonpath.spi.impl.AbstractJsonProvider;
import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.groovy.JsonSlurper;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PlainJavaJsonProvider extends AbstractJsonProvider {

    private JsonSlurper jsonSlurper = new JsonSlurper();
    private Object valueToWrite;

    public void setValueToWrite(Object valueToWrite) {
        this.valueToWrite = valueToWrite;
    }

    @Override
    public Mode getMode() {
        return Mode.SLACK;
    }

    @Override
    public Object parse(String json) throws InvalidJsonException {
        return parse(new StringReader(json));
    }


    @Override
    public Object parse(Reader jsonReader) throws InvalidJsonException {
        try {
            JSON jsonRoot = jsonSlurper.parse(jsonReader);
            Object converted = convertToPlainJavaImplementation(jsonRoot);
            return MutableValue.TO_MUTABLE_VALUE.apply(converted);
        } catch (Exception e) {
            throw new InvalidJsonException(e);
        }
    }

    @Override
    public Object parse(InputStream jsonStream) throws InvalidJsonException {
        return parse(new BufferedReader(new InputStreamReader(jsonStream)));
    }

    @Override
    public String toJson(Object obj) {
        return ((JSON) obj).toString(3);
    }

    @Override
    public Object createMap() {
        return new LinkedHashMap();
    }

    @Override
    public Iterable createArray() {
        return new ArrayList();
    }

    @Override
    public boolean isArray(Object obj) {
        return MutableValue.extractValueFromMutable(obj) instanceof List;
    }

    @Override
    public boolean isContainer(Object obj) {
        return super.isContainer(obj);
    }

    @Override
    public boolean isMap(Object obj) {
        return MutableValue.extractValueFromMutable(obj) instanceof Map;
    }

    @Override
    public Object getProperty(Object obj, Object key) {
        Object oldValue = super.getProperty(MutableValue.extractValueFromMutable(obj), key);
        if (oldValue instanceof MutableValue && valueToWrite != null) {
            ((MutableValue) oldValue).setValue(valueToWrite);
            return valueToWrite;
        }
        return MutableValue.extractValueFromMutable(oldValue);
    }

    @Override
    public void setProperty(Object obj, Object key, Object value) {
        super.setProperty(MutableValue.extractValueFromMutable(obj), key, value);
    }

    @Override
    public Collection<String> getPropertyKeys(Object obj) {
        return super.getPropertyKeys(MutableValue.extractValueFromMutable(obj));
    }

    @Override
    public int length(Object obj) {
        return super.length(MutableValue.extractValueFromMutable(obj));
    }

    @Override
    public Iterable<Object> toIterable(Object obj) {
        return super.toIterable(MutableValue.extractValueFromMutable(obj));
    }

    private Object convertToPlainJavaImplementation(JSON jsonRoot) {
        if (jsonRoot.isArray()) {
            List<Object> returnedList = new ArrayList<Object>();
            JSONArray array = (JSONArray) jsonRoot;
            for (Object originalValue : array) {
                if (originalValue instanceof JSON) {
                    returnedList.add(convertToPlainJavaImplementation((JSON) originalValue));
                } else {
                    returnedList.add(originalValue);
                }
            }
            return returnedList;
        } else if (jsonRoot instanceof JSONObject) {
            Map<Object, Object> returnedMap = new HashMap<Object, Object>();
            JSONObject jsonObject = (JSONObject) jsonRoot;
            for (Object o : jsonObject.keySet()) {
                Object value = jsonObject.get(o);
                if (value instanceof JSON) {
                    returnedMap.put(o, convertToPlainJavaImplementation((JSON) value));
                } else {
                    returnedMap.put(o, value);
                }
            }
            return returnedMap;
        } else {
            //should be JSONNull
            return null;
        }
    }

}
