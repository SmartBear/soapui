/*
 *  soapUI, copyright (C) 2004-2011 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.wsdl.support.wss.entries;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import junit.framework.JUnit4TestAdapter;

import org.apache.ws.commons.util.NamespaceContextImpl;
import org.apache.ws.security.WSSConfig;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.components.crypto.CredentialException;
import org.apache.ws.security.components.crypto.Merlin;
import org.apache.ws.security.message.WSSecHeader;
import org.apache.ws.security.util.Loader;
import org.apache.xmlbeans.XmlObject;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.tidy.Attribute;

import com.eviware.soapui.config.WSSEntryConfig;
import com.eviware.soapui.impl.wsdl.support.wss.OutgoingWss;
import com.eviware.soapui.impl.wsdl.support.wss.WssContainer;
import com.eviware.soapui.impl.wsdl.support.wss.WssCrypto;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContext;
import com.eviware.soapui.support.xml.XmlUtils;

/**
 * @author Erik R. Yverling
 */
public class AddSAMLEntryTest
{
	// TODO Can these be found in the wss4j lib instead?
	private static final String SAML_1_HOLDER_OF_KEY_NAMESPACE = "urn:oasis:names:tc:SAML:1.0:cm:holder-of-key";
	private static final String SAML_2_HOLDER_OF_KEY_NAMESPACE = "urn:oasis:names:tc:SAML:2.0:cm:holder-of-key";
	private static final String SAML_1_SENDER_VOUCHES_NAMESPACE = "urn:oasis:names:tc:SAML:1.0:cm:sender-vouches";
	private static final String SAML_2_SENDER_VOUCHES_NAMESPACE = "urn:oasis:names:tc:SAML:2.0:cm:sender-vouches";

	public static junit.framework.Test suite()
	{
		return new JUnit4TestAdapter( AddSAMLEntryTest.class );
	}

	private static final String ISSUER = "www.issuer.com";
	private static final String KEYSTORE_PATH = "keys/keystore.jks";
	private static final String KEYSTORE_PASSWORD = "foobar42";
	private static final String KEY_PASSWORD = "foobar42";
	private static final String ALIAS = "certificatekey";
	private static final String SUBJECT_QUALIFIER = "www.subject.com";
	private static final String SUBJECT_NAME = "uid=joe,ou=people,ou=saml-demo,o=example.com";

	private static final String SAMPLE_SOAP_MESSAGE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ws=\"http://ws.app4test.eviware.com/\">"
			+ "<soapenv:Header/>" + "<soapenv:Body>" + "<ws:getItems/>" + "</soapenv:Body>" + "</soapenv:Envelope>";

	private AddSAMLEntry addSamlEntry;

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
	public void setUp() throws Exception
	{
		MockitoAnnotations.initMocks( this );

		initXpath();

		doc = XmlUtils.parseXml( SAMPLE_SOAP_MESSAGE );

		secHeader = new WSSecHeader();
		secHeader.insertSecurityHeader( doc );

		addSamlEntry = new AddSAMLEntry();
		addSamlEntry.init( wssEntryConfigMock, outgoingWssMock );

		when( wssEntryConfigMock.getConfiguration() ).thenReturn( xmlObjectMock );

		addSamlEntry.setIssuer( ISSUER );
		addSamlEntry.setSubjectQualifier( SUBJECT_QUALIFIER );
		addSamlEntry.setSubjectName( SUBJECT_NAME );

		createCrypto();

		when( outgoingWssMock.getWssContainer() ).thenReturn( wssContainerMock );
		when( wssContainerMock.getCryptoByName( anyString() ) ).thenReturn( wssCryptoMock );
		when( wssCryptoMock.getCrypto() ).thenReturn( crypto );
		when( outgoingWssMock.getUsername() ).thenReturn( ALIAS );
		when( outgoingWssMock.getPassword() ).thenReturn( KEY_PASSWORD );
		when( contextMock.expand( ALIAS ) ).thenReturn( ALIAS );
		when( contextMock.expand( KEY_PASSWORD ) ).thenReturn( KEY_PASSWORD );
	}

