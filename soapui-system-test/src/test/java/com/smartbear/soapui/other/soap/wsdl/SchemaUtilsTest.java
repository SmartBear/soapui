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

package com.smartbear.soapui.other.soap.wsdl;

import com.eviware.soapui.impl.wsdl.support.UrlSchemaLoader;
import com.eviware.soapui.impl.wsdl.support.wsdl.UrlWsdlLoader;
import com.eviware.soapui.impl.wsdl.support.xsd.SchemaUtils;
import com.smartbear.soapui.utils.IntegrationTest;
import com.smartbear.soapui.utils.jetty.JettyTestCaseBase;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlObject;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.xml.namespace.QName;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@Category(IntegrationTest.class)
public class SchemaUtilsTest extends JettyTestCaseBase
{

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

		schemaTypes = validatePath( "/wsdls/test6/TestService.wsdl", 4 );
		assertNotNull( schemaTypes.findType( new QName( "http://schemas.eviware.com/TestService/v2/", "TestType" ) ) );
	}

	@Test
	public void testHttpImport7() throws Exception
	{
		SchemaTypeLoader schemaTypes = validate( "http://localhost:" + getPort() + "/wsdls/test7/TestService.wsdl", 4 );
		assertNotNull( schemaTypes.findType( new QName( "http://schemas.eviware.com/TestService/v2/", "TestType" ) ) );

		schemaTypes = validatePath( "/wsdls/test7/TestService.wsdl", 4 );
		assertNotNull( schemaTypes.findType( new QName( "http://schemas.eviware.com/TestService/v2/", "TestType" ) ) );
	}

	@Test
	public void testHttpImport8() throws Exception
	{
		SchemaTypeLoader schemaTypes = validate( "http://localhost:" + getPort() + "/wsdls/test8/TestService.wsdl", 4 );
		assertNotNull( schemaTypes.findType( new QName( "http://schemas.eviware.com/TestService/v2/", "TestType" ) ) );

		schemaTypes = validatePath( "/wsdls/test8/TestService.wsdl", 4 );
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

		url = SchemaUtilsTest.class.getResource( "/wsdls/test9/testcase.wsdl" ).toURI().toURL().toString();
		schemaTypes = SchemaUtils.loadSchemaTypes( url, new UrlWsdlLoader( url ) );
		assertNotNull( schemaTypes.findElement( new QName( "http://testcase/wsdl", "One" ) ) );
		assertNotNull( schemaTypes.findElement( new QName( "http://testcase/wsdl", "Two" ) ) );
	}

	@Test
	public void testWadlImport() throws Exception
	{
		String file = SchemaUtilsTest.class.getResource( "/wadl/YahooSearch.wadl" ).toURI().toURL().toString();
		SchemaTypeLoader types = SchemaUtils.loadSchemaTypes( file, new UrlSchemaLoader( file ) );

		assertNotNull( types.findElement( new QName( "urn:yahoo:yn", "ResultSet" ) ) );
		assertNotNull( types.findElement( new QName( "urn:yahoo:api", "Error" ) ) );
	}


	/*
	Helpers
	 */

	private SchemaTypeLoader validatePath( String wsdlPath, int count ) throws Exception
	{
		return validate( SchemaUtilsTest.class.getResource( wsdlPath ).toURI().toURL().toString(), count );
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
