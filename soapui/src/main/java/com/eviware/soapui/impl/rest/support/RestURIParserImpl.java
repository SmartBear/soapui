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

import com.eviware.soapui.impl.rest.RestURIParser;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Basic RestURIParser using java's .net.URI and .net.URL class
 *
 * @author Shadid Chowdhury
 * @since 4.5.6
 */
public class RestURIParserImpl implements RestURIParser {
    private static final String SCHEME_SEPARATOR = "://";
    private static final String DEFAULT_SCHEME = "http";

    private String resourcePath = "";
    private String query = "";
    private String scheme = "";
    private String authority = "";

    public RestURIParserImpl(String uriString) throws MalformedURLException {
        preValidation(uriString);

        parseURI(uriString);

        postValidation();
    }

    private void preValidation(String uriString) throws MalformedURLException {
        if (uriString == null || uriString.isEmpty()) {
            throw new MalformedURLException("Empty URI");
        }
    }

    private void parseURI(String uriString) throws MalformedURLException {
        try {
            if (isAPossibleEndPointWithoutScheme(uriString)) {
                uriString = DEFAULT_SCHEME + SCHEME_SEPARATOR + uriString;
            }

            parseWithURI(uriString);
        } catch (URISyntaxException e) {
            parseWithURL(uriString);

        }
    }

    private void postValidation() throws MalformedURLException {
        if (!validateScheme()) {
            throw new MalformedURLException("URI contains unsupported protocol. Supported protocols are HTTP/HTTPS");
        } else if (!validateAuthority()) {
            throw new MalformedURLException("Invalid endpoint");
        }
    }

    private boolean validateScheme() throws MalformedURLException {
        String scheme = getScheme();
        return scheme.isEmpty() || scheme.matches("(HTTP|http).*");

    }

    private boolean validateAuthority() throws MalformedURLException {
        String endpoint = getEndpoint();
        return endpoint.isEmpty() || !endpoint.matches(".*[\\\\]+.*");

    }

    @Override
    public String getEndpoint() {
        String endpoint;

        if (authority.isEmpty()) {
            endpoint = "";
        } else if (scheme.isEmpty()) {
            endpoint = DEFAULT_SCHEME + SCHEME_SEPARATOR + authority;
        } else {
            endpoint = scheme + SCHEME_SEPARATOR + authority;
        }

        return endpoint;
    }

    @Override
    public String getResourceName() {
        String path = getResourcePath();

        if (path.isEmpty()) {
            return path;
        }

        String[] splitResourcePath = path.split("/");
        if (splitResourcePath.length == 0) {
            return "";
        }
        String resourceName = splitResourcePath[splitResourcePath.length - 1];
        if (resourceName.startsWith(";")) {
            return "";
        }
        resourceName = resourceName.replaceAll("\\{", "").replaceAll("\\}", "");
        if (resourceName.contains(";")) {
            resourceName = resourceName.substring(0, resourceName.indexOf(";"));
        }

        return resourceName.substring(0, 1).toUpperCase() + resourceName.substring(1);
    }

    @Override
    public String getScheme() {
        return scheme;
    }

    @Override
    public String getResourcePath() {
        String path = resourcePath;
        path = addPrefixSlash(path);

        return path;
    }

    @Override
    public String getQuery() {
        return query;
    }

    private String addPrefixSlash(String path) {
        if (!path.startsWith("/") && !path.isEmpty()) {
            path = "/" + path;
        }

        return path;
    }

    private void parseWithURI(String uriString) throws URISyntaxException {

        URI uri = new URI(uriString);
        resourcePath = (uri.getPath() == null ? "" : uri.getPath());
        query = (uri.getQuery() == null ? "" : uri.getQuery());
        scheme = (uri.getScheme() == null ? "" : uri.getScheme());
        authority = (uri.getAuthority() == null ? "" : uri.getAuthority());
    }

    private boolean isURIWithoutScheme(String uriString) {
        return !uriString.matches("[a-zA-Z]+\\:[\\/\\/]+.*");

    }

    private boolean isAPossibleEndPointWithoutScheme(String uriString) {
        int indexOfDot = uriString.indexOf(".");

        return indexOfDot > 0 && isURIWithoutScheme(uriString);

    }

    private void parseWithURL(String uriString) throws MalformedURLException {
        URL url;
        try {
            url = new URL(uriString);
            resourcePath = (url.getPath() == null ? "" : url.getPath());
            query = (url.getQuery() == null ? "" : url.getQuery());
            scheme = (url.getProtocol() == null ? "" : url.getProtocol());
            authority = (url.getAuthority() == null ? "" : url.getAuthority());
        } catch (MalformedURLException e) {
            parseManually(uriString);
        }
    }

    private void parseManually(String uriString) {
        resourcePath = uriString;
        query = "";
        scheme = "";
        authority = "";

        int startIndexOfQuery = uriString.indexOf('?');
        if (startIndexOfQuery >= 0) {
            query = uriString.substring(startIndexOfQuery + 1);
            resourcePath = uriString.substring(0, startIndexOfQuery);
        }

        int startIndexOfResource = resourcePath.indexOf('/');
        if (startIndexOfResource >= 0) {
            resourcePath = resourcePath.substring(startIndexOfResource + 1);
            authority = resourcePath.substring(0, startIndexOfResource);
        }
    }

}