	@Test
	public void testProcessSignedSAML1AuthenticationAssertionUsingHolderOfKey() throws WSSecurityException,
			XPathExpressionException
	{
		addSamlEntry.setSamlVersion( AddSAMLEntry.SAML_VERSION_1 );
		addSamlEntry.setAssertionType( AddSAMLEntry.AUTHENTICATION_ASSERTION_TYPE );
		addSamlEntry.setSigningType( AddSAMLEntry.HOLDER_OF_KEY_SIGNING_TYPE );
		addSamlEntry.process( secHeader, doc, contextMock );

		assertEquals( xpath.evaluate( "//saml1:ConfirmationMethod", doc, XPathConstants.STRING ),
				SAML_1_HOLDER_OF_KEY_NAMESPACE );
		assertNotNull( xpath.evaluate( "//saml1:AuthenticationStatement", doc, XPathConstants.NODE ) );
	}

	@Test
	public void testProcessSignedSAML2AuthenticationAssertionUsingHolderOfKey() throws WSSecurityException,
			XPathExpressionException
	{
		addSamlEntry.setSamlVersion( AddSAMLEntry.SAML_VERSION_2 );
		addSamlEntry.setAssertionType( AddSAMLEntry.AUTHENTICATION_ASSERTION_TYPE );
		addSamlEntry.setSigningType( AddSAMLEntry.HOLDER_OF_KEY_SIGNING_TYPE );

		addSamlEntry.process( secHeader, doc, contextMock );

		assertEquals( xpath.evaluate( "//saml2:SubjectConfirmation/@Method", doc, XPathConstants.STRING ),
				SAML_2_HOLDER_OF_KEY_NAMESPACE );
		assertNotNull( xpath.evaluate( "//saml2:AuthnStatement", doc, XPathConstants.NODE ) );
	}

	@Test
	public void testProcessSignedSAML1AttributeAssertionUsingHolderOfKey() throws WSSecurityException,
			XPathExpressionException
	{
		addSamlEntry.setSamlVersion( AddSAMLEntry.SAML_VERSION_1 );
		addSamlEntry.setAssertionType( AddSAMLEntry.ATTRIBUTE_ASSERTION_TYPE );
		addSamlEntry.setSigningType( AddSAMLEntry.HOLDER_OF_KEY_SIGNING_TYPE );

		addSamlEntry.process( secHeader, doc, contextMock );

		assertEquals( xpath.evaluate( "//saml1:ConfirmationMethod", doc, XPathConstants.STRING ),
				SAML_1_HOLDER_OF_KEY_NAMESPACE );
		assertNotNull( xpath.evaluate( "//saml1:AttributeStatement", doc, XPathConstants.NODE ) );
	}

	@Test
	public void testProcessSignedSAML2AttributeAssertionUsingHolderOfKey() throws WSSecurityException,
			XPathExpressionException
	{
		addSamlEntry.setSamlVersion( AddSAMLEntry.SAML_VERSION_2 );
		addSamlEntry.setAssertionType( AddSAMLEntry.ATTRIBUTE_ASSERTION_TYPE );
		addSamlEntry.setSigningType( AddSAMLEntry.HOLDER_OF_KEY_SIGNING_TYPE );

		addSamlEntry.process( secHeader, doc, contextMock );

		assertEquals( xpath.evaluate( "//saml2:SubjectConfirmation/@Method", doc, XPathConstants.STRING ),
				SAML_2_HOLDER_OF_KEY_NAMESPACE );
		assertNotNull( xpath.evaluate( "//saml2:AttributeStatement", doc, XPathConstants.NODE ) );
	}

	@Test
	public void testProcessSignedSAML1AuthenticationAssertionUsingSenderVouces() throws WSSecurityException,
			XPathExpressionException
	{
		addSamlEntry.setSamlVersion( AddSAMLEntry.SAML_VERSION_1 );
		addSamlEntry.setAssertionType( AddSAMLEntry.AUTHENTICATION_ASSERTION_TYPE );
		addSamlEntry.setSigningType( AddSAMLEntry.SENDER_VOUCHES_SIGNING_TYPE );

		addSamlEntry.process( secHeader, doc, contextMock );
		System.out.println( XmlUtils.serializePretty( doc ) );

		assertEquals( xpath.evaluate( "//saml1:ConfirmationMethod", doc, XPathConstants.STRING ),
				SAML_1_SENDER_VOUCHES_NAMESPACE );
		assertNotNull( xpath.evaluate( "//saml1:AuthenticationStatement", doc, XPathConstants.NODE ) );
	}

