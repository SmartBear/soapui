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

import com.eviware.soapui.impl.support.definition.export.WsdlDefinitionExporter;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.support.wsdl.WsdlImporter;
import com.eviware.soapui.settings.WsdlSettings;
import com.smartbear.soapui.utils.IntegrationTest;
import com.smartbear.soapui.utils.jetty.JettyTestCaseBase;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Category( IntegrationTest.class )
public class WsdlDefinitionExporterTestCaseTest extends JettyTestCaseBase
{
	private static final String OUTPUT_FOLDER_BASE_PATH = WsdlDefinitionExporterTestCaseTest.class.getResource( "/" ).getPath();

	@Test
	public void shouldSaveDefinition() throws Exception
	{
		replaceInFile( "wsdls/test7/TestService.wsdl", "8082", "" + getPort() );
		replaceInFile( "wsdls/test8/TestService.wsdl", "8082", "" + getPort() );

		testLoader( "http://localhost:" + getPort() + "/wsdls/test1/TestService.wsdl" );
		testLoader( "http://localhost:" + getPort() + "/wsdls/test2/TestService.wsdl" );
		testLoader( "http://localhost:" + getPort() + "/wsdls/test3/TestService.wsdl" );
		testLoader( "http://localhost:" + getPort() + "/wsdls/test4/TestService.wsdl" );
		testLoader( "http://localhost:" + getPort() + "/wsdls/test5/TestService.wsdl" );
		testLoader( "http://localhost:" + getPort() + "/wsdls/test6/TestService.wsdl" );
		testLoader( "http://localhost:" + getPort() + "/wsdls/test7/TestService.wsdl" );
		testLoader( "http://localhost:" + getPort() + "/wsdls/test8/TestService.wsdl" );
		testLoader( "http://localhost:" + getPort() + "/wsdls/testonewayop/TestService.wsdl" );
	}

	private void testLoader( String wsdlUrl ) throws Exception
	{
		WsdlProject project = new WsdlProject();
		project.getSettings().setBoolean( WsdlSettings.CACHE_WSDLS, true );
		WsdlInterface wsdlInterface = WsdlImporter.importWsdl( project, wsdlUrl )[0];

		assertTrue( wsdlInterface.isCached() );

		WsdlDefinitionExporter exporter = new WsdlDefinitionExporter( wsdlInterface );

		String root = exporter.export( OUTPUT_FOLDER_BASE_PATH + "test" + File.separatorChar + "output" );

		WsdlProject project2 = new WsdlProject();
		WsdlInterface wsdl2 = WsdlImporter.importWsdl( project2, new File( root ).toURI().toURL().toString() )[0];

		assertEquals( wsdlInterface.getBindingName(), wsdl2.getBindingName() );
		assertEquals( wsdlInterface.getOperationCount(), wsdl2.getOperationCount() );
		assertEquals( wsdlInterface.getWsdlContext().getInterfaceDefinition().getDefinedNamespaces(), wsdl2
				.getWsdlContext().getInterfaceDefinition().getDefinedNamespaces() );
	}
}
