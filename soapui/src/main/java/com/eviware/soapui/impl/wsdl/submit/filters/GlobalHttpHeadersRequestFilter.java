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

package com.eviware.soapui.impl.wsdl.submit.filters;

import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.impl.support.AbstractHttpRequestInterface;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.propertyexpansion.PropertyExpander;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.types.StringToStringsMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Picks up custom HTTP headers to add to all requests from system properties, which should be named as
 * <p/>
 * soapui.http.custom.header.&lt;headerName&gt;=&lt;headerValue&gt;
 * <p/>
 * The list and values of headers will be cached unless the system property soapui.http.custom.headers.cache=false
 * <p/>
 * By default header values are added to existing header values - if you want to replace existing headers
 * add soapui.http.custom.headers.replace=true instead
 * <p/>
 * The values are PropertyExpanded each time before getting added to the request.
 * <p/>
 * Since header names are passed via system properties, you can only define each header once. Use the static
 * addGlobalHeader method from code if needed.
 */

public class GlobalHttpHeadersRequestFilter extends AbstractRequestFilter {
    public static final String REPLACE_HEADERS_SYSTEM_PROPERTY = "soapui.http.global.headers.cache";
    public static final String CACHE_HEADERS_SYSTEM_PROPERTY = "soapui.http.global.headers.replace";
    public static final String HEADER_SYSTEM_PROPERTY_PREFIX = "soapui.http.global.header.";

    private static StringToStringsMap globalHeadersToAdd = new StringToStringsMap();

    private StringToStringsMap headersToAdd;
    private Map<AbstractHttpRequest, StringToStringsMap> savedHeaders = new HashMap<AbstractHttpRequest, StringToStringsMap>();

    @Override
    public void filterAbstractHttpRequest(SubmitContext context, AbstractHttpRequest<?> request) {

        if (headersToAdd == null || !isCacheHeaders() ) {
            synchronized (this) {
                if (headersToAdd == null || !isCacheHeaders())
                    initHeadersToAdd();
            }
        }

        if (!headersToAdd.isEmpty()) {
            addHeadersToRequest(context, request);
        }
    }

    private boolean isCacheHeaders() {
        return Boolean.valueOf(System.getProperty(CACHE_HEADERS_SYSTEM_PROPERTY, "true"));
    }

    private void addHeadersToRequest(SubmitContext context, AbstractHttpRequest<?> request) {
        // get existing and save
        StringToStringsMap headers = request.getRequestHeaders();
        savedHeaders.put(request, createCopyOfHeaders(headers));

        boolean replaceHeaders = isReplaceHeaders();

        for (String name : headersToAdd.keySet()) {
            // remove existing if replace is enabled
            if (replaceHeaders && headers.containsKey(name))
                headers.get(name).clear();

            // add header values
            for (String value : headersToAdd.get(name)) {
                headers.add(name, PropertyExpander.expandProperties(context, value));
            }
        }

        request.setRequestHeaders(headers);
    }

    private boolean isReplaceHeaders() {
        return Boolean.valueOf(System.getProperty(REPLACE_HEADERS_SYSTEM_PROPERTY, "false"));
    }

    private StringToStringsMap createCopyOfHeaders(StringToStringsMap headers) {
        StringToStringsMap result = new StringToStringsMap();

        for (String name : headers.keySet()) {
            result.put(name, new ArrayList<String>(headers.get(name)));
        }

        return result;
    }

    private void initHeadersToAdd() {
        headersToAdd = new StringToStringsMap(globalHeadersToAdd);

        for (Object key : System.getProperties().keySet()) {
            if (String.valueOf(key).startsWith(HEADER_SYSTEM_PROPERTY_PREFIX)) {
                String headerName = String.valueOf(key).substring(HEADER_SYSTEM_PROPERTY_PREFIX.length());
                if (headerName.length() > 0) {
                    String headerValue = System.getProperty(String.valueOf(key));
                    if (StringUtils.hasContent(headerValue))
                        headersToAdd.add(headerName, headerValue);
                }
            }
        }
    }

    /**
     * Restore headers to pre-request state
     */

    @Override
    public void afterAbstractHttpResponse(SubmitContext context, AbstractHttpRequestInterface<?> request) {
        if (savedHeaders.containsKey(request)) {
            request.setRequestHeaders(savedHeaders.get(request));
            savedHeaders.remove(request);
        }
    }

    public static void addGlobalHeader(String name, String value) {
        globalHeadersToAdd.add(name, value);
    }

    public static void removeGlobalHeader(String name, String value) {

        if( value == null )
            globalHeadersToAdd.remove( name );
        else if( globalHeadersToAdd.containsKey( name ))
            globalHeadersToAdd.get( name ).remove( value );
    }
}
