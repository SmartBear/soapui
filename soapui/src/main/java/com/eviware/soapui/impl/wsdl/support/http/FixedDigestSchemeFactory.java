package com.eviware.soapui.impl.wsdl.support.http;

import org.apache.http.auth.AuthScheme;
import org.apache.http.impl.auth.DigestSchemeFactory;
import org.apache.http.protocol.HttpContext;

public class FixedDigestSchemeFactory extends DigestSchemeFactory {
    @Override
    public AuthScheme create(final HttpContext context) {
        return new FixedDigestScheme();
    }
}