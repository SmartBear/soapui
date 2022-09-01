package com.eviware.soapui.impl.wsdl.support.http;

import org.apache.http.auth.Credentials;
import org.apache.http.impl.auth.SPNegoScheme;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.Oid;

public class FixedSPNegoScheme extends SPNegoScheme {

    private static final String SPNEGO_OID = "1.3.6.1.5.5.2";

    public FixedSPNegoScheme(boolean stripPort, boolean useCanonicalHostname) {
        super(stripPort, useCanonicalHostname);
    }

    @Override
    protected byte[] generateToken(final byte[] input, final String authServer, final Credentials credentials) throws GSSException {
        return KerberosProtocolFixer.generateFixedToken(this, new Oid(SPNEGO_OID),
                input, authServer, credentials);
    }

}
