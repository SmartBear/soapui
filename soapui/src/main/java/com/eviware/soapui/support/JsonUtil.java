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

import net.sf.json.JSON;
import net.sf.json.JSONNull;
import net.sf.json.JSONSerializer;
import net.sf.json.groovy.JsonSlurper;

/**
 * @author joel.jonsson
 */
public class JsonUtil {

    private static final String WHILE_1 = "while(1);";

    public static boolean isValidJson(String value) {
        try {
            JSON json = new JsonSlurper().parseText(value);
            return json != null && !(json instanceof JSONNull);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * This method and its name are somewhat awkward, but both stem from the fact that there are so many commonly used
     * content types for JSON.
     *
     * @param contentType the MIME type to examine
     * @return <code>true</code> if content type is non-null and contains either "json" or "javascript"
     */
    public static boolean seemsToBeJsonContentType(String contentType) {
        return contentType != null && (contentType.contains("javascript") || contentType.contains("json"));
    }

    public static boolean seemsToBeJson(String content) {
        if (!StringUtils.hasContent(content)) {
            return false;
        }
        try {
            new JsonSlurper().parseText(content);
            return true;
        } catch (Exception e) {
            return false;
        }
    }


    public JSON parseTrimmedText(String text) {
        if (text == null) {
            return null;
        }
        String trimmedText = text.trim();
        if (trimmedText.startsWith(WHILE_1)) {
            trimmedText = trimmedText.substring(WHILE_1.length()).trim();
        }
        return JSONSerializer.toJSON(trimmedText);
    }
}
