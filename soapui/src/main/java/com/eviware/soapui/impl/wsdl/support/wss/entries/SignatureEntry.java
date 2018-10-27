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

package com.eviware.soapui.impl.wsdl.support.wss.entries;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.WSSEntryConfig;
import com.eviware.soapui.impl.wsdl.support.wss.ImprovedWSSecSignature;
import com.eviware.soapui.impl.wsdl.support.wss.OutgoingWss;
import com.eviware.soapui.impl.wsdl.support.wss.WssCrypto;
import com.eviware.soapui.impl.wsdl.support.wss.support.KeystoresComboBoxModel;
import com.eviware.soapui.impl.wsdl.support.wss.support.WSPartsTable;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContext;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionsResult;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.components.SimpleBindingForm;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.soapui.support.xml.XmlObjectConfigurationBuilder;
import com.eviware.soapui.support.xml.XmlObjectConfigurationReader;
import com.eviware.soapui.support.xml.XmlUtils;
import com.jgoodies.binding.PresentationModel;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSEncryptionPart;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.message.DOMCallbackLookup;
import org.apache.ws.security.message.WSSecHeader;
import org.apache.xml.security.algorithms.MessageDigestAlgorithm;
import org.apache.xml.security.signature.XMLSignature;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

public class SignatureEntry extends WssEntryBase {
    private static final String DEFAULT_OPTION = "<default>";
    public static final String TYPE = "Signature";
    private String crypto;
    private int keyIdentifierType = 0;
    private String signatureAlgorithm;
    private boolean useSingleCert;
    private boolean prependSignature = true;
    private String signatureCanonicalization;
    private String digestAlgorithm;
    private String customTokenValueType;
    private String customTokenId;
    private List<StringToStringMap> parts = new ArrayList<StringToStringMap>();
    private com.eviware.soapui.impl.wsdl.support.wss.entries.WssEntryBase.KeyAliasComboBoxModel keyAliasComboBoxModel;
    private com.eviware.soapui.impl.wsdl.support.wss.entries.SignatureEntry.InternalWssContainerListener wssContainerListener;

    private JTextField customTokenValueTypeField;
    private JTextField customTokenIdField;

    public void init(WSSEntryConfig config, OutgoingWss container) {
        super.init(config, container, TYPE);
    }

