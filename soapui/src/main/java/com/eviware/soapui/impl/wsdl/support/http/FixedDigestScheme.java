package com.eviware.soapui.impl.wsdl.support.http;

import com.eviware.soapui.support.MessageSupport;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.Credentials;
import org.apache.http.client.methods.HttpRequestWrapper;
import org.apache.http.impl.auth.DigestScheme;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

public class FixedDigestScheme extends DigestScheme {

    private static final Logger logger = LoggerFactory.getLogger(FixedDigestScheme.class);
    private static final MessageSupport messages = MessageSupport.getMessages(FixedDigestScheme.class);

    @Override
    public Header authenticate(
            final Credentials credentials,
            final HttpRequest request,
            final HttpContext context) throws AuthenticationException {
        URI originalUri = ((HttpRequestWrapper) request).getURI();
        HttpHost httphost = ((HttpRequestWrapper) request).getTarget();
        try {
            String digestUri = originalUri.toString();
            ((HttpRequestWrapper) request).setURI(URI.create(digestUri.replaceAll(httphost.toString(), "")));
            return super.authenticate(credentials, request, context);

        } catch (Exception ex) {
            logger.warn(messages.get("FixedDigestScheme.WarnMessage"));
            return super.authenticate(credentials, request, context);

        } finally {
            ((HttpRequestWrapper) request).setURI(originalUri);

        }
    }
}