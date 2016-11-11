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

import java.util.HashMap;
import java.util.Map;

/**
 * HashMap&lt;String,String&gt;
 *
 * @author Ole.Matzura
 */

public class StringToStringMap extends HashMap<String, String> {
    private boolean equalsOnThis;

    public StringToStringMap() {
        super();
    }

    public StringToStringMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    public StringToStringMap(int initialCapacity) {
        super(initialCapacity);
    }

    public StringToStringMap(Map<? extends String, ? extends String> m) {
        super(m);
    }

    public String get(String key, String defaultValue) {
        String value = get(key);
        return value == null ? defaultValue : value;
    }

    /**
     * Get the inverse of this map.
     */
    public StringToStringMap inverse() {
        StringToStringMap inverse = new StringToStringMap();
        for (String key : keySet()) {
            String value = get(key);
            inverse.put(value, key);
        }
        return inverse;
    }

    public String toXml() {
        StringToStringMapConfig xmlConfig = StringToStringMapConfig.Factory.newInstance();

        for (String key : keySet()) {
            StringToStringMapConfig.Entry entry = xmlConfig.addNewEntry();
            entry.setKey(key);
            entry.setValue(get(key));
        }

        return xmlConfig.toString();
    }

    public static StringToStringMap fromXml(String value) {
        if (value == null || value.trim().length() == 0 || value.equals("<xml-fragment/>")) {
            return new StringToStringMap();
        }

        try {
            StringToStringMapConfig nsMapping = StringToStringMapConfig.Factory.parse(value);

            return fromXml(nsMapping);
        } catch (Exception e) {
            SoapUI.logError(e);
        }

        return new StringToStringMap();
    }

    public static StringToStringMap fromXml(StringToStringMapConfig nsMapping) {
        StringToStringMap result = new StringToStringMap();
        for (StringToStringMapConfig.Entry entry : nsMapping.getEntryList()) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    public final boolean getBoolean(String key) {
        return Boolean.parseBoolean(get(key));
    }

    public boolean hasValue(String key) {
        return containsKey(key) && get(key).length() > 0;
    }

    public void putIfMissing(String key, String value) {
        if (!containsKey(key)) {
            put(key, value);
        }
    }

    public void put(String key, boolean value) {
        put(key, Boolean.toString(value));
    }

    public static StringToStringMap fromHttpHeader(String value) {
        StringToStringMap result = new StringToStringMap();

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

    private static void extractNVPair(String value, StringToStringMap result) {
        int ix;
        ix = value.indexOf('=');
        if (ix != -1) {
            String str = value.substring(ix + 1).trim();
            if (str.startsWith("\"") && str.endsWith("\"")) {
                str = str.substring(1, str.length() - 1);
            }

            result.put(value.substring(0, ix).trim(), str);
        }
    }

    public void setEqualsOnThis(boolean equalsOnThis) {
        this.equalsOnThis = equalsOnThis;
    }

    @Override
    public boolean equals(Object o) {
        return equalsOnThis ? this == o : super.equals(o);
    }

    public int getInt(String key, int def) {
        try {
            return Integer.parseInt(get(key));
        } catch (Exception e) {
            return def;
        }
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
}
