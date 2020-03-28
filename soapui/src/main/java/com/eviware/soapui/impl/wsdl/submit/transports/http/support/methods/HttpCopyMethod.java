package com.eviware.soapui.impl.wsdl.submit.transports.http.support.methods;

import com.eviware.soapui.impl.rest.RestRequestInterface;
import org.apache.http.client.methods.HttpRequestBase;

import java.net.URI;

/**
 * HTTP COPY Method https://tools.ietf.org/html/rfc4918
 */
public class HttpCopyMethod extends HttpRequestBase {

    public HttpCopyMethod() {
        super();
    }

    public HttpCopyMethod(final URI uri) {
        super();
        setURI(uri);
    }

    /**
     * @throws IllegalArgumentException if the uri is invalid.
     */
    public HttpCopyMethod(final String uri) {
        this(URI.create(uri));
    }

    @Override
    public String getMethod() {
        return RestRequestInterface.HttpMethod.COPY.toString();
    }
}
