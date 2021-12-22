package com.eviware.soapui.support;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.google.common.collect.Sets;
import org.apache.commons.beanutils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.Set;

public class DateObjectSerializer extends JsonSerializer<Date> {

    private static final Logger log = LoggerFactory.getLogger(DateObjectSerializer.class);
    private Set<String> exclusions = Sets.newHashSet("metaClass", "class", "declaringClass");

    @Override
    public void serialize(Date date, JsonGenerator gen, SerializerProvider provider) throws IOException {

        PropertyDescriptor[] propertyDescriptors = PropertyUtils.getPropertyDescriptors(date);
        gen.writeStartObject();
        Arrays.stream(propertyDescriptors).forEach(propertyDescriptor -> {
            try {
                String name = propertyDescriptor.getName();
                if (!exclusions.contains(name)) {
                    Object value = PropertyUtils.getProperty(date, name);
                    if (value instanceof Integer) {
                        gen.writeNumberField(name, (Integer) value);
                    } else {
                        gen.writeNumberField(name, (Long) value);
                    }
                }
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            }
        });
        gen.writeEndObject();
    }

    @Override
    public Class<Date> handledType() {
        return Date.class;
    }
}