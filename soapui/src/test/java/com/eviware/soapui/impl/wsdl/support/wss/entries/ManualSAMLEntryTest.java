/*
 * Copyright 2004-2014 SmartBear Software
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

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import java.io.IOException;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import junit.framework.JUnit4TestAdapter;

import org.apache.ws.commons.util.NamespaceContextImpl;
import org.apache.ws.security.message.WSSecHeader;
import org.apache.ws.security.saml.ext.builder.SAML1Constants;
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
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContext;
import com.eviware.soapui.support.xml.XmlUtils;
import com.eviware.soapui.utils.TestUtils;

/**
 * @author Erik R. Yverling
 * 
 */
public class ManualSAMLEntryTest
{

	public static junit.framework.Test suite()
	{
		return new JUnit4TestAdapter( ManualSAMLEntryTest.class );
	}

	@Mock
	private WSSEntryConfig wssEntryConfigMock;
	@Mock
	private OutgoingWss outgoingWssMock;
	@Mock
	private PropertyExpansionContext contextMock;
	@Mock
	private XmlObject xmlObjectMock;

	private ManualSAMLEntry manualSAMLentry;
	private Document doc;
	private WSSecHeader secHeader;
	private XPathFactory factory;
	private NamespaceContextImpl namespaceContext;
	private XPath xpath;

	@Before
	public void setUp() throws Exception
	{
		MockitoAnnotations.initMocks( this );

		initXpath();

		doc = XmlUtils.parseXml( TestUtils.SAMPLE_SOAP_MESSAGE );

		secHeader = new WSSecHeader();
		secHeader.insertSecurityHeader( doc );

		manualSAMLentry = new ManualSAMLEntry();

		manualSAMLentry.init( wssEntryConfigMock, outgoingWssMock );

		when( wssEntryConfigMock.getConfiguration() ).thenReturn( xmlObjectMock );
		when( contextMock.expand( TestUtils.SAMPLE_SAML_1_ASSERTION ) ).thenReturn( TestUtils.SAMPLE_SAML_1_ASSERTION );
	}

	@Test
	public void testProcess() throws XPathExpressionException, IOException
	{
		manualSAMLentry.setSamlAssertion( TestUtils.SAMPLE_SAML_1_ASSERTION );
		manualSAMLentry.process( secHeader, doc, contextMock );

		assertNotNull( xpath.evaluate( "//saml1:Assertion", doc, XPathConstants.NODE ) );
        assertEquals("1", xpath.evaluate("//saml1:Assertion/@MajorVersion", doc, XPathConstants.STRING));
        assertEquals("1", xpath.evaluate("//saml1:Assertion/@MinorVersion", doc, XPathConstants.STRING));
    }

	private void initXpath()
	{
		factory = XPathFactory.newInstance();
		namespaceContext = new NamespaceContextImpl();
		namespaceContext.startPrefixMapping( "saml1", "urn:oasis:names:tc:SAML:1.0:assertion" );
		xpath = factory.newXPath();
		xpath.setNamespaceContext( namespaceContext );
	}
}
