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

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

class MutableValue {

    static final ToMutableValue TO_MUTABLE_VALUE = new ToMutableValue();
    static final FromMutableValue FROM_MUTABLE_VALUE = new FromMutableValue();

    private Object value;

    MutableValue(Object value) {
        this.value = value;
    }

    static Object extractValueFromMutable(Object obj) {
        if (obj instanceof MutableValue) {
            return ((MutableValue) obj).getValue();
        } else {
            return obj;
        }
    }

    Object getValue() {
        return value;
    }

    void setValue(Object value) {
        this.value = value;
    }

    static class ToMutableValue implements Function<Object, MutableValue> {

        @Nullable
        @Override
        public MutableValue apply(@Nullable Object o) {
            if (o instanceof List) {
                return new MutableValue(new ArrayList<Object>(Lists.transform((List) o, this)));
            } else if (o instanceof Map) {
                return new MutableValue(new LinkedHashMap<Object, Object>(Maps.transformValues((Map) o, this)));
            } else {
                return new MutableValue(o);
            }
        }
    }

    static class FromMutableValue implements Function<Object, Object> {

        @Nullable
        @Override
        public Object apply(@Nullable Object o) {
            Object value = extractValueFromMutable(o);
            if (value instanceof List) {
                return new ArrayList<Object>(Lists.transform((List) value, this));
            } else if (value instanceof Map) {
                return new LinkedHashMap<Object, Object>(Maps.transformValues((Map) value, this));
            } else {
                return value;
            }
        }
    }
}
