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

package com.eviware.soapui.impl.rest.support;

import com.eviware.soapui.impl.rest.support.handlers.DefaultMediaTypeHandler;
import com.eviware.soapui.impl.rest.support.handlers.HtmlMediaTypeHandler;
import com.eviware.soapui.impl.rest.support.handlers.JsonMediaTypeHandler;

import java.util.ArrayList;
import java.util.List;

public class MediaTypeHandlerRegistry {
    private static List<MediaTypeHandler> mediaTypeHandlers = new ArrayList<MediaTypeHandler>();
    private static MediaTypeHandler defaultMediaTypeHandler = new DefaultMediaTypeHandler();

    static {
        mediaTypeHandlers.add(new JsonMediaTypeHandler());
        mediaTypeHandlers.add(new HtmlMediaTypeHandler());
    }

    public static MediaTypeHandler getTypeHandler(String contentType) {
        for (MediaTypeHandler handler : mediaTypeHandlers) {
            if (handler.canHandle(contentType)) {
                return handler;
            }
        }

        return defaultMediaTypeHandler;
    }

    public static MediaTypeHandler getDefaultMediaTypeHandler() {
        return defaultMediaTypeHandler;
    }

    public static void setDefaultMediaTypeHandler(MediaTypeHandler defaultMediaTypeHandler) {
        MediaTypeHandlerRegistry.defaultMediaTypeHandler = defaultMediaTypeHandler;
    }
}
