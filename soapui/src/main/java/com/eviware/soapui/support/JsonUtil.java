/*
 * Copyright 2004-2014 SmartBear Software
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
import net.sf.json.JSONSerializer;

/**
 * @author joel.jonsson
 */
public class JsonUtil {

    private static final String WHILE_1 = "while(1);";

    public JSON parseTrimmedText(String text) {
        if(text == null){
            return null;
        }
        String trimmedText = text.trim();
        if(trimmedText.startsWith(WHILE_1)) {
            trimmedText = trimmedText.substring(WHILE_1.length()).trim();
        }
        return JSONSerializer.toJSON(trimmedText);
    }
}
