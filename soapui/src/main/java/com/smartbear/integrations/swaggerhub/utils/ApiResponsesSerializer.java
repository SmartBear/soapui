package com.smartbear.integrations.swaggerhub.utils;

import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.std.MapSerializer;

import java.util.Set;

public class ApiResponsesSerializer extends MapSerializer {

    public ApiResponsesSerializer(Set<String> ignoredEntries, JavaType keyType, JavaType valueType, boolean valueTypeIsStatic, TypeSerializer vts, JsonSerializer<?> keySerializer, JsonSerializer<?> valueSerializer) {
        super(ignoredEntries, keyType, valueType, valueTypeIsStatic, vts, keySerializer, valueSerializer);
    }

    public ApiResponsesSerializer(MapSerializer src, BeanProperty property, JsonSerializer<?> keySerializer, JsonSerializer<?> valueSerializer, Set<String> ignoredEntries) {
        super(src, property, keySerializer, valueSerializer, ignoredEntries);
    }

    public ApiResponsesSerializer(MapSerializer src, TypeSerializer vts, Object suppressableValue) {
        super(src, vts, suppressableValue);
    }

    public ApiResponsesSerializer(MapSerializer src, Object filterId, boolean sortKeys) {
        super(src, filterId, sortKeys);
    }

    @Override
    protected void _ensureOverride() {
    }
}
