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

package com.eviware.soapui.impl.wsdl.support;

import com.eviware.soapui.impl.wsdl.support.wsdl.UrlWsdlLoader;
import com.eviware.soapui.impl.wsdl.support.xsd.SchemaUtils;
import com.eviware.soapui.support.TestCaseWithJetty;
import junit.framework.JUnit4TestAdapter;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlObject;
import org.junit.Test;

import javax.xml.namespace.QName;
import java.io.File;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class SchemaUtilsTestCaseIT extends TestCaseWithJetty
{
	public static junit.framework.Test suite()
	{
		return new JUnit4TestAdapter( SchemaUtilsTestCaseIT.class );
	}

	@Test
	public void testFileImport() throws Exception
	{
		File file = new File( "src" + File.separator + "test" + File.separator + "resources" + File.separator + "test1" + File.separator + "TestService.wsdl" );
		validate( file.toURI().toURL().toString(), 3 );
	}

	@Test
	public void testHttpImport() throws Exception
	{
		validate( "http://localhost:8082/test1/TestService.wsdl", 3 );
		validate( new File( "src" + File.separator + "test" + File.separator + "resources" + File.separator + "test1" + File.separator + "TestService.wsdl" ).toURI().toURL().toString(), 3 );
	}

	@Test
	public void testHttpImport2() throws Exception
	{
		validate( "http://localhost:8082/test2/TestService.wsdl", 3 );
		validate( new File( "src" + File.separator + "test" + File.separator + "resources" + File.separator + "test2" + File.separator + "TestService.wsdl" ).toURI().toURL().toString(), 3 );
	}

	@Test
	public void testHttpImport3() throws Exception
	{
		validate( "http://localhost:8082/test3/TestService.wsdl", 3 );
		validate( new File( "src" + File.separator + "test" + File.separator + "resources" + File.separator + "test3" + File.separator + "TestService.wsdl" ).toURI().toURL().toString(), 3 );
	}

	@Test
	public void testHttpImport4() throws Exception
	{
		validate( "http://localhost:8082/test4/TestService.wsdl", 3 );
		validate( new File( "src" + File.separator + "test" + File.separator + "resources" + File.separator + "test4" + File.separator + "TestService.wsdl" ).toURI().toURL().toString(), 3 );
	}

	@Test
	public void testHttpImport5() throws Exception
	{
		validate( "http://localhost:8082/test5/TestService.wsdl", 4 );
		validate( new File( "src" + File.separator + "test" + File.separator + "resources" + File.separator + "test5" + File.separator + "TestService.wsdl" ).toURI().toURL().toString(), 4 );
	}

	@Test
	public void testHttpImport6() throws Exception
	{
		SchemaTypeLoader schemaTypes = validate( "http://localhost:8082/test6/TestService.wsdl", 4 );
		assertNotNull( schemaTypes.findType( new QName( "http://schemas.eviware.com/TestService/v2/", "TestType" ) ) );

		schemaTypes = validate( new File( "src" + File.separator + "test" + File.separator + "resources" + File.separator + "test6" + File.separator + "TestService.wsdl" ).toURI().toURL().toString(), 4 );
		assertNotNull( schemaTypes.findType( new QName( "http://schemas.eviware.com/TestService/v2/", "TestType" ) ) );
	}

	@Test
	public void testHttpImport7() throws Exception
	{
		SchemaTypeLoader schemaTypes = validate( "http://localhost:8082/test7/TestService.wsdl", 4 );
		assertNotNull( schemaTypes.findType( new QName( "http://schemas.eviware.com/TestService/v2/", "TestType" ) ) );

		schemaTypes = validate( new File( "src" + File.separator + "test" + File.separator + "resources" + File.separator + "test7" + File.separator + "TestService.wsdl" ).toURI().toURL().toString(), 4 );
		assertNotNull( schemaTypes.findType( new QName( "http://schemas.eviware.com/TestService/v2/", "TestType" ) ) );
	}

	@Test
	public void testHttpImport8() throws Exception
	{
		SchemaTypeLoader schemaTypes = validate( "http://localhost:8082/test8/TestService.wsdl", 4 );
		assertNotNull( schemaTypes.findType( new QName( "http://schemas.eviware.com/TestService/v2/", "TestType" ) ) );

		schemaTypes = validate( new File( "src" + File.separator + "test" + File.separator + "resources" + File.separator + "test8" + File.separator + "TestService.wsdl" ).toURI().toURL().toString(), 4 );
		assertNotNull( schemaTypes.findType( new QName( "http://schemas.eviware.com/TestService/v2/", "TestType" ) ) );
	}

	@Test
	public void testHttpImport9() throws Exception
	{
		String url = "http://localhost:8082/test9/testcase.wsdl";
		SchemaTypeLoader schemaTypes = SchemaUtils.loadSchemaTypes( url, new UrlWsdlLoader( url ) );
		assertNotNull( schemaTypes.findElement( new QName( "http://testcase/wsdl", "One" ) ) );
		assertNotNull( schemaTypes.findElement( new QName( "http://testcase/wsdl", "Two" ) ) );
		assertNotNull( schemaTypes.findType( new QName( "http://testcase/one", "OneType" ) ) );
		assertNotNull( schemaTypes.findType( new QName( "http://testcase/two", "TwoType" ) ) );

		url = new File( "src" + File.separator + "test" + File.separator + "resources" + File.separator + "test9" + File.separator + "testcase.wsdl" ).toURI().toURL().toString();
		schemaTypes = SchemaUtils.loadSchemaTypes( url, new UrlWsdlLoader( url ) );
		assertNotNull( schemaTypes.findElement( new QName( "http://testcase/wsdl", "One" ) ) );
		assertNotNull( schemaTypes.findElement( new QName( "http://testcase/wsdl", "Two" ) ) );
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

	@Test
	public void testWadlImport() throws Exception
	{
		String file = new File( "src" + File.separator + "test" + File.separator + "resources" + File.separator + "wadl" + File.separator + "YahooSearch.wadl" ).toURI().toURL().toString();
		SchemaTypeLoader types = SchemaUtils.loadSchemaTypes( file, new UrlSchemaLoader( file ) );

		assertNotNull( types.findElement( new QName( "urn:yahoo:yn", "ResultSet" ) ) );
		assertNotNull( types.findElement( new QName( "urn:yahoo:api", "Error" ) ) );
	}
}
