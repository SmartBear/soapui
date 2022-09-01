package com.eviware.soapui.impl.wsdl.support.http;

import org.apache.http.auth.Credentials;
import org.apache.http.auth.KerberosCredentials;
import org.apache.http.impl.auth.GGSSchemeBase;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSManager;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.Oid;

import java.lang.reflect.Field;

public class KerberosProtocolFixer {

    public static byte[] generateFixedToken(GGSSchemeBase overridenClass, Oid oid,
                                            final byte[] input, final String authServer, final Credentials credentials) throws GSSException {
        final GSSManager manager = GSSManager.getInstance();
        String service = "HTTP";
        try {
            Field serviceField = GGSSchemeBase.class.getDeclaredField("service");
            serviceField.setAccessible(true);
            service = (String) serviceField.get(overridenClass);
            // service could be initialized with some method. I have no idea about possible methods since now they're only HTTP and HTTPS
            // but if some other method will be placed in service field credentials will be asked for them and not for http
            // This is a reason to keep this
        } catch (NoSuchFieldException e) {
            // Assume service as HTTP if we do not have service field in GSS
        } catch (IllegalAccessException e) {
            // Assume service as HTTP if we do not have access to service field
        }
        if (service.equals("HTTPS")) {
            service = "HTTP"; // HTTPS could not be used for token obtaining. It have to be replaced by http.
            // It'll be much better to fix this in componet itself but it's opesource and i have not idea how to propose the change there
        }
        final GSSName serverName = manager.createName(service + "@" + authServer, GSSName.NT_HOSTBASED_SERVICE);

        final GSSCredential gssCredential;
        if (credentials instanceof KerberosCredentials) {
            gssCredential = ((KerberosCredentials) credentials).getGSSCredential();
        } else {
            gssCredential = null;
        }

        final GSSContext gssContext = manager.createContext(
                serverName.canonicalize(oid), oid, gssCredential, GSSContext.DEFAULT_LIFETIME);
        gssContext.requestMutualAuth(true);
        gssContext.requestCredDeleg(true);
        byte[] inputBuff = input;
        if (inputBuff == null) {
            inputBuff = new byte[0];
        }
        return gssContext.initSecContext(inputBuff, 0, inputBuff.length);
    }

}
