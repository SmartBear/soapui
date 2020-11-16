/*
 * SoapUI, Copyright (C) 2004-2019 SmartBear Software
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

package com.eviware.soapui.impl.wsdl.support.wss.crypto;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.KeyMaterialCryptoConfig;
import com.eviware.soapui.impl.wsdl.AbstractWsdlModelItem;
import com.eviware.soapui.impl.wsdl.support.ExternalDependency;
import com.eviware.soapui.impl.wsdl.support.PathPropertyExternalDependency;
import com.eviware.soapui.impl.wsdl.support.wss.DefaultWssContainer;
import com.eviware.soapui.impl.wsdl.support.wss.WssContainer;
import com.eviware.soapui.impl.wsdl.support.wss.WssCrypto;
import com.eviware.soapui.impl.wsdl.teststeps.BeanPathPropertySupport;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.resolver.ResolveContext;
import com.google.common.io.Files;
import org.apache.commons.ssl.KeyStoreBuilder;
import org.apache.commons.ssl.ProbablyBadPasswordException;
import org.apache.commons.ssl.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.ws.security.components.crypto.CredentialException;
import org.apache.ws.security.components.crypto.Merlin;
import org.apache.ws.security.util.Loader;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.List;
import java.util.Properties;

public class KeyMaterialWssCrypto implements WssCrypto {
    private static final String JCEKS_KEYSTORE_TYPE = "jceks";
    private static final String JCEKS_FILE_EXTENSION = "jck";

    private static final String PKCS12_KEYSTORE_TYPE = "pkcs12";
    private static final String PKCS12_FILE_EXTENSION = "pk12";

    private KeyMaterialCryptoConfig config;
    private final WssContainer container;
    private KeyStore keyStore;
    private BeanPathPropertySupport sourceProperty;

    private static final Logger log = LogManager.getLogger(KeyMaterialWssCrypto.class);

    public KeyMaterialWssCrypto(KeyMaterialCryptoConfig config2, WssContainer container, String source,
                                String password, CryptoType type) {
        this(config2, container);
        setSource(source);
        setPassword(password);
        this.setType(type);
    }

    public KeyMaterialWssCrypto(KeyMaterialCryptoConfig cryptoConfig, WssContainer container2) {
        config = cryptoConfig;
        container = container2;

        sourceProperty = new BeanPathPropertySupport((AbstractWsdlModelItem<?>) container.getModelItem(), config,
                "source") {
            @Override
            protected void notifyUpdate(String value, String old) {
                getWssContainer().fireCryptoUpdated(KeyMaterialWssCrypto.this);
            }
        };
    }

    public Merlin getCrypto() {
        try {
            Properties properties = new Properties();

            properties.put("org.apache.ws.security.crypto.merlin.keystore.provider", "this");

            if (getType() == CryptoType.TRUSTSTORE) {
                properties.put("org.apache.ws.security.crypto.merlin.truststore.file", sourceProperty.expand());
            } else {
                properties.put("org.apache.ws.security.crypto.merlin.keystore.file", sourceProperty.expand());
                if (StringUtils.hasContent(getDefaultAlias())) {
                    properties.put("org.apache.ws.security.crypto.merlin.keystore.alias", getDefaultAlias());
                }
            }

            KeyMaterialCrypto keyMaterialCrypto = new KeyMaterialCrypto(properties);
            return keyMaterialCrypto;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getLabel() {
        String source = getSource();

        int ix = source.lastIndexOf(File.separatorChar);
        if (ix == -1) {
            ix = source.lastIndexOf('/');
        }

        if (ix != -1) {
            source = source.substring(ix + 1);
        }

        return source;
    }

    public String getSource() {
        return sourceProperty.expand();
    }

    public void udpateConfig(KeyMaterialCryptoConfig config) {
        this.config = config;
        sourceProperty.setConfig(config);
    }

    public void setSource(String source) {
        sourceProperty.set(source, true);
        keyStore = null;
    }

    /*
     * This loads the keystore / truststore file
     */
    // FIXME Why is this method called like times in a row?
    public KeyStore load() throws Exception {
        if (keyStore != null) {
            return keyStore;
        }

        try {
            UISupport.setHourglassCursor();

            String crypotFilePath = sourceProperty.expand();
            String fileExtension = Files.getFileExtension(crypotFilePath);
            String keystoreType = fileExtensionToKeystoreType(fileExtension);

            ClassLoader loader = Loader.getClassLoader(KeyMaterialWssCrypto.class);
            InputStream input = Merlin.loadInputStream(loader, crypotFilePath);
            keyStore = KeyStore.getInstance(keystoreType);

            char[] password = null;

            if (!StringUtils.isNullOrEmpty(getPassword())) {
                password = getPassword().toCharArray();
            }

            keyStore.load(input, password);

            return keyStore;
        } catch (Exception exceptionFromNormalLoad) {
            log.warn("Using fallback method to load keystore/truststore due to: " + exceptionFromNormalLoad.getMessage());
            try {
                keyStore = fallbackLoad();
                return keyStore;
            } catch (Exception exceptionFromFallbackLoad) {
                keyStore = null;
                SoapUI.logError(exceptionFromFallbackLoad, "Could not load keystore/truststore");
                throw new Exception(exceptionFromFallbackLoad);
            }
        } finally {
            UISupport.resetCursor();
        }
    }

    @Nonnull
    private String fileExtensionToKeystoreType(String fileExtension) {
        if (fileExtension.equals(PKCS12_FILE_EXTENSION)) {
            return PKCS12_KEYSTORE_TYPE;
        } else if (fileExtension.equals(JCEKS_FILE_EXTENSION)) {
            return JCEKS_KEYSTORE_TYPE;
        } else {
            return KeyStore.getDefaultType();
        }
    }

    /*
     * This is the less preferred way to loading cryptos, but is used for
     * backwards compability.
     */
    @javax.annotation.Nullable
    @Deprecated
    private KeyStore fallbackLoad() throws IOException, CertificateException, KeyStoreException,
            NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, ProbablyBadPasswordException,
            UnrecoverableKeyException, FileNotFoundException {
        KeyStore fallbackKeystore = null;
        if (StringUtils.hasContent(getDefaultAlias()) && StringUtils.hasContent(getAliasPassword())) {
            fallbackKeystore = KeyStoreBuilder.build(
                    Util.streamToBytes(new FileInputStream(sourceProperty.expand())), getDefaultAlias().getBytes(),
                    getPassword().toCharArray(), getAliasPassword().toCharArray());
        } else {
            fallbackKeystore = KeyStoreBuilder.build(
                    Util.streamToBytes(new FileInputStream(sourceProperty.expand())),
                    StringUtils.hasContent(getPassword()) ? getPassword().toCharArray() : null);
        }
        return fallbackKeystore;
    }

    public String getStatus() {
        try {
            if (StringUtils.hasContent(getSource())) {
                load();
                return "OK";
            } else {
                return "<unavailable>";
            }
        } catch (Exception e) {
            // FIXME We should also log the error if it weren't for all repedious calls to this method.
            return "<error: " + e.getMessage() + ">";
        }
    }

    public String getPassword() {
        return config.getPassword();
    }

    public String getAliasPassword() {
        return config.getAliasPassword();
    }

    public String getDefaultAlias() {
        return config.getDefaultAlias();
    }

    public void setAliasPassword(String arg0) {
        config.setAliasPassword(arg0);
    }

    public void setDefaultAlias(String arg0) {
        config.setDefaultAlias(arg0);
    }

    public void setPassword(String arg0) {
        config.setPassword(arg0);
        keyStore = null;
        getWssContainer().fireCryptoUpdated(this);
    }

    public String toString() {
        return getLabel();
    }

    public DefaultWssContainer getWssContainer() {
        return (DefaultWssContainer) container;
    }

    private class KeyMaterialCrypto extends Merlin {
        private KeyMaterialCrypto(Properties properties) throws CredentialException, IOException {
            super(properties);
        }

        @Override
        public KeyStore load(InputStream input, String storepass, String provider, String type)
                throws CredentialException {
            if ("this".equals(provider)) {
                try {
                    return KeyMaterialWssCrypto.this.load();
                } catch (Exception e) {
                    throw new CredentialException(0, null, e);
                }
            } else {
                return super.load(input, storepass, provider, type);
            }
        }

        @Override
        public String getCryptoProvider() {
            return config.getCryptoProvider();
        }
    }

    public String getCryptoProvider() {
        return config.getCryptoProvider();
    }

    public void setCryptoProvider(String provider) {
        config.setCryptoProvider(provider);
        keyStore = null;
        getWssContainer().fireCryptoUpdated(this);
    }

    @Override
    public CryptoType getType() {
        String typeConfig = config.getType();

        // Default to Keystore if type is not saved in configuration
        if (typeConfig == null) {
            typeConfig = CryptoType.KEYSTORE.name();
        }
        CryptoType type = CryptoType.valueOf(typeConfig);
        return type;
    }

    public void setType(@Nonnull CryptoType type) {
        config.setType(type.name());
    }

    public void resolve(ResolveContext<?> context) {
        sourceProperty.resolveFile(context, "Missing keystore/certificate file");
    }

    public void addExternalDependency(List<ExternalDependency> dependencies) {
        dependencies.add(new PathPropertyExternalDependency(sourceProperty));
    }
}
