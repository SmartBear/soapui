package com.eviware.soapui.impl.wsdl.support.http;

import com.eviware.soapui.impl.wsdl.support.VMOptionReader;
import org.apache.http.auth.AuthScheme;
import org.apache.http.impl.auth.SPNegoSchemeFactory;
import org.apache.http.protocol.HttpContext;

public class FixedSPNegoSchemeFactory extends SPNegoSchemeFactory {
    public static final String SPNEGO_USE_CANONICAL_HOSTNAME_VM_OPTION = "httpclient.spnego.usecanonicalname";

    @Override
    public AuthScheme create(final HttpContext context) {
        boolean useCanonicalNames = VMOptionReader.getValueAsBoolean(SPNEGO_USE_CANONICAL_HOSTNAME_VM_OPTION, isUseCanonicalHostname());
        return new FixedSPNegoScheme(isStripPort(), useCanonicalNames);
    }
}