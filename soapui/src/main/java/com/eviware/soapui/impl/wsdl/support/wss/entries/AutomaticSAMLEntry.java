/*
 * SoapUI, Copyright (C) 2004-2016 SmartBear Software 
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

package com.eviware.soapui.impl.wsdl.support.wss.entries;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.WSSEntryConfig;
import com.eviware.soapui.impl.wsdl.support.wss.OutgoingWss;
import com.eviware.soapui.impl.wsdl.support.wss.WssCrypto;
import com.eviware.soapui.impl.wsdl.support.wss.saml.callback.SAML1CallbackHandler;
import com.eviware.soapui.impl.wsdl.support.wss.saml.callback.SAML2CallbackHandler;
import com.eviware.soapui.impl.wsdl.support.wss.saml.callback.SAMLCallbackHandler;
import com.eviware.soapui.impl.wsdl.support.wss.support.KeystoresComboBoxModel;
import com.eviware.soapui.impl.wsdl.support.wss.support.SAMLAttributeValuesTable;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContext;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionsResult;
import com.eviware.soapui.support.components.SimpleBindingForm;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.soapui.support.xml.XmlObjectConfigurationBuilder;
import com.eviware.soapui.support.xml.XmlObjectConfigurationReader;
import com.google.common.base.Strings;
import com.jgoodies.binding.PresentationModel;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.message.WSSecHeader;
import org.apache.ws.security.message.WSSecSAMLToken;
import org.apache.ws.security.saml.WSSecSignatureSAML;
import org.apache.ws.security.saml.ext.AssertionWrapper;
import org.apache.ws.security.saml.ext.SAMLParms;
import org.apache.xml.security.algorithms.MessageDigestAlgorithm;
import org.apache.xml.security.signature.XMLSignature;
import org.w3c.dom.Document;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Erik R. Yverling
 *         <p/>
 *         Used to generate a SAML assertion using various input components
 */
public class AutomaticSAMLEntry extends WssEntryBase {
    public static final String TYPE = "SAML (Form)";

    public static final String SAML_VERSION_1 = "1.1";
    public static final String SAML_VERSION_2 = "2.0";

    public static final String AUTHENTICATION_ASSERTION_TYPE = "Authentication";
    public static final String ATTRIBUTE_ASSERTION_TYPE = "Attribute";
    public static final String AUTHORIZATION_ASSERTION_TYPE = "Authorization";

    public static final String ATTRIBUTE_VALUES_VALUE_COLUMN = "value";

    public static final String HOLDER_OF_KEY_CONFIRMATION_METHOD = "Holder-of-key";
    public static final String SENDER_VOUCHES_CONFIRMATION_METHOD = "Sender vouches";

    private static final String NOT_A_VALID_SAML_VERSION = "Not a valid SAML version";

    private KeyAliasComboBoxModel keyAliasComboBoxModel;
    private InternalWssContainerListener wssContainerListener;

    private String samlVersion;
    private String assertionType;
    private String confirmationMethod;
    private String crypto;
    private String issuer;
    private String subjectName;
    private String subjectQualifier;
    private String digestAlgorithm;
    private String signatureAlgorithm;
    private boolean signed;
    private String attributeName;
    private List<StringToStringMap> attributeValues;

    private SimpleBindingForm form;
    private JCheckBox signedCheckBox;
    private JComboBox confirmationMethodComboBox;
    private JComboBox cryptoComboBox;
    private JComboBox keyAliasComboBox;
    private JPasswordField passwordField;
    private JTextField attributeNameTextField;
    private SAMLAttributeValuesTable samlAttributeValuesTable;

    public void init(WSSEntryConfig config, OutgoingWss container) {
        super.init(config, container, TYPE);
    }

    // FIXME How can we make FindBugs that these fields will always be initialized and be able to add NonNull annotations?
    @Override
    protected void load(XmlObjectConfigurationReader reader) {
        samlVersion = reader.readString("samlVersion", SAML_VERSION_1);
        signed = reader.readBoolean("signed", false);
        assertionType = reader.readString("assertionType", AUTHENTICATION_ASSERTION_TYPE);
        confirmationMethod = reader.readString("confirmationMethod", SENDER_VOUCHES_CONFIRMATION_METHOD);
        crypto = reader.readString("crypto", null);
        issuer = reader.readString("issuer", null);
        subjectName = reader.readString("subjectName", null);
        subjectQualifier = reader.readString("subjectQualifier", null);
        digestAlgorithm = reader.readString("digestAlgorithm", MessageDigestAlgorithm.ALGO_ID_DIGEST_SHA1);
        signatureAlgorithm = reader.readString("signatureAlgorithm", WSConstants.RSA);
        attributeName = reader.readString("attributeName", null);
        attributeValues = readTableValues(reader, "attributeValues");
    }

