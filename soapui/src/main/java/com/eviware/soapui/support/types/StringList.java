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

import com.eviware.soapui.config.StringListConfig;
import com.eviware.soapui.support.StringUtils;
import org.apache.xmlbeans.XmlException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class StringList extends ArrayList<String> {
    public StringList() {
        super();
    }

    public StringList(int initialCapacity) {
        super(initialCapacity);
    }

    public StringList(String[] strings) {
        super(strings == null ? new StringList() : Arrays.asList(strings));
    }

    public StringList(Object[] objects) {
        super();

        if (objects != null) {
            for (Object object : objects) {
                add(object == null ? null : object.toString());
            }
        }
    }

    public StringList(Collection<?> objects) {
        super();

        if (objects != null) {
            for (Object object : objects) {
                add(object == null ? null : object.toString());
            }
        }
    }

    public StringList(String paramStr) {
        this();
        add(paramStr);
    }

    public void addAll(String[] strings) {
        if (strings != null && strings.length > 0) {
            addAll(Arrays.asList(strings));
        }
    }

    public String[] toStringArray() {
        return toArray(new String[size()]);
    }

    public static StringList fromXml(String value) throws XmlException {
        return StringUtils.isNullOrEmpty(value) || value.equals("<xml-fragment/>") ? new StringList()
                : new StringList(StringListConfig.Factory.parse(value).getEntryList());
    }

    public String toXml() {
        StringListConfig config = StringListConfig.Factory.newInstance();
        config.setEntryArray(toStringArray());
        return config.xmlText();
    }

    public boolean containsValue(String value) {
        for (String stringElement : this) {
            if (stringElement.contains(value)) {
                return true;
            }
        }
        return false;
    }
}
