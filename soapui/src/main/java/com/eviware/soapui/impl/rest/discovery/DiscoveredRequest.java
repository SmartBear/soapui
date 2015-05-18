/*
 *  SoapUI, copyright (C) 2004-2014 smartbear.com 
 *
 *  SoapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  SoapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */
package com.eviware.soapui.impl.rest.discovery;

import com.eviware.soapui.impl.rest.RestRequestInterface;
import com.eviware.soapui.impl.wsdl.monitor.WsdlMonitorMessageExchange;
import com.eviware.soapui.support.types.StringToStringsMap;
import com.eviware.soapui.tools.PropertyExpansionRemover;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import javax.mail.internet.ContentType;
import javax.mail.internet.ParseException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class DiscoveredRequest {
    private final String url;
    private final RestRequestInterface.HttpMethod requestMethod;
    private String protocolVersion;
    private final String body;
    private final StringToStringsMap headers;
    private DiscoveredResponse response;
    private WsdlMonitorMessageExchange exchange;

    private DiscoveredRequest(URL url, RestRequestInterface.HttpMethod requestMethod, String protocolVersion, String body, StringToStringsMap headers, WsdlMonitorMessageExchange exchange) {
        if (url == null || requestMethod == null) {
            throw new NullPointerException();
        }
        this.url = removeExpansionsFrom(url);
        this.requestMethod = requestMethod;
        this.protocolVersion = protocolVersion;
        this.body = PropertyExpansionRemover.removeExpansions(body);
        this.headers = headers == null ? new StringToStringsMap() : removeExpansionsFrom(headers);
        this.exchange = exchange;
    }

    public void setResponse(DiscoveredResponse response) {
        this.response = response;
    }

    public DiscoveredResponse getResponse() {
        return response;
    }

    public WsdlMonitorMessageExchange getExchange() {
        return exchange;
    }

    public static class Builder {
        private URL url;
        private RestRequestInterface.HttpMethod requestMethod;
        private String protocolVersion;
        private String body;
        private byte[] bodyByteArray;
        private StringToStringsMap headers = new StringToStringsMap();
        private WsdlMonitorMessageExchange exchange;

        public Builder setUrl(URL url) {
            this.url = url;
            return this;
        }

        public Builder setRequestMethod(RestRequestInterface.HttpMethod requestMethod) {
            this.requestMethod = requestMethod;
            return this;
        }

        public Builder setBody(String body) {
            this.body = body;
            this.bodyByteArray = null;
            return this;
        }

        public Builder setBody(byte[] body) {
            this.bodyByteArray = body;
            this.body = null;
            return this;
        }

        public Builder setHeaders(StringToStringsMap headers) {
            this.headers = headers;
            return this;
        }

        public Builder setHeaders(Map<String, List<String>> headers) {
            if (headers != null) {
                this.headers = new StringToStringsMap(headers);
            }
            return this;
        }

        public Builder setExchange(WsdlMonitorMessageExchange exchange) {
            this.exchange = exchange;
            return this;
        }

        public Builder setProtocolVersion(String protocolVersion) {
            this.protocolVersion = protocolVersion;
            return this;
        }

        public DiscoveredRequest createDiscoveredRequest() {
            if (bodyByteArray != null) {
                body = convertBody(bodyByteArray, headers);
            }
            return new DiscoveredRequest(url, requestMethod, protocolVersion, body, headers, exchange);
        }

        private String convertBody(byte[] bodyByteArray, StringToStringsMap headersMap) {
            if (bodyByteArray == null) {
                return null;
            }
            byte[] decodedBody = HttpContentDecoder.decode(bodyByteArray, headers);
            String charset = getCharset(headersMap);
            try {
                return charset == null ? new String(decodedBody, "UTF-8") : new String(decodedBody, charset);
            } catch (UnsupportedEncodingException e) {
                return new String(bodyByteArray);
            }
        }

        private String getCharset(StringToStringsMap headersMap) {
            if (headersMap == null) {
                return null;
            }
            String contentTypeValue = headersMap.getCaseInsensitive("Content-Type", null);
            if (contentTypeValue == null) {
                return null;
            }
            try {
                return new ContentType(contentTypeValue).getParameterList().get("charset");
            } catch (ParseException e) {
                return null;
            }
        }
    }

    public String getUrl() {
        return url;
    }

    public String getProtocolVersion() {
        return protocolVersion;
    }

    public RestRequestInterface.HttpMethod getRequestMethod() {
        return requestMethod;
    }

    @Override
    public String toString() {
        return "DiscoveredRequest{" +
                "url=" + url +
                ", requestMethod=" + requestMethod +
                ", body='" + body + '\'' +
                ", headers=" + headers +
                '}';
    }

    public String getBody() {
        return body;
    }

    public StringToStringsMap getHeaders() {
        return headers;
    }

    public String getHeader(String headerName) {
        if (headers == null) {
            return null;
        }
        List<String> values = headers.get(headerName);
        return values == null || values.isEmpty() ? null : values.get(0);
    }

    public List<ContentType> getResponseContentTypes() {
        if (response != null) {
            return response.getContentTypes();
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public boolean equals(Object o) {
        return EqualsBuilder.reflectionEquals(this, o, Arrays.asList("response"));
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this, Arrays.asList("response"));
    }

	/*
    Private helper methods.
	 */

    private String removeExpansionsFrom(URL url) {
        return PropertyExpansionRemover.removeExpansions(url.toString());
    }

    private StringToStringsMap removeExpansionsFrom(StringToStringsMap requestHeaders) {
        StringToStringsMap cleanedHeaders = new StringToStringsMap();
        for (Map.Entry<String, List<String>> header : requestHeaders.entrySet()) {
            for (String value : header.getValue()) {
                cleanedHeaders.put(header.getKey(), PropertyExpansionRemover.removeExpansions(value));
            }
        }
        return cleanedHeaders;
    }
}
