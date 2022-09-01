package com.eviware.soapui.impl.wsdl.support.http;

import org.apache.http.auth.Credentials;
import org.apache.http.impl.auth.KerberosScheme;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.Oid;

public class FixedKerberosScheme extends KerberosScheme {

    private static final String KERBEROS_OID = "1.2.840.113554.1.2.2";

    public FixedKerberosScheme(final boolean stripPort, final boolean useCanonicalHostname) {
        super(stripPort, useCanonicalHostname);
    }

    @Override
    protected byte[] generateToken(final byte[] input, final String authServer, final Credentials credentials) throws GSSException {
        return KerberosProtocolFixer.generateFixedToken(this, new Oid(KERBEROS_OID),
                input, authServer, credentials);
    }
}
