package com.eviware.soapui.impl.wsdl.submit.transports.http.support.methods;

import com.eviware.soapui.impl.rest.RestRequestInterface;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;

import java.net.URI;

/**
 * HTTP PROPFIND Method https://tools.ietf.org/html/rfc4918
 */
public class HttpPropFindMethod extends HttpEntityEnclosingRequestBase {

    public HttpPropFindMethod() {
        super();
    }

    public HttpPropFindMethod(final URI uri) {
        super();
        setURI(uri);
    }

    /**
     * @throws IllegalArgumentException if the uri is invalid.
     */
    public HttpPropFindMethod(final String uri) {
        this(URI.create(uri));
    }

    @Override
    public String getMethod() {
        return RestRequestInterface.HttpMethod.PROPFIND.toString();
    }
}