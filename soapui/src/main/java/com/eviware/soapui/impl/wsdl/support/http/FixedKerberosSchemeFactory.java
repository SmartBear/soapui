package com.eviware.soapui.impl.wsdl.support.http;

import com.eviware.soapui.impl.wsdl.support.VMOptionReader;
import org.apache.http.auth.AuthScheme;
import org.apache.http.impl.auth.KerberosSchemeFactory;
import org.apache.http.protocol.HttpContext;

public class FixedKerberosSchemeFactory extends KerberosSchemeFactory {

    public static final String KERBEROS_USE_CANONICAL_HOSTNAME_VM_OPTION = "httpclient.kerberos.usecanonicalname";

    @Override
    public AuthScheme create(final HttpContext context) {
        boolean useCanonicalNames = VMOptionReader.getValueAsBoolean(KERBEROS_USE_CANONICAL_HOSTNAME_VM_OPTION, isUseCanonicalHostname());
        return new FixedKerberosScheme(isStripPort(), useCanonicalNames);
    }
}