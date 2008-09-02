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

package com.eviware.soapui.impl.wsdl.support.wsdl;

import com.eviware.soapui.impl.support.definition.export.WsdlDefinitionExporter;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.settings.WsdlSettings;
import com.eviware.soapui.support.TestCaseWithJetty;
import org.apache.xmlbeans.XmlException;

import java.io.File;
import java.io.IOException;

public class WsdlDefinitionExporterTestCase extends TestCaseWithJetty
{
   public void testSaveDefinition() throws Exception 
   {
   	testLoader("http://localhost:8082/test1/TestService.wsdl");
   	testLoader("http://localhost:8082/test2/TestService.wsdl");
   	testLoader("http://localhost:8082/test3/TestService.wsdl");
   	testLoader("http://localhost:8082/test4/TestService.wsdl");
   	testLoader("http://localhost:8082/test5/TestService.wsdl");
   	testLoader("http://localhost:8082/test6/TestService.wsdl");
   	testLoader("http://localhost:8082/test7/TestService.wsdl");
   	testLoader("http://localhost:8082/test8/TestService.wsdl");
   	testLoader("http://localhost:8082/testonewayop/TestService.wsdl");
   }

	private void testLoader(String wsdlUrl) throws XmlException, IOException, Exception
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
   	assertEquals( wsdlInterface.getWsdlContext().getInterfaceDefinition().getDefinedNamespaces(), wsdl2.getWsdlContext().getInterfaceDefinition().getDefinedNamespaces() );
	}
}
