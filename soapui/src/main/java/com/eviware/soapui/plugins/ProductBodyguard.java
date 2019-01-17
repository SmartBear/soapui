/*
 * SoapUI, Copyright (C) 2004-2018 SmartBear Software
 *
 * Licensed under the EUPL, Version 1.1 or - as soon as they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and limitations
 * under the Licence.
 */

package com.eviware.soapui.plugins;

import com.eviware.soapui.SoapUI;

import javax.annotation.Nonnull;
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

public final class ProductBodyguard extends Provider {
    private X509Certificate[] acceptedCerts = null;

    public ProductBodyguard() {
        super("SoapUIOSPluginSignChecker", 1.0, "Plugin signature validity checker");
    }

    private static X509Certificate[] setupProviderCert()
            throws IOException, CertificateException {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        return new X509Certificate[]{
                (X509Certificate) cf.generateCertificate(ProductBodyguard.class.getResourceAsStream("/com/eviware/soapui/plugins/PublicKey.key")),
                (X509Certificate) cf.generateCertificate(ProductBodyguard.class.getResourceAsStream("/com/eviware/soapui/plugins/PublicKeySB.key"))
        };
    }

    public final synchronized boolean isKnown(File plugin) {
        JarVerifier jv = new JarVerifier(plugin);

        try {
            if (acceptedCerts == null) {
                acceptedCerts = setupProviderCert();
            }
        } catch (Exception e) {
            SoapUI.logError(e);
            return false;
        }

        for (X509Certificate certificate : acceptedCerts) {
            if (isCertificateAccepted(jv, certificate)) {
                return true;
            }
        }

        return false;
    }

    private boolean isCertificateAccepted(@Nonnull JarVerifier jarVerifier, @Nonnull X509Certificate certificate) {
        boolean passed = true;
        try {
            jarVerifier.verify(certificate);
        } catch (Exception e) {
            passed = false;
        }

        return passed;
    }

    private class JarVerifier {
        private JarFile jarFile = null;

        JarVerifier(File jarURL) {
            try {
                jarFile = new JarFile(jarURL);
            } catch (Exception e) {
                SoapUI.logError(e);
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

            Vector<JarEntry> entriesVec = new Vector<>();

            Manifest man = jarFile.getManifest();
            if (man == null) {
                throw new SecurityException("The plugin '" + jarFile.getName() + "' is not signed");
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
                int n;
                while ((n = is.read(buffer, 0, buffer.length)) != -1) {
                }
                is.close();
            }

            Enumeration e = entriesVec.elements();

            while (e.hasMoreElements()) {
                JarEntry je = (JarEntry) e.nextElement();

                Certificate[] certs = je.getCertificates();
                if ((certs == null) || (certs.length == 0)) {
                    if (!je.getName().startsWith("META-INF")) {
                        throw new SecurityException("The plugin '" + jarFile.getName() + "' has unsigned class files.");
                    }
                } else {
                    int startIndex = 0;
                    X509Certificate[] certChain;
                    boolean signedAsExpected = false;

                    while ((certChain = getAChain(certs, startIndex)) != null) {
                        if (certChain[0].equals(targetCert)) {
                            signedAsExpected = true;
                            break;
                        }
                        startIndex += certChain.length;
                    }

                    if (!signedAsExpected) {
                        throw new SecurityException("The plugin '" + jarFile.getName() + "' is not signed by SmartBear Software");
                    }
                }
            }
        }

        private X509Certificate[] getAChain(Certificate[] certs, int startIndex) {
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
