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

package com.eviware.soapui.support.types;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.StringToStringMapConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * HashMap&lt;String,String&gt;
 *
 * @author Ole.Matzura
 */

public class StringToStringsMap extends HashMap<String, List<String>> {
    private boolean equalsOnThis;

    public StringToStringsMap() {
        super();
    }

    public StringToStringsMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    public StringToStringsMap(int initialCapacity) {
        super(initialCapacity);
    }

    public StringToStringsMap(Map<? extends String, ? extends List<String>> m) {
        super(m);
    }

    public StringToStringsMap(StringToStringMap map) {
        super();

        for (String key : map.keySet()) {
            put(key, map.get(key));
        }
    }

    public List<String> get(String key, List<String> defaultValue) {
        List<String> value = get(key);
        return value == null ? defaultValue : value;
    }

    /**
     * Get the inverse of this map.
     */

    public String toXml() {
        StringToStringMapConfig xmlConfig = StringToStringMapConfig.Factory.newInstance();

        for (String key : keySet()) {
            for (String value : get(key)) {
                StringToStringMapConfig.Entry entry = xmlConfig.addNewEntry();
                entry.setKey(key);
                entry.setValue(value);
            }
        }

        return xmlConfig.toString();
    }

    public static StringToStringsMap fromXml(String value) {
        if (value == null || value.trim().length() == 0 || value.equals("<xml-fragment/>")) {
            return new StringToStringsMap();
        }

        try {
            StringToStringMapConfig nsMapping = StringToStringMapConfig.Factory.parse(value);

            return fromXml(nsMapping);
        } catch (Exception e) {
            SoapUI.logError(e);
        }

        return new StringToStringsMap();
    }

    public static StringToStringsMap fromXml(StringToStringMapConfig nsMapping) {
        StringToStringsMap result = new StringToStringsMap();
        for (StringToStringMapConfig.Entry entry : nsMapping.getEntryList()) {
            result.add(entry.getKey(), entry.getValue());
        }
        return result;
    }

    public boolean hasValues(String key) {
        return containsKey(key) && get(key).size() > 0;
    }

    public void add(String key, boolean value) {
        add(key, Boolean.toString(value));
    }

    public void add(String key, String string) {
        if (!containsKey(key)) {
            put(key, new ArrayList<String>());
        }

        get(key).add(string);
    }

    public static StringToStringsMap fromHttpHeader(String value) {
        StringToStringsMap result = new StringToStringsMap();

        int ix = value.indexOf(';');
        while (ix > 0) {
            extractNVPair(value.substring(0, ix), result);
            value = value.substring(ix + 1);
            ix = value.indexOf(';');
        }

        if (value.length() > 2) {
            extractNVPair(value, result);
        }

        return result;
    }

    private static void extractNVPair(String value, StringToStringsMap result) {
        int ix;
        ix = value.indexOf('=');
        if (ix != -1) {
            String str = value.substring(ix + 1).trim();
            if (str.startsWith("\"") && str.endsWith("\"")) {
                str = str.substring(1, str.length() - 1);
            }

            result.add(value.substring(0, ix).trim(), str);
        }
    }

    public void setEqualsOnThis(boolean equalsOnThis) {
        this.equalsOnThis = equalsOnThis;
    }

    @Override
    public boolean equals(Object o) {
        return equalsOnThis ? this == o : super.equals(o);
    }

    public String[] getKeys() {
        return keySet().toArray(new String[size()]);
    }

    public boolean containsKeyIgnoreCase(String string) {
        for (String key : keySet()) {
            if (key.equalsIgnoreCase(string)) {
                return true;
            }
        }

        return false;
    }

    public void put(String name, String value) {
        add(name, value);
    }

    public String get(String key, String defaultValue) {
        List<String> value = get(key);
        if (value == null || value.size() == 0) {
            return defaultValue;
        }

        return value.get(0);

    }

    public String getCaseInsensitive(String key, String defaultValue) {
        for (Map.Entry<String, List<String>> stringListEntry : entrySet()) {
            if (key.equalsIgnoreCase(stringListEntry.getKey()) && !stringListEntry.getValue().isEmpty()) {
                return stringListEntry.getValue().get(0);
            }
        }
        return defaultValue;

    }

    public StringToStringMap toStringToStringMap() {
        StringToStringMap result = new StringToStringMap();

        for (String key : keySet()) {
            List<String> list = get(key);
            if (list.size() == 1) {
                result.put(key, list.get(0));
            } else {
                result.put(key, list.toString());
            }
        }

        return result;
    }

    public void replace(String key, String oldValue, String value) {
        List<String> values = get(key);
        if (values == null) {
            return;
        }

        int ix = values.indexOf(oldValue);
        if (ix >= 0) {
            values.set(ix, value);
        }
    }

    public void remove(String key, String data) {
        List<String> values = get(key);
        if (values == null) {
            return;
        }

        values.remove(data);
    }

    public int valueCount() {
        int result = 0;

        for (String key : keySet()) {
            result += get(key).size();
        }

        return result;
    }

    public String toString() {
        StringBuilder result = new StringBuilder();

        for (String key : keySet()) {
            for (String value : get(key)) {
                result.append(key).append(" : ").append(value).append("\r\n");
            }
        }

        return result.toString();
    }
}
