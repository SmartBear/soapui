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

package com.eviware.soapui.support;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import javax.xml.namespace.QName;

import junit.framework.JUnit4TestAdapter;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.eviware.soapui.support.xml.XmlUtils;

public class XmlUtilsTestCase
{
	public static junit.framework.Test suite()
	{
		return new JUnit4TestAdapter( XmlUtilsTestCase.class );
	}

	@Test
	public void testGetElementIndex() throws Exception
	{
		Document dom = XmlUtils.parseXml( "<h1><p>p1</p><h2>lkj</h2><p>p2</p></h1>" );
		NodeList nl = dom.getDocumentElement().getElementsByTagName( "p" );

		assertEquals( 1, XmlUtils.getElementIndex( ( Element )nl.item( 0 ) ) );
		assertEquals( 2, XmlUtils.getElementIndex( ( Element )nl.item( 1 ) ) );
	}

	public void testGetElementPath() throws Exception
	{
		Document dom = XmlUtils.parseXml( "<h1><p>p1</p><h2>lkj</h2><p>p2</p></h1>" );
		NodeList nl = dom.getDocumentElement().getElementsByTagName( "p" );

		assertEquals( "/h1[1]/p[1]", XmlUtils.getElementPath( ( Element )nl.item( 0 ) ) );
		assertEquals( "/h1[1]/p[2]", XmlUtils.getElementPath( ( Element )nl.item( 1 ) ) );
	}

	@Test
	public void testTransferValues() throws Exception
	{
		String doc1 = "<h1><p>p1</p><h2 test=\"bil\">lkj</h2></h1>";
		String doc2 = "<h1><p>string</p><h2>string</h2><p>p2</p></h1>";

		String result = XmlUtils.transferValues( doc1, doc2 );
		assertEquals( "<h1><p>p1</p><h2 test=\"bil\">lkj</h2><p>p2</p></h1>", result );
	}

	@Test
	public void testTransferValuesWithList() throws Exception
	{
		String doc1 = "<h1><p>p1</p><p>p2</p><h2 test=\"bil\">lkj</h2></h1>";
		String doc2 = "<h1><p>string</p><h2>string</h2><p>p2</p></h1>";

		String result = XmlUtils.transferValues( doc1, doc2 );
		assertEquals( "<h1><p>p1</p><p>p2</p><h2 test=\"bil\">lkj</h2><p>p2</p></h1>", result );
	}

	@Test
	public void testTransferValuesNS() throws Exception
	{
		String doc1 = "<ns:h1 xmlns:ns=\"test\"><ns:p>p1</ns:p><ns:h2 test=\"bil\">lkj</ns:h2></ns:h1>";
		String doc2 = "<ns:h1 xmlns:ns=\"test\"><ns:p>string</ns:p><ns:h2>string</ns:h2><ns:p>p2</ns:p></ns:h1>";

		String result = XmlUtils.transferValues( doc1, doc2 );
		assertEquals( "<ns:h1 xmlns:ns=\"test\"><ns:p>p1</ns:p><ns:h2 test=\"bil\">lkj</ns:h2><ns:p>p2</ns:p></ns:h1>",
				result );
	}

	@Test
	public void testCreateXPath() throws Exception
	{
		String str = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" "
				+ "xmlns:ord=\"http://www.example.org/OrderService/\">"
				+ "<soapenv:Header/><soapenv:Body><ord:purchaseOrder><productId>?</productId>"
				+ "</ord:purchaseOrder></soapenv:Body></soapenv:Envelope>";

		// XmlObject xml = XmlObject.Factory.parse( str );
		XmlObject xml = XmlUtils.createXmlObject( str );
		XmlObject xmlobj = xml.selectPath( "//productId" )[0];
		String xpath = XmlUtils.createXPath( xmlobj.getDomNode() );
		assertEquals( xmlobj, xml.selectPath( xpath )[0] );

		System.out.println( "before removal: " + xpath );
		xpath = XmlUtils.removeXPathNamespaceDeclarations( xpath );
		System.out.println( "after removal:" + xpath );

		String ns = XmlUtils.declareXPathNamespaces( xml );
		System.out.println( "extracted namespaces:" + ns );

		assertEquals( xmlobj, xml.selectPath( ns + xpath )[0] );
	}

	@Test
	public void testCreateXPath2() throws Exception
	{
		String str = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" "
				+ "xmlns:ord=\"http://www.example.org/OrderService/\">"
				+ "<soapenv:Header/><soapenv:Body><purchaseOrder xmlns=\"http://test\"><productId>?</productId>"
				+ "</purchaseOrder></soapenv:Body></soapenv:Envelope>";

		// XmlObject xml = XmlObject.Factory.parse( str );
		XmlObject xml = XmlUtils.createXmlObject( str );
		XmlObject xmlobj = xml.selectPath( "declare namespace ns='http://test';//ns:productId" )[0];
		String xpath = XmlUtils.createXPath( xmlobj.getDomNode() );
		System.out.println( "created path: " + xpath );
		assertEquals( xmlobj, xml.selectPath( xpath )[0] );
	}

	@Test
	public void testGetFirstChildElementNS() throws XmlException
	{
		String xml = "<SofEnvelope><partnerid>test</partnerid><sessionID>asdadasdasd</sessionID></SofEnvelope>";
		// XmlObject xmlObject = XmlObject.Factory.parse( xml );
		XmlObject xmlObject = XmlUtils.createXmlObject( xml );

		Element documentElement = ( ( Document )xmlObject.getDomNode() ).getDocumentElement();
		assertNotNull( XmlUtils.getFirstChildElementNS( documentElement, new QName( "", "partnerid" ) ) );
		assertNotNull( XmlUtils.getFirstChildElementNS( documentElement, new QName( null, "partnerid" ) ) );

		assertNotNull( XmlUtils.getFirstChildElementNS( documentElement, "", "partnerid" ) );
		assertNotNull( XmlUtils.getFirstChildElementNS( documentElement, null, "partnerid" ) );
	}
}
