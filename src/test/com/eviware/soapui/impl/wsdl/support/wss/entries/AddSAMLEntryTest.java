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

import junit.framework.JUnit4TestAdapter;

import org.apache.ws.security.WSSConfig;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.components.crypto.CredentialException;
import org.apache.ws.security.components.crypto.Merlin;
import org.apache.ws.security.message.WSSecHeader;
import org.apache.ws.security.util.Loader;
import org.apache.xmlbeans.XmlObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
	public void setUp() throws Exception
	{
		MockitoAnnotations.initMocks( this );

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

	// FIXME The keystore can't be found by Maven... why is that. Do it have to be in the com.eviware folder?
	@Test
	public void testProcessSignedSAML1AuthenticationAssertionUsingHolderOfKey() throws WSSecurityException
	{

		addSamlEntry.setSamlVersion( AddSAMLEntry.DEFAULT_SAML_VERSION );

		addSamlEntry.process( secHeader, doc, contextMock );

		Node assertionNode = getAndAssertNodeByTagName( "saml1:Assertion" );

		Node issuerNode = assertionNode.getAttributes().getNamedItem( "Issuer" );
		assertEquals( issuerNode.getNodeValue(), ISSUER );

		Node subjectNode = getAndAssertNodeByTagName( "saml1:NameIdentifier" );
		assertEquals( getFirstChildValue( subjectNode ), SUBJECT_NAME );

		Node nameQualifierNode = subjectNode.getAttributes().getNamedItem( "NameQualifier" );
		assertEquals( nameQualifierNode.getNodeValue(), SUBJECT_QUALIFIER );
	}

	@Test
	public void testProcessSignedSAML2AuthenticationAssertionUsingHolderOfKey() throws WSSecurityException
	{
		addSamlEntry.setSamlVersion( AddSAMLEntry.SAML_VERSION_2 );

		addSamlEntry.process( secHeader, doc, contextMock );

		getAndAssertNodeByTagName( "saml2:Assertion" );

		Node issuerNode = getAndAssertNodeByTagName( "saml2:Issuer" );
		assertEquals( getFirstChildValue( issuerNode ), ISSUER );

		Node subjectNode = getAndAssertNodeByTagName( "saml2:NameID" );
		assertEquals( getFirstChildValue( subjectNode ), SUBJECT_NAME );

		Node nameQualifierNode = subjectNode.getAttributes().getNamedItem( "NameQualifier" );
		assertEquals( nameQualifierNode.getNodeValue(), SUBJECT_QUALIFIER );
	}

	// FIXME Could we make the finding of nodes simpler? XPath?
	private Node getAndAssertNodeByTagName( String tagName )
	{
		NodeList nodeList = doc.getElementsByTagName( tagName );
		assertNotNull( nodeList );
		assertEquals( nodeList.getLength(), 1 );
		Node node = nodeList.item( 0 );
		assertNotNull( node );
		return node;
	}

	private String getFirstChildValue( Node node )
	{
		return node.getFirstChild().getNodeValue();
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