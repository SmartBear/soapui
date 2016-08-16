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

package com.eviware.soapui.impl.support;

import com.eviware.soapui.support.StringUtils;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.http.client.utils.URIUtils;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.net.URLEncoder;

public class HttpUtils {
    private static String pingErrorMessage;

    public static boolean isErrorStatus(int statusCode) {
        return statusCode >= 400;
    }

    public static String extractHttpHeaderParameter(String headerString, String parameterName) {
        if (!StringUtils.hasContent(headerString) || !StringUtils.hasContent(parameterName)) {
            return null;
        }

        int ix = headerString.indexOf(parameterName + "=\"");
        if (ix > 0) {
            int ix2 = headerString.indexOf('"', ix + parameterName.length() + 2);
            if (ix2 > ix) {
                return headerString.substring(ix + parameterName.length() + 2, ix2);
            }
        }

        return null;
    }

    public static String completeUrlWithHttpIfProtocolIsNotHttpOrHttpsOrPropertyExpansion(String endpoint) {
        if (StringUtils.isNullOrEmpty(endpoint)) {
            return endpoint;
        }
        endpoint = endpoint.trim();
        if (!endpoint.startsWith("http://") && !endpoint.startsWith("https://") && !endpoint.startsWith("$")) {
            return "http://" + endpoint;
        }

        return endpoint;
    }

    public static String completeUrlWithHttpIfProtocolIsMissing(String endpoint) {
        if (StringUtils.isNullOrEmpty(endpoint)) {
            return endpoint;
        }
        endpoint = endpoint.trim();
        if (!endpoint.contains("://")) {
            return "http://" + endpoint;
        }
        return endpoint;
    }

    public static boolean ping(String host, int timeout) {
        pingErrorMessage = "No Error";
        try {
            InetAddress address = InetAddress.getByName(host);
            return address.isReachable(timeout);
        } catch (Exception e) {
            pingErrorMessage = e.getMessage();
            return false;
        }
    }

    public static String urlEncodeWithUtf8(String input) {
        try {
            return URLEncoder.encode(input, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // if UTF-8 isn't available we might as well die ...
            throw new Error("Unexpected error: charset UTF-8 not available", e);
        }
    }

    public static String getPingErrorMessage() {
        return pingErrorMessage;
    }

    public static java.net.URI createUri(URI uri) throws URISyntaxException, URIException {
        return createUri(uri.getScheme(), uri.getUserinfo(), uri.getHost(), uri.getPort(), uri.getEscapedPath(),
                uri.getEscapedQuery(), uri.getEscapedFragment());
    }

    public static java.net.URI createUri(String scheme, String userinfo, String host, int port, String escapedPath,
                                         String escapedQuery, String escapedFragment) throws URISyntaxException {
        return URIUtils.createURI(scheme, (userinfo == null ? "" : (userinfo + "@")) + host, port, escapedPath,
                escapedQuery, escapedFragment);
    }
}
