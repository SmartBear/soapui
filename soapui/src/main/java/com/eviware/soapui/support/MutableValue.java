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
