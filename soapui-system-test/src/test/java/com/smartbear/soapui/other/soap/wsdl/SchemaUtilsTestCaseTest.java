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

package com.smartbear.soapui.other.soap.wsdl;

import com.eviware.soapui.impl.wsdl.support.UrlSchemaLoader;
import com.eviware.soapui.impl.wsdl.support.wsdl.UrlWsdlLoader;
import com.eviware.soapui.impl.wsdl.support.xsd.SchemaUtils;
import com.smartbear.soapui.utils.IntegrationTest;
import com.smartbear.soapui.utils.jetty.JettyTestCaseBase;
import junit.framework.JUnit4TestAdapter;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlObject;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.xml.namespace.QName;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@Category( IntegrationTest.class )
public class SchemaUtilsTestCaseTest extends JettyTestCaseBase
{
	public static junit.framework.Test suite()
	{
		return new JUnit4TestAdapter( SchemaUtilsTestCaseTest.class );
	}

	@Test
	public void testFileImport() throws Exception
	{
		validatePath( "/wsdls/test1/TestService.wsdl", 3 );
	}

	@Test
	public void testHttpImport() throws Exception
	{
		validatePath( "/wsdls/test1/TestService.wsdl", 3 );
		validate( "http://localhost:" + getPort() + "/wsdls/test1/TestService.wsdl", 3 );
	}

	@Test
	public void testHttpImport2() throws Exception
	{
		validate( "http://localhost:" + getPort() + "/wsdls/test2/TestService.wsdl", 3 );
		validatePath( "/wsdls/test2/TestService.wsdl", 3 );
	}

	@Test
	public void testHttpImport3() throws Exception
	{
		validate( "http://localhost:" + getPort() + "/wsdls/test3/TestService.wsdl", 3 );
		validatePath( "/wsdls/test3/TestService.wsdl", 3 );

	}

	@Test
	public void testHttpImport4() throws Exception
	{
		validate( "http://localhost:" + getPort() + "/wsdls/test4/TestService.wsdl", 3 );
		validatePath( "/wsdls/test4/TestService.wsdl", 3 );

	}

	@Test
	public void testHttpImport5() throws Exception
	{
		validate( "http://localhost:" + getPort() + "/wsdls/test5/TestService.wsdl", 4 );
		validatePath( "/wsdls/test5/TestService.wsdl", 4 );
	}

	@Test
	public void testHttpImport6() throws Exception
	{
		SchemaTypeLoader schemaTypes = validate( "http://localhost:" + getPort() + "/wsdls/test6/TestService.wsdl", 4 );
		assertNotNull( schemaTypes.findType( new QName( "http://schemas.eviware.com/TestService/v2/", "TestType" ) ) );

		schemaTypes = 		validatePath( "/wsdls/test6/TestService.wsdl", 4 );
		assertNotNull( schemaTypes.findType( new QName( "http://schemas.eviware.com/TestService/v2/", "TestType" ) ) );
	}

	@Test
	public void testHttpImport7() throws Exception
	{
		SchemaTypeLoader schemaTypes = validate( "http://localhost:" + getPort() + "/wsdls/test7/TestService.wsdl", 4 );
		assertNotNull( schemaTypes.findType( new QName( "http://schemas.eviware.com/TestService/v2/", "TestType" ) ) );

		schemaTypes = 		validatePath( "/wsdls/test7/TestService.wsdl", 4 );
		assertNotNull( schemaTypes.findType( new QName( "http://schemas.eviware.com/TestService/v2/", "TestType" ) ) );
	}

	@Test
	public void testHttpImport8() throws Exception
	{
		SchemaTypeLoader schemaTypes = validate( "http://localhost:" + getPort() + "/wsdls/test8/TestService.wsdl", 4 );
		assertNotNull( schemaTypes.findType( new QName( "http://schemas.eviware.com/TestService/v2/", "TestType" ) ) );

		schemaTypes = 		validatePath( "/wsdls/test8/TestService.wsdl", 4 );
		assertNotNull( schemaTypes.findType( new QName( "http://schemas.eviware.com/TestService/v2/", "TestType" ) ) );
	}

	@Test
	public void testHttpImport9() throws Exception
	{
		String url = "http://localhost:" + getPort() + "/wsdls/test9/testcase.wsdl";
		SchemaTypeLoader schemaTypes = SchemaUtils.loadSchemaTypes( url, new UrlWsdlLoader( url ) );
		assertNotNull( schemaTypes.findElement( new QName( "http://testcase/wsdl", "One" ) ) );
		assertNotNull( schemaTypes.findElement( new QName( "http://testcase/wsdl", "Two" ) ) );
		assertNotNull( schemaTypes.findType( new QName( "http://testcase/one", "OneType" ) ) );
		assertNotNull( schemaTypes.findType( new QName( "http://testcase/two", "TwoType" ) ) );

		url = SchemaUtilsTestCaseTest.class.getResource( "/wsdls/test9/testcase.wsdl" ).toURI().toURL().toString();
		schemaTypes = SchemaUtils.loadSchemaTypes( url, new UrlWsdlLoader( url ) );
		assertNotNull( schemaTypes.findElement( new QName( "http://testcase/wsdl", "One" ) ) );
		assertNotNull( schemaTypes.findElement( new QName( "http://testcase/wsdl", "Two" ) ) );
	}

	@Test
	public void testWadlImport() throws Exception
	{
		String file = SchemaUtilsTestCaseTest.class.getResource( "/wadl/YahooSearch.wadl" ).toURI().toURL().toString();
		SchemaTypeLoader types = SchemaUtils.loadSchemaTypes( file, new UrlSchemaLoader( file ) );

		assertNotNull( types.findElement( new QName( "urn:yahoo:yn", "ResultSet" ) ) );
		assertNotNull( types.findElement( new QName( "urn:yahoo:api", "Error" ) ) );
	}


	/*
	Helpers
	 */

	private SchemaTypeLoader validatePath( String wsdlPath, int count ) throws Exception
	{
		return validate( SchemaUtilsTestCaseTest.class.getResource( wsdlPath ).toURI().toURL().toString(), count );
	}

	private SchemaTypeLoader validate( String url, int cnt ) throws Exception
	{
		SchemaTypeLoader schemaTypes = SchemaUtils.loadSchemaTypes( url, new UrlWsdlLoader( url ) );
		Map<String, XmlObject> definitionUrls = SchemaUtils.getDefinitionParts( new UrlWsdlLoader( url ) );

		assertNotNull( schemaTypes );
		assertNotNull( definitionUrls );
		assertEquals( cnt, definitionUrls.size() );

		assertNotNull( schemaTypes.findType( new QName( "http://schemas.eviware.com/TestService/v1/", "PageReference" ) ) );

		return schemaTypes;
	}
}