	@Test
	public void testProcessSignedSAML2AuthenticationAssertionUsingSenderVouches() throws WSSecurityException,
			XPathExpressionException
	{
		addSamlEntry.setSamlVersion( AddSAMLEntry.SAML_VERSION_2 );
		addSamlEntry.setAssertionType( AddSAMLEntry.AUTHENTICATION_ASSERTION_TYPE );
		addSamlEntry.setSigningType( AddSAMLEntry.SENDER_VOUCHES_SIGNING_TYPE );

		addSamlEntry.process( secHeader, doc, contextMock );

		assertEquals( xpath.evaluate( "//saml2:SubjectConfirmation/@Method", doc, XPathConstants.STRING ),
				SAML_2_SENDER_VOUCHES_NAMESPACE );
		assertNotNull( xpath.evaluate( "//saml2:AuthnStatement", doc, XPathConstants.NODE ) );

	}

	@Test
	public void testUserInputFieldsForSAML1() throws WSSecurityException, XPathExpressionException
	{
		addSamlEntry.setSamlVersion( AddSAMLEntry.SAML_VERSION_1 );
		addSamlEntry.process( secHeader, doc, contextMock );

		assertNotNull( xpath.evaluate( "//saml1:Assertion", doc, XPathConstants.NODE ) );
		assertNotNull( xpath.evaluate( "//saml1:Assertion/@Issuer", doc, XPathConstants.NODE ) );
		assertNotNull( xpath.evaluate( "//saml1:NameIdentifier", doc, XPathConstants.NODE ) );
		assertNotNull( xpath.evaluate( "//saml1:NameIdentifier/@NameQualifier", doc, XPathConstants.NODE ) );
	}

	@Test
	public void testUserInputFieldsForSAML2() throws WSSecurityException, XPathExpressionException
	{
		addSamlEntry.setSamlVersion( AddSAMLEntry.SAML_VERSION_2 );
		addSamlEntry.process( secHeader, doc, contextMock );

		assertNotNull( xpath.evaluate( "//saml2:Assertion", doc, XPathConstants.NODE ) );
		assertNotNull( xpath.evaluate( "//saml2:Issuer", doc, XPathConstants.NODE ) );
		assertNotNull( xpath.evaluate( "//saml2:NameID", doc, XPathConstants.NODE ) );
		assertNotNull( xpath.evaluate( "//saml2:NameID/@NameQualifier", doc, XPathConstants.NODE ) );
	}

	@Test
	@Ignore
	public void testDefaultValues()
	{
		// TODO Test the default values of the input fields
	}

	private void initXpath()
	{
		factory = XPathFactory.newInstance();
		namespaceContext = new NamespaceContextImpl();
		namespaceContext.startPrefixMapping( "saml1", "urn:oasis:names:tc:SAML:1.0:assertion" );
		namespaceContext.startPrefixMapping( "saml2", "urn:oasis:names:tc:SAML:2.0:assertion" );
		xpath = factory.newXPath();
		xpath.setNamespaceContext( namespaceContext );
	}

	private void createCrypto() throws KeyStoreException, CredentialException, IOException, NoSuchAlgorithmException,
			CertificateException
	{
		WSSConfig.init();
		crypto = new Merlin();
		KeyStore keyStore = KeyStore.getInstance( KeyStore.getDefaultType() );
		ClassLoader loader = Loader.getClassLoader( AddSAMLEntryTest.class );
		InputStream input = Merlin.loadInputStream( loader, KEYSTORE_PATH );
		keyStore.load( input, KEYSTORE_PASSWORD.toCharArray() );
		( ( Merlin )crypto ).setKeyStore( keyStore );
	}
}