/*
 *  soapUI, copyright (C) 2004-2008 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.wsdl.teststeps.assertions;

import com.eviware.soapui.config.TestAssertionConfig;
import com.eviware.soapui.impl.wsdl.WsdlSubmitContext;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.basic.XPathContainsAssertion;
import junit.framework.TestCase;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class XPathContainsAssertionTestCase extends TestCase
{
	private String testResponse;
	private XPathContainsAssertion assertion;
	private String testBody;

	protected void setUp() throws Exception
	{
		testResponse = readResource( "/testResponse.xml" );
		testBody = readResource( "/testBody.xml" );
		assertion = new XPathContainsAssertion( TestAssertionConfig.Factory.newInstance(), null );
	}

	public void testCreate() throws Exception
	{
		TestAssertionConfig config = createConfig( "testPath", "testContent" );

		XPathContainsAssertion assertion = new XPathContainsAssertion( config, null );

		assertEquals( "testPath", assertion.getPath() );
		assertEquals( "testContent", assertion.getExpectedContent() );

		XmlObject conf = assertion.createConfiguration();
		String str = conf.xmlText();
	}

	public void testFullContentMatch() throws Exception
	{
		assertion.setPath( "/" );
		assertion.setExpectedContent( testResponse );
		
		assertNotNull( assertion.assertContent( testResponse, new WsdlSubmitContext( null ), "" ));
	}
	
   public void testFullBodyMatch() throws Exception
	{
		assertion.setPath( "declare namespace urn='urn:schema:v1:companyservice:applications:bis.bonnier.se';" +
               "//urn:searchResponse" );
		
		assertion.setExpectedContent( testBody );
		
		assertNotNull( assertion.assertContent( testResponse, new WsdlSubmitContext( null ), "" ));
	}
   
   public void testAttributeMatch() throws Exception
	{
		assertion.setPath( "declare namespace env='http://schemas.xmlsoap.org/soap/envelope/';" +
				"declare namespace urn='urn:schema:v1:companyservice:applications:bis.bonnier.se';" +
				"declare namespace urn1='urn:v1:companysearch:common:bis.bonnier.se';" +
               "/env:Envelope/env:Body/urn:searchResponse/urn1:searchResult/@hitCount" );
		assertion.setExpectedContent( "131" );
		
		assertNotNull( assertion.assertContent( testResponse, new WsdlSubmitContext( null ), "" ));
	}
   
   public void testElementMatch() throws Exception
	{
		assertion.setPath( "declare namespace urn='urn:schema:v1:companyservice:applications:bis.bonnier.se';" +
				"declare namespace urn1='urn:v1:companysearch:common:bis.bonnier.se';" +
               "//urn:searchResponse/urn1:searchResult/company[2]/companyName" );
		assertion.setExpectedContent( "<companyName>Bonnier Otto Karl Adam</companyName>" );
		
		assertNotNull( assertion.assertContent( testResponse, new WsdlSubmitContext( null ), "" ));
	}

   public void testElementTextMatch() throws Exception
	{
		assertion.setPath( "declare namespace env='http://schemas.xmlsoap.org/soap/envelope/';" +
				"declare namespace urn='urn:schema:v1:companyservice:applications:bis.bonnier.se';" +
				"declare namespace urn1='urn:v1:companysearch:common:bis.bonnier.se';" +
               "/env:Envelope/env:Body/urn:searchResponse/urn1:searchResult/company[2]/companyName/text()" );
		assertion.setExpectedContent( "Bonnier Otto Karl Adam" );
		
		assertNotNull( assertion.assertContent( testResponse, new WsdlSubmitContext( null ), "" ));
	}
   
   public void testFragmentMatch() throws Exception
   {
		assertion.setPath( "declare namespace urn='urn:schema:v1:companyservice:applications:bis.bonnier.se';" +
				"declare namespace urn1='urn:v1:companysearch:common:bis.bonnier.se';" +
               "//urn:searchResponse/urn1:searchResult/company[4]" );
		assertion.setExpectedContent( readResource( "/testFragment.xml") );
		
		assertNotNull( assertion.assertContent( testResponse, new WsdlSubmitContext( null ), "" ));
   }
   
   public void testAnyFragmentMatch() throws Exception
   {
		assertion.setExpectedContent( readResource( "/testFragment.xml") );
		assertion.setPath( "//company" );

		assertNotNull( assertion.assertContent( testResponse, new WsdlSubmitContext( null ), "" ));
   }
   
   public void testLastElementTextMatch() throws Exception
	{
		assertion.setPath( "//company[last()]/companyName/text()" );
		assertion.setExpectedContent( "Bonnier Zoo Förlag AB" );
		
		assertNotNull( assertion.assertContent( testResponse, new WsdlSubmitContext( null ), "" ));
	}
   
   public void testElementCountMatch() throws Exception
	{
		assertion.setPath( "count(//company)" );
		assertion.setExpectedContent( "20" );
		
		assertNotNull( assertion.assertContent( testResponse, new WsdlSubmitContext( null ), "" ));
	}
   
   public void testAnyElementTextMatch() throws Exception
	{
		assertion.setPath( "declare namespace env='http://schemas.xmlsoap.org/soap/envelope/';" +
				"declare namespace urn='urn:schema:v1:companyservice:applications:bis.bonnier.se';" +
				"declare namespace urn1='urn:v1:companysearch:common:bis.bonnier.se';" +
               "/env:Envelope/env:Body/urn:searchResponse/urn1:searchResult/company/companyName/text()" );
		assertion.setExpectedContent( "Bonnier Otto Karl Adam" );
		
		assertNotNull( assertion.assertContent( testResponse, new WsdlSubmitContext( null ), "" ));
	}

   public void testAnyElementTextFail() throws Exception
	{
		assertion.setPath( "declare namespace env='http://schemas.xmlsoap.org/soap/envelope/';" +
				"declare namespace urn='urn:schema:v1:companyservice:applications:bis.bonnier.se';" +
				"declare namespace urn1='urn:v1:companysearch:common:bis.bonnier.se';" +
               "/env:Envelope/env:Body/urn:searchResponse/urn1:searchResult/company/companyName/text()" );
		assertion.setExpectedContent( "Bonnier Otto Karl Adams" );
		
		try
		{
			assertNotNull( assertion.assertContent(testResponse, new WsdlSubmitContext( null ), ""));
			assertFalse( "assertion should have failed", true );
		}
		catch (Exception e)
		{
		}
	}
   
   public void testComplexMatch() throws Exception
	{
   	String response = "<response><book>" + 
		"<bookID>1012</bookID>" +  
		"<author type=\"humanBeing\" href=\"#ID_1\"/>" +
		"<title type=\"string\">Birds</title>" + 
		"</book>" + 
		"<humanBeing id=\"ID_1\">" + 
		"<name>Stephen King</name>" + 
		"</humanBeing></response>";
   	
		assertion.setExpectedContent( "Stephen King"  );
		//assertion.setPath( "//*[@id=substring(//book/bookID[text()='1012']/following-sibling::author/@href,2)]" );
		
		assertion.setPath( "//*[@id=substring(//book/bookID[text()='1012']/following-sibling::author/@href,2)]/name/text()" );
		//assertion.setPath( "//*[@id='ID_1']/name/text()" );
		assertNotNull( assertion.assertContent( response, new WsdlSubmitContext( null ), "" ));
	}

	private String readResource(String string) throws Exception
	{
		BufferedReader reader = new BufferedReader( new InputStreamReader( getClass().getResourceAsStream( string ) ));
		StringBuffer result = new StringBuffer();
		
		String line = reader.readLine();
		while( line != null )
		{
			result.append( line );
			line = reader.readLine();
		}
		
		return result.toString();
	}

	private TestAssertionConfig createConfig( String path, String content ) throws XmlException
	{
		return TestAssertionConfig.Factory.parse( 
				"<con:configuration xmlns:con=\"http://eviware.com/soapui/config\">" +
				"<path>" + path + "</path><content>" + content + "</content></con:configuration>" );
	}
	
}
