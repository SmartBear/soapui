/*
 *  SoapUI, copyright (C) 2004-2012 smartbear.com
 *
 *  SoapUI is free software; you can redistribute it and/or modify it under the
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  SoapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
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
import junit.framework.JUnit4TestAdapter;
import org.apache.ws.commons.util.NamespaceContextImpl;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSSConfig;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.components.crypto.CredentialException;
import org.apache.ws.security.components.crypto.Merlin;
import org.apache.ws.security.message.WSSecHeader;
import org.apache.ws.security.util.Loader;
import org.apache.xml.security.algorithms.MessageDigestAlgorithm;
import org.apache.xml.security.signature.XMLSignature;
import org.apache.xmlbeans.XmlObject;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
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

/**
 * @author Erik R. Yverling
 */

public class SignatureEntryTest {
    public static junit.framework.Test suite() {
        return new JUnit4TestAdapter(SignatureEntryTest.class);
    }

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
    }

    @Test
    public void testProcessBinarySecurityToken() throws WSSecurityException, XPathExpressionException {
        setRequiredFields();

        signatureEntry.process(secHeader, doc, contextMock);

        assertNotNull(xpath.evaluate("//wsse:BinarySecurityToken", doc, XPathConstants.NODE));
        assertNotNull(xpath.evaluate("//ds:Signature", doc, XPathConstants.NODE));
    }

    @Test
    public void testProcessSignedBinarySecurityToken() throws WSSecurityException, XPathExpressionException {
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
    }


    private void setRequiredFields() {
        signatureEntry.setKeyIdentifierType(WSConstants.BST_DIRECT_REFERENCE);
        signatureEntry.setSignatureAlgorithm(XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA1);
        signatureEntry.setSignatureCanonicalization(WSConstants.C14N_EXCL_OMIT_COMMENTS);
        signatureEntry.setDigestAlgorithm(MessageDigestAlgorithm.ALGO_ID_DIGEST_SHA1);
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