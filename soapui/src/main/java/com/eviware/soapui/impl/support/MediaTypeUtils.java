package com.eviware.soapui.impl.support;

import javax.ws.rs.core.MediaType;

public class MediaTypeUtils {
    public static String getSubtype(String mediaType) {
        return MediaType.valueOf(mediaType).getSubtype();
    }

    public static String getSuffix(String mediaType) {
        String subtype = getSubtype(mediaType);
        int plusIndex = subtype.lastIndexOf("+");

        if (plusIndex == -1) {
            return null;
        }

        return subtype.substring(plusIndex + 1);
    }
}
