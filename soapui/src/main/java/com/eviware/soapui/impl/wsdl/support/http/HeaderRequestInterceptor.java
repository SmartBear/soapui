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

package com.eviware.soapui.impl.wsdl.support.http;

import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * This request interceptor checks if wrapper request have more http headers. If
 * that is true then it copies those headers in original request. This way they
 * will be visible in raw request and accessible for users in Groovy scripts.
 *
 * @author robert.nemet
 */
public class HeaderRequestInterceptor implements HttpRequestInterceptor {
    public static final String SOAPUI_REQUEST_HEADERS = "soapui.request.headers";

    @Override
    public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
        List<Header> wHeaders = Arrays.asList(request.getAllHeaders());
        context.setAttribute(SOAPUI_REQUEST_HEADERS, wHeaders);
    }
}
