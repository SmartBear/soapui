package com.eviware.soapui.impl.support;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.SoapUISystemProperties;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.settings.SSLSettings;
import com.eviware.soapui.support.UISupport;
import org.apache.commons.ssl.KeyMaterial;
import org.apache.logging.log4j.Logger;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.X509ExtendedTrustManager;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class SSLUtils {
    private static final String INIT_KEYSTORE_INFO_MESSAGE = "Initializing KeyStore";

    /**
     * Loads and returns key store based on the ReadyAPI Preferences and ReadyAPI system properties.
     */
    public static KeyStore getReadyApiKeystore() throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException {
        return getReadyApiKeystore(null);
    }

    /**
     * Loads and returns key store based on the ReadyAPI Preferences and ReadyAPI system properties.
     */
    public static KeyStore getReadyApiKeystore(Logger logger) throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException {
        Settings settings = SoapUI.getSettings();
        KeyStore keyStore;

        if (UISupport.isWindows() /*&& settings.getBoolean(SSLSettings.USE_WINDOWS_KEYSTORE, false)*/ && false) {
            keyStore = KeyStore.getInstance("Windows-MY");
            keyStore.load(null, null);
        } else {
            String keyStoreUrl = getKeyStoreUrl();
            String password = getKeyStorePassword();

            keyStore = getKeyStore(keyStoreUrl, password, logger);
        }

        return keyStore;
    }

    /**
     * Returns KeyStore URL specified in the ReadyAPI Preferences or in the ReadyAPI system properties
     */
    public static String getKeyStoreUrl() {
        Settings settings = SoapUI.getSettings();
        String keyStoreUrl = System.getProperty(SoapUISystemProperties.SOAPUI_SSL_KEYSTORE_LOCATION,
                settings.getString(SSLSettings.KEYSTORE, ""));
        return keyStoreUrl.trim();
    }

    /**
     * Returns KeyStore Password specified in the ReadyAPI Preferences or in the ReadyAPI system properties
     */
    public static String getKeyStorePassword() {
        Settings settings = SoapUI.getSettings();
        return System.getProperty(SoapUISystemProperties.SOAPUI_SSL_KEYSTORE_PASSWORD,
                settings.getString(SSLSettings.KEYSTORE_PASSWORD, ""));
    }

    private static KeyStore getKeyStore(String keyStoreUrl, String password, Logger logger) {
        KeyStore keyStore = null;
        char[] pwd = password.toCharArray();

        if (keyStoreUrl.trim().length() > 0) {
            File f = new File(keyStoreUrl);
            if (f.exists()) {
                if (logger != null) {
                    logger.info(INIT_KEYSTORE_INFO_MESSAGE);
                }

                try {
                    KeyMaterial km = new KeyMaterial(f, pwd);
                    keyStore = km.getKeyStore();
                } catch (Exception e) {
                    //Logging.logError(e);
                }
            }
        }

        return keyStore;
    }

    /**
     * Returns a TrustManager which trusts all certificates
     */
    public static X509ExtendedTrustManager getTrustAllManager() {
        return new X509ExtendedTrustManager() {
            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            @Override
            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }

            @Override
            public void checkClientTrusted(X509Certificate[] x509Certificates, String s, Socket socket) {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] x509Certificates, String s, Socket socket) {
            }

            @Override
            public void checkClientTrusted(X509Certificate[] x509Certificates, String s, SSLEngine sslEngine) {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] x509Certificates, String s, SSLEngine sslEngine) {
            }
        };
    }
}