    @Override
    protected JComponent buildUI() {
        SimpleBindingForm form = new SimpleBindingForm(new PresentationModel<SignatureEntry>(this));
        form.addSpace(5);
        wssContainerListener = new InternalWssContainerListener();
        getWssContainer().addWssContainerListener(wssContainerListener);

        form.appendComboBox("crypto", "Keystore",
                new KeystoresComboBoxModel(getWssContainer(), getWssContainer().getCryptoByName(crypto), true),
                "Selects the Keystore containing the key to use for signing").addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                keyAliasComboBoxModel.update(getWssContainer().getCryptoByName(crypto));
            }
        });

        keyAliasComboBoxModel = new KeyAliasComboBoxModel(getWssContainer().getCryptoByName(crypto));
        form.appendComboBox("username", "Alias", keyAliasComboBoxModel, "The alias for the key to use for encryption");

        form.appendPasswordField("password", "Password", "The certificate password");

        JComboBox keyIdentifierTypeComboBox = form.appendComboBox("keyIdentifierType", "Key Identifier Type", new Integer[]{1, 2, 3, 4, 8, 12},
                "Sets which key identifier to use");
        keyIdentifierTypeComboBox.setRenderer(new KeyIdentifierTypeRenderer());
        keyIdentifierTypeComboBox.addItemListener(new ItemListener() {
            	@Override
            	public void itemStateChanged(ItemEvent e) {
                    initCustomTokenState();
                }
        });
        form.appendComboBox("signatureAlgorithm", "Signature Algorithm", new String[]{DEFAULT_OPTION, WSConstants.RSA,
                WSConstants.DSA, XMLSignature.ALGO_ID_MAC_HMAC_SHA1, XMLSignature.ALGO_ID_MAC_HMAC_SHA256,
                XMLSignature.ALGO_ID_MAC_HMAC_SHA384, XMLSignature.ALGO_ID_MAC_HMAC_SHA512,
                XMLSignature.ALGO_ID_MAC_HMAC_RIPEMD160, XMLSignature.ALGO_ID_MAC_HMAC_NOT_RECOMMENDED_MD5,
                XMLSignature.ALGO_ID_SIGNATURE_ECDSA_SHA1, XMLSignature.ALGO_ID_SIGNATURE_NOT_RECOMMENDED_RSA_MD5,
                XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA1, XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA256,
                XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA384, XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA512,
                XMLSignature.ALGO_ID_SIGNATURE_RSA_RIPEMD160}, "Set the name of the signature encryption algorithm to use");
        form.appendComboBox("signatureCanonicalization", "Signature Canonicalization", new String[]{DEFAULT_OPTION,
                WSConstants.C14N_OMIT_COMMENTS, WSConstants.C14N_WITH_COMMENTS, WSConstants.C14N_EXCL_OMIT_COMMENTS,
                WSConstants.C14N_EXCL_WITH_COMMENTS}, "Set the canonicalization method to use.");

        form.appendComboBox("digestAlgorithm", "Digest Algorithm", new String[]{DEFAULT_OPTION,
                MessageDigestAlgorithm.ALGO_ID_DIGEST_NOT_RECOMMENDED_MD5, MessageDigestAlgorithm.ALGO_ID_DIGEST_SHA1,
                MessageDigestAlgorithm.ALGO_ID_DIGEST_SHA256, MessageDigestAlgorithm.ALGO_ID_DIGEST_SHA384,
                MessageDigestAlgorithm.ALGO_ID_DIGEST_SHA512, MessageDigestAlgorithm.ALGO_ID_DIGEST_RIPEMD160},
                "Set the digest algorithm to use");

        form.appendCheckBox("useSingleCert", "Use Single Certificate", "Use single certificate for signing");
        form.appendCheckBox("prependSignature", "Prepend Signature Element",
                "Prepend signature element to security header (non-strict layout)");

        customTokenIdField = form.appendTextField("customTokenId", "Custom Key Identifier", "Use a custom key identifier for signing");
        customTokenValueTypeField = form.appendTextField("customTokenValueType", "Custom Key Identifier ValueType", "Specify the custom key identifier value type");
        initCustomTokenState();

        form.append("Parts", new WSPartsTable(parts, this));

        return new JScrollPane(form.getPanel());
    }

    @Override
    public void release() {
        if (wssContainerListener != null) {
            getWssContainer().removeWssContainerListener(wssContainerListener);
        }
    }

    private void initCustomTokenState() {
        boolean enabled = keyIdentifierType == WSConstants.CUSTOM_KEY_IDENTIFIER;
        customTokenValueTypeField.setEnabled(enabled);
        customTokenIdField.setEnabled(enabled);
    }

    @Override
    protected void load(XmlObjectConfigurationReader reader) {
        crypto = reader.readString("crypto", null);
        keyIdentifierType = readKeyIdentifierType(reader);
        signatureAlgorithm = reader.readString("signatureAlgorithm", null);
        signatureCanonicalization = reader.readString("signatureCanonicalization", null);
        useSingleCert = reader.readBoolean("useSingleCert", false);
        prependSignature = reader.readBoolean("prependSignature", true);

        digestAlgorithm = reader.readString("digestAlgorithm", null);

        customTokenValueType = reader.readString( "customTokenValueType", null );
        customTokenId = reader.readString("customTokenId", null);

        parts = readTableValues(reader, "signaturePart");
    }

    @Override
    protected void save(XmlObjectConfigurationBuilder builder) {
        builder.add("crypto", crypto);
        builder.add("keyIdentifierType", keyIdentifierType);
        builder.add("signatureAlgorithm", signatureAlgorithm);
        builder.add("signatureCanonicalization", signatureCanonicalization);
        builder.add("useSingleCert", useSingleCert);
        builder.add("prependSignature", prependSignature);

        builder.add("digestAlgorithm", digestAlgorithm);

        builder.add( "customTokenValueType", customTokenValueType );
        builder.add( "customTokenId", customTokenId );

        saveTableValues(builder, parts, "signaturePart");
    }

    public void process(WSSecHeader secHeader, Document doc, PropertyExpansionContext context) {
        StringWriter writer = null;

        try {
            WssCrypto wssCrypto = getWssContainer().getCryptoByName(crypto);
            if (wssCrypto == null) {
                throw new Exception("Missing crypto [" + crypto + "] for signature entry");
            }

            ImprovedWSSecSignature wssSign = new ImprovedWSSecSignature();
            wssSign.setUserInfo(context.expand(getUsername()), context.expand(getPassword()));

            // default is
            // http://ws.apache.org/wss4j/apidocs/org/apache/ws/security/WSConstants.html#ISSUER_SERIAL
            if (keyIdentifierType != 0) {
                wssSign.setKeyIdentifierType(keyIdentifierType);
            }

            if (StringUtils.hasContent(signatureAlgorithm) && !signatureAlgorithm.equals(DEFAULT_OPTION)) {
                wssSign.setSignatureAlgorithm(signatureAlgorithm);
            }

            if (StringUtils.hasContent(signatureCanonicalization) && !signatureCanonicalization.equals(DEFAULT_OPTION)) {
                wssSign.setSigCanonicalization(signatureCanonicalization);
            }

            wssSign.setUseSingleCertificate(useSingleCert);

            wssSign.setPrependSignature(prependSignature);

            if (StringUtils.hasContent(digestAlgorithm)) {
                wssSign.setDigestAlgo(digestAlgorithm);
            }

            if (keyIdentifierType == WSConstants.CUSTOM_KEY_IDENTIFIER) {
                if(StringUtils.hasContent( customTokenId )) {
                    wssSign.setCustomTokenId(context.expand(customTokenId));
                }

                if(StringUtils.hasContent(customTokenValueType )) {
                    wssSign.setCustomTokenValueType(context.expand(customTokenValueType));
                }
            }

            Vector<WSEncryptionPart> wsParts = createWSParts(parts);
            if (!wsParts.isEmpty()) {
                wssSign.setParts(wsParts);
            }

            writer = new StringWriter();
            XmlUtils.serialize(doc, writer);

            wssSign.setCallbackLookup(new BinarySecurityTokenDOMCallbackLookup(doc, wssSign));
            wssSign.build(doc, wssCrypto.getCrypto(), secHeader);
        } catch (Exception e) {
            SoapUI.logError(e);

            if (writer != null && writer.getBuffer().length() > 0) {
                try {
                    doc.replaceChild(doc.importNode(XmlUtils.parseXml(writer.toString()).getDocumentElement(), true),
                            doc.getDocumentElement());
                } catch (Exception e1) {
                    SoapUI.logError(e1);
                }
            }
        }
    }

    @Override
    protected void addPropertyExpansions(PropertyExpansionsResult result) {
        super.addPropertyExpansions(result);
    }

    public String getCrypto() {
        return crypto;
    }

    public void setCrypto(String crypto) {
        this.crypto = crypto;
        saveConfig();
    }

    public int getKeyIdentifierType() {
        return keyIdentifierType;
    }

    public void setKeyIdentifierType(int keyIdentifierType) {
        this.keyIdentifierType = keyIdentifierType;
        saveConfig();
    }

    public String getSignatureAlgorithm() {
        return StringUtils.isNullOrEmpty(signatureAlgorithm) ? DEFAULT_OPTION : signatureAlgorithm;
    }

    public void setSignatureAlgorithm(String signatureAlgorithm) {
        if (DEFAULT_OPTION.equals(signatureAlgorithm)) {
            signatureAlgorithm = null;
        }

        this.signatureAlgorithm = signatureAlgorithm;
        saveConfig();
    }

    public String getDigestAlgorithm() {
        return StringUtils.isNullOrEmpty(digestAlgorithm) ? DEFAULT_OPTION : digestAlgorithm;
    }

    public void setDigestAlgorithm(String digestAlgorithm) {
        if (DEFAULT_OPTION.equals(digestAlgorithm)) {
            digestAlgorithm = null;
        }

        this.digestAlgorithm = digestAlgorithm;
        saveConfig();
    }

    public String getSignatureCanonicalization() {
        return StringUtils.isNullOrEmpty(signatureCanonicalization) ? DEFAULT_OPTION : signatureCanonicalization;
    }

    public void setSignatureCanonicalization(String signatureCanonicalization) {
        if (DEFAULT_OPTION.equals(signatureCanonicalization)) {
            signatureCanonicalization = null;
        }

        this.signatureCanonicalization = signatureCanonicalization;
        saveConfig();
    }

    public boolean isUseSingleCert() {
        return useSingleCert;
    }

    public void setUseSingleCert(boolean useSingleCert) {
        this.useSingleCert = useSingleCert;
        saveConfig();
    }

    public boolean isPrependSignature() {
        return prependSignature;
    }

    public void setPrependSignature(boolean prependSignature) {
        this.prependSignature = prependSignature;
        saveConfig();
    }

    public String getCustomTokenId() {
        return customTokenId;
    }

    public void setCustomTokenId(String customTokenId) {
        this.customTokenId = customTokenId;
        saveConfig();
    }

    public String getCustomTokenValueType() {
        return customTokenValueType;
    }

    public void setCustomTokenValueType(String customTokenValueType) {
        this.customTokenValueType = customTokenValueType;
        saveConfig();
    }

    public void setParts(List<StringToStringMap> parts) {
        this.parts = parts;
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

    /**
     * This callback class extends the default DOMCallbackLookup class with a hook to return the prepared
     * wsse:BinarySecurityToken
     */
    private static class BinarySecurityTokenDOMCallbackLookup extends DOMCallbackLookup {

        private final ImprovedWSSecSignature wssSign;

        public BinarySecurityTokenDOMCallbackLookup(Document doc, ImprovedWSSecSignature wssSign) {
            super(doc);
            this.wssSign = wssSign;
        }

        @Override
        public List<Element> getElements(String localname, String namespace) throws WSSecurityException {
            List<Element> elements = super.getElements(localname, namespace);
            if (elements.isEmpty()) {
                // element was not found in DOM document
                if (WSConstants.BINARY_TOKEN_LN.equals(localname) && WSConstants.WSSE_NS.equals(namespace)) {
                    /* In case the element searched for is the wsse:BinarySecurityToken, return the element prepared by
                       wsee4j. If we return the original DOM element, the digest calculation fails because the element
                       is not yet attached to the DOM tree, so instead return a copy which includes all namespaces */
                    try {
                        DOMResult result = new DOMResult();
                        Transformer transformer = TransformerFactory.newInstance().newTransformer();
                        transformer.transform(new DOMSource(wssSign.getBinarySecurityTokenElement()), result);
                        return Collections.singletonList(((Document) result.getNode()).getDocumentElement());
                    } catch (TransformerException e) {
                        SoapUI.logError(e);
                    }
                }
            }
            return elements;
        }
    }
}