    @Override
    protected void save(XmlObjectConfigurationBuilder builder) {
        builder.add("samlVersion", samlVersion);
        builder.add("signed", signed);
        builder.add("assertionType", assertionType);
        builder.add("confirmationMethod", confirmationMethod);
        builder.add("crypto", crypto);
        builder.add("issuer", issuer);
        builder.add("subjectName", subjectName);
        builder.add("subjectQualifier", subjectQualifier);
        builder.add("digestAlgorithm", digestAlgorithm);
        builder.add("signatureAlgorithm", signatureAlgorithm);
        builder.add("attributeName", attributeName);
        saveTableValues(builder, attributeValues, "attributeValues");
    }

    @Override
    protected JComponent buildUI() {
        wssContainerListener = new InternalWssContainerListener();
        getWssContainer().addWssContainerListener(wssContainerListener);

        form = new SimpleBindingForm(new PresentationModel<SignatureEntry>(this));

        form.addSpace(5);

        form.appendComboBox("samlVersion", "SAML version", new String[]{SAML_VERSION_1, SAML_VERSION_2},
                "Choose the SAML version");

        signedCheckBox = form.appendCheckBox("signed", "Signed", null);
        signedCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                checkSigned();
            }

        });

        form.appendComboBox("assertionType", "Assertion type",
                new String[]{AUTHENTICATION_ASSERTION_TYPE, ATTRIBUTE_ASSERTION_TYPE, AUTHORIZATION_ASSERTION_TYPE},
                "Choose the type of assertion").addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                checkAssertionType();
            }

        });

        confirmationMethodComboBox = form.appendComboBox("confirmationMethod", "Confirmation method",
                new String[]{SENDER_VOUCHES_CONFIRMATION_METHOD}, "Choose the confirmation method");

        cryptoComboBox = form.appendComboBox("crypto", "Keystore", new KeystoresComboBoxModel(getWssContainer(),
                getWssContainer().getCryptoByName(crypto), true),
                "Selects the Keystore containing the key to use for signing the SAML message");

        cryptoComboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                // FIXME This cases the drop down to be blank when changing keystore
                keyAliasComboBoxModel.update(getWssContainer().getCryptoByName(crypto));
            }
        });

        keyAliasComboBoxModel = new KeyAliasComboBoxModel(getWssContainer().getCryptoByName(crypto));
        keyAliasComboBox = form.appendComboBox("username", "Alias", keyAliasComboBoxModel,
                "The alias for the key to use for encryption");

        passwordField = form.appendPasswordField("password", "Password", "The certificate password");

        form.appendTextField("issuer", "Issuer", "The issuer");

        form.appendTextField("subjectName", "Subject Name", "The subject qualifier");

        form.appendTextField("subjectQualifier", "Subject Qualifier", "The subject qualifier");

        form.appendComboBox("digestAlgorithm", "Digest Algorithm", new String[]{
                MessageDigestAlgorithm.ALGO_ID_DIGEST_SHA1, MessageDigestAlgorithm.ALGO_ID_DIGEST_SHA256,
                MessageDigestAlgorithm.ALGO_ID_DIGEST_SHA384, MessageDigestAlgorithm.ALGO_ID_DIGEST_SHA512},
                "Set the digest algorithm to use");

        form.appendComboBox("signatureAlgorithm", "Signature Algorithm", new String[]{WSConstants.RSA,
                WSConstants.DSA, XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA256, XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA384,
                XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA512, XMLSignature.ALGO_ID_MAC_HMAC_SHA1,
                XMLSignature.ALGO_ID_MAC_HMAC_SHA256, XMLSignature.ALGO_ID_MAC_HMAC_SHA384,
                XMLSignature.ALGO_ID_MAC_HMAC_SHA512}, "Set the name of the signature encryption algorithm to use");

        attributeNameTextField = form.appendTextField("attributeName", "Attribute name", "The name of the attribute");

        samlAttributeValuesTable = new SAMLAttributeValuesTable(attributeValues, this);
        form.append("Attribute values", samlAttributeValuesTable);

        initComponentsEnabledState();

        return new JScrollPane(form.getPanel());
    }

    private void initComponentsEnabledState() {
        checkSigned();
        checkAssertionType();
    }

    private void checkSigned() {
        if (!signed) {
            form.setComboBoxItems("confirmationMethod", confirmationMethodComboBox,
                    new String[]{SENDER_VOUCHES_CONFIRMATION_METHOD});
            confirmationMethodComboBox.setSelectedIndex(0);
            cryptoComboBox.setEnabled(false);
            keyAliasComboBox.setEnabled(false);
            passwordField.setEnabled(false);
        } else {
            form.setComboBoxItems("confirmationMethod", confirmationMethodComboBox, new String[]{
                    SENDER_VOUCHES_CONFIRMATION_METHOD, HOLDER_OF_KEY_CONFIRMATION_METHOD});
            cryptoComboBox.setEnabled(true);
            keyAliasComboBox.setEnabled(true);
            passwordField.setEnabled(true);
        }
    }

    private void checkAssertionType() {
        if (assertionType.equals(AUTHORIZATION_ASSERTION_TYPE)) {
            signed = false;
            signedCheckBox.setSelected(false);
            signedCheckBox.setEnabled(false);
        } else {
            signedCheckBox.setEnabled(true);
        }

        if (assertionType.equals(ATTRIBUTE_ASSERTION_TYPE)) {
            attributeNameTextField.setEnabled(true);
            samlAttributeValuesTable.setEnabled(true);
        } else {
            attributeNameTextField.setEnabled(false);
            samlAttributeValuesTable.setEnabled(false);
        }
    }

    public void process(WSSecHeader secHeader, Document doc, PropertyExpansionContext context) {
        try {
            SAMLParms samlParms = new SAMLParms();
            SAMLCallbackHandler callbackHandler = null;

            if (!signed) {
                WSSecSAMLToken wsSecSAMLToken = new WSSecSAMLToken();

                if (samlVersion.equals(SAML_VERSION_1)) {
                    callbackHandler = new SAML1CallbackHandler(assertionType, confirmationMethod);
                } else if (samlVersion.equals(SAML_VERSION_2)) {
                    callbackHandler = new SAML2CallbackHandler(assertionType, confirmationMethod);
                } else {
                    throw new IllegalArgumentException(NOT_A_VALID_SAML_VERSION);
                }
                AssertionWrapper assertion = createAssertion(context, samlParms, callbackHandler);
                wsSecSAMLToken.build(doc, assertion, secHeader);
            } else {
                WSSecSignatureSAML wsSecSignatureSAML = new WSSecSignatureSAML();
                WssCrypto wssCrypto = getWssContainer().getCryptoByName(crypto, true);
                String alias = context.expand(getUsername());

                if (wssCrypto == null) {
                    throw new RuntimeException("Missing keystore [" + crypto + "] for signature entry");
                } else if (Strings.isNullOrEmpty(alias)) {
                    throw new RuntimeException(" No alias was provided for the keystore '" + crypto + "'. Please check your SAML (Form) configurations");
                }

                if (samlVersion.equals(SAML_VERSION_1)) {
                    callbackHandler = new SAML1CallbackHandler(wssCrypto.getCrypto(), alias,
                            assertionType, confirmationMethod);
                } else if (samlVersion.equals(SAML_VERSION_2)) {
                    callbackHandler = new SAML2CallbackHandler(wssCrypto.getCrypto(), alias,
                            assertionType, confirmationMethod);
                } else {
                    throw new IllegalArgumentException(NOT_A_VALID_SAML_VERSION);
                }

                AssertionWrapper assertion = createAssertion(context, samlParms, callbackHandler);

                assertion.signAssertion(context.expand(getUsername()), context.expand(getPassword()),
                        wssCrypto.getCrypto(), false);

                wsSecSignatureSAML.setUserInfo(context.expand(getUsername()), context.expand(getPassword()));

                if (confirmationMethod.equals(SENDER_VOUCHES_CONFIRMATION_METHOD)) {
                    wsSecSignatureSAML.setKeyIdentifierType(WSConstants.BST_DIRECT_REFERENCE);

                    wsSecSignatureSAML.build(doc, null, assertion, wssCrypto.getCrypto(), context.expand(getUsername()),
                            context.expand(getPassword()), secHeader);
                } else if (confirmationMethod.equals(HOLDER_OF_KEY_CONFIRMATION_METHOD)) {
                    wsSecSignatureSAML.setDigestAlgo(digestAlgorithm);

                    if (assertionType.equals(AUTHENTICATION_ASSERTION_TYPE)) {
                        wsSecSignatureSAML.setKeyIdentifierType(WSConstants.BST_DIRECT_REFERENCE);
                        wsSecSignatureSAML.setSignatureAlgorithm(signatureAlgorithm);
                    } else if (assertionType.equals(ATTRIBUTE_ASSERTION_TYPE)) {

                        wsSecSignatureSAML.setKeyIdentifierType(WSConstants.X509_KEY_IDENTIFIER);
                        wsSecSignatureSAML.setSignatureAlgorithm(signatureAlgorithm);

                        byte[] ephemeralKey = callbackHandler.getEphemeralKey();
                        wsSecSignatureSAML.setSecretKey(ephemeralKey);
                    }

                    wsSecSignatureSAML.build(doc, wssCrypto.getCrypto(), assertion, null, null, null, secHeader);
                }
            }

        } catch (Exception e) {
            SoapUI.logError(e);
        }
    }

    private AssertionWrapper createAssertion(PropertyExpansionContext context, SAMLParms samlParms,
                                             SAMLCallbackHandler callbackHandler) throws WSSecurityException {
        if (assertionType.equals(ATTRIBUTE_ASSERTION_TYPE)) {
            callbackHandler.setCustomAttributeName(context.expand(attributeName));
            callbackHandler.setCustomAttributeValues(extractValueColumnValues(attributeValues, context));
        }

        callbackHandler.setIssuer(context.expand(issuer));
        callbackHandler.setSubjectName(context.expand(subjectName));
        callbackHandler.setSubjectQualifier(context.expand(subjectQualifier));

        samlParms.setCallbackHandler(callbackHandler);
        return new AssertionWrapper(samlParms);
    }

    // Since we only use one column for the attribute values
    private List<String> extractValueColumnValues(List<StringToStringMap> table, PropertyExpansionContext context) {
        List<String> firstColumnValues = new ArrayList<String>();
        for (StringToStringMap row : table) {
            String columnValue = row.get(ATTRIBUTE_VALUES_VALUE_COLUMN);
            // TODO Add property expansion to each value
            firstColumnValues.add(columnValue);
        }
        return firstColumnValues;
    }

    public void relase() {
        if (wssContainerListener != null) {
            getWssContainer().removeWssContainerListener(wssContainerListener);
        }
    }

    @Override
    protected void addPropertyExpansions(PropertyExpansionsResult result) {
        super.addPropertyExpansions(result);
        result.extractAndAddAll(this, "issuer");
        result.extractAndAddAll(this, "subjectName");
        result.extractAndAddAll(this, "subjectQualifier");
        result.extractAndAddAll(this, "attributeName");
        // TODO Add property expansion refactoring for attributesValues, as with HttpTestRequestStep
    }

    public String getSamlVersion() {
        return samlVersion;
    }

    public void setSamlVersion(String samlVersion) {
        this.samlVersion = samlVersion;
        saveConfig();
    }

    public String getAssertionType() {
        return assertionType;
    }

    public void setAssertionType(String assertionType) {
        this.assertionType = assertionType;
        saveConfig();
    }

    public String getConfirmationMethod() {
        return confirmationMethod;
    }

    public void setConfirmationMethod(String confirmationMethod) {
        this.confirmationMethod = confirmationMethod;
        saveConfig();
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
        saveConfig();
    }

    public String getCrypto() {
        return crypto;
    }

    public void setCrypto(String crypto) {
        this.crypto = crypto;
        saveConfig();
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
        saveConfig();
    }

    public String getSubjectQualifier() {
        return subjectQualifier;
    }

    public void setSubjectQualifier(String subjectQualifier) {
        this.subjectQualifier = subjectQualifier;
        saveConfig();
    }

    public String getDigestAlgorithm() {
        return digestAlgorithm;
    }

    public void setDigestAlgorithm(String digestAlgorithm) {
        this.digestAlgorithm = digestAlgorithm;
        saveConfig();
    }

    public String getSignatureAlgorithm() {
        return signatureAlgorithm;
    }

    public void setSignatureAlgorithm(String signatureAlgorithm) {
        this.signatureAlgorithm = signatureAlgorithm;
        saveConfig();
    }

    public boolean isSigned() {
        return signed;
    }

    public void setSigned(boolean signed) {
        this.signed = signed;
        saveConfig();
    }

    public String getAttributeName() {
        return attributeName;
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
        saveConfig();
    }

    public List<StringToStringMap> getAttributeValues() {
        return attributeValues;
    }

    public void setAttributeValues(List<StringToStringMap> attributeValues) {
        this.attributeValues = attributeValues;
        saveConfig();
    }

    private final class InternalWssContainerListener extends WssContainerListenerAdapter {
        @Override
        public void cryptoUpdated(WssCrypto crypto) {
            if (crypto.getLabel().equals(getCrypto())) {
                keyAliasComboBoxModel.update(crypto);
            }
        }
    }
}
