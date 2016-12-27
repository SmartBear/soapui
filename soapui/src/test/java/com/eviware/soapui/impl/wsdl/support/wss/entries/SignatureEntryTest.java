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

import com.eviware.soapui.config.WSSEntryConfig;
import com.eviware.soapui.impl.wsdl.support.wss.OutgoingWss;
import com.eviware.soapui.impl.wsdl.support.wss.WssContainer;
import com.eviware.soapui.impl.wsdl.support.wss.WssCrypto;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContext;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.soapui.support.xml.XmlUtils;
import com.eviware.soapui.utils.TestUtils;
import org.apache.ws.commons.util.NamespaceContextImpl;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSSConfig;
import org.apache.ws.security.WSSecurityEngine;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.components.crypto.CredentialException;
import org.apache.ws.security.components.crypto.Merlin;
import org.apache.ws.security.message.WSSecHeader;
import org.apache.ws.security.util.Loader;
import org.apache.xml.security.algorithms.MessageDigestAlgorithm;
import org.apache.xml.security.signature.XMLSignature;
import org.apache.xmlbeans.XmlObject;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Collections;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

/**
 * @author Erik R. Yverling
 */

public class SignatureEntryTest {
    private static final String ISSUER = "www.issuer.com";
    private static final String KEYSTORE_PATH = "keys/keystore.jks";
    private static final String KEYSTORE_PASSWORD = "foobar42";
    private static final String KEY_PASSWORD = "foobar42";
    private static final String ALIAS = "certificatekey";

    private SignatureEntry signatureEntry;

    private Merlin crypto;
    private Document doc;
    private WSSecHeader secHeader;
    private XPath xpath;

    @Mock
    private PropertyExpansionContext contextMock;
    @Mock
    private OutgoingWss outgoingWssMock;
    @Mock
    private WSSEntryConfig wssEntryConfigMock;
    @Mock
    private WssContainer wssContainerMock;
    @Mock
    private WssCrypto wssCryptoMock;
    @Mock
    private XmlObject xmlObjectMock;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        initXpath();

        doc = XmlUtils.parseXml(TestUtils.SAMPLE_SOAP_MESSAGE);

        secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);

        signatureEntry = new SignatureEntry();
        signatureEntry.init(wssEntryConfigMock, outgoingWssMock);

        when(wssEntryConfigMock.getConfiguration()).thenReturn(xmlObjectMock);

        signatureEntry.setSignatureAlgorithm(WSConstants.RSA);
        signatureEntry.setDigestAlgorithm(MessageDigestAlgorithm.ALGO_ID_DIGEST_SHA1);

        createCrypto();

        when(outgoingWssMock.getWssContainer()).thenReturn(wssContainerMock);
        when(wssContainerMock.getCryptoByName(anyString(), anyBoolean())).thenReturn(wssCryptoMock);
        when(wssContainerMock.getCryptoByName(anyString())).thenReturn(wssCryptoMock);
        when(wssCryptoMock.getCrypto()).thenReturn(crypto);
        when(outgoingWssMock.getUsername()).thenReturn(ALIAS);
        when(outgoingWssMock.getPassword()).thenReturn(KEY_PASSWORD);

        when(contextMock.expand(ALIAS)).thenReturn(ALIAS);
        when(contextMock.expand(KEY_PASSWORD)).thenReturn(KEY_PASSWORD);
        when(contextMock.expand(ISSUER)).thenReturn(ISSUER);
        when(contextMock.expand("Assertion-01")).thenReturn("Assertion-01");
        when(contextMock.expand(WSConstants.WSS_SAML_KI_VALUE_TYPE)).thenReturn(WSConstants.WSS_SAML_KI_VALUE_TYPE);
    }

    @Test
    public void testProcessBinarySecurityToken() throws XPathExpressionException {
        signatureEntry.setKeyIdentifierType(WSConstants.BST_DIRECT_REFERENCE);
        setRequiredFields();

        signatureEntry.process(secHeader, doc, contextMock);

        assertNotNull(xpath.evaluate("//wsse:BinarySecurityToken", doc, XPathConstants.NODE));
        assertNotNull(xpath.evaluate("//ds:Signature", doc, XPathConstants.NODE));

        validateSignature();
    }

    @Test
    @Ignore("Failing every time")
    public void testProcessSignedBinarySecurityToken() throws Exception {
        signatureEntry.setKeyIdentifierType(WSConstants.BST_DIRECT_REFERENCE);
        setRequiredFields();

        StringToStringMap entry = new StringToStringMap();
        entry.put("name", WSConstants.BINARY_TOKEN_LN);
        entry.put("namespace", WSConstants.WSSE_NS);
        signatureEntry.setParts(Collections.singletonList(entry));

        signatureEntry.process(secHeader, doc, contextMock);

        assertNotNull(xpath.evaluate("//wsse:BinarySecurityToken", doc, XPathConstants.NODE));
        assertNotNull(xpath.evaluate("//ds:Signature", doc, XPathConstants.NODE));
        Element reference = (Element) xpath.evaluate("//ds:Reference", doc, XPathConstants.NODE);
        assertTrue(reference.getAttribute("URI").startsWith("#X509-"));

        validateSignature();
    }

    @Test
    public void testProcessCustomToken() throws Exception {
        signatureEntry.setKeyIdentifierType(WSConstants.CUSTOM_KEY_IDENTIFIER);
        signatureEntry.setCustomTokenId("Assertion-01");
        signatureEntry.setCustomTokenValueType(WSConstants.WSS_SAML_KI_VALUE_TYPE);
        setRequiredFields();

        // this is the only test which uses another SOAP envelope with prepared SAML assertion.
        doc = XmlUtils.parseXml(TestUtils.SAMPLE_SOAP_MESSAGE_CUSTOM_TOKEN);
        secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);

        signatureEntry.process(secHeader, doc, contextMock);
        System.out.println(TestUtils.SAMPLE_SOAP_MESSAGE_CUSTOM_TOKEN);
        System.out.println(XmlUtils.serialize(doc));
        assertNotNull(xpath.evaluate("//ds:Signature", doc, XPathConstants.NODE));
        validateSignature();
    }

    private void validateSignature() {
        try {
            new WSSecurityEngine().processSecurityHeader(doc, null, null, crypto);
        } catch (WSSecurityException e) {
            throw new AssertionError("Bad signature", e);
        }
    }

    private void setRequiredFields() {
        signatureEntry.setSignatureAlgorithm(XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA1);
        signatureEntry.setSignatureCanonicalization(WSConstants.C14N_EXCL_OMIT_COMMENTS);
        signatureEntry.setDigestAlgorithm(MessageDigestAlgorithm.ALGO_ID_DIGEST_SHA1);
        signatureEntry.setUseSingleCert(true);
    }


    private void initXpath() {
        XPathFactory factory = XPathFactory.newInstance();
        NamespaceContextImpl namespaceContext = new NamespaceContextImpl();
        namespaceContext.startPrefixMapping("wsse", WSConstants.WSSE_NS);
        namespaceContext.startPrefixMapping("ds", WSConstants.SIG_NS);
        xpath = factory.newXPath();
        xpath.setNamespaceContext(namespaceContext);
    }

    private void createCrypto() throws KeyStoreException, CredentialException, IOException, NoSuchAlgorithmException,
            CertificateException {
        WSSConfig.init();
        crypto = new Merlin();
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        ClassLoader loader = Loader.getClassLoader(SignatureEntryTest.class);
        InputStream input = Merlin.loadInputStream(loader, KEYSTORE_PATH);
        keyStore.load(input, KEYSTORE_PASSWORD.toCharArray());
        crypto.setKeyStore(keyStore);
    }

}
