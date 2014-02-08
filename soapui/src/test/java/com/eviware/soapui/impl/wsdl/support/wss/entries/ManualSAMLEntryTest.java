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
