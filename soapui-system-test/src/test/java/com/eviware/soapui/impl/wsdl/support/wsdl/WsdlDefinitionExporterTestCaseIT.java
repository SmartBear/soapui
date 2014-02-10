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

package com.eviware.soapui.impl.wsdl.support.wsdl;

import com.eviware.soapui.impl.support.definition.export.WsdlDefinitionExporter;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.settings.WsdlSettings;
import com.eviware.soapui.support.JettyTestCaseBase;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class  WsdlDefinitionExporterTestCaseIT extends JettyTestCaseBase
{

	@Test
	public void shouldSaveDefinition() throws Exception
	{
		replaceInFile("test7/TestService.wsdl","8082","" + getPort());
		replaceInFile("test8/TestService.wsdl","8082","" + getPort());

		testLoader( "http://localhost:" + getPort() + "/test1/TestService.wsdl" );
		testLoader( "http://localhost:" + getPort() + "/test2/TestService.wsdl" );
		testLoader( "http://localhost:" + getPort() + "/test3/TestService.wsdl" );
		testLoader( "http://localhost:" + getPort() + "/test4/TestService.wsdl" );
		testLoader( "http://localhost:" + getPort() + "/test5/TestService.wsdl" );
		testLoader( "http://localhost:" + getPort() + "/test6/TestService.wsdl" );
		testLoader( "http://localhost:" + getPort() + "/test7/TestService.wsdl" );
		testLoader( "http://localhost:" + getPort() + "/test8/TestService.wsdl" );
		testLoader( "http://localhost:" + getPort() + "/testonewayop/TestService.wsdl" );
	}

	private void testLoader( String wsdlUrl ) throws Exception
	{
		WsdlProject project = new WsdlProject();
		project.getSettings().setBoolean( WsdlSettings.CACHE_WSDLS, true );
		WsdlInterface wsdlInterface = WsdlImporter.importWsdl( project, wsdlUrl )[0];

		assertTrue( wsdlInterface.isCached() );

		WsdlDefinitionExporter exporter = new WsdlDefinitionExporter( wsdlInterface );

		String root = exporter.export( "test" + File.separatorChar + "output" );

		WsdlProject project2 = new WsdlProject();
		WsdlInterface wsdl2 = WsdlImporter.importWsdl( project2, new File( root ).toURI().toURL().toString() )[0];

		assertEquals( wsdlInterface.getBindingName(), wsdl2.getBindingName() );
		assertEquals( wsdlInterface.getOperationCount(), wsdl2.getOperationCount() );
		assertEquals( wsdlInterface.getWsdlContext().getInterfaceDefinition().getDefinedNamespaces(), wsdl2
				.getWsdlContext().getInterfaceDefinition().getDefinedNamespaces() );
	}
}
