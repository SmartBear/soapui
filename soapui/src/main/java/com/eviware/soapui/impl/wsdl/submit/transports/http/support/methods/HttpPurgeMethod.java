package com.eviware.soapui.impl.wsdl.submit.transports.http.support.methods;

import com.eviware.soapui.impl.rest.RestRequestInterface;
import org.apache.http.client.methods.HttpRequestBase;

import java.net.URI;

/**
 * HTTP PURGE Method https://varnish-cache.org/docs/3.0/tutorial/purging.html
 */
public class HttpPurgeMethod extends HttpRequestBase {

    public HttpPurgeMethod() {
        super();
    }

    public HttpPurgeMethod(final URI uri) {
        super();
        setURI(uri);
    }

    /**
     * @throws IllegalArgumentException if the uri is invalid.
     */
    public HttpPurgeMethod(final String uri) {
        this(URI.create(uri));
    }

    @Override
    public String getMethod() {
        return RestRequestInterface.HttpMethod.PURGE.toString();
    }
}
