package com.smartbear.ready.recipe;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;

/**
 * Hack since {@link NotNull] and {@link JsonProperty#required()} didn't help for checking mandatory fields.
 */
public class NullChecker {
    public static void checkNotNull(Object value, String propertyName) {
        if (value == null) {
            throw new NullPointerException(String.format("Required field missing: '%s'", propertyName));
        }
    }

    public static void checkNotEmpty(int[] value, String propertyName) {
        if (value == null) {
            throw new NullPointerException(String.format("Required field missing: '%s'", propertyName));
        }
        if (value.length == 0) {
            throw new NullPointerException(String.format("Required array is empty: '%s'", propertyName));
        }
    }

    public static void checkNotEmpty(Object[] value, String propertyName) {
        if (value == null) {
            throw new NullPointerException(String.format("Required field missing: '%s'", propertyName));
        }
        if (value.length == 0) {
            throw new NullPointerException(String.format("Required array is empty: '%s'", propertyName));
        }
    }
}
