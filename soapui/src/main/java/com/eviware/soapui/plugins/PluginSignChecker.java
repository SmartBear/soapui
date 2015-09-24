package com.eviware.soapui.plugins;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.Provider;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public final class PluginSignChecker extends Provider {
    private X509Certificate providerCert = null;

    public PluginSignChecker() {
        super("SoapUIOSPluginSignChecker", 1.0, "New plugin framework restriction");
    }

    public final synchronized boolean isSigned(File plugin) {
        JarVerifier jv = new JarVerifier(plugin);

        try {
            if (providerCert == null) {
                providerCert = setupProviderCert();
            }
            jv.verify(providerCert);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private static X509Certificate setupProviderCert()
            throws IOException, CertificateException {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        return (X509Certificate) cf.generateCertificate(PluginSignChecker.class.getResourceAsStream("/com/eviware/soapui/plugins/PublicKey.cer"));
    }

    private class JarVerifier {
        private JarFile jarFile = null;

        JarVerifier(File jarURL) {
            try {
                jarFile = new JarFile(jarURL);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void verify(X509Certificate targetCert)
                throws IOException {
            if (targetCert == null) {
                throw new SecurityException("The certificate is not specified.");
            }

            try {
                if (jarFile == null) {
                    throw new SecurityException("The plugin is not accessible.");
                }
            } catch (Exception ex) {
                SecurityException se = new SecurityException();
                se.initCause(ex);
                throw se;
            }

            Vector<JarEntry> entriesVec = new Vector<JarEntry>();

            Manifest man = jarFile.getManifest();
            if (man == null) {
                throw new SecurityException("The provider is not signed");
            }

            byte[] buffer = new byte[8192];
            Enumeration entries = jarFile.entries();

            while (entries.hasMoreElements()) {
                JarEntry je = (JarEntry) entries.nextElement();

                if (je.isDirectory()) {
                    continue;
                }
                entriesVec.addElement(je);
                InputStream is = jarFile.getInputStream(je);

                // Read in each jar entry. A security exception will
                // be thrown if a signature/digest check fails.
                int n;
                while ((n = is.read(buffer, 0, buffer.length)) != -1) {
                    // Don't care
                }
                is.close();
            }

            // Get the list of signer certificates
            Enumeration e = entriesVec.elements();

            while (e.hasMoreElements()) {
                JarEntry je = (JarEntry) e.nextElement();

                // Every file must be signed except files in META-INF.
                Certificate[] certs = je.getCertificates();
                if ((certs == null) || (certs.length == 0)) {
                    if (!je.getName().startsWith("META-INF")) {
                        throw new SecurityException("The provider " +
                                "has unsigned " +
                                "class files.");
                    }
                } else {
                    // Check whether the file is signed by the expected
                    // signer. The jar may be signed by multiple signers.
                    // See if one of the signers is 'targetCert'.
                    int startIndex = 0;
                    X509Certificate[] certChain;
                    boolean signedAsExpected = false;

                    while ((certChain = getAChain(certs, startIndex)) != null) {
                        if (certChain[0].equals(targetCert)) {
                            // Stop since one trusted signer is found.
                            signedAsExpected = true;
                            break;
                        }
                        // Proceed to the next chain.
                        startIndex += certChain.length;
                    }

                    if (!signedAsExpected) {
                        throw new SecurityException("The provider " +
                                "is not signed by a " +
                                "trusted signer");
                    }
                }
            }
        }

        private X509Certificate[] getAChain(Certificate[] certs,
                                                   int startIndex) {
            if (startIndex > certs.length - 1) {
                return null;
            }

            int i;
            for (i = startIndex; i < certs.length - 1; i++) {
                if (!((X509Certificate) certs[i + 1]).getSubjectDN().
                        equals(((X509Certificate) certs[i]).getIssuerDN())) {
                    break;
                }
            }
            int certChainSize = (i - startIndex) + 1;
            X509Certificate[] ret = new X509Certificate[certChainSize];
            for (int j = 0; j < certChainSize; j++) {
                ret[j] = (X509Certificate) certs[startIndex + j];
            }
            return ret;
        }

        protected void finalize() throws Throwable {
            jarFile.close();
        }
    }
}
