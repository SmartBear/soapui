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
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.components.crypto.CredentialException;
import org.apache.ws.security.components.crypto.Merlin;
import org.apache.ws.security.message.WSSecHeader;
import org.apache.ws.security.saml.ext.builder.SAML1Constants;
import org.apache.ws.security.saml.ext.builder.SAML2Constants;
import org.apache.ws.security.util.Loader;
import org.apache.xml.security.algorithms.MessageDigestAlgorithm;
import org.apache.xmlbeans.XmlObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.w3c.dom.Document;

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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

/**
 * @author Erik R. Yverling
 */

//FIXME Use the *-test keys instead of the keystore.jks
// TODO Add test for invalid SAML version
public class AutomaticSAMLEntryTest {
    private static final String ISSUER = "www.issuer.com";
    private static final String KEYSTORE_PATH = "keys/keystore.jks";
    private static final String KEYSTORE_PASSWORD = "foobar42";
    private static final String KEY_PASSWORD = "foobar42";
    private static final String ALIAS = "certificatekey";
    private static final String SUBJECT_QUALIFIER = "www.subject.com";
    private static final String SUBJECT_NAME = "uid=joe,ou=people,ou=saml-demo,o=example.com";
    private static final String ATTTRIBUTE_NAME = "attibuteName";
    private static final String ATTTRIBUTE_VALUE = "attributeValue";

    private AutomaticSAMLEntry automaticSamlEntry;

    private Merlin crypto;
    private Document doc;
    private WSSecHeader secHeader;
    private XPathFactory factory;
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
    private NamespaceContextImpl namespaceContext;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        initXpath();

        doc = XmlUtils.parseXml(TestUtils.SAMPLE_SOAP_MESSAGE);

        secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);

        automaticSamlEntry = new AutomaticSAMLEntry();
        automaticSamlEntry.init(wssEntryConfigMock, outgoingWssMock);

        when(wssEntryConfigMock.getConfiguration()).thenReturn(xmlObjectMock);

        automaticSamlEntry.setIssuer(ISSUER);
        automaticSamlEntry.setSubjectQualifier(SUBJECT_QUALIFIER);
        automaticSamlEntry.setSubjectName(SUBJECT_NAME);
        automaticSamlEntry.setSignatureAlgorithm(WSConstants.RSA);
        automaticSamlEntry.setDigestAlgorithm(MessageDigestAlgorithm.ALGO_ID_DIGEST_SHA1);

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
        when(contextMock.expand(SUBJECT_NAME)).thenReturn(SUBJECT_NAME);
        when(contextMock.expand(SUBJECT_QUALIFIER)).thenReturn(SUBJECT_QUALIFIER);
        when(contextMock.expand(ATTTRIBUTE_NAME)).thenReturn(ATTTRIBUTE_NAME);
    }

    @Test
    public void testProcessUnsignedSAML1AuthenticationAssertionUsingSenderVouches() throws WSSecurityException,
            XPathExpressionException {
        setRequiredFields(AutomaticSAMLEntry.SAML_VERSION_1, false, AutomaticSAMLEntry.AUTHENTICATION_ASSERTION_TYPE,
                AutomaticSAMLEntry.SENDER_VOUCHES_CONFIRMATION_METHOD);

        automaticSamlEntry.process(secHeader, doc, contextMock);

        assertEquals(xpath.evaluate("//saml1:ConfirmationMethod", doc, XPathConstants.STRING),
                SAML1Constants.CONF_SENDER_VOUCHES);
        assertNotNull(xpath.evaluate("//saml1:AuthenticationStatement", doc, XPathConstants.NODE));
    }

    @Test
    public void testProcessUnsignedSAML2AuthenticationAssertionUsingSenderVouches() throws WSSecurityException,
            XPathExpressionException {
        setRequiredFields(AutomaticSAMLEntry.SAML_VERSION_2, false, AutomaticSAMLEntry.AUTHENTICATION_ASSERTION_TYPE,
                AutomaticSAMLEntry.SENDER_VOUCHES_CONFIRMATION_METHOD);

        automaticSamlEntry.process(secHeader, doc, contextMock);

        assertEquals(xpath.evaluate("//saml2:SubjectConfirmation/@Method", doc, XPathConstants.STRING),
                SAML2Constants.CONF_SENDER_VOUCHES);
        assertNotNull(xpath.evaluate("//saml2:AuthnStatement", doc, XPathConstants.NODE));
    }

    @Test
    public void testProcessUnsignedSAML1AttributeAssertionUsingSenderVouches() throws WSSecurityException,
            XPathExpressionException {
        setRequiredFields(AutomaticSAMLEntry.SAML_VERSION_1, false, AutomaticSAMLEntry.ATTRIBUTE_ASSERTION_TYPE,
                AutomaticSAMLEntry.SENDER_VOUCHES_CONFIRMATION_METHOD);

        createAttribute();

        automaticSamlEntry.process(secHeader, doc, contextMock);

        System.out.println(XmlUtils.serializePretty(doc));

        assertEquals(xpath.evaluate("//saml1:ConfirmationMethod", doc, XPathConstants.STRING),
                SAML1Constants.CONF_SENDER_VOUCHES);
        assertNotNull(xpath.evaluate("//saml1:AttributeStatement", doc, XPathConstants.NODE));
        assertEquals(
                xpath.evaluate("//saml1:AttributeStatement/saml1:Attribute/@AttributeName", doc, XPathConstants.STRING),
                ATTTRIBUTE_NAME);
        assertEquals(xpath.evaluate("//saml1:AttributeStatement/saml1:Attribute/saml1:AttributeValue", doc,
                XPathConstants.STRING), ATTTRIBUTE_VALUE);
    }

    @Test
    public void testProcessUnsignedSAML2AttributeAssertionUsingSenderVouches() throws WSSecurityException,
            XPathExpressionException {
        setRequiredFields(AutomaticSAMLEntry.SAML_VERSION_2, false, AutomaticSAMLEntry.ATTRIBUTE_ASSERTION_TYPE,
                AutomaticSAMLEntry.SENDER_VOUCHES_CONFIRMATION_METHOD);

        createAttribute();

        automaticSamlEntry.process(secHeader, doc, contextMock);

        System.out.println(XmlUtils.serializePretty(doc));

        assertEquals(xpath.evaluate("//saml2:SubjectConfirmation/@Method", doc, XPathConstants.STRING),
                SAML2Constants.CONF_SENDER_VOUCHES);
        assertNotNull(xpath.evaluate("//saml2:AttributeStatement", doc, XPathConstants.NODE));
        assertEquals(
                xpath.evaluate("//saml2:AttributeStatement/saml2:Attribute/@FriendlyName", doc, XPathConstants.STRING),
                ATTTRIBUTE_NAME);
        assertEquals(xpath.evaluate("//saml2:AttributeStatement/saml2:Attribute/saml2:AttributeValue", doc,
                XPathConstants.STRING), ATTTRIBUTE_VALUE);
    }

    @Test
    public void testProcessUnsignedSAML1AuthorizationAssertionUsingSenderVouches() throws WSSecurityException,
            XPathExpressionException {
        setRequiredFields(AutomaticSAMLEntry.SAML_VERSION_1, false, AutomaticSAMLEntry.AUTHORIZATION_ASSERTION_TYPE,
                AutomaticSAMLEntry.SENDER_VOUCHES_CONFIRMATION_METHOD);

        automaticSamlEntry.process(secHeader, doc, contextMock);

        assertEquals(xpath.evaluate("//saml1:ConfirmationMethod", doc, XPathConstants.STRING),
                SAML1Constants.CONF_SENDER_VOUCHES);
        assertNotNull(xpath.evaluate("//saml1:AuthorizationDecisionStatement", doc, XPathConstants.NODE));
    }

    @Test
    public void testProcessUnsignedSAML2AuthorizationAssertionUsingSenderVouches() throws WSSecurityException,
            XPathExpressionException {
        setRequiredFields(AutomaticSAMLEntry.SAML_VERSION_2, false, AutomaticSAMLEntry.AUTHORIZATION_ASSERTION_TYPE,
                AutomaticSAMLEntry.SENDER_VOUCHES_CONFIRMATION_METHOD);

        automaticSamlEntry.process(secHeader, doc, contextMock);

        assertEquals(xpath.evaluate("//saml2:SubjectConfirmation/@Method", doc, XPathConstants.STRING),
                SAML2Constants.CONF_SENDER_VOUCHES);
        assertNotNull(xpath.evaluate("//saml2:AuthzDecisionStatement", doc, XPathConstants.NODE));
    }

    @Test
    public void testProcessSignedSAML1AuthenticationAssertionUsingHolderOfKey() throws WSSecurityException,
            XPathExpressionException {
        setRequiredFields(AutomaticSAMLEntry.SAML_VERSION_1, true, AutomaticSAMLEntry.AUTHENTICATION_ASSERTION_TYPE,
                AutomaticSAMLEntry.HOLDER_OF_KEY_CONFIRMATION_METHOD);

        automaticSamlEntry.process(secHeader, doc, contextMock);

        System.err.println(XmlUtils.serializePretty(doc));

        assertNotNull(xpath.evaluate("//ds:Signature", doc, XPathConstants.NODE));
        assertEquals(xpath.evaluate("//saml1:ConfirmationMethod", doc, XPathConstants.STRING),
                SAML1Constants.CONF_HOLDER_KEY);
        assertNotNull(xpath.evaluate("//saml1:AuthenticationStatement", doc, XPathConstants.NODE));

    }

    @Test
    public void testProcessSignedSAML2AuthenticationAssertionUsingHolderOfKey() throws WSSecurityException,
            XPathExpressionException {
        setRequiredFields(AutomaticSAMLEntry.SAML_VERSION_2, true, AutomaticSAMLEntry.AUTHENTICATION_ASSERTION_TYPE,
                AutomaticSAMLEntry.HOLDER_OF_KEY_CONFIRMATION_METHOD);

        automaticSamlEntry.process(secHeader, doc, contextMock);

        assertNotNull(xpath.evaluate("//ds:Signature", doc, XPathConstants.NODE));
        assertEquals(xpath.evaluate("//saml2:SubjectConfirmation/@Method", doc, XPathConstants.STRING),
                SAML2Constants.CONF_HOLDER_KEY);
        assertNotNull(xpath.evaluate("//saml2:AuthnStatement", doc, XPathConstants.NODE));
    }

    @Test
    public void testProcessSignedSAML1AttributeAssertionUsingHolderOfKey() throws WSSecurityException,
            XPathExpressionException {
        setRequiredFields(AutomaticSAMLEntry.SAML_VERSION_1, true, AutomaticSAMLEntry.ATTRIBUTE_ASSERTION_TYPE,
                AutomaticSAMLEntry.HOLDER_OF_KEY_CONFIRMATION_METHOD);

        createAttribute();

        automaticSamlEntry.process(secHeader, doc, contextMock);

        assertNotNull(xpath.evaluate("//ds:Signature", doc, XPathConstants.NODE));
        assertEquals(xpath.evaluate("//saml1:ConfirmationMethod", doc, XPathConstants.STRING),
                SAML1Constants.CONF_HOLDER_KEY);
        assertNotNull(xpath.evaluate("//saml1:AttributeStatement", doc, XPathConstants.NODE));
        assertEquals(
                xpath.evaluate("//saml1:AttributeStatement/saml1:Attribute/@AttributeName", doc, XPathConstants.STRING),
                ATTTRIBUTE_NAME);
        assertEquals(xpath.evaluate("//saml1:AttributeStatement/saml1:Attribute/saml1:AttributeValue", doc,
                XPathConstants.STRING), ATTTRIBUTE_VALUE);
    }

    @Test
    public void testProcessSignedSAML2AttributeAssertionUsingHolderOfKey() throws WSSecurityException,
            XPathExpressionException {
        setRequiredFields(AutomaticSAMLEntry.SAML_VERSION_2, true, AutomaticSAMLEntry.ATTRIBUTE_ASSERTION_TYPE,
                AutomaticSAMLEntry.HOLDER_OF_KEY_CONFIRMATION_METHOD);

        createAttribute();

        automaticSamlEntry.process(secHeader, doc, contextMock);

        assertNotNull(xpath.evaluate("//ds:Signature", doc, XPathConstants.NODE));
        assertEquals(xpath.evaluate("//saml2:SubjectConfirmation/@Method", doc, XPathConstants.STRING),
                SAML2Constants.CONF_HOLDER_KEY);
        assertNotNull(xpath.evaluate("//saml2:AttributeStatement", doc, XPathConstants.NODE));
        assertEquals(
                xpath.evaluate("//saml2:AttributeStatement/saml2:Attribute/@FriendlyName", doc, XPathConstants.STRING),
                ATTTRIBUTE_NAME);
        assertEquals(xpath.evaluate("//saml2:AttributeStatement/saml2:Attribute/saml2:AttributeValue", doc,
                XPathConstants.STRING), ATTTRIBUTE_VALUE);
    }

    @Test
    public void testProcessSignedSAML1AuthenticationAssertionUsingSenderVouces() throws WSSecurityException,
            XPathExpressionException {
        setRequiredFields(AutomaticSAMLEntry.SAML_VERSION_1, true, AutomaticSAMLEntry.AUTHENTICATION_ASSERTION_TYPE,
                AutomaticSAMLEntry.SENDER_VOUCHES_CONFIRMATION_METHOD);

        automaticSamlEntry.process(secHeader, doc, contextMock);

        assertNotNull(xpath.evaluate("//ds:Signature", doc, XPathConstants.NODE));
        assertEquals(xpath.evaluate("//saml1:ConfirmationMethod", doc, XPathConstants.STRING),
                SAML1Constants.CONF_SENDER_VOUCHES);
        assertNotNull(xpath.evaluate("//saml1:AuthenticationStatement", doc, XPathConstants.NODE));
    }

    @Test
    public void testProcessSignedSAML2AuthenticationAssertionUsingSenderVouches() throws WSSecurityException,
            XPathExpressionException {
        setRequiredFields(AutomaticSAMLEntry.SAML_VERSION_2, true, AutomaticSAMLEntry.AUTHENTICATION_ASSERTION_TYPE,
                AutomaticSAMLEntry.SENDER_VOUCHES_CONFIRMATION_METHOD);

        automaticSamlEntry.process(secHeader, doc, contextMock);

        assertNotNull(xpath.evaluate("//ds:Signature", doc, XPathConstants.NODE));
        assertEquals(xpath.evaluate("//saml2:SubjectConfirmation/@Method", doc, XPathConstants.STRING),
                SAML2Constants.CONF_SENDER_VOUCHES);
        assertNotNull(xpath.evaluate("//saml2:AuthnStatement", doc, XPathConstants.NODE));

    }

    @Test
    public void testUserInputFieldsForSAML1() throws WSSecurityException, XPathExpressionException {
        automaticSamlEntry.setSamlVersion(AutomaticSAMLEntry.SAML_VERSION_1);
        automaticSamlEntry.setSigned(true);
        automaticSamlEntry.process(secHeader, doc, contextMock);

        assertNotNull(xpath.evaluate("//saml1:Assertion", doc, XPathConstants.NODE));
        assertNotNull(xpath.evaluate("//saml1:Assertion/@Issuer", doc, XPathConstants.NODE));
        assertNotNull(xpath.evaluate("//saml1:NameIdentifier", doc, XPathConstants.NODE));
        assertNotNull(xpath.evaluate("//saml1:NameIdentifier/@NameQualifier", doc, XPathConstants.NODE));
    }

    @Test
    public void testUserInputFieldsForSAML2() throws WSSecurityException, XPathExpressionException {
        automaticSamlEntry.setSamlVersion(AutomaticSAMLEntry.SAML_VERSION_2);
        automaticSamlEntry.setSigned(true);
        automaticSamlEntry.process(secHeader, doc, contextMock);

        assertNotNull(xpath.evaluate("//saml2:Assertion", doc, XPathConstants.NODE));
        assertNotNull(xpath.evaluate("//saml2:Issuer", doc, XPathConstants.NODE));
        assertNotNull(xpath.evaluate("//saml2:NameID", doc, XPathConstants.NODE));
        assertNotNull(xpath.evaluate("//saml2:NameID/@NameQualifier", doc, XPathConstants.NODE));
    }

    private void initXpath() {
        factory = XPathFactory.newInstance();
        namespaceContext = new NamespaceContextImpl();
        namespaceContext.startPrefixMapping("saml1", "urn:oasis:names:tc:SAML:1.0:assertion");
        namespaceContext.startPrefixMapping("saml2", "urn:oasis:names:tc:SAML:2.0:assertion");
        namespaceContext.startPrefixMapping("ds", "http://www.w3.org/2000/09/xmldsig#");
        xpath = factory.newXPath();
        xpath.setNamespaceContext(namespaceContext);
    }

    private void createCrypto() throws KeyStoreException, CredentialException, IOException, NoSuchAlgorithmException,
            CertificateException {
        WSSConfig.init();
        crypto = new Merlin();
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        ClassLoader loader = Loader.getClassLoader(AutomaticSAMLEntryTest.class);
        InputStream input = Merlin.loadInputStream(loader, KEYSTORE_PATH);
        keyStore.load(input, KEYSTORE_PASSWORD.toCharArray());
        ((Merlin) crypto).setKeyStore(keyStore);
    }

    private void setRequiredFields(String version, boolean signed, String assertionType, String confirmationMethod) {
        automaticSamlEntry.setSamlVersion(version);
        automaticSamlEntry.setSigned(signed);
        automaticSamlEntry.setAssertionType(assertionType);
        automaticSamlEntry.setConfirmationMethod(confirmationMethod);
    }

    private void createAttribute() {
        automaticSamlEntry.setAttributeName(ATTTRIBUTE_NAME);
        StringToStringMap attributeValueRow = new StringToStringMap();
        attributeValueRow.put(AutomaticSAMLEntry.ATTRIBUTE_VALUES_VALUE_COLUMN, ATTTRIBUTE_VALUE);
        automaticSamlEntry.setAttributeValues(Collections.singletonList(attributeValueRow));
    }

}
